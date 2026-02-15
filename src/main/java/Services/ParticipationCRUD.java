package Services;

import Entites.Participation;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationCRUD {
    private Connection cnx;

    public ParticipationCRUD() {
        cnx = MyBD.getInstance().getConn();
    }

    // CREATE
    public void ajouter(Participation p) {
        String req = "INSERT INTO participation (id_event, id_user, date_participation, statut, " +
                "nom_participant, prenom_participant, email_participant, telephone, nombre_places) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, p.getId_event());
            ps.setInt(2, p.getId_user());
            ps.setDate(3, p.getDate_participation());
            ps.setString(4, p.getStatut());
            ps.setString(5, p.getNom_participant());
            ps.setString(6, p.getPrenom_participant());
            ps.setString(7, p.getEmail_participant());
            ps.setString(8, p.getTelephone());
            ps.setInt(9, p.getNombre_places());

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Participation added successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding participation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // READ ALL
    public List<Participation> afficherTous() {
        List<Participation> list = new ArrayList<>();
        String req = "SELECT p.*, e.title as eventTitle FROM participation p " +
                "LEFT JOIN event e ON p.id_event = e.id_event";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Participation p = new Participation();
                p.setId_participation(rs.getInt("id_participation"));
                p.setId_event(rs.getInt("id_event"));
                p.setId_user(rs.getInt("id_user"));
                p.setDate_participation(rs.getDate("date_participation"));
                p.setStatut(rs.getString("statut"));
                p.setNom_participant(rs.getString("nom_participant"));
                p.setPrenom_participant(rs.getString("prenom_participant"));
                p.setEmail_participant(rs.getString("email_participant"));
                p.setTelephone(rs.getString("telephone"));
                p.setNombre_places(rs.getInt("nombre_places"));
                p.setEventTitle(rs.getString("eventTitle"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching participations: " + e.getMessage());
        }
        return list;
    }

    // READ BY EVENT
    public List<Participation> afficherParEvent(int eventId) {
        List<Participation> list = new ArrayList<>();
        String req = "SELECT p.*, e.title as eventTitle FROM participation p " +
                "LEFT JOIN event e ON p.id_event = e.id_event WHERE p.id_event = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Participation p = new Participation();
                p.setId_participation(rs.getInt("id_participation"));
                p.setId_event(rs.getInt("id_event"));
                p.setId_user(rs.getInt("id_user"));
                p.setDate_participation(rs.getDate("date_participation"));
                p.setStatut(rs.getString("statut"));
                p.setNom_participant(rs.getString("nom_participant"));
                p.setPrenom_participant(rs.getString("prenom_participant"));
                p.setEmail_participant(rs.getString("email_participant"));
                p.setTelephone(rs.getString("telephone"));
                p.setNombre_places(rs.getInt("nombre_places"));
                p.setEventTitle(rs.getString("eventTitle"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching participations by event: " + e.getMessage());
        }
        return list;
    }

    // READ BY CLIENT EMAIL
    public List<Participation> afficherParClient(String email) {
        List<Participation> list = new ArrayList<>();
        String req = "SELECT p.*, e.title as eventTitle FROM participation p " +
                "LEFT JOIN event e ON p.id_event = e.id_event WHERE p.email_participant LIKE ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, "%" + email + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Participation p = new Participation();
                p.setId_participation(rs.getInt("id_participation"));
                p.setId_event(rs.getInt("id_event"));
                p.setId_user(rs.getInt("id_user"));
                p.setDate_participation(rs.getDate("date_participation"));
                p.setStatut(rs.getString("statut"));
                p.setNom_participant(rs.getString("nom_participant"));
                p.setPrenom_participant(rs.getString("prenom_participant"));
                p.setEmail_participant(rs.getString("email_participant"));
                p.setTelephone(rs.getString("telephone"));
                p.setNombre_places(rs.getInt("nombre_places"));
                p.setEventTitle(rs.getString("eventTitle"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching participations by email: " + e.getMessage());
        }
        return list;
    }

    // READ BY CLIENT NAME
    public List<Participation> afficherParNomClient(String nom, String prenom) {
        List<Participation> list = new ArrayList<>();
        String req = "SELECT p.*, e.title as eventTitle FROM participation p " +
                "LEFT JOIN event e ON p.id_event = e.id_event " +
                "WHERE p.nom_participant LIKE ? AND p.prenom_participant LIKE ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, "%" + nom + "%");
            ps.setString(2, "%" + prenom + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Participation p = new Participation();
                p.setId_participation(rs.getInt("id_participation"));
                p.setId_event(rs.getInt("id_event"));
                p.setId_user(rs.getInt("id_user"));
                p.setDate_participation(rs.getDate("date_participation"));
                p.setStatut(rs.getString("statut"));
                p.setNom_participant(rs.getString("nom_participant"));
                p.setPrenom_participant(rs.getString("prenom_participant"));
                p.setEmail_participant(rs.getString("email_participant"));
                p.setTelephone(rs.getString("telephone"));
                p.setNombre_places(rs.getInt("nombre_places"));
                p.setEventTitle(rs.getString("eventTitle"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching participations by name: " + e.getMessage());
        }
        return list;
    }

    // SEARCH
    public List<Participation> rechercherParticipations(String term) {
        List<Participation> list = new ArrayList<>();
        String req = "SELECT p.*, e.title as eventTitle FROM participation p " +
                "LEFT JOIN event e ON p.id_event = e.id_event " +
                "WHERE p.nom_participant LIKE ? OR p.prenom_participant LIKE ? " +
                "OR p.email_participant LIKE ? OR e.title LIKE ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            String searchTerm = "%" + term + "%";
            ps.setString(1, searchTerm);
            ps.setString(2, searchTerm);
            ps.setString(3, searchTerm);
            ps.setString(4, searchTerm);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Participation p = new Participation();
                p.setId_participation(rs.getInt("id_participation"));
                p.setId_event(rs.getInt("id_event"));
                p.setId_user(rs.getInt("id_user"));
                p.setDate_participation(rs.getDate("date_participation"));
                p.setStatut(rs.getString("statut"));
                p.setNom_participant(rs.getString("nom_participant"));
                p.setPrenom_participant(rs.getString("prenom_participant"));
                p.setEmail_participant(rs.getString("email_participant"));
                p.setTelephone(rs.getString("telephone"));
                p.setNombre_places(rs.getInt("nombre_places"));
                p.setEventTitle(rs.getString("eventTitle"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error searching participations: " + e.getMessage());
        }
        return list;
    }

    // UPDATE
    public void modifier(Participation p) {
        String req = "UPDATE participation SET id_event=?, id_user=?, date_participation=?, " +
                "statut=?, nom_participant=?, prenom_participant=?, email_participant=?, " +
                "telephone=?, nombre_places=? WHERE id_participation=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, p.getId_event());
            ps.setInt(2, p.getId_user());
            ps.setDate(3, p.getDate_participation());
            ps.setString(4, p.getStatut());
            ps.setString(5, p.getNom_participant());
            ps.setString(6, p.getPrenom_participant());
            ps.setString(7, p.getEmail_participant());
            ps.setString(8, p.getTelephone());
            ps.setInt(9, p.getNombre_places());
            ps.setInt(10, p.getId_participation());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Participation updated successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating participation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // DELETE
    public void supprimer(int id) {
        String req = "DELETE FROM participation WHERE id_participation=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);

            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Participation deleted successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting participation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}