package Controlles;

import Entites.Reservation;
import Entites.Transport;
import Services.ReservationCRUD;
import Services.TransportCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class MixedBack {

    @FXML private VBox articlesContainer, dashboardView, listView, selectionHub;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Label statTotalBlogs, statTotalComments, statEngagement;
    @FXML private PieChart regionChart, transportTypeChart;

    // Mise à jour des boutons Sidebar pour matcher le FXML
    @FXML private ToggleButton btnDashboard, btnManagement;

    private final ReservationCRUD resService = new ReservationCRUD();
    private final TransportCRUD transService = new TransportCRUD();
    private List<Reservation> allReservations = new ArrayList<>();
    private List<Transport> allTransports = new ArrayList<>();
    private boolean showingTransports = true;

    @FXML
    public void initialize() {
        sortComboBox.getItems().addAll("Prix (Croissant)", "Prix (Décroissant)", "Date (Récent)");
        searchField.textProperty().addListener((obs, old, val) -> updateDisplay());

        loadAllData();
    }

    private void loadAllData() {
        try {
            allReservations = resService.getAll();
            allTransports = transService.getAll();
            updateStats();
            updateCharts();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- NAVIGATION PRINCIPALE (SIDEBAR) ---
    @FXML
    private void handleNavigation(ActionEvent event) {
        ToggleButton activeBtn = (ToggleButton) event.getSource();
        updateSidebarStyles(activeBtn);

        // Reset des vues
        dashboardView.setVisible(false);
        selectionHub.setVisible(false);
        listView.setVisible(false);

        if (activeBtn == btnDashboard) {
            dashboardView.setVisible(true);
            loadAllData();
        } else if (activeBtn == btnManagement) {
            selectionHub.setVisible(true);
        }
    }

    // --- NAVIGATION INTERNE (HUB) ---
    @FXML
    private void showTransportsModule() {
        this.showingTransports = true;
        selectionHub.setVisible(false);
        listView.setVisible(true);
        updateDisplay();
    }

    @FXML
    private void showReservationsModule() {
        this.showingTransports = false;
        selectionHub.setVisible(false);
        listView.setVisible(true);
        updateDisplay();
    }

    @FXML
    private void backToHub() {
        listView.setVisible(false);
        selectionHub.setVisible(true);
    }

    // --- LOGIQUE D'AFFICHAGE ---
    private void updateSidebarStyles(ToggleButton activeBtn) {
        List<ToggleButton> btns = Arrays.asList(btnDashboard, btnManagement);
        for (ToggleButton b : btns) {
            if (b == activeBtn) {
                b.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-background-radius: 14; -fx-font-weight: bold;");
                b.setEffect(new DropShadow(10, Color.web("#6366F14D")));
            } else {
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-background-radius: 14; -fx-font-weight: bold;");
                b.setEffect(null);
            }
        }
    }

    @FXML
    private void updateDisplay() {
        articlesContainer.getChildren().clear();
        String query = searchField.getText().toLowerCase();

        if (showingTransports) {
            allTransports.stream()
                    .filter(t -> t.getType().toLowerCase().contains(query) || t.getDepart().toLowerCase().contains(query))
                    .forEach(t -> articlesContainer.getChildren().add(createRow("🚗 " + t.getType(), t.getDepart() + " -> " + t.getArrivee(), String.format("%.3f TND", t.getTarif()), "TR_"+t.getId())));
        } else {
            allReservations.stream()
                    .filter(r -> r.getUser().toLowerCase().contains(query))
                    .forEach(r -> articlesContainer.getChildren().add(createRow("📅 " + r.getUser(), "Statut: " + r.getStatut(), r.getDate().toString(), "RES_"+r.getId())));
        }
    }

    private HBox createRow(String t1, String t2, String val, String qr) {
        HBox row = new HBox(20); row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        ImageView qrView = new ImageView(MixedFX.generateQRCode(qr));
        qrView.setFitWidth(65);
        qrView.setFitHeight(65);

        VBox info = new VBox(5);
        Label lbl1 = new Label(t1); lbl1.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label lbl2 = new Label(t2); lbl2.setStyle("-fx-text-fill: #64748B;");
        info.getChildren().addAll(lbl1, lbl2);

        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Label price = new Label(val); price.setStyle("-fx-text-fill: #6366F1; -fx-font-weight: bold;");

        row.getChildren().addAll(qrView, info, s, price);
        return row;
    }

    private void updateCharts() {
        ObservableList<PieChart.Data> destData = FXCollections.observableArrayList();
        allReservations.stream()
                .collect(Collectors.groupingBy(Reservation::getStatut, Collectors.counting()))
                .forEach((k, v) -> destData.add(new PieChart.Data(k, v)));
        regionChart.setData(destData);

        ObservableList<PieChart.Data> typeData = FXCollections.observableArrayList();
        allTransports.stream()
                .collect(Collectors.groupingBy(Transport::getType, Collectors.counting()))
                .forEach((k, v) -> typeData.add(new PieChart.Data(k, v)));
        transportTypeChart.setData(typeData);
    }

    private void updateStats() {
        statTotalBlogs.setText(String.valueOf(allTransports.size()));
        statTotalComments.setText(String.valueOf(allReservations.size()));
        double totalRev = allTransports.stream().mapToDouble(Transport::getTarif).sum();
        statEngagement.setText(String.format("%.1f", totalRev));
    }

    @FXML private void loadData() { loadAllData(); }
}