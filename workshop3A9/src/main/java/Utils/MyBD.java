package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyBD {

    private static MyBD instance;
    private Connection cnx;

    private final String URL = "jdbc:mysql://localhost:3306/wingo";
    private final String USER = "root";
    private final String PWD = "";

    private MyBD() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("Connexion réussie !");
            fixTypeColumn();
            ensureAdminExists();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    /** Fix "Data truncated for column 'type'" - alter column to VARCHAR(50) */
    private void fixTypeColumn() {
        if (cnx == null) return;
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate("ALTER TABLE utilisateur MODIFY COLUMN type VARCHAR(50)");
            System.out.println("Type column updated.");
        } catch (SQLException e) {
            // Ignore - column may already be correct or table structure differs
        }
    }

    /** Create default admin if no admin exists */
    private void ensureAdminExists() {
        if (cnx == null) return;
        try (Statement st = cnx.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT id FROM utilisateur WHERE type = 'admin' LIMIT 1");
            if (!rs.next()) {
                st.executeUpdate("INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, type, telephone, age) " +
                    "VALUES ('Admin', 'User', 'admin@wingo.com', 'admin123', 'admin', '12345678', 30)");
                System.out.println("Admin account created: admin@wingo.com / admin123");
            }
        } catch (SQLException e) {
            // Ignore
        }
    }

    public static MyBD getInstance() {
        if (instance == null) {
            instance = new MyBD();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
