package tn.esprit;
import Entites.Reservation;
import Services.ReservationCRUD;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationServiceTest {

    private static ReservationCRUD reservationCRUD;
    private static int reservationId;

    @BeforeAll
    static void setUp() {
        reservationCRUD = new ReservationCRUD();
    }

    // -------------------- AJOUT --------------------
    @Test
    @Order(1)
    void testAjouterReservation() throws SQLException {

        Reservation reservation = new Reservation(
                "UserTest",
                "ExperienceTest",
                "EN_ATTENTE",
                new Timestamp(System.currentTimeMillis())
        );

        reservationCRUD.ajouter(reservation);

        List<Reservation> reservations = reservationCRUD.afficher();
        assertFalse(reservations.isEmpty());

        reservationId = reservations.stream()
                .filter(r -> "UserTest".equals(r.getUser()))
                .findFirst()
                .orElseThrow(() -> new SQLException("Réservation non trouvée"))
                .getId();

        assertTrue(reservationId > 0);
    }

    // -------------------- MODIFICATION --------------------
    @Test
    @Order(2)
    void testModifierReservation() throws SQLException {

        Reservation reservationModif = new Reservation();
        reservationModif.setId(reservationId);
        reservationModif.setUser("UserModifie");
        reservationModif.setExp("ExperienceModifie");
        reservationModif.setStatut("CONFIRMEE");
        reservationModif.setDate(new Timestamp(System.currentTimeMillis()));

        reservationCRUD.modifier(reservationModif);

        List<Reservation> reservations = reservationCRUD.afficher();

        boolean trouve = reservations.stream()
                .anyMatch(r ->
                        r.getId() == reservationId &&
                                "UserModifie".equals(r.getUser())
                );

        assertTrue(trouve);
    }

    // -------------------- SUPPRESSION --------------------
    @Test
    @Order(3)
    void testSupprimerReservation() throws SQLException {

        reservationCRUD.supprimer(reservationId);

        List<Reservation> reservations = reservationCRUD.afficher();

        boolean existe = reservations.stream()
                .anyMatch(r -> r.getId() == reservationId);

        assertFalse(existe);
    }
}


