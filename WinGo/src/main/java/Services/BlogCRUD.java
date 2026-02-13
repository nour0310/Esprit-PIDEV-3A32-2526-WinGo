package Services;

import Entites.Blog;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogCRUD implements IntrefaceCRUD<Blog> {

    Connection conn;

    public BlogCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Blog b) throws SQLException {
        String req = "INSERT INTO articles (titre, contenu, image, auteur, region, categorie) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, b.getTitre());
        ps.setString(2, b.getContenu());
        ps.setString(3, b.getImage());
        ps.setString(4, b.getAuteur());
        ps.setString(5, b.getRegion());
        ps.setString(6, b.getCategorie());
        ps.executeUpdate();
        System.out.println("Blog ajouté !");
    }

    @Override
    public void modifier(Blog b) throws SQLException {
        String req = "UPDATE articles SET titre=?, contenu=?, image=?, auteur=?, region=?, categorie=? WHERE id_article=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, b.getTitre());
        ps.setString(2, b.getContenu());
        ps.setString(3, b.getImage());
        ps.setString(4, b.getAuteur());
        ps.setString(5, b.getRegion());
        ps.setString(6, b.getCategorie());
        ps.setInt(7, b.getId_article());
        ps.executeUpdate();
        System.out.println("Blog modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM articles WHERE id_article=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Blog supprimé !");
    }

    @Override
    public List<Blog> afficher() throws SQLException {
        List<Blog> liste = new ArrayList<>();
        String req = "SELECT * FROM articles ORDER BY id_article DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Blog b = new Blog();
            b.setId_article(rs.getInt("id_article"));
            b.setTitre(rs.getString("titre"));
            b.setContenu(rs.getString("contenu"));
            b.setImage(rs.getString("image"));
            b.setAuteur(rs.getString("auteur"));
            b.setRegion(rs.getString("region"));
            b.setCategorie(rs.getString("categorie"));
            liste.add(b);
        }
        return liste;
    }
}