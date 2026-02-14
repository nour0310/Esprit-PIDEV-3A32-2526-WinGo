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

    private MyBD() { }

    public static MyBD getInstance() {
        if (instance == null) instance = new MyBD();
        return instance;
    }

    private Connection newConn() throws SQLException {
        Connection c = DriverManager.getConnection(URL, USER, PASS);
        System.out.println("Connected");
        return c;
    }

    public Connection getConn() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = newConn();
        }
        return conn;
    }
}