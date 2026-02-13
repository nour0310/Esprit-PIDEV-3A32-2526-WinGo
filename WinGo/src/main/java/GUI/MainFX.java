package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/Blog.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("WinGo by HexaVibe - Plateforme Touristique Intelligente");
        stage.setScene(scene);
        stage.setMinWidth(1400);
        stage.setMinHeight(900);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}