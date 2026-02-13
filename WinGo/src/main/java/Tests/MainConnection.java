package Tests;

import Entites.Blog;
import Entites.Commentaire;  // ← important !
import Utils.MyBD;

import java.sql.SQLException;
import java.util.Date;

public class MainConnection {

    public static void main(String[] args) throws SQLException {

        MyBD myBD = MyBD.getInstance();

        // Création des objets Blog
        Blog b1 = new Blog("Sahara Adventure", "Voyage inoubliable à Douz", "douz.jpg", "Admin", "Sud", "Voyage");
        Blog b2 = new Blog("Carthage Story", "Découverte historique de Carthage", "carthage.jpg", "Admin", "Nord", "Histoire");

        // Création des objets Commentaire
        Commentaire c1 = new Commentaire("Super article !", new Date(), b1.getId_article(), 1); // 1 = id utilisateur fictif
        Commentaire c2 = new Commentaire("J'adore cet endroit !", new Date(), b2.getId_article(), 2); // 2 = id utilisateur fictif

        // Exemple d'affichage
        System.out.println(b1.getTitre());
        System.out.println(c1.getContenu());
    }
}