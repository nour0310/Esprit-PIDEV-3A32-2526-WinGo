package Utils;

import Entites.Utilisateur;

/**
 * Holds the currently logged-in user across controllers.
 */
public class Session {
    private static Utilisateur currentUser;

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}
