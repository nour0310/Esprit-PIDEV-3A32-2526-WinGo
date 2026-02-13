package Entites;

public class Blog {
    private int id_article;        // correspond à la colonne dans la base
    private String titre;
    private String contenu;
    private String image;
    private String auteur;
    private String region;
    private String categorie;

    public Blog() {}

    public Blog(String titre, String contenu, String image,
                String auteur, String region, String categorie) {
        this.titre = titre;
        this.contenu = contenu;
        this.image = image;
        this.auteur = auteur;
        this.region = region;
        this.categorie = categorie;
    }

    // GETTERS & SETTERS
    public int getId_article() { return id_article; }
    public void setId_article(int id_article) { this.id_article = id_article; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
}