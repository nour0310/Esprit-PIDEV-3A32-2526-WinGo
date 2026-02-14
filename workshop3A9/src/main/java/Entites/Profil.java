package Entites;

public class Profil {

    private int id;
    private String bio;
    private String image;
    private int utilisateurId;

    public Profil() {
    }

    public Profil(String bio, String image, int utilisateurId) {
        this.bio = bio;
        this.image = image;
        this.utilisateurId = utilisateurId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
}
