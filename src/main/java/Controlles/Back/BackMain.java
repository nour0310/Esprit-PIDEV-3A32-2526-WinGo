package Controlles.Back;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BackMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackEvent.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("WinGO - Back Office");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}