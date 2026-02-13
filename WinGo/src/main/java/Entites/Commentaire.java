package Entites;

import java.time.LocalDateTime;

public class Commentaire {
    private int id;
    private String contenu;
    private LocalDateTime dateCommentaire;
    private int utilisateur;       // FK vers utilisateur.id
    private int articleId;         // FK vers article.id
    private String utilisateurNom;  // non persisté, pour affichage

    public Commentaire() {}

    public Commentaire(String contenu, int utilisateur, int articleId) {
        this.contenu = contenu;
        this.utilisateur = utilisateur;
        this.articleId = articleId;
    }

    public Commentaire(int id, String contenu, LocalDateTime dateCommentaire, int utilisateur, int articleId) {
        this.id = id;
        this.contenu = contenu;
        this.dateCommentaire = dateCommentaire;
        this.utilisateur = utilisateur;
        this.articleId = articleId;
    }

    // Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateCommentaire() { return dateCommentaire; }
    public void setDateCommentaire(LocalDateTime dateCommentaire) { this.dateCommentaire = dateCommentaire; }

    public int getUtilisateur() { return utilisateur; }
    public void setUtilisateur(int utilisateur) { this.utilisateur = utilisateur; }

    public int getArticleId() { return articleId; }
    public void setArticleId(int articleId) { this.articleId = articleId; }

    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }

    @Override
    public String toString() {
        return "Commentaire{" + "id=" + id + ", articleId=" + articleId + '}';
    }
}