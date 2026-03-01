package Entites;

import java.sql.Date;

public class Reclamation {
    private int id_reclamation;
    private int id_user;
    private String type_reclamation;
    private String sujet;
    private String description;
    private Date date_reclamation;
    private String statut;
    private String priorite;
    private String piece_jointe;
    private String reponse_admin;
    private Date date_reponse;

    // Default Constructor
    public Reclamation() {}

    // Full Constructor
    public Reclamation(int id_reclamation, int id_user, String type_reclamation, String sujet,
                       String description, Date date_reclamation, String statut, String priorite,
                       String piece_jointe, String reponse_admin, Date date_reponse) {
        this.id_reclamation = id_reclamation;
        this.id_user = id_user;
        this.type_reclamation = type_reclamation;
        this.sujet = sujet;
        this.description = description;
        this.date_reclamation = date_reclamation;
        this.statut = statut;
        this.priorite = priorite;
        this.piece_jointe = piece_jointe;
        this.reponse_admin = reponse_admin;
        this.date_reponse = date_reponse;
    }

    // Constructor without ID (for insertion)
    public Reclamation(int id_user, String type_reclamation, String sujet, String description,
                       String priorite, String piece_jointe) {
        this.id_user = id_user;
        this.type_reclamation = type_reclamation;
        this.sujet = sujet;
        this.description = description;
        this.priorite = priorite;
        this.piece_jointe = piece_jointe;
        this.statut = "En attente";
    }

    // Getters and Setters
    public int getId_reclamation() { return id_reclamation; }
    public void setId_reclamation(int id_reclamation) { this.id_reclamation = id_reclamation; }

    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }

    public String getType_reclamation() { return type_reclamation; }
    public void setType_reclamation(String type_reclamation) { this.type_reclamation = type_reclamation; }

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate_reclamation() { return date_reclamation; }
    public void setDate_reclamation(Date date_reclamation) { this.date_reclamation = date_reclamation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getPiece_jointe() { return piece_jointe; }
    public void setPiece_jointe(String piece_jointe) { this.piece_jointe = piece_jointe; }

    public String getReponse_admin() { return reponse_admin; }
    public void setReponse_admin(String reponse_admin) { this.reponse_admin = reponse_admin; }

    public Date getDate_reponse() { return date_reponse; }
    public void setDate_reponse(Date date_reponse) { this.date_reponse = date_reponse; }

    // Helper methods for JavaFX PropertyValueFactory
    public int getIdReclamation() { return id_reclamation; }
    public void setIdReclamation(int idReclamation) { this.id_reclamation = idReclamation; }

    public int getIdUser() { return id_user; }
    public void setIdUser(int idUser) { this.id_user = idUser; }

    public String getTypeReclamation() { return type_reclamation; }
    public void setTypeReclamation(String typeReclamation) { this.type_reclamation = typeReclamation; }

    public Date getDateReclamation() { return date_reclamation; }
    public void setDateReclamation(Date dateReclamation) { this.date_reclamation = dateReclamation; }

    public String getReponseAdmin() { return reponse_admin; }
    public void setReponseAdmin(String reponseAdmin) { this.reponse_admin = reponseAdmin; }

    public Date getDateReponse() { return date_reponse; }
    public void setDateReponse(Date dateReponse) { this.date_reponse = dateReponse; }

    public String getPieceJointe() { return piece_jointe; }
    public void setPieceJointe(String pieceJointe) { this.piece_jointe = pieceJointe; }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id_reclamation=" + id_reclamation +
                ", sujet='" + sujet + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}