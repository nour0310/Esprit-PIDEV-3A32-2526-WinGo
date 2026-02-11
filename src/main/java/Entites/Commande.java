package Entites;

import java.time.LocalDateTime;

public class Commande {
    private int idCommande;
    private int idUser;
    private String status; // panier, en_cours, livree, annulee
    private LocalDateTime dateCommande;

    public Commande() {}

    public Commande(int idCommande, int idUser, String status, LocalDateTime dateCommande) {
        this.idCommande = idCommande;
        this.idUser = idUser;
        this.status = status;
        this.dateCommande = dateCommande;
    }

    public int getIdCommande() { return idCommande; }
    public void setIdCommande(int idCommande) { this.idCommande = idCommande; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }

    @Override
    public String toString() {
        return "Commande{idCommande=" + idCommande + ", idUser=" + idUser +
                ", status='" + status + "', dateCommande=" + dateCommande + "}";
    }
}