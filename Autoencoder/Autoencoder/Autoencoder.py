from tensorflow.keras.layers import Dense
from tensorflow.keras.callbacks import ModelCheckpoint
from tensorflow.keras.models import load_model
from tensorflow.keras import Model, Input
import numpy as np
from typing import List, Union
from math import sqrt
from helper_files.AutoEncoderHelper import inverse_scale
from tensorflow.keras.losses import MeanSquaredError


class AutoEncoder:
    def __init__(self, layer_sizes: List[int] = None):
        # Define the model layers
        if layer_sizes is None:
            layer_sizes = [11, 10, 6]

        encoder_input_size, *encoder_layer_sizes = layer_sizes
        decoder_input_size, *decoder_layer_sizes = layer_sizes[::-1]

        # Create encoder model
        encoder_input = x = Input(shape=(encoder_input_size,))
        for size in encoder_layer_sizes:
            x = Dense(size, activation='sigmoid')(x)
        self.__encoder = Model(encoder_input, x, name="encoder")

        # Create decoder model
        decoder_input = x = Input(shape=(decoder_input_size,))
        for size in decoder_layer_sizes:
            x = Dense(size, activation='sigmoid')(x)
        self.__decoder = Model(decoder_input, x, name="decoder")

        # Create composed_autoencoder model
        autoencoder_input = Input(shape=(encoder_input_size,))
        encoded_data = self.__encoder(autoencoder_input)
        decoded_data = self.__decoder(encoded_data)
        self.__autoencoder = Model(autoencoder_input, decoded_data, name="composed_autoencoder")

        # Compile model
        self.__autoencoder.compile(optimizer='adam', loss='mse')

        # Set layers_sizes attribute
        self.layer_sizes = layer_sizes

        # Set layers attribute
        self.layers = self.__autoencoder.layers

    def get_encoder(self) -> Model:
        """
        Get encoder part of the composed_autoencoder

        :return: Encoder Model
        """
        return self.__encoder

    def get_decoder(self) -> Model:
        """
        Get decoder part of the composed_autoencoder

        :return: Decoder Model
        """
        return self.__decoder

    def load_model(self, file_path: str):
        """
        Load complete model from a .h5 file and override actual model

        :param file_path: Path to .h5 file
        """
        self.__autoencoder = load_model(file_path, compile=True)
        self.__encoder = self.__autoencoder.get_layer('encoder')
        self.__decoder = self.__autoencoder.get_layer('decoder')
        self.layers = self.__autoencoder.layers
        self.layer_sizes = self.__get_layer_sizes_from_model()

    def save_model(self, file_path: str):
        """
        Save current model to file

        :param file_path: Name/Path of the .h5 file
        """
        self.__autoencoder.save(file_path)

    def load_weights(self, file_path: str):
        """
        Load weights from .h5 file

        :param file_path: Path of the .h5 file
        """
        self.__autoencoder.load_weights(file_path)

    def set_weights(self, weights: List[np.ndarray]):
        """
        Set weights of the autoencoder

        :param weights: List of weights
        """
        for layer_ae, weight in zip(self.__autoencoder.layers, weights):
            layer_ae.set_weights(weight)

    def train(self, train_data: np.ndarray, epochs: int = 10, batch_size: int = 1, checkpoint_path: str = None):
        """
        Train model on data

        :param train_data: Scaled data for training as Numpy array
        :param epochs: Number of epochs
        :param batch_size: Number of samples for each batch
        :param checkpoint_path: Path for saving the model
        """
        callbacks = []

        # Check if checkpoints should be created
        if checkpoint_path is not None:
            checkpoint = ModelCheckpoint(filepath=checkpoint_path,
                                         monitor='val_loss',
                                         mode='min',
                                         save_best_only=True)

            # Add checkpoints to callback
            callbacks += [checkpoint]

        # Train model
        self.__autoencoder.fit(train_data,
                               train_data,
                               epochs=epochs,
                               batch_size=batch_size,
                               validation_data=(train_data, train_data),
                               callbacks=callbacks)

        # Load best model
        if checkpoint_path is not None:
            self.__autoencoder.load_weights(checkpoint_path)

    def predict(self, data: np.ndarray) -> np.ndarray:
        """
        Predict output for input data

        :param data: Input data as Numpy array
        :return: Numpy array of predictions
        """
        return self.__autoencoder.predict(data)

    def evaluate(self, data: np.ndarray) -> float:
        """
        Apply metric on model and return the MSE loss of the model

        :param data: Data for evaluation as Numpy array
        :return: Loss of the model on given data
        """
        return self.__autoencoder.evaluate(data, data, verbose=0)

    def get_deviation_from_original_data(self, scaled_data_to_predict, not_scaled_data):

        """
         Get the average deviation of unscaled data from the predicted values




        :param scaled_data_to_predict: :param data: data which wil be used to predict :param not_scaled_data: data
        unshaped and unscaled :return: deviation of data from unscaled data e.g. 3 can be returned and that means the
        average deviation from original data is +-3

        """
        number_of_lines = len(not_scaled_data)

        predicted_values = self.__autoencoder.predict(scaled_data_to_predict)

        # Reshape the data set to n = number_of_line lines
        decoded_data = predicted_values.reshape((number_of_lines, -1))

        # Inverse scale decoded data
        decoded_data = inverse_scale(decoded_data)

        decoded_data = decoded_data.tolist()

        # Round every Value of the decoded Data
        for i in range(len(decoded_data)):
            decoded_data[i][0] = int(round(decoded_data[i][0], 0))
            for j in range(1, len(decoded_data[i])):
                decoded_data[i][j] = round(decoded_data[i][j], 3)

        mse = MeanSquaredError()

        mse = mse(not_scaled_data.tolist(), decoded_data).numpy()

        # Loss of the model after eval is here the average deviation from data
        # E.g. the average deviation can be +/- 2
        root_mean_square_error = sqrt(mse)

        return root_mean_square_error

    def set_model_trainable(self):
        """
            Set the whole model trainable
        :return:
        """

        self.__autoencoder.trainable = True
        self.compile_model()

    def compile_model(self):
        self.__autoencoder.compile(optimizer='adam', loss='mse')

    def print_summary(self):
        print(self.__encoder.summary())
        print(self.__decoder.summary())
        print(self.__autoencoder.summary())

    def __get_layer_sizes_from_model(self) -> list:

        """
            Determine the size of the Layers

        :param model: Model to determine size of the layers
        :return: Size of layers
        """

        layer_sizes_of_model = []

        for layer_autoencoder in self.__autoencoder.layers:

            if type(layer_autoencoder) == Model:

                my_model = layer_autoencoder

                if my_model.name == "encoder":

                    for layer in my_model.layers:

                        if type(layer) != Input:
                            layer_sizes_of_model.append(layer.output_shape)

        # needed because layer_sizes_of_model would e.g be equal to [[(None, 55)], (None, 44), (None, 11), (None,
        # 9)] and the first element has to be extracted from the list so that to goal is to have
        # new_layer_sizes_of_model==[55,44,11,9]
        new_layer_sizes_of_model = []
        for output_shape in layer_sizes_of_model:

            if type(output_shape) == list:

                number_of_neurons = (output_shape[0])[1]

                new_layer_sizes_of_model.append(number_of_neurons)
            else:
                new_layer_sizes_of_model.append(output_shape[1])

        return new_layer_sizes_of_model


def get_weights(model: Union[Model, AutoEncoder]) -> List[np.ndarray]:
    """
    Get weights of a model as a list of Numpy arrays

    :param model: Model from which the weights are to be saved
    :return: Weights as list of Numpy arrays
    """
    return [layer.get_weights() for layer in model.layers]
