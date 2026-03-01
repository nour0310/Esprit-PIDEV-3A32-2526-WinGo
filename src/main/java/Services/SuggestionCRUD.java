package Services;

import Entites.Suggestion;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuggestionCRUD {
    private Connection cnx;

    public SuggestionCRUD() {
        cnx = MyBD.getInstance().getConn();
    }

    // CREATE
    public void ajouter(Suggestion suggestion) {
        String req = "INSERT INTO suggestion (id_user, sujet, description, categorie, id_reclamation) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, suggestion.getId_user());
            ps.setString(2, suggestion.getSujet());
            ps.setString(3, suggestion.getDescription());
            ps.setString(4, suggestion.getCategorie());

            if (suggestion.getId_reclamation() != null) {
                ps.setInt(5, suggestion.getId_reclamation());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    suggestion.setId_suggestion(generatedKeys.getInt(1));
                }
                System.out.println("✅ Suggestion added successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding suggestion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // READ ALL
    public List<Suggestion> afficherTous() {
        List<Suggestion> list = new ArrayList<>();
        String req = "SELECT * FROM suggestion ORDER BY date_suggestion DESC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Suggestion s = new Suggestion();
                s.setId_suggestion(rs.getInt("id_suggestion"));
                s.setId_user(rs.getInt("id_user"));
                s.setSujet(rs.getString("sujet"));
                s.setDescription(rs.getString("description"));
                s.setCategorie(rs.getString("categorie"));
                s.setDate_suggestion(rs.getDate("date_suggestion"));
                s.setStatut(rs.getString("statut"));
                s.setReponse_admin(rs.getString("reponse_admin"));
                s.setDate_reponse(rs.getDate("date_reponse"));
                s.setId_reclamation(rs.getInt("id_reclamation") == 0 ? null : rs.getInt("id_reclamation"));

                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching suggestions: " + e.getMessage());
        }
        return list;
    }

    // READ BY ID
    public Suggestion getById(int id) {
        String req = "SELECT * FROM suggestion WHERE id_suggestion = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Suggestion s = new Suggestion();
                s.setId_suggestion(rs.getInt("id_suggestion"));
                s.setId_user(rs.getInt("id_user"));
                s.setSujet(rs.getString("sujet"));
                s.setDescription(rs.getString("description"));
                s.setCategorie(rs.getString("categorie"));
                s.setDate_suggestion(rs.getDate("date_suggestion"));
                s.setStatut(rs.getString("statut"));
                s.setReponse_admin(rs.getString("reponse_admin"));
                s.setDate_reponse(rs.getDate("date_reponse"));
                s.setId_reclamation(rs.getInt("id_reclamation") == 0 ? null : rs.getInt("id_reclamation"));
                return s;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching suggestion: " + e.getMessage());
        }
        return null;
    }

    // READ BY USER
    public List<Suggestion> getByUser(int userId) {
        List<Suggestion> list = new ArrayList<>();
        String req = "SELECT * FROM suggestion WHERE id_user = ? ORDER BY date_suggestion DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Suggestion s = new Suggestion();
                s.setId_suggestion(rs.getInt("id_suggestion"));
                s.setId_user(rs.getInt("id_user"));
                s.setSujet(rs.getString("sujet"));
                s.setDescription(rs.getString("description"));
                s.setCategorie(rs.getString("categorie"));
                s.setDate_suggestion(rs.getDate("date_suggestion"));
                s.setStatut(rs.getString("statut"));
                s.setReponse_admin(rs.getString("reponse_admin"));
                s.setDate_reponse(rs.getDate("date_reponse"));
                s.setId_reclamation(rs.getInt("id_reclamation") == 0 ? null : rs.getInt("id_reclamation"));

                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching user suggestions: " + e.getMessage());
        }
        return list;
    }

    // READ BY CATEGORY
    public List<Suggestion> getByCategorie(String categorie) {
        List<Suggestion> list = new ArrayList<>();
        String req = "SELECT * FROM suggestion WHERE categorie = ? ORDER BY date_suggestion DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, categorie);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Suggestion s = new Suggestion();
                s.setId_suggestion(rs.getInt("id_suggestion"));
                s.setId_user(rs.getInt("id_user"));
                s.setSujet(rs.getString("sujet"));
                s.setDescription(rs.getString("description"));
                s.setCategorie(rs.getString("categorie"));
                s.setDate_suggestion(rs.getDate("date_suggestion"));
                s.setStatut(rs.getString("statut"));
                s.setReponse_admin(rs.getString("reponse_admin"));
                s.setDate_reponse(rs.getDate("date_reponse"));
                s.setId_reclamation(rs.getInt("id_reclamation") == 0 ? null : rs.getInt("id_reclamation"));

                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching suggestions by category: " + e.getMessage());
        }
        return list;
    }

    // UPDATE
    public void modifier(Suggestion suggestion) {
        String req = "UPDATE suggestion SET sujet=?, description=?, categorie=?, " +
                "id_reclamation=? WHERE id_suggestion=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, suggestion.getSujet());
            ps.setString(2, suggestion.getDescription());
            ps.setString(3, suggestion.getCategorie());

            if (suggestion.getId_reclamation() != null) {
                ps.setInt(4, suggestion.getId_reclamation());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, suggestion.getId_suggestion());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Suggestion updated successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating suggestion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // UPDATE STATUS AND RESPONSE
    public void repondre(int id, String reponse, String nouveauStatut) {
        String req = "UPDATE suggestion SET reponse_admin=?, date_reponse=NOW(), statut=? WHERE id_suggestion=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, reponse);
            ps.setString(2, nouveauStatut);
            ps.setInt(3, id);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Response added to suggestion!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error responding to suggestion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // DELETE
    public void supprimer(int id) {
        String req = "DELETE FROM suggestion WHERE id_suggestion=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);

            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Suggestion deleted successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting suggestion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // SEARCH
    public List<Suggestion> rechercher(String terme) {
        List<Suggestion> list = new ArrayList<>();
        String req = "SELECT * FROM suggestion WHERE sujet LIKE ? OR description LIKE ? OR categorie LIKE ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            String searchTerm = "%" + terme + "%";
            ps.setString(1, searchTerm);
            ps.setString(2, searchTerm);
            ps.setString(3, searchTerm);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Suggestion s = new Suggestion();
                s.setId_suggestion(rs.getInt("id_suggestion"));
                s.setId_user(rs.getInt("id_user"));
                s.setSujet(rs.getString("sujet"));
                s.setDescription(rs.getString("description"));
                s.setCategorie(rs.getString("categorie"));
                s.setDate_suggestion(rs.getDate("date_suggestion"));
                s.setStatut(rs.getString("statut"));
                s.setReponse_admin(rs.getString("reponse_admin"));
                s.setDate_reponse(rs.getDate("date_reponse"));
                s.setId_reclamation(rs.getInt("id_reclamation") == 0 ? null : rs.getInt("id_reclamation"));

                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error searching suggestions: " + e.getMessage());
        }
        return list;
    }

    // LINK SUGGESTION TO RECLAMATION
    public void lierAReclamation(int idSuggestion, int idReclamation) {
        String req = "UPDATE suggestion SET id_reclamation = ? WHERE id_suggestion = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, idReclamation);
            ps.setInt(2, idSuggestion);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Suggestion linked to reclamation!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error linking suggestion to reclamation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}