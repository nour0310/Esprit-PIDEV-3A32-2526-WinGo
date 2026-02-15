package Entites;

import java.sql.Date;
import java.sql.Time;

public class Event {
    private int id_event;              // matches DB column: id_event
    private String title;
    private String description;
    private String location;
    private Date date_event;            // matches DB column: date_event
    private Time start_time;            // matches DB column: start_time
    private int capacity;
    private int available_places;       // matches DB column: available_places
    private String season;
    private String event_type;          // matches DB column: event_type
    private String status;
    private String image_event;         // matches DB column: image_event

    // Default Constructor
    public Event() {}

    // Full Constructor with all fields
    public Event(int id_event, String title, String description, String location, Date date_event,
                 Time start_time, int capacity, int available_places, String season,
                 String event_type, String status, String image_event) {
        this.id_event = id_event;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date_event = date_event;
        this.start_time = start_time;
        this.capacity = capacity;
        this.available_places = available_places;
        this.season = season;
        this.event_type = event_type;
        this.status = status;
        this.image_event = image_event;
    }

    // ==================== ORIGINAL GETTERS/SETTERS (with underscores) ====================

    public int getId_event() { return id_event; }
    public void setId_event(int id_event) { this.id_event = id_event; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Date getDate_event() { return date_event; }
    public void setDate_event(Date date_event) { this.date_event = date_event; }

    public Time getStart_time() { return start_time; }
    public void setStart_time(Time start_time) { this.start_time = start_time; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getAvailable_places() { return available_places; }
    public void setAvailable_places(int available_places) { this.available_places = available_places; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public String getEvent_type() { return event_type; }
    public void setEvent_type(String event_type) { this.event_type = event_type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImage_event() { return image_event; }
    public void setImage_event(String image_event) { this.image_event = image_event; }

    // ==================== HELPER METHODS for JavaFX PropertyValueFactory (without underscores) ====================

    // These methods allow your FXML to use property names like "idEvent", "dateEvent", etc.

    public int getIdEvent() { return id_event; }
    public void setIdEvent(int idEvent) { this.id_event = idEvent; }

    public Date getDateEvent() { return date_event; }
    public void setDateEvent(Date dateEvent) { this.date_event = dateEvent; }

    public Time getStartTime() { return start_time; }
    public void setStartTime(Time startTime) { this.start_time = startTime; }

    public int getAvailablePlaces() { return available_places; }
    public void setAvailablePlaces(int availablePlaces) { this.available_places = availablePlaces; }

    public String getEventType() { return event_type; }
    public void setEventType(String eventType) { this.event_type = eventType; }

    public String getImageEvent() { return image_event; }
    public void setImageEvent(String imageEvent) { this.image_event = imageEvent; }

    @Override
    public String toString() {
        return "Event{" +
                "id_event=" + id_event +
                ", title='" + title + '\'' +
                '}';
    }
}