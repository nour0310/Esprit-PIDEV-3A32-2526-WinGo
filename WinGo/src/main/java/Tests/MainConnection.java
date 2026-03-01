package Tests;

import Entites.Blog;
import Entites.Commentaire;
import Utils.MyBD;

import java.sql.SQLException;

public class MainConnection {

    public static void main(String[] args) throws SQLException {

        MyBD myBD = MyBD.getInstance();

        // CrÃ©ation des objets Blog avec le constructeur complet (titre, contenu, auteur, image, region, categorie)
        Blog b1 = new Blog("Sahara Adventure", "Voyage inoubliable Ã  Douz", 1, "sahara.jpg", "Sud", "Aventure");
        Blog b2 = new Blog("Carthage Story", "DÃ©couverte historique de Carthage", 1, "carthage.jpg", "Nord", "Histoire");

        // CrÃ©ation des objets Commentaire avec le constructeur (contenu, utilisateur, articleId)
        // Les IDs des articles sont 0 car ils ne sont pas encore persistÃ©s, mais pour le test c'est acceptable
        Commentaire c1 = new Commentaire("Super article !", 1, 0);
        Commentaire c2 = new Commentaire("J'adore cet endroit !", 2, 0);

        // Affichage
        System.out.println(b1.getTitre());
        System.out.println(c1.getContenu());
    }
}