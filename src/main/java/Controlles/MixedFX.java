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

public class MixedFX {

    @FXML private Button reservationToggleBtn, transportToggleBtn;
    @FXML private FlowPane itemsFlowPane;
    @FXML private GridPane formGrid, detailGrid;
    @FXML private Label formTitle;
    @FXML private Button addBtn, editBtn, deleteBtn, clearBtn;
    @FXML private ScrollPane listScroll, detailScroll;
    @FXML private TextField searchField;
    @FXML private Label totalCountLabel;

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

    // Common style for inputs to match the modern, pill-shaped aesthetic
    private final String inputStyle = "-fx-background-color: #F8FAFC; -fx-background-radius: 50; -fx-border-color: #E2E8F0; -fx-border-radius: 50; -fx-padding: 8 15; -fx-text-fill: #1E293B;";


    @FXML
    public void initialize() {
        setupFormFields();
        loadReservations();
        loadTransports(); // start with reservations

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

    /** Setup persistent Reservation and Transport fields with modern styling */
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

        setupFormFields();
        loadReservations();
        selectedItem = null;
        searchField.clear();
    }

    @FXML
    private void showTransports() {
        showingReservations = false;
        transportToggleBtn.setStyle("-fx-background-color: #A3B1FF; -fx-text-fill: white; -fx-background-radius: 20;");
        reservationToggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8;");

        setupFormFields();
        loadTransports();
        selectedItem = null;
        searchField.clear();
    }

    private void loadReservations() {
        try {
            reservationList = reservationService.getAll();
            populateItems(reservationList);
            if (showingReservations && totalCountLabel != null) {
                totalCountLabel.setText(String.valueOf(reservationList.size()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTransports() {
        try {
            transportList = transportService.getAll();
            populateItems(transportList);
            if (!showingReservations && totalCountLabel != null) {
                totalCountLabel.setText(String.valueOf(transportList.size()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** * Populates the FlowPane with Modern Cards 
     */
    /** * Populates the FlowPane with Modern Cards */
    private void populateItems(List<?> items) {
        itemsFlowPane.getChildren().clear();

        for (Object obj : items) {
            VBox card = null;

            if (showingReservations && obj instanceof Reservation) {
                Reservation r = (Reservation) obj;
                String dateStr = (r.getDate() != null) ? r.getDate().toString().split(" ")[0] : "Pas de date";
                card = createModernCard(r.getUser(), "Statut: " + r.getStatut(), dateStr, null);

            } else if (!showingReservations && obj instanceof Transport) {
                Transport t = (Transport) obj;
                String route = t.getDepart() + " ➔ " + t.getArrivee();
                String price = t.getTarif() + " TND";
                card = createModernCard(t.getType(), route, price, null);

                // --- 🌟 NOUVEAU : LA BULLE CONSTANTE INTÉGRÉE ---
                Label apiBubble = new Label("⏳ Chargement en cours...");
                apiBubble.setWrapText(true); // Permet au texte de passer à la ligne
                // Style de la bulle : fond bleu très clair, texte bleu foncé, bords arrondis
                apiBubble.setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #4338CA; -fx-padding: 8; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");

                // On récupère la zone de texte de la carte (c'est le 2ème élément, donc index 1)
                VBox textArea = (VBox) card.getChildren().get(1);
                textArea.getChildren().add(apiBubble); // On ajoute la bulle tout en bas de la carte

                // Thread en arrière-plan pour ne pas figer l'application
                new Thread(() -> {
                    String infos = Services.TransportAPI.getInfosTrajet(t.getDepart(), t.getArrivee());
                    // Platform.runLater met à jour l'interface JavaFX une fois les données reçues
                    javafx.application.Platform.runLater(() -> {
                        apiBubble.setText(infos);
                    });
                }).start();
                // --------------------------------------------------
            }
            if (card != null) {
                card.setOnMouseClicked(e -> {
                    selectedItem = obj;
                    fillFormFields(obj);
                    populateForm();
                });
                itemsFlowPane.getChildren().add(card);
            }
        }
    }

    /** * Generates the floating capsule cards 
     */
    private VBox createModernCard(String title, String subtitle, String extraInfo, String imageUrl) {
        VBox card = new VBox();
        card.setPrefSize(200, 320);
        card.setMaxSize(200, 320);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 25; -fx-border-color: #EEF2FF; -fx-border-radius: 25; -fx-border-width: 2;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#0000000C"));
        shadow.setOffsetY(8);
        shadow.setRadius(20);
        card.setEffect(shadow);

        Pane imagePane = new Pane();
        imagePane.setPrefHeight(130);
        imagePane.setMinHeight(130);
        String bgImage = (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : "/assets/testt.jpg";
        imagePane.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 23 23 0 0; -fx-background-size: cover; -fx-background-position: center; -fx-background-image: url('" + bgImage + "');");

        VBox textArea = new VBox(5);
        textArea.setPadding(new Insets(15, 15, 15, 15));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #1E293B; -fx-font-weight: 900; -fx-font-size: 16px;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-font-weight: bold;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label extraLabel = new Label(extraInfo);
        extraLabel.setStyle("-fx-text-fill: #A3B1FF; -fx-font-weight: 900; -fx-font-size: 14px;");

        textArea.getChildren().addAll(titleLabel, subtitleLabel, spacer, extraLabel);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        card.getChildren().addAll(imagePane, textArea);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 25; -fx-border-color: #A3B1FF; -fx-border-radius: 25; -fx-border-width: 2; -fx-cursor: hand;");
            shadow.setOffsetY(12);
            shadow.setColor(Color.web("#00000015"));
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 25; -fx-border-color: #EEF2FF; -fx-border-radius: 25; -fx-border-width: 2;");
            shadow.setOffsetY(8);
            shadow.setColor(Color.web("#0000000C"));
        });

        return card;
    }

    /** * Fills TextFields when a card is clicked 
     */
    private void fillFormFields(Object item) {
        if (showingReservations && item instanceof Reservation) {
            Reservation r = (Reservation) item;
            resUserField.setText(r.getUser());
            resExpField.setText(r.getExp());
            resStatutField.setText(r.getStatut());
            if (r.getDate() != null) {
                resDateField.setValue(r.getDate().toLocalDateTime().toLocalDate());
            }
        } else if (!showingReservations && item instanceof Transport) {
            Transport t = (Transport) item;
            trTypeField.setText(t.getType());
            trCapField.setText(t.getCapacite());
            trTarifField.setText(String.valueOf(t.getTarif()));
            trDepartField.setText(t.getDepart());
            trArriveeField.setText(t.getArrivee());
            if (t.getDateDepart() != null) {
                trDateField.setValue(t.getDateDepart().toLocalDate());
            }
        }
    }

    /** Populate formGrid based on current entity */
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
                r.setUser(resUserField.getText());
                r.setExp(resExpField.getText());
                r.setStatut(resStatutField.getText());
                r.setDate(java.sql.Timestamp.valueOf(resDateField.getValue().atStartOfDay()));
                reservationService.ajouter(r);
                showAlert("Succès", "Réservation ajoutée avec succès !", Alert.AlertType.INFORMATION);
            } else {
                if (!validateTransport()) return;
                Transport t = new Transport();
                t.setType(trTypeField.getText());
                t.setCapacite(trCapField.getText());
                t.setTarif(Float.parseFloat(trTarifField.getText()));
                t.setDepart(trDepartField.getText());
                t.setArrivee(trArriveeField.getText());
                t.setDateDepart(trDateField.getValue().atStartOfDay());
                transportService.ajouter(t);
                showAlert("Succès", "Transport ajouté avec succès !", Alert.AlertType.INFORMATION);
                String villeDepart = trDepartField.getText();
                String villeArrivee = trArriveeField.getText();
                String infosApi = TransportAPI.getInfosTrajet(villeDepart, villeArrivee);

                System.out.println("🌍 Infos Trajet :\n" + infosApi);
                showAlert("Succès", "Transport ajouté avec succès !\n\n" + infosApi, Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            showAlert("Erreur Base de Données", "Erreur lors de l'ajout !", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
        reloadCurrent();
    }

    // Validation for Reservation
    private boolean validateReservation() {
        if (resUserField.getText().isEmpty() ||
                resExpField.getText().isEmpty() ||
                resStatutField.getText().isEmpty() ||
                resDateField.getValue() == null) {
            showAlert("Erreur", "Tous les champs de Réservation doivent être remplis !", Alert.AlertType.ERROR);
            return false;
        }
        if (resUserField.getText().length() < 3) {
            showAlert("Erreur", "Le User doit contenir au moins 3 caractères !", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    // Validation for Transport
    private boolean validateTransport() {
        if (trTypeField.getText().isEmpty() ||
                trCapField.getText().isEmpty() ||
                trTarifField.getText().isEmpty() ||
                trDepartField.getText().isEmpty() ||
                trArriveeField.getText().isEmpty() ||
                trDateField.getValue() == null) {
            showAlert("Erreur", "Tous les champs de Transport doivent être remplis !", Alert.AlertType.ERROR);
            return false;
        }
        try {
            Float.parseFloat(trTarifField.getText());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le Tarif doit être un nombre valide !", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleEdit() {
        if (selectedItem == null) {
            showAlert("Erreur", "Veuillez sélectionner un élément !", Alert.AlertType.ERROR);
            return;
        }
        try {
            if (showingReservations) {
                if (!validateReservation()) return;
                Reservation r = (Reservation) selectedItem;
                r.setUser(resUserField.getText());
                r.setExp(resExpField.getText());
                r.setStatut(resStatutField.getText());
                r.setDate(java.sql.Timestamp.valueOf(resDateField.getValue().atStartOfDay()));
                reservationService.modifier(r);
            } else {
                if (!validateTransport()) return;
                Transport t = (Transport) selectedItem;
                t.setType(trTypeField.getText());
                t.setCapacite(trCapField.getText());
                t.setTarif(Float.parseFloat(trTarifField.getText()));
                t.setDepart(trDepartField.getText());
                t.setArrivee(trArriveeField.getText());
                t.setDateDepart(trDateField.getValue().atStartOfDay());
                transportService.modifier(t);
            }
            showAlert("Succès", "Modification effectuée !", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur Base de Données", "Erreur lors de la modification !", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
        reloadCurrent();
    }

    @FXML
    private void handleDelete() {
        if (selectedItem == null) return;
        try {
            if (showingReservations) {
                reservationService.supprimer(((Reservation) selectedItem).getId());
            } else {
                transportService.supprimer(((Transport) selectedItem).getId());
            }
            reloadCurrent();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clearForm() {
        selectedItem = null;
        if (showingReservations) {
            resUserField.clear();
            resExpField.clear();
            resStatutField.clear();
            resDateField.setValue(null);
        } else {
            trTypeField.clear();
            trCapField.clear();
            trTarifField.clear();
            trDepartField.clear();
            trArriveeField.clear();
            trDateField.setValue(null);
        }
    }

    private void reloadCurrent() {
        if (showingReservations) loadReservations();
        else loadTransports();
        populateForm();
    }

    private void showDetails(Object item) {
        selectedItem = item;
        listScroll.setVisible(false);
        listScroll.setManaged(false);
        detailScroll.setVisible(true);
        detailScroll.setManaged(true);
        detailGrid.getChildren().clear();

        if (showingReservations) {
            Reservation r = (Reservation) item;
            detailGrid.addRow(0, createStyledLabel("User:"), new Label(r.getUser()));
            detailGrid.addRow(1, createStyledLabel("Exp:"), new Label(r.getExp()));
            detailGrid.addRow(2, createStyledLabel("Statut:"), new Label(r.getStatut()));
            detailGrid.addRow(3, createStyledLabel("Date:"), new Label(r.getDate().toString()));
        } else {
            Transport t = (Transport) item;
            detailGrid.addRow(0, createStyledLabel("Type:"), new Label(t.getType()));
            detailGrid.addRow(1, createStyledLabel("Capacité:"), new Label(t.getCapacite()));
            detailGrid.addRow(2, createStyledLabel("Tarif:"), new Label(String.valueOf(t.getTarif())));
            detailGrid.addRow(3, createStyledLabel("Départ:"), new Label(t.getDepart()));
            detailGrid.addRow(4, createStyledLabel("Arrivée:"), new Label(t.getArrivee()));
            detailGrid.addRow(5, createStyledLabel("Date départ:"), new Label(t.getDateDepart().toString()));
        }
    }

    @FXML
    private void backToList() {
        detailScroll.setVisible(false);
        detailScroll.setManaged(false);
        listScroll.setVisible(true);
        listScroll.setManaged(true);
    }
}