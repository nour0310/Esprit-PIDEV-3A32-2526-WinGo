package Services;

import Entites.Blog;
import Entites.Tag;
import Utils.MyBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogCRUD {

    private Connection conn;

    public BlogCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    /**
     * Ajoute un article dans la base de données et met à jour son ID avec la valeur générée.
     * @param blog l'article à ajouter (son ID sera modifié)
     * @throws SQLException
     */
    public void ajouter(Blog blog) throws SQLException {
        String req = "INSERT INTO article (titre, contenu, date_publication, auteur, image, region, categorie) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, blog.getTitre());
            pst.setString(2, blog.getContenu());
            pst.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(4, blog.getAuteur());
            pst.setString(5, blog.getImage());
            pst.setString(6, blog.getRegion());
            pst.setString(7, blog.getCategorie());
            pst.executeUpdate();

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    blog.setId(generatedKeys.getInt(1));
                }
            }
        }
        System.out.println("Blog ajouté avec ID : " + blog.getId());
    }

    /**
     * Modifie un article existant (sans toucher aux tags).
     * @param blog l'article avec les nouvelles données
     * @throws SQLException
     */
    public void modifier(Blog blog) throws SQLException {
        String req = "UPDATE article SET titre=?, contenu=?, auteur=?, image=?, region=?, categorie=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, blog.getTitre());
            pst.setString(2, blog.getContenu());
            pst.setInt(3, blog.getAuteur());
            pst.setString(4, blog.getImage());
            pst.setString(5, blog.getRegion());
            pst.setString(6, blog.getCategorie());
            pst.setInt(7, blog.getId());
            pst.executeUpdate();
        }
        System.out.println("Blog modifié !");
    }

    /**
     * Supprime un article par son ID (les tags associés seront supprimés automatiquement par CASCADE).
     * @param id
     * @throws SQLException
     */
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM article WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
        System.out.println("Blog supprimé !");
    }

    /**
     * Récupère tous les articles avec leurs auteurs (nom complet) et leurs tags associés.
     * @return liste d'articles (chaque article contient sa liste de tags)
     * @throws SQLException
     */
    public List<Blog> afficher() throws SQLException {
        // 1. Récupérer tous les articles avec les infos auteur
        String reqArticles = "SELECT a.*, u.nom, u.prenom FROM article a LEFT JOIN utilisateur u ON a.auteur = u.id ORDER BY a.date_publication DESC";
        List<Blog> articles = new ArrayList<>();
        Map<Integer, Blog> mapArticles = new HashMap<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(reqArticles)) {
            while (rs.next()) {
                Blog b = new Blog();
                b.setId(rs.getInt("id"));
                b.setTitre(rs.getString("titre"));
                b.setContenu(rs.getString("contenu"));
                b.setDatePublication(rs.getTimestamp("date_publication").toLocalDateTime());
                b.setAuteur(rs.getInt("auteur"));
                b.setImage(rs.getString("image"));
                b.setRegion(rs.getString("region"));
                b.setCategorie(rs.getString("categorie"));

                // Construction du nom complet de l'auteur
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String auteurNom;
                if (prenom != null && nom != null) {
                    auteurNom = prenom + " " + nom;
                } else if (prenom != null) {
                    auteurNom = prenom;
                } else if (nom != null) {
                    auteurNom = nom;
                } else {
                    auteurNom = "Utilisateur " + rs.getInt("auteur");
                }
                b.setAuteurNom(auteurNom);

                articles.add(b);
                mapArticles.put(b.getId(), b);
            }
        }

        // 2. Récupérer tous les tags associés aux articles (si la table article_tag existe)
        if (!articles.isEmpty()) {
            String reqTags = "SELECT at.article_id, t.id, t.nom FROM article_tag at JOIN tag t ON at.tag_id = t.id WHERE at.article_id IN (";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < articles.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("?");
            }
            reqTags += sb.toString() + ")";

            try (PreparedStatement pst = conn.prepareStatement(reqTags)) {
                int index = 1;
                for (Blog b : articles) {
                    pst.setInt(index++, b.getId());
                }
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        int articleId = rs.getInt("article_id");
                        Tag tag = new Tag();
                        tag.setId(rs.getInt("id"));
                        tag.setNom(rs.getString("nom"));
                        Blog b = mapArticles.get(articleId);
                        if (b != null) {
                            b.getTags().add(tag);
                        }
                    }
                }
            } catch (SQLException e) {
                // Si la table n'existe pas, on ignore (pas de tags)
                System.err.println("Note : table article_tag peut-être absente, aucun tag chargé.");
            }
        }

        return articles;
    }

    /**
     * Récupère un article par son ID (sans ses tags).
     * @param id
     * @return l'article ou null si non trouvé
     * @throws SQLException
     */
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
                    b.setImage(rs.getString("image"));
                    b.setRegion(rs.getString("region"));
                    b.setCategorie(rs.getString("categorie"));
                    return b;
                }
            }
        }
        return null;
    }
}