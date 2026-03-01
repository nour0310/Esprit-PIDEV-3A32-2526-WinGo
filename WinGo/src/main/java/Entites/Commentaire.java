package Entites;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Commentaire {
    private int id;
    private String contenu;
    private LocalDateTime dateCommentaire;
    private int utilisateur;       // FK vers utilisateur.id
    private int articleId;         // FK vers article.id
    private Integer parentId;      // FK vers commentaire.id (peut Ãªtre null)
    private String utilisateurNom; // non persistÃ©, pour affichage
    private String articleTitre;   // non persistÃ©, pour affichage
    private List<Commentaire> replies; // non persistÃ©, pour stocker les rÃ©ponses

    public Commentaire() {
        this.replies = new ArrayList<>();
    }
    
    // ... later in the getters/setters ...
    public String getArticleTitre() { return articleTitre; }
    public void setArticleTitre(String articleTitre) { this.articleTitre = articleTitre; }

    public Commentaire(String contenu, int utilisateur, int articleId) {
        this.contenu = contenu;
        this.utilisateur = utilisateur;
        this.articleId = articleId;
        this.replies = new ArrayList<>();
    }

    public Commentaire(int id, String contenu, LocalDateTime dateCommentaire, int utilisateur, int articleId, Integer parentId) {
        this.id = id;
        this.contenu = contenu;
        this.dateCommentaire = dateCommentaire;
        this.utilisateur = utilisateur;
        this.articleId = articleId;
        this.parentId = parentId;
        this.replies = new ArrayList<>();
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

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }

    public List<Commentaire> getReplies() { return replies; }
    public void setReplies(List<Commentaire> replies) { this.replies = replies; }

    @Override
    public String toString() {
        return "Commentaire{" + "id=" + id + ", articleId=" + articleId + ", parentId=" + parentId + '}';
    }
}