package Controlles;

import Entites.Reclamation;
import Entites.Suggestion;
import Services.EmailService;
import Services.ReclamationCRUD;
import Services.SuggestionCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ReclamationBackController implements Initializable {

    // ===== Services =====
    private ReclamationCRUD reclamationCRUD;
    private SuggestionCRUD suggestionCRUD;
    private EmailService emailService;

    private Reclamation reclamationSelectionnee;
    private Suggestion suggestionSelectionnee;

    // ===== FXML - Sidebar =====
    @FXML private ToggleButton btnDashboard;
    @FXML private ToggleButton btnReclamations;
    @FXML private ToggleButton btnSuggestions;

    // ===== FXML - Vues =====
    @FXML private VBox viewDashboard;
    @FXML private VBox viewReclamations;
    @FXML private VBox viewSuggestions;

    // ===== FXML - Dashboard =====
    @FXML private Label kpiTotal;
    @FXML private Label kpiEnAttente;
    @FXML private Label kpiResolues;
    @FXML private Label kpiUrgentes;
    @FXML private PieChart statutChart;
    @FXML private VBox recentReclamationsContainer;

    // ===== FXML - Vue Réclamations =====
    @FXML private TextField recSearchField;
    @FXML private ComboBox<String> filterStatutBox;
    @FXML private ComboBox<String> filterPrioriteBox;
    @FXML private Label recCountLabel;
    @FXML private VBox reclamationsContainer;

    // ===== FXML - Vue Suggestions =====
    @FXML private TextField sugSearchField;
    @FXML private ComboBox<String> filterSugStatutBox;
    @FXML private Label sugCountLabel;
    @FXML private VBox suggestionsContainer;

    // ===== INITIALISATION =====
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            reclamationCRUD = new ReclamationCRUD();
            suggestionCRUD = new SuggestionCRUD();
            emailService = new EmailService();

            setupFiltres();
            loadData();

            System.out.println("✅ ReclamationBackController initialisé");
        } catch (Exception e) {
            showAlert("Erreur d'initialisation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ===== SETUP FILTRES =====
    private void setupFiltres() {
        if (filterStatutBox != null)
            filterStatutBox.setItems(FXCollections.observableArrayList("Tous", "En attente", "En cours", "Résolue", "Rejetée"));
        if (filterPrioriteBox != null)
            filterPrioriteBox.setItems(FXCollections.observableArrayList("Tous", "Basse", "Moyenne", "Haute", "Critique"));
        if (filterSugStatutBox != null)
            filterSugStatutBox.setItems(FXCollections.observableArrayList("Tous", "Recue", "En examen", "Acceptée", "Refusée"));
    }

    // ===== NAVIGATION =====
    @FXML
    private void switchToDashboard() {
        showView(viewDashboard);
        setToggleStyle(btnDashboard, true);
        setToggleStyle(btnReclamations, false);
        setToggleStyle(btnSuggestions, false);
        loadData();
    }

    @FXML
    private void switchToReclamations() {
        showView(viewReclamations);
        setToggleStyle(btnDashboard, false);
        setToggleStyle(btnReclamations, true);
        setToggleStyle(btnSuggestions, false);
        loadReclamations();
    }

    @FXML
    private void switchToSuggestions() {
        showView(viewSuggestions);
        setToggleStyle(btnDashboard, false);
        setToggleStyle(btnReclamations, false);
        setToggleStyle(btnSuggestions, true);
        loadSuggestions();
    }

    private void showView(VBox vue) {
        if (viewDashboard != null)   { viewDashboard.setVisible(false);   viewDashboard.setManaged(false); }
        if (viewReclamations != null) { viewReclamations.setVisible(false); viewReclamations.setManaged(false); }
        if (viewSuggestions != null)  { viewSuggestions.setVisible(false);  viewSuggestions.setManaged(false); }
        vue.setVisible(true);
        vue.setManaged(true);
    }

    private void setToggleStyle(ToggleButton btn, boolean actif) {
        if (btn == null) return;
        if (actif) {
            btn.setStyle("-fx-background-color: #A3B1FF; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 14 22; -fx-background-radius: 14; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-alignment: CENTER_LEFT; -fx-padding: 14 22; -fx-background-radius: 14; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
    }

    // ===== CHARGEMENT DASHBOARD =====
    @FXML
    public void loadData() {
        try {
            List<Reclamation> recs = reclamationCRUD.afficherTous();
            List<Suggestion> sugs = suggestionCRUD.afficherTous();

            long total     = recs.size();
            long enAttente = recs.stream().filter(r -> "En attente".equals(r.getStatut())).count();
            long enCours   = recs.stream().filter(r -> "En cours".equals(r.getStatut())).count();
            long resolues  = recs.stream().filter(r -> "Résolue".equals(r.getStatut())).count();
            long rejetees  = recs.stream().filter(r -> "Rejetée".equals(r.getStatut())).count();
            long urgentes  = recs.stream().filter(r -> "Critique".equals(r.getPriorite())).count();

            // KPIs
            if (kpiTotal != null)     kpiTotal.setText(String.valueOf(total));
            if (kpiEnAttente != null) kpiEnAttente.setText(String.valueOf(enAttente));
            if (kpiResolues != null)  kpiResolues.setText(String.valueOf(resolues));
            if (kpiUrgentes != null)  kpiUrgentes.setText(String.valueOf(urgentes));

            // PieChart Statuts
            if (statutChart != null) {
                statutChart.getData().clear();
                if (enAttente > 0) statutChart.getData().add(new PieChart.Data("En attente (" + enAttente + ")", enAttente));
                if (enCours > 0)   statutChart.getData().add(new PieChart.Data("En cours (" + enCours + ")", enCours));
                if (resolues > 0)  statutChart.getData().add(new PieChart.Data("Résolues (" + resolues + ")", resolues));
                if (rejetees > 0)  statutChart.getData().add(new PieChart.Data("Rejetées (" + rejetees + ")", rejetees));
                if (total == 0)    statutChart.getData().add(new PieChart.Data("Aucune donnée", 1));
            }

            // Réclamations récentes
            if (recentReclamationsContainer != null) {
                recentReclamationsContainer.getChildren().clear();
                recs.stream()
                        .filter(r -> !"Résolue".equals(r.getStatut()))
                        .limit(5)
                        .forEach(r -> recentReclamationsContainer.getChildren().add(buildRecentCard(r)));
                if (recentReclamationsContainer.getChildren().isEmpty()) {
                    Label vide = new Label("✅ Aucune réclamation en attente !");
                    vide.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px; -fx-padding: 15;");
                    recentReclamationsContainer.getChildren().add(vide);
                }
            }

        } catch (Exception e) {
            showAlert("Erreur de chargement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ===== CHARGEMENT RÉCLAMATIONS - cartes style collègue =====
    @FXML
    public void loadReclamations() {
        try {
            List<Reclamation> liste = reclamationCRUD.afficherTous();
            afficherCartesReclamations(liste);
        } catch (Exception e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void afficherCartesReclamations(List<Reclamation> liste) {
        if (reclamationsContainer == null) return;
        reclamationsContainer.getChildren().clear();
        if (recCountLabel != null) recCountLabel.setText(liste.size() + " réclamation(s)");

        if (liste.isEmpty()) {
            VBox vide = new VBox(10);
            vide.setAlignment(Pos.CENTER);
            vide.setStyle("-fx-padding: 60;");
            Label icon = new Label("🛟");
            icon.setStyle("-fx-font-size: 48px;");
            Label msg = new Label("Aucune réclamation trouvée");
            msg.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 18px;");
            vide.getChildren().addAll(icon, msg);
            reclamationsContainer.getChildren().add(vide);
            return;
        }

        for (Reclamation r : liste) {
            reclamationsContainer.getChildren().add(buildReclamationCard(r));
        }
    }

    // ===== CARTE RÉCLAMATION - style identique aux articles de la collègue =====
    private HBox buildReclamationCard(Reclamation r) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20 25; " +
                "-fx-border-color: #F1F5F9; -fx-border-width: 1; -fx-border-radius: 20; -fx-cursor: hand;");
        card.getStyleClass().add("rec-card");

        // Icône priorité colorée (comme la miniature des articles)
        StackPane iconPane = new StackPane();
        iconPane.setMinWidth(60); iconPane.setMinHeight(60);
        iconPane.setMaxWidth(60); iconPane.setMaxHeight(60);
        String bgColor = switch (r.getPriorite() != null ? r.getPriorite() : "") {
            case "Critique" -> "#FEE2E2";
            case "Haute"    -> "#FEF3C7";
            case "Basse"    -> "#DCFCE7";
            default         -> "#DBEAFE";
        };
        String fgColor = switch (r.getPriorite() != null ? r.getPriorite() : "") {
            case "Critique" -> "#EF4444";
            case "Haute"    -> "#F59E0B";
            case "Basse"    -> "#22C55E";
            default         -> "#3B82F6";
        };
        String emoji = switch (r.getType_reclamation() != null ? r.getType_reclamation() : "") {
            case "Transport"    -> "🚌";
            case "Hébergement"  -> "🏨";
            case "Restauration" -> "🍽️";
            case "Technique"    -> "⚙️";
            case "Facturation"  -> "💳";
            case "Service"      -> "🎯";
            default             -> "📋";
        };
        iconPane.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 15;");
        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 22px;");
        iconPane.getChildren().add(iconLabel);

        // Infos principales
        VBox infos = new VBox(5);
        HBox.setHgrow(infos, Priority.ALWAYS);

        Label sujet = new Label(r.getSujet() != null ? r.getSujet() : "Sans sujet");
        sujet.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: #1E293B;");

        HBox meta = new HBox(15);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label userLabel = new Label("👤 User #" + r.getId_user());
        userLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");

        // Badge type
        Label typeLabel = new Label(r.getType_reclamation() != null ? r.getType_reclamation().toUpperCase() : "GÉNÉRAL");
        typeLabel.setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #6366F1; " +
                "-fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: 900;");

        String dateStr = r.getDate_reclamation() != null ? "📅 " + r.getDate_reclamation().toString() : "";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");

        meta.getChildren().addAll(userLabel, typeLabel, dateLabel);
        infos.getChildren().addAll(sujet, meta);

        // Colonne droite : priorité + statut + boutons
        VBox droite = new VBox(8);
        droite.setAlignment(Pos.CENTER_RIGHT);

        // Badge priorité
        Label prioLabel = new Label(r.getPriorite() != null ? r.getPriorite() : "Moyenne");
        prioLabel.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + fgColor + "; " +
                "-fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: 900;");

        // Badge statut
        String statutBg = switch (r.getStatut() != null ? r.getStatut() : "") {
            case "Résolue"    -> "#DCFCE7";
            case "En cours"   -> "#DBEAFE";
            case "Rejetée"    -> "#FEE2E2";
            default           -> "#FEF3C7";
        };
        String statutFg = switch (r.getStatut() != null ? r.getStatut() : "") {
            case "Résolue"    -> "#16A34A";
            case "En cours"   -> "#1D4ED8";
            case "Rejetée"    -> "#DC2626";
            default           -> "#92400E";
        };
        Label statutLabel = new Label(r.getStatut() != null ? r.getStatut() : "En attente");
        statutLabel.setStyle("-fx-background-color: " + statutBg + "; -fx-text-fill: " + statutFg + "; " +
                "-fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: 900;");

        // Boutons éditer / répondre / supprimer
        HBox btns = new HBox(8);
        btns.setAlignment(Pos.CENTER_RIGHT);

        Button btnRepondre = new Button("✉ Répondre");
        btnRepondre.setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #A3B1FF; " +
                "-fx-background-radius: 10; -fx-padding: 6 14; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px;");
        btnRepondre.setOnAction(e -> ouvrirDialogReponse(r));

        Button btnSupprimer = new Button("🗑");
        btnSupprimer.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; " +
                "-fx-background-radius: 10; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 14px;");
        btnSupprimer.setOnAction(e -> supprimerReclamationDirecte(r));

        btns.getChildren().addAll(btnRepondre, btnSupprimer);
        droite.getChildren().addAll(prioLabel, statutLabel, btns);

        card.getChildren().addAll(iconPane, infos, droite);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 20; -fx-padding: 20 25; " +
                "-fx-border-color: #A3B1FF; -fx-border-width: 2; -fx-border-radius: 20; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20 25; " +
                "-fx-border-color: #F1F5F9; -fx-border-width: 1; -fx-border-radius: 20; -fx-cursor: hand;"));

        return card;
    }

    // ===== DIALOG RÉPONSE ADMIN =====
    private void ouvrirDialogReponse(Reclamation r) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Répondre à la réclamation #" + r.getId_reclamation());
        dialog.setHeaderText(r.getSujet());

        VBox content = new VBox(15);
        content.setPrefWidth(500);

        // Description réclamation
        Label descLabel = new Label("Description: " + (r.getDescription() != null ? r.getDescription() : ""));
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #64748B; -fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 8;");

        // Réponse
        Label repLabel = new Label("Votre réponse :");
        repLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");
        TextArea reponseTA = new TextArea(r.getReponse_admin() != null ? r.getReponse_admin() : "");
        reponseTA.setPromptText("Saisissez votre réponse...");
        reponseTA.setPrefHeight(100);
        reponseTA.setWrapText(true);

        // Statut
        Label statLabel = new Label("Nouveau statut :");
        statLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");
        ComboBox<String> statutCB = new ComboBox<>();
        statutCB.setItems(FXCollections.observableArrayList("En attente", "En cours", "Résolue", "Rejetée"));
        statutCB.setValue(r.getStatut() != null ? r.getStatut() : "En attente");
        statutCB.setMaxWidth(Double.MAX_VALUE);

        // Email
        Label emailLabel = new Label("Email client (optionnel) :");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");
        TextField emailTF = new TextField();
        emailTF.setPromptText("email@client.com");

        content.getChildren().addAll(descLabel, repLabel, reponseTA, statLabel, statutCB, emailLabel, emailTF);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Styliser le bouton OK
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("✉ Envoyer la réponse");
        okBtn.setStyle("-fx-background-color: #A3B1FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String reponse = reponseTA.getText().trim();
                String statut  = statutCB.getValue();
                String email   = emailTF.getText().trim();

                if (reponse.isEmpty()) {
                    showAlert("Veuillez saisir une réponse.", Alert.AlertType.WARNING);
                    return;
                }

                try {
                    reclamationCRUD.repondre(r.getId_reclamation(), reponse, statut);
                    r.setReponse_admin(reponse);
                    r.setStatut(statut);

                    if (!email.isEmpty()) {
                        emailService.envoyerReponseReclamation(r, email, "Utilisateur #" + r.getId_user());
                        showAlert("✅ Réponse enregistrée et email envoyé à " + email, Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("✅ Réponse enregistrée avec succès !", Alert.AlertType.INFORMATION);
                    }
                    loadReclamations();
                } catch (Exception ex) {
                    showAlert("Erreur: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void supprimerReclamationDirecte(Reclamation r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText("Supprimer la réclamation #" + r.getId_reclamation() + " ?");
        confirm.setContentText("Cette action est irréversible.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                reclamationCRUD.supprimer(r.getId_reclamation());
                loadReclamations();
            } catch (Exception e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // ===== CHARGEMENT SUGGESTIONS - cartes =====
    @FXML
    public void loadSuggestions() {
        try {
            List<Suggestion> liste = suggestionCRUD.afficherTous();
            afficherCartesSuggestions(liste);
        } catch (Exception e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void afficherCartesSuggestions(List<Suggestion> liste) {
        if (suggestionsContainer == null) return;
        suggestionsContainer.getChildren().clear();
        if (sugCountLabel != null) sugCountLabel.setText(liste.size() + " suggestion(s)");

        if (liste.isEmpty()) {
            VBox vide = new VBox(10);
            vide.setAlignment(Pos.CENTER);
            vide.setStyle("-fx-padding: 60;");
            Label icon = new Label("💡");
            icon.setStyle("-fx-font-size: 48px;");
            Label msg = new Label("Aucune suggestion trouvée");
            msg.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 18px;");
            vide.getChildren().addAll(icon, msg);
            suggestionsContainer.getChildren().add(vide);
            return;
        }

        for (Suggestion s : liste) {
            suggestionsContainer.getChildren().add(buildSuggestionCard(s));
        }
    }

    private HBox buildSuggestionCard(Suggestion s) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20 25; " +
                "-fx-border-color: #F1F5F9; -fx-border-width: 1; -fx-border-radius: 20; -fx-cursor: hand;");

        // Icône catégorie
        StackPane iconPane = new StackPane();
        iconPane.setMinWidth(60); iconPane.setMinHeight(60);
        iconPane.setMaxWidth(60); iconPane.setMaxHeight(60);
        String emoji = switch (s.getCategorie() != null ? s.getCategorie() : "") {
            case "Interface"    -> "🎨";
            case "Technique"    -> "⚙️";
            case "Service"      -> "🎯";
            case "Application"  -> "📱";
            case "Sécurité"     -> "🔒";
            default             -> "💡";
        };
        iconPane.setStyle("-fx-background-color: #F5F3FF; -fx-background-radius: 15;");
        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 22px;");
        iconPane.getChildren().add(iconLabel);

        // Infos
        VBox infos = new VBox(5);
        HBox.setHgrow(infos, Priority.ALWAYS);

        Label sujet = new Label(s.getSujet() != null ? s.getSujet() : "Sans sujet");
        sujet.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: #1E293B;");

        HBox meta = new HBox(15);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label userL = new Label("👤 User #" + s.getId_user());
        userL.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");
        Label catL = new Label(s.getCategorie() != null ? s.getCategorie().toUpperCase() : "AUTRE");
        catL.setStyle("-fx-background-color: #F5F3FF; -fx-text-fill: #7C3AED; " +
                "-fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: 900;");
        String dateStr = s.getDate_suggestion() != null ? "📅 " + s.getDate_suggestion().toString() : "";
        Label dateL = new Label(dateStr);
        dateL.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");

        meta.getChildren().addAll(userL, catL, dateL);
        if (s.getId_reclamation() != null) {
            Label lienL = new Label("🔗 Réc. #" + s.getId_reclamation());
            lienL.setStyle("-fx-text-fill: #A3B1FF; -fx-font-size: 12px; -fx-font-weight: bold;");
            meta.getChildren().add(lienL);
        }
        infos.getChildren().addAll(sujet, meta);

        // Droite : statut + boutons
        VBox droite = new VBox(8);
        droite.setAlignment(Pos.CENTER_RIGHT);

        String sStatutBg = switch (s.getStatut() != null ? s.getStatut() : "") {
            case "Acceptée" -> "#DCFCE7";
            case "Refusée"  -> "#FEE2E2";
            case "En examen"-> "#DBEAFE";
            default          -> "#F5F3FF";
        };
        String sStatutFg = switch (s.getStatut() != null ? s.getStatut() : "") {
            case "Acceptée" -> "#16A34A";
            case "Refusée"  -> "#DC2626";
            case "En examen"-> "#1D4ED8";
            default          -> "#7C3AED";
        };
        Label statutL = new Label(s.getStatut() != null ? s.getStatut() : "Recue");
        statutL.setStyle("-fx-background-color: " + sStatutBg + "; -fx-text-fill: " + sStatutFg + "; " +
                "-fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: 900;");

        HBox btns = new HBox(8);
        btns.setAlignment(Pos.CENTER_RIGHT);

        Button btnRep = new Button("✉ Répondre");
        btnRep.setStyle("-fx-background-color: #F5F3FF; -fx-text-fill: #8B5CF6; " +
                "-fx-background-radius: 10; -fx-padding: 6 14; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px;");
        btnRep.setOnAction(e -> ouvrirDialogSugReponse(s));

        Button btnSup = new Button("🗑");
        btnSup.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; " +
                "-fx-background-radius: 10; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 14px;");
        btnSup.setOnAction(e -> supprimerSuggestionDirecte(s));

        btns.getChildren().addAll(btnRep, btnSup);
        droite.getChildren().addAll(statutL, btns);

        card.getChildren().addAll(iconPane, infos, droite);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 20; -fx-padding: 20 25; " +
                "-fx-border-color: #8B5CF6; -fx-border-width: 2; -fx-border-radius: 20; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20 25; " +
                "-fx-border-color: #F1F5F9; -fx-border-width: 1; -fx-border-radius: 20; -fx-cursor: hand;"));

        return card;
    }

    private void ouvrirDialogSugReponse(Suggestion s) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Répondre à la suggestion #" + s.getId_suggestion());
        dialog.setHeaderText(s.getSujet());

        VBox content = new VBox(15);
        content.setPrefWidth(500);

        Label descLabel = new Label("Description: " + (s.getDescription() != null ? s.getDescription() : ""));
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #64748B; -fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 8;");

        Label repLabel = new Label("Votre réponse :");
        repLabel.setStyle("-fx-font-weight: bold;");
        TextArea reponseTA = new TextArea(s.getReponse_admin() != null ? s.getReponse_admin() : "");
        reponseTA.setPrefHeight(100);
        reponseTA.setWrapText(true);

        Label statLabel = new Label("Nouveau statut :");
        statLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> statutCB = new ComboBox<>();
        statutCB.setItems(FXCollections.observableArrayList("Recue", "En examen", "Acceptée", "Refusée"));
        statutCB.setValue(s.getStatut() != null ? s.getStatut() : "Recue");
        statutCB.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().addAll(descLabel, repLabel, reponseTA, statLabel, statutCB);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("✉ Envoyer");
        okBtn.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String reponse = reponseTA.getText().trim();
                if (reponse.isEmpty()) {
                    showAlert("Veuillez saisir une réponse.", Alert.AlertType.WARNING);
                    return;
                }
                try {
                    suggestionCRUD.repondre(s.getId_suggestion(), reponse, statutCB.getValue());
                    showAlert("✅ Réponse enregistrée !", Alert.AlertType.INFORMATION);
                    loadSuggestions();
                } catch (Exception ex) {
                    showAlert("Erreur: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void supprimerSuggestionDirecte(Suggestion s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText("Supprimer la suggestion #" + s.getId_suggestion() + " ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                suggestionCRUD.supprimer(s.getId_suggestion());
                loadSuggestions();
            } catch (Exception e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // ===== RECHERCHE / FILTRES =====
    @FXML
    private void rechercherReclamation() {
        String terme = recSearchField != null ? recSearchField.getText().trim() : "";
        try {
            List<Reclamation> res = terme.isEmpty() ? reclamationCRUD.afficherTous() : reclamationCRUD.rechercher(terme);
            afficherCartesReclamations(res);
        } catch (Exception e) { showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR); }
    }

    @FXML
    private void filtrerParStatut() {
        String val = filterStatutBox != null ? filterStatutBox.getValue() : "Tous";
        try {
            List<Reclamation> liste = reclamationCRUD.afficherTous();
            if (val != null && !"Tous".equals(val))
                liste = liste.stream().filter(r -> val.equals(r.getStatut())).collect(java.util.stream.Collectors.toList());
            afficherCartesReclamations(liste);
        } catch (Exception e) { showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR); }
    }

    @FXML
    private void filtrerParPriorite() {
        String val = filterPrioriteBox != null ? filterPrioriteBox.getValue() : "Tous";
        try {
            List<Reclamation> liste = reclamationCRUD.afficherTous();
            if (val != null && !"Tous".equals(val))
                liste = liste.stream().filter(r -> val.equals(r.getPriorite())).collect(java.util.stream.Collectors.toList());
            afficherCartesReclamations(liste);
        } catch (Exception e) { showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR); }
    }

    @FXML
    private void rechercherSuggestion() {
        String terme = sugSearchField != null ? sugSearchField.getText().trim() : "";
        try {
            List<Suggestion> res = terme.isEmpty() ? suggestionCRUD.afficherTous() : suggestionCRUD.rechercher(terme);
            afficherCartesSuggestions(res);
        } catch (Exception e) { showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR); }
    }

    @FXML
    private void filtrerSugParStatut() {
        String val = filterSugStatutBox != null ? filterSugStatutBox.getValue() : "Tous";
        try {
            List<Suggestion> liste = suggestionCRUD.afficherTous();
            if (val != null && !"Tous".equals(val))
                liste = liste.stream().filter(s -> val.equals(s.getStatut())).collect(java.util.stream.Collectors.toList());
            afficherCartesSuggestions(liste);
        } catch (Exception e) { showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR); }
    }

    // ===== CARTE RÉCENTE (Dashboard) =====
    private HBox buildRecentCard(Reclamation r) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; -fx-padding: 12 18; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1;");

        Circle dot = new Circle(6);
        switch (r.getPriorite() != null ? r.getPriorite() : "") {
            case "Critique" -> dot.setFill(Color.web("#EF4444"));
            case "Haute"    -> dot.setFill(Color.web("#F59E0B"));
            case "Moyenne"  -> dot.setFill(Color.web("#3B82F6"));
            default         -> dot.setFill(Color.web("#94A3B8"));
        }

        VBox info = new VBox(3);
        Label sujet = new Label("#" + r.getId_reclamation() + " — " + r.getSujet());
        sujet.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label meta = new Label(r.getType_reclamation() + " • " + r.getPriorite() + " • User " + r.getId_user());
        meta.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
        info.getChildren().addAll(sujet, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String bg = "En attente".equals(r.getStatut()) ? "#FEF3C7" : "#DBEAFE";
        String fg = "En attente".equals(r.getStatut()) ? "#92400E" : "#1E40AF";
        Label statutLabel = new Label(r.getStatut());
        statutLabel.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg +
                "; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 12px;");

        card.getChildren().addAll(dot, info, spacer, statutLabel);
        return card;
    }

    // ===== UTILITAIRE =====
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Erreur" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}