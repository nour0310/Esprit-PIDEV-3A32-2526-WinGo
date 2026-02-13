package Tests;

import Entites.Blog;
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
        Commentaire c1 = new Commentaire("Super article !", new Date(), 5, "Alice");
        Commentaire c2 = new Commentaire("Jadore cet endroit !", new Date(), 6, "Bob");



    }
}
