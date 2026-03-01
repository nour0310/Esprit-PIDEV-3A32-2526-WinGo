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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class SupportController implements Initializable {

    // Services
    private ReclamationCRUD reclamationCRUD;
    private SuggestionCRUD suggestionCRUD;
    private EmailService emailService;

    private int userId = Session.getInstance().isConnecte() ? Session.getInstance().getUserId() : -1;
    private String userEmail = "";
    private String userName = "Utilisateur";
    private boolean darkMode = false;
    private String selectedFilePath = null;
    private Timeline liveTimer;
    private int countdown = 30;

    // FXML fields - Réclamations
    @FXML private ComboBox<String> recTypeBox;
    @FXML private ComboBox<String> recPrioriteBox;
    @FXML private TextField recSujetField;
    @FXML private TextArea recDescArea;
    @FXML private Label recPieceJointeLabel;
    @FXML private TextField recSearchField;
    @FXML private ComboBox<String> recSearchTypeBox;
    @FXML private ComboBox<String> recFilterStatutBox;
    @FXML private Label recResultCountLabel;
    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, Integer> recColId;
    @FXML private TableColumn<Reclamation, Integer> recColUserId;
    @FXML private TableColumn<Reclamation, String> recColType;
    @FXML private TableColumn<Reclamation, String> recColSujet;
    @FXML private TableColumn<Reclamation, String> recColDate;
    @FXML private TableColumn<Reclamation, String> recColStatut;
    @FXML private TableColumn<Reclamation, String> recColPriorite;
    @FXML private TableColumn<Reclamation, String> recColReponse;
    @FXML private TableColumn<Reclamation, String> recColDateReponse;
    @FXML private TextArea recReponseArea;
    @FXML private ComboBox<String> recNouveauStatutBox;
    @FXML private TextField recEmailClientField;    // Email du client pour réclamation
    @FXML private TextField recNomClientField;      // Nom du client pour réclamation

    // FXML fields - Suggestions
    @FXML private ComboBox<String> sugCategorieBox;
    @FXML private TextField sugSujetField;
    @FXML private TextArea sugDescArea;
    @FXML private ComboBox<String> sugReclamationBox;
    @FXML private TextField sugSearchField;
    @FXML private ComboBox<String> sugSearchTypeBox;
    @FXML private ComboBox<String> sugFilterCategorieBox;
    @FXML private Label sugResultCountLabel;
    @FXML private TableView<Suggestion> suggestionTable;
    @FXML private TableColumn<Suggestion, Integer> sugColId;
    @FXML private TableColumn<Suggestion, Integer> sugColUserId;
    @FXML private TableColumn<Suggestion, String> sugColCategorie;
    @FXML private TableColumn<Suggestion, String> sugColSujet;
    @FXML private TableColumn<Suggestion, String> sugColDate;
    @FXML private TableColumn<Suggestion, String> sugColStatut;
    @FXML private TableColumn<Suggestion, String> sugColReclamationLien;
    @FXML private TableColumn<Suggestion, String> sugColReponse;
    @FXML private TableColumn<Suggestion, String> sugColDateReponse;
    @FXML private TextArea sugReponseArea;
    @FXML private ComboBox<String> sugNouveauStatutBox;
    @FXML private TextField sugEmailClientField;    // Email du client pour suggestion
    @FXML private TextField sugNomClientField;      // Nom du client pour suggestion

    // FXML fields - Stats
    @FXML private Label statsKpiTotalRec;
    @FXML private Label statsKpiEnAttente;
    @FXML private Label statsKpiResolues;
    @FXML private Label statsKpiUrgentes;
    @FXML private Label statsKpiTotalSug;
    @FXML private Label statsKpiTauxResolution;
    @FXML private ProgressBar statsProgressBar;
    @FXML private Label statsLiveIndicator;
    @FXML private Label statsCountdownLabel;
    @FXML private Label statsLastUpdate;

    // FXML fields - Sidebar (anciens)
    @FXML private Label userIdLabel;
    @FXML private Label darkModeLabel;
    @FXML private Pane darkModeToggle;
    @FXML private Circle darkModeCircle;
    @FXML private VBox sidebar;

    // ==================== NOUVEAUX COMPOSANTS POUR LA NOUVELLE INTERFACE ====================
    @FXML private ScrollPane reclamationPanel;
    @FXML private ScrollPane suggestionPanel;
    @FXML private ScrollPane statsPanel;
    @FXML private Button reclamationTabBtn;
    @FXML private Button suggestionTabBtn;
    @FXML private Button statsTabBtn;
    @FXML private Label mainTitle;
    @FXML private Label mainSubtitle;
    @FXML private VBox reclamationForm;
    @FXML private VBox suggestionForm;
    @FXML private TextField globalSearchField;
    @FXML private Label profileName;
    @FXML private Label profileEmail;
    @FXML private Label profileInitials;
    @FXML private Label profileInitialsHeader;
    @FXML private Label infoCardTitle;
    @FXML private Label infoCardContent;
    @FXML private ProgressBar quickProgress;
    @FXML private Label infoCardFooter;
    @FXML private Button recFilterBtn;
    @FXML private Label darkModeIcon;
    @FXML private VBox infoCard;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // ✅ LIRE LA SESSION EN PREMIER — avant tout chargement de données
            if (Session.getInstance().isConnecte()) {
                this.userId    = Session.getInstance().getUserId();
                this.userEmail = Session.getInstance().getEmail();
                this.userName  = Session.getInstance().getNom();
            }
            System.out.println("👤 Utilisateur connecté : " + userName + " (ID=" + userId + ")");

            reclamationCRUD = new ReclamationCRUD();
            suggestionCRUD = new SuggestionCRUD();
            emailService = new EmailService();

            setupFormulaires();
            setupTables();
            loadAllData();
            setupLiveStats();
            setupEmailValidation();

            setupNavigation();

            if (userIdLabel != null) {
                userIdLabel.setText(userName);
            }

            System.out.println("✅ SupportController initialisé");

        } catch (Exception e) {
            showError("Erreur", "Impossible d'initialiser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUserId(int userId) {
        this.userId = userId;
        this.userName = "User #" + userId;
        if (userIdLabel != null) userIdLabel.setText(userName);
        updateProfileInfo();
    }

    public void setCurrentUserId(int userId) {
        setUserId(userId);
    }

    public void setUserInfo(int userId, String email, String name) {
        this.userId = userId;
        this.userEmail = email != null ? email : "";
        this.userName = name != null ? name : "Utilisateur #" + userId;
        if (userIdLabel != null) userIdLabel.setText(this.userName);

        // Mettre à jour les informations du profil
        updateProfileInfo();

        // ✅ RECHARGER avec le bon userId
        // initialize() s'exécute AVANT setUserInfo() → les données étaient chargées avec l'ancien userId
        loadReclamations();
        loadSuggestions();
    }

    private void setupFormulaires() {
        // Réclamation
        if (recTypeBox != null) {
            recTypeBox.setItems(FXCollections.observableArrayList(
                    "Général", "Service", "Transport", "Hébergement", "Restauration", "Technique", "Facturation", "Autre"));
        }
        if (recPrioriteBox != null) {
            recPrioriteBox.setItems(FXCollections.observableArrayList("Basse", "Moyenne", "Haute", "Critique"));
            recPrioriteBox.setValue("Moyenne");
        }
        if (recSearchTypeBox != null) {
            recSearchTypeBox.setItems(FXCollections.observableArrayList("Tous", "Sujet", "Type", "Description"));
            recSearchTypeBox.setValue("Tous");
        }
        if (recFilterStatutBox != null) {
            recFilterStatutBox.setItems(FXCollections.observableArrayList(
                    "Tous", "En attente", "En cours", "Résolue", "Rejetée"));
            recFilterStatutBox.setValue("Tous");
            recFilterStatutBox.setOnAction(e -> filterReclamations());
        }
        if (recNouveauStatutBox != null) {
            recNouveauStatutBox.setItems(FXCollections.observableArrayList("En cours", "Résolue", "Rejetée", "En attente"));
        }

        // Suggestion
        if (sugCategorieBox != null) {
            sugCategorieBox.setItems(FXCollections.observableArrayList(
                    "Interface", "Technique", "Service", "Application", "Sécurité", "Performance", "Autre"));
        }
        if (sugSearchTypeBox != null) {
            sugSearchTypeBox.setItems(FXCollections.observableArrayList("Tous", "Sujet", "Catégorie", "Description"));
            sugSearchTypeBox.setValue("Tous");
        }
        if (sugFilterCategorieBox != null) {
            sugFilterCategorieBox.setItems(FXCollections.observableArrayList(
                    "Tous", "Interface", "Technique", "Service", "Application", "Sécurité", "Performance", "Autre"));
            sugFilterCategorieBox.setValue("Tous");
            sugFilterCategorieBox.setOnAction(e -> filterSuggestions());
        }
        if (sugNouveauStatutBox != null) {
            sugNouveauStatutBox.setItems(FXCollections.observableArrayList("Acceptée", "Refusée", "En étude", "Implémentée"));
        }

        // Populate reclamation combo for suggestions
        if (sugReclamationBox != null) {
            sugReclamationBox.getItems().add("Aucune");
            try {
                for (Reclamation r : reclamationCRUD.afficherTous()) {
                    sugReclamationBox.getItems().add("#" + r.getId_reclamation() + " - " + r.getSujet());
                }
            } catch (Exception e) {
                // Ignorer
            }
            sugReclamationBox.setValue("Aucune");
        }

        // Pré-remplir les champs email avec l'email utilisateur si disponible
        if (recEmailClientField != null && !userEmail.isEmpty()) {
            recEmailClientField.setText(userEmail);
        }
        if (recNomClientField != null && !userName.isEmpty()) {
            recNomClientField.setText(userName);
        }
        if (sugEmailClientField != null && !userEmail.isEmpty()) {
            sugEmailClientField.setText(userEmail);
        }
        if (sugNomClientField != null && !userName.isEmpty()) {
            sugNomClientField.setText(userName);
        }
    }

    private void setupTables() {
        // Réclamation columns
        if (recColId != null) recColId.setCellValueFactory(new PropertyValueFactory<>("id_reclamation"));
        if (recColUserId != null) recColUserId.setCellValueFactory(new PropertyValueFactory<>("id_user"));
        if (recColType != null) recColType.setCellValueFactory(new PropertyValueFactory<>("type_reclamation"));
        if (recColSujet != null) recColSujet.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        if (recColStatut != null) recColStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        if (recColPriorite != null) recColPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));

        if (recColDate != null) {
            recColDate.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getDate_reclamation() != null ? c.getValue().getDate_reclamation().toString() : "—"));
        }

        if (recColReponse != null) {
            recColReponse.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getReponse_admin() != null ? c.getValue().getReponse_admin() : "—"));
        }

        if (recColDateReponse != null) {
            recColDateReponse.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getDate_reponse() != null ? c.getValue().getDate_reponse().toString() : "—"));
        }

        // Suggestion columns
        if (sugColId != null) sugColId.setCellValueFactory(new PropertyValueFactory<>("id_suggestion"));
        if (sugColUserId != null) sugColUserId.setCellValueFactory(new PropertyValueFactory<>("id_user"));
        if (sugColCategorie != null) sugColCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        if (sugColSujet != null) sugColSujet.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        if (sugColStatut != null) sugColStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        if (sugColDate != null) {
            sugColDate.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getDate_suggestion() != null ? c.getValue().getDate_suggestion().toString() : "—"));
        }

        if (sugColReclamationLien != null) {
            sugColReclamationLien.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getId_reclamation() != null ? "#" + c.getValue().getId_reclamation() : "—"));
        }

        if (sugColReponse != null) {
            sugColReponse.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getReponse_admin() != null ? c.getValue().getReponse_admin() : "—"));
        }

        if (sugColDateReponse != null) {
            sugColDateReponse.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getDate_reponse() != null ? c.getValue().getDate_reponse().toString() : "—"));
        }

        // Row selection listeners
        if (reclamationTable != null) {
            reclamationTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
                if (sel != null) {
                    if (recSujetField != null) recSujetField.setText(sel.getSujet());
                    if (recTypeBox != null) recTypeBox.setValue(sel.getType_reclamation());
                    if (recDescArea != null) recDescArea.setText(sel.getDescription() != null ? sel.getDescription() : "");
                    if (recPrioriteBox != null) recPrioriteBox.setValue(sel.getPriorite());
                    if (recReponseArea != null && sel.getReponse_admin() != null) {
                        recReponseArea.setText(sel.getReponse_admin());
                    }
                    updateInfoCardForReclamations();
                }
            });
        }

        if (suggestionTable != null) {
            suggestionTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
                if (sel != null) {
                    if (sugSujetField != null) sugSujetField.setText(sel.getSujet());
                    if (sugCategorieBox != null) sugCategorieBox.setValue(sel.getCategorie());
                    if (sugDescArea != null) sugDescArea.setText(sel.getDescription() != null ? sel.getDescription() : "");
                    if (sugReponseArea != null && sel.getReponse_admin() != null) {
                        sugReponseArea.setText(sel.getReponse_admin());
                    }
                    updateInfoCardForSuggestions();
                }
            });
        }
    }

    private void loadAllData() {
        loadReclamations();
        loadSuggestions();
        updateStats();
    }

    private void loadReclamations() {
        if (reclamationTable == null || reclamationCRUD == null) return;
        if (userId == -1) {
            System.err.println("⚠ Aucun utilisateur connecté — chargement des réclamations ignoré");
            reclamationTable.setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            List<Reclamation> list = reclamationCRUD.getByUser(userId); // ← getByUser au lieu de afficherTous
            reclamationTable.setItems(FXCollections.observableArrayList(list));
            if (recResultCountLabel != null) {
                recResultCountLabel.setText(list.size() + " réclamation(s)");
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement réclamations: " + e.getMessage());
        }
    }

    private void loadSuggestions() {
        if (suggestionTable == null || suggestionCRUD == null) return;
        if (userId == -1) {
            System.err.println("⚠ Aucun utilisateur connecté — chargement des suggestions ignoré");
            suggestionTable.setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            List<Suggestion> list = suggestionCRUD.getByUser(userId); // ← getByUser au lieu de afficherTous
            suggestionTable.setItems(FXCollections.observableArrayList(list));
            if (sugResultCountLabel != null) {
                sugResultCountLabel.setText(list.size() + " suggestion(s)");
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement suggestions: " + e.getMessage());
        }
    }

    private void updateStats() {
        try {
            if (reclamationCRUD == null || suggestionCRUD == null) return;

            List<Reclamation> recs = reclamationCRUD.afficherTous();
            List<Suggestion> sugs = suggestionCRUD.afficherTous();

            long total = recs.size();
            long enAttente = recs.stream().filter(r -> "En attente".equals(r.getStatut())).count();
            long resolues = recs.stream().filter(r -> "Résolue".equals(r.getStatut())).count();
            long urgentes = recs.stream().filter(r -> "Critique".equals(r.getPriorite()) || "Haute".equals(r.getPriorite())).count();
            double taux = total > 0 ? (double) resolues / total : 0;

            if (statsKpiTotalRec != null) statsKpiTotalRec.setText(String.valueOf(total));
            if (statsKpiEnAttente != null) statsKpiEnAttente.setText(String.valueOf(enAttente));
            if (statsKpiResolues != null) statsKpiResolues.setText(String.valueOf(resolues));
            if (statsKpiUrgentes != null) statsKpiUrgentes.setText(String.valueOf(urgentes));
            if (statsKpiTotalSug != null) statsKpiTotalSug.setText(String.valueOf(sugs.size()));
            if (statsKpiTauxResolution != null) statsKpiTauxResolution.setText(String.format("%.0f%%", taux * 100));
            if (statsProgressBar != null) statsProgressBar.setProgress(taux);
            if (statsLastUpdate != null) {
                statsLastUpdate.setText("🕐 " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        } catch (Exception e) {
            System.err.println("Erreur stats: " + e.getMessage());
        }
    }

    private void setupLiveStats() {
        liveTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            countdown--;
            if (countdown <= 0) {
                countdown = 30;
                updateStats();
            }
            if (statsCountdownLabel != null) statsCountdownLabel.setText(countdown + "s");
        }));
        liveTimer.setCycleCount(Timeline.INDEFINITE);
        liveTimer.play();
    }

    // ==================== VALIDATION EMAIL ====================

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        // Regex pour validation d'email (standard)
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        // Validation supplémentaire pour Gmail (optionnel)
        if (email.toLowerCase().endsWith("@gmail.com")) {
            // Pour Gmail, on peut ajouter des vérifications supplémentaires
            String localPart = email.substring(0, email.indexOf('@')).toLowerCase();

            // Règles pour Gmail :
            // - Pas de point à la fin du local-part
            // - Pas de points consécutifs
            if (localPart.endsWith(".") || localPart.contains("..")) {
                return false;
            }
        }

        return email.matches(emailRegex);
    }

    private void setupEmailValidation() {
        if (recEmailClientField != null) {
            recEmailClientField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { // Quand le champ perd le focus
                    String email = recEmailClientField.getText().trim();
                    if (!email.isBlank() && !isValidEmail(email)) {
                        // Afficher un indicateur visuel
                        recEmailClientField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2px;");

                        // Tooltip pour informer l'utilisateur
                        Tooltip tooltip = new Tooltip("Format d'email invalide");
                        Tooltip.install(recEmailClientField, tooltip);

                        // Afficher une alerte discrète
                        showValidationError("L'adresse email '" + email + "' n'est pas valide");
                    } else {
                        recEmailClientField.setStyle("");
                        Tooltip.uninstall(recEmailClientField, null);
                    }
                }
            });
        }

        if (sugEmailClientField != null) {
            sugEmailClientField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    String email = sugEmailClientField.getText().trim();
                    if (!email.isBlank() && !isValidEmail(email)) {
                        sugEmailClientField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2px;");
                        Tooltip tooltip = new Tooltip("Format d'email invalide");
                        Tooltip.install(sugEmailClientField, tooltip);
                        showValidationError("L'adresse email '" + email + "' n'est pas valide");
                    } else {
                        sugEmailClientField.setStyle("");
                        Tooltip.uninstall(sugEmailClientField, null);
                    }
                }
            });
        }
    }

    // Réclamation actions
    @FXML
    private void addReclamation() {
        try {
            String type = recTypeBox != null ? recTypeBox.getValue() : null;
            String sujet = recSujetField != null ? recSujetField.getText().trim() : "";
            String desc = recDescArea != null ? recDescArea.getText().trim() : "";
            String prio = recPrioriteBox != null && recPrioriteBox.getValue() != null ? recPrioriteBox.getValue() : "Moyenne";

            // Récupération email/nom client
            String emailClient = (recEmailClientField != null) ? recEmailClientField.getText().trim() : userEmail;
            String nomClient = (recNomClientField != null) ? recNomClientField.getText().trim() : userName;

            if (type == null || type.isEmpty()) {
                showValidationError("Veuillez sélectionner un type");
                return;
            }
            if (sujet.length() < 5) {
                showValidationError("Le sujet doit contenir au moins 5 caractères");
                return;
            }
            if (desc.length() < 10) {
                showValidationError("La description doit contenir au moins 10 caractères");
                return;
            }

            // VALIDATION EMAIL AMÉLIORÉE
            if (emailClient != null && !emailClient.isBlank() && !isValidEmail(emailClient)) {
                showValidationError("L'adresse email '" + emailClient + "' n'est pas valide.\nVeuillez vérifier le format (ex: nom@domaine.com)");
                return;
            }

            Reclamation r = new Reclamation(userId, type, sujet, desc, prio, selectedFilePath);
            reclamationCRUD.ajouter(r);

            // Email de confirmation
            boolean emailEnvoye = false;
            if (emailClient != null && !emailClient.isBlank() && isValidEmail(emailClient)) {
                emailEnvoye = emailService.envoyerConfirmationReclamation(r, emailClient, nomClient);
            }

            // Notification admin si priorité haute/critique
            emailService.notifierAdminNouvelleReclamation(r);

            String msgEmail = emailEnvoye
                    ? "\n📧 Email envoyé à " + emailClient
                    : (emailClient != null && !emailClient.isBlank() ? "\n⚠ Aucun email envoyé - Adresse invalide" : "");

            showSuccess("Succès", "Réclamation #" + r.getId_reclamation() + " créée" + msgEmail);

            clearReclamationFields();
            loadReclamations();
            updateStats();
            hideReclamationForm();

        } catch (Exception e) {
            showError("Erreur", "Création échouée: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void updateReclamation() {
        Reclamation sel = getSelectedReclamation();
        if (sel == null) return;

        String sujet = recSujetField != null ? recSujetField.getText().trim() : "";
        if (sujet.isEmpty()) {
            showValidationError("Le sujet ne peut pas être vide");
            return;
        }

        sel.setSujet(sujet);
        if (recDescArea != null) sel.setDescription(recDescArea.getText().trim());
        if (recTypeBox != null) sel.setType_reclamation(recTypeBox.getValue());
        if (recPrioriteBox != null) sel.setPriorite(recPrioriteBox.getValue());

        reclamationCRUD.modifier(sel);
        showSuccess("Succès", "Réclamation #" + sel.getId_reclamation() + " modifiée");
        loadReclamations();
    }

    @FXML
    private void deleteReclamation() {
        Reclamation sel = getSelectedReclamation();
        if (sel == null) return;

        boolean confirmed = showConfirmation("Supprimer", "Supprimer la réclamation #" + sel.getId_reclamation() + " ?");
        if (confirmed) {
            reclamationCRUD.supprimer(sel.getId_reclamation());
            loadReclamations();
            updateStats();
            showSuccess("Succès", "Réclamation supprimée");
        }
    }

    @FXML
    private void clearReclamationFields() {
        if (recSujetField != null) recSujetField.clear();
        if (recDescArea != null) recDescArea.clear();
        if (recTypeBox != null) recTypeBox.setValue(null);
        if (recPrioriteBox != null) recPrioriteBox.setValue("Moyenne");
        if (recReponseArea != null) recReponseArea.clear();
        if (recEmailClientField != null) recEmailClientField.setText(userEmail);
        if (recNomClientField != null) recNomClientField.setText(userName);
        if (recPieceJointeLabel != null) recPieceJointeLabel.setText("Aucun fichier sélectionné");
        if (reclamationTable != null) reclamationTable.getSelectionModel().clearSelection();
        selectedFilePath = null;
    }

    @FXML
    private void attachFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir un fichier");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tous fichiers", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx")
        );
        File file = fc.showOpenDialog(null);
        if (file != null) {
            selectedFilePath = file.getAbsolutePath();
            if (recPieceJointeLabel != null) recPieceJointeLabel.setText("📎 " + file.getName());
        }
    }

    @FXML
    private void respondToReclamation() {
        Reclamation sel = getSelectedReclamation();
        if (sel == null) return;

        String reponse = recReponseArea != null ? recReponseArea.getText().trim() : "";
        if (reponse.isEmpty()) {
            showValidationError("Veuillez saisir une réponse");
            return;
        }
        if (reponse.length() < 10) {
            showValidationError("La réponse doit contenir au moins 10 caractères");
            return;
        }

        String statut = recNouveauStatutBox != null && recNouveauStatutBox.getValue() != null
                ? recNouveauStatutBox.getValue() : "En cours";

        reclamationCRUD.repondre(sel.getId_reclamation(), reponse, statut);
        sel.setReponse_admin(reponse);
        sel.setStatut(statut);

        // Demander email pour notification
        String emailClient = demanderEmailClient("Email du client pour la réclamation #" + sel.getId_reclamation());
        boolean emailEnvoye = false;
        if (emailClient != null && !emailClient.isBlank() && isValidEmail(emailClient)) {
            emailEnvoye = emailService.envoyerReponseReclamation(sel, emailClient, "Client #" + sel.getId_user());
        }

        String msgEmail = emailEnvoye
                ? "\n📧 Email envoyé"
                : (emailClient != null && !emailClient.isBlank() ? "\n⚠ Échec envoi email - Adresse invalide" : "\n(Aucun email envoyé)");

        showSuccess("Succès", "Réponse enregistrée pour #" + sel.getId_reclamation() + msgEmail);
        loadReclamations();
    }

    @FXML
    private void viewReclamationDetails() {
        Reclamation sel = getSelectedReclamation();
        if (sel == null) return;

        String details = String.format("""
            📋 *Réclamation #%d*
            
            📌 Sujet     : %s
            📂 Type      : %s
            ⚡ Priorité  : %s
            📊 Statut    : %s
            📅 Date      : %s
            
            📝 Description :
            %s
            
            💬 Réponse admin :
            %s
            📅 Date réponse : %s""",
                sel.getId_reclamation(),
                nvl(sel.getSujet()),
                nvl(sel.getType_reclamation()),
                nvl(sel.getPriorite()),
                nvl(sel.getStatut()),
                nvl(sel.getDate_reclamation()),
                nvl(sel.getDescription()),
                nvl(sel.getReponse_admin()),
                nvl(sel.getDate_reponse())
        );

        showInfo("Détails", details);
    }

    @FXML
    private void onRecSearch() {
        String term = recSearchField != null ? recSearchField.getText().trim() : "";
        if (term.isEmpty()) {
            loadReclamations();
            return;
        }

        String type = recSearchTypeBox != null ? recSearchTypeBox.getValue() : "Tous";

        // On part toujours des réclamations de CET utilisateur uniquement
        List<Reclamation> mesReclamations;
        try {
            mesReclamations = reclamationCRUD.getByUser(userId);
        } catch (Exception e) {
            mesReclamations = new java.util.ArrayList<>();
        }

        final String termLower = term.toLowerCase();
        List<Reclamation> results;

        if ("Statut".equals(type)) {
            results = mesReclamations.stream()
                    .filter(r -> r.getStatut() != null && r.getStatut().toLowerCase().contains(termLower))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // Recherche dans sujet, type, description selon le filtre choisi
            results = mesReclamations.stream()
                    .filter(r -> {
                        boolean matchSujet = r.getSujet() != null && r.getSujet().toLowerCase().contains(termLower);
                        boolean matchType  = r.getType_reclamation() != null && r.getType_reclamation().toLowerCase().contains(termLower);
                        boolean matchDesc  = r.getDescription() != null && r.getDescription().toLowerCase().contains(termLower);
                        if ("Sujet".equals(type))       return matchSujet;
                        if ("Type".equals(type))        return matchType;
                        if ("Description".equals(type)) return matchDesc;
                        return matchSujet || matchType || matchDesc; // "Tous"
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        if (reclamationTable != null) {
            reclamationTable.setItems(FXCollections.observableArrayList(results));
        }
        if (recResultCountLabel != null) {
            recResultCountLabel.setText(results.size() + " résultat(s) pour « " + term + " »");
        }
    }

    @FXML
    private void onRecClearSearch() {
        if (recSearchField != null) recSearchField.clear();
        if (recFilterStatutBox != null) recFilterStatutBox.setValue("Tous");
        loadReclamations();
    }

    @FXML
    private void filterReclamations() {
        String statut = recFilterStatutBox != null ? recFilterStatutBox.getValue() : "Tous";
        if ("Tous".equals(statut)) {
            loadReclamations();
            return;
        }

        // On part toujours des réclamations de CET utilisateur, puis on filtre par statut
        try {
            List<Reclamation> filtered = reclamationCRUD.getByUser(userId).stream()
                    .filter(r -> statut.equals(r.getStatut()))
                    .collect(java.util.stream.Collectors.toList());

            if (reclamationTable != null) {
                reclamationTable.setItems(FXCollections.observableArrayList(filtered));
            }
            if (recResultCountLabel != null) {
                recResultCountLabel.setText(filtered.size() + " réclamation(s) — Statut : " + statut);
            }
        } catch (Exception e) {
            System.err.println("Erreur filtre réclamations: " + e.getMessage());
        }
    }

    // Suggestion actions
    @FXML
    private void addSuggestion() {
        try {
            String categorie = sugCategorieBox != null ? sugCategorieBox.getValue() : null;
            String sujet = sugSujetField != null ? sugSujetField.getText().trim() : "";
            String desc = sugDescArea != null ? sugDescArea.getText().trim() : "";

            String emailClient = (sugEmailClientField != null) ? sugEmailClientField.getText().trim() : userEmail;
            String nomClient = (sugNomClientField != null) ? sugNomClientField.getText().trim() : userName;

            if (categorie == null || categorie.isEmpty()) {
                showValidationError("Veuillez sélectionner une catégorie");
                return;
            }
            if (sujet.length() < 5) {
                showValidationError("Le sujet doit contenir au moins 5 caractères");
                return;
            }
            if (desc.length() < 10) {
                showValidationError("La description doit contenir au moins 10 caractères");
                return;
            }

            // VALIDATION EMAIL POUR SUGGESTION
            if (emailClient != null && !emailClient.isBlank() && !isValidEmail(emailClient)) {
                showValidationError("L'adresse email '" + emailClient + "' n'est pas valide.\nVeuillez vérifier le format (ex: nom@domaine.com)");
                return;
            }

            Integer recId = null;
            if (sugReclamationBox != null && !"Aucune".equals(sugReclamationBox.getValue())) {
                String val = sugReclamationBox.getValue();
                try {
                    recId = Integer.parseInt(val.substring(1, val.indexOf(" - ")));
                } catch (Exception ignored) {}
            }

            Suggestion s = new Suggestion(userId, sujet, desc, categorie, recId);
            suggestionCRUD.ajouter(s);

            // Email de confirmation
            boolean emailEnvoye = false;
            if (emailClient != null && !emailClient.isBlank() && isValidEmail(emailClient)) {
                emailEnvoye = emailService.envoyerConfirmationSuggestion(s, emailClient, nomClient);
            }

            String msgEmail = emailEnvoye
                    ? "\n📧 Email envoyé à " + emailClient
                    : (emailClient != null && !emailClient.isBlank() ? "\n⚠ Aucun email envoyé - Adresse invalide" : "");

            showSuccess("Succès", "Suggestion #" + s.getId_suggestion() + " créée" + msgEmail);

            clearSuggestionFields();
            loadSuggestions();
            updateStats();
            hideSuggestionForm();

        } catch (Exception e) {
            showError("Erreur", "Création échouée: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void updateSuggestion() {
        Suggestion sel = getSelectedSuggestion();
        if (sel == null) return;

        String sujet = sugSujetField != null ? sugSujetField.getText().trim() : "";
        if (sujet.isEmpty()) {
            showValidationError("Le sujet ne peut pas être vide");
            return;
        }

        sel.setSujet(sujet);
        if (sugDescArea != null) sel.setDescription(sugDescArea.getText().trim());
        if (sugCategorieBox != null) sel.setCategorie(sugCategorieBox.getValue());

        suggestionCRUD.modifier(sel);
        showSuccess("Succès", "Suggestion #" + sel.getId_suggestion() + " modifiée");
        loadSuggestions();
    }

    @FXML
    private void deleteSuggestion() {
        Suggestion sel = getSelectedSuggestion();
        if (sel == null) return;

        boolean confirmed = showConfirmation("Supprimer", "Supprimer la suggestion #" + sel.getId_suggestion() + " ?");
        if (confirmed) {
            suggestionCRUD.supprimer(sel.getId_suggestion());
            loadSuggestions();
            updateStats();
            showSuccess("Succès", "Suggestion supprimée");
        }
    }

    @FXML
    private void clearSuggestionFields() {
        if (sugSujetField != null) sugSujetField.clear();
        if (sugDescArea != null) sugDescArea.clear();
        if (sugCategorieBox != null) sugCategorieBox.setValue(null);
        if (sugReclamationBox != null) sugReclamationBox.setValue("Aucune");
        if (sugReponseArea != null) sugReponseArea.clear();
        if (sugEmailClientField != null) sugEmailClientField.setText(userEmail);
        if (sugNomClientField != null) sugNomClientField.setText(userName);
        if (suggestionTable != null) suggestionTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void respondToSuggestion() {
        Suggestion sel = getSelectedSuggestion();
        if (sel == null) return;

        String reponse = sugReponseArea != null ? sugReponseArea.getText().trim() : "";
        if (reponse.isEmpty()) {
            showValidationError("Veuillez saisir une réponse");
            return;
        }
        if (reponse.length() < 10) {
            showValidationError("La réponse doit contenir au moins 10 caractères");
            return;
        }

        String statut = sugNouveauStatutBox != null && sugNouveauStatutBox.getValue() != null
                ? sugNouveauStatutBox.getValue() : "En étude";

        suggestionCRUD.repondre(sel.getId_suggestion(), reponse, statut);
        showSuccess("Succès", "Réponse enregistrée pour #" + sel.getId_suggestion());
        loadSuggestions();
    }

    @FXML
    private void viewSuggestionDetails() {
        Suggestion sel = getSelectedSuggestion();
        if (sel == null) return;

        String details = String.format("""
            💡 *Suggestion #%d*
            
            📌 Sujet      : %s
            📂 Catégorie  : %s
            📊 Statut     : %s
            📅 Date       : %s
            🔗 Réclamation: %s
            
            📝 Description :
            %s
            
            💬 Réponse admin :
            %s
            📅 Date réponse : %s""",
                sel.getId_suggestion(),
                nvl(sel.getSujet()),
                nvl(sel.getCategorie()),
                nvl(sel.getStatut()),
                nvl(sel.getDate_suggestion()),
                sel.getId_reclamation() != null ? "#" + sel.getId_reclamation() : "—",
                nvl(sel.getDescription()),
                nvl(sel.getReponse_admin()),
                nvl(sel.getDate_reponse())
        );

        showInfo("Détails", details);
    }

    @FXML
    private void onSugSearch() {
        String term = sugSearchField != null ? sugSearchField.getText().trim() : "";
        if (term.isEmpty()) {
            loadSuggestions();
            return;
        }

        // On part toujours des suggestions de CET utilisateur uniquement
        List<Suggestion> mesSuggestions;
        try {
            mesSuggestions = suggestionCRUD.getByUser(userId);
        } catch (Exception e) {
            mesSuggestions = new java.util.ArrayList<>();
        }

        final String termLower = term.toLowerCase();
        List<Suggestion> results = mesSuggestions.stream()
                .filter(s -> {
                    boolean matchSujet = s.getSujet() != null && s.getSujet().toLowerCase().contains(termLower);
                    boolean matchCat   = s.getCategorie() != null && s.getCategorie().toLowerCase().contains(termLower);
                    boolean matchDesc  = s.getDescription() != null && s.getDescription().toLowerCase().contains(termLower);
                    return matchSujet || matchCat || matchDesc;
                })
                .collect(java.util.stream.Collectors.toList());

        if (suggestionTable != null) {
            suggestionTable.setItems(FXCollections.observableArrayList(results));
        }
        if (sugResultCountLabel != null) {
            sugResultCountLabel.setText(results.size() + " résultat(s) pour « " + term + " »");
        }
    }

    @FXML
    private void onSugClearSearch() {
        if (sugSearchField != null) sugSearchField.clear();
        if (sugFilterCategorieBox != null) sugFilterCategorieBox.setValue("Tous");
        loadSuggestions();
    }

    @FXML
    private void filterSuggestions() {
        String cat = sugFilterCategorieBox != null ? sugFilterCategorieBox.getValue() : "Tous";
        if ("Tous".equals(cat)) {
            loadSuggestions();
            return;
        }

        // On part toujours des suggestions de CET utilisateur, puis on filtre par catégorie
        try {
            List<Suggestion> filtered = suggestionCRUD.getByUser(userId).stream()
                    .filter(s -> cat.equals(s.getCategorie()))
                    .collect(java.util.stream.Collectors.toList());

            if (suggestionTable != null) {
                suggestionTable.setItems(FXCollections.observableArrayList(filtered));
            }
            if (sugResultCountLabel != null) {
                sugResultCountLabel.setText(filtered.size() + " suggestion(s) — Catégorie : " + cat);
            }
        } catch (Exception e) {
            System.err.println("Erreur filtre suggestions: " + e.getMessage());
        }
    }

    // Stats actions
    @FXML
    private void onStatsRefresh() {
        countdown = 30;
        updateStats();
        showInfo("Actualisation", "Statistiques mises à jour");
    }

    // Sidebar actions
    @FXML
    private void goToHome() {
        try {
            Stage stage = (Stage) globalSearchField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            // ignorer
        }
    }

    @FXML
    private void toggleDarkMode() {
        darkMode = !darkMode;
        if (sidebar != null) {
            sidebar.setStyle(darkMode
                    ? "-fx-background-color: #0F172A;"
                    : "-fx-background-color: #1A1D3A;");
        }
        if (darkModeLabel != null) darkModeLabel.setText(darkMode ? "Light mode" : "Dark mode");
        if (darkModeCircle != null) darkModeCircle.setTranslateX(darkMode ? 22 : 2);
        if (darkModeIcon != null) darkModeIcon.setText(darkMode ? "☀️" : "🌙");
    }

    @FXML
    private void setFrench() {
        showInfo("Langue", "Français activé");
    }

    @FXML
    private void setEnglish() {
        showInfo("Language", "English activated");
    }

    @FXML
    private void setArabic() {
        showInfo("اللغة", "تم التغيير إلى العربية");
    }

    @FXML
    private void openWhatsApp() {
        String message = """
            📱 *Configuration WhatsApp Bot*
            
            1. Lancer WhatsAppWebhook.java
            2. Lancer ngrok http 8080
            3. Copier l'URL ngrok
            4. Configurer dans Twilio Console
            
            Commandes disponibles:
            • MENU - Afficher le menu
            • 1 - Nouvelle réclamation
            • 2 - Nouvelle suggestion
            • 3 - Suivre réclamation
            """;
        showInfo("WhatsApp Bot", message);
    }

    @FXML
    private void closeForm() {
        if (liveTimer != null) liveTimer.stop();
        try {
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            Platform.exit();
        }
    }

    // Helper methods
    private Reclamation getSelectedReclamation() {
        Reclamation sel = reclamationTable != null ? reclamationTable.getSelectionModel().getSelectedItem() : null;
        if (sel == null) {
            showValidationError("Veuillez sélectionner une réclamation");
        }
        return sel;
    }

    private Suggestion getSelectedSuggestion() {
        Suggestion sel = suggestionTable != null ? suggestionTable.getSelectionModel().getSelectedItem() : null;
        if (sel == null) {
            showValidationError("Veuillez sélectionner une suggestion");
        }
        return sel;
    }

    private String demanderEmailClient(String prompt) {
        TextInputDialog dialog = new TextInputDialog(userEmail);
        dialog.setTitle("Email client");
        dialog.setHeaderText("📧 Notification par email");
        dialog.setContentText(prompt + "\n(Laissez vide pour ne pas envoyer d'email) :");
        dialog.getEditor().setPromptText("exemple@email.com");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText("Données invalides");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String nvl(Object o) {
        return o != null ? o.toString() : "—";
    }

    private void closeCurrentWindow() {
        try {
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            // Ignorer
        }
    }

    // ==================== MÉTHODES DE NAVIGATION ====================

    // Initialisation des panneaux
    private void setupNavigation() {
        // Configuration des boutons de la sidebar
        if (reclamationTabBtn != null) {
            reclamationTabBtn.setOnAction(e -> showReclamationsTab());
        }
        if (suggestionTabBtn != null) {
            suggestionTabBtn.setOnAction(e -> showSuggestionsTab());
        }
        if (statsTabBtn != null) {
            statsTabBtn.setOnAction(e -> showStatsTab());
        }

        // Masquer les formulaires au démarrage
        if (reclamationForm != null) {
            reclamationForm.setVisible(false);
            reclamationForm.setManaged(false);
        }
        if (suggestionForm != null) {
            suggestionForm.setVisible(false);
            suggestionForm.setManaged(false);
        }

        // Initialiser les informations du profil
        updateProfileInfo();

        // Afficher les réclamations par défaut
        showReclamationsTab();
    }

    private void updateProfileInfo() {
        if (profileName != null) {
            profileName.setText(userName);
        }
        if (profileEmail != null) {
            profileEmail.setText(userEmail.isEmpty() ? "utilisateur@wingo.tn" : userEmail);
        }
        if (profileInitials != null) {
            // Prendre les initiales du nom
            String initials = "NH";
            if (userName != null && !userName.isEmpty()) {
                String[] parts = userName.split(" ");
                if (parts.length >= 2) {
                    initials = (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
                } else if (parts.length == 1 && !parts[0].isEmpty()) {
                    initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
                }
            }
            profileInitials.setText(initials);
        }
        if (profileInitialsHeader != null) {
            profileInitialsHeader.setText(profileInitials != null ? profileInitials.getText() : "NH");
        }
    }

    @FXML
    private void showReclamationsTab() {
        // Afficher le panneau des réclamations
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

        // Mettre à jour les styles des boutons
        updateTabButtons(reclamationTabBtn);

        // Mettre à jour les titres
        if (mainTitle != null) mainTitle.setText("Centre de Support");
        if (mainSubtitle != null) mainSubtitle.setText("Gérez les réclamations des clients");

        // Mettre à jour la carte d'info
        updateInfoCardForReclamations();

        // Charger les données
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

        if (mainTitle != null) mainTitle.setText("Suggestions");
        if (mainSubtitle != null) mainSubtitle.setText("Collectez et gérez les idées d'amélioration");

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

        if (mainTitle != null) mainTitle.setText("Statistiques");
        if (mainSubtitle != null) mainSubtitle.setText("Analyse en temps réel de l'activité support");

        updateStats();
        updateInfoCardForStats();
    }

    private void updateTabButtons(Button activeButton) {
        Button[] buttons = {reclamationTabBtn, suggestionTabBtn, statsTabBtn};
        for (Button btn : buttons) {
            if (btn != null) {
                if (btn == activeButton) {
                    btn.setStyle("-fx-background-color: #A3B1FF; -fx-text-fill: white; -fx-font-size: 22px; -fx-background-radius: 20; -fx-padding: 15 15;");
                } else {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 22px; -fx-background-radius: 20; -fx-padding: 15 15;");
                }
            }
        }
    }

    @FXML
    private void showAddReclamationForm() {
        if (reclamationForm != null) {
            reclamationForm.setVisible(true);
            reclamationForm.setManaged(true);
            clearReclamationFields();
        }
    }
    @FXML
    private void ouvrirBackOffice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReclamationBackOffice.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Back Office - Réclamations Admin");
            stage.setScene(new Scene(root, 1300, 850));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur ouverture back office: " + e.getMessage());
            alert.showAndWait();
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
            clearSuggestionFields();
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
        if (infoCardTitle != null) infoCardTitle.setText("Réclamation en cours");
        if (infoCardContent != null) {
            // MODIFICATION ICI - Remplacer Reclamation selected = getSelectedReclamation();
            Reclamation selected = null;
            if (reclamationTable != null) {
                selected = reclamationTable.getSelectionModel().getSelectedItem();
            }

            if (selected != null) {
                infoCardContent.setText(selected.getSujet() + "\nStatut: " + selected.getStatut());
                if (quickProgress != null) {
                    double progress = "Résolue".equals(selected.getStatut()) ? 1.0 :
                            "En cours".equals(selected.getStatut()) ? 0.5 : 0.2;
                    quickProgress.setProgress(progress);
                }
                if (infoCardFooter != null) infoCardFooter.setText("Priorité: " + selected.getPriorite());
            } else {
                infoCardContent.setText("Sélectionnez une réclamation dans la liste");
                if (quickProgress != null) quickProgress.setProgress(0);
                if (infoCardFooter != null) infoCardFooter.setText("Statistiques en temps réel");
            }
        }
    }
    private void updateInfoCardForSuggestions() {
        if (infoCardTitle != null) infoCardTitle.setText("Suggestion active");
        if (infoCardContent != null) {
            // MODIFICATION ICI - Remplacer Suggestion selected = getSelectedSuggestion();
            Suggestion selected = null;
            if (suggestionTable != null) {
                selected = suggestionTable.getSelectionModel().getSelectedItem();
            }

            if (selected != null) {
                infoCardContent.setText(selected.getSujet() + "\nCatégorie: " + selected.getCategorie());
                if (quickProgress != null) quickProgress.setProgress(0.3);
                if (infoCardFooter != null) infoCardFooter.setText("Statut: " + selected.getStatut());
            } else {
                infoCardContent.setText("Sélectionnez une suggestion dans la liste");
                if (quickProgress != null) quickProgress.setProgress(0);
                if (infoCardFooter != null) infoCardFooter.setText("Partagez vos idées !");
            }
        }
    }



    private void updateInfoCardForStats() {
        if (infoCardTitle != null) infoCardTitle.setText("Taux de résolution");
        if (infoCardContent != null) {
            try {
                List<Reclamation> recs = reclamationCRUD.afficherTous();
                long total = recs.size();
                long resolues = recs.stream().filter(r -> "Résolue".equals(r.getStatut())).count();
                double taux = total > 0 ? (double) resolues / total : 0;

                infoCardContent.setText(String.format("%d réclamations résolues sur %d", resolues, total));
                if (quickProgress != null) quickProgress.setProgress(taux);
                if (infoCardFooter != null) infoCardFooter.setText(String.format("Taux: %.1f%%", taux * 100));
            } catch (Exception e) {
                infoCardContent.setText("Chargement des statistiques...");
            }
        }
    }
}