package Controlles;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {

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

    public static void goToParticipation(int idEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/ParticipationForm.fxml"));
            Parent root = loader.load();

            // Use EventController instead of ParticipationFormController
            EventController controller = loader.getController();
            controller.setEventId(idEvent);

            Stage stage = new Stage();
            stage.setTitle("Participation - Event #" + idEvent);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goToMesParticipations(String clientEmail) {
        // Ignorer l'email pour l'instant
        goTo("MesParticipations.fxml", "Mes Participations");
    }
}