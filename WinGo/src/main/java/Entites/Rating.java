package Entites;

import java.time.LocalDateTime;

public class Rating {
    private int id;
    private int utilisateurId;
    private int articleId;
    private int note; // 1 à 5
    private LocalDateTime dateRating;

    public Rating() {}

    public Rating(int utilisateurId, int articleId, int note) {
        this.utilisateurId = utilisateurId;
        this.articleId = articleId;
        this.note = note;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public int getArticleId() { return articleId; }
    public void setArticleId(int articleId) { this.articleId = articleId; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public LocalDateTime getDateRating() { return dateRating; }
    public void setDateRating(LocalDateTime dateRating) { this.dateRating = dateRating; }
}