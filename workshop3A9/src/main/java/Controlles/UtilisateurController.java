package Controlles;

import Entites.Utilisateur;
import Services.UtilisateurCRUD;
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
import java.util.regex.Pattern;

public class UtilisateurController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s'-]{2,50}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,15}$");

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField typeField;
    @FXML private TextField telephoneField;
    @FXML private TextField ageField;

    @FXML private TableView<Utilisateur> tableUser;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colType;
    @FXML private TableColumn<Utilisateur, String> colTelephone;
    @FXML private TableColumn<Utilisateur, Integer> colAge;

    private UtilisateurCRUD service = new UtilisateurCRUD();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));
        colType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("type"));
        colTelephone.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("telephone"));
        colAge.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("age"));

        // Control de saisie
        UnaryOperator<javafx.scene.control.TextFormatter.Change> digitsOnly = c ->
            c.getText().matches("[0-9]*") ? c : null;
        UnaryOperator<javafx.scene.control.TextFormatter.Change> lettersOnly = c ->
            c.getText().matches("[a-zA-ZÀ-ÿ\\s'-]*") ? c : null;
        ageField.setTextFormatter(new javafx.scene.control.TextFormatter<>(digitsOnly));
        telephoneField.setTextFormatter(new javafx.scene.control.TextFormatter<>(digitsOnly));
        nomField.setTextFormatter(new javafx.scene.control.TextFormatter<>(lettersOnly));
        prenomField.setTextFormatter(new javafx.scene.control.TextFormatter<>(lettersOnly));

        loadUsers();
    }

    @FXML
    public void ajouterUtilisateur() {
        String err = validateUserFields();
        if (err != null) {
            showAlert(Alert.AlertType.WARNING, err);
            return;
        }
        try {
            Utilisateur u = new Utilisateur();
            u.setNom(nomField.getText().trim());
            u.setPrenom(prenomField.getText().trim());
            u.setEmail(emailField.getText().trim());
            u.setMotDePasse(passwordField.getText());
            u.setType(typeField.getText().trim());
            u.setTelephone(telephoneField.getText().trim());
            u.setAge(Integer.parseInt(ageField.getText()));

            service.ajouter(u);
            clearFields();
            loadUsers();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error adding user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void modifierUtilisateur() {
        Utilisateur selected = tableUser.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a user to modify");
            return;
        }
        String err = validateUserFields();
        if (err != null) {
            showAlert(Alert.AlertType.WARNING, err);
            return;
        }
        try {
            selected.setNom(nomField.getText().trim());
            selected.setPrenom(prenomField.getText().trim());
            selected.setEmail(emailField.getText().trim());
            selected.setMotDePasse(passwordField.getText());
            selected.setType(typeField.getText().trim());
            selected.setTelephone(telephoneField.getText().trim());
            selected.setAge(Integer.parseInt(ageField.getText()));

            service.modifier(selected);
            loadUsers();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimerUtilisateur() {
        Utilisateur selected = tableUser.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a user to delete");
            return;
        }
        try {
            service.supprimer(selected.getId());
            loadUsers();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String validateUserFields() {
        if (nomField.getText().trim().isEmpty() || prenomField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() || passwordField.getText().isEmpty() ||
            typeField.getText().trim().isEmpty() || telephoneField.getText().trim().isEmpty() ||
            ageField.getText().trim().isEmpty()) {
            return "Please fill all fields";
        }
        if (!NAME_PATTERN.matcher(nomField.getText().trim()).matches()) {
            return "Nom: letters only, 2-50 characters";
        }
        if (!NAME_PATTERN.matcher(prenomField.getText().trim()).matches()) {
            return "Prenom: letters only, 2-50 characters";
        }
        if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            return "Invalid email format";
        }
        if (passwordField.getText().length() < 6) {
            return "Password must be at least 6 characters";
        }
        if (!PHONE_PATTERN.matcher(telephoneField.getText().trim()).matches()) {
            return "Telephone: 8-15 digits only";
        }
        try {
            int age = Integer.parseInt(ageField.getText());
            if (age < 1 || age > 120) return "Age must be between 1 and 120";
        } catch (NumberFormatException e) {
            return "Age must be a valid number";
        }
        return null;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadUsers() {
        try {
            List<Utilisateur> users = service.afficher();
            tableUser.setItems(FXCollections.observableArrayList(users));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        typeField.clear();
        telephoneField.clear();
        ageField.clear();
    }

    @FXML
    public void backToDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AdminDashboard.fxml"));
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
