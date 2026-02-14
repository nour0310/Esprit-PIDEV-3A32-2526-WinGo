package GUI;

import Entites.Reservation;
import Entites.Transport;
import Services.ReservationCRUD;
import Services.TransportCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class MixedFX {

    // --- Reservation Fields ---
    @FXML private TextField userField;
    @FXML private TextField expField;
    @FXML private TextField statutField;

    // --- Transport Fields ---
    @FXML private TextField typeField;
    @FXML private TextField capaciteField;
    @FXML private TextField tarifField;
    @FXML private TextField departField;
    @FXML private TextField arriveeField;

    // --- Common UI ---
    @FXML private VBox cardsContainer;
    @FXML private Label statusLabel;

    private boolean showingReservation = true;

    private Reservation selectedReservation;
    private Transport selectedTransport;

    private final ReservationCRUD reservationService = new ReservationCRUD();
    private final TransportCRUD transportService = new TransportCRUD();

    @FXML
    public void initialize() {
        loadCards();
    }

    // ================= RESERVATION LOGIC =================
    @FXML
    private void addReservation() {
        if (!showingReservation) return;
        try {
            Reservation r = new Reservation();
            r.setUser(userField.getText());
            r.setExp(expField.getText());
            r.setStatut(statutField.getText());
            r.setDate(Timestamp.valueOf(LocalDateTime.now()));

            reservationService.ajouter(r);
            loadCards();
            statusLabel.setText("✅ Reservation added");
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("❌ Add Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateReservation() {
        if (!showingReservation || selectedReservation == null) return;
        try {
            selectedReservation.setUser(userField.getText());
            selectedReservation.setExp(expField.getText());
            selectedReservation.setStatut(statutField.getText());
            selectedReservation.setDate(Timestamp.valueOf(LocalDateTime.now()));

            reservationService.modifier(selectedReservation);
            loadCards();
            statusLabel.setText("✅ Reservation updated");
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("❌ Update Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteReservation() {
        if (!showingReservation || selectedReservation == null) return;
        try {
            reservationService.supprimer(selectedReservation.getId());
            loadCards();
            statusLabel.setText("❌ Reservation deleted");
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("❌ Delete Error: " + e.getMessage());
        }
    }

    // ================= TRANSPORT LOGIC =================
    @FXML
    private void addTransport() {
        if (showingReservation) return;
        try {
            Transport t = new Transport();
            t.setType(typeField.getText());
            t.setCapacite(capaciteField.getText());
            t.setTarif(Float.parseFloat(tarifField.getText()));
            t.setDepart(departField.getText());
            t.setArrivee(arriveeField.getText());
            t.setDateDepart(LocalDateTime.now());

            transportService.ajouter(t);
            loadCards();
            statusLabel.setText("✅ Transport added");
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("❌ Add Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Invalid number format");
        }
    }

    @FXML
    private void updateTransport() {
        if (showingReservation || selectedTransport == null) return;
        try {
            selectedTransport.setType(typeField.getText());
            selectedTransport.setCapacite(capaciteField.getText());
            selectedTransport.setTarif(Float.parseFloat(tarifField.getText()));
            selectedTransport.setDepart(departField.getText());
            selectedTransport.setArrivee(arriveeField.getText());
            selectedTransport.setDateDepart(LocalDateTime.now());

            transportService.modifier(selectedTransport);
            loadCards();
            statusLabel.setText("✅ Transport updated");
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("❌ Update Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Invalid number format");
        }
    }

    @FXML
    private void deleteTransport() {
        if (showingReservation || selectedTransport == null) return;
        try {
            transportService.supprimer(selectedTransport.getId());
            loadCards();
            statusLabel.setText("❌ Transport deleted");
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("❌ Delete Error: " + e.getMessage());
        }
    }

    // ================= COMMON =================
    private void loadCards() {
        try {
            cardsContainer.getChildren().clear();
            if (showingReservation) {
                List<Reservation> list = reservationService.getAll();
                for (Reservation r : list) {
                    cardsContainer.getChildren().add(createReservationCard(r));
                }
            } else {
                List<Transport> list = transportService.getAll();
                for (Transport t : list) {
                    cardsContainer.getChildren().add(createTransportCard(t));
                }
            }
        } catch (SQLException e) {
            statusLabel.setText("❌ Load Error: " + e.getMessage());
        }
    }

    private HBox createReservationCard(Reservation r) {
        HBox card = new HBox(10);
        Label userLabel = new Label(r.getUser());
        Label expLabel = new Label(r.getExp());
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> {
            selectedReservation = r;
            userField.setText(r.getUser());
            expField.setText(r.getExp());
            statutField.setText(r.getStatut());
            statusLabel.setText("✏️ Editing reservation");
        });
        card.getChildren().addAll(userLabel, expLabel, editButton);
        return card;
    }

    private HBox createTransportCard(Transport t) {
        HBox card = new HBox(10);
        Label typeLabel = new Label(t.getType());
        Label capaciteLabel = new Label(t.getCapacite());
        Label tarifLabel = new Label(String.valueOf(t.getTarif()));
        Label departLabel = new Label(t.getDepart());
        Label arriveeLabel = new Label(t.getArrivee());

        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> {
            selectedTransport = t;
            typeField.setText(t.getType());
            capaciteField.setText(t.getCapacite());
            tarifField.setText(String.valueOf(t.getTarif()));
            departField.setText(t.getDepart());
            arriveeField.setText(t.getArrivee());
            statusLabel.setText("✏️ Editing transport");
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteTransport());

        card.getChildren().addAll(typeLabel, capaciteLabel, tarifLabel, departLabel, arriveeLabel, editButton, deleteButton);
        return card;
    }

    @FXML
    private void clearForm() {
        userField.clear();
        expField.clear();
        statutField.clear();
        typeField.clear();
        capaciteField.clear();
        tarifField.clear();
        departField.clear();
        arriveeField.clear();
        selectedReservation = null;
        selectedTransport = null;
    }

    @FXML
    private void showTransport() {
        showingReservation = !showingReservation;
        clearForm();
        loadCards();
    }
}
