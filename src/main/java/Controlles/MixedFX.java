package Controlles;

import Entites.Reservation;
import Entites.Transport;
import Services.ReservationCRUD;
import Services.TransportCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.List;

public class MixedFX {

    @FXML private Button reservationToggleBtn, transportToggleBtn;
    @FXML private FlowPane itemsFlowPane;
    @FXML private GridPane formGrid, detailGrid;
    @FXML private Label formTitle;
    @FXML private Button addBtn, editBtn, deleteBtn, clearBtn;
    @FXML private ScrollPane listScroll, detailScroll;

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

    @FXML
    public void initialize() {
        setupFormFields();
        loadReservations();
        loadTransports();// start with reservations
    }

    /** Setup persistent Reservation fields */
    private void setupFormFields() {
        formGrid.getChildren().clear();

        if (showingReservations) {
            formTitle.setText("Formulaire Réservation");

            Label userLabel = new Label("User:");
            resUserField = new TextField();
            Label expLabel = new Label("Exp:");
            resExpField = new TextField();
            Label statLabel = new Label("Stat:");
            resStatutField = new TextField();
            Label dateLabel = new Label("Date:");
            resDateField = new DatePicker();

            formGrid.add(userLabel, 0, 0); formGrid.add(resUserField, 1, 0);
            formGrid.add(expLabel, 0, 1); formGrid.add(resExpField, 1, 1);
            formGrid.add(statLabel, 0, 2); formGrid.add(resStatutField, 1, 2);
            formGrid.add(dateLabel, 0, 3); formGrid.add(resDateField, 1, 3);

        } else {
            formTitle.setText("Formulaire Transport");

            Label typeLabel = new Label("Type:");
            trTypeField = new TextField();
            Label capLabel = new Label("Capacité:");
            trCapField = new TextField();
            Label tarifLabel = new Label("Tarif:");
            trTarifField = new TextField();
            Label departLabel = new Label("Départ:");
            trDepartField = new TextField();
            Label arriveeLabel = new Label("Arrivée:");
            trArriveeField = new TextField();
            Label dateLabel = new Label("Date départ:");
            trDateField = new DatePicker();

            formGrid.add(typeLabel, 0, 0); formGrid.add(trTypeField, 1, 0);
            formGrid.add(capLabel, 0, 1); formGrid.add(trCapField, 1, 1);
            formGrid.add(tarifLabel, 0, 2); formGrid.add(trTarifField, 1, 2);
            formGrid.add(departLabel, 0, 3); formGrid.add(trDepartField, 1, 3);
            formGrid.add(arriveeLabel, 0, 4); formGrid.add(trArriveeField, 1, 4);
            formGrid.add(dateLabel, 0, 5); formGrid.add(trDateField, 1, 5);
        }
    }

    @FXML
    private void showReservations() {
        showingReservations = true;

        reservationToggleBtn.setStyle("-fx-background-color: #FFBD00;");
        transportToggleBtn.setStyle("-fx-background-color: transparent;");

        setupFormFields();   // ✅ VERY IMPORTANT
        loadReservations();
        selectedItem = null;
    }

    @FXML
    private void showTransports() {
        showingReservations = false;

        transportToggleBtn.setStyle("-fx-background-color: #FFBD00;");
        reservationToggleBtn.setStyle("-fx-background-color: transparent;");

        setupFormFields();   // ✅ CREATE THE FIELDS FIRST
        loadTransports();
        selectedItem = null;
    }

    private void loadReservations() {
        try {
            reservationList = reservationService.getAll();
            populateItems(reservationList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTransports() {
        try {
            transportList = transportService.getAll();
            populateItems(transportList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateItems(List<?> items) {

        itemsFlowPane.getChildren().clear();

        for (Object obj : items) {

            Button card = new Button(obj.toString());

            card.setOnMouseClicked(e -> {

                selectedItem = obj;     // store selected object
                populateForm();      // send data to textfields

            });

            itemsFlowPane.getChildren().add(card);
        }
    }



    /** Populate formGrid based on current entity */
    private void populateForm() {
        formGrid.getChildren().clear();
        if (showingReservations) {
            formTitle.setText("Formulaire Réservation");
            formGrid.add(new Label("User:"), 0, 0); formGrid.add(resUserField, 1, 0);
            formGrid.add(new Label("Exp:"), 0, 1); formGrid.add(resExpField, 1, 1);
            formGrid.add(new Label("Statut:"), 0, 2); formGrid.add(resStatutField, 1, 2);
            formGrid.add(new Label("Date:"), 0, 3); formGrid.add(resDateField, 1, 3);
        } else {
            formTitle.setText("Formulaire Transport");
            formGrid.add(new Label("Type:"), 0, 0); formGrid.add(trTypeField, 1, 0);
            formGrid.add(new Label("Capacité:"), 0, 1); formGrid.add(trCapField, 1, 1);
            formGrid.add(new Label("Tarif:"), 0, 2); formGrid.add(trTarifField, 1, 2);
            formGrid.add(new Label("Départ:"), 0, 3); formGrid.add(trDepartField, 1, 3);
            formGrid.add(new Label("Arrivée:"), 0, 4); formGrid.add(trArriveeField, 1, 4);
            formGrid.add(new Label("Date départ:"), 0, 5); formGrid.add(trDateField, 1, 5);
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
            }
        } catch (SQLException e) {
            showAlert("Erreur Base de Données", "Erreur lors de l'ajout !", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
        reloadCurrent();
    }
    // 🔎 Validation for Reservation
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


    // 🔎 Validation for Transport
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

        // Check if tarif is numeric
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
            detailGrid.addRow(0, new Label("User:"), new Label(r.getUser()));
            detailGrid.addRow(1, new Label("Exp:"), new Label(r.getExp()));
            detailGrid.addRow(2, new Label("Statut:"), new Label(r.getStatut()));
            detailGrid.addRow(3, new Label("Date:"), new Label(r.getDate().toString()));
        } else {
            Transport t = (Transport) item;
            detailGrid.addRow(0, new Label("Type:"), new Label(t.getType()));
            detailGrid.addRow(1, new Label("Capacité:"), new Label(t.getCapacite()));
            detailGrid.addRow(2, new Label("Tarif:"), new Label(String.valueOf(t.getTarif())));
            detailGrid.addRow(3, new Label("Départ:"), new Label(t.getDepart()));
            detailGrid.addRow(4, new Label("Arrivée:"), new Label(t.getArrivee()));
            detailGrid.addRow(5, new Label("Date départ:"), new Label(t.getDateDepart().toString()));
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
