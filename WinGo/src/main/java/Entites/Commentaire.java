package Entites;

import java.util.Date;

public class Commentaire {

    private int id;              // correspond à commentaire.id
    private String contenu;
    private Date dateCommentaire;
    private int articleId;       // correspond à commentaire.article_id
    private int utilisateur;     // correspond à commentaire.utilisateur

    public Commentaire() {}

    public Commentaire(String contenu, Date dateCommentaire, int articleId, int utilisateur) {
        this.contenu = contenu;
        this.dateCommentaire = dateCommentaire;
        this.articleId = articleId;
        this.utilisateur = utilisateur;
    }

    // GETTERS & SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Date getDateCommentaire() { return dateCommentaire; }
    public void setDateCommentaire(Date dateCommentaire) { this.dateCommentaire = dateCommentaire; }

    // ⚠️ C'EST CELUI QUE TON CRUD ATTEND
    public int getArticleId() { return articleId; }
    public void setArticleId(int articleId) { this.articleId = articleId; }

    public int getUtilisateur() { return utilisateur; }
    public void setUtilisateur(int utilisateur) { this.utilisateur = utilisateur; }
}