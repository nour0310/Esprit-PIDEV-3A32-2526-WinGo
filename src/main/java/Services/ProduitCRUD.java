package Services;

import Entites.Produit;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitCRUD implements IntrefaceCRUD<Produit> {

    Connection conn;

    public ProduitCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Produit produit) throws SQLException {
        String req = "INSERT INTO produit (id_commercant, nom, description, prix, region, categorie, stock, image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, produit.getIdCommercant());
        pst.setString(2, produit.getNom());
        pst.setString(3, produit.getDescription());
        pst.setDouble(4, produit.getPrix());
        pst.setString(5, produit.getRegion());
        pst.setString(6, produit.getCategorie());
        pst.setInt(7, produit.getStock());
        pst.setString(8, produit.getImage());

        pst.executeUpdate();
        System.out.println("✅ Produit ajouté !");
    }

    @Override
    public void modifier(Produit produit) throws SQLException {
        String req = "UPDATE produit SET id_commercant=?, nom=?, description=?, prix=?, region=?, categorie=?, stock=?, image=? " +
                "WHERE id_produit=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, produit.getIdCommercant());
        pst.setString(2, produit.getNom());
        pst.setString(3, produit.getDescription());
        pst.setDouble(4, produit.getPrix());
        pst.setString(5, produit.getRegion());
        pst.setString(6, produit.getCategorie());
        pst.setInt(7, produit.getStock());
        pst.setString(8, produit.getImage());
        pst.setInt(9, produit.getIdProduit());

        pst.executeUpdate();
        System.out.println("✅ Produit modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM produit WHERE id_produit=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("✅ Produit supprimé !");
    }

    @Override
    public List<Produit> afficher() throws SQLException {
        String req = "SELECT * FROM produit";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        List<Produit> listeProduits = new ArrayList<>();

        while (rs.next()) {
            Produit p = new Produit();
            p.setIdProduit(rs.getInt("id_produit"));
            p.setIdCommercant(rs.getInt("id_commercant"));
            p.setNom(rs.getString("nom"));
            p.setDescription(rs.getString("description"));
            p.setPrix(rs.getDouble("prix"));
            p.setRegion(rs.getString("region"));
            p.setCategorie(rs.getString("categorie"));
            p.setStock(rs.getInt("stock"));
            p.setImage(rs.getString("image"));
            // si tu as Timestamp dans Produit :
            // p.setDateAjout(rs.getTimestamp("date_ajout"));

            listeProduits.add(p);
        }

        return listeProduits;
    }
}