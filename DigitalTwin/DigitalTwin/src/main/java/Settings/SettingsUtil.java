package Settings;

import DB.DBUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.sql.*;

@Path("/settings")
public class SettingsUtil {
    String updateDataPerHour = "updateDataPerHour";
    String recordsToSend = "recordsToSend";
    Connection connection = DBUtil.getConnection();
    private static final Logger logger = LoggerFactory.getLogger(SettingsUtil.class);

    private void insertDefaultValues() throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT updateDataPerHour, recordsToSend FROM settings WHERE id=1;");
        if (resultSet.isClosed()) {
            Statement statement = connection.createStatement();
            statement.execute("INSERT INTO settings (updateDataPerHour, recordsToSend) VALUES (1,5);");
        }
    }

    // public methods

    public int getUpdateDataPerHour() throws SQLException {
        //Insert default values if no values on the table
        insertDefaultValues();
        int result = 0;
        String sql = "SELECT updateDataPerHour FROM settings WHERE id=1;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            result = resultSet.getInt("updateDataPerHour");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    public int getRecordsToSend() throws SQLException {
        //Insert default values if no values on the table
        insertDefaultValues();
        int result = 0;
        String sql = "SELECT recordsToSend FROM settings WHERE id=1;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            result = resultSet.getInt("recordsToSend");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    public void setUpdateDataPerHour(String value) throws SQLException {
        String sql = "UPDATE settings SET updateDataPerHour = " + value + " WHERE id=1;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        int resultSet = preparedStatement.executeUpdate();
        if (resultSet != 0) {
            logger.info("UpdateDataPerHour set successfully!");
        } else {
            logger.error("There was a problem setting the new value for UpdateDataPerHour!");
        }
    }

    public void setRecordsToSend(String value) throws SQLException {
        String sql = "UPDATE settings SET recordsToSend = " + value + " WHERE id=1;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        int resultSet = preparedStatement.executeUpdate();
        if (resultSet != 0) {
            logger.info("UpdateDataPerHour set successfully!");
        } else {
            logger.error("There was a problem setting the new value for UpdateDataPerHour!");
        }
    }

    @POST
    @Consumes("*/*")
    public void setSettingsFromJson(String propertiesJson) {
        logger.debug(propertiesJson);
        JSONObject jsonObject = new JSONObject(propertiesJson);
        try {
            if (!jsonObject.isNull(updateDataPerHour))
                setUpdateDataPerHour(Integer.toString(jsonObject.getInt(updateDataPerHour)));
            if (!jsonObject.isNull(recordsToSend))
                setRecordsToSend(Integer.toString(jsonObject.getInt(recordsToSend)));

            logger.info("Settings updated!");
        } catch (Exception e) {
            logger.error("Updating settings failed!");
            e.printStackTrace();
        }
    }

    @GET
    @Produces("application/json")
    public String getSettingsAsJson() {
        String result = "";
        try {
            result = "\"" + updateDataPerHour + "\":" + getUpdateDataPerHour();
            result += ",\"" + recordsToSend + "\":" + getRecordsToSend();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{" + result + "}";
    }
}
