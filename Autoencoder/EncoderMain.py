import sys
import socket
import json
import simplejson
from queue import Queue
from threading import Thread
import numpy as np
from helper_files.AutoEncoderHelper import weigths_to_json, init_gpu, scale
from Autoencoder.Autoencoder import get_weights
from Autoencoder.ComposedAutoencoder import ComposedAutoEncoder


# check if data was sent as a group that appears in our grid_set_list
def check_grouping(inc_data):
    f = open('data/grid_set_list.txt', 'r')
    grid_set_list = simplejson.load(f)

    gridnos = []
    for i in inc_data:
        gridnos.append(i[0])
    print(gridnos)

    index = -1
    for idx, grid_set in enumerate(grid_set_list):
        if all(gridno in grid_set for gridno in gridnos):
            index = idx

    if index == -1:
        print('Incoming data was not grouped correctly.')
        return False
    else:
        print('Incoming data belongs to group ' + str(index))
        return True


# call these functions with a sorted list of incoming data (ascending by gridno)
# returns corresponding model's filename
def get_trained_model(inc_data):
    return 'models/trained_models/' + str(int(inc_data[0][0])) + '.h5'


def get_trained_model_by_gridno(smallest_grid_no):
    return 'models/trained_models/' + str(int(smallest_grid_no)) + '.h5'


def get_reference_model(inc_data):
    return 'models/reference_models/' + str(int(inc_data[0][0])) + '.h5'


def get_reference_model_by_gridno(smallest_grid_no):
    return 'models/reference_models/' + str(int(smallest_grid_no)) + '.h5'


def thread_decode(data_queue, composed_autoencoder: ComposedAutoEncoder):
    while True:
        while not (data_queue.empty()):
            data_elem = data_queue.get()
            not_scaled_data = data_elem.copy()

            smallest_grid_number = int(data_elem[0][0])

            # Scale data
            data_elem = scale(data_elem)

            # Reshape the numpy Array to an 2-Dimensional Array for input of the encoder
            data_elem = data_elem.reshape((1, -1))

            print("Flatten and scaled numpy array:", data_elem)

            composed_autoencoder.train(data_elem, get_trained_model_by_gridno(smallest_grid_number))

            max_avg_abs_deviation = 2

            if composed_autoencoder.model_with_better_compression_in_build():
                print("Deviation of data from the  Autoencoder with better compression +/-: ",
                      composed_autoencoder.get_Deviation_From_Original_Data_AE_better_compression(data_elem,
                                                                                                  not_scaled_data))
                if composed_autoencoder.get_Deviation_From_Original_Data_AE_better_compression(data_elem,
                                                                                               not_scaled_data) <= max_avg_abs_deviation:
                    composed_autoencoder.set_model_with_better_compression_as_new_autoencoder(
                        get_trained_model_by_gridno(smallest_grid_number))

            number_of_neurons_to_decrease = 2

            # Create model with better compression
            print("Deviation of data from the  Autoencoder +/-: ",
                  composed_autoencoder.get_Deviation_From_Original_Data_Autoencoder(data_elem, not_scaled_data))
            if composed_autoencoder.get_Deviation_From_Original_Data_Autoencoder(data_elem,
                                                                                 not_scaled_data) <= max_avg_abs_deviation and not composed_autoencoder.model_with_better_compression_in_build() and composed_autoencoder.better_compression_possible(
                number_of_neurons_to_decrease):
                composed_autoencoder.create_autoencoder_better_compression(number_of_neurons_to_decrease)

                # save model directly otherwise it will never be saved because in the next
                # turn we possibly load new models
                composed_autoencoder.save_autoencoder_with_better_compression(
                    autoencoder_path=get_trained_model_by_gridno(smallest_grid_number))

            # How many times must trained_ae perform better than reference_ae to distribute weights
            # TODO: set realistic factor
            factor = 0.01

            if composed_autoencoder.better_than_reference(data_elem, factor):
                # Load trained model which is better than the reference model
                composed_autoencoder.load_models(reference_path=get_trained_model_by_gridno(smallest_grid_number))

                # Save a copy of the reference model
                composed_autoencoder.save_models(reference_path=get_reference_model_by_gridno(smallest_grid_number))

                # Encode scaled data_elem
                encoder = composed_autoencoder.get_encoder()

                encoded_data = encoder.predict(data_elem)

                # Prepare message for Java
                encoded_data_as_json = json.dumps(encoded_data.tolist())
                weights_as_json = weigths_to_json(get_weights(composed_autoencoder.get_decoder()))

                # Print encoded data_elem and weights
                print("EncodedData:", encoded_data_as_json)
                print("Weights:", weights_as_json)

                sending_message_as_json = json.dumps([1, smallest_grid_number,
                                                      encoded_data_as_json, weights_as_json])

                message_length = len(sending_message_as_json) + len("|")
                message_length += len(str(message_length))

                sending_message = bytes(str(message_length) + "|" + sending_message_as_json + "\n",
                                        encoding="utf-8")

                # Update the weights relevant for feature extracting of the autoencoder with better compression
                # beacause the normal trained autoencoder is now a specified factor better and we dont want to lose
                # the newly learned progress
                if composed_autoencoder.model_with_better_compression_in_build():
                    composed_autoencoder.update_weights_of_autoencoder_with_better_compression()

                # Send encoded data_elem and  weights
                conn.sendall(sending_message)

            else:
                # Encode scaled data_elem
                encoder = composed_autoencoder.get_encoder()
                encoded_data = encoder.predict(data_elem)

                # Prepare message for Java
                encoded_data_as_json = json.dumps(encoded_data.tolist())

                # Print encoded data_elem
                print("EncodedData:", encoded_data_as_json)

                sending_message_as_json = json.dumps([0, smallest_grid_number, encoded_data_as_json])

                message_length = len(sending_message_as_json) + len("|")
                message_length += len(str(message_length))

                sending_message = bytes(str(message_length) + "|" + sending_message_as_json + "\n",
                                        encoding="utf-8")

                # Send encoded data_elem
                conn.sendall(sending_message)


if __name__ == '__main__':
    init_gpu()

    queue = Queue(maxsize=0)

    # Lines per request
    number_of_lines = 5

    # Structure of the composed_autoencoder
    layer_sizes = [
        11 * number_of_lines,
        int(11 * number_of_lines * 0.8),
        int(11 * number_of_lines * 0.2)
    ]

    # Create composed composed_autoencoder
    autoencoder = ComposedAutoEncoder(layer_sizes)

    process = Thread(target=thread_decode, args=(queue, autoencoder,))
    process.start()

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as my_socket:
        try:
            try:
                port_number = int(sys.argv[1])
            except IndexError:
                port_number = 3141  # Standard Port Number

            print("Port Number:", port_number)

            my_socket.bind(("localhost", port_number))
            print("listening...")
            my_socket.listen()

        except OSError:
            print("Something went wrong while Setting up Sockets")

        while True:
            try:
                conn, addr = my_socket.accept()

                data = conn.recv(1024)

                print("Data fresh from java:", data, "Length: ", len(data))

                data_as_string = json.loads(data)
                print("Data as String:", data_as_string)

                numpy_array = np.array(data_as_string)

                print("Data as numpy array:", numpy_array)
                numpy_array = numpy_array[np.argsort(numpy_array[:, 0])]

                if check_grouping(numpy_array):
                    try:
                        autoencoder.load_models(get_trained_model(numpy_array), get_reference_model(numpy_array))
                        print('Successfully load models', get_trained_model(numpy_array), 'and',
                              get_reference_model(numpy_array))

                        queue.put(numpy_array)
                    except OSError:
                        print('Existing model could not be loaded.')

            except OSError:
                print("Something went wrong while getting and sending data...")
                print("Closing Socket now...")
                break
