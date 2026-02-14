package Controlles;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Home extends Application {

    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        try {
            primaryStage = stage;

            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Scene scene = new Scene(root);

            stage.setTitle("🚀 My JavaFX Project");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔥 Global navigation method (very useful)
    public static void loadPage(String fxml) {
        try {
            Parent root = FXMLLoader.load(Home.class.getResource("/" + fxml));
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Optional: access stage if needed
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
