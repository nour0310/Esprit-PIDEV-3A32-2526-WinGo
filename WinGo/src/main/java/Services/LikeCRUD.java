package Services;

import Entites.Like;
import Utils.MyBD;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LikeCRUD {

    private Connection conn;

    public LikeCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    // Ajouter un like
    public void ajouter(Like like) throws SQLException {
        String req = "INSERT INTO likes (utilisateur_id, article_id, date_like) VALUES (?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, like.getUtilisateurId());
            pst.setInt(2, like.getArticleId());
            pst.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
        }
    }

    // Supprimer un like par son ID
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM likes WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    // Supprimer un like par utilisateur et article (utile pour toggle)
    public void supprimerParUtilisateurEtArticle(int userId, int articleId) throws SQLException {
        String req = "DELETE FROM likes WHERE utilisateur_id=? AND article_id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, userId);
            pst.setInt(2, articleId);
            pst.executeUpdate();
        }
    }

    // Vérifier si un utilisateur a déjà liké un article
    public boolean existe(int userId, int articleId) throws SQLException {
        String req = "SELECT COUNT(*) FROM likes WHERE utilisateur_id=? AND article_id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, userId);
            pst.setInt(2, articleId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Compter les likes d'un article
    public int compterParArticle(int articleId) throws SQLException {
        String req = "SELECT COUNT(*) FROM likes WHERE article_id=?";
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

    // Récupérer tous les likes (pour chargement initial)
    public List<Like> afficherTous() throws SQLException {
        List<Like> list = new ArrayList<>();
        String req = "SELECT * FROM likes ORDER BY date_like DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Like l = new Like();
                l.setId(rs.getInt("id"));
                l.setUtilisateurId(rs.getInt("utilisateur_id"));
                l.setArticleId(rs.getInt("article_id"));
                l.setDateLike(rs.getTimestamp("date_like").toLocalDateTime());
                list.add(l);
            }
        }
        return list;
    }
}