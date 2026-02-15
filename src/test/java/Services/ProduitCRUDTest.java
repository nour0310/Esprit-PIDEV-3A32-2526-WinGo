package Services;

import Entites.Produit;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProduitCRUDTest {

    private static ProduitCRUD produitCRUD;

    @BeforeAll
    static void setUp() {
        produitCRUD = new ProduitCRUD();
    }

    @Test
    @Order(1)
    void testAjouterProduit() throws SQLException {

        Produit p = new Produit(
                1,                      // idUser (ex: commerçant connecté)
                "Produit Test",         // nom
                "Description test",     // description
                19.9,                   // prix
                "Tunis",                // region
                "Tech",                 // categorie
                10,                     // stock
                "test.png"              // image
        );

        produitCRUD.ajouter(p);

        List<Produit> produits = produitCRUD.afficher();
        assertFalse(produits.isEmpty(), "La liste des produits ne doit pas être vide après ajout");

        boolean trouve = produits.stream()
                .anyMatch(x -> "Produit Test".equals(x.getNom()));
        assertTrue(trouve, "Le produit ajouté doit exister dans la liste");
    }

    @Test
    @Order(2)
    void testModifierProduit() throws SQLException {

        // 1) créer un produit à modifier
        Produit p = new Produit(
                1,
                "Ancien Nom",
                "Ancienne desc",
                10.0,
                "Sfax",
                "Maison",
                5,
                "old.png"
        );
        produitCRUD.ajouter(p);

        // 2) récupérer l'idProduit
        List<Produit> produits = produitCRUD.afficher();
        int idProduit = produits.stream()
                .filter(x -> "Ancien Nom".equals(x.getNom()))
                .findFirst()
                .orElseThrow(() -> new SQLException("Produit non trouvé"))
                .getIdProduit();

        // 3) modifier (UPDATE constructor)
        Produit pModif = new Produit(
                idProduit,
                1,
                "Nouveau Nom",
                "Nouvelle desc",
                25.5,
                "Tunis",
                "Sport",
                20,
                "new.png"
        );

        produitCRUD.modifier(pModif);

        // 4) vérifier
        produits = produitCRUD.afficher();
        boolean ok = produits.stream()
                .anyMatch(x -> x.getIdProduit() == idProduit
                        && "Nouveau Nom".equals(x.getNom())
                        && x.getPrix() == 25.5
                        && x.getStock() == 20);

        assertTrue(ok, "Le produit modifié doit avoir les nouvelles valeurs");
    }
}