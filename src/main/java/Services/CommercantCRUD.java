package Services;

import Entites.Commercant;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommercantCRUD {

    private final Connection conn;

    public CommercantCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    public List<Commercant> afficher() throws SQLException {
        String req = "SELECT id_commercant, nom_boutique FROM commercant";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        List<Commercant> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Commercant(rs.getInt("id_commercant"), rs.getString("nom_boutique")));
        }
        return list;
    }
}