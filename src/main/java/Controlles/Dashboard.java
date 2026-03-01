package Controlles;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class Dashboard {

    // L'ID vient maintenant de la Session — plus de valeur hardcodée à 1
    private int currentUserId = Session.getInstance().getUserId();

    // ==================== SUPPORT CENTER NAVIGATION ====================

    @FXML
    private void goToSupportCenter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            // Pass the current user ID to the controller
            SupportController controller = loader.getController();
            controller.setUserId(currentUserId);

            Stage stage = new Stage();
            stage.setTitle("Centre de Support WinGo");
            stage.setScene(new Scene(root));
            stage.show();

            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du centre de support");
        }
    }

    @FXML
    private void goToReclamationForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            SupportController controller = loader.getController();
            controller.setUserId(currentUserId);
            // You could add a method to switch to the reclamation tab
            // controller.switchToReclamationTab();

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Réclamation");
            stage.setScene(new Scene(root));
            stage.show();

            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire de réclamation");
        }
    }

    @FXML
    private void goToSuggestionForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            SupportController controller = loader.getController();
            controller.setUserId(currentUserId);
            // You could add a method to switch to the suggestion tab
            // controller.switchToSuggestionTab();

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Suggestion");
            stage.setScene(new Scene(root));
            stage.show();

            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire de suggestion");
        }
    }

    @FXML
    private void goToListReclamations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Liste des Réclamations");
            stage.setScene(new Scene(root));
            stage.show();

            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de la liste des réclamations");
        }
    }

    @FXML
    private void goToListSuggestions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Liste des Suggestions");
            stage.setScene(new Scene(root));
            stage.show();

            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de la liste des suggestions");
        }
    }

    @FXML
    private void goToMesReclamations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            SupportController controller = loader.getController();
            controller.setUserId(currentUserId);

            Stage stage = new Stage();
            stage.setTitle("Mes Réclamations");
            stage.setScene(new Scene(root));
            stage.show();

            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de vos réclamations");
        }
    }

    @FXML
    private void goToMesSuggestions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            SupportController controller = loader.getController();
            controller.setUserId(currentUserId);

            Stage stage = new Stage();
            stage.setTitle("Mes Suggestions");
            stage.setScene(new Scene(root));
            stage.show();

            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de vos suggestions");
        }
    }

    // ==================== STATISTICS DASHBOARD ====================

    @FXML
    private void goToStatsReclamations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Statistiques des Réclamations");
            stage.setScene(new Scene(root));
            stage.show();

            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture des statistiques");
        }
    }

    @FXML
    private void goToStatsSuggestions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Statistiques des Suggestions");
            stage.setScene(new Scene(root));
            stage.show();

            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture des statistiques");
        }
    }

    // ==================== ADMIN FUNCTIONS ====================
    @FXML
    private void goToAdminReclamations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReclamationBackOffice.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Back Office - Réclamations");
            stage.setScene(new Scene(root, 1300, 850));
            stage.show();
            closeCurrentWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de l'interface admin");
        }
    }


    @FXML
    private void goToAdminSuggestions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SupportCenter.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Administration - Suggestions");
            stage.setScene(new Scene(root));
            stage.show();

            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de l'interface admin");
        }
    }

    // ==================== HELPER METHODS ====================

    private void closeCurrentWindow() {
        try {
            Stage stage = (Stage) javafx.scene.Node.class.getMethod("getScene").invoke(this).getClass()
                    .getMethod("getWindow").invoke(javafx.scene.Node.class.getMethod("getScene").invoke(this));
            if (stage != null) {
                stage.close();
            }
        } catch (Exception e) {
            // Window might not be closable from here, that's ok
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to set the current user (called from login)
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }
}