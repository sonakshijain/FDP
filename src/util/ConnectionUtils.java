package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionUtils {

    private static final String DB_NAME = "sit";
    private static final String HOST_NAME = "localhost";
    private static final String PORT = "5432";
    private static final String USER_NAME = "postgres";
    private static final String PASSWORD = "password";

    private static final String CONN_URL = "jdbc:postgresql://" + HOST_NAME + ":" + PORT + "/" + DB_NAME;

    public static Connection getDBConnection() throws SQLException {
        System.out.println(CONN_URL + " " + USER_NAME + " " + PASSWORD);
        Connection connection = DriverManager.getConnection(CONN_URL, USER_NAME, PASSWORD);
        return connection;
    }

}
