package Entites;

import java.sql.Timestamp;

public class Produit {
    private int idProduit;
    private int idCommercant;
    private String nom;
    private String description;
    private double prix;
    private String region;
    private String categorie;
    private int stock;
    private String image;
    private Timestamp dateAjout;

    public Produit() {}

    public Produit(int idProduit, int idCommercant, String nom, String description, double prix,
                   String region, String categorie, int stock, String image, Timestamp dateAjout) {
        this.idProduit = idProduit;
        this.idCommercant = idCommercant;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.region = region;
        this.categorie = categorie;
        this.stock = stock;
        this.image = image;
        this.dateAjout = dateAjout;
    }

    public int getIdProduit() { return idProduit; }
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }

    public int getIdCommercant() { return idCommercant; }
    public void setIdCommercant(int idCommercant) { this.idCommercant = idCommercant; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Timestamp getDateAjout() { return dateAjout; }
    public void setDateAjout(Timestamp dateAjout) { this.dateAjout = dateAjout; }

    @Override
    public String toString() {
        return "Produit{" +
                "idProduit=" + idProduit +
                ", idCommercant=" + idCommercant +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", stock=" + stock +
                '}';
    }
}