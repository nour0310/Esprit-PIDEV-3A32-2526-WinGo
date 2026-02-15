package Services;

import Entites.Panier;
import Tests.MainConnection;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PanierCRUDTest {

    private static PanierCRUD panierCRUD;

    @BeforeAll
    static void setUp() {
        panierCRUD = new PanierCRUD();
    }

    private int getAnyUserId() throws SQLException {
        try (Connection cnx = MainConnection.getInstance().getCnx();
             PreparedStatement ps = cnx.prepareStatement("SELECT id FROM utilisateur LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Aucun utilisateur trouvé dans la table utilisateur");
    }

    private int getAnyProduitId() throws SQLException {
        try (Connection cnx = MainConnection.getInstance().getCnx();
             PreparedStatement ps = cnx.prepareStatement("SELECT id_produit FROM produit LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Aucun produit trouvé dans la table produit");
    }

    @Test
    @Order(1)
    void testAjouterPanier() throws SQLException {
        int idUser = getAnyUserId();
        int idProduit = getAnyProduitId();

        int quantite = 2;
        double prixUnitaire = 9.50;
        double total = quantite * prixUnitaire;

        Panier p = new Panier(idUser, idProduit, quantite, prixUnitaire, total);

        panierCRUD.ajouter(p);

        List<Panier> items = panierCRUD.afficher();
        assertFalse(items.isEmpty());

        boolean trouve = items.stream().anyMatch(x ->
                x.getIdUser() == idUser &&
                        x.getIdProduit() == idProduit &&
                        x.getQuantite() == quantite
        );
        assertTrue(trouve, "L'item ajouté doit apparaître dans la liste");
    }

    @Test
    @Order(2)
    void testModifierPanier() throws SQLException {
        int idUser = getAnyUserId();
        int idProduit = getAnyProduitId();

        // 1) ajouter un item à modifier
        Panier p = new Panier(idUser, idProduit, 1, 5.0, 5.0);
        panierCRUD.ajouter(p);

        // 2) récupérer l'id_panier (dernier item correspondant)
        List<Panier> items = panierCRUD.afficher();
        int idPanier = items.stream()
                .filter(x -> x.getIdUser() == idUser && x.getIdProduit() == idProduit)
                .reduce((a, b) -> b)
                .orElseThrow(() -> new SQLException("Item panier non trouvé"))
                .getIdPanier();

        // 3) modifier
        int newQte = 4;
        double newPrix = 12.5;
        double newTotal = newQte * newPrix;

        Panier modif = new Panier(idPanier, idUser, idProduit, newQte, newPrix, newTotal);
        panierCRUD.modifier(modif);

        // 4) vérifier
        items = panierCRUD.afficher();
        boolean ok = items.stream().anyMatch(x ->
                x.getIdPanier() == idPanier &&
                        x.getQuantite() == newQte &&
                        x.getPrixUnitaire() != null &&
                        x.getPrixUnitaire() == newPrix
        );

        assertTrue(ok, "L'item modifié doit avoir la nouvelle quantité et le nouveau prix");
    }
}