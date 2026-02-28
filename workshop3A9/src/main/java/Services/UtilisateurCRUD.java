package Services;

import Entites.Utilisateur;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurCRUD {

    Connection cnx;

    public UtilisateurCRUD() {
        cnx = MyBD.getInstance().getCnx();
    }

    private void checkConnection() throws SQLException {
        if (cnx == null) {
            throw new SQLException(
                    "Database connection failed. Is MySQL running on localhost:3306? Database 'wingo' exists?");
        }
    }

    // ADD
    public void ajouter(Utilisateur u) throws SQLException {
        checkConnection();
        String req = "INSERT INTO utilisateur(nom, prenom, email, mot_de_passe, type, telephone, age, is_verified, verification_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getMotDePasse());
        ps.setString(5, u.getType());
        ps.setString(6, u.getTelephone());
        ps.setInt(7, u.getAge());
        ps.setBoolean(8, u.isVerified());
        ps.setString(9, u.getVerificationCode());

        ps.executeUpdate();
        System.out.println("Utilisateur ajouté !");
    }

    // SHOW ALL
    public List<Utilisateur> afficher() throws SQLException {
        checkConnection();
        List<Utilisateur> list = new ArrayList<>();
        String req = "SELECT * FROM utilisateur";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Utilisateur u = new Utilisateur();
            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setMotDePasse(rs.getString("mot_de_passe"));
            u.setType(rs.getString("type"));
            u.setTelephone(rs.getString("telephone"));
            u.setAge(rs.getInt("age"));
            u.setVerified(rs.getBoolean("is_verified"));
            u.setVerificationCode(rs.getString("verification_code"));

            list.add(u);
        }

        return list;
    }

    // UPDATE
    public void modifier(Utilisateur u) throws SQLException {
        checkConnection();
        String req = "UPDATE utilisateur SET nom=?, prenom=?, email=?, mot_de_passe=?, type=?, telephone=?, age=?, is_verified=?, verification_code=? WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getMotDePasse());
        ps.setString(5, u.getType());
        ps.setString(6, u.getTelephone());
        ps.setInt(7, u.getAge());
        ps.setBoolean(8, u.isVerified());
        ps.setString(9, u.getVerificationCode());
        ps.setInt(10, u.getId());

        ps.executeUpdate();
        System.out.println("Utilisateur modifié !");
    }

    // DELETE
    public void supprimer(int id) throws SQLException {
        checkConnection();
        String req = "DELETE FROM utilisateur WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("Utilisateur supprimé !");
    }
}
