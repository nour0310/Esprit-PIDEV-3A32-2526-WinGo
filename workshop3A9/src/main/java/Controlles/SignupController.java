package Controlles;

import Entites.Utilisateur;
import Services.UtilisateurCRUD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;

public class SignupController implements Initializable {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField ageField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private UtilisateurCRUD service = new UtilisateurCRUD();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s'-]{2,50}$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Control de saisie: restrict input by field type
        UnaryOperator<TextFormatter.Change> digitsOnly = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*"))
                return change;
            return null;
        };
        UnaryOperator<TextFormatter.Change> lettersOnly = change -> {
            String text = change.getText();
            if (text.matches("[a-zA-ZÀ-ÿ\\s'-]*"))
                return change;
            return null;
        };
        ageField.setTextFormatter(new TextFormatter<>(digitsOnly));
        telephoneField.setTextFormatter(new TextFormatter<>(digitsOnly));
        nomField.setTextFormatter(new TextFormatter<>(lettersOnly));
        prenomField.setTextFormatter(new TextFormatter<>(lettersOnly));
    }

    @FXML
    public void register() {
        // Trim all inputs
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String ageStr = ageField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Check empty fields
        if (nom.isEmpty() || prenom.isEmpty() || telephone.isEmpty() || ageStr.isEmpty() ||
                email.isEmpty() || password.isEmpty()) {
            showError("⚠ Please fill all fields");
            return;
        }

        // Validate nom (letters only, 2-50 chars)
        if (!NAME_PATTERN.matcher(nom).matches()) {
            showError("⚠ Nom: letters only, 2-50 characters");
            return;
        }

        // Validate prenom
        if (!NAME_PATTERN.matcher(prenom).matches()) {
            showError("⚠ Prenom: letters only, 2-50 characters");
            return;
        }

        // Validate telephone (8-15 digits)
        if (!PHONE_PATTERN.matcher(telephone).matches()) {
            showError("⚠ Telephone: 8-15 digits only");
            return;
        }

        // Validate age
        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 1 || age > 120) {
                showError("⚠ Age must be between 1 and 120");
                return;
            }
        } catch (NumberFormatException e) {
            showError("⚠ Age must be a valid number");
            return;
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("⚠ Invalid email format");
            return;
        }

        // Validate password (min 6 chars)
        if (password.length() < 6) {
            showError("⚠ Password must be at least 6 characters");
            return;
        }

        try {
            // Check if email already exists
            for (Utilisateur u : service.afficher()) {
                if (u.getEmail().equalsIgnoreCase(email)) {
                    showError("⚠ Email already registered");
                    return;
                }
            }

            // Generate verification code
            String verificationCode = String.format("%06d", new java.util.Random().nextInt(1000000));

            Utilisateur user = new Utilisateur(nom, prenom, email, password, "user", telephone, age);
            user.setVerified(false);
            user.setVerificationCode(verificationCode);

            service.ajouter(user);

            // Send verification email
            new Services.EmailService().envoyerCodeVerification(email, verificationCode);

            messageLabel.setStyle("-fx-text-fill: lightgreen;");
            messageLabel.setText("✅ Account created! Please verify your email.");

            // Navigate to verification page
            goToVerification(email, verificationCode);

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Data truncated")) {
                showError("❌ Database 'type' column too small. Run: ALTER TABLE utilisateur MODIFY type VARCHAR(50);");
            } else {
                showError("❌ Error creating account: " + e.getMessage());
            }
            e.printStackTrace();
        } catch (Exception e) {
            showError("❌ Error creating account");
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(msg);
    }

    @FXML
    public void backToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToVerification(String email, String code) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/VerifyEmail.fxml"));
            Parent root = loader.load();

            VerifyEmailController controller = loader.getController();
            controller.setUserEmail(email, code);

            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("❌ Error navigating to verification: " + e.getMessage());
        }
    }

}
