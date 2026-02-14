package Services;

import Entites.Profil;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfilCRUD {

    Connection cnx;

    public ProfilCRUD() {
        cnx = MyBD.getInstance().getCnx();
    }

    private void checkConnection() throws SQLException {
        if (cnx == null) {
            throw new SQLException("Database connection failed. Is MySQL running on localhost:3306? Database 'wingo' exists?");
        }
    }

    // ADD
    public void ajouter(Profil p) throws SQLException {
        checkConnection();
        String req = "INSERT INTO profil(bio, image, utilisateur_id) VALUES (?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, p.getBio());
        ps.setString(2, p.getImage());
        ps.setInt(3, p.getUtilisateurId());

        ps.executeUpdate();
        System.out.println("Profil ajouté !");
    }

    // SHOW ALL
    public List<Profil> afficher() throws SQLException {
        checkConnection();
        List<Profil> list = new ArrayList<>();
        String req = "SELECT * FROM profil";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Profil p = new Profil();
            p.setId(rs.getInt("id"));
            p.setBio(rs.getString("bio"));
            p.setImage(rs.getString("image"));
            p.setUtilisateurId(rs.getInt("utilisateur_id"));

            list.add(p);
        }

        return list;
    }

    // UPDATE
    public void modifier(Profil p) throws SQLException {
        checkConnection();
        String req = "UPDATE profil SET bio=?, image=? WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, p.getBio());
        ps.setString(2, p.getImage());
        ps.setInt(3, p.getId());

        ps.executeUpdate();
        System.out.println("Profil modifié !");
    }

    // DELETE
    public void supprimer(int id) throws SQLException {
        checkConnection();
        String req = "DELETE FROM profil WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("Profil supprimé !");
    }
}
