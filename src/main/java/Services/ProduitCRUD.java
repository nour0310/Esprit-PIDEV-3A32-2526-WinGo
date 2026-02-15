package Services;

import Entites.Produit;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitCRUD implements IntrefaceCRUD<Produit> {

    @Override
    public void ajouter(Produit produit) throws SQLException {
        String req = "INSERT INTO produit (id_user, nom, description, prix, region, categorie, stock, image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = MyBD.getInstance().getConn();
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, produit.getIdUser());
            pst.setString(2, produit.getNom());
            pst.setString(3, produit.getDescription());
            pst.setDouble(4, produit.getPrix());
            pst.setString(5, produit.getRegion());
            pst.setString(6, produit.getCategorie());
            pst.setInt(7, produit.getStock());
            pst.setString(8, produit.getImage());

            pst.executeUpdate();
        }

        System.out.println("✅ Produit ajouté !");
    }

    @Override
    public void modifier(Produit produit) throws SQLException {
        String req = "UPDATE produit SET id_user=?, nom=?, description=?, prix=?, region=?, categorie=?, stock=?, image=? " +
                "WHERE id_produit=?";

        Connection conn = MyBD.getInstance().getConn();
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, produit.getIdUser());
            pst.setString(2, produit.getNom());
            pst.setString(3, produit.getDescription());
            pst.setDouble(4, produit.getPrix());
            pst.setString(5, produit.getRegion());
            pst.setString(6, produit.getCategorie());
            pst.setInt(7, produit.getStock());
            pst.setString(8, produit.getImage());
            pst.setInt(9, produit.getIdProduit());

            pst.executeUpdate();
        }
        System.out.println("✅ Produit modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM produit WHERE id_produit=?";

        Connection conn = MyBD.getInstance().getConn();
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
        System.out.println("✅ Produit supprimé !");
    }

    @Override
    public List<Produit> afficher() throws SQLException {
        String req = "SELECT * FROM produit";
        List<Produit> liste = new ArrayList<>();

        Connection conn = MyBD.getInstance().getConn();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Produit p = new Produit();
                p.setIdProduit(rs.getInt("id_produit"));
                p.setIdUser(rs.getInt("id_user"));
                p.setNom(rs.getString("nom"));
                p.setDescription(rs.getString("description"));
                p.setPrix(rs.getDouble("prix"));
                p.setRegion(rs.getString("region"));
                p.setCategorie(rs.getString("categorie"));
                p.setStock(rs.getInt("stock"));
                p.setImage(rs.getString("image"));
                liste.add(p);
            }
        }
        return liste;
    }

    public List<Produit> afficherParUser(int idUser) throws SQLException {
        String req = "SELECT * FROM produit WHERE id_user=? ORDER BY date_ajout DESC";
        List<Produit> list = new ArrayList<>();

        Connection conn = MyBD.getInstance().getConn();
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, idUser);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Produit p = new Produit();
                    p.setIdProduit(rs.getInt("id_produit"));
                    p.setIdUser(rs.getInt("id_user"));
                    p.setNom(rs.getString("nom"));
                    p.setDescription(rs.getString("description"));
                    p.setPrix(rs.getDouble("prix"));
                    p.setRegion(rs.getString("region"));
                    p.setCategorie(rs.getString("categorie"));
                    p.setStock(rs.getInt("stock"));
                    p.setImage(rs.getString("image"));
                    list.add(p);
                }
            }
        }
        return list;
    }
}