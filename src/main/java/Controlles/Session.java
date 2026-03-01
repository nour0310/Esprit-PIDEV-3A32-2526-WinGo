package Controlles;

/**
 * Singleton qui stocke les informations de l'utilisateur connecté.
 * À appeler une seule fois après le login, puis lisible partout dans l'app.
 *
 * Utilisation après login :
 *   Session.getInstance().login(userId, email, nom);
 *
 * Utilisation dans n'importe quel contrôleur :
 *   int id    = Session.getInstance().getUserId();
 *   String mail = Session.getInstance().getEmail();
 */
public class Session {

    // ===== Singleton =====
    private static Session instance;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    // ===== Données utilisateur =====
    private int    userId   = -1;          // -1 = non connecté
    private String email    = "";
    private String nom      = "Utilisateur";
    private boolean isAdmin = false;

    // ===== Méthodes =====

    /** Appeler cette méthode juste après la vérification du login en BDD */
    public void login(int userId, String email, String nom) {
        this.userId = userId;
        this.email  = email != null ? email : "";
        this.nom    = nom   != null ? nom   : "Utilisateur";
    }

    /** Surcharge avec le rôle admin */
    public void login(int userId, String email, String nom, boolean isAdmin) {
        login(userId, email, nom);
        this.isAdmin = isAdmin;
    }

    /** Réinitialiser à la déconnexion */
    public void logout() {
        this.userId  = -1;
        this.email   = "";
        this.nom     = "Utilisateur";
        this.isAdmin = false;
    }

    /** Vérifie si un utilisateur est connecté */
    public boolean isConnecte() {
        return userId != -1;
    }

    // ===== Getters =====
    public int     getUserId() { return userId; }
    public String  getEmail()  { return email;  }
    public String  getNom()    { return nom;    }
    public boolean isAdmin()   { return isAdmin; }
}