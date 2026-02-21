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

    /**
     * Ajoute un tag s'il n'existe pas déjà, retourne son ID.
     * @param nom le nom du tag (unique)
     * @return l'ID du tag (nouvel ID si insertion, ID existant si déjà présent)
     * @throws SQLException
     */
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

    /**
     * Associe un tag à un article.
     * @param articleId l'ID de l'article
     * @param tagId l'ID du tag
     * @throws SQLException
     */
    public void associerTagArticle(int articleId, int tagId) throws SQLException {
        String req = "INSERT INTO article_tag (article_id, tag_id) VALUES (?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            pst.setInt(2, tagId);
            pst.executeUpdate();
        }
    }

    /**
     * Supprime toutes les associations de tags pour un article donné.
     * @param articleId l'ID de l'article
     * @throws SQLException
     */
    public void supprimerAssociationsArticle(int articleId) throws SQLException {
        String req = "DELETE FROM article_tag WHERE article_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            pst.executeUpdate();
        }
    }

    /**
     * Récupère la liste des tags associés à un article.
     * @param articleId l'ID de l'article
     * @return liste de tags
     * @throws SQLException
     */
    public List<Tag> getTagsByArticle(int articleId) throws SQLException {
        List<Tag> tags = new ArrayList<>();
        String req = "SELECT t.* FROM tag t JOIN article_tag at ON t.id = at.tag_id WHERE at.article_id = ? ORDER BY t.nom";
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

    /**
     * Récupère tous les tags existants dans la base (pour l'auto-complétion, par exemple).
     * @return liste de tous les tags, triés par nom
     * @throws SQLException
     */
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

    /**
     * Supprime un tag par son ID (les associations seront supprimées par CASCADE).
     * @param tagId l'ID du tag
     * @throws SQLException
     */
    public void supprimerTag(int tagId) throws SQLException {
        String req = "DELETE FROM tag WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, tagId);
            pst.executeUpdate();
        }
    }

    /**
     * Met à jour le nom d'un tag.
     * @param tagId l'ID du tag
     * @param nouveauNom le nouveau nom (doit être unique)
     * @throws SQLException (si le nom existe déjà)
     */
    public void modifierTag(int tagId, String nouveauNom) throws SQLException {
        String req = "UPDATE tag SET nom = ? WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, nouveauNom);
            pst.setInt(2, tagId);
            pst.executeUpdate();
        }
    }
}