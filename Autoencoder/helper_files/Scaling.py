# List of all columns
columns = [
    'GRID_NO',
    'LATITUDE',
    'LONGITUDE',
    'ALTITUDE',
    'DAY',
    'TEMPERATURE_MAX',
    'TEMPERATURE_MIN',
    'TEMPERATURE_AVG',
    'WINDSPEED',
    'VAPOURPRESSURE',
    'RADIATION'
]

# Scaling ranges -> <feature>: [min_value, max_value]
# 'TEMPERATURE_MAX' - 'RADIATION' scaled: [min - 50%, max + 50%]
absolute_min_max = {
    'GRID_NO': [85110, 120105],
    'LATITUDE': [47.22616, 55.05387],
    'LONGITUDE': [5.880880, 14.93512],
    'ALTITUDE': [1, 1568],
    'DAY': [19750100, 22000101],
    'TEMPERATURE_MAX': [-30.15, 61.35],
    'TEMPERATURE_MIN': [-45.3, 38.1],
    'TEMPERATURE_AVG': [-34.35, 48.15],
    'WINDSPEED': [0.0, 48.75],
    'VAPOURPRESSURE': [0.4, 40.695],
    'RADIATION': [0.0, 53325]
}
