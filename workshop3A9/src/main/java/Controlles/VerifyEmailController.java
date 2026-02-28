package Controlles;

import Entites.Utilisateur;
import Services.EmailService;
import Services.UtilisateurCRUD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Random;

public class VerifyEmailController {

    @FXML
    private Label emailLabel;
    @FXML
    private TextField codeField;
    @FXML
    private Label messageLabel;

    private String userEmail;
    private String correctCode;

    private UtilisateurCRUD service = new UtilisateurCRUD();
    private EmailService emailService = new EmailService();

    public void setUserEmail(String email, String code) {
        this.userEmail = email;
        this.correctCode = code;
        this.emailLabel.setText(email);
    }

    @FXML
    public void handleVerify() {
        String enteredCode = codeField.getText().trim();

        if (enteredCode.isEmpty()) {
            showError("Veuillez saisir le code.");
            return;
        }

        if (enteredCode.equals(correctCode)) {
            try {
                // Update user verification status in DB
                for (Utilisateur u : service.afficher()) {
                    if (u.getEmail().equalsIgnoreCase(userEmail)) {
                        u.setVerified(true);
                        u.setVerificationCode(null);
                        service.modifier(u);
                        break;
                    }
                }

                messageLabel.setStyle("-fx-text-fill: #10B981;");
                messageLabel.setText("Compte vérifié avec succès !");

                // Navigate to Login after a short delay or immediately
                goToLogin();
            } catch (SQLException e) {
                showError("Erreur lors de la mise à jour : " + e.getMessage());
            }
        } else {
            showError("Code incorrect. Veuillez réessayer.");
        }
    }

    @FXML
    public void handleResend() {
        // Generate new code
        String newCode = String.format("%06d", new Random().nextInt(1000000));
        this.correctCode = newCode;

        try {
            // Update code in DB
            for (Utilisateur u : service.afficher()) {
                if (u.getEmail().equalsIgnoreCase(userEmail)) {
                    u.setVerificationCode(newCode);
                    service.modifier(u);
                    break;
                }
            }

            // Send new email
            emailService.envoyerCodeVerification(userEmail, newCode);
            messageLabel.setStyle("-fx-text-fill: #A3B1FF;");
            messageLabel.setText("Nouveau code envoyé !");
        } catch (SQLException e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #EF4444;");
        messageLabel.setText(msg);
    }

    @FXML
    public void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) codeField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
