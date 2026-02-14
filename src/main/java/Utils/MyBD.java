package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBD {
    private Connection conn;

    private static final String URL  = "jdbc:mysql://localhost:3306/wingo?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    private static MyBD instance;

    private MyBD() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected");
        } catch (SQLException e) {
            throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
        }
    }

    public static MyBD getInstance() {
        if (instance == null) instance = new MyBD();
        return instance;
    }

    public Connection getConn() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Reconnected");
            }
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
        }
    }
}