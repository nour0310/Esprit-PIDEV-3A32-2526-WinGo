package Controlles;

import Controlles.Back.BackEventController;
import Controlles.Front.FrontEventController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Navigator — central navigation helper.
 *
 * Front-office views use FrontEvent.fxml  → FrontEventController
 * Back-office views  use BackEvent.fxml   → BackEventController
 */
public class Navigator {

    // ── Generic launcher ─────────────────────────────────────────────

    public static void goTo(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/" + fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }











    // ── Front-office shortcuts ────────────────────────────────────────

    /** Opens the client-facing event browser. */
    public static void goToFront() {
        goTo("FrontEvent.fxml", "WinGO — Discover Events");
    }

    /**
     * Opens the front-office and pre-selects the My Trips tab
     * filtered by the given event ID.
     */
    public static void goToParticipation(int idEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/FrontEvent.fxml"));
            Parent root = loader.load();

            FrontEventController controller = loader.getController();
            controller.setEventId(idEvent);

            Stage stage = new Stage();
            stage.setTitle("My Trips — Event #" + idEvent);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the front-office My Trips tab filtered by client email.
     */
    public static void goToMesParticipations(String clientEmail) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/FrontEvent.fxml"));
            Parent root = loader.load();

            FrontEventController controller = loader.getController();
            controller.setClientEmail(clientEmail);

            Stage stage = new Stage();
            stage.setTitle("My Trips");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Back-office shortcuts ─────────────────────────────────────────

    /** Opens the admin back-office dashboard. */
    public static void goToBack() {
        goTo("BackEvent.fxml", "WinGO — Back Office");
    }
}