package Entites;


import java.time.LocalDateTime;
public class Transport {
    private int id;
    private String type;
    private String capacite;
    private Float tarif;
    private String depart;
    private String arrivee;
    private LocalDateTime datedepart;
    public Transport() {}
    public Transport(String type, String capacite, Float tarif, String depart, String arrivee,LocalDateTime datedepart) {
        this.type = type;
        this.capacite = capacite;
        this.tarif = tarif;
        this.depart = depart;
        this.arrivee = arrivee;
        this.datedepart = datedepart;

    }
    public Transport(int id, String type, String capacite, Float tarif, String depart, String arrivee,LocalDateTime datedepart) {
        this.id = id;
        this.type = type;
        this.capacite = capacite;
        this.tarif = tarif;
        this.depart = depart;
        this.arrivee = arrivee;
        this.datedepart = datedepart;

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCapacite() {
        return capacite;
    }

    public void setCapacite(String capacite) {
        this.capacite = capacite;
    }

    public Float getTarif() {
        return tarif;
    }

    public void setTarif(Float tarif) {
        this.tarif = tarif;
    }
    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }
    public String getArrivee() {return arrivee;}

    public void setArrivee(String arrivee) {
        this.arrivee = arrivee;
    }
    public LocalDateTime getDateDepart() {return datedepart;}

    public void setDateDepart(LocalDateTime datedepart) {
        this.datedepart = datedepart;
    }





    @Override
    public String toString() {
        return "Reservation{" +
                "user=" + type +
                ", id=" + id +
                ", tarif='" + tarif + '\'' +
                ", Experience='" + capacite + '\'' +
                ", départ=" + depart +'\'' +
                ", arrivee=" + arrivee +'\'' +
                ", date départ=" + datedepart +
                '}';
    }
}
