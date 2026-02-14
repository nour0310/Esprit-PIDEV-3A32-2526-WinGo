package Services;

import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PanierCRUD {

    // ✅ retourne le panier actuel (sans id_commande)
    public List<CartItem> getActiveCart(int userId) throws SQLException {
        String sql = """
            SELECT p.id_produit, pr.nom, p.prix, p.qte
            FROM panier p
            JOIN produit pr ON pr.idProduit = p.id_produit
            WHERE p.utilisateur_id = ?
            ORDER BY pr.nom
        """;

        List<CartItem> list = new ArrayList<>();
        Connection conn = MyBD.getInstance().getConn();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idProduit = rs.getInt("id_produit");
                    String nom = rs.getString("nom");
                    double prix = rs.getDouble("prix");
                    int qty = rs.getInt("qte");
                    list.add(new CartItem(idProduit, nom, prix, qty));
                }
            }
        }
        return list;
    }

    // ✅ ajoute au panier : si existe -> +1 sinon insert
    public void addToCart(int userId, int idProduit, double prix, int qtyToAdd) throws SQLException {
        String check = "SELECT qte FROM panier WHERE utilisateur_id=? AND id_produit=?";
        String insert = "INSERT INTO panier(utilisateur_id, id_produit, prix, qte) VALUES (?,?,?,?)";
        String update = "UPDATE panier SET qte = qte + ? WHERE utilisateur_id=? AND id_produit=?";

        Connection conn = MyBD.getInstance().getConn();

        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, userId);
            ps.setInt(2, idProduit);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement up = conn.prepareStatement(update)) {
                        up.setInt(1, qtyToAdd);
                        up.setInt(2, userId);
                        up.setInt(3, idProduit);
                        up.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ins = conn.prepareStatement(insert)) {
                        ins.setInt(1, userId);
                        ins.setInt(2, idProduit);
                        ins.setDouble(3, prix);
                        ins.setInt(4, qtyToAdd);
                        ins.executeUpdate();
                    }
                }
            }
        }
    }

    // ✅ change quantité (+1 / -1) et supprime si <=0
    public void changeQty(int userId, int idProduit, int delta) throws SQLException {
        String sql = "UPDATE panier SET qte = qte + ? WHERE utilisateur_id=? AND id_produit=?";
        Connection conn = MyBD.getInstance().getConn();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, userId);
            ps.setInt(3, idProduit);
            ps.executeUpdate();
        }

        // delete si qte <= 0
        String cleanup = "DELETE FROM panier WHERE utilisateur_id=? AND id_produit=? AND qte <= 0";
        try (PreparedStatement ps = conn.prepareStatement(cleanup)) {
            ps.setInt(1, userId);
            ps.setInt(2, idProduit);
            ps.executeUpdate();
        }
    }

    public void remove(int userId, int idProduit) throws SQLException {
        String sql = "DELETE FROM panier WHERE utilisateur_id=? AND id_produit=?";
        Connection conn = MyBD.getInstance().getConn();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, idProduit);
            ps.executeUpdate();
        }
    }

    public void clear(int userId) throws SQLException {
        String sql = "DELETE FROM panier WHERE utilisateur_id=?";
        Connection conn = MyBD.getInstance().getConn();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    // ✅ checkout : créer commande + vider panier
    // (version simple : tu adaptes à ta table commande / commande_ligne)
    public int checkout(int userId) throws SQLException {
        Connection conn = MyBD.getInstance().getConn();
        conn.setAutoCommit(false);

        try {
            // 1) créer commande
            int idCommande;
            String insertCmd = "INSERT INTO commande(utilisateur_id, date_commande) VALUES (?, NOW())";
            try (PreparedStatement ps = conn.prepareStatement(insertCmd, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Impossible de créer commande.");
                    idCommande = keys.getInt(1);
                }
            }

            // 2) copier panier -> commande_ligne
            String insertLines = """
                INSERT INTO commande_ligne(id_commande, id_produit, prix, qte)
                SELECT ?, id_produit, prix, qte
                FROM panier
                WHERE utilisateur_id=?
            """;
            try (PreparedStatement ps = conn.prepareStatement(insertLines)) {
                ps.setInt(1, idCommande);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            // 3) vider panier
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM panier WHERE utilisateur_id=?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
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