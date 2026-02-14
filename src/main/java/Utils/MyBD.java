package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBD {

    private static MyBD instance;
    private Connection conn;

    private static final String URL  =
            "jdbc:mysql://localhost:3306/wingo?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    private MyBD() { }

    public static MyBD getInstance() {
        if (instance == null) instance = new MyBD();
        return instance;
    }

    // ✅ toujours retourner une connexion ouverte
    public Connection getConn() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected");
        }
        return conn;
    }
}