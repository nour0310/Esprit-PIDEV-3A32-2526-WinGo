package Entites;

import java.util.Objects;

public class Tag {
    private int id;
    private String nom;

    public Tag() {}

    public Tag(String nom) {
        this.nom = nom;
    }

    public Tag(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return id == tag.id || Objects.equals(nom, tag.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom);
    }

    @Override
    public String toString() {
        return "#" + nom;
    }
}