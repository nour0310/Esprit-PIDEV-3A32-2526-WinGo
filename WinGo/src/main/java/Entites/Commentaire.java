package Entites;

import java.util.Date;

public class Commentaire {
    private int id_commentaire;
    private String contenu;
    private Date dateCommentaire;
    private int id_article;   // FK vers article
    private int utilisateur;  // FK vers utilisateur.id

    public Commentaire() {}

    public Commentaire(String contenu, Date dateCommentaire, int id_article, int utilisateur) {
        this.contenu = contenu;
        this.dateCommentaire = dateCommentaire;
        this.id_article = id_article;
        this.utilisateur = utilisateur;
    }

    // GETTERS & SETTERS
    public int getId_commentaire() { return id_commentaire; }
    public void setId_commentaire(int id_commentaire) { this.id_commentaire = id_commentaire; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Date getDateCommentaire() { return dateCommentaire; }
    public void setDateCommentaire(Date dateCommentaire) { this.dateCommentaire = dateCommentaire; }

    public int getId_article() { return id_article; }
    public void setId_article(int id_article) { this.id_article = id_article; }

    public int getUtilisateur() { return utilisateur; }
    public void setUtilisateur(int utilisateur) { this.utilisateur = utilisateur; }
}