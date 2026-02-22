package Controlles;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Home extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // CSS ICI
            var css = getClass().getResource("/wingo-styles.css");
            System.out.println("CSS = " + css); // debug
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            primaryStage.setScene(scene);
            primaryStage.show();

            Navigator.setStage(primaryStage);
            Navigator.goTo("Home.fxml", "Ajouter Produit");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}