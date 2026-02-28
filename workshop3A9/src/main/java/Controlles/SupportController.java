package Controlles;

import Entites.Reclamation;
import Entites.Suggestion;
import Services.EmailService;
import Services.ReclamationCRUD;
import Services.SuggestionCRUD;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SupportController implements Initializable {

    // Services
    private ReclamationCRUD reclamationCRUD;
    private SuggestionCRUD suggestionCRUD;
    private EmailService emailService;

    private int userId = 1;
    private String userEmail = "";
    private String userName = "Utilisateur";
    private String selectedFilePath = null;
    private Timeline liveTimer;
    private int countdown = 30;

    // FXML fields - Navigation & Layout
    @FXML
    private ScrollPane reclamationPanel;
    @FXML
    private ScrollPane suggestionPanel;
    @FXML
    private ScrollPane statsPanel;
    @FXML
    private Button reclamationTabBtn;
    @FXML
    private Button suggestionTabBtn;
    @FXML
    private Button statsTabBtn;
    @FXML
    private Label mainTitle;
    @FXML
    private Label mainSubtitle;
    @FXML
    private VBox reclamationForm;
    @FXML
    private VBox suggestionForm;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label profileEmail;
    @FXML
    private ImageView userAvatarView;
    @FXML
    private javafx.scene.layout.HBox userProfileBox;
    @FXML
    private Button adminDashboardBtn;

    // FXML fields - Réclamations
    @FXML
    private ComboBox<String> recTypeBox;
    @FXML
    private ComboBox<String> recPrioriteBox;
    @FXML
    private TextField recSujetField;
    @FXML
    private TextArea recDescArea;
    @FXML
    private Label recPieceJointeLabel;
    @FXML
    private ComboBox<String> recFilterStatutBox;
    @FXML
    private Label recResultCountLabel;
    @FXML
    private TableView<Reclamation> reclamationTable;
    @FXML
    private TableColumn<Reclamation, Integer> recColId;
    @FXML
    private TableColumn<Reclamation, String> recColType;
    @FXML
    private TableColumn<Reclamation, String> recColSujet;
    @FXML
    private TableColumn<Reclamation, String> recColDate;
    @FXML
    private TableColumn<Reclamation, String> recColStatut;
    @FXML
    private TableColumn<Reclamation, String> recColPriorite;
    @FXML
    private TableColumn<Reclamation, String> recColReponse;
    @FXML
    private TextArea recReponseArea;
    @FXML
    private ComboBox<String> recNouveauStatutBox;
    @FXML
    private TextField recEmailClientField;
    @FXML
    private TextField recNomClientField;

    // FXML fields - Suggestions
    @FXML
    private ComboBox<String> sugCategorieBox;
    @FXML
    private TextField sugSujetField;
    @FXML
    private TextArea sugDescArea;
    @FXML
    private ComboBox<String> sugReclamationBox;
    @FXML
    private ComboBox<String> sugFilterCategorieBox;
    @FXML
    private Label sugResultCountLabel;
    @FXML
    private TableView<Suggestion> suggestionTable;
    @FXML
    private TableColumn<Suggestion, Integer> sugColId;
    @FXML
    private TableColumn<Suggestion, String> sugColCategorie;
    @FXML
    private TableColumn<Suggestion, String> sugColSujet;
    @FXML
    private TableColumn<Suggestion, String> sugColDate;
    @FXML
    private TableColumn<Suggestion, String> sugColStatut;
    @FXML
    private TableColumn<Suggestion, String> sugColReclamationLien;

    // FXML fields - Stats
    @FXML
    private Label statsKpiTotalRec;
    @FXML
    private Label statsKpiEnAttente;
    @FXML
    private Label statsKpiResolues;
    @FXML
    private Label statsKpiUrgentes;
    @FXML
    private Label statsKpiTotalSug;
    @FXML
    private Label statsKpiTauxResolution;
    @FXML
    private ProgressBar statsProgressBar;

    @FXML
    private Label statsCountdownLabel;

    @FXML
    private Label infoCardTitle;
    @FXML
    private Label infoCardContent;
    @FXML
    private ProgressBar quickProgress;
    @FXML
    private Label infoCardFooter;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            reclamationCRUD = new ReclamationCRUD();
            suggestionCRUD = new SuggestionCRUD();
            emailService = new EmailService();

            if (Utils.Session.getCurrentUser() == null) {
                if (userProfileBox != null) {
                    userProfileBox.setVisible(false);
                    userProfileBox.setManaged(false);
                }
            } else {
                updateProfileInfo();
                checkAdminStatus();
            }

            setupFormulaires();
            setupTables();
            loadAllData();
            setupLiveStats();
            setupNavigation();

            System.out.println("✅ SupportController initialisé");

        } catch (Exception e) {
            showError("Erreur", "Impossible d'initialiser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUserInfo(int userId, String email, String name) {
        this.userId = userId;
        this.userEmail = email != null ? email : "";
        this.userName = name != null ? name : "Utilisateur #" + userId;
        updateProfileInfo();
    }

    private void setupFormulaires() {
        // Réclamation
        if (recTypeBox != null) {
            recTypeBox.setItems(FXCollections.observableArrayList(
                    "Général", "Service", "Transport", "Hébergement", "Restauration", "Technique", "Facturation",
                    "Autre"));
        }
        if (recPrioriteBox != null) {
            recPrioriteBox.setItems(FXCollections.observableArrayList("Basse", "Moyenne", "Haute", "Critique"));
            recPrioriteBox.setValue("Moyenne");
        }
        if (recFilterStatutBox != null) {
            recFilterStatutBox.setItems(FXCollections.observableArrayList(
                    "Tous", "En attente", "En cours", "Résolue", "Rejetée"));
            recFilterStatutBox.setValue("Tous");
            recFilterStatutBox.setOnAction(e -> filterReclamations());
        }
        if (recNouveauStatutBox != null) {
            recNouveauStatutBox
                    .setItems(FXCollections.observableArrayList("En cours", "Résolue", "Rejetée", "En attente"));
        }

        // Suggestion
        if (sugCategorieBox != null) {
            sugCategorieBox.setItems(FXCollections.observableArrayList(
                    "Interface", "Technique", "Service", "Application", "Sécurité", "Performance", "Autre"));
        }
        if (sugFilterCategorieBox != null) {
            sugFilterCategorieBox.setItems(FXCollections.observableArrayList(
                    "Tous", "Interface", "Technique", "Service", "Application", "Sécurité", "Performance", "Autre"));
            sugFilterCategorieBox.setValue("Tous");
            sugFilterCategorieBox.setOnAction(e -> filterSuggestions());
        }

        // Populate reclamation combo for suggestions
        if (sugReclamationBox != null) {
            sugReclamationBox.getItems().add("Aucune");
            try {
                for (Reclamation r : reclamationCRUD.afficherTous()) {
                    sugReclamationBox.getItems().add("#" + r.getId_reclamation() + " - " + r.getSujet());
                }
            } catch (Exception e) {
            }
            sugReclamationBox.setValue("Aucune");
        }
    }

    private void setupTables() {
        // Réclamation columns
        if (recColId != null)
            recColId.setCellValueFactory(new PropertyValueFactory<>("id_reclamation"));
        if (recColType != null)
            recColType.setCellValueFactory(new PropertyValueFactory<>("type_reclamation"));
        if (recColSujet != null)
            recColSujet.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        if (recColStatut != null)
            recColStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        if (recColPriorite != null)
            recColPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        if (recColDate != null) {
            recColDate.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getDate_reclamation() != null ? c.getValue().getDate_reclamation().toString() : "—"));
        }
        if (recColReponse != null) {
            recColReponse.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getReponse_admin() != null ? c.getValue().getReponse_admin() : "—"));
        }

        // Suggestion columns
        if (sugColId != null)
            sugColId.setCellValueFactory(new PropertyValueFactory<>("id_suggestion"));
        if (sugColCategorie != null)
            sugColCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        if (sugColSujet != null)
            sugColSujet.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        if (sugColStatut != null)
            sugColStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        if (sugColDate != null) {
            sugColDate.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getDate_suggestion() != null ? c.getValue().getDate_suggestion().toString() : "—"));
        }
        if (sugColReclamationLien != null) {
            sugColReclamationLien.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getId_reclamation() != null ? "#" + c.getValue().getId_reclamation() : "—"));
        }

        // Row selection listeners
        if (reclamationTable != null) {
            reclamationTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
                if (sel != null)
                    updateInfoCardForReclamations();
            });
        }
        if (suggestionTable != null) {
            suggestionTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
                if (sel != null)
                    updateInfoCardForSuggestions();
            });
        }
    }

    private void loadAllData() {
        loadReclamations();
        loadSuggestions();
        updateStats();
    }

    private void loadReclamations() {
        if (reclamationTable == null)
            return;
        List<Reclamation> list = reclamationCRUD.afficherTous();
        reclamationTable.setItems(FXCollections.observableArrayList(list));
        if (recResultCountLabel != null)
            recResultCountLabel.setText(list.size() + " réclamation(s)");
    }

    private void loadSuggestions() {
        if (suggestionTable == null)
            return;
        List<Suggestion> list = suggestionCRUD.afficherTous();
        suggestionTable.setItems(FXCollections.observableArrayList(list));
        if (sugResultCountLabel != null)
            sugResultCountLabel.setText(list.size() + " suggestion(s)");
    }

    private void updateStats() {
        List<Reclamation> recs = reclamationCRUD.afficherTous();
        List<Suggestion> sugs = suggestionCRUD.afficherTous();

        long total = recs.size();
        long enAttente = recs.stream().filter(r -> "En attente".equals(r.getStatut())).count();
        long resolues = recs.stream().filter(r -> "Résolue".equals(r.getStatut())).count();
        long urgentes = recs.stream().filter(r -> "Critique".equals(r.getPriorite()) || "Haute".equals(r.getPriorite()))
                .count();
        double taux = total > 0 ? (double) resolues / total : 0;

        if (statsKpiTotalRec != null)
            statsKpiTotalRec.setText(String.valueOf(total));
        if (statsKpiEnAttente != null)
            statsKpiEnAttente.setText(String.valueOf(enAttente));
        if (statsKpiResolues != null)
            statsKpiResolues.setText(String.valueOf(resolues));
        if (statsKpiUrgentes != null)
            statsKpiUrgentes.setText(String.valueOf(urgentes));
        if (statsKpiTotalSug != null)
            statsKpiTotalSug.setText(String.valueOf(sugs.size()));
        if (statsKpiTauxResolution != null)
            statsKpiTauxResolution.setText(String.format("%.0f%%", taux * 100));
        if (statsProgressBar != null)
            statsProgressBar.setProgress(taux);
    }

    private void setupLiveStats() {
        liveTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            countdown--;
            if (countdown <= 0) {
                countdown = 30;
                updateStats();
            }
            if (statsCountdownLabel != null)
                statsCountdownLabel.setText(countdown + "s");
        }));
        liveTimer.setCycleCount(Timeline.INDEFINITE);
        liveTimer.play();
    }

    // ==================== MÉTHODES DE NAVIGATION ====================

    private void setupNavigation() {
        if (reclamationTabBtn != null)
            reclamationTabBtn.setOnAction(e -> showReclamationsTab());
        if (suggestionTabBtn != null)
            suggestionTabBtn.setOnAction(e -> showSuggestionsTab());
        if (statsTabBtn != null)
            statsTabBtn.setOnAction(e -> showStatsTab());

        if (reclamationForm != null) {
            reclamationForm.setVisible(false);
            reclamationForm.setManaged(false);
        }
        if (suggestionForm != null) {
            suggestionForm.setVisible(false);
            suggestionForm.setManaged(false);
        }
        updateProfileInfo();
        showReclamationsTab();
    }

    private void updateProfileInfo() {
        Entites.Utilisateur u = Utils.Session.getCurrentUser();
        if (u == null)
            return;

        this.userId = u.getId();
        this.userEmail = u.getEmail();
        this.userName = u.getNom() + " " + (u.getPrenom() != null ? u.getPrenom() : "");

        if (userNameLabel != null)
            userNameLabel.setText(userName);
        if (profileEmail != null)
            profileEmail.setText(userEmail);

        if (userAvatarView != null) {
            try {
                String gravatarUrl = Utils.GravatarUtil.getGravatarURL(userEmail, 80);
                Image img = new Image(gravatarUrl, true);
                userAvatarView.setImage(img);
            } catch (Exception e) {
                System.out.println("⚠️ Erreur Gravatar: " + e.getMessage());
            }
        }
    }

    private void checkAdminStatus() {
        if (Utils.Session.getCurrentUser() != null) {
            String role = Utils.Session.getCurrentUser().getType();
            if ("admin".equalsIgnoreCase(role)) {
                if (adminDashboardBtn != null) {
                    adminDashboardBtn.setVisible(true);
                    adminDashboardBtn.setManaged(true);
                }
            }
        }
    }

    @FXML
    private void showReclamationsTab() {
        if (reclamationPanel != null) {
            reclamationPanel.setVisible(true);
            reclamationPanel.setManaged(true);
        }
        if (suggestionPanel != null) {
            suggestionPanel.setVisible(false);
            suggestionPanel.setManaged(false);
        }
        if (statsPanel != null) {
            statsPanel.setVisible(false);
            statsPanel.setManaged(false);
        }
        updateTabButtons(reclamationTabBtn);
        if (mainTitle != null)
            mainTitle.setText("Centre de Support");
        if (mainSubtitle != null)
            mainSubtitle.setText("Gérez les réclamations des clients");
        updateInfoCardForReclamations();
        loadReclamations();
    }

    @FXML
    private void showSuggestionsTab() {
        if (reclamationPanel != null) {
            reclamationPanel.setVisible(false);
            reclamationPanel.setManaged(false);
        }
        if (suggestionPanel != null) {
            suggestionPanel.setVisible(true);
            suggestionPanel.setManaged(true);
        }
        if (statsPanel != null) {
            statsPanel.setVisible(false);
            statsPanel.setManaged(false);
        }
        updateTabButtons(suggestionTabBtn);
        if (mainTitle != null)
            mainTitle.setText("Suggestions");
        if (mainSubtitle != null)
            mainSubtitle.setText("Collectez et gérez les idées d'amélioration");
        updateInfoCardForSuggestions();
        loadSuggestions();
    }

    @FXML
    private void showStatsTab() {
        if (reclamationPanel != null) {
            reclamationPanel.setVisible(false);
            reclamationPanel.setManaged(false);
        }
        if (suggestionPanel != null) {
            suggestionPanel.setVisible(false);
            suggestionPanel.setManaged(false);
        }
        if (statsPanel != null) {
            statsPanel.setVisible(true);
            statsPanel.setManaged(true);
        }
        updateTabButtons(statsTabBtn);
        if (mainTitle != null)
            mainTitle.setText("Statistiques");
        if (mainSubtitle != null)
            mainSubtitle.setText("Analyse en temps réel de l'activité support");
        updateStats();
        updateInfoCardForStats();
    }

    private void updateTabButtons(Button activeButton) {
        Button[] buttons = { reclamationTabBtn, suggestionTabBtn, statsTabBtn };
        for (Button btn : buttons) {
            if (btn != null) {
                if (btn == activeButton)
                    btn.setStyle(
                            "-fx-background-color: #A3B1FF; -fx-text-fill: white; -fx-font-size: 22px; -fx-background-radius: 20; -fx-padding: 15 15;");
                else
                    btn.setStyle(
                            "-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 22px; -fx-background-radius: 20; -fx-padding: 15 15;");
            }
        }
    }

    @FXML
    private void showAddReclamationForm() {
        if (reclamationForm != null) {
            reclamationForm.setVisible(true);
            reclamationForm.setManaged(true);
        }
    }

    @FXML
    private void hideReclamationForm() {
        if (reclamationForm != null) {
            reclamationForm.setVisible(false);
            reclamationForm.setManaged(false);
        }
    }

    @FXML
    private void showAddSuggestionForm() {
        if (suggestionForm != null) {
            suggestionForm.setVisible(true);
            suggestionForm.setManaged(true);
        }
    }

    @FXML
    private void hideSuggestionForm() {
        if (suggestionForm != null) {
            suggestionForm.setVisible(false);
            suggestionForm.setManaged(false);
        }
    }

    private void updateInfoCardForReclamations() {
        if (infoCardTitle != null)
            infoCardTitle.setText("Réclamation en cours");
        Reclamation selected = reclamationTable != null ? reclamationTable.getSelectionModel().getSelectedItem() : null;
        if (selected != null) {
            if (infoCardContent != null)
                infoCardContent.setText(selected.getSujet() + "\nStatut: " + selected.getStatut());
            if (quickProgress != null)
                quickProgress.setProgress("Résolue".equals(selected.getStatut()) ? 1.0
                        : "En cours".equals(selected.getStatut()) ? 0.5 : 0.2);
            if (infoCardFooter != null)
                infoCardFooter.setText("Priorité: " + selected.getPriorite());
        } else {
            if (infoCardContent != null)
                infoCardContent.setText("Sélectionnez une réclamation");
            if (quickProgress != null)
                quickProgress.setProgress(0);
            if (infoCardFooter != null)
                infoCardFooter.setText("Statistiques en temps réel");
        }
    }

    private void updateInfoCardForSuggestions() {
        if (infoCardTitle != null)
            infoCardTitle.setText("Suggestion active");
        Suggestion selected = suggestionTable != null ? suggestionTable.getSelectionModel().getSelectedItem() : null;
        if (selected != null) {
            if (infoCardContent != null)
                infoCardContent.setText(selected.getSujet() + "\nCatégorie: " + selected.getCategorie());
            if (quickProgress != null)
                quickProgress.setProgress(0.3);
            if (infoCardFooter != null)
                infoCardFooter.setText("Statut: " + selected.getStatut());
        } else {
            if (infoCardContent != null)
                infoCardContent.setText("Sélectionnez une suggestion");
            if (quickProgress != null)
                quickProgress.setProgress(0);
            if (infoCardFooter != null)
                infoCardFooter.setText("Partagez vos idées !");
        }
    }

    private void updateInfoCardForStats() {
        if (infoCardTitle != null)
            infoCardTitle.setText("Taux de résolution");
        List<Reclamation> recs = reclamationCRUD.afficherTous();
        long total = recs.size();
        long resolues = recs.stream().filter(r -> "Résolue".equals(r.getStatut())).count();
        double taux = total > 0 ? (double) resolues / total : 0;
        if (infoCardContent != null)
            infoCardContent.setText(String.format("%d réclamations résolues sur %d", resolues, total));
        if (quickProgress != null)
            quickProgress.setProgress(taux);
        if (infoCardFooter != null)
            infoCardFooter.setText(String.format("Taux: %.1f%%", taux * 100));
    }

    // ==================== ACTIONS ====================

    @FXML
    private void addReclamation() {
        String type = recTypeBox.getValue();
        String sujet = recSujetField.getText();
        String desc = recDescArea.getText();
        String prio = recPrioriteBox.getValue();
        if (type == null || sujet == null || desc == null)
            return;

        Reclamation r = new Reclamation(userId, type, sujet, desc, prio, selectedFilePath);
        reclamationCRUD.ajouter(r);
        emailService.envoyerConfirmationReclamation(r, userEmail, userName);
        hideReclamationForm();
        loadReclamations();
        updateStats();
    }

    @FXML
    private void addSuggestion() {
        String cat = sugCategorieBox.getValue();
        String sujet = sugSujetField.getText();
        String desc = sugDescArea.getText();
        if (cat == null || sujet == null || desc == null)
            return;

        Suggestion s = new Suggestion(userId, sujet, desc, cat, null);
        suggestionCRUD.ajouter(s);
        emailService.envoyerConfirmationSuggestion(s, userEmail, userName);
        hideSuggestionForm();
        loadSuggestions();
        updateStats();
    }

    @FXML
    private void respondToReclamation() {
        Reclamation sel = reclamationTable.getSelectionModel().getSelectedItem();
        if (sel == null)
            return;
        String reponse = recReponseArea.getText();
        String statut = recNouveauStatutBox.getValue();
        if (reponse == null || statut == null)
            return;
        reclamationCRUD.repondre(sel.getId_reclamation(), reponse, statut);
        loadReclamations();
    }

    @FXML
    private void filterReclamations() {
        String statut = recFilterStatutBox.getValue();
        if ("Tous".equals(statut))
            loadReclamations();
        else {
            List<Reclamation> filtered = reclamationCRUD.getByStatut(statut);
            reclamationTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    @FXML
    private void filterSuggestions() {
        String cat = sugFilterCategorieBox.getValue();
        if ("Tous".equals(cat))
            loadSuggestions();
        else {
            List<Suggestion> filtered = suggestionCRUD.getByCategorie(cat);
            suggestionTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    @FXML
    private void attachFile() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(null);
        if (file != null) {
            selectedFilePath = file.getAbsolutePath();
            recPieceJointeLabel.setText(file.getName());
        }
    }

    @FXML
    private void onStatsRefresh() {
        updateStats();
    }

    @FXML
    private void goToHome() {
        loadPage("/Home.fxml");
    }

    @FXML
    private void goToSupport() {
        // Déjà sur la page support
    }

    @FXML
    private void goToAdminDashboard() {
        loadPage("/AdminDashboard.fxml");
    }

    @FXML
    private void goToProfile() {
        if (Utils.Session.getCurrentUser() == null) {
            loadPage("/Signup.fxml");
            return;
        }
        loadPage("/Home.fxml"); // Redirige vers home pour éditer le profil
    }

    private void loadPage(String path) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(path));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) reclamationTable.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openWhatsApp() {
        System.out.println("Ouvrir WhatsApp Bot");
    }

    @FXML
    private void closeForm() {
        Platform.exit();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
