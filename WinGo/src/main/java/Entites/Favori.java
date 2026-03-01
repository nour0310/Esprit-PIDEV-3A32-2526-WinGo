package Entites;

import java.time.LocalDateTime;

public class Favori {
    private int utilisateurId;
    private int articleId;
    private LocalDateTime dateAjout;

    public Favori() {}

    public Favori(int utilisateurId, int articleId) {
        this.utilisateurId = utilisateurId;
        this.articleId = articleId;
    }

    // Getters et setters
    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public int getArticleId() { return articleId; }
    public void setArticleId(int articleId) { this.articleId = articleId; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }
}