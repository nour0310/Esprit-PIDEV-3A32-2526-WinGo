package Controlles;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {
    private static Stage stage;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void goTo(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(Navigator.class.getResource("/" + fxml));
            if (stage.getScene() == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.getScene().setRoot(root); // switch page without creating new stage
            }
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot load: " + fxml + " => " + e.getMessage());
        }
    }
}