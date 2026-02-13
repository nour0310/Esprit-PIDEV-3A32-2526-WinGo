package Services;

import Entites.Commentaire;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireCRUD implements IntrefaceCRUD<Commentaire> {

    Connection conn;

    public CommentaireCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Commentaire c) throws SQLException {
        String req = "INSERT INTO commentaire (contenu, date_commentaire, id_article, utilisateur) " +
                "VALUES ('" + c.getContenu() + "', '" + new java.sql.Date(c.getDateCommentaire().getTime()) + "', " +
                c.getId_article() + ", " + c.getUtilisateur() + ")";
        Statement st = conn.createStatement();
        st.executeUpdate(req);
        System.out.println("Commentaire ajouté !");
    }

    @Override
    public void modifier(Commentaire c) throws SQLException {
        String req = "UPDATE commentaire SET contenu=?, date_commentaire=?, id_article=?, utilisateur=? WHERE id_commentaire=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, c.getContenu());
        pst.setDate(2, new java.sql.Date(c.getDateCommentaire().getTime()));
        pst.setInt(3, c.getId_article());
        pst.setInt(4, c.getUtilisateur());
        pst.setInt(5, c.getId_commentaire());
        pst.executeUpdate();
        System.out.println("Commentaire modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM commentaire WHERE id_commentaire=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Commentaire supprimé !");
    }

    @Override
    public List<Commentaire> afficher() throws SQLException {
        String req = "SELECT * FROM commentaire ORDER BY date_commentaire DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Commentaire> listeCommentaires = new ArrayList<>();

        while (rs.next()) {
            Commentaire c = new Commentaire();
            c.setId_commentaire(rs.getInt("id_commentaire"));
            c.setContenu(rs.getString("contenu"));
            c.setDateCommentaire(rs.getDate("date_commentaire"));
            c.setId_article(rs.getInt("id_article"));
            c.setUtilisateur(rs.getInt("utilisateur"));

            listeCommentaires.add(c);
        }

        return listeCommentaires;
    }

    // Méthode pour récupérer tous les commentaires d’un article
    public List<Commentaire> getCommentsByBlogId(int id_article) throws SQLException {
        String req = "SELECT * FROM commentaire WHERE id_article=" + id_article + " ORDER BY date_commentaire DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Commentaire> listeCommentaires = new ArrayList<>();

        while (rs.next()) {
            Commentaire c = new Commentaire();
            c.setId_commentaire(rs.getInt("id_commentaire"));
            c.setContenu(rs.getString("contenu"));
            c.setDateCommentaire(rs.getDate("date_commentaire"));
            c.setId_article(rs.getInt("id_article"));
            c.setUtilisateur(rs.getInt("utilisateur"));

            listeCommentaires.add(c);
        }

        return listeCommentaires;
    }
}