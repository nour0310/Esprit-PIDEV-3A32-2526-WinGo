package Services;

import Entites.Reclamation;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationCRUD {
    private Connection cnx;

    public ReclamationCRUD() {
        cnx = MyBD.getInstance().getCnx();
    }

    public void ajouter(Reclamation r) {
        String sql = "INSERT INTO reclamation (id_user, type_reclamation, sujet, description, date_reclamation, statut, priorite, piece_jointe) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getId_user());
            ps.setString(2, r.getType_reclamation());
            ps.setString(3, r.getSujet());
            ps.setString(4, r.getDescription());
            ps.setDate(5, r.getDate_reclamation());
            ps.setString(6, r.getStatut());
            ps.setString(7, r.getPriorite());
            ps.setString(8, r.getPiece_jointe());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                r.setId_reclamation(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout reclamation: " + e.getMessage());
        }
    }

    public List<Reclamation> afficherTous() {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId_reclamation(rs.getInt("id_reclamation"));
                r.setId_user(rs.getInt("id_user"));
                r.setType_reclamation(rs.getString("type_reclamation"));
                r.setSujet(rs.getString("sujet"));
                r.setDescription(rs.getString("description"));
                r.setDate_reclamation(rs.getDate("date_reclamation"));
                r.setStatut(rs.getString("statut"));
                r.setPriorite(rs.getString("priorite"));
                r.setReponse_admin(rs.getString("reponse_admin"));
                r.setDate_reponse(rs.getDate("date_reponse"));
                r.setPiece_jointe(rs.getString("piece_jointe"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur affichage reclamations: " + e.getMessage());
        }
        return list;
    }

    public void modifier(Reclamation r) {
        String sql = "UPDATE reclamation SET type_reclamation=?, sujet=?, description=?, priorite=?, piece_jointe=? WHERE id_reclamation=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getType_reclamation());
            ps.setString(2, r.getSujet());
            ps.setString(3, r.getDescription());
            ps.setString(4, r.getPriorite());
            ps.setString(5, r.getPiece_jointe());
            ps.setInt(6, r.getId_reclamation());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur modification reclamation: " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM reclamation WHERE id_reclamation=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur suppression reclamation: " + e.getMessage());
        }
    }

    public void repondre(int id, String reponse, String statut) {
        String sql = "UPDATE reclamation SET reponse_admin=?, statut=?, date_reponse=? WHERE id_reclamation=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, reponse);
            ps.setString(2, statut);
            ps.setDate(3, new Date(System.currentTimeMillis()));
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur reponse reclamation: " + e.getMessage());
        }
    }

    public List<Reclamation> getByStatut(String statut) {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE statut = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId_reclamation(rs.getInt("id_reclamation"));
                r.setId_user(rs.getInt("id_user"));
                r.setType_reclamation(rs.getString("type_reclamation"));
                r.setSujet(rs.getString("sujet"));
                r.setDescription(rs.getString("description"));
                r.setDate_reclamation(rs.getDate("date_reclamation"));
                r.setStatut(rs.getString("statut"));
                r.setPriorite(rs.getString("priorite"));
                r.setReponse_admin(rs.getString("reponse_admin"));
                r.setDate_reponse(rs.getDate("date_reponse"));
                r.setPiece_jointe(rs.getString("piece_jointe"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche par statut: " + e.getMessage());
        }
        return list;
    }

    public List<Reclamation> rechercher(String term) {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE sujet LIKE ? OR description LIKE ? OR type_reclamation LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String t = "%" + term + "%";
            ps.setString(1, t);
            ps.setString(2, t);
            ps.setString(3, t);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId_reclamation(rs.getInt("id_reclamation"));
                r.setId_user(rs.getInt("id_user"));
                r.setType_reclamation(rs.getString("type_reclamation"));
                r.setSujet(rs.getString("sujet"));
                r.setDescription(rs.getString("description"));
                r.setDate_reclamation(rs.getDate("date_reclamation"));
                r.setStatut(rs.getString("statut"));
                r.setPriorite(rs.getString("priorite"));
                r.setReponse_admin(rs.getString("reponse_admin"));
                r.setDate_reponse(rs.getDate("date_reponse"));
                r.setPiece_jointe(rs.getString("piece_jointe"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche reclamation: " + e.getMessage());
        }
        return list;
    }
}
