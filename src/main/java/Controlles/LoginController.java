package Controlles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    // =====================================================================
    // UTILISATEURS DE TEST — À remplacer par une vraie requête BDD
    // quand le module Users sera intégré la semaine prochaine
    // =====================================================================
    private static final int[][] USER_IDS     = {{1}, {2}, {3}, {99}};
    private static final String[] USER_EMAILS = {
            "client1@wingo.tn",
            "client2@wingo.tn",
            "client3@wingo.tn",
            "admin@wingo.tn"       // userId=99 → admin
    };
    private static final String[] USER_NOMS   = {
            "Client Un",
            "Client Deux",
            "Client Trois",
            "Administrateur"
    };
    private static final String[] USER_PASS   = {
            "pass1",
            "pass2",
            "pass3",
            "admin"
    };
    private static final boolean[] USER_ADMIN = {
            false, false, false, true
    };
    // =====================================================================

    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        // ── Vérification temporaire (sera remplacée par requête BDD) ──────
        int foundIndex = -1;
        for (int i = 0; i < USER_EMAILS.length; i++) {
            if (USER_EMAILS[i].equalsIgnoreCase(email) && USER_PASS[i].equals(password)) {
                foundIndex = i;
                break;
            }
        }

        if (foundIndex == -1) {
            showError("Email ou mot de passe incorrect.");
            return;
        }

        // ── Login réussi : on remplit la Session ──────────────────────────
        // C'EST CETTE LIGNE QUI RÉSOUT VOTRE PROBLÈME !
        // Quand le vrai module Users sera prêt, remplacez juste ces valeurs
        // par celles venant de votre base de données.
        Session.getInstance().login(
                USER_IDS[foundIndex][0],
                USER_EMAILS[foundIndex],
                USER_NOMS[foundIndex],
                USER_ADMIN[foundIndex]
        );

        System.out.println("✅ Connecté en tant que : "
                + USER_NOMS[foundIndex]
                + " (ID=" + USER_IDS[foundIndex][0] + ")"
                + (USER_ADMIN[foundIndex] ? " [ADMIN]" : " [CLIENT]"));

        // ── Ouvrir l'écran principal ──────────────────────────────────────
        ouvrirAccueil();
    }

    private void ouvrirAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation.fxml"));
            Parent root = loader.load();

            SupportController controller = loader.getController();
            // setUserInfo lit déjà la Session, mais on passe aussi explicitement
            controller.setUserInfo(
                    Session.getInstance().getUserId(),
                    Session.getInstance().getEmail(),
                    Session.getInstance().getNom()
            );

            Stage stage = new Stage();
            stage.setTitle("WinGo - Centre de Support");
            stage.setScene(new Scene(root, 1400, 850));
            stage.show();

            // Fermer la fenêtre de login
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur ouverture: " + e.getMessage());
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText("⚠ " + message);
            errorLabel.setVisible(true);
        }
    }

    // =====================================================================
    // À REMPLACER LA SEMAINE PROCHAINE
    // Quand le module Users sera prêt, remplacez handleLogin() par :
    //
    //   UserCRUD userCRUD = new UserCRUD();
    //   User user = userCRUD.findByEmailAndPassword(email, password);
    //   if (user == null) { showError("Identifiants incorrects"); return; }
    //   Session.getInstance().login(user.getId(), user.getEmail(), user.getNom(), user.isAdmin());
    //   ouvrirAccueil();
    //
    // =====================================================================
}