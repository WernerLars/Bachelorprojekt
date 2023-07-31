import json
import socket

from Autoencoder import decoder
from helper_files.AutoEncoderHelper import init_gpu
from helper_files.DecoderMainHelper import initialize_decoder, send_weights, decode


def get_decoder_model(smallest_grid_no):
    return 'models/decoder_models/' + str(smallest_grid_no) + '.h5'


if __name__ == '__main__':
    init_gpu()

    # Lines per request
    number_of_lines = 5

    # Structure of the decoder
    layer_sizes = initialize_decoder(number_of_lines)

    # Create decoder object
    decoder_model = decoder.Decoder(layer_sizes)

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as my_socket:
        try:
            my_socket.bind(("localhost", 3142))
            my_socket.listen()
        except OSError:
            print("Something went wrong while Setting up Sockets")

        while True:
            try:
                conn, addr = my_socket.accept()

                send_data = conn.recv(8192)
                send_data = send_data.split(b'|')
                message_length = int(send_data[0]) - len("|") - len(send_data[0])

                print("Message Length:", message_length)

                data = send_data[1]

                while len(data) < message_length:
                    send_data = conn.recv(8192)
                    data += send_data

                print("Data from Java: ", data)

                data = json.loads(data)

                weights_send = bool(data[0])
                print("Weights send? ", weights_send)

                smallest_grid_number = data[1]
                print("Smallest Grid Number: ", smallest_grid_number)

                try:
                    decoder_model.load_model(get_decoder_model(smallest_grid_number))
                    print('Successfully load model', get_decoder_model(smallest_grid_number))
                except OSError:
                    print('Existing model could not be loaded.')

                if weights_send:

                    # Set received weights
                    decoder_model.set_weights(send_weights(data, decoder_model))

                    # Save changed model
                    decoder_model.save_model(get_decoder_model(smallest_grid_number))

                encoded_data_as_string = data[2]
                encoded_data_as_list = json.loads(encoded_data_as_string)
                print("Encoded Data from java:", encoded_data_as_list)

                # Decode data

                decoded_data_as_bytes = decode(decoder_model, encoded_data_as_list, number_of_lines)

                conn.sendall(decoded_data_as_bytes)

            except OSError:
                print("Something went wrong while getting and sending data...")
                print("Closing Socket now...")
                break
