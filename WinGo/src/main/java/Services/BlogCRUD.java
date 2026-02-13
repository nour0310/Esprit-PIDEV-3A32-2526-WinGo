package Services;

import Entites.Blog;
import Utils.MyBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BlogCRUD implements IntrefaceCRUD<Blog> {

    private Connection conn;

    public BlogCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Blog blog) throws SQLException {
        String req = "INSERT INTO article (titre, contenu, image, region, categorie, auteur, date_publication) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, blog.getTitre());
        pst.setString(2, blog.getContenu());
        pst.setString(3, blog.getImage());
        pst.setString(4, blog.getRegion());
        pst.setString(5, blog.getCategorie());
        pst.setInt(6, blog.getAuteur());
        pst.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
        pst.executeUpdate();
        System.out.println("Blog ajouté !");
    }

    @Override
    public void modifier(Blog blog) throws SQLException {
        String req = "UPDATE article SET titre=?, contenu=?, image=?, region=?, categorie=?, auteur=? WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, blog.getTitre());
        pst.setString(2, blog.getContenu());
        pst.setString(3, blog.getImage());
        pst.setString(4, blog.getRegion());
        pst.setString(5, blog.getCategorie());
        pst.setInt(6, blog.getAuteur());
        pst.setInt(7, blog.getId());
        pst.executeUpdate();
        System.out.println("Blog modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM article WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Blog supprimé !");
    }

    @Override
    public List<Blog> afficher() throws SQLException {
        String req = "SELECT a.*, u.nom, u.prenom FROM article a " +
                "LEFT JOIN utilisateur u ON a.auteur = u.id ORDER BY a.date_publication DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Blog> liste = new ArrayList<>();

        while (rs.next()) {
            Blog b = new Blog();
            b.setId(rs.getInt("id"));
            b.setTitre(rs.getString("titre"));
            b.setContenu(rs.getString("contenu"));
            b.setDatePublication(rs.getTimestamp("date_publication").toLocalDateTime());
            b.setImage(rs.getString("image"));
            b.setRegion(rs.getString("region"));
            b.setCategorie(rs.getString("categorie"));
            b.setAuteur(rs.getInt("auteur"));
            // nom complet de l'auteur
            String auteurNom = rs.getString("nom") + " " + rs.getString("prenom");
            b.setAuteurNom(auteurNom);
            liste.add(b);
        }
        return liste;
    }

    // Méthode pour récupérer un blog par son ID (optionnelle)
    public Blog getById(int id) throws SQLException {
        String req = "SELECT a.*, u.nom, u.prenom FROM article a " +
                "LEFT JOIN utilisateur u ON a.auteur = u.id WHERE a.id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            Blog b = new Blog();
            b.setId(rs.getInt("id"));
            b.setTitre(rs.getString("titre"));
            b.setContenu(rs.getString("contenu"));
            b.setDatePublication(rs.getTimestamp("date_publication").toLocalDateTime());
            b.setImage(rs.getString("image"));
            b.setRegion(rs.getString("region"));
            b.setCategorie(rs.getString("categorie"));
            b.setAuteur(rs.getInt("auteur"));
            b.setAuteurNom(rs.getString("nom") + " " + rs.getString("prenom"));
            return b;
        }
        return null;
    }
}