package Controlles;

import Entites.Event;
import Entites.Participation;
import Services.EventCRUD;
import Services.ParticipationCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

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

public class EventController implements Initializable {

    // ==================== FXML FIELDS ====================

    // TabPane and Tabs
    @FXML private TabPane tabPane;
    @FXML private Tab homeTab;
    @FXML private Tab eventsTab;
    @FXML private Tab participationsTab;
    @FXML private Tab myTripsTab;

    // Statistics Dashboard
    @FXML private VBox statisticsDashboard;
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

    // Image Selection
    @FXML private ImageView eventImageView;
    @FXML private Label eventImagePathLabel;
    @FXML private Button chooseImageButton;

    // Event Cards Container
    @FXML private FlowPane eventCardsContainer;

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

    // Event Selection
    @FXML private Label selectedEventLabel;
    @FXML private ComboBox<Event> eventSelectorBox;

    // Statistics Labels
    @FXML private Label totalEventsLabel;
    @FXML private Label totalParticipationsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label averagePriceLabel;

    // Participation Form Fields
    @FXML private Label eventIdLabel;
    @FXML private TextField userIdField;
    @FXML private DatePicker partDatePicker;
    @FXML private ComboBox<String> statutBox;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField placesField;

    // Participation Table
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

    // My Participations Table
    @FXML private TableView<Participation> participationTable2;
    @FXML private TableColumn<Participation, Integer> colId2;
    @FXML private TableColumn<Participation, String> colEventTitle2;
    @FXML private TableColumn<Participation, Date> colDate2;
    @FXML private TableColumn<Participation, String> colStatut2;
    @FXML private TableColumn<Participation, String> colPrenom2;
    @FXML private TableColumn<Participation, String> colNom2;
    @FXML private TableColumn<Participation, String> colEmail2;
    @FXML private TableColumn<Participation, Integer> colPlaces2;

    // Search Components
    @FXML private TextField globalSearchField;
    @FXML private Label eventResultCountLabel;
    @FXML private TextField participationsSearchField;
    @FXML private ComboBox<String> searchTypeBox;
    @FXML private Label resultCountLabel;
    @FXML private Label clientInfoLabel;

    // My Trips Search Components
    @FXML private ComboBox<String> myTripsSearchType;
    @FXML private TextField myTripsSearchField;
    @FXML private Label myTripsResultLabel;

    // Dark Mode Components
    @FXML private HBox darkModeToggle;
    @FXML private Pane darkModeSlider;
    @FXML private Circle darkModeCircle;
    @FXML private StackPane rootPane;
    @FXML private HBox mainContainer;
    @FXML private VBox sidebar;
    @FXML private VBox mainContent;
    @FXML private HBox topBar;
    @FXML private HBox searchBar;
    @FXML private HBox userProfile;
    @FXML private VBox statisticsPanel;

    // ==================== SERVICES ====================

    private final EventCRUD eventCRUD = new EventCRUD();
    private final ParticipationCRUD participationCRUD = new ParticipationCRUD();
    private int currentEventId = 0;
    private String currentClientEmail = "";
    private String selectedImagePath = "";
    private final String IMAGE_DIRECTORY = "src/main/resources/images/events/";
    private boolean isDarkMode = false;

    // Cache for statistics
    private final Map<Integer, Integer> eventBookings = new HashMap<>();
    private final Map<Integer, Double> eventRevenue = new HashMap<>();

    // ==================== INITIALIZATION ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeEventSection();
        initializeParticipationSection();
        initializeStatisticsSection();
        initializeDarkMode();

        // Make sure Home tab is selected by default
        if (tabPane != null && homeTab != null) {
            tabPane.getSelectionModel().select(homeTab);
        }

        // Create image directory if it doesn't exist
        File directory = new File(IMAGE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @FXML
    public void initializeEventSection() {
        // Initialize event table columns
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("idEvent"));
        if (colTitle != null) colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colLocation != null) colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        // Setup price column with proper formatting
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

        // Setup bookings column
        if (colBookings != null) {
            colBookings.setCellValueFactory(cellData -> {
                Event event = cellData.getValue();
                return new javafx.beans.property.SimpleIntegerProperty(getEventBookings(event.getIdEvent())).asObject();
            });
        }

        // Setup revenue column
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

        // Handle Date column
        if (colDate != null) {
            colDate.setCellValueFactory(cellData -> {
                Date date = cellData.getValue().getDateEvent();
                return new javafx.beans.property.SimpleStringProperty(
                        date != null ? date.toString() : ""
                );
            });
        }

        // Handle Time column
        if (colTime != null) {
            colTime.setCellValueFactory(cellData -> {
                Time time = cellData.getValue().getStartTime();
                return new javafx.beans.property.SimpleStringProperty(
                        time != null ? time.toString().substring(0, 5) : ""
                );
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
        if (statusField != null) {
            statusField.setText("Planifié");
        }
        if (priceField != null) {
            priceField.setText("0.00");
        }

        // Load events
        loadEvents();

        // Add table selection listener
        if (eventTable != null) {
            eventTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            fillEventFields(newVal);
                            if (selectedEventLabel != null) {
                                selectedEventLabel.setText("Selected: " + newVal.getTitle() +
                                        " ($" + String.format("%.2f", newVal.getPrice()) +
                                        ") - " + getEventBookings(newVal.getIdEvent()) + " bookings");
                            }
                        }
                    }
            );
        }

        // Initialize event selector
        if (eventSelectorBox != null) {
            loadEventSelector();
        }
    }

    @FXML
    public void initializeParticipationSection() {
        // Initialize participation table columns
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

        // Initialize combo boxes
        if (statutBox != null) {
            statutBox.getItems().addAll("Confirmé", "En attente", "Annulé");
            statutBox.setValue("Confirmé");
        }
        if (searchTypeBox != null) {
            searchTypeBox.getItems().addAll("Par Email", "Par Nom", "Toutes");
            searchTypeBox.setValue("Par Email");
        }
        if (myTripsSearchType != null) {
            myTripsSearchType.getItems().addAll("Par Email", "Par Nom", "Toutes");
            myTripsSearchType.setValue("Par Email");
        }

        // Set default values
        if (partDatePicker != null) partDatePicker.setValue(LocalDate.now());
        if (userIdField != null) userIdField.setText("1");

        // Add table selection listener
        if (participationTable != null) {
            participationTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) fillParticipationFields(newSelection);
                    }
            );
        }

        // Load participations
        loadAllParticipations();

        // Initialize My Participations table
        if (colId2 != null) colId2.setCellValueFactory(new PropertyValueFactory<>("idParticipation"));
        if (colEventTitle2 != null) colEventTitle2.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
        if (colDate2 != null) {
            colDate2.setCellValueFactory(new PropertyValueFactory<>("dateParticipation"));
            colDate2.setCellFactory(column -> new TableCell<Participation, Date>() {
                @Override
                protected void updateItem(Date date, boolean empty) {
                    super.updateItem(date, empty);
                    setText(empty || date == null ? null : date.toString());
                }
            });
        }
        if (colStatut2 != null) colStatut2.setCellValueFactory(new PropertyValueFactory<>("statut"));
        if (colPrenom2 != null) colPrenom2.setCellValueFactory(new PropertyValueFactory<>("prenomParticipant"));
        if (colNom2 != null) colNom2.setCellValueFactory(new PropertyValueFactory<>("nomParticipant"));
        if (colEmail2 != null) colEmail2.setCellValueFactory(new PropertyValueFactory<>("emailParticipant"));
        if (colPlaces2 != null) colPlaces2.setCellValueFactory(new PropertyValueFactory<>("nombrePlaces"));

        loadAllParticipationsIntoTable2();
    }

    @FXML
    public void initializeStatisticsSection() {
        // Initialize top events table columns
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
    public void initializeDarkMode() {
        // Add click handler to dark mode toggle
        if (darkModeToggle != null) {
            darkModeToggle.setOnMouseClicked(e -> toggleDarkMode());
        }
    }

    // ==================== DARK MODE METHODS ====================

    @FXML
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;

        // Update the slider position
        if (darkModeSlider != null) {
            darkModeSlider.getChildren().clear();

            Circle circle = new Circle();
            circle.setRadius(7);
            circle.setFill(Color.WHITE);

            if (isDarkMode) {
                // Dark mode - circle on the right
                circle.setCenterX(28);
                circle.setCenterY(9);
                darkModeSlider.setStyle("-fx-background-color: #8B5CF6; -fx-background-radius: 10;");
            } else {
                // Light mode - circle on the left
                circle.setCenterX(7);
                circle.setCenterY(9);
                darkModeSlider.setStyle("-fx-background-color: #6366F1; -fx-background-radius: 10;");
            }

            darkModeSlider.getChildren().add(circle);
            darkModeCircle = circle;
        }

        // Apply dark/light mode styles
        applyTheme();
    }

    private void applyTheme() {
        if (isDarkMode) {
            applyDarkMode();
        } else {
            applyLightMode();
        }
    }

    private void applyDarkMode() {
        // Root and main container
        if (rootPane != null) {
            rootPane.setStyle("-fx-background-color: #1a1a2e;");
        }

        // Sidebar
        if (sidebar != null) {
            sidebar.setStyle("-fx-background-color: #16213e; -fx-border-color: #0f3460; -fx-border-width: 0 1 0 0;");
        }

        // Main content
        if (mainContent != null) {
            mainContent.setStyle("-fx-background-color: #1a1a2e;");
        }

        // Top bar
        if (topBar != null) {
            topBar.setStyle("-fx-background-color: #16213e; -fx-background-radius: 20;");
        }

        // Search bar
        if (searchBar != null) {
            searchBar.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 15;");
        }

        // User profile
        if (userProfile != null) {
            userProfile.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 30;");
        }

        // Statistics panel
        if (statisticsPanel != null) {
            statisticsPanel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 15;");
        }

        // Statistics dashboard
        if (statisticsDashboard != null) {
            statisticsDashboard.setStyle("-fx-background-color: #16213e; -fx-background-radius: 25;");
        }

        // Update all labels and text colors
        updateTextColorsForDarkMode();

        // Update tab pane
        if (tabPane != null) {
            tabPane.setStyle("-fx-background-color: transparent;");
        }

        // Update tables
        updateTableStylesForDarkMode();

        // Update form fields
        updateFormStylesForDarkMode();
    }

    private void applyLightMode() {
        // Root and main container
        if (rootPane != null) {
            rootPane.setStyle("-fx-background-color: #F8F9FA;");
        }

        // Sidebar
        if (sidebar != null) {
            sidebar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 0 0;");
        }

        // Main content
        if (mainContent != null) {
            mainContent.setStyle("-fx-background-color: #F8F9FA;");
        }

        // Top bar
        if (topBar != null) {
            topBar.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 20;");
        }

        // Search bar
        if (searchBar != null) {
            searchBar.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 15;");
        }

        // User profile
        if (userProfile != null) {
            userProfile.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 30;");
        }

        // Statistics panel
        if (statisticsPanel != null) {
            statisticsPanel.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;");
        }

        // Statistics dashboard
        if (statisticsDashboard != null) {
            statisticsDashboard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 25;");
        }

        // Update all labels and text colors
        updateTextColorsForLightMode();

        // Update tab pane
        if (tabPane != null) {
            tabPane.setStyle("-fx-background-color: transparent;");
        }

        // Update tables
        updateTableStylesForLightMode();

        // Update form fields
        updateFormStylesForLightMode();
    }

    private void updateTextColorsForDarkMode() {
        String darkTextStyle = "-fx-text-fill: #e0e0e0;";
        String darkSecondaryTextStyle = "-fx-text-fill: #a0a0a0;";
        String darkHeadingStyle = "-fx-text-fill: white; -fx-font-weight: 900;";

        // Update sidebar navigation labels
        updateLabelsInContainer(sidebar, darkTextStyle, darkSecondaryTextStyle, darkHeadingStyle);

        // Update main content labels
        updateLabelsInContainer(mainContent, darkTextStyle, darkSecondaryTextStyle, darkHeadingStyle);

        // Update statistics labels
        if (totalEventsLabel != null) totalEventsLabel.setStyle(darkHeadingStyle);
        if (totalParticipationsLabel != null) totalParticipationsLabel.setStyle(darkHeadingStyle);
        if (totalRevenueLabel != null) totalRevenueLabel.setStyle(darkHeadingStyle);
        if (averagePriceLabel != null) averagePriceLabel.setStyle(darkHeadingStyle);
        if (eventResultCountLabel != null) eventResultCountLabel.setStyle("-fx-text-fill: #8b5cf6;");
        if (selectedEventLabel != null) selectedEventLabel.setStyle("-fx-text-fill: #8b5cf6;");
    }

    private void updateTextColorsForLightMode() {
        String lightTextStyle = "-fx-text-fill: #1E293B;";
        String lightSecondaryStyle = "-fx-text-fill: #64748B;";
        String lightHeadingStyle = "-fx-text-fill: #1E293B; -fx-font-weight: 900;";

        // Update sidebar navigation labels
        updateLabelsInContainer(sidebar, lightTextStyle, lightSecondaryStyle, lightHeadingStyle);

        // Update main content labels
        updateLabelsInContainer(mainContent, lightTextStyle, lightSecondaryStyle, lightHeadingStyle);

        // Reset statistics labels
        if (totalEventsLabel != null) totalEventsLabel.setStyle(lightHeadingStyle);
        if (totalParticipationsLabel != null) totalParticipationsLabel.setStyle(lightHeadingStyle);
        if (totalRevenueLabel != null) totalRevenueLabel.setStyle(lightHeadingStyle);
        if (averagePriceLabel != null) averagePriceLabel.setStyle(lightHeadingStyle);
        if (eventResultCountLabel != null) eventResultCountLabel.setStyle("-fx-text-fill: #6366F1;");
        if (selectedEventLabel != null) selectedEventLabel.setStyle("-fx-text-fill: #6366F1;");
    }

    private void updateLabelsInContainer(Parent container, String textStyle, String secondaryStyle, String headingStyle) {
        if (container == null) return;

        for (javafx.scene.Node node : container.getChildrenUnmodifiable()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                String currentText = label.getText();
                if (currentText != null) {
                    if (currentText.contains("WinGO") || currentText.contains("Discover") ||
                            currentText.contains("Management") || currentText.contains("Participations") ||
                            currentText.contains("Events") || currentText.contains("Home") ||
                            currentText.contains("My Trips") || currentText.contains("Statistics")) {
                        label.setStyle(headingStyle);
                    } else if (currentText.contains("📍") || currentText.contains("📅") ||
                            currentText.contains("👥") || currentText.contains("💰") ||
                            currentText.contains("🎫") || currentText.contains("⭐") ||
                            currentText.contains("🔍") || currentText.contains("✕")) {
                        label.setStyle(textStyle);
                    } else {
                        label.setStyle(secondaryStyle);
                    }
                }
            } else if (node instanceof Parent) {
                updateLabelsInContainer((Parent) node, textStyle, secondaryStyle, headingStyle);
            }
        }
    }

    private void updateTableStylesForDarkMode() {
        String darkTableStyle = "-fx-background-color: #16213e; -fx-border-color: #0f3460; -fx-border-radius: 10; -fx-background-radius: 10;";

        if (eventTable != null) {
            eventTable.setStyle(darkTableStyle);
        }

        if (participationTable != null) {
            participationTable.setStyle(darkTableStyle);
        }

        if (participationTable2 != null) {
            participationTable2.setStyle(darkTableStyle);
        }

        if (topEventsTable != null) {
            topEventsTable.setStyle(darkTableStyle);
        }
    }

    private void updateTableStylesForLightMode() {
        String lightTableStyle = "-fx-background-color: transparent; -fx-border-color: #E2E8F0; -fx-border-radius: 10;";

        if (eventTable != null) {
            eventTable.setStyle(lightTableStyle);
        }

        if (participationTable != null) {
            participationTable.setStyle(lightTableStyle);
        }

        if (participationTable2 != null) {
            participationTable2.setStyle(lightTableStyle);
        }

        if (topEventsTable != null) {
            topEventsTable.setStyle(lightTableStyle);
        }
    }

    private void updateFormStylesForDarkMode() {
        String darkFieldStyle = "-fx-background-color: #0f3460; -fx-border-color: #1e3a5f; -fx-text-fill: white; -fx-prompt-text-fill: #a0a0a0;";
        String darkComboStyle = "-fx-background-color: #0f3460; -fx-border-color: #1e3a5f; -fx-text-fill: white;";

        // Update form fields
        if (titleField != null) titleField.setStyle(darkFieldStyle);
        if (locationField != null) locationField.setStyle(darkFieldStyle);
        if (statusField != null) statusField.setStyle(darkFieldStyle);
        if (timeField != null) timeField.setStyle(darkFieldStyle);
        if (capacityField != null) capacityField.setStyle(darkFieldStyle);
        if (priceField != null) priceField.setStyle(darkFieldStyle);
        if (imageField != null) imageField.setStyle(darkFieldStyle);
        if (descriptionArea != null) descriptionArea.setStyle(darkFieldStyle);

        if (eventTypeBox != null) eventTypeBox.setStyle(darkComboStyle);
        if (seasonBox != null) seasonBox.setStyle(darkComboStyle);
    }

    private void updateFormStylesForLightMode() {
        String lightFieldStyle = "-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-text-fill: #1E293B; -fx-prompt-text-fill: #94A3B8;";
        String lightComboStyle = "-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-text-fill: #1E293B;";

        // Update form fields
        if (titleField != null) titleField.setStyle(lightFieldStyle);
        if (locationField != null) locationField.setStyle(lightFieldStyle);
        if (statusField != null) statusField.setStyle(lightFieldStyle);
        if (timeField != null) timeField.setStyle(lightFieldStyle);
        if (capacityField != null) capacityField.setStyle(lightFieldStyle);
        if (priceField != null) priceField.setStyle(lightFieldStyle);
        if (imageField != null) imageField.setStyle(lightFieldStyle);
        if (descriptionArea != null) descriptionArea.setStyle(lightFieldStyle);

        if (eventTypeBox != null) eventTypeBox.setStyle(lightComboStyle);
        if (seasonBox != null) seasonBox.setStyle(lightComboStyle);
    }

    // ==================== STATISTICS METHODS ====================

    @FXML
    private void openStatistics() {
        if (statisticsDashboard != null) {
            statisticsDashboard.setVisible(true);
            statisticsDashboard.setManaged(true);
            updateStatistics();

            // Add fade animation
            FadeTransition ft = new FadeTransition(Duration.millis(300), statisticsDashboard);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    @FXML
    private void closeStatistics() {
        if (statisticsDashboard != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(200), statisticsDashboard);
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(e -> {
                statisticsDashboard.setVisible(false);
                statisticsDashboard.setManaged(false);
            });
            ft.play();
        }
    }

    private void updateStatistics() {
        calculateStatistics();
        updateEventTypePieChart();
        updateMonthlyBookingsChart();
        updateTopEventsTable();
        updateQuickStats();
    }

    private void calculateStatistics() {
        List<Participation> allParticipations = participationCRUD.afficherTous();
        List<Event> allEvents = eventCRUD.afficher();

        // Clear previous stats
        eventBookings.clear();
        eventRevenue.clear();

        int totalParticipations = 0;
        double totalRevenue = 0.0;

        // Calculate per-event statistics
        for (Participation p : allParticipations) {
            int eventId = p.getIdEvent();
            int places = p.getNombrePlaces();

            // Find event price
            double price = 0.0;
            for (Event e : allEvents) {
                if (e.getIdEvent() == eventId) {
                    price = e.getPrice();
                    break;
                }
            }

            // Update bookings count
            int currentBookings = eventBookings.getOrDefault(eventId, 0);
            eventBookings.put(eventId, currentBookings + places);

            // Update revenue
            double currentRevenue = eventRevenue.getOrDefault(eventId, 0.0);
            eventRevenue.put(eventId, currentRevenue + (price * places));

            totalParticipations += places;
            totalRevenue += (price * places);
        }

        // Update global statistics labels
        if (totalEventsLabel != null) {
            totalEventsLabel.setText(String.valueOf(allEvents.size()));
        }
        if (totalParticipationsLabel != null) {
            totalParticipationsLabel.setText(String.valueOf(totalParticipations));
        }
        if (totalRevenueLabel != null) {
            totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
        }
        if (averagePriceLabel != null && !allEvents.isEmpty()) {
            double avgPrice = allEvents.stream().mapToDouble(Event::getPrice).average().orElse(0.0);
            averagePriceLabel.setText(String.format("$%.2f", avgPrice));
        }
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

        // Initialize all months
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String month : months) {
            monthlyBookings.put(month, 0);
        }

        // Count bookings per month
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

        // Sort events by bookings (descending)
        events.sort((e1, e2) -> {
            int bookings1 = getEventBookings(e1.getIdEvent());
            int bookings2 = getEventBookings(e2.getIdEvent());
            return Integer.compare(bookings2, bookings1);
        });

        // Take top 5 events
        List<Event> topEvents = events.stream().limit(5).collect(Collectors.toList());
        topEventsTable.setItems(FXCollections.observableArrayList(topEvents));
    }

    private void updateQuickStats() {
        List<Event> events = eventCRUD.afficher();

        // Find most booked event
        if (!eventBookings.isEmpty() && mostBookedEventLabel != null) {
            Map.Entry<Integer, Integer> maxEntry = Collections.max(eventBookings.entrySet(),
                    Map.Entry.comparingByValue());
            Event mostBooked = events.stream()
                    .filter(e -> e.getIdEvent() == maxEntry.getKey())
                    .findFirst()
                    .orElse(null);
            if (mostBooked != null) {
                mostBookedEventLabel.setText(mostBooked.getTitle() + " (" + maxEntry.getValue() + ")");
            }
        }

        // Find highest revenue event
        if (!eventRevenue.isEmpty() && highestRevenueEventLabel != null) {
            Map.Entry<Integer, Double> maxRevenue = Collections.max(eventRevenue.entrySet(),
                    Map.Entry.comparingByValue());
            Event highestRevenue = events.stream()
                    .filter(e -> e.getIdEvent() == maxRevenue.getKey())
                    .findFirst()
                    .orElse(null);
            if (highestRevenue != null) {
                highestRevenueEventLabel.setText(highestRevenue.getTitle() + " ($" +
                        String.format("%.2f", maxRevenue.getValue()) + ")");
            }
        }

        // Count upcoming events
        long upcomingCount = events.stream()
                .filter(e -> e.getDateEvent() != null &&
                        e.getDateEvent().toLocalDate().isAfter(LocalDate.now()))
                .count();
        if (upcomingEventsLabel != null) {
            upcomingEventsLabel.setText(upcomingCount + " upcoming");
        }

        // Calculate average fill rate
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

    // ==================== IMAGE HANDLING ====================

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Event Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Create directory if it doesn't exist
                File directory = new File(IMAGE_DIRECTORY);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Generate unique filename
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destination = Paths.get(IMAGE_DIRECTORY + fileName);

                // Copy file to project directory
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Save relative path
                selectedImagePath = "images/events/" + fileName;
                imageField.setText(selectedImagePath);
                eventImagePathLabel.setText("Image selected: " + fileName);
                eventImagePathLabel.setStyle("-fx-text-fill: #10B981;");

                // Show preview
                Image image = new Image(destination.toUri().toString());
                eventImageView.setImage(image);

            } catch (Exception e) {
                showAlert("Error copying image: " + e.getMessage());
            }
        }
    }

    // ==================== EVENT CARD DISPLAY ====================

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

        // Apply card style based on theme
        String cardStyle = isDarkMode ?
                "-fx-background-color: #16213e; -fx-background-radius: 20; -fx-padding: 15; -fx-cursor: hand; -fx-border-color: #0f3460; -fx-border-radius: 20;" :
                "-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-padding: 15; -fx-cursor: hand;";

        card.setStyle(cardStyle);
        card.setEffect(new DropShadow(5, 0, 5, isDarkMode ? Color.rgb(0, 0, 0, 0.5) : Color.rgb(0, 0, 0, 0.1)));

        // Image container
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(180);
        imageContainer.setStyle("-fx-background-radius: 15; -fx-background-color: " + (isDarkMode ? "#0f3460" : "#F1F5F9") + ";");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(180);
        imageView.setFitWidth(280);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 15;");

        boolean imageLoaded = false;

        // Try to load image
        if (event.getImageEvent() != null && !event.getImageEvent().isEmpty()) {
            try {
                String imagePath = "file:src/main/resources/" + event.getImageEvent();
                File imageFile = new File("src/main/resources/" + event.getImageEvent());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), 280, 180, true, true);
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
            Label placeholder = new Label("🎨");
            placeholder.setStyle("-fx-font-size: 48px; -fx-text-fill: " + (isDarkMode ? "#4a5568" : "#94A3B8") + ";");
            imageContainer.getChildren().add(placeholder);
        }

        // Event type badge
        Label typeBadge = new Label(event.getEventType() != null ? event.getEventType() : "Event");
        typeBadge.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: 800; -fx-background-radius: 999; -fx-padding: 3 10;");
        typeBadge.setTranslateX(10);
        typeBadge.setTranslateY(-75);
        typeBadge.setAlignment(Pos.TOP_LEFT);
        imageContainer.getChildren().add(typeBadge);

        // Event details
        VBox details = new VBox(5);
        details.setPadding(new Insets(5, 0, 0, 0));

        String titleColor = isDarkMode ? "white" : "#1E293B";
        String textColor = isDarkMode ? "#a0a0a0" : "#64748B";
        String priceColor = isDarkMode ? "#f97316" : "#C2410C";
        String statColor = isDarkMode ? "#60a5fa" : "#3B82F6";
        String availableColor = isDarkMode ? "#34d399" : "#10B981";

        Label titleLabel = new Label(event.getTitle());
        titleLabel.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-weight: 900; -fx-font-size: 16px;");
        titleLabel.setWrapText(true);

        HBox locationBox = new HBox(5);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        locationIcon.setStyle("-fx-font-size: 12px; -fx-text-fill: " + textColor + ";");
        Label locationLabel = new Label(event.getLocation() != null ? event.getLocation() : "Location TBD");
        locationLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 12px;");
        locationBox.getChildren().addAll(locationIcon, locationLabel);

        HBox dateBox = new HBox(5);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = new Label("📅");
        dateIcon.setStyle("-fx-font-size: 12px; -fx-text-fill: " + textColor + ";");
        Label dateLabel = new Label(event.getDateEvent() != null ? event.getDateEvent().toString() : "Date TBD");
        dateLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 12px;");
        dateBox.getChildren().addAll(dateIcon, dateLabel);

        // Statistics Section
        int bookings = getEventBookings(event.getIdEvent());
        double revenue = getEventRevenue(event.getIdEvent());
        int availableSpots = event.getAvailablePlaces() - bookings;

        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(5, 0, 5, 0));

        Label bookingsLabel = new Label("👥 " + bookings + " booked");
        bookingsLabel.setStyle("-fx-text-fill: " + statColor + "; -fx-font-size: 11px; -fx-font-weight: 700;");

        Label revenueLabel = new Label("💰 $" + String.format("%.2f", revenue));
        revenueLabel.setStyle("-fx-text-fill: " + priceColor + "; -fx-font-size: 11px; -fx-font-weight: 700;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        statsBox.getChildren().addAll(bookingsLabel, spacer1, revenueLabel);

        // Price and availability
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label("$" + String.format("%.2f", event.getPrice()));
        priceLabel.setStyle("-fx-text-fill: " + priceColor + "; -fx-font-weight: 900; -fx-font-size: 18px;");

        Label availableLabel = new Label("🎫 " + Math.max(0, availableSpots) + " left");
        availableLabel.setStyle("-fx-text-fill: " + availableColor + "; -fx-font-size: 12px; -fx-font-weight: 700;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        priceBox.getChildren().addAll(priceLabel, spacer2, availableLabel);

        // Progress bar for capacity
        ProgressBar capacityBar = new ProgressBar(event.getCapacity() > 0 ? (double)bookings / event.getCapacity() : 0);
        capacityBar.setPrefWidth(250);
        capacityBar.setPrefHeight(8);
        capacityBar.setStyle("-fx-accent: #6366F1;");

        HBox progressBox = new HBox(5);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        Label fillRateLabel = new Label("Fill rate:");
        fillRateLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 11px;");
        progressBox.getChildren().addAll(fillRateLabel, capacityBar);

        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        Button participateBtn = new Button("Make a bid!");
        participateBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 20; -fx-font-size: 12px; -fx-cursor: hand;");
        participateBtn.setOnAction(e -> {
            currentEventId = event.getIdEvent();
            if (tabPane != null && participationsTab != null) {
                tabPane.getSelectionModel().select(participationsTab);
                setEventId(event.getIdEvent());
            }
        });

        Button reviewBtn = new Button("Review");
        String reviewBtnStyle = isDarkMode ?
                "-fx-background-color: #0f3460; -fx-text-fill: #a0a0a0; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 20; -fx-font-size: 12px; -fx-cursor: hand;" :
                "-fx-background-color: #EEF2FF; -fx-text-fill: #6366F1; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 20; -fx-font-size: 12px; -fx-cursor: hand;";
        reviewBtn.setStyle(reviewBtnStyle);
        reviewBtn.setOnAction(e -> {
            showEventDetails(event);
        });

        actions.getChildren().addAll(participateBtn, reviewBtn);

        details.getChildren().addAll(
                titleLabel,
                locationBox,
                dateBox,
                statsBox,
                priceBox,
                progressBox,
                actions
        );

        card.getChildren().addAll(imageContainer, details);

        // Add click event to the whole card
        card.setOnMouseClicked(e -> {
            currentEventId = event.getIdEvent();
            if (tabPane != null && eventsTab != null) {
                tabPane.getSelectionModel().select(eventsTab);
                if (eventTable != null) {
                    eventTable.getSelectionModel().select(event);
                    eventTable.scrollTo(event);
                }
                fillEventFields(event);
            }
        });

        return card;
    }

    private void showEventDetails(Event event) {
        int bookings = getEventBookings(event.getIdEvent());
        double revenue = getEventRevenue(event.getIdEvent());
        int availableSpots = event.getAvailablePlaces() - bookings;

        String details = String.format(
                "Event Details\n\n" +
                        "Title: %s\n" +
                        "Location: %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n" +
                        "Type: %s\n" +
                        "Season: %s\n" +
                        "Price: $%.2f\n" +
                        "Capacity: %d\n" +
                        "Booked: %d\n" +
                        "Available: %d\n" +
                        "Revenue: $%.2f\n" +
                        "Fill Rate: %.1f%%\n" +
                        "Status: %s\n\n" +
                        "Description:\n%s",
                event.getTitle(),
                event.getLocation(),
                event.getDateEvent() != null ? event.getDateEvent().toString() : "TBD",
                event.getStartTime() != null ? event.getStartTime().toString().substring(0, 5) : "TBD",
                event.getEventType(),
                event.getSeason(),
                event.getPrice(),
                event.getCapacity(),
                bookings,
                availableSpots,
                revenue,
                event.getCapacity() > 0 ? (bookings * 100.0 / event.getCapacity()) : 0,
                event.getStatus(),
                event.getDescription() != null ? event.getDescription() : "No description"
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Event Details - " + event.getTitle());
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        textArea.setPrefWidth(450);

        // Apply dark mode to alert if needed
        if (isDarkMode) {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #16213e;");
            textArea.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-control-inner-background: #0f3460;");
        }

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    // ==================== NAVIGATION METHODS ====================

    @FXML
    private void goToHome() {
        if (tabPane != null && homeTab != null) {
            tabPane.getSelectionModel().select(homeTab);
            loadEvents(); // Refresh events to update cards
        }
    }

    @FXML
    private void goToEvents() {
        if (tabPane != null && eventsTab != null) {
            tabPane.getSelectionModel().select(eventsTab);
            loadEvents(); // Refresh events when tab is selected
        }
    }

    @FXML
    private void goToParticipations() {
        if (tabPane != null && participationsTab != null) {
            tabPane.getSelectionModel().select(participationsTab);
            loadAllParticipations(); // Refresh participations when tab is selected
        }
    }

    @FXML
    private void goToMesParticipations() {
        if (tabPane != null && myTripsTab != null) {
            tabPane.getSelectionModel().select(myTripsTab);
            loadAllParticipationsIntoTable2(); // Refresh my trips when tab is selected
        }
    }

    // ==================== EVENT CRUD METHODS ====================

    @FXML
    private void addEvent() {
        try {
            if (!validateEventInputs()) return;

            Event e = buildEventFromForm();
            System.out.println("💰 Adding event with price: $" + e.getPrice());

            eventCRUD.ajouter(e);

            loadEvents();
            clearEventFields();
            loadEventSelector();
            showInfo("Event added successfully! Price: $" + String.format("%.2f", e.getPrice()));

        } catch (NumberFormatException ex) {
            showAlert("Capacity and Price must be valid numbers!");
        } catch (DateTimeParseException ex) {
            showAlert("Time must be like HH:mm (e.g., 19:30)!");
        } catch (Exception ex) {
            showAlert("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void updateEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select an event to update");
            return;
        }

        try {
            if (!validateEventInputs()) return;

            Event e = buildEventFromForm();
            e.setIdEvent(selected.getIdEvent());

            System.out.println("💰 Updating event ID " + selected.getIdEvent() +
                    " from price: $" + selected.getPrice() +
                    " to new price: $" + e.getPrice());

            eventCRUD.modifier(e);
            loadEvents();
            clearEventFields();
            loadEventSelector();
            showInfo("Event updated successfully! New price: $" + String.format("%.2f", e.getPrice()));

        } catch (NumberFormatException ex) {
            showAlert("Capacity and Price must be valid numbers!");
        } catch (Exception ex) {
            showAlert("Invalid input: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void deleteEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select an event to delete");
            return;
        }

        if (confirmAction("Delete Event", "Are you sure you want to delete this event?")) {
            eventCRUD.supprimer(selected.getIdEvent());
            loadEvents();
            clearEventFields();
            loadEventSelector();
            showInfo("Event deleted successfully!");
        }
    }

    @FXML
    private void openParticipationForSelectedEvent() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert("Please select an event first!");
            return;
        }

        // Switch to Participations tab and set the event ID
        if (tabPane != null && participationsTab != null) {
            tabPane.getSelectionModel().select(participationsTab);
            setEventId(selectedEvent.getIdEvent());
        }
    }

    private Event buildEventFromForm() {
        LocalDate localDate = datePicker.getValue();
        LocalTime localTime = LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        int capacity = Integer.parseInt(capacityField.getText().trim());

        // Parse price with better error handling
        double price = 0.0;
        String priceText = priceField.getText().trim();
        if (!priceText.isEmpty()) {
            try {
                // Handle both comma and decimal point
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
        e.setStatus(statusField.getText() != null ? statusField.getText().trim() : "Planifié");
        e.setImageEvent(imageField.getText() != null ? imageField.getText().trim() : "");
        e.setPrice(price);

        System.out.println("📝 Built event: " + e.getTitle() + " with price: $" + price);

        return e;
    }

    private boolean validateEventInputs() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Title is required");
            return false;
        }

        if (locationField.getText().trim().isEmpty()) {
            showAlert("Location is required");
            return false;
        }

        if (datePicker.getValue() == null) {
            showAlert("Please select a date!");
            return false;
        }

        if (timeField.getText().trim().isEmpty()) {
            showAlert("Time is required (HH:mm)!");
            return false;
        }

        try {
            LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showAlert("Time must be like HH:mm (e.g., 19:30)!");
            return false;
        }

        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) {
                showAlert("Capacity must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Capacity must be a number!");
            return false;
        }

        // Validate price
        String priceText = priceField.getText().trim();
        if (!priceText.isEmpty()) {
            try {
                priceText = priceText.replace(',', '.');
                double price = Double.parseDouble(priceText);
                if (price < 0) {
                    showAlert("Price cannot be negative");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Price must be a valid number!");
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
            timeField.setText(e.getStartTime().toString().substring(0,5));

        capacityField.setText(String.valueOf(e.getCapacity()));
        priceField.setText(String.valueOf(e.getPrice()));
        seasonBox.setValue(e.getSeason());
        eventTypeBox.setValue(e.getEventType());
        statusField.setText(e.getStatus());
        imageField.setText(e.getImageEvent());

        System.out.println("📋 Filling form with price: $" + e.getPrice());

        // Load image preview
        if (e.getImageEvent() != null && !e.getImageEvent().isEmpty()) {
            try {
                String imagePath = "file:src/main/resources/" + e.getImageEvent();
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
        if (statusField != null) statusField.setText("Planifié");
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

    private void loadEvents() {
        if (eventTable != null) {
            List<Event> events = eventCRUD.afficher();
            calculateStatistics();
            eventTable.setItems(FXCollections.observableArrayList(events));
            updateEventResultCount(events.size());
            displayEventCards(events); // Update cards on home tab

            // Refresh table to show statistics
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
                                " ($" + String.format("%.2f", event.getPrice()) +
                                ") - " + getEventBookings(event.getIdEvent()) + " bookings");
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
                                " ($" + String.format("%.2f", event.getPrice()) +
                                ") - " + getEventBookings(event.getIdEvent()) + " bookings");
                    }
                }
            });
        }
    }

    // ==================== PARTICIPATION METHODS ====================

    public void setEventId(int eventId) {
        this.currentEventId = eventId;
        if (eventIdLabel != null) {
            Event event = eventCRUD.getById(eventId);
            if (event != null) {
                int bookings = getEventBookings(eventId);
                double revenue = getEventRevenue(eventId);
                eventIdLabel.setText("Event #" + eventId + ": " + event.getTitle() +
                        " - " + bookings + " bookings ($" + String.format("%.2f", revenue) + ")");
            } else {
                eventIdLabel.setText("Participation to Event #" + eventId);
            }
            eventIdLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: 800;");
        }
        loadParticipationsForEvent();
    }

    public void setClientEmail(String email) {
        this.currentClientEmail = email;
        if (clientInfoLabel != null) {
            clientInfoLabel.setText("Client: " + email);
        }
        loadParticipationsByEmail(email);
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
            showAlert("Please select an event first!");
            return;
        }

        if (!validateParticipationInputs()) return;

        try {
            Participation p = buildParticipationFromForm();

            // Check if enough spots available
            int currentBookings = getEventBookings(currentEventId);
            Event event = eventCRUD.getById(currentEventId);
            if (event != null && currentBookings + p.getNombrePlaces() > event.getCapacity()) {
                showAlert("Not enough spots available! Only " + (event.getCapacity() - currentBookings) + " spots left.");
                return;
            }

            participationCRUD.ajouter(p);

            refreshParticipations();
            clearParticipationFields();
            loadEvents(); // Refresh to update statistics
            showInfo("Participation added successfully!");

        } catch (NumberFormatException e) {
            showAlert("User ID and places must be valid numbers!");
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void updateParticipation() {
        Participation selected = participationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Please select a participation to update!");
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

            refreshParticipations();
            clearParticipationFields();
            loadEvents(); // Refresh to update statistics
            showInfo("Participation updated successfully!");

        } catch (NumberFormatException e) {
            showAlert("User ID and places must be valid numbers!");
        }
    }

    @FXML
    private void deleteParticipation() {
        Participation selected = participationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Please select a participation to delete!");
            return;
        }

        if (confirmAction("Delete Participation", "Are you sure you want to delete this participation?")) {
            participationCRUD.supprimer(selected.getIdParticipation());
            refreshParticipations();
            clearParticipationFields();
            loadEvents(); // Refresh to update statistics
            showInfo("Participation deleted successfully!");
        }
    }

    @FXML
    private void viewParticipationDetails() {
        Participation selected = participationTable != null ?
                participationTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null && participationTable2 != null) {
            selected = participationTable2.getSelectionModel().getSelectedItem();
        }
        if (selected == null) {
            showAlert("Please select a participation!");
            return;
        }

        // Get event details
        Event event = eventCRUD.getById(selected.getIdEvent());
        double price = event != null ? event.getPrice() : 0.0;
        double totalPrice = price * selected.getNombrePlaces();

        String details = String.format(
                "Participation Details\n\n" +
                        "ID: %d\n" +
                        "Event ID: %d\n" +
                        "Event Title: %s\n" +
                        "User ID: %d\n" +
                        "Date: %s\n" +
                        "Status: %s\n" +
                        "Name: %s %s\n" +
                        "Email: %s\n" +
                        "Phone: %s\n" +
                        "Places: %d\n" +
                        "Price per person: $%.2f\n" +
                        "Total Amount: $%.2f",
                selected.getIdParticipation(),
                selected.getIdEvent(),
                selected.getEventTitle() != null ? selected.getEventTitle() : "N/A",
                selected.getIdUser(),
                selected.getDateParticipation(),
                selected.getStatut(),
                selected.getPrenomParticipant(),
                selected.getNomParticipant(),
                selected.getEmailParticipant(),
                selected.getTelephone() != null ? selected.getTelephone() : "Not provided",
                selected.getNombrePlaces(),
                price,
                totalPrice
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Participation Details");
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(15);

        // Apply dark mode to alert if needed
        if (isDarkMode) {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #16213e;");
            textArea.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-control-inner-background: #0f3460;");
        }

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void loadParticipationsForSelectedEvent() {
        Event selectedEvent = eventSelectorBox.getValue();
        if (selectedEvent == null) {
            showAlert("Please select an event!");
            return;
        }

        setEventId(selectedEvent.getIdEvent());
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
            showAlert("Please select a date!");
            return false;
        }

        if (statutBox.getValue() == null) {
            showAlert("Please select a status!");
            return false;
        }

        try {
            int userId = Integer.parseInt(userIdField.getText().trim());
            if (userId <= 0) {
                showAlert("User ID must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("User ID must be a valid number!");
            return false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            showAlert("First name is required!");
            return false;
        }

        if (nomField.getText().trim().isEmpty()) {
            showAlert("Last name is required!");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showAlert("Email is required!");
            return false;
        }

        if (!emailField.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Please enter a valid email!");
            return false;
        }

        try {
            int places = Integer.parseInt(placesField.getText().trim());
            if (places <= 0) {
                showAlert("Number of places must be greater than 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Number of places must be a valid number!");
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
        if (statutBox != null) statutBox.setValue("Confirmé");
        if (userIdField != null) userIdField.setText("1");
        if (nomField != null) nomField.clear();
        if (prenomField != null) prenomField.clear();
        if (emailField != null) emailField.clear();
        if (telephoneField != null) telephoneField.clear();
        if (placesField != null) placesField.clear();
        if (participationTable != null) participationTable.getSelectionModel().clearSelection();
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
            displayEventCards(events);
            eventTable.refresh();
        }
    }

    private void updateEventResultCount(int count) {
        if (eventResultCountLabel != null) {
            eventResultCountLabel.setText(count + " event(s) found");
        }
    }

    @FXML
    private void onSearch() {
        // Determine which search field is being used based on the active tab
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        String searchTerm = "";
        String searchType = "";

        if (selectedTab == participationsTab) {
            searchTerm = participationsSearchField != null ? participationsSearchField.getText().trim() : "";
            searchType = searchTypeBox != null ? searchTypeBox.getValue() : "Par Email";
        } else if (selectedTab == myTripsTab) {
            searchTerm = myTripsSearchField != null ? myTripsSearchField.getText().trim() : "";
            searchType = myTripsSearchType != null ? myTripsSearchType.getValue() : "Par Email";
        }

        if (searchTerm.isEmpty()) {
            if (!currentClientEmail.isEmpty() && "Par Email".equals(searchType)) {
                loadParticipationsByEmail(currentClientEmail);
            } else {
                loadAllParticipations();
                loadAllParticipationsIntoTable2();
            }
            return;
        }

        switch (searchType) {
            case "Par Email":
                loadParticipationsByEmail(searchTerm);
                break;
            case "Par Nom":
                String[] parts = searchTerm.split(" ");
                if (parts.length >= 2) {
                    loadParticipationsByName(parts[0], parts[1]);
                } else {
                    searchParticipations(searchTerm);
                }
                break;
            default:
                searchParticipations(searchTerm);
                break;
        }
    }

    @FXML
    private void onClearSearch() {
        if (participationsSearchField != null) participationsSearchField.clear();
        if (myTripsSearchField != null) myTripsSearchField.clear();

        if (!currentClientEmail.isEmpty()) {
            loadParticipationsByEmail(currentClientEmail);
        } else {
            loadAllParticipations();
            loadAllParticipationsIntoTable2();
        }
    }

    @FXML
    private void onRefresh() {
        onClearSearch();
    }

    private void loadParticipationsByEmail(String email) {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.afficherParClient(email);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
        if (participationTable2 != null) {
            List<Participation> participations = participationCRUD.afficherParClient(email);
            participationTable2.setItems(FXCollections.observableArrayList(participations));
            if (myTripsResultLabel != null) {
                myTripsResultLabel.setText(participations.size() + " participation(s) trouvee(s)");
            }
        }
    }

    private void loadParticipationsByName(String nom, String prenom) {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.afficherParNomClient(nom, prenom);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
        if (participationTable2 != null) {
            List<Participation> participations = participationCRUD.afficherParNomClient(nom, prenom);
            participationTable2.setItems(FXCollections.observableArrayList(participations));
            if (myTripsResultLabel != null) {
                myTripsResultLabel.setText(participations.size() + " participation(s) trouvee(s)");
            }
        }
    }

    private void searchParticipations(String term) {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.rechercherParticipations(term);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
        if (participationTable2 != null) {
            List<Participation> participations = participationCRUD.rechercherParticipations(term);
            participationTable2.setItems(FXCollections.observableArrayList(participations));
            if (myTripsResultLabel != null) {
                myTripsResultLabel.setText(participations.size() + " participation(s) trouvee(s)");
            }
        }
    }

    private void loadAllParticipations() {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.afficherTous();
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
    }

    private void loadAllParticipationsIntoTable2() {
        if (participationTable2 != null) {
            List<Participation> participations = participationCRUD.afficherTous();
            participationTable2.setItems(FXCollections.observableArrayList(participations));
            if (myTripsResultLabel != null) {
                myTripsResultLabel.setText(participations.size() + " participation(s) trouvee(s)");
            }
        }
    }

    private void loadParticipationsForEvent() {
        if (participationTable != null && currentEventId > 0) {
            List<Participation> participations = participationCRUD.afficherParEvent(currentEventId);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
    }

    private void refreshParticipations() {
        if (!currentClientEmail.isEmpty()) {
            loadParticipationsByEmail(currentClientEmail);
        } else if (currentEventId > 0) {
            loadParticipationsForEvent();
        } else {
            loadAllParticipations();
            loadAllParticipationsIntoTable2();
        }
        calculateStatistics(); // Update statistics
        if (eventTable != null) {
            eventTable.refresh(); // Refresh event table to show updated stats
        }
    }

    private void updateResultCount(int count) {
        if (resultCountLabel != null) {
            resultCountLabel.setText(count + " participation(s) trouvée(s)");
        }
    }

    // ==================== UTILITY METHODS ====================

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setTitle("Error");
        alert.setHeaderText(null);

        // Apply dark mode to alert if needed
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

        // Apply dark mode to alert if needed
        if (isDarkMode) {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #16213e;");
        }

        alert.showAndWait();
    }

    private boolean confirmAction(String title, String message) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(title);
        confirm.setContentText(message);

        // Apply dark mode to alert if needed
        if (isDarkMode) {
            DialogPane dialogPane = confirm.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #16213e;");
        }

        return confirm.showAndWait().get() == ButtonType.OK;
    }
}