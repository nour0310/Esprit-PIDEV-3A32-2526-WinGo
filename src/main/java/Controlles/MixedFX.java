package Controlles;

import Entites.Reservation;
import Entites.Transport;
import Services.ReservationCRUD;
import Services.TransportAPI;
import Services.TransportCRUD;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.sql.SQLException;
import java.util.List;

public class MixedFX {

    @FXML private Button reservationToggleBtn, transportToggleBtn;
    @FXML private FlowPane itemsFlowPane;
    @FXML private GridPane formGrid;
    @FXML private Label formTitle;
    @FXML private ScrollPane listScroll, detailScroll;
    @FXML private TextField searchField;
    @FXML private Label totalCountLabel;

    private boolean showingReservations = true;
    private final ReservationCRUD reservationService = new ReservationCRUD();
    private final TransportCRUD transportService = new TransportCRUD();

    private List<Reservation> reservationList;
    private List<Transport> transportList;
    private Object selectedItem;

    // Fields
    private TextField resUserField, resExpField, resStatutField;
    private DatePicker resDateField;
    private TextField trTypeField, trCapField, trTarifField, trDepartField, trArriveeField;
    private DatePicker trDateField;

    private final String inputStyle = "-fx-background-color: #F8FAFC; -fx-background-radius: 50; -fx-border-color: #E2E8F0; -fx-border-radius: 50; -fx-padding: 8 15;";

    @FXML
    public void initialize() {
        setupFormFields();
        loadReservations(); // Start with reservations
        searchField.textProperty().addListener((obs, old, val) -> filterItems(val));
    }

    private void loadReservations() {
        try {
            reservationList = reservationService.getAll();
            if (showingReservations) populateItems(reservationList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadTransports() {
        try {
            transportList = transportService.getAll();
            if (!showingReservations) populateItems(transportList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void populateItems(List<?> items) {
        itemsFlowPane.getChildren().clear();
        if (totalCountLabel != null) totalCountLabel.setText(String.valueOf(items.size()));

        int count = 0;
        for (Object obj : items) {
            VBox card;
            if (showingReservations && obj instanceof Reservation) {
                Reservation r = (Reservation) obj;
                card = createModernCard(r.getUser(), "Statut: " + r.getStatut(), r.getDate().toString(), null);
            } else if (!showingReservations && obj instanceof Transport) {
                Transport t = (Transport) obj;
                card = createModernCard(t.getType(), t.getDepart() + " ➔ " + t.getArrivee(), t.getTarif() + " TND", null);

                // --- 📝 LOOK NOTE FLOTTANTE ---
                Label apiNote = new Label("⏳ GPS...");
                apiNote.setWrapText(true);
                apiNote.setMaxWidth(180);
                apiNote.setStyle("-fx-background-color: #FFFBEB; -fx-text-fill: #92400E; -fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #F59E0B; -fx-border-width: 0 0 0 4; -fx-font-size: 11px;");

                VBox textArea = (VBox) card.lookup("#textArea");
                if (textArea != null) textArea.getChildren().add(apiNote);

                // --- GESTION DU DÉLAI API (Anti-Spam) ---
                int delay = count * 1200; // 1.2s entre chaque carte
                new Thread(() -> {
                    try {
                        Thread.sleep(delay);
                        String infos = TransportAPI.getInfosTrajet(t.getDepart(), t.getArrivee());
                        Platform.runLater(() -> {
                            apiNote.setText(infos);
                            if (!infos.contains("❌")) {
                                apiNote.setStyle("-fx-background-color: #F0FDF4; -fx-text-fill: #166534; -fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #22C55E; -fx-border-width: 0 0 0 4; -fx-font-size: 11px;");
                            }
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }).start();
                count++;
            } else continue;

            card.setOnMouseClicked(e -> {
                selectedItem = obj;
                fillFormFields(obj);
                // Highlight effet
                itemsFlowPane.getChildren().forEach(n -> n.setStyle(((VBox)n).getStyle().replace("-fx-border-color: #A3B1FF;", "-fx-border-color: #EEF2FF;")));
                card.setStyle(card.getStyle() + "-fx-border-color: #A3B1FF;");
            });

            itemsFlowPane.getChildren().add(card);
        }
    }

    private VBox createModernCard(String title, String subtitle, String extraInfo, String imageUrl) {
        VBox card = new VBox();
        card.setPrefSize(210, 340); // Hauteur augmentée pour la note
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 25; -fx-border-color: #EEF2FF; -fx-border-width: 2;");

        DropShadow ds = new DropShadow(20, Color.web("#00000010"));
        ds.setOffsetY(8);
        card.setEffect(ds);

        Pane img = new Pane();
        img.setPrefHeight(110);
        String url = (imageUrl == null) ? "/assets/testt.jpg" : imageUrl;
        img.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 23 23 0 0; -fx-background-image: url('" + url + "'); -fx-background-size: cover;");

        VBox textArea = new VBox(8);
        textArea.setId("textArea");
        textArea.setPadding(new Insets(12));

        Label tL = new Label(title); tL.setStyle("-fx-font-weight: 900; -fx-font-size: 15px;");
        Label sL = new Label(subtitle); sL.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
        Label eL = new Label(extraInfo); eL.setStyle("-fx-text-fill: #A3B1FF; -fx-font-weight: bold;");

        textArea.getChildren().addAll(tL, sL, eL);
        card.getChildren().addAll(img, textArea);

        return card;
    }

    @FXML private void handleAdd() {
        try {
            if (showingReservations) {
                if (!validateReservation()) return;
                Reservation r = new Reservation();
                r.setUser(resUserField.getText());
                r.setExp(resExpField.getText());
                r.setStatut(resStatutField.getText());
                r.setDate(java.sql.Timestamp.valueOf(resDateField.getValue().atStartOfDay()));
                reservationService.ajouter(r);
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
            }
            showAlert("Succès", "Ajout réussi !", Alert.AlertType.INFORMATION);
            reloadCurrent();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleDelete() {
        if (selectedItem == null) return;
        try {
            if (showingReservations) reservationService.supprimer(((Reservation) selectedItem).getId());
            else transportService.supprimer(((Transport) selectedItem).getId());
            selectedItem = null;
            reloadCurrent();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- Helpers existants ---
    private void reloadCurrent() { if (showingReservations) loadReservations(); else loadTransports(); }

    @FXML private void showReservations() { showingReservations = true; setupFormFields(); loadReservations(); }
    @FXML private void showTransports() { showingReservations = false; setupFormFields(); loadTransports(); }

    private void setupFormFields() {
        formGrid.getChildren().clear();
        if (showingReservations) {
            formTitle.setText("Réservation");
            resUserField = new TextField(); resUserField.setStyle(inputStyle);
            resExpField = new TextField(); resExpField.setStyle(inputStyle);
            resStatutField = new TextField(); resStatutField.setStyle(inputStyle);
            resDateField = new DatePicker(); resDateField.setStyle(inputStyle);
            formGrid.add(new Label("User:"), 0, 0); formGrid.add(resUserField, 1, 0);
            formGrid.add(new Label("Statut:"), 0, 1); formGrid.add(resStatutField, 1, 1);
            formGrid.add(new Label("Date:"), 0, 2); formGrid.add(resDateField, 1, 2);
        } else {
            formTitle.setText("Transport");
            trTypeField = new TextField(); trTypeField.setStyle(inputStyle);
            trDepartField = new TextField(); trDepartField.setStyle(inputStyle);
            trArriveeField = new TextField(); trArriveeField.setStyle(inputStyle);
            trTarifField = new TextField(); trTarifField.setStyle(inputStyle);
            trDateField = new DatePicker(); trDateField.setStyle(inputStyle);
            formGrid.add(new Label("Départ:"), 0, 0); formGrid.add(trDepartField, 1, 0);
            formGrid.add(new Label("Arrivée:"), 0, 1); formGrid.add(trArriveeField, 1, 1);
            formGrid.add(new Label("Tarif:"), 0, 2); formGrid.add(trTarifField, 1, 2);
            formGrid.add(new Label("Date:"), 0, 3); formGrid.add(trDateField, 1, 3);
        }
    }

    private void fillFormFields(Object item) {
        if (item instanceof Reservation) {
            Reservation r = (Reservation) item;
            resUserField.setText(r.getUser());
            resStatutField.setText(r.getStatut());
        } else if (item instanceof Transport) {
            Transport t = (Transport) item;
            trDepartField.setText(t.getDepart());
            trArriveeField.setText(t.getArrivee());
            trTarifField.setText(String.valueOf(t.getTarif()));
        }
    }

    private void filterItems(String query) {
        if (showingReservations) {
            List<Reservation> filtered = reservationList.stream().filter(r -> r.getUser().toLowerCase().contains(query.toLowerCase())).toList();
            populateItems(filtered);
        } else {
            List<Transport> filtered = transportList.stream().filter(t -> t.getDepart().toLowerCase().contains(query.toLowerCase())).toList();
            populateItems(filtered);
        }
    }

    private boolean validateReservation() { return !resUserField.getText().isEmpty() && resDateField.getValue() != null; }
    private boolean validateTransport() { return !trDepartField.getText().isEmpty() && !trArriveeField.getText().isEmpty(); }
    private void showAlert(String t, String m, Alert.AlertType at) { Alert a = new Alert(at); a.setTitle(t); a.setContentText(m); a.show(); }
}