import json

import numpy as np

from Autoencoder import decoder
from helper_files.AutoEncoderHelper import inverse_scale, get_weights_from_json, determine_layer_sizes_from_weights


def initialize_decoder(number_of_lines):
    # Structure of the decoder
    layer_sizes = [
        int(11 * number_of_lines * 0.2),
        int(11 * number_of_lines * 0.8),
        11 * number_of_lines
    ]
    return layer_sizes


def send_weights(data, decoder_model):

    decoder_weights_as_string = data[3]
    decoder_weights_as_list = get_weights_from_json(decoder_weights_as_string)

    print("Decoder Weights from java:", decoder_weights_as_list)
    print("New Decoder weights len: ", len(decoder_weights_as_list))
    print("Decoder layer sizes: ", len(decoder_model.layer_sizes))

    # determine number of neurons per layer
    list_of_new_layer_sizes = determine_layer_sizes_from_weights(
        weights_as_list=decoder_weights_as_list)

    print("layer_sizes: ", decoder_model.layer_sizes)
    print("listOfNewLayerSizes: ", list_of_new_layer_sizes)

    weights_have_more_layer = len(decoder_weights_as_list) > len(decoder_model.layer_sizes)
    if weights_have_more_layer:
        print("Decoder Upgraded")
        decoder_model = decoder.Decoder(list_of_new_layer_sizes)

    # Set received weights
    return decoder_weights_as_list


def decode(decoder_model, encoded_data_as_list, number_of_lines):
    # Decode data
    decoded_data = decoder_model.decode(np.array(encoded_data_as_list))

    print("Decoded Data:", decoded_data)

    # Reshape the data set to n = number_of_line lines
    decoded_data = decoded_data.reshape((number_of_lines, -1))

    print("Reshaped numpy array:", decoded_data)

    # Inverse scale decoded data
    decoded_data = inverse_scale(decoded_data)

    decoded_data = decoded_data.tolist()

    print("Decoded Data as List:", decoded_data)

    # Round every Value of the decoded Data
    for i in range(len(decoded_data)):
        decoded_data[i][0] = int(round(decoded_data[i][0], 0))
        for j in range(1, len(decoded_data[i])):
            decoded_data[i][j] = round(decoded_data[i][j], 3)

    print("Rounded Data:", decoded_data)

    decoded_data = json.dumps(decoded_data)

    decoded_data_as_bytes = bytes(decoded_data + "\n", encoding="utf-8")
    return decoded_data_as_bytes
