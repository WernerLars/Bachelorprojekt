package CSV;

import org.json.JSONArray;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Double.NaN;
import static java.lang.Integer.MIN_VALUE;

public class WeatherEntry {

    private int gridNo = 0;
    private double latitude = 0;
    private double longitude = 0;
    private double altitude = 0;
    private int day = 0;
    private double tempMax = 0;
    private double tempMin = 0;
    private double tempAvg = 0;
    private double windspeed = 0;
    private double vapourpressure = 0;
    private int radiation = 0;
    private double snowdepth = 0;
    private Timestamp timestamp = null;

    public int getGridNo() {
        return gridNo;
    }

    public void setGridNo(int gridNo) {
        this.gridNo = gridNo;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLatitude(String latitude) {
        if (latitude != null && !latitude.isEmpty()) {
            try {
                this.latitude = Math.rint(Double.parseDouble(latitude) * 100000) / 100000;
            } catch (NumberFormatException e) {
                this.latitude = NaN;
            }
        } else
            this.latitude = 0;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLongitude(String longitude) {
        if (longitude != null && !longitude.isEmpty()) {
            try {
                this.longitude = Math.rint(Double.parseDouble(longitude) * 100000) / 100000;
            } catch (NumberFormatException e) {
                this.longitude = NaN;
            }
        } else
            this.longitude = 0;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setAltitude(String altitude) {
        if (altitude != null && !altitude.isEmpty()) {
            try {
                this.altitude = Math.rint(Double.parseDouble(altitude) * 100000) / 100000;
            } catch (NumberFormatException e) {
                this.altitude = NaN;
            }
        } else
            this.altitude = 0;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setDay(String day) {
        if (day != null && !day.isEmpty()) {
            try {
                this.day = Double.valueOf(day).intValue();
            } catch (NumberFormatException e) {
                this.day = MIN_VALUE;
            }
        } else
            this.day = 0;
    }

    public double getTempMax() {
        return tempMax;
    }

    public void setTempMax(double tempMax) {
        this.tempMax = tempMax;
    }

    public void setTempMax(String tempMax) {
        if (tempMax != null && !tempMax.isEmpty()) {
            try {
                this.tempMax = Math.rint(Double.parseDouble(tempMax) * 100000) / 100000;
            } catch (NumberFormatException e) {
                this.tempMax = NaN;
            }
        } else
            this.tempMax = 0;
    }

    public double getTempMin() {
        return tempMin;
    }

    public void setTempMin(double tempMin) {
        this.tempMin = tempMin;
    }

    public void setTempMin(String tempMin) {
        if (tempMin != null && !tempMin.isEmpty()) {
            try {
                this.tempMin = Math.rint(Double.parseDouble(tempMin) * 100000) / 100000;
            } catch (NumberFormatException e) {
                this.tempMin = NaN;
            }
        } else
            this.tempMin = 0;
    }

    public double getTempAvg() {
        return tempAvg;
    }

    public void setTempAvg(double tempAvg) {
        this.tempAvg = tempAvg;
    }

    public void setTempAvg(String tempAvg) {
        if (tempAvg != null && !tempAvg.isEmpty()) {
            try {
                this.tempAvg = Math.rint(Double.parseDouble(tempAvg) * 100000) / 100000;
            } catch (NumberFormatException e) {
                this.tempAvg = NaN;
            }
        } else
            this.tempAvg = 0;
    }

    public double getWindspeed() {
        return windspeed;
    }

    public void setWindspeed(double windspeed) {
        this.windspeed = windspeed;
    }

    public void setWindspeed(String windspeed) {
        if (windspeed != null && !windspeed.isEmpty()) {
            try {
                this.windspeed = Math.rint(Double.parseDouble(windspeed) * 100000) / 100000;
            } catch (NumberFormatException e) {
                this.windspeed = NaN;
            }
        } else
            this.windspeed = 0;
    }

    public double getVapourpressure() {
        return vapourpressure;
    }

    public void setVapourpressure(double vapourpressure) {
        this.vapourpressure = vapourpressure;
    }

    public void setVapourpressure(String vapourpressure) {
        if (vapourpressure != null && !vapourpressure.isEmpty()) {
            try {
                this.vapourpressure = Math.rint(Double.parseDouble(vapourpressure) * 100000) / 100000;
            } catch (NumberFormatException e) {
                this.vapourpressure = NaN;
            }
        } else
            this.vapourpressure = 0;
    }

    public int getRadiation() {
        return radiation;
    }

    public void setRadiation(int radiation) {
        this.radiation = radiation;
    }

    public void setRadiation(String radiation) {
        if (radiation != null && !radiation.isEmpty()) {
            try {
                this.radiation = Double.valueOf(radiation).intValue();
            } catch (NumberFormatException e) {
                this.radiation = MIN_VALUE;
            }
        } else
            this.radiation = 0;
    }

    public double getSnowdepth() {
        return snowdepth;
    }

    public void setSnowdepth(double snowdepth) {
        this.snowdepth = snowdepth;
    }

    public void setSnowdepth(String snowdepth) {
        if (snowdepth != null && !snowdepth.isEmpty()) {
            try {
                this.snowdepth = Math.rint(Double.parseDouble(snowdepth) * 100000) / 100000;
            } catch (NumberFormatException e) {
                this.snowdepth = NaN;
            }
        } else
            this.snowdepth = 0;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return "Grid No: " + this.gridNo + "\nLatitude: " + this.latitude + "\nLongitude: " + this.longitude
                + "\nAltitude: " + this.altitude + "\nDay: " + this.day + "\nTemperature (max): " + this.tempMax
                + "\nTemperature (min): " + this.tempMin + "\nTemperature (avg): " + this.tempAvg + "\nWindspeed: "
                + this.windspeed + "\nVapourpressure: " + this.vapourpressure + "\nRadiation: " + this.radiation
                + "\nSnowdepth: " + this.snowdepth + "\n";
    }

    public String toJSON() {
        return "{\"gridno\":" + getGridNo() + ",\"latitude\":" + getLatitude() + ",\"longitude\":" + getLongitude()
                + ",\"altitude\":" + getAltitude() + ",\"day\":" + getDay() + ",\"tempmax\":" + getTempMax()
                + ",\"tempmin\":" + getTempMin() + ",\"tempavg\":" + getTempAvg() + ",\"windspeed\":" + getWindspeed()
                + ",\"vapourpressure\":" + getVapourpressure() + ",\"radiation\":" + getRadiation() + ",\"snowdepth\":"
                + getSnowdepth() + ",\"timestamp\":\"" + getTimestamp() + "\"}";
    }

    public String toJSONArrayString() {
        Object[] values = {gridNo, latitude, longitude, altitude, day, tempMax, tempMin, tempAvg, windspeed,
                vapourpressure, radiation};
        JSONArray jsonArray = new JSONArray(values);
        return jsonArray.toString();
    }

    public String getValuesAsJson(String valueKeyString) {
        List<String> valueKeys = Arrays.asList(valueKeyString.split(","));
        LinkedList<String> kvList = new LinkedList<>();
        try {

            if (valueKeys.contains("gridno"))
                kvList.add("\"gridno\":" + this.getGridNo());
            if (valueKeys.contains("latitude"))
                kvList.add("\"latitude\":" + this.getLatitude());
            if (valueKeys.contains("longitude"))
                kvList.add("\"longitude\":" + this.getLongitude());
            if (valueKeys.contains("altitude"))
                kvList.add("\"altitude\":" + this.getAltitude());
            if (valueKeys.contains("day"))
                kvList.add("\"day\":" + this.getDay());
            if (valueKeys.contains("tempmax"))
                kvList.add("\"tempmax\":" + this.getTempMax());
            if (valueKeys.contains("tempmin"))
                kvList.add("\"tempmin\":" + this.getTempMin());
            if (valueKeys.contains("tempavg"))
                kvList.add("\"tempavg\":" + this.getTempAvg());
            if (valueKeys.contains("windspeed"))
                kvList.add("\"windspeed\":" + this.getWindspeed());
            if (valueKeys.contains("vapourpressure"))
                kvList.add("\"vapourpressure\":" + this.getVapourpressure());
            if (valueKeys.contains("radiation"))
                kvList.add("\"radiation\":" + this.getRadiation());
            if (valueKeys.contains("snowdepth"))
                kvList.add("\"snowdepth\":" + this.getSnowdepth());
            if (valueKeys.contains("timestamp"))
                kvList.add("\"timestamp\":\"" + this.getTimestamp() + "\"");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{" + String.join(",", kvList) + "}";
    }
}