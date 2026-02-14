package Services;

import Entites.Transport;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransportCRUD implements InterfaceCRUD<Transport> {

    Connection conn;

    public TransportCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Transport transport) throws SQLException {

        String req = "INSERT INTO transport (type, capacite, tarif, depart, arrivee, dateDepart) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req);

        pst.setString(1, transport.getType());
        pst.setString(2, transport.getCapacite());
        pst.setFloat(3, transport.getTarif());
        pst.setString(4, transport.getDepart());
        pst.setString(5, transport.getArrivee());

        // 🔥 correct LocalDateTime → DATETIME conversion
        pst.setTimestamp(6, Timestamp.valueOf(transport.getDateDepart()));

        pst.executeUpdate();
        System.out.println("Transport ajouté !");
    }

    @Override
    public void modifier(Transport transport) throws SQLException {

        String req = "UPDATE transport SET type=?, capacite=?, tarif=?, depart=?, arrivee=?, dateDepart=? WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);

        pst.setString(1, transport.getType());
        pst.setString(2, transport.getCapacite());
        pst.setFloat(3, transport.getTarif());
        pst.setString(4, transport.getDepart());
        pst.setString(5, transport.getArrivee());
        pst.setTimestamp(6, Timestamp.valueOf(transport.getDateDepart()));
        pst.setInt(7, transport.getId());

        pst.executeUpdate();
        System.out.println("Transport modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {

        String req = "DELETE FROM transport WHERE id = ?";
        PreparedStatement pst = conn.prepareStatement(req);

        pst.setInt(1, id);
        pst.executeUpdate();

        System.out.println("Transport supprimé !");
    }

    @Override
    public List<Transport> afficher() throws SQLException {

        String req = "SELECT * FROM transport";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        List<Transport> listeTransport = new ArrayList<>();

        while (rs.next()) {

            Transport t = new Transport();

            t.setId(rs.getInt("id"));
            t.setType(rs.getString("type"));
            t.setCapacite(rs.getString("capacite"));
            t.setTarif(rs.getFloat("tarif"));
            t.setDepart(rs.getString("depart"));
            t.setArrivee(rs.getString("arrivee"));

            // 🔥 DATETIME → LocalDateTime conversion
            Timestamp ts = rs.getTimestamp("dateDepart");
            if (ts != null) {
                t.setDateDepart(ts.toLocalDateTime());
            }

            listeTransport.add(t);
        }

        return listeTransport;
    }
    public List<Transport> getAll() throws SQLException {
        List<Transport> transports = new ArrayList<>();
        String query = "SELECT * FROM transport"; // adjust table name if needed
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Transport t = new Transport();
            t.setId(rs.getInt("id"));
            t.setType(rs.getString("type"));
            t.setCapacite(rs.getString("capacite"));
            t.setTarif(rs.getFloat("tarif"));
            t.setDepart(rs.getString("depart"));
            t.setArrivee(rs.getString("arrivee"));
            t.setDateDepart(rs.getTimestamp("dateDepart").toLocalDateTime()); // assuming dateDepart is TIMESTAMP
            transports.add(t);
        }

        rs.close();
        st.close();

        return transports;
    }

}
