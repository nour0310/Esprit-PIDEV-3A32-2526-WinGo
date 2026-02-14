package GUI;
import Services.ReservationCRUD;
import Entites.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.swing.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationFX {

    @FXML
    private TextField userField;

    @FXML
    private TextField expField;
    private Reservation selectedReservation;

    @FXML
    private TextField statutField;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox cardsContainer;
    @FXML
    private boolean showingReservation = true;

    private ReservationCRUD service = new ReservationCRUD(); // Your service class

    @FXML
    public void initialize() {
        loadCards();
    }


    /** Add new reservation */
    @FXML
    private void addReservation() {
        try {
            Reservation r = new Reservation();
            r.setUser(userField.getText());
            r.setExp(expField.getText());
            r.setStatut(statutField.getText());
            r.setDate(Timestamp.valueOf(LocalDateTime.now()));

            service.ajouter(r);
            loadCards();
            statusLabel.setText("✅ Reservation ajoutée");
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("❌ Add Error: " + e.getMessage());
        }
    }
    @FXML
    /** Clear input fields */
    private void clearForm() {
        userField.clear();
        expField.clear();
        statutField.clear();
    }

    /** Load reservations as cards */
    private void loadCards() {
        try {
            cardsContainer.getChildren().clear();
            List<Reservation> reservations = service.getAll();

            for (Reservation r : reservations) {
                HBox card = createCard(r);
                cardsContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Load Error: " + e.getMessage());
        }

    }

    /** Create one card for a reservation */
    private HBox createCard(Reservation r) {

        HBox card = new HBox(10);

        Label userLabel = new Label(r.getUser());
        Label expLabel = new Label(r.getExp());

        // ⭐ CREATE BUTTON HERE
        Button editButton = new Button("Edit");

        // ⭐ CONFIGURE BUTTON HERE
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


    /** Delete reservation */
    private void deleteReservation(Reservation r) {
        try {
            service.supprimer(r.getId());
            loadCards();
            statusLabel.setText("❌ Reservation deleted");
        } catch (SQLException e) {
            statusLabel.setText("❌ Delete Error: " + e.getMessage());
        }
    }

    /** Update reservation (example: updates current timestamp and user input) */
    private void updateReservation(Reservation r) {
        try {
            r.setUser(userField.getText());
            r.setExp(expField.getText());
            r.setStatut(statutField.getText());
            r.setDate(Timestamp.valueOf(LocalDateTime.now()));

            service.modifier(r);
            loadCards();
            statusLabel.setText("✅ Reservation updated");
        } catch (SQLException e) {
            statusLabel.setText("❌ Update Error: " + e.getMessage());
        }
    }
    @FXML

    private void handleUpdateReservation(ActionEvent event) {
        if (selectedReservation == null) {
            statusLabel.setText("⚠️ Select a reservation first");
            return;
        }

        updateReservation(selectedReservation);
    }

    @FXML

    private void handleDeleteReservation(ActionEvent event) {

        if (selectedReservation != null) {
            deleteReservation(selectedReservation);
        } else {
            statusLabel.setText("⚠️ Select a reservation first");
        }
    }



}

