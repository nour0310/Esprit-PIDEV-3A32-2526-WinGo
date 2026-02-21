package Entites;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int utilisateurId;      // destinataire
    private int emetteurId;          // celui qui a généré la notification
    private String type;             // "mention", "like", "commentaire", etc.
    private String contenu;
    private String lien;             // lien vers le contenu concerné
    private boolean lu;
    private LocalDateTime dateCreation;

    public Notification() {}

    public Notification(int utilisateurId, int emetteurId, String type, String contenu, String lien) {
        this.utilisateurId = utilisateurId;
        this.emetteurId = emetteurId;
        this.type = type;
        this.contenu = contenu;
        this.lien = lien;
        this.lu = false;
        this.dateCreation = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public int getEmetteurId() { return emetteurId; }
    public void setEmetteurId(int emetteurId) { this.emetteurId = emetteurId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getLien() { return lien; }
    public void setLien(String lien) { this.lien = lien; }

    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}