package Entites;

import java.time.LocalDateTime;

public class Blog {
    private int id;
    private String titre;
    private String contenu;
    private LocalDateTime datePublication;
    private int auteur;          // FK vers utilisateur.id
    private String auteurNom;     // non persisté, pour affichage

    public Blog() {}

    public Blog(String titre, String contenu, int auteur) {
        this.titre = titre;
        this.contenu = contenu;
        this.auteur = auteur;
    }

    public Blog(int id, String titre, String contenu, LocalDateTime datePublication, int auteur) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.auteur = auteur;
    }

    // Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public int getAuteur() { return auteur; }
    public void setAuteur(int auteur) { this.auteur = auteur; }

    public String getAuteurNom() { return auteurNom; }
    public void setAuteurNom(String auteurNom) { this.auteurNom = auteurNom; }

    @Override
    public String toString() {
        return "Blog{" + "id=" + id + ", titre='" + titre + '\'' + ", auteur=" + auteur + '}';
    }
}