package Services;

import Entites.Commentaire;
import Utils.MyBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentaireCRUD implements IntrefaceCRUD<Commentaire> {

    private Connection conn;

    public CommentaireCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Commentaire commentaire) throws SQLException {
        String req = "INSERT INTO commentaire (contenu, date_commentaire, utilisateur, article_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, commentaire.getContenu());
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(3, commentaire.getUtilisateur());
            pst.setInt(4, commentaire.getArticleId());
            pst.executeUpdate();
        }
        System.out.println("Commentaire ajouté !");
    }

    @Override
    public void modifier(Commentaire commentaire) throws SQLException {
        String req = "UPDATE commentaire SET contenu=?, utilisateur=?, article_id=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, commentaire.getContenu());
            pst.setInt(2, commentaire.getUtilisateur());
            pst.setInt(3, commentaire.getArticleId());
            pst.setInt(4, commentaire.getId());
            pst.executeUpdate();
        }
        System.out.println("Commentaire modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM commentaire WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
        System.out.println("Commentaire supprimé !");
    }

    @Override
    public List<Commentaire> afficher() throws SQLException {
        String req = "SELECT c.*, u.nom, u.prenom FROM commentaire c LEFT JOIN utilisateur u ON c.utilisateur = u.id ORDER BY c.date_commentaire DESC";
        List<Commentaire> liste = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setContenu(rs.getString("contenu"));
                c.setDateCommentaire(rs.getTimestamp("date_commentaire").toLocalDateTime());
                c.setUtilisateur(rs.getInt("utilisateur"));
                c.setArticleId(rs.getInt("article_id"));

                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String utilisateurNom;
                if (prenom != null && nom != null) {
                    utilisateurNom = prenom + " " + nom;
                } else if (prenom != null) {
                    utilisateurNom = prenom;
                } else if (nom != null) {
                    utilisateurNom = nom;
                } else {
                    utilisateurNom = "Utilisateur " + rs.getInt("utilisateur");
                }
                c.setUtilisateurNom(utilisateurNom);
                liste.add(c);
            }
        }
        return liste;
    }

    public List<Commentaire> getCommentsByArticle(int articleId) throws SQLException {
        String req = "SELECT c.*, u.nom, u.prenom FROM commentaire c LEFT JOIN utilisateur u ON c.utilisateur = u.id WHERE c.article_id=? ORDER BY c.date_commentaire DESC";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            try (ResultSet rs = pst.executeQuery()) {
                List<Commentaire> liste = new ArrayList<>();
                while (rs.next()) {
                    Commentaire c = new Commentaire();
                    c.setId(rs.getInt("id"));
                    c.setContenu(rs.getString("contenu"));
                    c.setDateCommentaire(rs.getTimestamp("date_commentaire").toLocalDateTime());
                    c.setUtilisateur(rs.getInt("utilisateur"));
                    c.setArticleId(rs.getInt("article_id"));

                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String utilisateurNom;
                    if (prenom != null && nom != null) {
                        utilisateurNom = prenom + " " + nom;
                    } else if (prenom != null) {
                        utilisateurNom = prenom;
                    } else if (nom != null) {
                        utilisateurNom = nom;
                    } else {
                        utilisateurNom = "Utilisateur " + rs.getInt("utilisateur");
                    }
                    c.setUtilisateurNom(utilisateurNom);
                    liste.add(c);
                }
                return liste;
            }
        }
    }

    public void supprimerParArticle(int articleId) throws SQLException {
        String req = "DELETE FROM commentaire WHERE article_id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            pst.executeUpdate();
        }
    }
}