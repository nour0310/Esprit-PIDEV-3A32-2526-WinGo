package Entites;

import java.time.LocalDateTime;

public class Like {
    private int id;
    private int utilisateurId;
    private int articleId;
    private LocalDateTime dateLike;

    public Like() {}

    public Like(int utilisateurId, int articleId) {
        this.utilisateurId = utilisateurId;
        this.articleId = articleId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public int getArticleId() { return articleId; }
    public void setArticleId(int articleId) { this.articleId = articleId; }

    public LocalDateTime getDateLike() { return dateLike; }
    public void setDateLike(LocalDateTime dateLike) { this.dateLike = dateLike; }
}