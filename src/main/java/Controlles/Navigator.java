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

    public static void goToSupportCenter(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            SupportController controller = loader.getController();
            controller.setUserId(userId);

            Stage stage = new Stage();
            stage.setTitle("Centre de Support");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goToReclamationForm(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            SupportController controller = loader.getController();
            controller.setUserId(userId);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Réclamation");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goToSuggestionForm(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            SupportController controller = loader.getController();
            controller.setUserId(userId);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Suggestion");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}