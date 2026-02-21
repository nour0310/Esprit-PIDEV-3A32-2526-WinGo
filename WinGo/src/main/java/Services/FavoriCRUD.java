package Services;

import Entites.Favori;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriCRUD {
    private Connection conn;

    public FavoriCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    public void ajouter(Favori favori) throws SQLException {
        String req = "INSERT INTO favori (utilisateur_id, article_id, date_ajout) VALUES (?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, favori.getUtilisateurId());
            pst.setInt(2, favori.getArticleId());
            pst.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
        }
    }

    public void supprimer(int utilisateurId, int articleId) throws SQLException {
        String req = "DELETE FROM favori WHERE utilisateur_id = ? AND article_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, utilisateurId);
            pst.setInt(2, articleId);
            pst.executeUpdate();
        }
    }

    public boolean estFavori(int utilisateurId, int articleId) throws SQLException {
        String req = "SELECT COUNT(*) FROM favori WHERE utilisateur_id = ? AND article_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, utilisateurId);
            pst.setInt(2, articleId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Integer> getFavorisByUser(int utilisateurId) throws SQLException {
        List<Integer> articleIds = new ArrayList<>();
        String req = "SELECT article_id FROM favori WHERE utilisateur_id = ? ORDER BY date_ajout DESC";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, utilisateurId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    articleIds.add(rs.getInt("article_id"));
                }
            }
        }
        return articleIds;
    }
}