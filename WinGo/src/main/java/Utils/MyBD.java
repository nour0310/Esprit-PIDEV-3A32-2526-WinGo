package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;

public class MyBD {

    private static MyBD instance;
    private Connection cnx;

    private final String URL = "jdbc:mysql://localhost:3306/wingo";
    private final String USER = "root";
    private final String PWD = "";

    private MyBD() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("Connexion rÃ©ussie !");
            fixTypeColumn();
            ensureAdminExists();
            ensureProfilTable();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    /** Fix "Data truncated for column 'type'" - alter column to VARCHAR(50) */
    private void fixTypeColumn() {
        if (cnx == null)
            return;
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate("ALTER TABLE utilisateur MODIFY COLUMN type VARCHAR(50)");
            System.out.println("Type column updated.");
            ensureVerificationColumns();
        } catch (SQLException e) {
            // Ignore - column may already be correct or table structure differs
        }
    }

    private void ensureVerificationColumns() {
        if (cnx == null)
            return;
        try (Statement st = cnx.createStatement()) {
            // Check if column exists is hard in generic SQL, so we try and ignore error or
            // check metadata
            DatabaseMetaData md = cnx.getMetaData();

            ResultSet rs1 = md.getColumns(null, null, "utilisateur", "is_verified");
            if (!rs1.next()) {
                st.executeUpdate("ALTER TABLE utilisateur ADD COLUMN is_verified BOOLEAN DEFAULT FALSE");
            }

            ResultSet rs2 = md.getColumns(null, null, "utilisateur", "verification_code");
            if (!rs2.next()) {
                st.executeUpdate("ALTER TABLE utilisateur ADD COLUMN verification_code VARCHAR(10)");
            }
        } catch (SQLException e) {
            System.err.println("Error updating user columns: " + e.getMessage());
        }
    }

    /** Create default admin if no admin exists */
    private void ensureAdminExists() {
        if (cnx == null)
            return;
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

    private void ensureProfilTable() {
        if (cnx == null) return;
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS profil (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "bio TEXT, " +
                    "image TEXT, " +
                    "utilisateur_id INT)");

            // Force AUTO_INCREMENT even if column exists but isn't set
            try {
                // Some older MySQL versions/MariaDB require re-stating PRIMARY KEY during MODIFY
                st.executeUpdate("ALTER TABLE profil MODIFY id INT AUTO_INCREMENT PRIMARY KEY");
            } catch (SQLException e) {
                try {
                    st.executeUpdate("ALTER TABLE profil MODIFY id INT AUTO_INCREMENT");
                } catch (SQLException e2) {
                    // Log but ignore, usually means it's already Correct
                    System.out.println("Note: profil table id might already be auto_increment.");
                }
            }
            System.out.println("Profil table schema verified.");
        } catch (SQLException e) {
            System.err.println("Error sync profil table: " + e.getMessage());
        }
    }
}
