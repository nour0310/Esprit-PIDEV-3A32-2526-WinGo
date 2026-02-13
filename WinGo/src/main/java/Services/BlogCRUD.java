package Services;

import Entites.Blog;
import Utils.MyBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BlogCRUD {

    private Connection conn;

    public BlogCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    public void ajouter(Blog blog) throws SQLException {
        String req = "INSERT INTO article (titre, contenu, date_publication, auteur) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, blog.getTitre());
            pst.setString(2, blog.getContenu());
            pst.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(4, blog.getAuteur());
            pst.executeUpdate();
        }
        System.out.println("Blog ajouté !");
    }

    public void modifier(Blog blog) throws SQLException {
        String req = "UPDATE article SET titre=?, contenu=?, auteur=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, blog.getTitre());
            pst.setString(2, blog.getContenu());
            pst.setInt(3, blog.getAuteur());
            pst.setInt(4, blog.getId());
            pst.executeUpdate();
        }
        System.out.println("Blog modifié !");
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM article WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
        System.out.println("Blog supprimé !");
    }

    public List<Blog> afficher() throws SQLException {
        String req = "SELECT a.*, u.nom, u.prenom FROM article a LEFT JOIN utilisateur u ON a.auteur = u.id ORDER BY a.date_publication DESC";
        List<Blog> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Blog b = new Blog();
                b.setId(rs.getInt("id"));
                b.setTitre(rs.getString("titre"));
                b.setContenu(rs.getString("contenu"));
                b.setDatePublication(rs.getTimestamp("date_publication").toLocalDateTime());
                b.setAuteur(rs.getInt("auteur"));
                // On peut aussi récupérer le nom de l'auteur si on veut l'afficher
                String auteurNom = rs.getString("nom") + " " + rs.getString("prenom");
                // Optionnel : ajouter un setter pour auteurNom dans Blog (non persistant)
                // b.setAuteurNom(auteurNom);
                list.add(b);
            }
        }
        return list;
    }

    // Méthode utilitaire pour récupérer un blog par son ID
    public Blog getById(int id) throws SQLException {
        String req = "SELECT * FROM article WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Blog b = new Blog();
                    b.setId(rs.getInt("id"));
                    b.setTitre(rs.getString("titre"));
                    b.setContenu(rs.getString("contenu"));
                    b.setDatePublication(rs.getTimestamp("date_publication").toLocalDateTime());
                    b.setAuteur(rs.getInt("auteur"));
                    return b;
                }
            }
        }
        return null;
    }
}