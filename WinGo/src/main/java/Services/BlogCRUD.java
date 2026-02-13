package Services;

import Entites.Blog;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogCRUD implements IntrefaceCRUD<Blog> {

    private Connection conn;

    public BlogCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Blog b) throws SQLException {
        String sql = "INSERT INTO article (titre, contenu, auteur) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, b.getTitre());
        ps.setString(2, b.getContenu());
        ps.setInt(3, b.getAuteur());   // FK utilisateur.id
        ps.executeUpdate();
    }

    @Override
    public void modifier(Blog b) throws SQLException {
        String sql = "UPDATE article SET titre=?, contenu=?, auteur=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, b.getTitre());
        ps.setString(2, b.getContenu());
        ps.setInt(3, b.getAuteur());
        ps.setInt(4, b.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM article WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Blog> afficher() throws SQLException {
        List<Blog> liste = new ArrayList<>();
        String sql = "SELECT * FROM article ORDER BY id DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Blog b = new Blog();
            b.setId(rs.getInt("id"));
            b.setTitre(rs.getString("titre"));
            b.setContenu(rs.getString("contenu"));
            b.setAuteur(rs.getInt("auteur"));
            liste.add(b);
        }
        return liste;
    }
}