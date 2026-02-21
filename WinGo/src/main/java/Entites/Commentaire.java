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
    private Integer parentId;      // FK vers commentaire.id (nullable)
    private String utilisateurNom;  // non persisté, pour affichage
    private List<Commentaire> reponses = new ArrayList<>(); // pour les réponses

    public Commentaire() {}

    public Commentaire(String contenu, int utilisateur, int articleId, Integer parentId) {
        this.contenu = contenu;
        this.utilisateur = utilisateur;
        this.articleId = articleId;
        this.parentId = parentId;
    }

    public Commentaire(int id, String contenu, LocalDateTime dateCommentaire, int utilisateur, int articleId, Integer parentId) {
        this.id = id;
        this.contenu = contenu;
        this.dateCommentaire = dateCommentaire;
        this.utilisateur = utilisateur;
        this.articleId = articleId;
        this.parentId = parentId;
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

    public List<Commentaire> getReponses() { return reponses; }
    public void setReponses(List<Commentaire> reponses) { this.reponses = reponses; }

    @Override
    public String toString() {
        return "Commentaire{" + "id=" + id + ", articleId=" + articleId + ", parentId=" + parentId + '}';
    }
}