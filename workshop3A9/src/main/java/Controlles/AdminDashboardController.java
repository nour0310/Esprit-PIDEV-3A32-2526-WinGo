package Controlles;

import Entites.Profil;
import Entites.Utilisateur;
import Services.ProfilCRUD;
import Services.UtilisateurCRUD;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    private UtilisateurCRUD utilisateurCRUD;
    private ProfilCRUD profilCRUD;

    @FXML
    private ScrollPane userPanel;
    @FXML
    private ScrollPane profilePanel;
    @FXML
    private Button userTabBtn;
    @FXML
    private Button profileTabBtn;
    @FXML
    private Label mainTitle;
    @FXML
    private Label mainSubtitle;

    // Add User Form Fields
    @FXML
    private StackPane addUserOverlay;
    @FXML
    private TextField newNomField;
    @FXML
    private TextField newPrenomField;
    @FXML
    private TextField newEmailField;
    @FXML
    private TextField newTelephoneField;
    @FXML
    private TextField newAgeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private ComboBox<String> newTypeCombo;
    @FXML
    private Label addErrorMessage;

    @FXML
    private TextField userSearchField;

    // Add Profile Form Fields
    @FXML
    private StackPane addProfileOverlay;
    @FXML
    private TextField newProfUserIdField;
    @FXML
    private TextArea newProfBioArea;
    @FXML
    private TextField newProfImageField;
    @FXML
    private Label addProfErrorMessage;

    @FXML
    private TableView<Utilisateur> userTable;
    @FXML
    private TableColumn<Utilisateur, Integer> userColId;
    @FXML
    private TableColumn<Utilisateur, String> userColNom;
    @FXML
    private TableColumn<Utilisateur, String> userColPrenom;
    @FXML
    private TableColumn<Utilisateur, String> userColEmail;
    @FXML
    private TableColumn<Utilisateur, String> userColType;
    @FXML
    private TableColumn<Utilisateur, String> userColTelephone;
    @FXML
    private TableColumn<Utilisateur, Integer> userColAge;
    @FXML
    private TextField userNomField;
    @FXML
    private TextField userPrenomField;
    @FXML
    private TextField userTypeField;

    @FXML
    private TableView<Profil> profileTable;
    @FXML
    private TableColumn<Profil, Integer> profColId;
    @FXML
    private TableColumn<Profil, Integer> profColUser;
    @FXML
    private TableColumn<Profil, String> profColBio;
    @FXML
    private TableColumn<Profil, String> profColImage;
    @FXML
    private TextArea profBioArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        utilisateurCRUD = new UtilisateurCRUD();
        profilCRUD = new ProfilCRUD();
        setupTables();
        loadUsers();

        if (newTypeCombo != null) {
            newTypeCombo.setItems(FXCollections.observableArrayList("user", "admin"));
            newTypeCombo.setValue("user");
        }
    }

    private void setupTables() {
        userColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        userColNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        userColPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        userColEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        userColType.setCellValueFactory(new PropertyValueFactory<>("type"));
        userColTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        userColAge.setCellValueFactory(new PropertyValueFactory<>("age"));

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                userNomField.setText(sel.getNom());
                userPrenomField.setText(sel.getPrenom());
                userTypeField.setText(sel.getType());
            }
        });

        profColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        profColUser.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
        profColBio.setCellValueFactory(new PropertyValueFactory<>("bio"));
        profColImage.setCellValueFactory(new PropertyValueFactory<>("image"));

        profileTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                profBioArea.setText(sel.getBio());
            }
        });
    }

    @FXML
    private void showUsersTab() {
        userPanel.setVisible(true);
        userPanel.setManaged(true);
        profilePanel.setVisible(false);
        profilePanel.setManaged(false);
        updateTabButtons(userTabBtn);
        mainTitle.setText("Utilisateurs");
        mainSubtitle.setText("Gérez les comptes utilisateurs");
        loadUsers();
    }

    @FXML
    private void showProfilesTab() {
        userPanel.setVisible(false);
        userPanel.setManaged(false);
        profilePanel.setVisible(true);
        profilePanel.setManaged(true);
        updateTabButtons(profileTabBtn);
        mainTitle.setText("Profils");
        mainSubtitle.setText("Gérez les informations de profil");
        loadProfiles();
    }

    private void updateTabButtons(Button activeButton) {
        Button[] buttons = { userTabBtn, profileTabBtn };
        for (Button btn : buttons) {
            if (btn == activeButton)
                btn.setStyle(
                        "-fx-background-color: #A3B1FF; -fx-text-fill: white; -fx-font-size: 22px; -fx-background-radius: 20; -fx-padding: 15 15;");
            else
                btn.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 22px; -fx-background-radius: 20; -fx-padding: 15 15;");
        }
    }

    private void loadUsers() {
        try {
            javafx.collections.ObservableList<Utilisateur> allUsers = FXCollections
                    .observableArrayList(utilisateurCRUD.afficher());

            // 1. Wrap the ObservableList in a FilteredList
            FilteredList<Utilisateur> filteredData = new FilteredList<>(allUsers, p -> true);

            // 2. Set the filter Predicate whenever the filter changes.
            if (userSearchField != null) {
                userSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredData.setPredicate(user -> {
                        // If filter text is empty, display all users.
                        if (newValue == null || newValue.isEmpty()) {
                            return true;
                        }

                        // Compare name and prename of every user with filter text.
                        String lowerCaseFilter = newValue.toLowerCase();

                        if (user.getNom().toLowerCase().contains(lowerCaseFilter)) {
                            return true; // Filter matches nom.
                        } else if (user.getPrenom().toLowerCase().contains(lowerCaseFilter)) {
                            return true; // Filter matches prenom.
                        }
                        return false; // Does not match.
                    });
                });
            }

            // 3. Wrap the FilteredList in a SortedList.
            SortedList<Utilisateur> sortedData = new SortedList<>(filteredData);

            // 4. Bind the SortedList comparator to the TableView comparator.
            sortedData.comparatorProperty().bind(userTable.comparatorProperty());

            // 5. Add sorted (and filtered) data to the table.
            userTable.setItems(sortedData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfiles() {
        try {
            profileTable.setItems(FXCollections.observableArrayList(profilCRUD.afficher()));
        } catch (Exception e) {
        }
    }

    @FXML
    private void updateUser() {
        Utilisateur sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null)
            return;
        try {
            sel.setNom(userNomField.getText());
            sel.setPrenom(userPrenomField.getText());
            sel.setType(userTypeField.getText());
            utilisateurCRUD.modifier(sel);
            loadUsers();
        } catch (Exception e) {
        }
    }

    @FXML
    private void deleteUser() {
        Utilisateur sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null)
            return;
        try {
            utilisateurCRUD.supprimer(sel.getId());
            loadUsers();
        } catch (Exception e) {
        }
    }

    @FXML
    private void updateProfile() {
        Profil sel = profileTable.getSelectionModel().getSelectedItem();
        if (sel == null)
            return;
        try {
            sel.setBio(profBioArea.getText());
            profilCRUD.modifier(sel);
            loadProfiles();
        } catch (Exception e) {
        }
    }

    @FXML
    private void deleteProfile() {
        Profil sel = profileTable.getSelectionModel().getSelectedItem();
        if (sel == null)
            return;
        try {
            profilCRUD.supprimer(sel.getId());
            loadProfiles();
        } catch (Exception e) {
        }
    }

    @FXML
    private void handleAddAction() {
        if (userPanel.isVisible()) {
            showAddUserForm();
        } else if (profilePanel.isVisible()) {
            showAddProfileForm();
        }
    }

    @FXML
    private void showAddUserForm() {
        if (addUserOverlay != null) {
            addUserOverlay.setVisible(true);
            addErrorMessage.setText("");
        }
    }

    @FXML
    private void hideAddUserForm() {
        if (addUserOverlay != null) {
            addUserOverlay.setVisible(false);
            clearAddUserForm();
        }
    }

    private void clearAddUserForm() {
        newNomField.clear();
        newPrenomField.clear();
        newEmailField.clear();
        newTelephoneField.clear();
        newAgeField.clear();
        newPasswordField.clear();
        newTypeCombo.setValue("user");
    }

    @FXML
    private void addUser() {
        if (newNomField.getText().isEmpty() || newPrenomField.getText().isEmpty() ||
                newEmailField.getText().isEmpty() || newPasswordField.getText().isEmpty()) {
            addErrorMessage.setText("Veuillez remplir les champs obligatoires.");
            return;
        }

        try {
            int age = Integer.parseInt(newAgeField.getText());
            Utilisateur newUser = new Utilisateur();
            newUser.setNom(newNomField.getText());
            newUser.setPrenom(newPrenomField.getText());
            newUser.setEmail(newEmailField.getText());
            newUser.setTelephone(newTelephoneField.getText());
            newUser.setAge(age);
            newUser.setMotDePasse(newPasswordField.getText());
            newUser.setType(newTypeCombo.getValue());
            newUser.setVerified(true);

            utilisateurCRUD.ajouter(newUser);
            loadUsers();
            hideAddUserForm();
        } catch (NumberFormatException e) {
            addErrorMessage.setText("L'âge doit être un nombre.");
        } catch (Exception e) {
            addErrorMessage.setText("Erreur lors de la création.");
        }
    }

    @FXML
    private void showAddProfileForm() {
        if (addProfileOverlay != null) {
            addProfileOverlay.setVisible(true);
            addProfErrorMessage.setText("");
        }
    }

    @FXML
    private void hideAddProfileForm() {
        if (addProfileOverlay != null) {
            addProfileOverlay.setVisible(false);
            clearAddProfileForm();
        }
    }

    private void clearAddProfileForm() {
        newProfUserIdField.clear();
        newProfBioArea.clear();
        newProfImageField.clear();
    }

    @FXML
    private void addProfile() {
        if (newProfUserIdField.getText().isEmpty()) {
            addProfErrorMessage.setText("ID Utilisateur est obligatoire.");
            return;
        }

        try {
            int userId = Integer.parseInt(newProfUserIdField.getText());

            // Check if user exists
            boolean userExists = false;
            for (Utilisateur u : utilisateurCRUD.afficher()) {
                if (u.getId() == userId) {
                    userExists = true;
                    break;
                }
            }

            if (!userExists) {
                addProfErrorMessage.setText("Utilisateur non trouvé.");
                return;
            }

            Profil newProfil = new Profil();
            newProfil.setUtilisateurId(userId);
            newProfil.setBio(newProfBioArea.getText());
            newProfil.setImage(newProfImageField.getText());

            profilCRUD.ajouter(newProfil);
            loadProfiles();
            hideAddProfileForm();
        } catch (NumberFormatException e) {
            addProfErrorMessage.setText("ID doit être un nombre.");
        } catch (Exception e) {
            addProfErrorMessage.setText("Erreur lors de la création.");
        }
    }

    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
