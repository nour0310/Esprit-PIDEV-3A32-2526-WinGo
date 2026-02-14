package Tests;

import Entites.Utilisateur;
import Entites.Profil;
import Services.UtilisateurCRUD;
import Services.ProfilCRUD;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        UtilisateurCRUD userService = new UtilisateurCRUD();
        ProfilCRUD profilService = new ProfilCRUD();

        try {

            // CREATE USER
            Utilisateur u = new Utilisateur(
                    "Hassairi",
                    "Abderrahmen",
                    "abdo@gmail.com",
                    "123456",
                    "CLIENT",
                    "22123456",
                    22
            );

            userService.ajouter(u);

            // CREATE PROFILE
            Profil p = new Profil(
                    "I love traveling and coding",
                    "profile1.png",
                    1   // user id (change if needed)
            );

            profilService.ajouter(p);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
