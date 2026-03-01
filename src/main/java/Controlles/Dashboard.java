package Controlles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Dashboard {

    @FXML
    private void goAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackEvent.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("WinGO — Back Office");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToBackOffice() {
        goAjouter();
    }
}