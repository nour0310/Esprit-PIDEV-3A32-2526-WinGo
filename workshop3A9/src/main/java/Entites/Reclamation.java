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
    private String reponse_admin;
    private Date date_reponse;
    private String piece_jointe;

    public Reclamation() {
    }

    public Reclamation(int id_user, String type_reclamation, String sujet, String description, String priorite,
            String piece_jointe) {
        this.id_user = id_user;
        this.type_reclamation = type_reclamation;
        this.sujet = sujet;
        this.description = description;
        this.priorite = priorite;
        this.piece_jointe = piece_jointe;
        this.statut = "En attente";
        this.date_reclamation = new Date(System.currentTimeMillis());
    }

    public int getId_reclamation() {
        return id_reclamation;
    }

    public void setId_reclamation(int id_reclamation) {
        this.id_reclamation = id_reclamation;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getType_reclamation() {
        return type_reclamation;
    }

    public void setType_reclamation(String type_reclamation) {
        this.type_reclamation = type_reclamation;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate_reclamation() {
        return date_reclamation;
    }

    public void setDate_reclamation(Date date_reclamation) {
        this.date_reclamation = date_reclamation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getReponse_admin() {
        return reponse_admin;
    }

    public void setReponse_admin(String reponse_admin) {
        this.reponse_admin = reponse_admin;
    }

    public Date getDate_reponse() {
        return date_reponse;
    }

    public void setDate_reponse(Date date_reponse) {
        this.date_reponse = date_reponse;
    }

    public String getPiece_jointe() {
        return piece_jointe;
    }

    public void setPiece_jointe(String piece_jointe) {
        this.piece_jointe = piece_jointe;
    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id_reclamation +
                ", sujet='" + sujet + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
