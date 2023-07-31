import json

import numpy as np
import tensorflow as tf
from pandas import read_csv, DataFrame

from helper_files.Scaling import absolute_min_max


def get_weights_from_json(data):
    data_decoded = json.loads(data)

    # For each element the weight will be extracted
    for i in range(len(data_decoded)):
        for j in range(len(data_decoded[i])):
            data_decoded[i][j] = np.array(data_decoded[i][j])

    return data_decoded


def weigths_to_json(data):
    """
        Convert the weights to a json file

    :param data: weigths of the decoder
    :return: data_encoded
    """
    # Each weight will put into a list
    for i in range(len(data)):
        for j in range(len(data[i])):
            data[i][j] = data[i][j].tolist()

    # Encode list as json
    data_encoded = json.dumps(data)

    return data_encoded


def get_data_frame(path_of_csv):
    """
    Loads a csv file from a given path and returns a DataFrame

    :param path_of_csv: path of csv file
    :return: dataFrameWithoutSnowdepth The dataframe that contains the csv without Snowdepth
    """

    weather_data = read_csv(path_of_csv, delimiter=";")

    data_frame_of_weather_data = DataFrame(data=weather_data)

    # "SNOWDEPTH" collum can be ignored since agri4cast will remove the variable "SNOWDEPTH" of
    # the Dataset "Gridded Agro-Meteorological Data in Europe" completley, for various reasons.
    # For more information visit the GitLab wiki of the Project "AutoEncoder"
    data_frame_without_snowdepth = data_frame_of_weather_data.iloc[:, :-1]

    return data_frame_without_snowdepth


def scale(data: np.ndarray) -> np.ndarray:
    """
    Scale data between 0 and 1

    :param data: Numpy Array with data
    :return: Scaled Data
    """
    data_copy = data.copy()

    for i, feature in enumerate(absolute_min_max.values()):
        x_min, x_max = feature
        data_copy[:, i] = (data_copy[:, i] - x_min) / (x_max - x_min)

    # Cut all outliners larger than 1 and lower than 0 to 1 and 0
    data_copy.clip(0, 1)

    return data_copy


def inverse_scale(data: np.ndarray) -> np.ndarray:
    """
        Scale data back to original format

        :param data: Numpy Array with data
        :return: Rescaled Data
        """
    data_copy = data.copy()

    for i, feature in enumerate(absolute_min_max.values()):
        x_min, x_max = feature
        data_copy[:, i] = data_copy[:, i] * (x_max - x_min) + x_min

    return data_copy


def init_gpu():
    """
    Set up GPU memory growth option to avoid memory error
    """
    gpus = tf.config.experimental.list_physical_devices('GPU')
    if gpus:
        try:
            for gpu in gpus:
                tf.config.experimental.set_memory_growth(gpu, True)
        except RuntimeError as e:
            print(e)


def determine_layer_sizes_from_weights(weights_as_list: list) -> list:
    """
        Determines number of Neurons per layer in list of weights
    :param weights_as_list: list that contains the weights of the model
    :return: list which contains starting from the first layer how much neurons the layers contain
    """
    list_of_new_layer_sizes = []
    number_of_layers_in_new_model = len(weights_as_list)
    layer_number_in_list = list(range(number_of_layers_in_new_model))

    for (layer_Number, weights) in zip(layer_number_in_list, weights_as_list):

        not_first_or_last_layer = not (
                layer_Number == layer_number_in_list[0] | layer_Number == layer_number_in_list[-1])

        if not_first_or_last_layer:
            for array in weights[:1]:
                list_of_new_layer_sizes.append(len(array))
        else:
            for array in weights:
                list_of_new_layer_sizes.append(len(array))

    return list_of_new_layer_sizes
