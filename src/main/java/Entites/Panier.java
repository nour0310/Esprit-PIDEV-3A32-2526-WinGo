package Entites;

import java.sql.Timestamp;

public class Panier {
    private int idPanier;
    private int idUser;
    private int idProduit;
    private int quantite;
    private Double prixUnitaire;
    private Timestamp dateAjout; // optionnel si tu ne l’utilises pas
    private Double total;        // optionnel si tu ne l’utilises pas

    public Panier() {}

    // INSERT (sans idPanier, sans dateAjout)
    public Panier(int idUser, int idProduit, int quantite, Double prixUnitaire, Double total) {
        this.idUser = idUser;
        this.idProduit = idProduit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.total = total;
    }

    // UPDATE (avec idPanier)
    public Panier(int idPanier, int idUser, int idProduit, int quantite, Double prixUnitaire, Double total) {
        this.idPanier = idPanier;
        this.idUser = idUser;
        this.idProduit = idProduit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.total = total;
    }

    public int getIdPanier() { return idPanier; }
    public void setIdPanier(int idPanier) { this.idPanier = idPanier; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public int getIdProduit() { return idProduit; }
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public Timestamp getDateAjout() { return dateAjout; }
    public void setDateAjout(Timestamp dateAjout) { this.dateAjout = dateAjout; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}