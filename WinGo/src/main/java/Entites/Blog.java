package Entites;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Blog {
    private int id;
    private String titre;
    private String contenu;
    private LocalDateTime datePublication;
    private int auteur;
    private String auteurNom; // non persisté
    private String image;
    private String region;
    private String categorie;
    private List<Tag> tags = new ArrayList<>();

    public Blog() {}

    public Blog(String titre, String contenu, int auteur, String image, String region, String categorie) {
        this.titre = titre;
        this.contenu = contenu;
        this.auteur = auteur;
        this.image = image;
        this.region = region;
        this.categorie = categorie;
    }

    public Blog(int id, String titre, String contenu, LocalDateTime datePublication, int auteur, String image, String region, String categorie) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.auteur = auteur;
        this.image = image;
        this.region = region;
        this.categorie = categorie;
    }

    // Getters et Setters
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

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }

    @Override
    public String toString() {
        return "Blog{" + "id=" + id + ", titre='" + titre + '\'' + ", auteur=" + auteur + '}';
    }
}