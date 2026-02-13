package Controlles;

public class CartItem {
    private final int idProduit;
    private final String nom;
    private final double prix;
    private int qty;

    public CartItem(int idProduit, String nom, double prix, int qty) {
        this.idProduit = idProduit;
        this.nom = nom;
        this.prix = prix;
        this.qty = qty;
    }

    public int getIdProduit() { return idProduit; }
    public String getNom() { return nom; }
    public double getPrix() { return prix; }
    public int getQty() { return qty; }

    public void setQty(int qty) { this.qty = qty; }

    public double getSubtotal() { return prix * qty; }
}