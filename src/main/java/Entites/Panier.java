package Entites;

public class Panier {
    private int idPanier;
    private int idCommande;
    private int idProduit;
    private int quantite;
    private Double prixUnitaire; // peut être null si tu ne l'utilises pas

    public Panier() {}

    public Panier(int idPanier, int idCommande, int idProduit, int quantite, Double prixUnitaire) {
        this.idPanier = idPanier;
        this.idCommande = idCommande;
        this.idProduit = idProduit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int getIdPanier() { return idPanier; }
    public void setIdPanier(int idPanier) { this.idPanier = idPanier; }

    public int getIdCommande() { return idCommande; }
    public void setIdCommande(int idCommande) { this.idCommande = idCommande; }

    public int getIdProduit() { return idProduit; }
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    @Override
    public String toString() {
        return "PanierItem{idPanier=" + idPanier + ", idCommande=" + idCommande +
                ", idProduit=" + idProduit + ", quantite=" + quantite +
                ", prixUnitaire=" + prixUnitaire + "}";
    }
}