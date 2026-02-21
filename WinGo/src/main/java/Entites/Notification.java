package Entites;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int utilisateurId;      // destinataire
    private int emetteurId;          // celui qui a tagué
    private String type;             // "mention", "like", "commentaire"
    private String contenu;
    private String lien;             // ex: "/article/25#commentaire-12"
    private boolean lu;
    private LocalDateTime dateCreation;

    // Constructeurs, getters et setters
    public Notification() {}

    public Notification(int utilisateurId, int emetteurId, String type, String contenu, String lien) {
        this.utilisateurId = utilisateurId;
        this.emetteurId = emetteurId;
        this.type = type;
        this.contenu = contenu;
        this.lien = lien;
        this.lu = false;
    }

    // Getters/Setters...
}