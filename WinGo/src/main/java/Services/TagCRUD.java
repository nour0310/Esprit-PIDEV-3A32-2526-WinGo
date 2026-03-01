package Services;

import Entites.Tag;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagCRUD {

    private Connection conn;

    public TagCRUD() {
        conn = MyBD.getInstance().getCnx();
    }

    public int ajouterOuRecuperer(String nom) throws SQLException {
        // VÃ©rifier si le tag existe dÃ©jÃ 
        String select = "SELECT id FROM tag WHERE nom = ?";
        try (PreparedStatement pst = conn.prepareStatement(select)) {
            pst.setString(1, nom);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        // Sinon, l'insÃ©rer
        String insert = "INSERT INTO tag (nom) VALUES (?)";
        try (PreparedStatement pst = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, nom);
            int affected = pst.executeUpdate();
            if (affected == 0) {
                throw new SQLException("CrÃ©ation de tag Ã©chouÃ©e, aucune ligne affectÃ©e.");
            }
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("CrÃ©ation de tag Ã©chouÃ©e, aucun ID obtenu.");
                }
            }
        }
    }

    public void associerTagArticle(int articleId, int tagId) throws SQLException {
        String req = "INSERT INTO article_tag (article_id, tag_id) VALUES (?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            pst.setInt(2, tagId);
            pst.executeUpdate();
        }
    }

    public void supprimerAssociationsArticle(int articleId) throws SQLException {
        String req = "DELETE FROM article_tag WHERE article_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            pst.executeUpdate();
        }
    }

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

    public void supprimerTag(int tagId) throws SQLException {
        String req = "DELETE FROM tag WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, tagId);
            pst.executeUpdate();
        }
    }

    public void modifierTag(int tagId, String nouveauNom) throws SQLException {
        String req = "UPDATE tag SET nom = ? WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, nouveauNom);
            pst.setInt(2, tagId);
            pst.executeUpdate();
        }
    }
}