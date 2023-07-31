package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

    private static Connection conn;
    private static final String DB_FILE = "twin.db";

    private static void init() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            conn.setAutoCommit(true);

            Statement statement = conn.createStatement();

            statement.execute(
                    "create table if not exists weatherentries" +
                            "	(timestamp INTEGER DEFAULT CURRENT_TIMESTAMP," +
                            "	day INTEGER," +
                            "	gridno INTEGER," +
                            "	latitude REAL, " +
                            "	longitude REAL, " +
                            "	altitude REAL, " +
                            "	tempmax REAL, " +
                            "	tempmin REAL," +
                            "	tempavg REAL, " +
                            "	windspeed REAL, " +
                            "	vapourpressure REAL, " +
                            "	radiation INTEGER, " +
                            "	snowdepth REAL, " +
                            "	PRIMARY KEY (timestamp, gridno))" +
                            "	without rowid");

            statement.execute(
                    "CREATE TABLE IF NOT EXISTS settings " +
                            "(id INTEGER DEFAULT 1 PRIMARY KEY, " +
                            "updateDataPerHour INTEGER DEFAULT 1," +
                            " recordsToSend INTEGER DEFAULT 5)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        if (conn == null)
            init();
        return conn;
    }

}
