package Entites;

import java.sql.Timestamp;

public class Reservation {
    private int id;
    private String user;
    private String exp;
    private Timestamp date;
    private String statut;
    public Reservation() {}
    public Reservation(String user, String exp, String statut, Timestamp date) {
        this.user = user;
        this.exp = exp;
        this.statut = statut;
        this.date = date;
    }
    public Reservation(int id, String user, String exp, String statut, Timestamp date) {
        this.id = id;
        this.user = user;
        this.exp = exp;
        this.statut = statut;
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "user=" + user +
                ", statut='" + statut + '\'' +
                ", Experience='" + exp + '\'' +
                ", date=" + date +
                '}';
    }
}
