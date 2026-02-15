package Controlles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML
    private Button manageUsersButton;

    @FXML
    public void openUsers() {
        loadPage("/User.fxml");
    }

    @FXML
    public void openProfiles() {
        loadPage("/Profil.fxml");
    }

    @FXML
    public void backToShop() {
        loadPage("/WinGoShop.fxml");
    }

    @FXML
    public void logout() {
        loadPage("/Login.fxml");
    }

    private void loadPage(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) manageUsersButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
