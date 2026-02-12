package Services;

import Utils.MyBD;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;

public class UserCRUD {

    private final Connection conn;

    public UserCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE email = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void signup(String nom, String prenom, String email, String password) throws SQLException {
        String sql = "INSERT INTO user(nom, prenom, email, password_hash, role) VALUES(?,?,?,?,?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, nom);
            pst.setString(2, prenom);
            pst.setString(3, email);
            pst.setString(4, hashPassword(password));
            pst.setString(5, "COMMERÇANT");
            pst.executeUpdate();
        }
    }

    public boolean login(String email, String password) throws SQLException {
        String sql = "SELECT password_hash FROM user WHERE email = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return false;
                String hash = rs.getString("password_hash");
                return hash.equals(hashPassword(password));
            }
        }
    }

    private String hashPassword(String password) {
        // Simple SHA-256 (OK pour projet étudiant). En prod: BCrypt/Argon2.
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hash error", e);
        }
    }
}