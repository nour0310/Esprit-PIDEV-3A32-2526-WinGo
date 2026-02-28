package Services;

import Entites.Commentaire;
import Utils.MyBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CommentaireCRUD {

    private Connection conn;

    public CommentaireCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    // Ajouter un commentaire (racine ou réponse)
    public void ajouter(Commentaire commentaire) throws SQLException {
        String req = "INSERT INTO commentaire (contenu, date_commentaire, utilisateur, article_id, parent_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, commentaire.getContenu());
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(3, commentaire.getUtilisateur());
            pst.setInt(4, commentaire.getArticleId());
            if (commentaire.getParentId() != null) {
                pst.setInt(5, commentaire.getParentId());
            } else {
                pst.setNull(5, Types.INTEGER);
            }
            pst.executeUpdate();
        }
        System.out.println("Commentaire ajouté !");
    }

    // Modifier un commentaire
    public void modifier(Commentaire commentaire) throws SQLException {
        String req = "UPDATE commentaire SET contenu=?, utilisateur=?, article_id=?, parent_id=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, commentaire.getContenu());
            pst.setInt(2, commentaire.getUtilisateur());
            pst.setInt(3, commentaire.getArticleId());
            if (commentaire.getParentId() != null) {
                pst.setInt(4, commentaire.getParentId());
            } else {
                pst.setNull(4, Types.INTEGER);
            }
            pst.setInt(5, commentaire.getId());
            pst.executeUpdate();
        }
        System.out.println("Commentaire modifié !");
    }

    // Supprimer un commentaire (les réponses seront supprimées par CASCADE)
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM commentaire WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
        System.out.println("Commentaire supprimé !");
    }

    // Supprimer tous les commentaires d'un article
    public void supprimerParArticle(int articleId) throws SQLException {
        String req = "DELETE FROM commentaire WHERE article_id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            pst.executeUpdate();
        }
    }

    // Récupérer tous les commentaires d'un article (sans hiérarchie)
    public List<Commentaire> getCommentsByArticle(int articleId) throws SQLException {
        String req = "SELECT c.*, u.nom, u.prenom FROM commentaire c LEFT JOIN utilisateur u ON c.utilisateur = u.id WHERE c.article_id=? ORDER BY c.date_commentaire DESC";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            try (ResultSet rs = pst.executeQuery()) {
                List<Commentaire> liste = new ArrayList<>();
                while (rs.next()) {
                    liste.add(mapResultSetToCommentaire(rs));
                }
                return liste;
            }
        }
    }

    // Récupérer les commentaires d'un article de manière hiérarchique (racines + réponses)
    public List<Commentaire> getHierarchicalComments(int articleId) throws SQLException {
        // 1. Récupérer tous les commentaires de l'article triés par date croissante
        List<Commentaire> allComments = new ArrayList<>();
        String req = "SELECT c.*, u.nom, u.prenom FROM commentaire c LEFT JOIN utilisateur u ON c.utilisateur = u.id WHERE c.article_id=? ORDER BY c.date_commentaire ASC";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, articleId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    allComments.add(mapResultSetToCommentaire(rs));
                }
            }
        }

        // 2. Construire l'arbre : séparer les racines et attacher les réponses
        List<Commentaire> roots = new ArrayList<>();
        Map<Integer, Commentaire> map = new HashMap<>();

        // Indexer tous les commentaires par ID
        for (Commentaire c : allComments) {
            map.put(c.getId(), c);
            // Initialiser la liste des réponses
            c.setReplies(new ArrayList<>());
        }

        // Organiser les relations parent-enfant
        for (Commentaire c : allComments) {
            if (c.getParentId() == null || c.getParentId() == 0) {
                roots.add(c);
            } else {
                Commentaire parent = map.get(c.getParentId());
                if (parent != null) {
                    parent.getReplies().add(c);
                } else {
                    // parent non trouvé (cas improbable), on le met en racine
                    roots.add(c);
                }
            }
        }

        return roots;
    }

    // Récupérer un commentaire par son ID
    public Commentaire getOne(int id) throws SQLException {
        String req = "SELECT c.*, u.nom, u.prenom FROM commentaire c LEFT JOIN utilisateur u ON c.utilisateur = u.id WHERE c.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCommentaire(rs);
                }
            }
        }
        return null;
    }

    // Récupérer les N derniers commentaires (pour l'activité récente)
    public List<Commentaire> getRecentComments(int limit) throws SQLException {
        String req = "SELECT c.*, u.nom, u.prenom FROM commentaire c LEFT JOIN utilisateur u ON c.utilisateur = u.id ORDER BY c.date_commentaire DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Commentaire> liste = new ArrayList<>();
                while (rs.next()) {
                    liste.add(mapResultSetToCommentaire(rs));
                }
                return liste;
            }
        }
    }

    // Récupérer tous les commentaires (pour l'affichage global)
    public List<Commentaire> afficher() throws SQLException {
        String req = "SELECT c.*, u.nom, u.prenom, a.titre as article_titre FROM commentaire c " +
                     "LEFT JOIN utilisateur u ON c.utilisateur = u.id " +
                     "LEFT JOIN article a ON c.article_id = a.id " +
                     "ORDER BY c.date_commentaire DESC";
        List<Commentaire> liste = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                liste.add(mapResultSetToCommentaire(rs));
            }
        }
        return liste;
    }

    // Méthode utilitaire pour mapper un ResultSet à un objet Commentaire
    private Commentaire mapResultSetToCommentaire(ResultSet rs) throws SQLException {
        Commentaire c = new Commentaire();
        c.setId(rs.getInt("id"));
        c.setContenu(rs.getString("contenu"));
        c.setDateCommentaire(rs.getTimestamp("date_commentaire").toLocalDateTime());
        c.setUtilisateur(rs.getInt("utilisateur"));
        c.setArticleId(rs.getInt("article_id"));
        
        try {
            String artTitre = rs.getString("article_titre");
            if (artTitre != null) c.setArticleTitre(artTitre);
        } catch (SQLException e) {
            // colonne peut-être absente dans certains resultsets
        }

        int parentId = rs.getInt("parent_id");
        if (rs.wasNull()) {
            c.setParentId(null);
        } else {
            c.setParentId(parentId);
        }

        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        if (prenom != null && nom != null) {
            c.setUtilisateurNom(prenom + " " + nom);
        } else if (prenom != null) {
            c.setUtilisateurNom(prenom);
        } else if (nom != null) {
            c.setUtilisateurNom(nom);
        } else {
            c.setUtilisateurNom("Utilisateur " + rs.getInt("utilisateur"));
        }

        return c;
    }
}