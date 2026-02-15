package Entites;

import java.sql.Date;

public class Participation {
    private int id_participation;        // matches DB column: id_participation
    private int id_event;                 // matches DB column: id_event
    private int id_user;                  // matches DB column: id_user
    private Date date_participation;      // matches DB column: date_participation
    private String statut;
    private String nom_participant;       // matches DB column: nom_participant
    private String prenom_participant;    // matches DB column: prenom_participant
    private String email_participant;     // matches DB column: email_participant
    private String telephone;
    private int nombre_places;            // matches DB column: nombre_places

    // For join with Event
    private String eventTitle;

    // Constructors
    public Participation() {}

    public Participation(int id_participation, int id_event, int id_user, Date date_participation,
                         String statut, String nom_participant, String prenom_participant,
                         String email_participant, String telephone, int nombre_places) {
        this.id_participation = id_participation;
        this.id_event = id_event;
        this.id_user = id_user;
        this.date_participation = date_participation;
        this.statut = statut;
        this.nom_participant = nom_participant;
        this.prenom_participant = prenom_participant;
        this.email_participant = email_participant;
        this.telephone = telephone;
        this.nombre_places = nombre_places;
    }

    // ==================== ORIGINAL GETTERS/SETTERS (with underscores) ====================

    public int getId_participation() { return id_participation; }
    public void setId_participation(int id_participation) { this.id_participation = id_participation; }

    public int getId_event() { return id_event; }
    public void setId_event(int id_event) { this.id_event = id_event; }

    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }

    public Date getDate_participation() { return date_participation; }
    public void setDate_participation(Date date_participation) { this.date_participation = date_participation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getNom_participant() { return nom_participant; }
    public void setNom_participant(String nom_participant) { this.nom_participant = nom_participant; }

    public String getPrenom_participant() { return prenom_participant; }
    public void setPrenom_participant(String prenom_participant) { this.prenom_participant = prenom_participant; }

    public String getEmail_participant() { return email_participant; }
    public void setEmail_participant(String email_participant) { this.email_participant = email_participant; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public int getNombre_places() { return nombre_places; }
    public void setNombre_places(int nombre_places) { this.nombre_places = nombre_places; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    // ==================== HELPER METHODS for JavaFX PropertyValueFactory (without underscores) ====================

    // These methods allow your FXML to use property names like "idParticipation", "idEvent", etc.

    public int getIdParticipation() { return id_participation; }
    public void setIdParticipation(int idParticipation) { this.id_participation = idParticipation; }

    public int getIdEvent() { return id_event; }
    public void setIdEvent(int idEvent) { this.id_event = idEvent; }

    public int getIdUser() { return id_user; }
    public void setIdUser(int idUser) { this.id_user = idUser; }

    public Date getDateParticipation() { return date_participation; }
    public void setDateParticipation(Date dateParticipation) { this.date_participation = dateParticipation; }

    public String getNomParticipant() { return nom_participant; }
    public void setNomParticipant(String nomParticipant) { this.nom_participant = nomParticipant; }

    public String getPrenomParticipant() { return prenom_participant; }
    public void setPrenomParticipant(String prenomParticipant) { this.prenom_participant = prenomParticipant; }

    public String getEmailParticipant() { return email_participant; }
    public void setEmailParticipant(String emailParticipant) { this.email_participant = emailParticipant; }

    public int getNombrePlaces() { return nombre_places; }
    public void setNombrePlaces(int nombrePlaces) { this.nombre_places = nombrePlaces; }

    @Override
    public String toString() {
        return "Participation{" +
                "id_participation=" + id_participation +
                ", id_event=" + id_event +
                ", eventTitle='" + eventTitle + '\'' +
                '}';
    }
}