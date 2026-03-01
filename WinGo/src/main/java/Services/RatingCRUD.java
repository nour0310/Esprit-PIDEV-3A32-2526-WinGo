package Services;

import Entites.Rating;
import Utils.MyBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RatingCRUD {

    private Connection conn;

    public RatingCRUD() {
        conn = MyBD.getInstance().getCnx();
    }

    // Ajouter ou mettre Ã  jour une note (unique par utilisateur/article)
    public void ajouterOuModifier(Rating rating) throws SQLException {
        // VÃ©rifier si une note existe dÃ©jÃ 
        String check = "SELECT id FROM rating WHERE utilisateur_id=? AND article_id=?";
        try (PreparedStatement pst = conn.prepareStatement(check)) {
            pst.setInt(1, rating.getUtilisateurId());
            pst.setInt(2, rating.getArticleId());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Mise Ã  jour
                    int id = rs.getInt("id");
                    String update = "UPDATE rating SET note=?, date_rating=? WHERE id=?";
                    try (PreparedStatement pstUpdate = conn.prepareStatement(update)) {
                        pstUpdate.setInt(1, rating.getNote());
                        pstUpdate.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                        pstUpdate.setInt(3, id);
                        pstUpdate.executeUpdate();
                    }
                } else {
                    // Insertion
                    String insert = "INSERT INTO rating (utilisateur_id, article_id, note, date_rating) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstInsert = conn.prepareStatement(insert)) {
                        pstInsert.setInt(1, rating.getUtilisateurId());
                        pstInsert.setInt(2, rating.getArticleId());
                        pstInsert.setInt(3, rating.getNote());
                        pstInsert.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                        pstInsert.executeUpdate();
                    }
                }
            }
        }
    }

    // Supprimer la note d'un utilisateur pour un article
    public void supprimer(int userId, int articleId) throws SQLException {
        String req = "DELETE FROM rating WHERE utilisateur_id=? AND article_id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, userId);
            pst.setInt(2, articleId);
            pst.executeUpdate();
        }
    }

    // RÃ©cupÃ©rer la note d'un utilisateur pour un article
    public Integer getNoteUtilisateur(int userId, int articleId) throws SQLException {
        String req = "SELECT note FROM rating WHERE utilisateur_id=? AND article_id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, userId);
            pst.setInt(2, articleId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("note");
                }
            }
        }
        return null;
    }

    // Calculer la moyenne des notes pour un article
    public double getMoyenne(int articleId) throws SQLException {
        String req = "SELECT AVG(note) as moyenne FROM rating WHERE article_id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("moyenne");
                }
            }
        }
        return 0.0;
    }

    // Compter le nombre de votes pour un article
    public int getNombreVotes(int articleId) throws SQLException {
        String req = "SELECT COUNT(*) FROM rating WHERE article_id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // RÃ©cupÃ©rer toutes les notes (pour chargement initial)
    public List<Rating> afficherTous() throws SQLException {
        List<Rating> list = new ArrayList<>();
        String req = "SELECT * FROM rating ORDER BY date_rating DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Rating r = new Rating();
                r.setId(rs.getInt("id"));
                r.setUtilisateurId(rs.getInt("utilisateur_id"));
                r.setArticleId(rs.getInt("article_id"));
                r.setNote(rs.getInt("note"));
                r.setDateRating(rs.getTimestamp("date_rating").toLocalDateTime());
                list.add(r);
            }
        }
        return list;
    }
}