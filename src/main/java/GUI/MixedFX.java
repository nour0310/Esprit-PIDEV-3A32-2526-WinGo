package GUI;

import Entites.Reservation;
import Entites.Transport;
import Services.ReservationCRUD;
import Services.TransportCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
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
        setupReservationFields();
        setupTransportFields();
        showReservations(); // start with reservations
    }

    /** Setup persistent Reservation fields */
    private void setupReservationFields() {
        resUserField = new TextField();
        resExpField = new TextField();
        resStatutField = new TextField();
        resDateField = new DatePicker();
    }

    /** Setup persistent Transport fields */
    private void setupTransportFields() {
        trTypeField = new TextField();
        trCapField = new TextField();
        trTarifField = new TextField();
        trDepartField = new TextField();
        trArriveeField = new TextField();
        trDateField = new DatePicker();
    }

    @FXML
    private void showReservations() {
        showingReservations = true;
        reservationToggleBtn.setStyle("-fx-background-color: #FFBD00;");
        transportToggleBtn.setStyle("-fx-background-color: transparent;");
        loadReservations();
        populateForm();
    }

    @FXML
    private void showTransports() {
        showingReservations = false;
        transportToggleBtn.setStyle("-fx-background-color: #FFBD00;");
        reservationToggleBtn.setStyle("-fx-background-color: transparent;");
        loadTransports();
        populateForm();
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
            Button btn = new Button(obj.toString());
            btn.setOnAction(e -> showDetails(obj));
            itemsFlowPane.getChildren().add(btn);
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
                Reservation r = new Reservation();
                r.setUser(resUserField.getText());
                r.setExp(resExpField.getText());
                r.setStatut(resStatutField.getText());
                r.setDate(java.sql.Timestamp.valueOf(resDateField.getValue().atStartOfDay()));
                reservationService.ajouter(r);
            } else {
                Transport t = new Transport();
                t.setType(trTypeField.getText());
                t.setCapacite(trCapField.getText());
                t.setTarif(Float.parseFloat(trTarifField.getText()));
                t.setDepart(trDepartField.getText());
                t.setArrivee(trArriveeField.getText());
                t.setDateDepart(trDateField.getValue().atStartOfDay());
                transportService.ajouter(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        reloadCurrent();
    }

    @FXML
    private void handleEdit() {
        if (selectedItem == null) return;
        try {
            if (showingReservations) {
                Reservation r = (Reservation) selectedItem;
                r.setUser(resUserField.getText());
                r.setExp(resExpField.getText());
                r.setStatut(resStatutField.getText());
                r.setDate(java.sql.Timestamp.valueOf(resDateField.getValue().atStartOfDay()));
                reservationService.modifier(r);
            } else {
                Transport t = (Transport) selectedItem;
                t.setType(trTypeField.getText());
                t.setCapacite(trCapField.getText());
                t.setTarif(Float.parseFloat(trTarifField.getText()));
                t.setDepart(trDepartField.getText());
                t.setArrivee(trArriveeField.getText());
                t.setDateDepart(trDateField.getValue().atStartOfDay());
                transportService.modifier(t);
            }
        } catch (SQLException e) {
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
