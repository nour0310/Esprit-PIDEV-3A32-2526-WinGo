package Entites;

import java.time.LocalDateTime;

public class Blog {
    private int id;                  // correspond à id_article
    private String titre;
    private String contenu;
    private LocalDateTime datePublication;
    private String image;
    private String region;
    private String categorie;
    private int auteur;               // FK vers utilisateur.id
    private String auteurNom;         // non persisté, pour affichage

    public Blog() {}

    public Blog(String titre, String contenu, String image, String region,
                String categorie, int auteur) {
        this.titre = titre;
        this.contenu = contenu;
        this.image = image;
        this.region = region;
        this.categorie = categorie;
        this.auteur = auteur;
    }

    public Blog(int id, String titre, String contenu, LocalDateTime datePublication,
                String image, String region, String categorie, int auteur) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.image = image;
        this.region = region;
        this.categorie = categorie;
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

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public int getAuteur() { return auteur; }
    public void setAuteur(int auteur) { this.auteur = auteur; }

    public String getAuteurNom() { return auteurNom; }
    public void setAuteurNom(String auteurNom) { this.auteurNom = auteurNom; }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", auteur=" + auteur +
                '}';
    }
}