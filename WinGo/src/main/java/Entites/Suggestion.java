package Entites;

import java.sql.Date;

public class Suggestion {
    private int id_suggestion;
    private int id_user;
    private String sujet;
    private String description;
    private String categorie;
    private Date date_suggestion;
    private String statut;
    private Integer id_reclamation;
    private String reponse_admin;
    private Date date_reponse;

    public Suggestion() {
    }

    public Suggestion(int id_user, String sujet, String description, String categorie, Integer id_reclamation) {
        this.id_user = id_user;
        this.sujet = sujet;
        this.description = description;
        this.categorie = categorie;
        this.id_reclamation = id_reclamation;
        this.statut = "Attente";
        this.date_suggestion = new Date(System.currentTimeMillis());
    }

    public int getId_suggestion() {
        return id_suggestion;
    }

    public void setId_suggestion(int id_suggestion) {
        this.id_suggestion = id_suggestion;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
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

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public Date getDate_suggestion() {
        return date_suggestion;
    }

    public void setDate_suggestion(Date date_suggestion) {
        this.date_suggestion = date_suggestion;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getId_reclamation() {
        return id_reclamation;
    }

    public void setId_reclamation(Integer id_reclamation) {
        this.id_reclamation = id_reclamation;
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

    @Override
    public String toString() {
        return "Suggestion{" +
                "id=" + id_suggestion +
                ", sujet='" + sujet + '\'' +
                ", categorie='" + categorie + '\'' +
                '}';
    }
}
