package Controlles;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Home extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/HomeTravel.fxml")
        );

        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Home");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}