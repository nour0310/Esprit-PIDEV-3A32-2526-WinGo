package Entites;

import java.util.Date;

public class Commentaire {
    private int id;
    private String contenu;
    private Date dateCommentaire;
    private int blogId;
    private String utilisateur;

    public Commentaire() {}

    public Commentaire(String contenu, Date dateCommentaire, int blogId, String utilisateur) {
        this.contenu = contenu;
        this.dateCommentaire = dateCommentaire;
        this.blogId = blogId;
        this.utilisateur = utilisateur;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public Date getDateCommentaire() { return dateCommentaire; }
    public void setDateCommentaire(Date dateCommentaire) { this.dateCommentaire = dateCommentaire; }
    public int getBlogId() { return blogId; }
    public void setBlogId(int blogId) { this.blogId = blogId; }
    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }

    @Override
    public String toString() {
        return "Commentaire [id=" + id + ", contenu=" + contenu + ", blogId=" + blogId + ", utilisateur=" + utilisateur + "]";
    }
}
