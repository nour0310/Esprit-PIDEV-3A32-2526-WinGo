package Controlles;

import Entites.Event;
import Entites.Participation;
import Services.EventCRUD;
import Services.ParticipationCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;

public class EventController implements Initializable {

    // ==================== NAVIGATION METHODS ====================

    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeTravel.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Home");
            stage.setScene(new Scene(root));
            stage.show();
            closeCurrentWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading Home: " + e.getMessage());
        }
    }

    @FXML
    private void goToEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventForm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Events");
            stage.setScene(new Scene(root));
            stage.show();
            closeCurrentWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading Events: " + e.getMessage());
        }
    }

    @FXML
    private void goToParticipations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParticipationForm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Participations");
            stage.setScene(new Scene(root));
            stage.show();
            closeCurrentWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading Participations: " + e.getMessage());
        }
    }

    @FXML
    private void goToMesParticipations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MesParticipations.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Mes Participations");
            stage.setScene(new Scene(root));
            stage.show();
            closeCurrentWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading Mes Participations: " + e.getMessage());
        }
    }

    private void closeCurrentWindow() {
        if (titleField != null && titleField.getScene() != null) {
            titleField.getScene().getWindow().hide();
        } else if (searchField != null && searchField.getScene() != null) {
            searchField.getScene().getWindow().hide();
        }
    }

    @FXML
    private void closeForm() {
        closeCurrentWindow();
    }

    @FXML
    private void onSearchClick() {
        System.out.println("Search clicked");
    }

    @FXML
    private void onFilter() {
        System.out.println("Filter clicked");
    }

    @FXML
    private void onReserveDeal() {
        new Alert(Alert.AlertType.INFORMATION, "Deal reserved!").show();
    }

    // ==================== EVENT MANAGEMENT ====================

    // Event Form Fields
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField capacityField;
    @FXML private TextField statusField;
    @FXML private TextField imageField;
    @FXML private ComboBox<String> seasonBox;
    @FXML private ComboBox<String> eventTypeBox;

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

    private final EventCRUD eventCRUD = new EventCRUD();
    private int currentEventId = 0;

    @FXML
    public void initializeEventSection() {
        // Initialize event table columns with String converters for Date and Time
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("idEvent"));
        if (colTitle != null) colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colLocation != null) colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

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
        if (eventTypeBox != null) eventTypeBox.getItems().addAll("Cultural", "Sport", "Music", "Business");
        if (seasonBox != null) seasonBox.getItems().addAll("Winter", "Spring", "Summer", "Autumn");

        // Load events
        loadEvents();

        // Add table selection listener
        if (eventTable != null) {
            eventTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> {
                        if (newVal != null) fillEventFields(newVal);
                    }
            );
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
            showInfo("Event added successfully ");

        } catch (NumberFormatException ex) {
            showAlert("Capacity must be a number ");
        } catch (DateTimeParseException ex) {
            showAlert("Time must be like 22:22 ");
        } catch (Exception ex) {
            showAlert("Error: " + ex.getMessage());
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
            Event e = buildEventFromForm();
            e.setIdEvent(selected.getIdEvent());

            eventCRUD.modifier(e);
            loadEvents();
            clearEventFields();
            showInfo("Event modified successfully ");

        } catch (Exception ex) {
            showAlert("Invalid input ");
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
            showInfo("Event deleted successfully ");
        }
    }

    @FXML
    private void openParticipation() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();

        if (selectedEvent == null) {
            showAlert("Veuillez sélectionner un événement d'abord ⚠");
            return;
        }

        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParticipationForm.fxml"));
            Parent root = loader.load();

            // Get the controller (which is also EventController)
            EventController controller = loader.getController();
            controller.setEventId(selectedEvent.getIdEvent());

            // Create and show the stage
            Stage stage = new Stage();
            stage.setTitle("Participation - Event #" + selectedEvent.getIdEvent());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening participation form: " + e.getMessage());
        }
    }

    private Event buildEventFromForm() {
        LocalDate localDate = datePicker.getValue();
        LocalTime localTime = LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        int capacity = Integer.parseInt(capacityField.getText().trim());

        Event e = new Event();
        e.setTitle(titleField.getText());
        e.setDescription(descriptionArea.getText());
        e.setLocation(locationField.getText());
        e.setDateEvent(Date.valueOf(localDate));
        e.setStartTime(Time.valueOf(localTime));
        e.setCapacity(capacity);
        e.setAvailablePlaces(capacity);
        e.setSeason(seasonBox.getValue());
        e.setEventType(eventTypeBox.getValue());
        e.setStatus(statusField.getText());
        e.setImageEvent(imageField.getText());

        return e;
    }

    private boolean validateEventInputs() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Title is required");
            return false;
        }

        if (datePicker.getValue() == null) {
            showAlert("Please select a date ");
            return false;
        }

        if (timeField.getText().trim().isEmpty()) {
            showAlert("Time must be like 22:22 ");
            return false;
        }

        try {
            LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showAlert("Time must be like 22:22 ");
            return false;
        }

        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) {
                showAlert("Capacity must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Capacity must be a number ");
            return false;
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
        seasonBox.setValue(e.getSeason());
        eventTypeBox.setValue(e.getEventType());
        statusField.setText(e.getStatus());
        imageField.setText(e.getImageEvent());
    }

    @FXML
    private void clearEventFields() {
        if (titleField != null) titleField.clear();
        if (descriptionArea != null) descriptionArea.clear();
        if (locationField != null) locationField.clear();
        if (datePicker != null) datePicker.setValue(null);
        if (timeField != null) timeField.clear();
        if (capacityField != null) capacityField.clear();
        if (seasonBox != null) seasonBox.getSelectionModel().clearSelection();
        if (eventTypeBox != null) eventTypeBox.getSelectionModel().clearSelection();
        if (statusField != null) statusField.clear();
        if (imageField != null) imageField.clear();
        if (eventTable != null) eventTable.getSelectionModel().clearSelection();
    }

    private void loadEvents() {
        if (eventTable != null) {
            List<Event> events = eventCRUD.afficher();
            System.out.println(" Loading " + events.size() + " events");
            eventTable.setItems(FXCollections.observableArrayList(events));
        }
    }

    // ==================== PARTICIPATION MANAGEMENT ====================

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

    // Search Components
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchTypeBox;
    @FXML private Label resultCountLabel;
    @FXML private Label clientInfoLabel;

    private final ParticipationCRUD participationCRUD = new ParticipationCRUD();
    private String currentClientEmail = "";

    @FXML
    public void initializeParticipationSection() {
        // Initialize participation table columns with proper type handling

        // For ID columns - these work fine with PropertyValueFactory
        if (colPartId != null) colPartId.setCellValueFactory(new PropertyValueFactory<>("idParticipation"));
        if (colEvent != null) colEvent.setCellValueFactory(new PropertyValueFactory<>("idEvent"));
        if (colEventTitle != null) colEventTitle.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));

        // Fix for Date column
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

        // For String columns
        if (colStatut != null) colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        if (colNom != null) colNom.setCellValueFactory(new PropertyValueFactory<>("nomParticipant"));
        if (colPrenom != null) colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenomParticipant"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("emailParticipant"));

        // For Integer columns
        if (colPlaces != null) colPlaces.setCellValueFactory(new PropertyValueFactory<>("nombrePlaces"));
        if (colUserId != null) colUserId.setCellValueFactory(new PropertyValueFactory<>("idUser"));

        // Initialize combo boxes
        if (statutBox != null) statutBox.getItems().addAll("Confirmé", "En attente", "Annulé");

        // Initialize search
        if (searchTypeBox != null) {
            searchTypeBox.getItems().addAll("Par Email", "Par Nom", "Toutes");
            searchTypeBox.setValue("Par Email");
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
    }

    public void setEventId(int eventId) {
        this.currentEventId = eventId;
        if (eventIdLabel != null) {
            eventIdLabel.setText(" Participation à l'événement #" + eventId);
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
            showAlert("⚠ Veuillez sélectionner un événement d'abord");
            return;
        }

        if (!validateParticipationInputs()) return;

        try {
            Participation p = buildParticipationFromForm();
            participationCRUD.ajouter(p);

            refreshParticipations();
            clearParticipationFields();
            showInfo(" Participation ajoutée avec succès");

        } catch (NumberFormatException e) {
            showAlert(" L'ID utilisateur et le nombre de places doivent être des nombres valides");
        } catch (Exception e) {
            showAlert(" Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void updateParticipation() {
        Participation selected = participationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("⚠ Veuillez sélectionner une participation à modifier");
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
            showInfo(" Participation modifiée avec succès");

        } catch (NumberFormatException e) {
            showAlert("L'ID utilisateur et le nombre de places doivent être des nombres valides");
        }
    }

    @FXML
    private void deleteParticipation() {
        Participation selected = participationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("⚠ Veuillez sélectionner une participation à supprimer");
            return;
        }

        if (confirmAction("Supprimer la participation", "Êtes-vous sûr de vouloir supprimer cette participation ?")) {
            participationCRUD.supprimer(selected.getIdParticipation());
            refreshParticipations();
            clearParticipationFields();
            showInfo(" Participation supprimée avec succès");
        }
    }

    @FXML
    private void viewParticipationDetails() {
        Participation selected = participationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une participation");
            return;
        }

        String details = String.format(
                "Détails de la participation\n\n" +
                        "ID: %d\n" +
                        "ID Événement: %d\n" +
                        "Titre: %s\n" +
                        "ID Utilisateur: %d\n" +
                        "Date: %s\n" +
                        "Statut: %s\n" +
                        "Nom complet: %s %s\n" +
                        "Email: %s\n" +
                        "Téléphone: %s\n" +
                        "Nombre de places: %d",
                selected.getIdParticipation(),
                selected.getIdEvent(),
                selected.getEventTitle() != null ? selected.getEventTitle() : "N/A",
                selected.getIdUser(),
                selected.getDateParticipation(),
                selected.getStatut(),
                selected.getPrenomParticipant(),
                selected.getNomParticipant(),
                selected.getEmailParticipant(),
                selected.getTelephone() != null ? selected.getTelephone() : "Non renseigné",
                selected.getNombrePlaces()
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la participation");
        alert.setHeaderText(null);
        alert.setContentText(details);
        alert.showAndWait();
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
            showAlert(" Veuillez sélectionner une date");
            return false;
        }

        if (statutBox.getValue() == null) {
            showAlert(" Veuillez sélectionner un statut");
            return false;
        }

        try {
            int userId = Integer.parseInt(userIdField.getText().trim());
            if (userId <= 0) {
                showAlert(" L'ID utilisateur doit être un nombre positif");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(" L'ID utilisateur doit être un nombre valide");
            return false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            showAlert(" Le prénom est requis");
            return false;
        }

        if (nomField.getText().trim().isEmpty()) {
            showAlert(" Le nom est requis");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showAlert(" L'email est requis");
            return false;
        }

        if (!emailField.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(" Veuillez entrer un email valide");
            return false;
        }

        try {
            int places = Integer.parseInt(placesField.getText().trim());
            if (places <= 0) {
                showAlert(" Le nombre de places doit être supérieur à 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(" Le nombre de places doit être un nombre valide");
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
        if (statutBox != null) statutBox.getSelectionModel().clearSelection();
        if (userIdField != null) userIdField.setText("1");
        if (nomField != null) nomField.clear();
        if (prenomField != null) prenomField.clear();
        if (emailField != null) emailField.clear();
        if (telephoneField != null) telephoneField.clear();
        if (placesField != null) placesField.clear();
        if (participationTable != null) participationTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSearch() {
        String searchTerm = searchField.getText().trim();
        String searchType = searchTypeBox.getValue();

        if (searchTerm.isEmpty()) {
            if ("Par Email".equals(searchType) && !currentClientEmail.isEmpty()) {
                loadParticipationsByEmail(currentClientEmail);
            } else {
                loadAllParticipations();
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
            case "Toutes":
                searchParticipations(searchTerm);
                break;
            default:
                searchParticipations(searchTerm);
                break;
        }
    }

    @FXML
    private void onClearSearch() {
        searchField.clear();
        if (!currentClientEmail.isEmpty()) {
            loadParticipationsByEmail(currentClientEmail);
        } else {
            loadAllParticipations();
        }
    }

    @FXML
    private void onRefresh() {
        onClearSearch();
        showInfo(" Données actualisées");
    }

    private void loadParticipationsByEmail(String email) {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.afficherParClient(email);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
    }

    private void loadParticipationsByName(String nom, String prenom) {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.afficherParNomClient(nom, prenom);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
    }

    private void searchParticipations(String term) {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.rechercherParticipations(term);
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
    }

    private void loadAllParticipations() {
        if (participationTable != null) {
            List<Participation> participations = participationCRUD.afficherTous();
            participationTable.setItems(FXCollections.observableArrayList(participations));
            updateResultCount(participations.size());
        }
    }

    private void loadParticipationsForEvent() {
        if (participationTable != null && currentEventId > 0) {
            participationTable.setItems(
                    FXCollections.observableArrayList(
                            participationCRUD.afficherParEvent(currentEventId)
                    )
            );
            updateResultCount(participationTable.getItems().size());
        }
    }

    private void refreshParticipations() {
        if (!currentClientEmail.isEmpty()) {
            loadParticipationsByEmail(currentClientEmail);
        } else if (currentEventId > 0) {
            loadParticipationsForEvent();
        } else {
            loadAllParticipations();
        }
    }

    private void updateResultCount(int count) {
        if (resultCountLabel != null) {
            resultCountLabel.setText(count + " participation(s) trouvée(s)");
        }
    }

    // ==================== UTILITY METHODS ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeEventSection();
        initializeParticipationSection();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
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