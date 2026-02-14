package Services;

import Entites.Reservation;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationCRUD implements InterfaceCRUD<Reservation> {

    Connection conn;

    public ReservationCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Reservation reservation) throws SQLException {
        String req = "INSERT INTO reservation (user, exp, statut, date) VALUES (?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, reservation.getUser());
        pst.setString(2, reservation.getExp());
        pst.setString(3, reservation.getStatut());
        pst.setTimestamp(4, reservation.getDate());
        pst.executeUpdate();
        System.out.println("Reservation ajoutée !");
    }

    @Override
    public void modifier(Reservation reservation) throws SQLException {
        String req = "UPDATE reservation SET user = ?, exp = ?, statut = ?, date = ? WHERE id = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, reservation.getUser());
        pst.setString(2, reservation.getExp());
        pst.setString(3, reservation.getStatut());
        pst.setTimestamp(4, reservation.getDate());
        pst.setInt(5, reservation.getId());
        pst.executeUpdate();
        System.out.println("Reservation modifiée !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM reservation WHERE id = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Reservation supprimée !");
    }

    @Override
    public List<Reservation> afficher() throws SQLException {
        String req = "SELECT * FROM reservation";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<Reservation> listereservation = new ArrayList<>();

        while (rs.next()) {
            Reservation r = new Reservation();
            r.setId(rs.getInt("id"));
            r.setUser(rs.getString("user"));
            r.setExp(rs.getString("exp"));
            r.setStatut(rs.getString("statut"));
            r.setDate(rs.getTimestamp("date"));
            listereservation.add(r);
        }
        return listereservation;
    }
    public List<Reservation> getAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation"; // adjust table name if needed
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Reservation r = new Reservation();
            r.setId(rs.getInt("id"));
            r.setUser(rs.getString("user"));
            r.setExp(rs.getString("exp"));
            r.setStatut(rs.getString("statut"));
            r.setDate(rs.getTimestamp("date"));
            reservations.add(r);
        }
        return reservations;
    }



}
