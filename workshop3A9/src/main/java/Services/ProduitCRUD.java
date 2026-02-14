package Services;

import Entites.Produit;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitCRUD {

    Connection cnx;

    public ProduitCRUD() {
        cnx = MyBD.getInstance().getCnx();
    }

    private void checkConnection() throws SQLException {
        if (cnx == null) {
            throw new SQLException("Database connection failed. Is MySQL running on localhost:3306? Database 'wingo' exists?");
        }
    }

    public void ajouter(Produit p) throws SQLException {
        checkConnection();
        String req = "INSERT INTO produit(nom, prix, stock, categorie, region, description, image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, p.getNom());
        ps.setDouble(2, p.getPrix());
        ps.setInt(3, p.getStock());
        ps.setString(4, p.getCategorie());
        ps.setString(5, p.getRegion());
        ps.setString(6, p.getDescription());
        ps.setString(7, p.getImage());
        ps.executeUpdate();
    }

    public List<Produit> afficher() throws SQLException {
        checkConnection();
        List<Produit> list = new ArrayList<>();
        String req = "SELECT * FROM produit";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Produit p = new Produit();
            p.setId(rs.getInt("id"));
            p.setNom(rs.getString("nom"));
            p.setPrix(rs.getDouble("prix"));
            p.setStock(rs.getInt("stock"));
            p.setCategorie(rs.getString("categorie"));
            p.setRegion(rs.getString("region"));
            p.setDescription(rs.getString("description"));
            p.setImage(rs.getString("image"));
            list.add(p);
        }
        return list;
    }

    public void modifier(Produit p) throws SQLException {
        checkConnection();
        String req = "UPDATE produit SET nom=?, prix=?, stock=?, categorie=?, region=?, description=?, image=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, p.getNom());
        ps.setDouble(2, p.getPrix());
        ps.setInt(3, p.getStock());
        ps.setString(4, p.getCategorie());
        ps.setString(5, p.getRegion());
        ps.setString(6, p.getDescription());
        ps.setString(7, p.getImage());
        ps.setInt(8, p.getId());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        checkConnection();
        String req = "DELETE FROM produit WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}
