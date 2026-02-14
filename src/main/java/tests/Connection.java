package tests;

import Entites.Reservation;
import Services.ReservationCRUD;
import java.sql.SQLException;
import java.util.List;

public class Connection {
    public static void main(String[] args) {

        ReservationCRUD service = new ReservationCRUD();

        try {

            List<Reservation> reservations = service.afficher();

              them
            for (Reservation r : reservations) {
                System.out.println(
                        r.getId() + " | " + r.getUser() + " | " + r.getExp() + " | " +
                                r.getStatut() + " | " + r.getDate()
                );
            }

            // Example: update a reservation
            if (!reservations.isEmpty()) {
                Reservation first = reservations.get(0);
                service.modifier(first);
            }

            // Example: delete a reservation
            if (!reservations.isEmpty()) {
                Reservation last = reservations.get(reservations.size() - 1);
                service.supprimer(last.getId());
            }

        } catch (SQLException e) {
            System.out.println("❌ Database operation failed!");
            e.printStackTrace();
        }
    }
}
