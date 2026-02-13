package Services;

import Entites.Utilisateur;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurCRUD {

    Connection conn;

    public UtilisateurCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    // ADD
    public void ajouter(Utilisateur u) throws SQLException {
        String req = "INSERT INTO utilisateur(nom, prenom, email, mot_de_passe, type, telephone, age) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getMotDePasse());
        ps.setString(5, u.getType());
        ps.setString(6, u.getTelephone());
        ps.setInt(7, u.getAge());

        ps.executeUpdate();
        System.out.println("Utilisateur ajouté !");
    }

    // SHOW ALL
    public List<Utilisateur> afficher() throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        String req = "SELECT * FROM utilisateur";
        Statement st = conn.createStatement();
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

            list.add(u);
        }

        return list;
    }

    // UPDATE
    public void modifier(Utilisateur u) throws SQLException {
        String req = "UPDATE utilisateur SET nom=?, prenom=?, email=?, mot_de_passe=?, type=?, telephone=?, age=? WHERE id=?";

        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getMotDePasse());
        ps.setString(5, u.getType());
        ps.setString(6, u.getTelephone());
        ps.setInt(7, u.getAge());
        ps.setInt(8, u.getId());

        ps.executeUpdate();
        System.out.println("Utilisateur modifié !");
    }

    // DELETE
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM utilisateur WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("Utilisateur supprimé !");
    }
}
