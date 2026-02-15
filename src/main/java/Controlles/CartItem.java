package Controlles;

public class CartItem {
    private final int idProduit;
    private final String nom;
    private final String image;   // vient de produit.image
    private final double prix;
    private int qty;

    public CartItem(int idProduit, String nom, String image, double prix, int qty) {
        this.idProduit = idProduit;
        this.nom = nom;
        this.image = image;
        this.prix = prix;
        this.qty = qty;
    }

    public int getIdProduit() { return idProduit; }
    public String getNom() { return nom; }
    public String getImage() { return image; }
    public double getPrix() { return prix; }
    public int getQty() { return qty; }

    public void setQty(int qty) { this.qty = qty; }

    public double getSubtotal() { return prix * qty; }
}