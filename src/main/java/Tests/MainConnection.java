package Tests;

import Entites.Produit;
import Services.ProduitCRUD;
import Utils.MyBD;

import java.sql.SQLException;

public class MainConnection {

    public static void main(String[] args) {

        // Test connexion
        MyBD myBD = MyBD.getInstance();

        // Produits test (idCommercant doit exister dans ta table commercant)
        Produit pr1 = new Produit();
        pr1.setIdCommercant(1);
        pr1.setNom("Mug");
        pr1.setDescription("Mug souvenir");
        pr1.setPrix(15.0);
        pr1.setRegion("Sousse");
        pr1.setCategorie("Souvenir");
        pr1.setStock(10);
        pr1.setImage("mug.jpg");

        Produit pr2 = new Produit();
        pr2.setIdCommercant(1);
        pr2.setNom("Bracelet");
        pr2.setDescription("Bracelet artisanal");
        pr2.setPrix(30.0);
        pr2.setRegion("Djerba");
        pr2.setCategorie("Artisanat");
        pr2.setStock(5);
        pr2.setImage("bracelet.jpg");

        ProduitCRUD ps = new ProduitCRUD();

        try {
            // Décommente si tu veux tester l'insertion
            // ps.ajouter(pr1);
            // ps.ajouter(pr2);

            System.out.println(ps.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}