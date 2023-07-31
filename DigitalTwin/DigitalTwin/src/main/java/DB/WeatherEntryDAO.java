package DB;

import CSV.CsvReader;
import CSV.WeatherEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WeatherEntryDAO implements DAO<WeatherEntry> {

    private static boolean isEmpty;
    private static final Logger logger = LoggerFactory.getLogger(WeatherEntryDAO.class);

	@Override
    public List<WeatherEntry> getAll() {
        Connection conn = DBUtil.getConnection();
        List<WeatherEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM weatherentries";
        logger.info("Fetching all entries from DB...");
        try (PreparedStatement prep = conn.prepareStatement(sql); ResultSet rs = prep.executeQuery()) {
            while (rs.next()) {
                entries.add(getEntry4ResultSet(rs));
            }
            logger.info("GetAllComplete!");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error fetching data");
        }
        return entries;
    }

    @Override
    public List<WeatherEntry> getWhere(Map<String, List<String>> queryParams) {
        Connection conn = DBUtil.getConnection();
        List<WeatherEntry> entries = new ArrayList<>();
        List<Object> sqlParams = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM weatherentries WHERE 1 = 1");
        queryParams.forEach((key, list) -> {
            if (!key.equals("limit") && !key.equals("offset")) {
                for (String value : list) {
                    sql.append(" AND " + key);
                    if (value.startsWith("(")) {
                        // in array
                        sql.append(" IN (");
                        String[] arrayValues = value.substring(1, value.length() - 1).split(",");
                        for (String arrayValue : arrayValues) {
                            sql.append("?,");
                            sqlParams.add(Double.parseDouble(arrayValue));
                        }
                        sql.deleteCharAt(sql.length() - 1).append(")");
                    } else if (value.startsWith("le")) {
                        // less or equal
                        sql.append(" <= ?");
                        sqlParams.add(Double.parseDouble(value.substring(2)));
                    } else if (value.startsWith("l")) {
                        // less
                        sql.append(" < ?");
                        sqlParams.add(Double.parseDouble(value.substring(1)));
                    } else if (value.startsWith("ge")) {
                        // greater or equal
                        sql.append(" >= ?");
                        sqlParams.add(Double.parseDouble(value.substring(2)));
                    } else if (value.startsWith("g")) {
                        // greater
                        sql.append(" > ?");
                        sqlParams.add(Double.parseDouble(value.substring(1)));
                    } else {
                        // should be numeric
                        sql.append(" = ?");
                        sqlParams.add(Double.parseDouble(value));
                    }
                }
            }
        });
        // order, limit and offset have to be at the very end of the query
        sql.append(" ORDER BY timestamp desc");
        if (queryParams.containsKey("limit")) {
            sql.append(" LIMIT ?");
            sqlParams.add(Integer.parseInt(queryParams.get("limit").get(0)));
        }
        if (queryParams.containsKey("offset")) {
            sql.append(" OFFSET ?");
            sqlParams.add(Integer.parseInt(queryParams.get("offset").get(0)));
        }
        logger.debug("Assembled query: " + sql.toString());
        ResultSet rs = null;
        try (PreparedStatement prep = conn.prepareStatement(sql.toString())) {
            for (int i = 1; i <= sqlParams.size(); i++)
                prep.setObject(i, sqlParams.get(i - 1));
            rs = prep.executeQuery();
            while (rs.next()) {
                entries.add(getEntry4ResultSet(rs));
            }
            logger.info("GetWhereComplete!");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error fetching data");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    logger.error("ResultSet already closed or never existed");
                }
            }
        }
        return entries;
    }

    @Override
    public void save(WeatherEntry entry) {
        Connection conn = DBUtil.getConnection();
        String sql = "INSERT INTO weatherentries "
                + "(day,gridno,latitude,longitude,altitude,tempmax,tempmin,tempavg,windspeed,vapourpressure,radiation,snowdepth) "
                + "values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        logger.info("Saving entry in DB...");
        try (PreparedStatement prep = conn.prepareStatement(sql)) {
            // timestamp has default value of now
            prep.setInt(1, entry.getDay());
            prep.setInt(2, entry.getGridNo());
            prep.setDouble(3, entry.getLatitude());
            prep.setDouble(4, entry.getLongitude());
            prep.setDouble(5, entry.getAltitude());
            prep.setDouble(6, entry.getTempMax());
            prep.setDouble(7, entry.getTempMin());
            prep.setDouble(8, entry.getTempAvg());
            prep.setDouble(9, entry.getWindspeed());
            prep.setDouble(10, entry.getVapourpressure());
            prep.setInt(11, entry.getRadiation());
            prep.setDouble(12, entry.getSnowdepth());
            prep.execute();
            logger.info("SaveComplete!");
        } catch (SQLException e) {
            logger.error("Save failed!");
            e.printStackTrace();
        }
    }

    @Override
    public void update(WeatherEntry entry) {
        Connection conn = DBUtil.getConnection();
        String sql = "UPDATE weatherentries SET "
                + "(day,gridno,latitude,longitude,altitude,tempmax,tempmin,tempavg,windspeed,vapourpressure,radiation,snowdepth) "
                + "values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) WHERE timestamp = ?";
        logger.info("Updating entry in DB...");
        try (PreparedStatement prep = conn.prepareStatement(sql)) {
            // timestamp has default value of now
            prep.setInt(1, entry.getDay());
            prep.setInt(2, entry.getGridNo());
            prep.setDouble(3, entry.getLatitude());
            prep.setDouble(4, entry.getLongitude());
            prep.setDouble(5, entry.getAltitude());
            prep.setDouble(6, entry.getTempMax());
            prep.setDouble(7, entry.getTempMin());
            prep.setDouble(8, entry.getTempAvg());
            prep.setDouble(9, entry.getWindspeed());
            prep.setDouble(10, entry.getVapourpressure());
            prep.setInt(11, entry.getRadiation());
            prep.setDouble(12, entry.getSnowdepth());
            prep.setTimestamp(13, entry.getTimestamp());
            prep.execute();
            logger.info("UpdateComplete!");
        } catch (SQLException e) {
            logger.error("Update failed!");
            e.printStackTrace();
        }
    }

    @Override
    public void delete(WeatherEntry entry) {
        Connection conn = DBUtil.getConnection();
        String sql = "DELETE FROM weatherentries WHERE timestamp = ?";
        logger.info("Deleting entry from DB...");
        try (PreparedStatement prep = conn.prepareStatement(sql)) {
            // timestamp has default value of now
            prep.setTimestamp(1, entry.getTimestamp());
            prep.execute();
            logger.info("DeleteComplete!");
        } catch (SQLException e) {
            logger.error("Delete failed!");
            e.printStackTrace();
        }
    }

    private WeatherEntry getEntry4ResultSet(ResultSet rs) throws SQLException {
        WeatherEntry entry = new WeatherEntry();
        entry.setDay(rs.getInt("day"));
        entry.setGridNo(rs.getInt("gridno"));
        entry.setLatitude(rs.getDouble("latitude"));
        entry.setLongitude(rs.getDouble("longitude"));
        entry.setAltitude(rs.getDouble("altitude"));
        entry.setTempMax(rs.getDouble("tempmax"));
        entry.setTempMin(rs.getDouble("tempmin"));
        entry.setTempAvg(rs.getDouble("tempavg"));
        entry.setWindspeed(rs.getDouble("windspeed"));
        entry.setRadiation(rs.getInt("radiation"));
        entry.setSnowdepth(rs.getDouble("snowdepth"));
        entry.setVapourpressure(rs.getDouble("vapourpressure"));
        // SQLite's datetime format is not compatible with sql.Timestamp, therefore get
        // it as String first
        entry.setTimestamp(Timestamp.valueOf(rs.getString("timestamp")));
        return entry;
    }

    public static WeatherEntry getEntry4JSONArray(String jsonArray) {
        String[] values = jsonArray.strip().substring(1, jsonArray.length() - 1).split(",");
        if (values.length != CsvReader.NUMBER_OF_FIELDS) {
            return null;
        }
        return getEntry4Values(values);
    }

    public static WeatherEntry getEntry4Values(String[] values) {
        WeatherEntry entry = new WeatherEntry();
        // we have to set isEmpty to false, if isEmpty was true than for the next line
        // it will be also true if we don't set it to false
        isEmpty = false;
        entry.setGridNo(Integer.parseInt(checkValue(values[0])));
        entry.setLatitude(checkValue(values[1]));
        entry.setLongitude(checkValue(values[2]));
        entry.setAltitude(checkValue(values[3]));
        entry.setDay(checkValue(values[4]));
        entry.setTempMax(checkValue(values[5]));
        entry.setTempMin(checkValue(values[6]));
        entry.setTempAvg(checkValue(values[7]));
        entry.setWindspeed(checkValue(values[8]));
        entry.setVapourpressure(checkValue(values[9]));
        entry.setRadiation(checkValue(values[10]));
        entry.setSnowdepth(-1);
        entry.setTimestamp(new Timestamp(new Date().getTime()));
        if (!isEmpty) {
            return entry;
        }
        isEmpty = false;
        return null;
    }

    private static String checkValue(String value) {
        if (value == null || value.isEmpty()) {
            isEmpty = true;
        }
        return value;
    }

    public static List<WeatherEntry> getEntries4Values(String decodedData) {
        List<WeatherEntry> entries = new ArrayList<WeatherEntry>();
        decodedData = decodedData.substring(1, decodedData.length() - 1);
        int start;
        int end;
        while ((start = decodedData.indexOf("[")) != -1 && (end = decodedData.indexOf("]")) != -1) {
            String entryAsJson = decodedData.substring(start, end - 1);
            entries.add(WeatherEntryDAO
                    .getEntry4Values(entryAsJson.replaceAll("\\[|\\]", "").split(",")));
            if (decodedData.length() <= end + 1) {
                break;
            }
            decodedData = decodedData.substring(end + 2);
        }
        return entries;
    }
}
