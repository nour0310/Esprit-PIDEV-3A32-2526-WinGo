package tn.esprit;
import Entites.Reservation;
import Services.ReservationCRUD;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class ReservationServicetest {





        static ReservationCRUD service;
        static int idReservationTest;

        @BeforeAll
        static void setup() {
            service = new ReservationCRUD();
        }

        // ------------------- AJOUT -------------------
        @Test
        @Order(1)
        void testAjouterReservation() throws SQLException {

            Reservation r = new Reservation(
                    "TestUser",
                    "TestExp",
                    new Timestamp(System.currentTimeMillis()),
                    "EN_ATTENTE"
            );

            service.ajouter(r);

            List<Reservation> reservations = service.afficher();

            assertFalse(reservations.isEmpty());

            assertTrue(
                    reservations.stream()
                            .anyMatch(res -> res.getUser().equals("TestUser"))
            );

            // Get last inserted ID
            idReservationTest = reservations.get(reservations.size() - 1).getId();

            System.out.println("ID ajouté : " + idReservationTest);
        }

        // ------------------- MODIFICATION -------------------
        @Test
        @Order(2)
        void testModifierReservation() throws SQLException {

            Reservation r = new Reservation();
            r.setId(idReservationTest);
            r.setUser("UserModifie");
            r.setExp("ExpModifie");
            r.setStatut("CONFIRMEE");
            r.setDate(new Timestamp(System.currentTimeMillis()));

            service.modifier(r);

            List<Reservation> reservations = service.afficher();

            boolean trouve = reservations.stream()
                    .anyMatch(res ->
                            res.getId() == idReservationTest &&
                                    res.getUser().equals("UserModifie")
                    );

            assertTrue(trouve);
        }

        // ------------------- SUPPRESSION -------------------
        @Test
        @Order(3)
        void testSupprimerReservation() throws SQLException {

            service.supprimer(idReservationTest);

            List<Reservation> reservations = service.afficher();

            boolean existe = reservations.stream()
                    .anyMatch(res -> res.getId() == idReservationTest);

            assertFalse(existe);
        }
    }


