package Tests;

import Entites.Blog;
import Entites.Commentaire;
import Utils.MyBD;

import java.sql.SQLException;

public class MainConnection {

    public static void main(String[] args) throws SQLException {

        MyBD myBD = MyBD.getInstance();

        // Création des objets Blog (auteur = ID utilisateur existant, par exemple 1)
        Blog b1 = new Blog("Sahara Adventure", "Voyage inoubliable à Douz", "douz.jpg", "Sud", "Voyage", 1);
        Blog b2 = new Blog("Carthage Story", "Découverte historique de Carthage", "carthage.jpg", "Nord", "Histoire", 1);

        // Création des objets Commentaire (sans date, elle sera générée automatiquement par la base)
        Commentaire c1 = new Commentaire("Super article !", 1, b1.getId());
        Commentaire c2 = new Commentaire("J'adore cet endroit !", 2, b2.getId());

        // Affichage
        System.out.println("Titre du blog : " + b1.getTitre());
        System.out.println("Commentaire : " + c1.getContenu());
    }
}