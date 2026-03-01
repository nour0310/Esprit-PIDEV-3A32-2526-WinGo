package Controlles.Front;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FrontMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEvent.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("WinGO - Discover Events");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}