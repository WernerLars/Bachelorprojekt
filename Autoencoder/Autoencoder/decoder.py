from typing import List
import numpy as np
from tensorflow.keras import Input, Model
from tensorflow.keras.layers import Dense
from tensorflow.python.keras.models import load_model


class Decoder:
    def __init__(self, layer_sizes: List[int] = None):
        # Define new model layers if none exist
        if layer_sizes is None:
            layer_sizes = [6, 10, 11]

        decoder_input_size, *decoder_layer_sizes = layer_sizes

        # Create decoder model
        decoder_input = x = Input(shape=(decoder_input_size,))
        for size in decoder_layer_sizes:
            x = Dense(size, activation='sigmoid')(x)
        self.__decoder = Model(decoder_input, x, name="decoder")
        self.layer_sizes = layer_sizes

    def set_weights(self, weights: List[np.ndarray]):
        """
        Set weights of the decoder

        :param weights: List of weights
        """
        # Copy weights to layers
        for layer_dec, layer_weights in zip(self.__decoder.layers, weights):
            layer_dec.set_weights(layer_weights)

    def save_model(self, file_path: str):
        """
        Save current model to file

        :param file_path: Name/Path of the .h5 file
        """
        self.__decoder.save(file_path)

    def load_model(self, file_path: str):
        """
        Load complete model from a .h5 file and override actual model

        :param file_path: Path to .h5 file
        """
        self.__decoder = load_model(file_path, compile=False)
        self.layer_sizes = self.__get_layer_sizes_from_model

    def decode(self, data: np.ndarray) -> np.ndarray:
        """
        Decode given data

        :param data: Data for decoding
        """
        return self.__decoder.predict(data)

    @property
    def __get_layer_sizes_from_model(self) -> list:
        """
            Determine the size of the Layers

        :param model: Model to determine size of the layers
        :return: Size of layers
        """

        layer_sizes_of_model = []

        for layer_autoencoder in self.__decoder.layers:

            if type(layer_autoencoder) != Input:
                layer_sizes_of_model.append(layer_autoencoder.output_shape)

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
