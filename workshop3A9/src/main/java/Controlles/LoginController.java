package Controlles;

import Entites.Utilisateur;
import Services.UtilisateurCRUD;
import Utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LoginController implements Initializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private javafx.scene.canvas.Canvas captchaCanvas;

    @FXML
    private TextField captchaField;

    private String currentCaptcha;

    private UtilisateurCRUD service = new UtilisateurCRUD();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshCaptcha();
    }

    @FXML
    public void refreshCaptcha() {
        currentCaptcha = generateCaptcha(6);
        drawCaptcha(currentCaptcha);
        captchaField.clear();
    }

    private void drawCaptcha(String code) {
        javafx.scene.canvas.GraphicsContext gc = captchaCanvas.getGraphicsContext2D();
        double w = captchaCanvas.getWidth();
        double h = captchaCanvas.getHeight();

        // Background noise - light grid/dots
        gc.setFill(javafx.scene.paint.Color.rgb(241, 245, 249));
        gc.fillRect(0, 0, w, h);

        Random rnd = new Random();

        // Random lines for noise
        gc.setLineWidth(1);
        for (int i = 0; i < 8; i++) {
            gc.setStroke(javafx.scene.paint.Color.rgb(rnd.nextInt(200), rnd.nextInt(200), rnd.nextInt(200), 0.3));
            gc.strokeLine(rnd.nextDouble() * w, rnd.nextDouble() * h, rnd.nextDouble() * w, rnd.nextDouble() * h);
        }

        // Random circles/dots
        for (int i = 0; i < 20; i++) {
            gc.setFill(javafx.scene.paint.Color.rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255), 0.2));
            double size = rnd.nextDouble() * 10;
            gc.fillOval(rnd.nextDouble() * w, rnd.nextDouble() * h, size, size);
        }

        // Draw characters with rotation and different colors
        gc.setFont(javafx.scene.text.Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 22));
        double charWidth = w / (code.length() + 1);

        for (int i = 0; i < code.length(); i++) {
            gc.save();
            double x = (i + 0.5) * charWidth + (rnd.nextDouble() * 5 - 2.5);
            double y = h / 2 + 8 + (rnd.nextDouble() * 10 - 5);

            // Random rotation
            double angle = rnd.nextDouble() * 40 - 20;
            gc.translate(x, y);
            gc.rotate(angle);

            gc.setFill(javafx.scene.paint.Color.rgb(rnd.nextInt(100), rnd.nextInt(100), rnd.nextInt(100)));
            gc.fillText(String.valueOf(code.charAt(i)), 0, 0);
            gc.restore();
        }

        // Final strike-through line
        gc.setStroke(javafx.scene.paint.Color.rgb(0, 0, 0, 0.4));
        gc.setLineWidth(2);
        gc.strokeLine(5, h / 2 + (rnd.nextDouble() * 10 - 5), w - 5, h / 2 + (rnd.nextDouble() * 10 - 5));
    }

    private String generateCaptcha(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        while (sb.length() < length) {
            int index = (int) (rnd.nextFloat() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    @FXML
    public void goToShopWithoutAccount() {
        Session.clear();
        loadPage("/Home.fxml");
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

        // CAPTCHA VALIDATION
        String enteredCaptcha = captchaField.getText().trim();
        if (enteredCaptcha.isEmpty()) {
            errorLabel.setText("⚠ Veuillez saisir le code CAPTCHA");
            return;
        }
        if (!enteredCaptcha.equalsIgnoreCase(currentCaptcha)) {
            errorLabel.setText("❌ Code CAPTCHA incorrect");
            refreshCaptcha();
            return;
        }

        try {
            for (Utilisateur u : service.afficher()) {

                if (u.getEmail().equals(email)
                        && u.getMotDePasse().equals(password)) {

                    // ✅ Successful login
                    errorLabel.setText("");

                    // admin → AdminDashboard; user → WinGo Shop
                    // Skip verification for admins
                    if (!"admin".equalsIgnoreCase(u.getType()) && !u.isVerified()) {
                        errorLabel.setText("⚠ Veuillez vérifier votre email.");
                        goToVerification(u.getEmail(), u.getVerificationCode());
                        return;
                    }

                    Session.setCurrentUser(u);
                    loadPage("/Home.fxml");
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

    private void goToVerification(String email, String code) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/VerifyEmail.fxml"));
            Parent root = loader.load();

            VerifyEmailController controller = loader.getController();
            controller.setUserEmail(email, code);

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
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
