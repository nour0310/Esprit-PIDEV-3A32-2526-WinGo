package Controlles;

import Entites.Profil;
import Services.ProfilCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.function.UnaryOperator;

public class ProfilController {

    @FXML private TextField bioField;
    @FXML private TextField imageField;
    @FXML private TextField utilisateurIdField;

    @FXML private TableView<Profil> tableProfil;
    @FXML private TableColumn<Profil, Integer> colId;
    @FXML private TableColumn<Profil, String> colBio;
    @FXML private TableColumn<Profil, String> colImage;
    @FXML private TableColumn<Profil, Integer> colUtilisateurId;

    private ProfilCRUD service = new ProfilCRUD();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colBio.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("bio"));
        colImage.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("image"));
        colUtilisateurId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("utilisateurId"));

        // Control de saisie: User ID digits only
        UnaryOperator<TextFormatter.Change> digitsOnly = c ->
            c.getText().matches("[0-9]*") ? c : null;
        utilisateurIdField.setTextFormatter(new TextFormatter<>(digitsOnly));

        loadProfils();
    }

    @FXML
    public void ajouterProfil() {
        String err = validateProfilFields();
        if (err != null) {
            showAlert(Alert.AlertType.WARNING, err);
            return;
        }
        try {
            Profil p = new Profil();
            p.setBio(bioField.getText().trim());
            p.setImage(imageField.getText().trim());
            p.setUtilisateurId(Integer.parseInt(utilisateurIdField.getText().trim()));

            service.ajouter(p);
            clearFields();
            loadProfils();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error adding profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void modifierProfil() {
        Profil selected = tableProfil.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a profile to modify");
            return;
        }
        if (bioField.getText().trim().isEmpty() || imageField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please fill Bio and Image fields");
            return;
        }
        try {
            selected.setBio(bioField.getText().trim());
            selected.setImage(imageField.getText().trim());
            service.modifier(selected);
            loadProfils();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error updating profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimerProfil() {
        Profil selected = tableProfil.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a profile to delete");
            return;
        }
        try {
            service.supprimer(selected.getId());
            loadProfils();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error deleting profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String validateProfilFields() {
        if (bioField.getText().trim().isEmpty() || imageField.getText().trim().isEmpty() ||
            utilisateurIdField.getText().trim().isEmpty()) {
            return "Please fill all fields (Bio, Image, User ID)";
        }
        try {
            int id = Integer.parseInt(utilisateurIdField.getText().trim());
            if (id < 1) return "User ID must be a positive number";
        } catch (NumberFormatException e) {
            return "User ID must be a valid number";
        }
        return null;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadProfils() {
        try {
            List<Profil> profils = service.afficher();
            tableProfil.setItems(FXCollections.observableArrayList(profils));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        bioField.clear();
        imageField.clear();
        utilisateurIdField.clear();
    }

    @FXML
    public void backToDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AdminDashboard.fxml"));
            Stage stage = (Stage) bioField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
