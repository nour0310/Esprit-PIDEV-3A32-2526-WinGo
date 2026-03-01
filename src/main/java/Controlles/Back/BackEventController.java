package Controlles.Back;

import Entites.Event;
import Entites.Participation;
import Services.EventCRUD;
import Services.ParticipationCRUD;
import Services.MailService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class BackEventController implements Initializable {

    // ==================== FXML FIELDS ====================
    // Add these FXML fields for scroll panes
    @FXML private ScrollPane dashboardScroll;
    @FXML private ScrollPane eventsScroll;
    @FXML private ScrollPane participationsScroll;

    // Navigation Views
    @FXML private VBox viewDashboard;
    @FXML private VBox viewEvents;
    @FXML private VBox viewParticipations;
    @FXML private ToggleButton btnDashboard;
    @FXML private ToggleButton btnEvents;
    @FXML private ToggleButton btnParticipations;
    @FXML private ToggleGroup navGroup;

    // Statistics Dashboard
    @FXML private PieChart eventTypePieChart;
    @FXML private BarChart<String, Number> monthlyBookingsChart;
    @FXML private TableView<Event> topEventsTable;
    @FXML private TableColumn<Event, Integer> topEventRank;
    @FXML private TableColumn<Event, String> topEventTitle;
    @FXML private TableColumn<Event, Integer> topEventBookings;
    @FXML private TableColumn<Event, Double> topEventRevenue;
    @FXML private TableColumn<Event, Double> topEventFillRate;
    @FXML private Label averageFillRateLabel;
    @FXML private Label mostBookedEventLabel;
    @FXML private Label highestRevenueEventLabel;
    @FXML private Label upcomingEventsLabel;
    @FXML private Label totalEventsLabel;
    @FXML private Label totalParticipationsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label averagePriceLabel;
    @FXML private Label activeEventsLabel;

    // Event Form Fields
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField capacityField;
    @FXML private TextField statusField;
    @FXML private TextField imageField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> seasonBox;
    @FXML private ComboBox<String> eventTypeBox;
    @FXML private ImageView eventImageView;
    @FXML private Label eventImagePathLabel;
    @FXML private Button chooseImageButton;

    // Event Table
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, Integer> colId;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, String> colLocation;
    @FXML private TableColumn<Event, String> colDate;
    @FXML private TableColumn<Event, String> colTime;
    @FXML private TableColumn<Event, Integer> colCapacity;
    @FXML private TableColumn<Event, String> colSeason;
    @FXML private TableColumn<Event, String> colStatus;
    @FXML private TableColumn<Event, Double> colPrice;
    @FXML private TableColumn<Event, Integer> colBookings;
    @FXML private TableColumn<Event, Double> colRevenue;

    // Participation Management
    @FXML private TableView<Participation> participationTable;
    @FXML private TableColumn<Participation, Integer> colPartId;
    @FXML private TableColumn<Participation, Integer> colEvent;
    @FXML private TableColumn<Participation, String> colEventTitle;
    @FXML private TableColumn<Participation, Date> colPartDate;
    @FXML private TableColumn<Participation, String> colStatut;
    @FXML private TableColumn<Participation, String> colNom;
    @FXML private TableColumn<Participation, String> colPrenom;
    @FXML private TableColumn<Participation, String> colEmail;
    @FXML private TableColumn<Participation, Integer> colPlaces;
    @FXML private TableColumn<Participation, Integer> colUserId;
    @FXML private ComboBox<Event> eventSelectorBox;
    @FXML private Label eventIdLabel;

    // Participation Form Fields
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField placesField;
    @FXML private DatePicker partDatePicker;
    @FXML private ComboBox<String> statutBox;
    @FXML private TextField userIdField;

    // Search Components
    @FXML private TextField globalSearchField;
    @FXML private Label eventResultCountLabel;
    @FXML private Label participationCountBadge;
    @FXML private TextField participationsSearchField;
    @FXML private ComboBox<String> searchTypeBox;
    @FXML private Label resultCountLabel;
    @FXML private Label eventCountBadge;
    @FXML private Label selectedEventLabel;

    // ==================== SERVICES ====================

    private final EventCRUD eventCRUD = new EventCRUD();
    private final ParticipationCRUD participationCRUD = new ParticipationCRUD();
    private int currentEventId = 0;
    private String selectedImagePath = "";
    private final String IMAGE_DIRECTORY = "src/main/resources/images/events/";
    private Window primaryWindow;

    // Cache for statistics
    private final Map<Integer, Integer> eventBookings = new HashMap<>();
    private final Map<Integer, Double> eventRevenue = new HashMap<>();

    // ==================== INITIALIZATION ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeEventSection();
        initializeParticipationSection();
        initializeStatisticsSection();

        // Create image directory if it doesn't exist
        File directory = new File(IMAGE_DIRECTORY);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Image directory created: " + IMAGE_DIRECTORY);
            }
        }

        // Store window reference for later use
        Platform.runLater(() -> {
            if (chooseImageButton != null && chooseImageButton.getScene() != null) {
                primaryWindow = chooseImageButton.getScene().getWindow();
            } else if (viewEvents != null && viewEvents.getScene() != null) {
                primaryWindow = viewEvents.getScene().getWindow();
            }
        });

        loadEvents();
        loadAllParticipations();
        calculateStatistics();

        // Set default view
        showDashboard();
    }

    // ==================== NAVIGATION METHODS ====================

    @FXML
    private void showDashboard() {
        // Show/hide scroll panes
        dashboardScroll.setVisible(true);
        dashboardScroll.setManaged(true);
        eventsScroll.setVisible(false);
        eventsScroll.setManaged(false);
        participationsScroll.setVisible(false);
        participationsScroll.setManaged(false);

        // Reset scroll position to top
        dashboardScroll.setVvalue(0);

        // Request focus to enable scrolling
        dashboardScroll.requestFocus();

        // Update statistics
        updateStatistics();
    }

    @FXML
    private void showEvents() {
        // Show/hide scroll panes
        dashboardScroll.setVisible(false);
        dashboardScroll.setManaged(false);
        eventsScroll.setVisible(true);
        eventsScroll.setManaged(true);
        participationsScroll.setVisible(false);
        participationsScroll.setManaged(false);

        // Reset scroll position to top
        eventsScroll.setVvalue(0);

        // Request focus to enable scrolling
        eventsScroll.requestFocus();

        // Refresh events list
        loadEvents();
    }

    @FXML
    private void showParticipations() {
        // Show/hide scroll panes
        dashboardScroll.setVisible(false);
        dashboardScroll.setManaged(false);
        eventsScroll.setVisible(false);
        eventsScroll.setManaged(false);
        participationsScroll.setVisible(true);
        participationsScroll.setManaged(true);

        // Reset scroll position to top
        participationsScroll.setVvalue(0);

        // Request focus to enable scrolling
        participationsScroll.requestFocus();

        // Refresh participations list
        loadAllParticipations();
    }

    // ==================== STATISTICS METHODS ====================

    @FXML
    public void initializeStatisticsSection() {
        if (topEventRank != null) {
            topEventRank.setCellValueFactory(cellData -> {
                int index = topEventsTable.getItems().indexOf(cellData.getValue()) + 1;
                return new javafx.beans.property.SimpleIntegerProperty(index).asObject();
            });
        }
        if (topEventTitle != null) topEventTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (topEventBookings != null) {
            topEventBookings.setCellValueFactory(cellData -> {
                Event event = cellData.getValue();
                return new javafx.beans.property.SimpleIntegerProperty(getEventBookings(event.getIdEvent())).asObject();
            });
        }
        if (topEventRevenue != null) {
            topEventRevenue.setCellValueFactory(cellData -> {
                Event event = cellData.getValue();
                return new javafx.beans.property.SimpleDoubleProperty(getEventRevenue(event.getIdEvent())).asObject();
            });
            topEventRevenue.setCellFactory(column -> new TableCell<Event, Double>() {
                @Override
                protected void updateItem(Double revenue, boolean empty) {
                    super.updateItem(revenue, empty);
                    if (empty || revenue == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", revenue));
                    }
                }
            });
        }
        if (topEventFillRate != null) {
            topEventFillRate.setCellValueFactory(cellData -> {
                Event event = cellData.getValue();
                int bookings = getEventBookings(event.getIdEvent());
                double fillRate = event.getCapacity() > 0 ? (bookings * 100.0 / event.getCapacity()) : 0;
                return new javafx.beans.property.SimpleDoubleProperty(fillRate).asObject();
            });
            topEventFillRate.setCellFactory(column -> new TableCell<Event, Double>() {
                @Override
                protected void updateItem(Double fillRate, boolean empty) {
                    super.updateItem(fillRate, empty);
                    if (empty || fillRate == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.1f%%", fillRate));
                    }
                }
            });
        }
    }

    @FXML
    public void calculateStatistics() {
        List<Participation> allParticipations = participationCRUD.afficherTous();
        List<Event> allEvents = eventCRUD.afficher();

        eventBookings.clear();
        eventRevenue.clear();

        int totalParticipations = 0;
        double totalRevenue = 0.0;

        for (Participation p : allParticipations) {
            int eventId = p.getIdEvent();
            int places = p.getNombrePlaces();

            double price = 0.0;
            for (Event e : allEvents) {
                if (e.getIdEvent() == eventId) {
                    price = e.getPrice();
                    break;
                }
            }

            int currentBookings = eventBookings.getOrDefault(eventId, 0);
            eventBookings.put(eventId, currentBookings + places);

            double currentRevenue = eventRevenue.getOrDefault(eventId, 0.0);
            eventRevenue.put(eventId, currentRevenue + (price * places));

            totalParticipations += places;
            totalRevenue += (price * places);
        }

        if (totalEventsLabel != null) totalEventsLabel.setText(String.valueOf(allEvents.size()));
        if (totalParticipationsLabel != null) totalParticipationsLabel.setText(String.valueOf(totalParticipations));
        if (totalRevenueLabel != null) totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
        if (averagePriceLabel != null && !allEvents.isEmpty()) {
            double avgPrice = allEvents.stream().mapToDouble(Event::getPrice).average().orElse(0.0);
            averagePriceLabel.setText(String.format("$%.2f", avgPrice));
        }

        if (activeEventsLabel != null) {
            long activeCount = allEvents.stream()
                    .filter(e -> "Planifie".equals(e.getStatus()) || "Active".equals(e.getStatus()))
                    .count();
            activeEventsLabel.setText(activeCount + " active");
        }

        updateEventTypePieChart();
        updateMonthlyBookingsChart();
        updateTopEventsTable();
        updateQuickStats();
    }

    private void updateStatistics() {
        calculateStatistics();
    }

    private int getEventBookings(int eventId) {
        return eventBookings.getOrDefault(eventId, 0);
    }

    private double getEventRevenue(int eventId) {
        return eventRevenue.getOrDefault(eventId, 0.0);
    }

    private void updateEventTypePieChart() {
        if (eventTypePieChart == null) return;

        List<Event> events = eventCRUD.afficher();
        Map<String, Integer> typeCount = new HashMap<>();

        for (Event event : events) {
            String type = event.getEventType();
            if (type != null && !type.isEmpty()) {
                typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        eventTypePieChart.setData(pieChartData);
        eventTypePieChart.setTitle("Events by Type");
    }

    private void updateMonthlyBookingsChart() {
        if (monthlyBookingsChart == null) return;

        List<Participation> participations = participationCRUD.afficherTous();
        Map<String, Integer> monthlyBookings = new TreeMap<>();

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String month : months) {
            monthlyBookings.put(month, 0);
        }

        for (Participation p : participations) {
            Date date = p.getDateParticipation();
            if (date != null) {
                LocalDate localDate = date.toLocalDate();
                String month = months[localDate.getMonthValue() - 1];
                monthlyBookings.put(month, monthlyBookings.get(month) + p.getNombrePlaces());
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bookings");

        for (Map.Entry<String, Integer> entry : monthlyBookings.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        monthlyBookingsChart.getData().clear();
        monthlyBookingsChart.getData().add(series);
    }

    private void updateTopEventsTable() {
        if (topEventsTable == null) return;

        List<Event> events = eventCRUD.afficher();
        events.sort((e1, e2) -> Integer.compare(getEventBookings(e2.getIdEvent()), getEventBookings(e1.getIdEvent())));

        List<Event> topEvents = events.stream().limit(5).collect(Collectors.toList());
        topEventsTable.setItems(FXCollections.observableArrayList(topEvents));
    }

    private void updateQuickStats() {
        List<Event> events = eventCRUD.afficher();

        if (!eventBookings.isEmpty() && mostBookedEventLabel != null) {
            Map.Entry<Integer, Integer> maxEntry = Collections.max(eventBookings.entrySet(), Map.Entry.comparingByValue());
            Event mostBooked = events.stream().filter(e -> e.getIdEvent() == maxEntry.getKey()).findFirst().orElse(null);
            if (mostBooked != null) {
                mostBookedEventLabel.setText(mostBooked.getTitle() + " (" + maxEntry.getValue() + ")");
            }
        }

        if (!eventRevenue.isEmpty() && highestRevenueEventLabel != null) {
            Map.Entry<Integer, Double> maxRevenue = Collections.max(eventRevenue.entrySet(), Map.Entry.comparingByValue());
            Event highestRevenue = events.stream().filter(e -> e.getIdEvent() == maxRevenue.getKey()).findFirst().orElse(null);
            if (highestRevenue != null) {
                highestRevenueEventLabel.setText(highestRevenue.getTitle() + " ($" + String.format("%.2f", maxRevenue.getValue()) + ")");
            }
        }

        long upcomingCount = events.stream()
                .filter(e -> e.getDateEvent() != null && e.getDateEvent().toLocalDate().isAfter(LocalDate.now()))
                .count();
        if (upcomingEventsLabel != null) {
            upcomingEventsLabel.setText(upcomingCount + " upcoming");
        }

        if (!events.isEmpty() && averageFillRateLabel != null) {
            double totalFillRate = 0;
            int count = 0;
            for (Event e : events) {
                int bookings = getEventBookings(e.getIdEvent());
                if (e.getCapacity() > 0) {
                    totalFillRate += (bookings * 100.0 / e.getCapacity());
                    count++;
                }
            }
            double avgFillRate = count > 0 ? totalFillRate / count : 0;
            averageFillRateLabel.setText(String.format("%.1f%%", avgFillRate));
        }
    }

    // ==================== EVENT CRUD METHODS ====================

    @FXML
    public void initializeEventSection() {
        // Initialize event table columns
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("idEvent"));
        if (colTitle != null) colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colLocation != null) colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        if (colPrice != null) {
            colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
            colPrice.setCellFactory(column -> new TableCell<Event, Double>() {
                @Override
                protected void updateItem(Double price, boolean empty) {
                    super.updateItem(price, empty);
                    if (empty || price == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", price));
                    }
                }
            });
        }

        if (colBookings != null) {
            colBookings.setCellValueFactory(cellData -> {
                Event event = cellData.getValue();
                return new javafx.beans.property.SimpleIntegerProperty(getEventBookings(event.getIdEvent())).asObject();
            });
        }

        if (colRevenue != null) {
            colRevenue.setCellValueFactory(cellData -> {
                Event event = cellData.getValue();
                return new javafx.beans.property.SimpleDoubleProperty(getEventRevenue(event.getIdEvent())).asObject();
            });
            colRevenue.setCellFactory(column -> new TableCell<Event, Double>() {
                @Override
                protected void updateItem(Double revenue, boolean empty) {
                    super.updateItem(revenue, empty);
                    if (empty || revenue == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", revenue));
                    }
                }
            });
        }

        if (colDate != null) {
            colDate.setCellValueFactory(cellData -> {
                Date date = cellData.getValue().getDateEvent();
                return new javafx.beans.property.SimpleStringProperty(date != null ? date.toString() : "");
            });
        }

        if (colTime != null) {
            colTime.setCellValueFactory(cellData -> {
                Time time = cellData.getValue().getStartTime();
                return new javafx.beans.property.SimpleStringProperty(time != null ? time.toString().substring(0, 5) : "");
            });
        }

        if (colCapacity != null) colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        if (colSeason != null) colSeason.setCellValueFactory(new PropertyValueFactory<>("season"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Initialize combo boxes
        if (eventTypeBox != null) {
            eventTypeBox.getItems().addAll("Cultural", "Sport", "Music", "Business", "Food", "Art");
            eventTypeBox.setValue("Cultural");
        }
        if (seasonBox != null) {
            seasonBox.getItems().addAll("Winter", "Spring", "Summer", "Autumn");
            seasonBox.setValue("Summer");
        }
        if (statusField != null) statusField.setText("Planifie");
        if (priceField != null) priceField.setText("0.00");

        // Add table selection listener
        if (eventTable != null) {
            eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    fillEventFields(newVal);
                    if (selectedEventLabel != null) {
                        selectedEventLabel.setText("Selected: " + newVal.getTitle() +
                                " ($" + String.format("%.2f", newVal.getPrice()) + ") - " +
                                getEventBookings(newVal.getIdEvent()) + " bookings");
                    }
                }
            });
        }

        // Initialize event selector
        if (eventSelectorBox != null) {
            loadEventSelector();
        }
    }

    @FXML
    private void addEvent() {
        try {
            if (!validateEventInputs()) return;

            Event e = buildEventFromForm();
            eventCRUD.ajouter(e);

            loadEvents();
            clearEventFields();
            loadEventSelector();
            calculateStatistics();
            showInfo("✅ Event added successfully! Price: $" + String.format("%.2f", e.getPrice()));

        } catch (NumberFormatException ex) {
            showAlert("❌ Capacity and Price must be valid numbers!");
        } catch (DateTimeParseException ex) {
            showAlert("❌ Time must be like HH:mm (e.g., 19:30)!");
        } catch (Exception ex) {
            showAlert("❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void updateEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("⚠️ Please select an event to update");
            return;
        }

        try {
            if (!validateEventInputs()) return;

            Event e = buildEventFromForm();
            e.setIdEvent(selected.getIdEvent());

            eventCRUD.modifier(e);
            loadEvents();
            clearEventFields();
            loadEventSelector();
            calculateStatistics();
            showInfo("✅ Event updated successfully! New price: $" + String.format("%.2f", e.getPrice()));

        } catch (NumberFormatException ex) {
            showAlert("❌ Capacity and Price must be valid numbers!");
        } catch (Exception ex) {
            showAlert("❌ Invalid input: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void deleteEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("⚠️ Please select an event to delete");
            return;
        }

        if (confirmAction("Delete Event", "Are you sure you want to delete this event?")) {
            eventCRUD.supprimer(selected.getIdEvent());
            loadEvents();
            clearEventFields();
            loadEventSelector();
            calculateStatistics();
            showInfo("✅ Event deleted successfully!");
        }
    }

    @FXML
    private void clearEventFields() {
        if (titleField != null) titleField.clear();
        if (descriptionArea != null) descriptionArea.clear();
        if (locationField != null) locationField.clear();
        if (datePicker != null) datePicker.setValue(null);
        if (timeField != null) timeField.clear();
        if (capacityField != null) capacityField.clear();
        if (priceField != null) priceField.setText("0.00");
        if (seasonBox != null) seasonBox.setValue("Summer");
        if (eventTypeBox != null) eventTypeBox.setValue("Cultural");
        if (statusField != null) statusField.setText("Planifie");
        if (imageField != null) imageField.clear();
        if (eventImageView != null) eventImageView.setImage(null);
        if (eventImagePathLabel != null) {
            eventImagePathLabel.setText("No image selected");
            eventImagePathLabel.setStyle("-fx-text-fill: #64748B;");
        }
        if (eventTable != null) eventTable.getSelectionModel().clearSelection();
        if (selectedEventLabel != null) selectedEventLabel.setText("");
        selectedImagePath = "";
    }

    private Event buildEventFromForm() {
        LocalDate localDate = datePicker.getValue();
        LocalTime localTime = LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        int capacity = Integer.parseInt(capacityField.getText().trim());

        double price = 0.0;
        String priceText = priceField.getText().trim();
        if (!priceText.isEmpty()) {
            try {
                priceText = priceText.replace(',', '.');
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid price format: " + priceText + ", using 0.0");
                price = 0.0;
            }
        }

        Event e = new Event();
        e.setTitle(titleField.getText().trim());
        e.setDescription(descriptionArea.getText() != null ? descriptionArea.getText().trim() : "");
        e.setLocation(locationField.getText().trim());
        e.setDateEvent(Date.valueOf(localDate));
        e.setStartTime(Time.valueOf(localTime));
        e.setCapacity(capacity);
        e.setAvailablePlaces(capacity);
        e.setSeason(seasonBox.getValue());
        e.setEventType(eventTypeBox.getValue());
        e.setStatus(statusField.getText() != null ? statusField.getText().trim() : "Planifie");
        e.setImageEvent(imageField.getText() != null ? imageField.getText().trim() : "");
        e.setPrice(price);

        return e;
    }

    private boolean validateEventInputs() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert("⚠️ Title is required");
            return false;
        }

        if (locationField.getText().trim().isEmpty()) {
            showAlert("⚠️ Location is required");
            return false;
        }

        if (datePicker.getValue() == null) {
            showAlert("⚠️ Please select a date!");
            return false;
        }

        if (timeField.getText().trim().isEmpty()) {
            showAlert("⚠️ Time is required (HH:mm)!");
            return false;
        }

        try {
            LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showAlert("⚠️ Time must be like HH:mm (e.g., 19:30)!");
            return false;
        }

        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) {
                showAlert("⚠️ Capacity must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("⚠️ Capacity must be a number!");
            return false;
        }

        String priceText = priceField.getText().trim();
        if (!priceText.isEmpty()) {
            try {
                priceText = priceText.replace(',', '.');
                double price = Double.parseDouble(priceText);
                if (price < 0) {
                    showAlert("⚠️ Price cannot be negative");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("⚠️ Price must be a valid number!");
                return false;
            }
        }

        return true;
    }

    private void fillEventFields(Event e) {
        titleField.setText(e.getTitle());
        descriptionArea.setText(e.getDescription());
        locationField.setText(e.getLocation());

        if (e.getDateEvent() != null)
            datePicker.setValue(e.getDateEvent().toLocalDate());

        if (e.getStartTime() != null)
            timeField.setText(e.getStartTime().toString().substring(0, 5));

        capacityField.setText(String.valueOf(e.getCapacity()));
        priceField.setText(String.valueOf(e.getPrice()));
        seasonBox.setValue(e.getSeason());
        eventTypeBox.setValue(e.getEventType());
        statusField.setText(e.getStatus());
        imageField.setText(e.getImageEvent());

        if (e.getImageEvent() != null && !e.getImageEvent().isEmpty()) {
            try {
                File imageFile = new File("src/main/resources/" + e.getImageEvent());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), 80, 80, true, true);
                    eventImageView.setImage(image);
                    eventImagePathLabel.setText("Image: " + new File(e.getImageEvent()).getName());
                    eventImagePathLabel.setStyle("-fx-text-fill: #10B981;");
                }
            } catch (Exception ex) {
                eventImageView.setImage(null);
                eventImagePathLabel.setText("Image not found");
                eventImagePathLabel.setStyle("-fx-text-fill: #EF4444;");
            }
        } else {
            eventImageView.setImage(null);
            eventImagePathLabel.setText("No image");
            eventImagePathLabel.setStyle("-fx-text-fill: #64748B;");
        }
    }

    @FXML
    private void chooseImage() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Event Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
            );

            // Get window from multiple possible sources
            Window window = null;

            // Try stored primary window first
            if (primaryWindow != null) {
                window = primaryWindow;
            }
            // Try button's scene
            else if (chooseImageButton != null && chooseImageButton.getScene() != null) {
                window = chooseImageButton.getScene().getWindow();
            }
            // Try viewEvents scene
            else if (viewEvents != null && viewEvents.getScene() != null) {
                window = viewEvents.getScene().getWindow();
            }
            // Try any node in the scene
            else {
                // Try to get from any FXML field that might have a scene
                Node[] nodes = {btnDashboard, btnEvents, btnParticipations, eventTable};
                for (Node node : nodes) {
                    if (node != null && node.getScene() != null) {
                        window = node.getScene().getWindow();
                        break;
                    }
                }
            }

            if (window == null) {
                showAlert("Cannot open file chooser: No window available. Please try again.");
                return;
            }

            // Set initial directory to user's pictures folder
            String userHome = System.getProperty("user.home");
            File picturesDir = new File(userHome + "/Pictures");
            if (picturesDir.exists()) {
                fileChooser.setInitialDirectory(picturesDir);
            }

            File selectedFile = fileChooser.showOpenDialog(window);

            if (selectedFile != null) {
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());

                // Create directory if it doesn't exist
                File directory = new File(IMAGE_DIRECTORY);
                if (!directory.exists()) {
                    boolean created = directory.mkdirs();
                    System.out.println("Directory created: " + created);
                    if (!created) {
                        showAlert("Could not create image directory. Please check permissions.");
                        return;
                    }
                }

                // Generate unique filename
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destination = Paths.get(IMAGE_DIRECTORY + fileName);

                System.out.println("Destination: " + destination.toString());

                // Copy file
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Update fields
                selectedImagePath = "images/events/" + fileName;
                imageField.setText(selectedImagePath);
                eventImagePathLabel.setText("Image selected: " + fileName);
                eventImagePathLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");

                // Load and display image
                if (destination.toFile().exists()) {
                    Image image = new Image(destination.toUri().toString(), 80, 80, true, true);
                    eventImageView.setImage(image);
                    System.out.println("Image loaded successfully");
                    showInfo("Image uploaded successfully: " + fileName);
                } else {
                    System.err.println("Image file not found after copy");
                    showAlert("Image file not found after copy");
                }

            } else {
                System.out.println("No file selected");
            }

        } catch (Exception e) {
            System.err.println("Error in chooseImage: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error copying image: " + e.getMessage());
        }
    }

    private void loadEvents() {
        if (eventTable != null) {
            List<Event> events = eventCRUD.afficher();
            eventTable.setItems(FXCollections.observableArrayList(events));
            updateEventResultCount(events.size());

            if (eventCountBadge != null) {
                eventCountBadge.setText(String.valueOf(events.size()));
            }

            eventTable.refresh();
        }
    }

    private void loadEventSelector() {
        if (eventSelectorBox != null) {
            List<Event> events = eventCRUD.afficher();
            eventSelectorBox.setItems(FXCollections.observableArrayList(events));
            eventSelectorBox.setCellFactory(param -> new ListCell<Event>() {
                @Override
                protected void updateItem(Event event, boolean empty) {
                    super.updateItem(event, empty);
                    if (empty || event == null) {
                        setText(null);
                    } else {
                        setText(event.getIdEvent() + " - " + event.getTitle() +
                                " ($" + String.format("%.2f", event.getPrice()) + ") - " +
                                getEventBookings(event.getIdEvent()) + " bookings");
                    }
                }
            });
            eventSelectorBox.setButtonCell(new ListCell<Event>() {
                @Override
                protected void updateItem(Event event, boolean empty) {
                    super.updateItem(event, empty);
                    if (empty || event == null) {
                        setText(null);
                    } else {
                        setText(event.getIdEvent() + " - " + event.getTitle() +
                                " ($" + String.format("%.2f", event.getPrice()) + ") - " +
                                getEventBookings(event.getIdEvent()) + " bookings");
                    }
                }
            });
        }
    }

    private void updateEventResultCount(int count) {
        if (eventResultCountLabel != null) {
            eventResultCountLabel.setText(count + " event(s) found");
        }
    }

    // ==================== PARTICIPATION MANAGEMENT ====================

    @FXML
    public void initializeParticipationSection() {
        if (colPartId != null) colPartId.setCellValueFactory(new PropertyValueFactory<>("idParticipation"));
        if (colEvent != null) colEvent.setCellValueFactory(new PropertyValueFactory<>("idEvent"));
        if (colEventTitle != null) colEventTitle.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));

        if (colPartDate != null) {
            colPartDate.setCellValueFactory(new PropertyValueFactory<>("dateParticipation"));
            colPartDate.setCellFactory(column -> new TableCell<Participation, Date>() {
                @Override
                protected void updateItem(Date date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.toString());
                    }
                }
            });
        }

        if (colStatut != null) colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        if (colNom != null) colNom.setCellValueFactory(new PropertyValueFactory<>("nomParticipant"));
        if (colPrenom != null) colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenomParticipant"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("emailParticipant"));
        if (colPlaces != null) colPlaces.setCellValueFactory(new PropertyValueFactory<>("nombrePlaces"));
        if (colUserId != null) colUserId.setCellValueFactory(new PropertyValueFactory<>("idUser"));

        if (statutBox != null) {
            statutBox.getItems().addAll("Confirme", "En attente", "Annule");
            statutBox.setValue("Confirme");
        }
        if (searchTypeBox != null) {
            searchTypeBox.getItems().addAll("Par Email", "Par Nom", "Toutes");
            searchTypeBox.setValue("Par Email");
        }

        if (partDatePicker != null) partDatePicker.setValue(LocalDate.now());
        if (userIdField != null) userIdField.setText("1");

        if (participationTable != null) {
            participationTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) fillParticipationFields(newSelection);
                    }
            );
        }
    }

    @FXML
    private void addParticipation() {
        if (currentEventId == 0 && eventTable != null) {
            Event selected = eventTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                currentEventId = selected.getIdEvent();
            }
        }

        if (currentEventId == 0) {
            showAlert("⚠️ Please select an event first!");
            return;
        }

        if (!validateParticipationInputs()) return;

        try {
            Participation p = buildParticipationFromForm();

            int currentBookings = getEventBookings(currentEventId);
            Event event = eventCRUD.getById(currentEventId);
            if (event != null && currentBookings + p.getNombrePlaces() > event.getCapacity()) {
                showAlert("⚠️ Not enough spots available! Only " + (event.getCapacity() - currentBookings) + " spots left.");
                return;
            }

            participationCRUD.ajouter(p);

            Event mailEvent = eventCRUD.getById(currentEventId);
            MailService.sendParticipationConfirmation(p, mailEvent);

            refreshParticipations();
            clearParticipationFields();
            loadEvents();
            calculateStatistics();
            showInfo("✅ Participation added! Confirmation email sent to " + p.getEmailParticipant());

        } catch (NumberFormatException e) {
            showAlert("❌ User ID and places must be valid numbers!");
        } catch (Exception e) {
            showAlert("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void updateParticipation() {
        Participation selected = participationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("⚠️ Please select a participation to update!");
            return;
        }

        if (!validateParticipationInputs()) return;

        try {
            selected.setIdEvent(currentEventId);
            selected.setIdUser(Integer.parseInt(userIdField.getText().trim()));
            selected.setDateParticipation(Date.valueOf(partDatePicker.getValue()));
            selected.setStatut(statutBox.getValue());
            selected.setNomParticipant(nomField.getText().trim());
            selected.setPrenomParticipant(prenomField.getText().trim());
            selected.setEmailParticipant(emailField.getText().trim());
            selected.setTelephone(telephoneField.getText().trim());
            selected.setNombrePlaces(Integer.parseInt(placesField.getText().trim()));

            participationCRUD.modifier(selected);

            Event mailEventU = eventCRUD.getById(selected.getIdEvent());
            MailService.sendStatusUpdate(selected, mailEventU);

            refreshParticipations();
            clearParticipationFields();
            loadEvents();
            calculateStatistics();
            showInfo("✅ Participation updated! Update email sent to " + selected.getEmailParticipant());

        } catch (NumberFormatException e) {
            showAlert("❌ User ID and places must be valid numbers!");
        }
    }

    @FXML
    private void deleteParticipation() {
        Participation selected = participationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("⚠️ Please select a participation to delete!");
            return;
        }

        if (confirmAction("Delete Participation", "Are you sure you want to delete this participation?")) {
            Event mailEventD = eventCRUD.getById(selected.getIdEvent());
            MailService.sendParticipationCancellation(selected, mailEventD);

            participationCRUD.supprimer(selected.getIdParticipation());
            refreshParticipations();
            clearParticipationFields();
            loadEvents();
            calculateStatistics();
            showInfo("✅ Participation deleted! Cancellation email sent to " + selected.getEmailParticipant());
        }
    }

    @FXML
    private void loadParticipationsForSelectedEvent() {
        Event selectedEvent = eventSelectorBox.getValue();
        if (selectedEvent == null) {
            showAlert("⚠️ Please select an event!");
            return;
        }

        setEventId(selectedEvent.getIdEvent());
    }

    public void setEventId(int eventId) {
        this.currentEventId = eventId;
        if (eventIdLabel != null) {
            Event event = eventCRUD.getById(eventId);
            if (event != null) {
                int bookings = getEventBookings(eventId);
                double revenue = getEventRevenue(eventId);
                eventIdLabel.setText(event.getTitle() + " - " + bookings + " bookings ($" + String.format("%.2f", revenue) + ")");
            } else {
                eventIdLabel.setText("Event #" + eventId);
            }
            eventIdLabel.setStyle("-fx-text-fill: #6366F1; -fx-font-weight: 800;");
        }
        loadParticipationsForEvent();
    }

    private void loadParticipationsForEvent() {
        if (participationTable != null && currentEventId > 0) {
            List<Participation> participations = participationCRUD.afficherParEvent(currentEventId);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
        updateParticipationBadge();
    }

    private void loadAllParticipations() {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.afficherTous();
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
        updateParticipationBadge();
    }

    private Participation buildParticipationFromForm() {
        Participation p = new Participation();
        p.setIdEvent(currentEventId);
        p.setIdUser(Integer.parseInt(userIdField.getText().trim()));
        p.setDateParticipation(Date.valueOf(partDatePicker.getValue()));
        p.setStatut(statutBox.getValue());
        p.setNomParticipant(nomField.getText().trim());
        p.setPrenomParticipant(prenomField.getText().trim());
        p.setEmailParticipant(emailField.getText().trim());
        p.setTelephone(telephoneField.getText().trim());
        p.setNombrePlaces(Integer.parseInt(placesField.getText().trim()));
        return p;
    }

    private boolean validateParticipationInputs() {
        if (partDatePicker.getValue() == null) {
            showAlert("⚠️ Please select a date!");
            return false;
        }

        if (statutBox.getValue() == null) {
            showAlert("⚠️ Please select a status!");
            return false;
        }

        try {
            int userId = Integer.parseInt(userIdField.getText().trim());
            if (userId <= 0) {
                showAlert("⚠️ User ID must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("❌ User ID must be a valid number!");
            return false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            showAlert("⚠️ First name is required!");
            return false;
        }

        if (nomField.getText().trim().isEmpty()) {
            showAlert("⚠️ Last name is required!");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showAlert("⚠️ Email is required!");
            return false;
        }

        if (!emailField.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("⚠️ Please enter a valid email!");
            return false;
        }

        try {
            int places = Integer.parseInt(placesField.getText().trim());
            if (places <= 0) {
                showAlert("⚠️ Number of places must be greater than 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("❌ Number of places must be a valid number!");
            return false;
        }

        return true;
    }

    private void fillParticipationFields(Participation p) {
        if (partDatePicker != null) partDatePicker.setValue(p.getDateParticipation().toLocalDate());
        if (statutBox != null) statutBox.setValue(p.getStatut());
        if (userIdField != null) userIdField.setText(String.valueOf(p.getIdUser()));
        if (nomField != null) nomField.setText(p.getNomParticipant());
        if (prenomField != null) prenomField.setText(p.getPrenomParticipant());
        if (emailField != null) emailField.setText(p.getEmailParticipant());
        if (telephoneField != null) telephoneField.setText(p.getTelephone());
        if (placesField != null) placesField.setText(String.valueOf(p.getNombrePlaces()));
    }

    @FXML
    private void clearParticipationFields() {
        if (partDatePicker != null) partDatePicker.setValue(LocalDate.now());
        if (statutBox != null) statutBox.setValue("Confirme");
        if (userIdField != null) userIdField.setText("1");
        if (nomField != null) nomField.clear();
        if (prenomField != null) prenomField.clear();
        if (emailField != null) emailField.clear();
        if (telephoneField != null) telephoneField.clear();
        if (placesField != null) placesField.clear();
        if (participationTable != null) participationTable.getSelectionModel().clearSelection();
    }

    private void refreshParticipations() {
        if (currentEventId > 0) {
            loadParticipationsForEvent();
        } else {
            loadAllParticipations();
        }
        calculateStatistics();
        if (eventTable != null) {
            eventTable.refresh();
        }
    }

    private void updateResultCount(int count) {
        if (resultCountLabel != null) {
            resultCountLabel.setText(count + " participation(s) found");
        }
    }

    private void updateParticipationBadge() {
        if (participationCountBadge != null && participationTable != null) {
            participationCountBadge.setText(String.valueOf(participationTable.getItems().size()));
        }
    }

    // ==================== SEARCH METHODS ====================

    @FXML
    private void onSearchEvent() {
        if (globalSearchField == null) return;
        String term = globalSearchField.getText().trim();
        if (term.isEmpty()) loadEvents(); else searchEvents(term);
    }

    @FXML
    private void onClearEventSearch() {
        if (globalSearchField != null) globalSearchField.clear();
        loadEvents();
    }

    private void searchEvents(String term) {
        if (eventTable != null) {
            List<Event> events = eventCRUD.rechercherEvents(term);
            calculateStatistics();
            eventTable.setItems(FXCollections.observableArrayList(events));
            updateEventResultCount(events.size());
            eventTable.refresh();
        }
    }

    @FXML
    private void onSearch() {
        String searchTerm = participationsSearchField != null ? participationsSearchField.getText().trim() : "";
        String searchType = searchTypeBox != null ? searchTypeBox.getValue() : "Par Email";

        if (searchTerm.isEmpty()) {
            loadAllParticipations();
            return;
        }

        switch (searchType) {
            case "Par Email":
                searchParticipationsByEmail(searchTerm);
                break;
            case "Par Nom":
                String[] parts = searchTerm.split(" ");
                if (parts.length >= 2) {
                    searchParticipationsByName(parts[0], parts[1]);
                } else {
                    searchParticipationsGeneral(searchTerm);
                }
                break;
            default:
                searchParticipationsGeneral(searchTerm);
                break;
        }
    }

    private void searchParticipationsByEmail(String email) {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.afficherParClient(email);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
        updateParticipationBadge();
    }

    private void searchParticipationsByName(String nom, String prenom) {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.afficherParNomClient(nom, prenom);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
        updateParticipationBadge();
    }

    private void searchParticipationsGeneral(String term) {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.rechercherParticipations(term);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
        updateParticipationBadge();
    }

    @FXML
    private void onClearSearch() {
        if (participationsSearchField != null) participationsSearchField.clear();
        loadAllParticipations();
    }

    @FXML
    private void onRefresh() {
        onClearSearch();
    }

    // ==================== UTILITY METHODS ====================

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private boolean confirmAction(String title, String message) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(title);
        confirm.setContentText(message);
        return confirm.showAndWait().get() == ButtonType.OK;
    }
}