package Controlles;

import Entites.Reservation;
import Entites.Transport;
import Services.ReservationCRUD;
import Services.TransportAPI;
import Services.TransportCRUD;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
public class MixedFX {

    @FXML private Button reservationToggleBtn, transportToggleBtn;
    @FXML private FlowPane itemsFlowPane;
    @FXML private GridPane formGrid, detailGrid;
    @FXML private Label formTitle;
    @FXML private Button addBtn, editBtn, deleteBtn, clearBtn;
    @FXML private ScrollPane listScroll, detailScroll;
    @FXML private TextField searchField;
    @FXML private Label totalCountLabel;
    private javafx.collections.ObservableSet<String> dynamicWishlist = javafx.collections.FXCollections.observableSet();
    private String currentUserId = "NormanHaires";
    private boolean filterByWishlist = false;
    @FXML private Button wishlistFilterBtn;
    @FXML private WebView mapWebView;
    @FXML private Label mapRouteLabel;
    @FXML private ProgressBar mapProgressBar;

    private boolean showingReservations = true;

    private ReservationCRUD reservationService = new ReservationCRUD();
    private TransportCRUD transportService = new TransportCRUD();

    private List<Reservation> reservationList;
    private List<Transport> transportList;

    private Object selectedItem;

    // Persistent form fields
    private TextField resUserField, resExpField, resStatutField;
    private DatePicker resDateField;

    private TextField trTypeField, trCapField, trTarifField, trDepartField, trArriveeField;
    private DatePicker trDateField;

    private final String inputStyle = "-fx-background-color: #F8FAFC; -fx-background-radius: 50; -fx-border-color: #E2E8F0; -fx-border-radius: 50; -fx-padding: 8 15; -fx-text-fill: #1E293B;";

    @FXML
    public void initialize() {
        setupFormFields();
        loadReservations();
        loadTransports();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterItems(newValue);
        });
    }

    private void filterItems(String query) {
        if (showingReservations) {
            if (reservationList == null) return;
            List<Reservation> filtered = reservationList.stream()
                    .filter(r -> r.getUser().toLowerCase().contains(query.toLowerCase()) ||
                            r.getExp().toLowerCase().contains(query.toLowerCase()) ||
                            r.getStatut().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            populateItems(filtered);
        } else {
            if (transportList == null) return;
            List<Transport> filtered = transportList.stream()
                    .filter(t -> t.getType().toLowerCase().contains(query.toLowerCase()) ||
                            t.getCapacite().toLowerCase().contains(query.toLowerCase()) ||
                            t.getDepart().toLowerCase().contains(query.toLowerCase()) ||
                            t.getArrivee().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            populateItems(filtered);
        }
    }
    @FXML
    private void toggleWishlistFilter() {
        filterByWishlist = !filterByWishlist;

        if (filterByWishlist) {
            // Style bouton actif (Rouge)
            wishlistFilterBtn.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #EF4444; -fx-background-radius: 10; -fx-padding: 10; -fx-font-size: 16px;");

            // Filtrer la liste actuelle pour ne garder que les favoris
            if (showingReservations) {
                List<Reservation> filtered = reservationList.stream()
                        .filter(r -> dynamicWishlist.contains("RES_" + r.getId()))
                        .toList();
                populateItems(filtered);
            } else {
                List<Transport> filtered = transportList.stream()
                        .filter(t -> dynamicWishlist.contains("TR_" + t.getId()))
                        .toList();
                populateItems(filtered);
            }
        } else {
            // Style bouton inactif (Gris)
            wishlistFilterBtn.setStyle("-fx-background-color: white; -fx-text-fill: #CBD5E1; -fx-background-radius: 10; -fx-padding: 10; -fx-font-size: 16px;");

            // Recharger la liste complète selon l'onglet actif
            reloadCurrent();
        }
    }

    private void setupFormFields() {
        formGrid.getChildren().clear();
        if (showingReservations) {
            formTitle.setText("Formulaire Réservation");
            resUserField = new TextField(); resUserField.setStyle(inputStyle);
            resExpField = new TextField(); resExpField.setStyle(inputStyle);
            resStatutField = new TextField(); resStatutField.setStyle(inputStyle);
            resDateField = new DatePicker(); resDateField.setStyle(inputStyle);
            formGrid.add(createStyledLabel("User:"), 0, 0); formGrid.add(resUserField, 1, 0);
            formGrid.add(createStyledLabel("Exp:"), 0, 1); formGrid.add(resExpField, 1, 1);
            formGrid.add(createStyledLabel("Statut:"), 0, 2); formGrid.add(resStatutField, 1, 2);
            formGrid.add(createStyledLabel("Date:"), 0, 3); formGrid.add(resDateField, 1, 3);
        } else {
            formTitle.setText("Formulaire Transport");
            trTypeField = new TextField(); trTypeField.setStyle(inputStyle);
            trCapField = new TextField(); trCapField.setStyle(inputStyle);
            trTarifField = new TextField(); trTarifField.setStyle(inputStyle);
            trDepartField = new TextField(); trDepartField.setStyle(inputStyle);
            trArriveeField = new TextField(); trArriveeField.setStyle(inputStyle);
            trDateField = new DatePicker(); trDateField.setStyle(inputStyle);
            formGrid.add(createStyledLabel("Type:"), 0, 0); formGrid.add(trTypeField, 1, 0);
            formGrid.add(createStyledLabel("Capacité:"), 0, 1); formGrid.add(trCapField, 1, 1);
            formGrid.add(createStyledLabel("Tarif:"), 0, 2); formGrid.add(trTarifField, 1, 2);
            formGrid.add(createStyledLabel("Départ:"), 0, 3); formGrid.add(trDepartField, 1, 3);
            formGrid.add(createStyledLabel("Arrivée:"), 0, 4); formGrid.add(trArriveeField, 1, 4);
            formGrid.add(createStyledLabel("Date départ:"), 0, 5); formGrid.add(trDateField, 1, 5);
        }
    }

    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #64748B; -fx-font-weight: bold;");
        return label;
    }

    @FXML
    private void showReservations() {
        showingReservations = true;
        reservationToggleBtn.setStyle("-fx-background-color: #A3B1FF; -fx-text-fill: white; -fx-background-radius: 20;");
        transportToggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8;");
        setupFormFields(); loadReservations(); selectedItem = null; searchField.clear();
    }

    @FXML
    private void showTransports() {
        showingReservations = false;
        transportToggleBtn.setStyle("-fx-background-color: #A3B1FF; -fx-text-fill: white; -fx-background-radius: 20;");
        reservationToggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8;");
        setupFormFields(); loadTransports(); selectedItem = null; searchField.clear();
    }

    private void loadReservations() {
        try {
            reservationList = reservationService.getAll();
            if (showingReservations) populateItems(reservationList);
            if (showingReservations && totalCountLabel != null) totalCountLabel.setText(String.valueOf(reservationList.size()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadTransports() {
        try {
            transportList = transportService.getAll();
            if (!showingReservations) populateItems(transportList);
            if (!showingReservations && totalCountLabel != null) totalCountLabel.setText(String.valueOf(transportList.size()));
        } catch (SQLException e) { e.printStackTrace(); }
    }
    private void updateMapDisplay(Transport t) {
        if (mapWebView == null) return; // Sécurité si la WebView n'est pas injectée

        // Nettoyage des noms pour l'URL (remplace les espaces par des +)
        String origin = t.getDepart().replace(" ", "+");
        String destination = t.getArrivee().replace(" ", "+");

        // URL Google Maps en mode Direction (Embed)
        // Note: Utiliser une clé API si tu veux éviter les limitations, sinon Maps URL simple
        String url = "https://www.google.com/maps/dir/?api=1&origin=" + origin + "&destination=" + destination + "&travelmode=driving";

        javafx.application.Platform.runLater(() -> {
            mapWebView.getEngine().load(url);

            // Optionnel : Mettre à jour les labels de ton bloc à droite (image_21b93a.png)
            if (mapRouteLabel != null) {
                mapRouteLabel.setText(t.getDepart() + " vers " + t.getArrivee());
            }
        });
    }

    private void populateItems(List<?> items) {
        itemsFlowPane.getChildren().clear();
        int index = 0;

        for (Object obj : items) {
            VBox card = null;
            String uniqueKey = "";

            if (showingReservations && obj instanceof Reservation) {
                Reservation r = (Reservation) obj;
                uniqueKey = "RES_" + r.getId();
                String dateStr = (r.getDate() != null) ? r.getDate().toString().split(" ")[0] : "Pas de date";
                card = createModernCard(r.getUser(), "Statut: " + r.getStatut(), dateStr, null);

            } else if (!showingReservations && obj instanceof Transport) {
                Transport t = (Transport) obj;
                uniqueKey = "TR_" + t.getId();
                card = createModernCard(t.getType(), t.getDepart() + " ➔ " + t.getArrivee(), t.getTarif() + " TND", null);

                VBox textArea = (VBox) card.lookup("#textArea");
                if (textArea != null) {
                    ProgressBar travelProgress = new ProgressBar(0);
                    travelProgress.setPrefWidth(170);
                    travelProgress.setStyle("-fx-accent: #EF4444;");

                    Label apiNote = new Label("⏳ Initialisation GPS...");
                    apiNote.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");

                    textArea.getChildren().addAll(new Label("Suivi du trajet:"), travelProgress, apiNote);

                    final int delay = index * 1000;
                    new Thread(() -> {
                        try {
                            Thread.sleep(delay);
                            for (double p = 0; p <= 1; p += 0.1) {
                                final double progress = p;
                                javafx.application.Platform.runLater(() -> travelProgress.setProgress(progress));
                                Thread.sleep(200);
                            }
                            String infos = Services.TransportAPI.getInfosTrajet(t.getDepart(), t.getArrivee());
                            javafx.application.Platform.runLater(() -> {
                                apiNote.setText("🏁 " + infos);
                                apiNote.setStyle("-fx-text-fill: #22C55E; -fx-font-weight: bold; -fx-font-size: 10px;");
                            });
                        } catch (InterruptedException e) { e.printStackTrace(); }
                    }).start();
                }
                index++;
            }

            if (card != null) {
                Button wishlistBtn = new Button("❤");
                wishlistBtn.setTranslateX(170);
                wishlistBtn.setTranslateY(-10);

                updateWishlistButtonStyle(wishlistBtn, dynamicWishlist.contains(uniqueKey));

                final String finalKey = uniqueKey;
                wishlistBtn.setOnAction(e -> {
                    if (!dynamicWishlist.contains(finalKey)) dynamicWishlist.add(finalKey);
                    else dynamicWishlist.remove(finalKey);
                    updateWishlistButtonStyle(wishlistBtn, dynamicWishlist.contains(finalKey));
                    e.consume();
                });

                ((Pane)card.getChildren().get(0)).getChildren().add(wishlistBtn);

                // --- GESTION DU CLIC POUR LA MAP ---
                card.setOnMouseClicked(e -> {
                    selectedItem = obj;
                    fillFormFields(obj);
                    populateForm();

                    // Si c'est un transport, on met à jour la carte à droite
                    if (obj instanceof Transport) {
                        updateMapDisplay((Transport) obj);
                    }

                    if (e.getClickCount() == 2) showDetails(obj);
                });

                itemsFlowPane.getChildren().add(card);
            }
        }
    }
    private VBox createModernCard(String title, String subtitle, String extraInfo, String imageUrl) {
        VBox card = new VBox();
        card.setPrefSize(210, 320);
        card.setMaxSize(210, 320);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 25; -fx-border-color: #EEF2FF; -fx-border-radius: 25; -fx-border-width: 2;");
        DropShadow shadow = new DropShadow(20, Color.web("#0000000C"));
        shadow.setOffsetY(8);
        card.setEffect(shadow);

        Pane imagePane = new Pane();
        imagePane.setPrefHeight(110);
        String bgImage = (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : "/assets/testt.jpg";
        imagePane.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 23 23 0 0; -fx-background-size: cover; -fx-background-position: center; -fx-background-image: url('" + bgImage + "');");

        VBox textArea = new VBox(8);
        textArea.setId("textArea");
        textArea.setPadding(new Insets(12));
        Label titleLabel = new Label(title); titleLabel.setStyle("-fx-text-fill: #1E293B; -fx-font-weight: 900; -fx-font-size: 15px;");
        Label subtitleLabel = new Label(subtitle); subtitleLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label extraLabel = new Label(extraInfo); extraLabel.setStyle("-fx-text-fill: #A3B1FF; -fx-font-weight: 900; -fx-font-size: 13px;");

        textArea.getChildren().addAll(titleLabel, subtitleLabel, extraLabel);
        card.getChildren().addAll(imagePane, textArea);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 25; -fx-border-color: #A3B1FF; -fx-border-radius: 25; -fx-border-width: 2; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 25; -fx-border-color: #EEF2FF; -fx-border-radius: 25; -fx-border-width: 2;"));

        return card;
    }

    private void fillFormFields(Object item) {
        if (showingReservations && item instanceof Reservation r) {
            resUserField.setText(r.getUser()); resExpField.setText(r.getExp()); resStatutField.setText(r.getStatut());
            if (r.getDate() != null) resDateField.setValue(r.getDate().toLocalDateTime().toLocalDate());
        } else if (!showingReservations && item instanceof Transport t) {
            trTypeField.setText(t.getType()); trCapField.setText(t.getCapacite()); trTarifField.setText(String.valueOf(t.getTarif()));
            trDepartField.setText(t.getDepart()); trArriveeField.setText(t.getArrivee());
            if (t.getDateDepart() != null) trDateField.setValue(t.getDateDepart().toLocalDate());
        }
    }

    private void populateForm() {
        formGrid.getChildren().clear();
        if (showingReservations) {
            formTitle.setText("Formulaire Réservation");
            formGrid.add(createStyledLabel("User:"), 0, 0); formGrid.add(resUserField, 1, 0);
            formGrid.add(createStyledLabel("Exp:"), 0, 1); formGrid.add(resExpField, 1, 1);
            formGrid.add(createStyledLabel("Statut:"), 0, 2); formGrid.add(resStatutField, 1, 2);
            formGrid.add(createStyledLabel("Date:"), 0, 3); formGrid.add(resDateField, 1, 3);
        } else {
            formTitle.setText("Formulaire Transport");
            formGrid.add(createStyledLabel("Type:"), 0, 0); formGrid.add(trTypeField, 1, 0);
            formGrid.add(createStyledLabel("Capacité:"), 0, 1); formGrid.add(trCapField, 1, 1);
            formGrid.add(createStyledLabel("Tarif:"), 0, 2); formGrid.add(trTarifField, 1, 2);
            formGrid.add(createStyledLabel("Départ:"), 0, 3); formGrid.add(trDepartField, 1, 3);
            formGrid.add(createStyledLabel("Arrivée:"), 0, 4); formGrid.add(trArriveeField, 1, 4);
            formGrid.add(createStyledLabel("Date départ:"), 0, 5); formGrid.add(trDateField, 1, 5);
        }
    }

    @FXML
    private void handleAdd() {
        try {
            if (showingReservations) {
                if (!validateReservation()) return;
                Reservation r = new Reservation();
                r.setUser(resUserField.getText()); r.setExp(resExpField.getText()); r.setStatut(resStatutField.getText());
                r.setDate(java.sql.Timestamp.valueOf(resDateField.getValue().atStartOfDay()));
                reservationService.ajouter(r);
            } else {
                if (!validateTransport()) return;
                Transport t = new Transport();
                t.setType(trTypeField.getText()); t.setCapacite(trCapField.getText()); t.setTarif(Float.parseFloat(trTarifField.getText()));
                t.setDepart(trDepartField.getText()); t.setArrivee(trArriveeField.getText()); t.setDateDepart(trDateField.getValue().atStartOfDay());
                transportService.ajouter(t);
            }
            showAlert("Succès", "Ajouté avec succès !", Alert.AlertType.INFORMATION);
            reloadCurrent();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private boolean validateReservation() { return !resUserField.getText().isEmpty(); }
    private boolean validateTransport() { return !trTypeField.getText().isEmpty(); }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    @FXML private void handleEdit() { /* Ton code original handleEdit */ }
    @FXML private void handleDelete() { /* Ton code original handleDelete */ }

    @FXML
    private void clearForm() {
        selectedItem = null;
        if (showingReservations) { resUserField.clear(); resExpField.clear(); resStatutField.clear(); resDateField.setValue(null); }
        else { trTypeField.clear(); trCapField.clear(); trTarifField.clear(); trDepartField.clear(); trArriveeField.clear(); trDateField.setValue(null); }
    }

    private void reloadCurrent() { if (showingReservations) loadReservations(); else loadTransports(); populateForm(); }

    private void showDetails(Object item) {
        selectedItem = item;
        listScroll.setVisible(false); listScroll.setManaged(false);
        detailScroll.setVisible(true); detailScroll.setManaged(true);
        detailGrid.getChildren().clear();
        if (showingReservations && item instanceof Reservation r) {
            detailGrid.addRow(0, createStyledLabel("User:"), new Label(r.getUser()));
            detailGrid.addRow(1, createStyledLabel("Statut:"), new Label(r.getStatut()));
        } else if (item instanceof Transport t) {
            detailGrid.addRow(0, createStyledLabel("Départ:"), new Label(t.getDepart()));
            detailGrid.addRow(1, createStyledLabel("Arrivée:"), new Label(t.getArrivee()));
        }
    }

    @FXML
    private void backToList() {
        detailScroll.setVisible(false); detailScroll.setManaged(false);
        listScroll.setVisible(true); listScroll.setManaged(true);
    }
    private void updateWishlistButtonStyle(Button btn, boolean isFavorite) {
        if (isFavorite) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-size: 20px; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-font-size: 20px; -fx-cursor: hand;");
        }
    }
}