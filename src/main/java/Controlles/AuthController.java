package Controlles;

import Services.UserCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AuthController {

    @FXML private RadioButton loginMode;
    @FXML private RadioButton signupMode;

    @FXML private HBox nameRow;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;

    @FXML private Label statusLabel;
    @FXML private Button actionBtn;

    private UserCRUD userCRUD;

    @FXML
    public void initialize() {
        userCRUD = new UserCRUD();
        setLoginMode();
    }

    @FXML
    private void switchMode() {
        if (signupMode.isSelected()) setSignupMode();
        else setLoginMode();
    }

    private void setLoginMode() {
        nameRow.setVisible(false);
        nameRow.setManaged(false);

        confirmField.setVisible(false);
        confirmField.setManaged(false);

        actionBtn.setText("Login");
        statusLabel.setText("");
    }

    private void setSignupMode() {
        nameRow.setVisible(true);
        nameRow.setManaged(true);

        confirmField.setVisible(true);
        confirmField.setManaged(true);

        actionBtn.setText("Sign up");
        statusLabel.setText("");
    }

    @FXML
    private void handleAction() {
        if (signupMode.isSelected()) doSignup();
        else doLogin();
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String pass = passwordField.getText();

        if (email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("⚠ Email et mot de passe obligatoires.");
            return;
        }

        try {
            boolean ok = userCRUD.login(email, pass);
            if (!ok) {
                statusLabel.setText("❌ Email ou mot de passe incorrect.");
                return;
            }

            statusLabel.setText("");
            Navigator.goTo("AjouterProduit.fxml", "Wingo - Backoffice");

        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void doSignup() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("⚠ Tous les champs sont obligatoires.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setText("⚠ Email invalide.");
            return;
        }

        if (pass.length() < 6) {
            statusLabel.setText("⚠ Mot de passe min 6 caractères.");
            return;
        }

        if (!pass.equals(confirm)) {
            statusLabel.setText("⚠ Confirmation mot de passe incorrecte.");
            return;
        }

        try {
            if (userCRUD.emailExists(email)) {
                statusLabel.setText("⚠ Email déjà utilisé.");
                return;
            }

            userCRUD.signup(nom, prenom, email, pass);
            statusLabel.setText("✅ Compte créé ! Tu peux login.");

            // Switch automatique vers login
            loginMode.setSelected(true);
            setLoginMode();

        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}