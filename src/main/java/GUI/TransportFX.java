package GUI;

import Entites.Transport;
import Services.TransportCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TransportFX {

    @FXML
    private TextField typeField;

    @FXML
    private TextField capaciteField;

    @FXML
    private TextField tarifField;

    @FXML
    private TextField departField;

    @FXML
    private TextField arriveeField;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox cardsContainer;

    private Transport selectedTransport;

    private TransportCRUD service = new TransportCRUD();

    @FXML
    public void initialize() {
        loadCards();
    }

    /** Add new transport */
    @FXML
    private void addTransport() {
        try {
            Transport t = new Transport();
            t.setType(typeField.getText());
            t.setCapacite(capaciteField.getText());
            t.setTarif(Float.parseFloat(tarifField.getText()));
            t.setDepart(departField.getText());
            t.setArrivee(arriveeField.getText());
            t.setDateDepart(LocalDateTime.now());

            service.ajouter(t);
            loadCards();
            statusLabel.setText("✅ Transport added");
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("❌ Add Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Invalid number format");
        }
    }

    /** Clear input fields */
    private void clearForm() {
        typeField.clear();
        capaciteField.clear();
        tarifField.clear();
        departField.clear();
        arriveeField.clear();
        selectedTransport = null;
    }

    /** Load transports as cards */
    private void loadCards() {
        try {
            cardsContainer.getChildren().clear();
            List<Transport> transports = service.afficher();

            for (Transport t : transports) {
                HBox card = createCard(t);
                cardsContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Load Error: " + e.getMessage());
        }
    }

    /** Create one card for a transport */
    private HBox createCard(Transport t) {
        HBox card = new HBox(10);

        Label typeLabel = new Label(t.getType());
        Label capaciteLabel = new Label(t.getCapacite());
        Label tarifLabel = new Label(String.valueOf(t.getTarif()));
        Label departLabel = new Label(t.getDepart());
        Label arriveeLabel = new Label(t.getArrivee());

        // Edit button
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

        // Delete button
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteTransport(t));

        card.getChildren().addAll(typeLabel, capaciteLabel, tarifLabel, departLabel, arriveeLabel, editButton, deleteButton);

        return card;
    }

    /** Delete transport */
    private void deleteTransport(Transport t) {
        try {
            service.supprimer(t.getId());
            loadCards();
            statusLabel.setText("❌ Transport deleted");
        } catch (SQLException e) {
            statusLabel.setText("❌ Delete Error: " + e.getMessage());
        }
    }

    /** Update transport */
    @FXML
    private void updateTransport() {
        if (selectedTransport == null) {
            statusLabel.setText("⚠️ Select a transport first");
            return;
        }

        try {
            selectedTransport.setType(typeField.getText());
            selectedTransport.setCapacite(capaciteField.getText());
            selectedTransport.setTarif(Float.parseFloat(tarifField.getText()));
            selectedTransport.setDepart(departField.getText());
            selectedTransport.setArrivee(arriveeField.getText());
            selectedTransport.setDateDepart(LocalDateTime.now());

            service.modifier(selectedTransport);
            loadCards();
            statusLabel.setText("✅ Transport updated");
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("❌ Update Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Invalid number format");
        }
    }
}
