package Controlles;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Home extends Application {

    // Les infos utilisateur viennent maintenant de Session (plus de valeur hardcodée)
    private int    currentUserId    = Session.getInstance().getUserId();
    private String currentUserEmail = Session.getInstance().getEmail();
    private String currentUserName  = Session.getInstance().getNom();

    @FXML private Label welcomeLabel;


    @Override
    public void start(Stage stage) throws Exception {
        // Ouvrir le Login en premier
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 480, 580));
        stage.setTitle("WinGo - Connexion");
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + currentUserName + " !");
        }
    }

    @FXML
    private void goToSupportCenter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation.fxml"));
            Parent root = loader.load();

            SupportController controller = loader.getController();
            controller.setUserInfo(currentUserId, currentUserEmail, currentUserName);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1400, 850));
            stage.setTitle("WinGo - Centre de Support");
            stage.show();

            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.close();

            // Rouvrir la page de login (simulé)
            start(new Stage());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeCurrentWindow() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            // Ignorer
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setCurrentUser(int userId, String email, String name) {
        this.currentUserId = userId;
        this.currentUserEmail = email;
        this.currentUserName = name;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + name + " !");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}