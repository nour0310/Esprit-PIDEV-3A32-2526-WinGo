package Services;

import Entites.Reclamation;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationCRUD {
    private Connection cnx;

    public ReclamationCRUD() {
        cnx = MyBD.getInstance().getConn();
    }

    // CREATE
    public void ajouter(Reclamation reclamation) {
        String req = "INSERT INTO reclamation (id_user, type_reclamation, sujet, description, " +
                "priorite, piece_jointe) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, reclamation.getId_user());
            ps.setString(2, reclamation.getType_reclamation());
            ps.setString(3, reclamation.getSujet());
            ps.setString(4, reclamation.getDescription());
            ps.setString(5, reclamation.getPriorite());
            ps.setString(6, reclamation.getPiece_jointe());

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    reclamation.setId_reclamation(generatedKeys.getInt(1));
                }
                System.out.println("✅ Reclamation added successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding reclamation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // READ ALL
    public List<Reclamation> afficherTous() {
        List<Reclamation> list = new ArrayList<>();
        String req = "SELECT * FROM reclamation ORDER BY date_reclamation DESC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

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
                r.setPiece_jointe(rs.getString("piece_jointe"));
                r.setReponse_admin(rs.getString("reponse_admin"));
                r.setDate_reponse(rs.getDate("date_reponse"));

                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching reclamations: " + e.getMessage());
        }
        return list;
    }

    // READ BY ID
    public Reclamation getById(int id) {
        String req = "SELECT * FROM reclamation WHERE id_reclamation = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId_reclamation(rs.getInt("id_reclamation"));
                r.setId_user(rs.getInt("id_user"));
                r.setType_reclamation(rs.getString("type_reclamation"));
                r.setSujet(rs.getString("sujet"));
                r.setDescription(rs.getString("description"));
                r.setDate_reclamation(rs.getDate("date_reclamation"));
                r.setStatut(rs.getString("statut"));
                r.setPriorite(rs.getString("priorite"));
                r.setPiece_jointe(rs.getString("piece_jointe"));
                r.setReponse_admin(rs.getString("reponse_admin"));
                r.setDate_reponse(rs.getDate("date_reponse"));
                return r;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching reclamation: " + e.getMessage());
        }
        return null;
    }

    // READ BY USER
    public List<Reclamation> getByUser(int userId) {
        List<Reclamation> list = new ArrayList<>();
        String req = "SELECT * FROM reclamation WHERE id_user = ? ORDER BY date_reclamation DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
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
                r.setPiece_jointe(rs.getString("piece_jointe"));
                r.setReponse_admin(rs.getString("reponse_admin"));
                r.setDate_reponse(rs.getDate("date_reponse"));

                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching user reclamations: " + e.getMessage());
        }
        return list;
    }

    // READ BY STATUS
    public List<Reclamation> getByStatut(String statut) {
        List<Reclamation> list = new ArrayList<>();
        String req = "SELECT * FROM reclamation WHERE statut = ? ORDER BY date_reclamation DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
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
                r.setPiece_jointe(rs.getString("piece_jointe"));
                r.setReponse_admin(rs.getString("reponse_admin"));
                r.setDate_reponse(rs.getDate("date_reponse"));

                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching reclamations by status: " + e.getMessage());
        }
        return list;
    }

    // UPDATE
    public void modifier(Reclamation reclamation) {
        String req = "UPDATE reclamation SET type_reclamation=?, sujet=?, description=?, " +
                "priorite=?, piece_jointe=? WHERE id_reclamation=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, reclamation.getType_reclamation());
            ps.setString(2, reclamation.getSujet());
            ps.setString(3, reclamation.getDescription());
            ps.setString(4, reclamation.getPriorite());
            ps.setString(5, reclamation.getPiece_jointe());
            ps.setInt(6, reclamation.getId_reclamation());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Reclamation updated successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating reclamation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // UPDATE STATUS AND RESPONSE
    public void repondre(int id, String reponse, String nouveauStatut) {
        String req = "UPDATE reclamation SET reponse_admin=?, date_reponse=NOW(), statut=? WHERE id_reclamation=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, reponse);
            ps.setString(2, nouveauStatut);
            ps.setInt(3, id);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Response added to reclamation!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error responding to reclamation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // DELETE
    public void supprimer(int id) {
        String req = "DELETE FROM reclamation WHERE id_reclamation=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);

            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Reclamation deleted successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting reclamation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // SEARCH
    public List<Reclamation> rechercher(String terme) {
        List<Reclamation> list = new ArrayList<>();
        String req = "SELECT * FROM reclamation WHERE sujet LIKE ? OR description LIKE ? OR type_reclamation LIKE ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            String searchTerm = "%" + terme + "%";
            ps.setString(1, searchTerm);
            ps.setString(2, searchTerm);
            ps.setString(3, searchTerm);

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
                r.setPiece_jointe(rs.getString("piece_jointe"));
                r.setReponse_admin(rs.getString("reponse_admin"));
                r.setDate_reponse(rs.getDate("date_reponse"));

                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error searching reclamations: " + e.getMessage());
        }
        return list;
    }
}