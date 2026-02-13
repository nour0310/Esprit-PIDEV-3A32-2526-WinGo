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
        String req = "INSERT INTO commentaires (contenu, date_commentaire, id_article, utilisateur) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, c.getContenu());
        ps.setDate(2, new java.sql.Date(c.getDateCommentaire().getTime()));
        ps.setInt(3, c.getBlogId());
        ps.setString(4, c.getUtilisateur());
        ps.executeUpdate();
        System.out.println("Commentaire ajouté !");
    }

    @Override
    public void modifier(Commentaire c) throws SQLException {
        String req = "UPDATE commentaires SET contenu=?, date_commentaire=?, id_article=?, utilisateur=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, c.getContenu());
        ps.setDate(2, new java.sql.Date(c.getDateCommentaire().getTime()));
        ps.setInt(3, c.getBlogId());
        ps.setString(4, c.getUtilisateur());
        ps.setInt(5, c.getId());
        ps.executeUpdate();
        System.out.println("Commentaire modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM commentaires WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Commentaire supprimé !");
    }

    @Override
    public List<Commentaire> afficher() throws SQLException {
        List<Commentaire> liste = new ArrayList<>();
        String req = "SELECT * FROM commentaires ORDER BY date_commentaire DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Commentaire c = new Commentaire();
            c.setId(rs.getInt("id"));
            c.setContenu(rs.getString("contenu"));
            c.setDateCommentaire(rs.getDate("date_commentaire"));
            c.setBlogId(rs.getInt("id_article"));
            c.setUtilisateur(rs.getString("utilisateur"));
            liste.add(c);
        }
        return liste;
    }

    // Méthode spécifique pour récupérer les commentaires d’un blog
    public List<Commentaire> getCommentsByBlogId(int blogId) throws SQLException {
        List<Commentaire> liste = new ArrayList<>();
        String req = "SELECT * FROM commentaires WHERE id_article=? ORDER BY date_commentaire DESC";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, blogId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Commentaire c = new Commentaire();
            c.setId(rs.getInt("id"));
            c.setContenu(rs.getString("contenu"));
            c.setDateCommentaire(rs.getDate("date_commentaire"));
            c.setBlogId(rs.getInt("id_article"));
            c.setUtilisateur(rs.getString("utilisateur"));
            liste.add(c);
        }
        return liste;
    }
}