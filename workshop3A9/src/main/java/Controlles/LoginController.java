package Controlles;

import Entites.Utilisateur;
import Services.UtilisateurCRUD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class LoginController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private UtilisateurCRUD service = new UtilisateurCRUD();

    @FXML
    public void goToShopWithoutAccount() {
        loadPage("/WinGoShop.fxml");
    }

    @FXML
    public void goToSignup() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Signup.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void login() {

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("⚠ Please fill all fields");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errorLabel.setText("⚠ Invalid email format");
            return;
        }

        if (password.length() < 6) {
            errorLabel.setText("⚠ Password must be at least 6 characters");
            return;
        }

        try {
            for (Utilisateur u : service.afficher()) {

                if (u.getEmail().equals(email)
                        && u.getMotDePasse().equals(password)) {

                    // ✅ Successful login
                    errorLabel.setText("");

                    // admin → AdminDashboard; user → WinGo Shop
                    if ("admin".equalsIgnoreCase(u.getType())) {
                        loadPage("/AdminDashboard.fxml");
                    } else {
                        loadPage("/WinGoShop.fxml");
                    }
                    return;
                }
            }

            // ❌ If no match found
            errorLabel.setText("❌ Email or password incorrect");

        } catch (Exception e) {
            errorLabel.setText("⚠ Error connecting to database");
            e.printStackTrace();
        }
    }

    private void loadPage(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            errorLabel.setText("⚠ Error loading page");
            e.printStackTrace();
        }
    }
}
