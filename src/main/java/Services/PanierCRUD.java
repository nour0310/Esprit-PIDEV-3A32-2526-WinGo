package Services;

import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PanierCRUD {

    // ---------------------------
    // 1) Lire le panier actif (avant commande)
    // ---------------------------
    public List<CartItem> getActiveCart(int userId) throws SQLException {
        String sql = """
            SELECT p.id_produit,
                   pr.nom,
                   p.prix_unitaire,
                   p.quantite
            FROM panier p
            JOIN produit pr ON pr.idProduit = p.id_produit
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
                    double prix = rs.getDouble("prix_unitaire");
                    int qty = rs.getInt("quantite");

                    list.add(new CartItem(idProduit, nom, prix, qty));
                }
            }
        }

        return list;
    }

    // ---------------------------
    // 2) Ajouter au panier (si existe -> increment)
    // ---------------------------
    public void addToCart(int userId, int idProduit, double prixUnitaire, int qty) throws SQLException {
        String checkSql = "SELECT quantite FROM panier WHERE id_user=? AND id_produit=?";
        String updateSql = "UPDATE panier SET quantite = quantite + ?, prix_unitaire=? WHERE id_user=? AND id_produit=?";
        String insertSql = "INSERT INTO panier (id_user, id_produit, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";

        try (Connection conn = MyBD.getInstance().getConn()) {

            // check exist
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, idProduit);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // exists -> update qty
                        try (PreparedStatement up = conn.prepareStatement(updateSql)) {
                            up.setInt(1, qty);
                            up.setDouble(2, prixUnitaire);
                            up.setInt(3, userId);
                            up.setInt(4, idProduit);
                            up.executeUpdate();
                        }
                    } else {
                        // not exists -> insert
                        try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                            ins.setInt(1, userId);
                            ins.setInt(2, idProduit);
                            ins.setInt(3, qty);
                            ins.setDouble(4, prixUnitaire);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    // ---------------------------
    // 3) Changer quantité (+1 / -1). Si <=0 => remove
    // ---------------------------
    public void changeQty(int userId, int idProduit, int delta) throws SQLException {
        String sqlGet = "SELECT quantite FROM panier WHERE id_user=? AND id_produit=?";
        String sqlUpdate = "UPDATE panier SET quantite=? WHERE id_user=? AND id_produit=?";
        String sqlDelete = "DELETE FROM panier WHERE id_user=? AND id_produit=?";

        try (Connection conn = MyBD.getInstance().getConn()) {
            int current = 0;

            try (PreparedStatement ps = conn.prepareStatement(sqlGet)) {
                ps.setInt(1, userId);
                ps.setInt(2, idProduit);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) current = rs.getInt("quantite");
                    else return;
                }
            }

            int newQty = current + delta;

            if (newQty <= 0) {
                try (PreparedStatement del = conn.prepareStatement(sqlDelete)) {
                    del.setInt(1, userId);
                    del.setInt(2, idProduit);
                    del.executeUpdate();
                }
            } else {
                try (PreparedStatement up = conn.prepareStatement(sqlUpdate)) {
                    up.setInt(1, newQty);
                    up.setInt(2, userId);
                    up.setInt(3, idProduit);
                    up.executeUpdate();
                }
            }
        }
    }

    // ---------------------------
    // 4) Retirer un produit du panier
    // ---------------------------
    public void remove(int userId, int idProduit) throws SQLException {
        String sql = "DELETE FROM panier WHERE id_user=? AND id_produit=?";

        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, idProduit);
            ps.executeUpdate();
        }
    }

    // ---------------------------
    // 5) Vider panier
    // ---------------------------
    public void clear(int userId) throws SQLException {
        String sql = "DELETE FROM panier WHERE id_user=?";

        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    // ---------------------------
    // 6) Checkout (optionnel)
    // ---------------------------
    // Tu n’as pas id_commande dans panier => checkout doit créer une commande
    // et déplacer les lignes du panier vers une table "ligne_commande".
    // Pour l’instant on peut juste vider le panier.
    public int checkout(int userId) throws SQLException {
        clear(userId);
        return 0; // si tu ajoutes table commande après, on retourne l'id de la commande.
    }
}