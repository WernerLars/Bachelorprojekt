import json
import numpy as np
import pandas as pd

from Autoencoder.Autoencoder import AutoEncoder, get_weights
from Autoencoder.decoder import Decoder
from helper_files.AutoEncoderHelper import scale


if __name__ == "__main__":
    # Read csv file

    FILE = 'data/weather_germany_ver2_0_16752_796037235.csv'
    df = pd.read_csv(FILE, delimiter=';')

    # Drop column SNOWDEPTH
    df.drop('SNOWDEPTH', axis=1, inplace=True)

    # Get groups of grid numbers

    with open('data/grid_set_list.txt') as file:
        groups = json.load(file)

    # Get separate DataFrame for each group in groups
    data_for_each_group = [
        df[df['GRID_NO'].isin(group)].sort_values(by='GRID_NO', kind='mergesort') for group in groups
    ]

    # Get first 20 years of each group as a numpy array
    data_for_each_group = [
        data.groupby('GRID_NO').head(7300).to_numpy() for data in data_for_each_group
    ]

    # Lines per request
    number_of_lines = len(groups[0])

    # Structure of the autoencoder
    layer_sizes = [
        11 * number_of_lines,
        int(11 * number_of_lines * 0.8),
        int(11 * number_of_lines * 0.2)
    ]

    # Some variables for output progress
    counter = 0
    number_of_groups = len(data_for_each_group)

    for data in data_for_each_group:
        # Create autoencoder object
        autoencoder = AutoEncoder(layer_sizes)

        # Name pretrained model like smallest grid number of the group
        file_name = '{}.h5'.format(int(data[0, 0]))

        # Scale all values between 0 and 1
        data_scaled = scale(data)

        # Split data on in arrays for each grid number
        data_split = np.array_split(data_scaled, number_of_lines)

        # Merge arrays line by line
        data_flatten = np.hstack(data_split)

        # Train autoencoder on data and save best model
        autoencoder.train(data_flatten, epochs=10, batch_size=16,
                          checkpoint_path='models/trained_models/{}'.format(file_name))

        # Save copy of best model for reference
        autoencoder.save_model('models/reference_models/{}'.format(file_name))

        # Create decoder object
        decoder = Decoder(layer_sizes[::-1])

        # Load decoder weights
        decoder.set_weights(get_weights(autoencoder.get_decoder()))

        # Save decoder model
        decoder.save_model('models/decoder_models/{}'.format(file_name))

        counter += 1
        print('[Progress] {}/{} Done...'.format(counter, number_of_groups))
