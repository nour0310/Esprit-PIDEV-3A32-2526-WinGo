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
        String req = "INSERT INTO article (titre, contenu, image, auteur, region, categorie) " +
                "VALUES ('" + b.getTitre() + "', '" + b.getContenu() + "', '" + b.getImage() + "', '" +
                b.getAuteur() + "', '" + b.getRegion() + "', '" + b.getCategorie() + "')";
        Statement st = conn.createStatement();
        st.executeUpdate(req);
        System.out.println("Blog ajouté !");
    }

    @Override
    public void modifier(Blog b) throws SQLException {
        String req = "UPDATE article SET titre=?, contenu=?, image=?, auteur=?, region=?, categorie=? WHERE id_article=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, b.getTitre());
        pst.setString(2, b.getContenu());
        pst.setString(3, b.getImage());
        pst.setString(4, b.getAuteur());
        pst.setString(5, b.getRegion());
        pst.setString(6, b.getCategorie());
        pst.setInt(7, b.getId_article());
        pst.executeUpdate();
        System.out.println("Blog modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM article WHERE id_article=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Blog supprimé !");
    }

    @Override
    public List<Blog> afficher() throws SQLException {
        String req = "SELECT * FROM article ORDER BY id_article DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Blog> listeBlogs = new ArrayList<>();

        while (rs.next()) {
            Blog b = new Blog();
            b.setId_article(rs.getInt("id_article"));
            b.setTitre(rs.getString("titre"));
            b.setContenu(rs.getString("contenu"));
            b.setImage(rs.getString("image"));
            b.setAuteur(rs.getString("auteur"));
            b.setRegion(rs.getString("region"));
            b.setCategorie(rs.getString("categorie"));

            listeBlogs.add(b);
        }

        return listeBlogs;
    }
}