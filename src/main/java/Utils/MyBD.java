package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBD {
    private Connection conn;
    private final String URL = "jdbc:mysql://localhost:3306/wingo";
    private final String USER = "root";
    private final String PASS = "";

    private static MyBD instance;

    private MyBD() {
        reconnect();
    }

    public static MyBD getInstance() {
        if (instance == null) instance = new MyBD();
        return instance;
    }

    private void reconnect() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected");
        } catch (SQLException e) {
            throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
        }
    }

    public Connection getConn() {
        try {
            if (conn == null || conn.isClosed()) {
                System.out.println("Reconnected");
                reconnect();
            }
        } catch (SQLException e) {
            System.out.println("Reconnected (exception)");
            reconnect();
        }
        return conn;
    }
}