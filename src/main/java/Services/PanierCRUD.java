package Services;

import Controlles.CartItem;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PanierCRUD {
    private final Connection conn;

    public PanierCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    // ✅ Ajouter au panier (si produit déjà dans panier actif -> increment)
    public void addToCart(int idUser, int idProduit, double prixUnitaire, int qty) throws SQLException {
        String check = "SELECT id_panier, quantite FROM panier WHERE id_user=? AND id_produit=? AND id_commande IS NULL";
        try (PreparedStatement pst = conn.prepareStatement(check)) {
            pst.setInt(1, idUser);
            pst.setInt(2, idProduit);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int idPanier = rs.getInt("id_panier");
                    int oldQty = rs.getInt("quantite");
                    String upd = "UPDATE panier SET quantite=? WHERE id_panier=?";
                    try (PreparedStatement up = conn.prepareStatement(upd)) {
                        up.setInt(1, oldQty + qty);
                        up.setInt(2, idPanier);
                        up.executeUpdate();
                    }
                    return;
                }
            }
        }

        String ins = "INSERT INTO panier(id_user, id_produit, quantite, prix_unitaire) VALUES(?,?,?,?)";
        try (PreparedStatement pst = conn.prepareStatement(ins)) {
            pst.setInt(1, idUser);
            pst.setInt(2, idProduit);
            pst.setInt(3, qty);
            pst.setDouble(4, prixUnitaire);
            pst.executeUpdate();
        }
    }

    // ✅ Récupérer panier actif
    public List<CartItem> getActiveCart(int idUser) throws SQLException {
        String req = """
            SELECT p.id_produit, pr.nom, p.prix_unitaire, p.quantite
            FROM panier p
            JOIN produit pr ON pr.id_produit = p.id_produit
            WHERE p.id_user=? AND p.id_commande IS NULL
            ORDER BY p.date_ajout DESC
        """;
        List<CartItem> list = new ArrayList<>();
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, idUser);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(new CartItem(
                            rs.getInt("id_produit"),
                            rs.getString("nom"),
                            rs.getDouble("prix_unitaire"),
                            rs.getInt("quantite")
                    ));
                }
            }
        }
        return list;
    }

    public void changeQty(int idUser, int idProduit, int delta) throws SQLException {
        String req = "UPDATE panier SET quantite = GREATEST(1, quantite + ?) " +
                "WHERE id_user=? AND id_produit=? AND id_commande IS NULL";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, delta);
            pst.setInt(2, idUser);
            pst.setInt(3, idProduit);
            pst.executeUpdate();
        }
    }

    public void remove(int idUser, int idProduit) throws SQLException {
        String req = "DELETE FROM panier WHERE id_user=? AND id_produit=? AND id_commande IS NULL";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, idUser);
            pst.setInt(2, idProduit);
            pst.executeUpdate();
        }
    }

    public void clear(int idUser) throws SQLException {
        String req = "DELETE FROM panier WHERE id_user=? AND id_commande IS NULL";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, idUser);
            pst.executeUpdate();
        }
    }

    // ✅ CONFIRMER COMMANDE: créer commande + lier les lignes panier
    public int checkout(int idUser) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // 1) créer commande
            int idCommande;
            String insC = "INSERT INTO commande(id_user, status) VALUES(?, 'en_cours')";
            try (PreparedStatement pst = conn.prepareStatement(insC, Statement.RETURN_GENERATED_KEYS)) {
                pst.setInt(1, idUser);
                pst.executeUpdate();
                try (ResultSet keys = pst.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Impossible de créer la commande.");
                    idCommande = keys.getInt(1);
                }
            }

            // 2) lier panier actif à cette commande
            String upd = "UPDATE panier SET id_commande=? WHERE id_user=? AND id_commande IS NULL";
            try (PreparedStatement pst = conn.prepareStatement(upd)) {
                pst.setInt(1, idCommande);
                pst.setInt(2, idUser);
                pst.executeUpdate();
            }

            conn.commit();
            return idCommande;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}