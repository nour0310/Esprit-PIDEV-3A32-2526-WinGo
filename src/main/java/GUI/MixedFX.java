package GUI;

import Entites.Reservation;
import Entites.Transport;
import Services.ReservationCRUD;
import Services.TransportCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
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

    @FXML
    public void initialize() {
        loadReservations();
    }

    @FXML
    private void showReservations() {
        showingReservations = true;
        reservationToggleBtn.setStyle("-fx-background-color: #FFBD00;");
        transportToggleBtn.setStyle("-fx-background-color: transparent;");
        loadReservations();
        clearForm();
    }

    @FXML
    private void showTransports() {
        showingReservations = false;
        transportToggleBtn.setStyle("-fx-background-color: #FFBD00;");
        reservationToggleBtn.setStyle("-fx-background-color: transparent;");
        loadTransports();
        clearForm();
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
            Button btn = new Button(obj.toString()); // replace with custom display
            btn.setOnAction(e -> showDetails(obj));
            itemsFlowPane.getChildren().add(btn);
        }
    }

    private void showDetails(Object item) {
        selectedItem = item;
        listScroll.setVisible(false);
        listScroll.setManaged(false);
        detailScroll.setVisible(true);
        detailScroll.setManaged(true);
        detailGrid.getChildren().clear();
        // populate detailGrid dynamically based on type
    }

    @FXML
    private void backToList() {
        detailScroll.setVisible(false);
        detailScroll.setManaged(false);
        listScroll.setVisible(true);
        listScroll.setManaged(true);
    }

    @FXML
    private void handleAdd() {
        if (showingReservations) {
            // call reservationService.ajouter(...)
        } else {
            // call transportService.ajouter(...)
        }
        reloadCurrent();
    }

    @FXML
    private void handleEdit() {
        if (selectedItem == null) return;
        if (showingReservations) {
            // call reservationService.modifier(...)
        } else {
            // call transportService.modifier(...)
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
        formGrid.getChildren().clear();
        selectedItem = null;
        if (showingReservations) {
            formTitle.setText("Formulaire Réservation");
            // dynamically create Reservation fields in formGrid
        } else {
            formTitle.setText("Formulaire Transport");
            // dynamically create Transport fields in formGrid
        }
    }

    private void reloadCurrent() {
        if (showingReservations) loadReservations();
        else loadTransports();
        clearForm();
    }
}
