from operator import itemgetter
from helper_files import AutoEncoderHelper
from itertools import chain
import simplejson


# file contains 665 different sensors, using a divider of that as set_size is recommended(e.g. 5, 7)
def get_sensor_sets(set_size):
    # get data frame and drop unneeded columns
    weather_data = AutoEncoderHelper.get_data_frame('data/weather_germany_ver2_0_16752_796037235.csv')
    sensor_locations = weather_data.drop(
        columns=['ALTITUDE', 'DAY', 'TEMPERATURE_MAX', 'TEMPERATURE_MIN', 'TEMPERATURE_AVG', 'WINDSPEED',
                 'VAPOURPRESSURE', 'RADIATION']).drop_duplicates()

    # round longitude and sort locations by latitude for each longitude value
    sensor_locations['LONGITUDE'] = sensor_locations['LONGITUDE'].round()
    sensor_locations['LATITUDE'] = sensor_locations['LATITUDE'].round(decimals=1)
    sensor_locations = sensor_locations.sort_values(by=['LONGITUDE', 'LATITUDE']).to_numpy().tolist()

    # wrap a list around data from the same longitude
    sorted_locations = [[], [], [], [], [], [], [], [], [], []]
    for i in sensor_locations:
        index = int(i[2] - 6)
        sorted_locations[index].append(i)

    # sort descending for every other longitude so that neighbouring entries are always geographically close to each
    # other
    for j in range(0, 9, 2):
        sorted_locations[j] = sorted(sorted_locations[j], key=itemgetter(1), reverse=True)

    # flatten the list
    sorted_locations = list(chain.from_iterable(sorted_locations))

    # create sets of sensors
    sensor_sets = []
    temp_list = []
    counter = 0
    for k in sorted_locations:
        if counter % set_size == 0 and counter != 0:
            sensor_sets.append(temp_list)
            temp_list = []
        temp_list.append(int(k[0]))
        counter += 1
    if temp_list:
        sensor_sets.append(temp_list)
    return sensor_sets


f = open('data/grid_set_list.txt', 'w')
simplejson.dump(get_sensor_sets(5), f)
f.close()
