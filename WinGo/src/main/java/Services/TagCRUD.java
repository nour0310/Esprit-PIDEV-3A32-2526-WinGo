package Services;

import Entites.Tag;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagCRUD {

    private Connection conn;

    public TagCRUD() {
        conn = MyBD.getInstance().getConn();
    }
    heloo

    // Ajouter un tag s'il n'existe pas déjà, retourne l'ID
    public int ajouterOuRecuperer(String nom) throws SQLException {
        // Vérifier si le tag existe déjà
        String select = "SELECT id FROM tag WHERE nom = ?";
        try (PreparedStatement pst = conn.prepareStatement(select)) {
            pst.setString(1, nom);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        // Sinon, l'insérer
        String insert = "INSERT INTO tag (nom) VALUES (?)";
        try (PreparedStatement pst = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, nom);
            pst.executeUpdate();
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Création de tag échouée, aucun ID obtenu.");
                }
            }
        }
    }

    // Associer un tag à un article
    public void associerTagArticle(int articleId, int tagId) throws SQLException {
        String req = "INSERT INTO article_tag (article_id, tag_id) VALUES (?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            pst.setInt(2, tagId);
            pst.executeUpdate();
        }
    }

    // Supprimer toutes les associations d'un article (utile avant mise à jour)
    public void supprimerAssociationsArticle(int articleId) throws SQLException {
        String req = "DELETE FROM article_tag WHERE article_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            pst.executeUpdate();
        }
    }

    // Récupérer les tags d'un article
    public List<Tag> getTagsByArticle(int articleId) throws SQLException {
        List<Tag> tags = new ArrayList<>();
        String req = "SELECT t.* FROM tag t JOIN article_tag at ON t.id = at.tag_id WHERE at.article_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Tag t = new Tag();
                    t.setId(rs.getInt("id"));
                    t.setNom(rs.getString("nom"));
                    tags.add(t);
                }
            }
        }
        return tags;
    }

    // Récupérer tous les tags (pour l'auto-complétion)
    public List<Tag> getAllTags() throws SQLException {
        List<Tag> tags = new ArrayList<>();
        String req = "SELECT * FROM tag ORDER BY nom";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Tag t = new Tag();
                t.setId(rs.getInt("id"));
                t.setNom(rs.getString("nom"));
                tags.add(t);
            }
        }
        return tags;
    }
}