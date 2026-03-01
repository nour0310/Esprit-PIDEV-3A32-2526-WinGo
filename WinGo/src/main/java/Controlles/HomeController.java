package Controlles;

import Entites.Profil;
import Entites.Utilisateur;
import Services.ProfilCRUD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private Label userNameLabel;

    @FXML
    private ImageView userAvatarView;

    @FXML
    private ScrollPane mainContentPane;

    @FXML
    private VBox profilePane;

    @FXML
    private TextArea profileBioField;

    @FXML
    private TextField profileImageField;

    @FXML
    private Label profileStatusLabel;

    @FXML
    private ImageView profilePreviewImg;

    @FXML private NavbarController navbarController;

    @FXML
    private javafx.scene.layout.HBox userProfileBox;

    @FXML
    private Button getStartedBtn;

    private ProfilCRUD profilCRUD = new ProfilCRUD();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Utils.Session.getCurrentUser() == null) {
            if (userProfileBox != null) {
                userProfileBox.setVisible(false);
                userProfileBox.setManaged(false);
            }
        } else {
            updateProfileInfo();
            // Admin status is now handled by NavbarController
            setupImagePreviewListener();
        }
    }

    private void setupImagePreviewListener() {
        profileImageField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePreview(newVal);
        });
    }

    private void updatePreview(String url) {
        if (profilePreviewImg != null) {
            if (url == null || url.trim().isEmpty()) {
                profilePreviewImg.setImage(null);
            } else {
                try {
                    Image img = new Image(url, true);
                    profilePreviewImg.setImage(img);
                } catch (Exception e) {
                    profilePreviewImg.setImage(null);
                }
            }
        }
    }

    // Admin status handled by NavbarController

    private void updateProfileInfo() {
        if (Utils.Session.getCurrentUser() != null) {
            Entites.Utilisateur u = Utils.Session.getCurrentUser();
            if (userNameLabel != null) {
                userNameLabel.setText(u.getNom() + " " + u.getPrenom());
            }
            if (userAvatarView != null) {
                try {
                    Services.ProfilCRUD profilCRUD = new Services.ProfilCRUD();
                    Entites.Profil p = profilCRUD.getProfilByUtilisateurId(u.getId());
                    if (p != null && p.getImage() != null && !p.getImage().isEmpty()) {
                        userAvatarView.setImage(new Image(p.getImage(), true));
                    } else {
                        String gravatarUrl = Utils.GravatarUtil.getGravatarURL(u.getEmail(), 80);
                        userAvatarView.setImage(new Image(gravatarUrl, true));
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Error loading profile avatar: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    public void goToHome(javafx.event.ActionEvent event) {
        showPane(mainContentPane);
    }

    @FXML
    public void goToProfile() {
        if (Utils.Session.getCurrentUser() == null) {
            loadPage("/Signup.fxml");
            return;
        }
        loadMyProfile();
        showPane(profilePane);
    }

    @FXML
    public void goToProfileFromBtn(javafx.event.ActionEvent event) {
        goToProfile();
    }

    private void showPane(javafx.scene.Node pane) {
        mainContentPane.setVisible(pane == mainContentPane);
        mainContentPane.setManaged(pane == mainContentPane);
        profilePane.setVisible(pane == profilePane);
        profilePane.setManaged(pane == profilePane);
    }

    private void loadMyProfile() {
        Utilisateur u = Utils.Session.getCurrentUser();
        if (u == null)
            return;
        try {
            Profil myProfil = null;
            for (Profil p : profilCRUD.afficher()) {
                if (p.getUtilisateurId() == u.getId()) {
                    myProfil = p;
                    break;
                }
            }
            if (myProfil != null) {
                profileBioField.setText(myProfil.getBio() != null ? myProfil.getBio() : "");
                profileImageField.setText(myProfil.getImage() != null ? myProfil.getImage() : "");
                updatePreview(myProfil.getImage());
            } else {
                profileBioField.clear();
                profileImageField.clear();
                updatePreview(null);
            }
            profileStatusLabel.setText("");
        } catch (Exception e) {
            profileStatusLabel.setText("âš ï¸ Error loading profile");
        }
    }

    @FXML
    public void saveProfile() {
        Utilisateur u = Utils.Session.getCurrentUser();
        if (u == null)
            return;
        try {
            String bio = profileBioField.getText() == null ? "" : profileBioField.getText().trim();
            String image = profileImageField.getText() == null ? "" : profileImageField.getText().trim();

            Profil existing = null;
            for (Profil p : profilCRUD.afficher()) {
                if (p.getUtilisateurId() == u.getId()) {
                    existing = p;
                    break;
                }
            }
            if (existing != null) {
                existing.setBio(bio);
                existing.setImage(image);
                profilCRUD.modifier(existing);
            } else {
                Profil p = new Profil(bio, image, u.getId());
                profilCRUD.ajouter(p);
            }

            profileStatusLabel.setText("âœ… Profile updated!");
            profileStatusLabel.setStyle("-fx-text-fill: green;");
            updateProfileInfo();
        } catch (Exception e) {
            profileStatusLabel.setText("âŒ Error: " + e.getMessage());
            profileStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void goToBlogs(javafx.event.ActionEvent event) {
        loadPage("/Blogs.fxml");
    }

    @FXML
    public void goToSupport(javafx.event.ActionEvent event) {
        loadPage("/Support.fxml");
    }

    @FXML
    public void handleLogout(javafx.event.ActionEvent event) {
        Utils.Session.clear();
        loadPage("/Login.fxml");
    }

    @FXML
    public void goToAdminDashboard(javafx.event.ActionEvent event) {
        loadPage("/AdminDashboard.fxml");
    }

    @FXML
    public void importImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir une Photo de Profil");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        java.io.File selectedFile = fileChooser.showOpenDialog(userNameLabel.getScene().getWindow());
        if (selectedFile != null) {
            String filePath = selectedFile.toURI().toString();
            profileImageField.setText(filePath);
            updatePreview(filePath);
        }
    }

    // Logout handled by NavbarController

    private void loadPage(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
