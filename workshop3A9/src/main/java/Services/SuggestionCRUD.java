package Services;

import Entites.Suggestion;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuggestionCRUD {
    private Connection cnx;

    public SuggestionCRUD() {
        cnx = MyBD.getInstance().getCnx();
    }

    public void ajouter(Suggestion s) {
        String sql = "INSERT INTO suggestion (id_user, sujet, description, categorie, date_suggestion, statut, id_reclamation) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getId_user());
            ps.setString(2, s.getSujet());
            ps.setString(3, s.getDescription());
            ps.setString(4, s.getCategorie());
            ps.setDate(5, s.getDate_suggestion());
            ps.setString(6, s.getStatut());
            if (s.getId_reclamation() != null) {
                ps.setInt(7, s.getId_reclamation());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                s.setId_suggestion(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout suggestion: " + e.getMessage());
        }
    }

    public List<Suggestion> afficherTous() {
        List<Suggestion> list = new ArrayList<>();
        String sql = "SELECT * FROM suggestion";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Suggestion s = new Suggestion();
                s.setId_suggestion(rs.getInt("id_suggestion"));
                s.setId_user(rs.getInt("id_user"));
                s.setSujet(rs.getString("sujet"));
                s.setDescription(rs.getString("description"));
                s.setCategorie(rs.getString("categorie"));
                s.setDate_suggestion(rs.getDate("date_suggestion"));
                s.setStatut(rs.getString("statut"));
                int recId = rs.getInt("id_reclamation");
                if (!rs.wasNull())
                    s.setId_reclamation(recId);
                s.setReponse_admin(rs.getString("reponse_admin"));
                s.setDate_reponse(rs.getDate("date_reponse"));
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Erreur affichage suggestions: " + e.getMessage());
        }
        return list;
    }

    public void modifier(Suggestion s) {
        String sql = "UPDATE suggestion SET sujet=?, description=?, categorie=? WHERE id_suggestion=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, s.getSujet());
            ps.setString(2, s.getDescription());
            ps.setString(3, s.getCategorie());
            ps.setInt(4, s.getId_suggestion());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur modification suggestion: " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM suggestion WHERE id_suggestion=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur suppression suggestion: " + e.getMessage());
        }
    }

    public void repondre(int id, String reponse, String statut) {
        String sql = "UPDATE suggestion SET reponse_admin=?, statut=?, date_reponse=? WHERE id_suggestion=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, reponse);
            ps.setString(2, statut);
            ps.setDate(3, new Date(System.currentTimeMillis()));
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur reponse suggestion: " + e.getMessage());
        }
    }

    public List<Suggestion> getByCategorie(String cat) {
        List<Suggestion> list = new ArrayList<>();
        String sql = "SELECT * FROM suggestion WHERE categorie = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, cat);
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
                int recId = rs.getInt("id_reclamation");
                if (!rs.wasNull())
                    s.setId_reclamation(recId);
                s.setReponse_admin(rs.getString("reponse_admin"));
                s.setDate_reponse(rs.getDate("date_reponse"));
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche par categorie: " + e.getMessage());
        }
        return list;
    }

    public List<Suggestion> rechercher(String term) {
        List<Suggestion> list = new ArrayList<>();
        String sql = "SELECT * FROM suggestion WHERE sujet LIKE ? OR description LIKE ? OR categorie LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String t = "%" + term + "%";
            ps.setString(1, t);
            ps.setString(2, t);
            ps.setString(3, t);
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
                int recId = rs.getInt("id_reclamation");
                if (!rs.wasNull())
                    s.setId_reclamation(recId);
                s.setReponse_admin(rs.getString("reponse_admin"));
                s.setDate_reponse(rs.getDate("date_reponse"));
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche suggestion: " + e.getMessage());
        }
        return list;
    }
}
