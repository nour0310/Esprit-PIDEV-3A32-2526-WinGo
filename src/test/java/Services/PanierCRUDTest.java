package Services;

import Controlles.CartItem;
import Utils.MyBD;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PanierCRUDTest {

    private static PanierCRUD panierCRUD;
    private static int userId;
    private static int produitId;

    @BeforeAll
    static void setUp() throws SQLException {
        panierCRUD = new PanierCRUD();
        userId = getAnyUserId();
        produitId = getAnyProduitId();
        panierCRUD.clear(userId); // clean start
    }

    @AfterAll
    static void tearDown() throws SQLException {
        panierCRUD.clear(userId); // clean end
    }

    private static int getAnyUserId() throws SQLException {
        try (Connection cnx = MyBD.getInstance().getConn();
             PreparedStatement ps = cnx.prepareStatement("SELECT id FROM utilisateur LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Aucun utilisateur trouvé dans la table utilisateur");
    }

    private static int getAnyProduitId() throws SQLException {
        try (Connection cnx = MyBD.getInstance().getConn();
             PreparedStatement ps = cnx.prepareStatement("SELECT id_produit FROM produit LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Aucun produit trouvé dans la table produit");
    }

    private static CartItem findItem(List<CartItem> items, int idProduit) {
        return items.stream()
                .filter(i -> i.getIdProduit() == idProduit)
                .findFirst()
                .orElse(null);
    }

    @Test
    @Order(1)
    void testAddToCartEtGetActiveCart() throws SQLException {
        panierCRUD.clear(userId);

        panierCRUD.addToCart(userId, produitId, 10.0, 2);

        List<CartItem> items = panierCRUD.getActiveCart(userId);
        assertFalse(items.isEmpty(), "Le panier ne doit pas être vide après addToCart");

        CartItem item = findItem(items, produitId);
        assertNotNull(item, "Le produit doit exister dans le panier");
        assertEquals(2, item.getQty(), "La quantité doit être 2");
        assertEquals(10.0, item.getPrixUnitaire(), 0.0001, "Le prix unitaire doit être 10.0");
    }

    @Test
    @Order(2)
    void testChangeQty() throws SQLException {
        // On suppose que le produit existe déjà dans le panier après le test 1
        panierCRUD.changeQty(userId, produitId, +3); // 2 + 3 = 5

        List<CartItem> items = panierCRUD.getActiveCart(userId);
        CartItem item = findItem(items, produitId);
        assertNotNull(item);
        assertEquals(5, item.getQty(), "La quantité doit devenir 5");
    }

    @Test
    @Order(3)
    void testRemove() throws SQLException {
        panierCRUD.remove(userId, produitId);

        List<CartItem> items = panierCRUD.getActiveCart(userId);
        CartItem item = findItem(items, produitId);
        assertNull(item, "Après remove, l'item ne doit plus exister");
    }

    @Test
    @Order(4)
    void testClear() throws SQLException {
        // Ajouter 2 produits (le même produit 2 fois c’est ok: ton code update la quantité)
        panierCRUD.addToCart(userId, produitId, 7.0, 1);
        panierCRUD.addToCart(userId, produitId, 7.0, 1);

        assertFalse(panierCRUD.getActiveCart(userId).isEmpty());

        panierCRUD.clear(userId);

        assertTrue(panierCRUD.getActiveCart(userId).isEmpty(), "Après clear, panier doit être vide");
    }
}