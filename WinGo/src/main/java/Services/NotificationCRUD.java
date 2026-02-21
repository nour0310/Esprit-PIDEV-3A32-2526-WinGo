package Services;

import Entites.Notification;
import Utils.MyBD;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationCRUD {
    private Connection conn;

    public NotificationCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    public void ajouter(Notification notif) throws SQLException {
        String req = "INSERT INTO notification (utilisateur_id, emetteur_id, type, contenu, lien, date_creation) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, notif.getUtilisateurId());
            pst.setInt(2, notif.getEmetteurId());
            pst.setString(3, notif.getType());
            pst.setString(4, notif.getContenu());
            pst.setString(5, notif.getLien());
            pst.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
        }
    }

    public List<Notification> getNotificationsByUser(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String req = "SELECT * FROM notification WHERE utilisateur_id = ? ORDER BY date_creation DESC";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setUtilisateurId(rs.getInt("utilisateur_id"));
                    n.setEmetteurId(rs.getInt("emetteur_id"));
                    n.setType(rs.getString("type"));
                    n.setContenu(rs.getString("contenu"));
                    n.setLien(rs.getString("lien"));
                    n.setLu(rs.getBoolean("lu"));
                    n.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                    list.add(n);
                }
            }
        }
        return list;
    }

    public void marquerCommeLu(int notificationId) throws SQLException {
        String req = "UPDATE notification SET lu = 1 WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, notificationId);
            pst.executeUpdate();
        }
    }

    public void marquerToutLu(int userId) throws SQLException {
        String req = "UPDATE notification SET lu = 1 WHERE utilisateur_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        }
    }
}