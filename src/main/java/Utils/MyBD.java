package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MyBD — Singleton de connexion MySQL avec création automatique des tables
 */
public class MyBD {

    private static final String URL = "jdbc:mysql://localhost:3306/wingo?useSSL=false&serverTimezone=Africa/Tunis&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = ""; // Changez selon votre configuration

    private Connection conn;
    private static MyBD instance;

    private MyBD() {
        connect();
        createTablesIfNotExist();
    }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✅ Connexion MySQL établie");
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
            throw new RuntimeException("Connexion MySQL impossible", e);
        }
    }

    private void createTablesIfNotExist() {
        String createReclamationTable = """
            CREATE TABLE IF NOT EXISTS reclamation (
                id_reclamation INT PRIMARY KEY AUTO_INCREMENT,
                id_user INT NOT NULL,
                type_reclamation VARCHAR(50),
                sujet VARCHAR(255),
                description TEXT,
                date_reclamation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                statut VARCHAR(50) DEFAULT 'En attente',
                priorite VARCHAR(20),
                piece_jointe VARCHAR(500),
                reponse_admin TEXT,
                date_reponse TIMESTAMP NULL
            )
        """;

        String createSuggestionTable = """
            CREATE TABLE IF NOT EXISTS suggestion (
                id_suggestion INT PRIMARY KEY AUTO_INCREMENT,
                id_user INT NOT NULL,
                sujet VARCHAR(255),
                description TEXT,
                categorie VARCHAR(50),
                date_suggestion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                statut VARCHAR(50) DEFAULT 'Reçue',
                reponse_admin TEXT,
                date_reponse TIMESTAMP NULL,
                id_reclamation INT NULL
            )
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createReclamationTable);
            stmt.execute(createSuggestionTable);
            System.out.println("✅ Tables vérifiées/créées");
        } catch (SQLException e) {
            System.err.println("❌ Erreur création tables: " + e.getMessage());
        }
    }

    public static MyBD getInstance() {
        if (instance == null) {
            instance = new MyBD();
        }
        return instance;
    }

    public Connection getConn() {
        try {
            if (conn == null || !conn.isValid(3)) {
                System.out.println("⚠ Reconnexion...");
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return conn;
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("🔌 Connexion fermée");
            } catch (SQLException e) {
                System.err.println("⚠ Erreur fermeture: " + e.getMessage());
            }
        }
    }
}