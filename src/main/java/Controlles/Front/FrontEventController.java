package Controlles.Front;

import Entites.Event;
import Entites.Participation;
import Services.EventCRUD;
import Services.ParticipationCRUD;
import Services.MailService;
import Services.TicketServer;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class FrontEventController implements Initializable {

    // ==================== FXML FIELDS ====================

    // Navigation
    @FXML private TabPane tabPane;
    @FXML private Tab homeTab;
    @FXML private Tab myTripsTab;

    // Event Cards Container
    @FXML private FlowPane eventCardsContainer;

    // Search & Filter
    @FXML private TextField globalSearchField;
    @FXML private Label eventResultCountLabel;
    @FXML private ComboBox<String> categoryFilterBox;
    @FXML private ComboBox<String> seasonFilterBox;

    // My Participations Table
    @FXML private TableView<Participation> participationTable;
    @FXML private TableColumn<Participation, Integer> colId;
    @FXML private TableColumn<Participation, String> colEventTitle;
    @FXML private TableColumn<Participation, Date> colDate;
    @FXML private TableColumn<Participation, String> colStatut;
    @FXML private TableColumn<Participation, String> colPrenom;
    @FXML private TableColumn<Participation, String> colNom;
    @FXML private TableColumn<Participation, String> colEmail;
    @FXML private TableColumn<Participation, Integer> colPlaces;

    // My Participations Search
    @FXML private TextField myTripsSearchField;
    @FXML private ComboBox<String> myTripsSearchType;
    @FXML private Label myTripsResultLabel;
    @FXML private Label clientInfoLabel;

    // Detail Views
    @FXML private ScrollPane detailScroll;
    @FXML private ScrollPane listScroll;
    @FXML private GridPane detailGrid;

    // Book Now Modal Overlay
    @FXML private VBox bookingOverlay;
    @FXML private Label bookingEventNameLabel;
    @FXML private Label bookingEventDateLabel;
    @FXML private Label bookingEventLocationLabel;
    @FXML private Label bookingEventPriceLabel;
    @FXML private Label bookingTotalLabel;

    // Modal form fields
    @FXML private TextField modalPrenomField;
    @FXML private TextField modalNomField;
    @FXML private TextField modalEmailField;
    @FXML private TextField modalTelephoneField;
    @FXML private TextField modalPlacesField;
    @FXML private DatePicker modalPartDatePicker;

    // REMOVED: Ticket Section Fields (ticketSection, ticketIdLabel, ticketEventLabel, etc.)

    // Dark Mode
    @FXML private HBox darkModeToggle;
    @FXML private Pane darkModeSlider;
    @FXML private Circle darkModeCircle;
    @FXML private StackPane rootPane;

    // ==================== SERVICES ====================

    private final EventCRUD eventCRUD = new EventCRUD();
    private final ParticipationCRUD participationCRUD = new ParticipationCRUD();
    private Event currentBookingEvent = null;
    private String currentClientEmail = "client@example.com"; // Default, would come from session
    private boolean isDarkMode = false;

    // Cache for statistics
    private final Map<Integer, Integer> eventBookings = new HashMap<>();

    // ==================== INITIALIZATION ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeEventSection();
        initializeMyParticipationsSection();
        initializeDarkMode();

        // Make sure Home tab is selected by default
        if (tabPane != null && homeTab != null) {
            tabPane.getSelectionModel().select(homeTab);
        }

        // Initialize filters
        if (categoryFilterBox != null) {
            categoryFilterBox.getItems().addAll("All", "Cultural", "Sport", "Music", "Business", "Food", "Art");
            categoryFilterBox.setValue("All");
            categoryFilterBox.setOnAction(e -> filterEvents());
        }

        if (seasonFilterBox != null) {
            seasonFilterBox.getItems().addAll("All", "Winter", "Spring", "Summer", "Autumn");
            seasonFilterBox.setValue("All");
            seasonFilterBox.setOnAction(e -> filterEvents());
        }

        // Start embedded ticket server
        TicketServer.start();

        // Set client info
        if (clientInfoLabel != null) {
            clientInfoLabel.setText("Client: " + currentClientEmail);
        }

        loadEvents();
        loadMyParticipations();
    }

    // ==================== PUBLIC METHODS FOR NAVIGATION ====================

    /**
     * Sets the event ID and switches to My Participations tab filtered by this event
     */
    public void setEventId(int eventId) {
        if (tabPane != null && myTripsTab != null) {
            tabPane.getSelectionModel().select(myTripsTab);
            loadMyParticipations();

            // Optional: highlight the specific event in the table
            participationTable.getItems().stream()
                    .filter(p -> p.getIdEvent() == eventId)
                    .findFirst()
                    .ifPresent(p -> participationTable.getSelectionModel().select(p));
        }
    }

    /**
     * Sets the client email and loads their participations
     */
    public void setClientEmail(String email) {
        this.currentClientEmail = email;
        if (clientInfoLabel != null) {
            clientInfoLabel.setText("Client: " + email);
        }
        if (tabPane != null && myTripsTab != null) {
            tabPane.getSelectionModel().select(myTripsTab);
        }
        loadMyParticipations();
    }

    // ==================== EVENT SECTION ====================

    @FXML
    public void initializeEventSection() {
        loadEvents();
    }

    private void loadEvents() {
        List<Event> events = eventCRUD.afficher();
        calculateBookings();
        displayEventCards(events);
        updateEventResultCount(events.size());
    }

    private void calculateBookings() {
        eventBookings.clear();
        List<Participation> allParticipations = participationCRUD.afficherTous();

        for (Participation p : allParticipations) {
            int eventId = p.getIdEvent();
            int places = p.getNombrePlaces();
            eventBookings.put(eventId, eventBookings.getOrDefault(eventId, 0) + places);
        }
    }

    private int getEventBookings(int eventId) {
        return eventBookings.getOrDefault(eventId, 0);
    }

    private void filterEvents() {
        String category = categoryFilterBox.getValue();
        String season = seasonFilterBox.getValue();
        String searchTerm = globalSearchField != null ? globalSearchField.getText().trim().toLowerCase() : "";

        List<Event> allEvents = eventCRUD.afficher();
        List<Event> filteredEvents = new ArrayList<>();

        for (Event event : allEvents) {
            boolean matchesCategory = category.equals("All") || category.equals(event.getEventType());
            boolean matchesSeason = season.equals("All") || season.equals(event.getSeason());
            boolean matchesSearch = searchTerm.isEmpty() ||
                    event.getTitle().toLowerCase().contains(searchTerm) ||
                    (event.getLocation() != null && event.getLocation().toLowerCase().contains(searchTerm)) ||
                    (event.getDescription() != null && event.getDescription().toLowerCase().contains(searchTerm));

            if (matchesCategory && matchesSeason && matchesSearch) {
                filteredEvents.add(event);
            }
        }

        displayEventCards(filteredEvents);
        updateEventResultCount(filteredEvents.size());
    }

    @FXML
    private void onSearchEvent() {
        filterEvents();
    }

    @FXML
    private void onClearEventSearch() {
        if (globalSearchField != null) globalSearchField.clear();
        if (categoryFilterBox != null) categoryFilterBox.setValue("All");
        if (seasonFilterBox != null) seasonFilterBox.setValue("All");
        filterEvents();
    }

    private void updateEventResultCount(int count) {
        if (eventResultCountLabel != null) {
            eventResultCountLabel.setText(count + " event(s) found");
        }
    }

    private void displayEventCards(List<Event> events) {
        if (eventCardsContainer == null) return;

        eventCardsContainer.getChildren().clear();

        for (Event event : events) {
            VBox card = createEventCard(event);
            eventCardsContainer.getChildren().add(card);
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);

        String cardStyle = isDarkMode ?
                "-fx-background-color: #16213e; -fx-background-radius: 20; -fx-padding: 15; -fx-cursor: hand; -fx-border-color: #0f3460; -fx-border-radius: 20;" :
                "-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 10, 0, 0, 5);";
        card.setStyle(cardStyle);
        card.setEffect(new DropShadow(5, 0, 5, isDarkMode ? Color.rgb(0,0,0,0.5) : Color.rgb(0,0,0,0.05)));

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(160);
        imageContainer.setStyle("-fx-background-radius: 15; -fx-background-color: " + (isDarkMode ? "#0f3460" : "#F1F5F9") + ";");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(160);
        imageView.setFitWidth(280);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 15;");

        boolean imageLoaded = false;
        if (event.getImageEvent() != null && !event.getImageEvent().isEmpty()) {
            try {
                File imageFile = new File("src/main/resources/" + event.getImageEvent());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), 280, 160, true, true);
                    imageView.setImage(image);
                    imageLoaded = true;
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }

        if (imageLoaded) {
            imageContainer.getChildren().add(imageView);
        } else {
            Label placeholder = new Label("No Image");
            placeholder.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (isDarkMode ? "#4a5568" : "#94A3B8") + ";");
            imageContainer.getChildren().add(placeholder);
        }

        Label typeBadge = new Label(event.getEventType() != null ? event.getEventType() : "Event");
        typeBadge.setStyle("-fx-background-color: #667EEA; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: 800; -fx-background-radius: 999; -fx-padding: 3 10;");
        typeBadge.setTranslateX(10);
        typeBadge.setTranslateY(-70);
        typeBadge.setAlignment(Pos.TOP_LEFT);
        imageContainer.getChildren().add(typeBadge);

        VBox details = new VBox(8);
        details.setPadding(new Insets(10, 0, 0, 0));

        String titleColor = isDarkMode ? "white" : "#1E293B";
        String textColor = isDarkMode ? "#a0a0a0" : "#64748B";
        String priceColor = isDarkMode ? "#f97316" : "#C2410C";
        String statColor = isDarkMode ? "#60a5fa" : "#667EEA";
        String availableColor = isDarkMode ? "#34d399" : "#10B981";

        Label titleLabel = new Label(event.getTitle());
        titleLabel.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-weight: 900; -fx-font-size: 16px;");
        titleLabel.setWrapText(true);

        HBox locationBox = new HBox(5);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        locationIcon.setStyle("-fx-font-size: 11px;");
        Label locationLabel = new Label(event.getLocation() != null ? event.getLocation() : "Location TBD");
        locationLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 12px;");
        locationBox.getChildren().addAll(locationIcon, locationLabel);

        HBox dateBox = new HBox(5);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = new Label("📅");
        dateIcon.setStyle("-fx-font-size: 11px;");
        Label dateLabel = new Label(event.getDateEvent() != null ? event.getDateEvent().toString() : "Date TBD");
        dateLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 12px;");
        dateBox.getChildren().addAll(dateIcon, dateLabel);

        int bookings = getEventBookings(event.getIdEvent());
        int availableSpots = event.getCapacity() - bookings;

        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(5, 0, 5, 0));
        Label bookingsLabel = new Label(bookings + " booked");
        bookingsLabel.setStyle("-fx-text-fill: " + statColor + "; -fx-font-size: 11px; -fx-font-weight: 700;");
        statsBox.getChildren().add(bookingsLabel);

        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label("$" + String.format("%.2f", event.getPrice()));
        priceLabel.setStyle("-fx-text-fill: " + priceColor + "; -fx-font-weight: 900; -fx-font-size: 18px;");
        Label availableLabel = new Label(Math.max(0, availableSpots) + " left");
        availableLabel.setStyle("-fx-text-fill: " + availableColor + "; -fx-font-size: 12px; -fx-font-weight: 700;");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        priceBox.getChildren().addAll(priceLabel, spacer2, availableLabel);

        ProgressBar capacityBar = new ProgressBar(event.getCapacity() > 0 ? (double) bookings / event.getCapacity() : 0);
        capacityBar.setPrefWidth(250);
        capacityBar.setPrefHeight(6);
        capacityBar.setStyle("-fx-accent: #667EEA;");

        HBox progressBox = new HBox(5);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        Label fillRateLabel = new Label("Fill rate:");
        fillRateLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 11px;");
        progressBox.getChildren().addAll(fillRateLabel, capacityBar);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(5, 0, 0, 0));

        Button participateBtn = new Button("Book Now");
        participateBtn.setStyle("-fx-background-color: #667EEA; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 30; -fx-padding: 10 25; -fx-font-size: 12px; -fx-cursor: hand;");
        participateBtn.setOnAction(e -> openBookingOverlay(event));

        Button reviewBtn = new Button("Details");
        reviewBtn.setStyle(isDarkMode
                ? "-fx-background-color: #0f3460; -fx-text-fill: #a0a0a0; -fx-font-weight: 800; -fx-background-radius: 30; -fx-padding: 10 25; -fx-font-size: 12px; -fx-cursor: hand;"
                : "-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-font-weight: 800; -fx-background-radius: 30; -fx-padding: 10 25; -fx-font-size: 12px; -fx-cursor: hand;");
        reviewBtn.setOnAction(e -> showEventDetails(event));

        actions.getChildren().addAll(participateBtn, reviewBtn);

        details.getChildren().addAll(titleLabel, locationBox, dateBox, statsBox, priceBox, progressBox, actions);
        card.getChildren().addAll(imageContainer, details);

        return card;
    }

    private void showEventDetails(Event event) {
        int bookings = getEventBookings(event.getIdEvent());
        int availableSpots = event.getCapacity() - bookings;

        String details = String.format(
                "📋 EVENT DETAILS\n\n" +
                        "Title:       %s\n" +
                        "Location:    %s\n" +
                        "Date:        %s\n" +
                        "Time:        %s\n" +
                        "Type:        %s\n" +
                        "Season:      %s\n" +
                        "Price:       $%.2f\n" +
                        "Capacity:    %d\n" +
                        "Booked:      %d\n" +
                        "Available:   %d\n" +
                        "Status:      %s\n\n" +
                        "─────────────────────────────\n" +
                        "📝 DESCRIPTION\n" +
                        "─────────────────────────────\n" +
                        "%s",
                event.getTitle(),
                event.getLocation(),
                event.getDateEvent() != null ? event.getDateEvent().toString() : "TBD",
                event.getStartTime() != null ? event.getStartTime().toString().substring(0, 5) : "TBD",
                event.getEventType(),
                event.getSeason(),
                event.getPrice(),
                event.getCapacity(),
                bookings,
                Math.max(0, availableSpots),
                event.getStatus(),
                event.getDescription() != null ? event.getDescription() : "No description"
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Event Details — " + event.getTitle());
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        textArea.setPrefWidth(480);
        textArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");

        if (isDarkMode) {
            alert.getDialogPane().setStyle("-fx-background-color: #16213e;");
            textArea.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-control-inner-background: #0f3460; -fx-font-family: 'Monospaced';");
        }

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    // ==================== BOOKING MODAL METHODS ====================

    @FXML
    public void openBookingOverlay(Event event) {
        if (bookingOverlay == null || event == null) return;

        currentBookingEvent = event;

        if (bookingEventNameLabel != null)
            bookingEventNameLabel.setText(event.getTitle());
        if (bookingEventDateLabel != null)
            bookingEventDateLabel.setText("📅  " + (event.getDateEvent() != null ? event.getDateEvent().toString() : "TBD") + "  " +
                    (event.getStartTime() != null ? event.getStartTime().toString().substring(0,5) : ""));
        if (bookingEventLocationLabel != null)
            bookingEventLocationLabel.setText("📍  " + (event.getLocation() != null ? event.getLocation() : "TBD"));
        if (bookingEventPriceLabel != null)
            bookingEventPriceLabel.setText(String.format("$%.2f", event.getPrice()));

        if (modalPrenomField != null) modalPrenomField.clear();
        if (modalNomField != null) modalNomField.clear();
        if (modalEmailField != null) modalEmailField.clear();
        if (modalTelephoneField != null) modalTelephoneField.clear();
        if (modalPlacesField != null) {
            modalPlacesField.setText("1");
            updateBookingTotal();
        }
        if (modalPartDatePicker != null) modalPartDatePicker.setValue(LocalDate.now());
        if (bookingTotalLabel != null) bookingTotalLabel.setText(String.format("$%.2f", event.getPrice()));

        if (modalPlacesField != null)
            modalPlacesField.textProperty().addListener((obs, o, n) -> updateBookingTotal());

        bookingOverlay.setVisible(true);
        bookingOverlay.setManaged(true);
    }

    @FXML
    private void closeBookingOverlay() {
        if (bookingOverlay != null) {
            bookingOverlay.setVisible(false);
            bookingOverlay.setManaged(false);
        }
        currentBookingEvent = null;
    }

    @FXML
    private void confirmBooking() {
        if (currentBookingEvent == null) {
            closeBookingOverlay();
            return;
        }

        Event bookingEvent = currentBookingEvent;
        int eventId = bookingEvent.getIdEvent();

        // Validation
        String prenom = modalPrenomField != null ? modalPrenomField.getText().trim() : "";
        String nom = modalNomField != null ? modalNomField.getText().trim() : "";
        String email = modalEmailField != null ? modalEmailField.getText().trim() : "";
        String tel = modalTelephoneField != null ? modalTelephoneField.getText().trim() : "";
        String places = modalPlacesField != null ? modalPlacesField.getText().trim() : "1";

        if (prenom.isEmpty()) { showAlert("⚠️ First name is required!"); return; }
        if (nom.isEmpty()) { showAlert("⚠️ Last name is required!"); return; }
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("⚠️ A valid email is required!");
            return;
        }
        if (modalPartDatePicker == null || modalPartDatePicker.getValue() == null) {
            showAlert("⚠️ Please select a booking date!");
            return;
        }

        int nbPlaces;
        try {
            nbPlaces = Integer.parseInt(places);
            if (nbPlaces <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showAlert("⚠️ Number of seats must be a positive number!");
            return;
        }

        int booked = getEventBookings(eventId);
        if (booked + nbPlaces > bookingEvent.getCapacity()) {
            showAlert("⚠️ Not enough spots! Only " + (bookingEvent.getCapacity() - booked) + " left.");
            return;
        }

        // Save participation
        try {
            Participation p = new Participation();
            p.setIdEvent(eventId);
            p.setIdUser(1); // Default user
            p.setDateParticipation(Date.valueOf(modalPartDatePicker.getValue()));
            p.setStatut("Confirme");
            p.setNomParticipant(nom);
            p.setPrenomParticipant(prenom);
            p.setEmailParticipant(email);
            p.setTelephone(tel);
            p.setNombrePlaces(nbPlaces);

            participationCRUD.ajouter(p);

            // Send confirmation email with ticket
            MailService.sendParticipationConfirmation(p, bookingEvent);

            // Refresh data
            calculateBookings();
            loadEvents();
            loadMyParticipations();

            // Show success message and close overlay
            showInfo("✅ Booking confirmed! A confirmation email with your ticket has been sent to " + email);
            closeBookingOverlay();

        } catch (Exception ex) {
            showAlert("❌ Error saving booking: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateBookingTotal() {
        if (currentBookingEvent == null || bookingTotalLabel == null || modalPlacesField == null) return;
        try {
            int n = Integer.parseInt(modalPlacesField.getText().trim());
            bookingTotalLabel.setText(String.format("$%.2f", currentBookingEvent.getPrice() * n));
        } catch (NumberFormatException ignored) {
            bookingTotalLabel.setText("$—");
        }
    }

    // ==================== MY PARTICIPATIONS SECTION ====================

    @FXML
    public void initializeMyParticipationsSection() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("idParticipation"));
        if (colEventTitle != null) colEventTitle.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
        if (colDate != null) {
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateParticipation"));
            colDate.setCellFactory(column -> new TableCell<Participation, Date>() {
                @Override
                protected void updateItem(Date date, boolean empty) {
                    super.updateItem(date, empty);
                    setText(empty || date == null ? null : date.toString());
                }
            });
        }
        if (colStatut != null) colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        if (colPrenom != null) colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenomParticipant"));
        if (colNom != null) colNom.setCellValueFactory(new PropertyValueFactory<>("nomParticipant"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("emailParticipant"));
        if (colPlaces != null) colPlaces.setCellValueFactory(new PropertyValueFactory<>("nombrePlaces"));

        if (myTripsSearchType != null) {
            myTripsSearchType.getItems().addAll("Par Email", "Par Nom", "Toutes");
            myTripsSearchType.setValue("Par Email");
        }

        // Connect table selection to detail view (now just shows details, no QR)
        if (participationTable != null) {
            participationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showParticipationDetails(newSelection);
                }
            });
        }

        loadMyParticipations();
    }

    private void loadMyParticipations() {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.afficherParClient(currentClientEmail);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            if (myTripsResultLabel != null) {
                myTripsResultLabel.setText(participations.size() + " participation(s) found");
            }
        }
    }

    @FXML
    private void onSearchMyTrips() {
        String searchTerm = myTripsSearchField != null ? myTripsSearchField.getText().trim() : "";
        String searchType = myTripsSearchType != null ? myTripsSearchType.getValue() : "Par Email";

        if (searchTerm.isEmpty()) {
            loadMyParticipations();
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
            if (myTripsResultLabel != null) {
                myTripsResultLabel.setText(participations.size() + " participation(s) found");
            }
        }
    }

    private void searchParticipationsByName(String nom, String prenom) {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.afficherParNomClient(nom, prenom);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            if (myTripsResultLabel != null) {
                myTripsResultLabel.setText(participations.size() + " participation(s) found");
            }
        }
    }

    private void searchParticipationsGeneral(String term) {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.rechercherParticipations(term);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            if (myTripsResultLabel != null) {
                myTripsResultLabel.setText(participations.size() + " participation(s) found");
            }
        }
    }

    @FXML
    private void onClearMyTripsSearch() {
        if (myTripsSearchField != null) myTripsSearchField.clear();
        loadMyParticipations();
    }

    @FXML
    private void onRefreshMyTrips() {
        onClearMyTripsSearch();
    }

    // ==================== DETAIL VIEW METHODS ====================

    private void showParticipationDetails(Participation p) {
        Event event = eventCRUD.getById(p.getIdEvent());
        double price = event != null ? event.getPrice() : 0.0;
        double totalPrice = price * p.getNombrePlaces();

        String details = String.format(
                "👤 PARTICIPATION DETAILS\n\n" +
                        "ID: %d\n" +
                        "Event: %s\n" +
                        "Date: %s\n" +
                        "Status: %s\n" +
                        "Name: %s %s\n" +
                        "Email: %s\n" +
                        "Phone: %s\n" +
                        "Places: %d\n" +
                        "Price per person: $%.2f\n" +
                        "Total Amount: $%.2f\n\n" +
                        "📧 A confirmation email with your ticket has been sent to your email address.",
                p.getIdParticipation(),
                p.getEventTitle() != null ? p.getEventTitle() : "N/A",
                p.getDateParticipation(),
                p.getStatut(),
                p.getPrenomParticipant(),
                p.getNomParticipant(),
                p.getEmailParticipant(),
                p.getTelephone() != null ? p.getTelephone() : "Not provided",
                p.getNombrePlaces(),
                price,
                totalPrice
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Participation Details");
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(18);
        textArea.setPrefWidth(450);
        textArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");

        if (isDarkMode) {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #16213e;");
            textArea.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-control-inner-background: #0f3460; -fx-font-family: 'Monospaced';");
        }

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void viewParticipationDetails() {
        Participation selected = participationTable != null ? participationTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showAlert("⚠️ Please select a participation!");
            return;
        }
        showParticipationDetails(selected);
    }

    // REMOVED: displayParticipationDetails method (no longer needed)
    // REMOVED: backToList method (no longer needed)

    // ==================== NAVIGATION METHODS ====================

    @FXML
    private void goToHome() {
        if (tabPane != null && homeTab != null) {
            tabPane.getSelectionModel().select(homeTab);
            loadEvents();
        }
    }

    @FXML
    private void goToMesParticipations() {
        if (tabPane != null && myTripsTab != null) {
            tabPane.getSelectionModel().select(myTripsTab);
            loadMyParticipations();
        }
    }

    @FXML
    private void goToBackOffice() {
        try {
            // Open BackEvent.fxml in a new window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackEvent.fxml"));
            Stage stage = new Stage();
            stage.setTitle("WinGO - Back Office");
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Could not open Back Office: " + e.getMessage());
        }
    }

    // ==================== DARK MODE METHODS ====================

    @FXML
    public void initializeDarkMode() {
        if (darkModeToggle != null) {
            darkModeToggle.setOnMouseClicked(e -> toggleDarkMode());
        }
    }

    @FXML
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;

        if (darkModeSlider != null) {
            darkModeSlider.getChildren().clear();

            Circle circle = new Circle();
            circle.setRadius(10);
            circle.setFill(Color.WHITE);

            if (isDarkMode) {
                circle.setCenterX(36);
                circle.setCenterY(12);
                darkModeSlider.setStyle("-fx-background-color: #8B5CF6; -fx-background-radius: 30;");
            } else {
                circle.setCenterX(12);
                circle.setCenterY(12);
                darkModeSlider.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 30;");
            }

            darkModeSlider.getChildren().add(circle);
            darkModeCircle = circle;
        }

        applyTheme();
        loadEvents(); // Refresh cards with new theme
    }

    private void applyTheme() {
        if (isDarkMode) {
            applyDarkMode();
        } else {
            applyLightMode();
        }
    }

    private void applyDarkMode() {
        if (rootPane != null) rootPane.setStyle("-fx-background-color: #1a1a2e;");
        // Additional dark mode styling can be added here
    }

    private void applyLightMode() {
        if (rootPane != null) rootPane.setStyle("-fx-background-color: #F8FAFC;");
        // Additional light mode styling can be added here
    }

    // ==================== UTILITY METHODS ====================

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setTitle("Error");
        alert.setHeaderText(null);

        if (isDarkMode) {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #16213e;");
        }

        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setTitle("Success");
        alert.setHeaderText(null);

        if (isDarkMode) {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #16213e;");
        }

        alert.showAndWait();
    }
}