package Entites;

public class Commercant {
    private int idCommercant;
    private String nomBoutique;

    public Commercant() {}

    public Commercant(int idCommercant, String nomBoutique) {
        this.idCommercant = idCommercant;
        this.nomBoutique = nomBoutique;
    }

    public int getIdCommercant() { return idCommercant; }
    public void setIdCommercant(int idCommercant) { this.idCommercant = idCommercant; }

    public String getNomBoutique() { return nomBoutique; }
    public void setNomBoutique(String nomBoutique) { this.nomBoutique = nomBoutique; }

    @Override
    public String toString() {
        return idCommercant + " - " + nomBoutique;
    }
}