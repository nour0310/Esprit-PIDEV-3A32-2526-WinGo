package Services;

import Utils.MyBD;
import Controlles.CartItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PanierCRUD {

    public List<CartItem> getActiveCart(int userId) throws SQLException {

        String sql = """
    SELECT p.id_produit,
           pr.nom,
           pr.image,
           p.prix_unitaire,
           p.quantite
    FROM panier p
    JOIN produit pr ON pr.id_produit = p.id_produit
    WHERE p.id_user = ?
    ORDER BY pr.nom
""";

        List<CartItem> list = new ArrayList<>();

        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idProduit = rs.getInt("id_produit");
                    String nom = rs.getString("nom");
                    double prixUnitaire = rs.getDouble("prix_unitaire");
                    int qty = rs.getInt("quantite");
                    // si ton CartItem calcule total tout seul, tu peux ignorer total
                    list.add(new CartItem(idProduit, nom, prixUnitaire, qty));
                }
            }
        }

        return list;
    }

    public void addToCart(int userId, int idProduit, double prixUnitaire, int qty) throws SQLException {

        String checkSql  = "SELECT quantite FROM panier WHERE id_user=? AND id_produit=?";
        String updateSql = """
            UPDATE panier
            SET quantite = quantite + ?,
                prix_unitaire = ?,
                total = (quantite + ?) * ?
            WHERE id_user=? AND id_produit=?
        """;
        String insertSql = """
            INSERT INTO panier (id_user, id_produit, quantite, prix_unitaire, total)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = MyBD.getInstance().getConn()) {

            boolean exists;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, idProduit);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement up = conn.prepareStatement(updateSql)) {
                    up.setInt(1, qty);           // +qty
                    up.setDouble(2, prixUnitaire);
                    up.setInt(3, qty);           // pour (quantite + qty)
                    up.setDouble(4, prixUnitaire);
                    up.setInt(5, userId);
                    up.setInt(6, idProduit);
                    up.executeUpdate();
                }
            } else {
                double total = prixUnitaire * qty;
                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    ins.setInt(1, userId);
                    ins.setInt(2, idProduit);
                    ins.setInt(3, qty);
                    ins.setDouble(4, prixUnitaire);
                    ins.setDouble(5, total);
                    ins.executeUpdate();
                }
            }
        }
    }

    public void changeQty(int userId, int idProduit, int delta) throws SQLException {

        String getSql = "SELECT quantite, prix_unitaire FROM panier WHERE id_user=? AND id_produit=?";
        String updateSql = "UPDATE panier SET quantite=?, total=? WHERE id_user=? AND id_produit=?";
        String deleteSql = "DELETE FROM panier WHERE id_user=? AND id_produit=?";

        try (Connection conn = MyBD.getInstance().getConn()) {

            int currentQty;
            double prixUnitaire;

            try (PreparedStatement ps = conn.prepareStatement(getSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, idProduit);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return;
                    currentQty = rs.getInt("quantite");
                    prixUnitaire = rs.getDouble("prix_unitaire");
                }
            }

            int newQty = currentQty + delta;

            if (newQty <= 0) {
                try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                    del.setInt(1, userId);
                    del.setInt(2, idProduit);
                    del.executeUpdate();
                }
            } else {
                double newTotal = newQty * prixUnitaire;
                try (PreparedStatement up = conn.prepareStatement(updateSql)) {
                    up.setInt(1, newQty);
                    up.setDouble(2, newTotal);
                    up.setInt(3, userId);
                    up.setInt(4, idProduit);
                    up.executeUpdate();
                }
            }
        }
    }

    public void remove(int userId, int idProduit) throws SQLException {
        String sql = "DELETE FROM panier WHERE id_user=? AND id_produit=?";
        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, idProduit);
            ps.executeUpdate();
        }
    }

    public void clear(int userId) throws SQLException {
        String sql = "DELETE FROM panier WHERE id_user=?";
        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public int checkout(int userId) throws SQLException {
        clear(userId);
        return 0;
    }
}