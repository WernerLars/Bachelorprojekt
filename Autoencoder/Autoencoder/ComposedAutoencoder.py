from os import remove
from os.path import exists
from typing import List

import numpy as np
from tensorflow.keras import Model

from Autoencoder.Autoencoder import AutoEncoder, get_weights


class ComposedAutoEncoder:
    def __init__(self, layer_sizes: List[int] = None):
        """
        A class which manages 2 composed_autoencoder objects.
        The composed_autoencoder object is improved by running training and the reference
        model is only changed when the weights are synchronized.

        :param layer_sizes: List of integer. Each integer represent a number of neurons in a layer until the smallest
        mid layer.
        """
        if layer_sizes is None:
            layer_sizes = [11, 10, 6]

        # Create composed_autoencoder and reference object
        self.__autoencoder = AutoEncoder(layer_sizes)
        self.__reference_ae = AutoEncoder(layer_sizes)
        self.__autoencoder_with_better_compression: AutoEncoder = None

        # Synchronize weights of both composed_autoencoder
        self.__reference_ae.set_weights(get_weights(self.__autoencoder))
        self.__model_with_better_compression_in_build = False

    def get_encoder(self) -> Model:
        """
        Get encoder part of the reference composed_autoencoder

        :return: Encoder Model
        """
        return self.__reference_ae.get_encoder()

    def get_decoder(self) -> Model:
        """
        Get decoder part of the reference composed_autoencoder

        :return: Decoder Model
        """
        return self.__reference_ae.get_decoder()

    def load_models(self, autoencoder_path: str = None, reference_path: str = None):
        """
        Load complete model from a .h5 file and override actual composed_autoencoder and reference model

        :param autoencoder_path: Path to .h5 file of the composed_autoencoder model
        :param reference_path: Path to .h5 file of the reference model
        """
        if autoencoder_path is not None:
            self.__autoencoder.load_model(autoencoder_path)

            # if model with better compression exists load it too
            path_of_model_with_better_compression = autoencoder_path.replace(".h5", "_Better_Compression.h5")

            try:
                self.__autoencoder_with_better_compression = AutoEncoder()
                self.__autoencoder_with_better_compression.load_model(path_of_model_with_better_compression)
                self.set_model_with_better_compression_in_build(boolean_value=True)
                print("Model with better compression  is loaded...")
            except Exception:
                self.__autoencoder_with_better_compression = None
                self.set_model_with_better_compression_in_build(boolean_value=False)
                print("Model with better compression  does not exists therefore it is not loaded...")

        if reference_path is not None:
            self.__reference_ae.load_model(reference_path)

    def save_models(self, autoencoder_path: str = None, reference_path: str = None):
        """
        Save autoencoder model an reference model to a .h5 file.

        :param autoencoder_path: Name/Path of .h5 file of the composed_autoencoder model.
        If None no autoencoder model will not be saved.
        :param reference_path: Name/Path of .h5 file of the reference model.
        If None no reference model will not be saved.
        """
        if autoencoder_path is not None:
            self.__autoencoder.save_model(autoencoder_path)
        if reference_path is not None:
            self.__reference_ae.save_model(reference_path)

    def better_than_reference(self, data: np.ndarray, factor: float, verbose=True) -> bool:
        """
        Evaluate both autoencoders and if the trained composed_autoencoder is factor times better than the reference
        synchronize (copy) weights

        :param data: Data for evaluation
        :param factor: How many times must reference better than composed_autoencoder
        :param verbose: If True additional output
        :return: Return True if changes where made to the reference model. Otherwise False
        """
        # Get losses for each model on given data
        loss_ae = self.__autoencoder.evaluate(data)
        loss_ref = self.__reference_ae.evaluate(data)
        # Calculate the factor how many times reference is better than composed_autoencoder
        times_better = loss_ref / loss_ae

        # Print this factor
        if verbose:
            print('Trained model is {} times better than reference'.format(round(times_better, 2)))

        return times_better >= factor

    def train(self, train_data: np.ndarray, checkpoint_path: str = None):
        """
        Train composed_autoencoder model on data

        :param train_data: Scaled data for training as Numpy array
        :param checkpoint_path: Path for saving the model
        """
        self.__autoencoder.train(train_data, checkpoint_path=checkpoint_path)

        if self.__autoencoder_with_better_compression is not None:
            self.__autoencoder_with_better_compression.train(train_data, checkpoint_path=checkpoint_path.replace(".h5",
                                                                                                                 "_Better_Compression.h5"))

    def model_with_better_compression_in_build(self):
        return self.__model_with_better_compression_in_build

    def set_model_with_better_compression_in_build(self, boolean_value):
        self.__model_with_better_compression_in_build = boolean_value

    def get_Deviation_From_Original_Data_Autoencoder(self, data, not_scaled_data, ):
        """
         Get the deviation of +/- x  of unscaled data from the predicted values

        :param data: data which wil be used to predict
        :param not_scaled_data: data unshaped and unscaled
        :return: deviation of data from unscaled data
        """
        return self.__autoencoder.get_deviation_from_original_data(data, not_scaled_data)

    def get_Deviation_From_Original_Data_AE_better_compression(self, data, not_scaled_data):
        """
         Get the deviation of +/- x  of unscaled data from the predicted values

        :param data: data which wil be used to predict
        :param not_scaled_data: data unshaped and unscaled
        :return: deviation of data from unscaled data
        """
        return self.__autoencoder_with_better_compression.get_deviation_from_original_data(data, not_scaled_data)

    def update_weights_of_autoencoder_with_better_compression(self):
        """
            Update the weights of autoencoder_with_better_compression with weights from the normal autoencoder
        """

        for layer_autoencoder in self.__autoencoder.layers:

            if type(layer_autoencoder) == Model:

                my_model = layer_autoencoder

                if my_model.name == "encoder":

                    # set weights and trainability of all the layers in the encoder that are not new
                    for (layer_number, layer) in enumerate(my_model.layers, 0):
                        layer_weights = layer.get_weights()

                        self.__autoencoder_with_better_compression.get_encoder().get_layer(
                            index=layer_number).set_weights(layer_weights)
                        self.__autoencoder_with_better_compression.get_encoder().get_layer(
                            index=layer_number).trainable = False


                elif my_model.name == "decoder":

                    # integer to skip the first to layers from the top of the encoder because it is not relevant to
                    # set the weights there because input layer odes not need weights and the following layer of the
                    # inputlayer is the only one to train
                    start_with_third_layer_from_top = 2

                    # set weights and trainability of all the layers in the decoder that are not new
                    for (layer_number, layer) in enumerate(my_model.layers[1:], start_with_third_layer_from_top):
                        layer_weights = layer.get_weights()

                        self.__autoencoder_with_better_compression.get_decoder().get_layer(
                            index=layer_number).set_weights(layer_weights)
                        self.__autoencoder_with_better_compression.get_decoder().get_layer(
                            index=layer_number).trainable = False

    def create_autoencoder_better_compression(self, number_of_neurons_to_decrease):
        """
        Creates a new Model with higher compression i.e. adds two Denselayer with one less Node

        """
        layer_sizes_for_new_autoencoder = self.__autoencoder.layer_sizes.copy()

        # determine number of neurons in the new layer
        size_of_next_smaller_layer = (layer_sizes_for_new_autoencoder[-1] - number_of_neurons_to_decrease)

        # add number of neurons for the new layer in the list
        layer_sizes_for_new_autoencoder.append(size_of_next_smaller_layer)

        # create a new autoencoder with the layers
        self.__autoencoder_with_better_compression = AutoEncoder(layer_sizes_for_new_autoencoder)

        self.update_weights_of_autoencoder_with_better_compression()

        # compile model because trainability of layers has changed
        self.__autoencoder_with_better_compression.compile_model()

        self.set_model_with_better_compression_in_build(boolean_value=True)

        print("Autoencoder with better Compression created")

    def better_compression_possible(self, number_of_neurons_to_decrease) -> bool:

        # get layer sizes from old autoencoder
        layer_sizes_for_new_autoencoder = self.__autoencoder.layer_sizes

        # Specify how many Neurons less there should be in the new layer of the new Autoencoder
        # remember the Structure of the Autoencoder is synchronous!
        number_of_neurons_last_layer = number_of_neurons_to_decrease

        # check if the middle layer that is responsible for compression has enough layers
        middle_layer_has_enough_neurons = layer_sizes_for_new_autoencoder[-1] > number_of_neurons_last_layer

        return middle_layer_has_enough_neurons

    def save_autoencoder_with_better_compression(self, autoencoder_path: str = None):
        """
            Save autoencoder_with_better_compression model to a .h5 file.
        :param autoencoder_path:
        :return:
        """

        if autoencoder_path is not None:
            self.__autoencoder_with_better_compression.save_model(
                autoencoder_path.replace(".h5", "_Better_Compression.h5"))

    # TODO: delete later
    def autoencoder_predict(self, data):
        """
            Let the autoencoder Predict data
        :param data: Data to predict
        :return: Predicted Data
        """
        return self.__autoencoder.predict(data)

    def set_model_with_better_compression_as_new_autoencoder(self, path_to_load_and_save_model):

        autoencoder_compression_path = path_to_load_and_save_model.replace(".h5", "_Better_Compression.h5")

        # set new autoencoder with better compression as new model
        self.__autoencoder.load_model(autoencoder_compression_path)
        self.__autoencoder_with_better_compression = None

        self.set_model_with_better_compression_in_build(boolean_value=False)

        # save new Autoencoder
        self.save_models(autoencoder_path=path_to_load_and_save_model)

        # delete old "model with better compression file"
        if exists(autoencoder_compression_path):
            remove(autoencoder_compression_path)

        # set the model trainable again
        self.__autoencoder.set_model_trainable()

        print("Autoencoder with better compression is set")
