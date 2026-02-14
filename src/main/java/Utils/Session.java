package Utils;

public final class Session {

    private static int currentUserId = 1;          // ✅ ID FIXE (change selon ton utilisateur)
    private static String currentUserType = "COMMERCANT"; // ✅ ou "CLIENT"

    private Session() {}

    public static int getUserId() {
        return currentUserId;
    }

    public static String getUserType() {
        return currentUserType;
    }

    public static boolean isLoggedIn() {
        return currentUserId > 0;
    }

    public static boolean isCommercant() {
        return "COMMERCANT".equalsIgnoreCase(currentUserType);
    }

    // ✅ plus tard quand login sera prêt
    public static void setUser(int id, String type) {
        currentUserId = id;
        currentUserType = type;
    }

    public static void clear() {
        currentUserId = -1;
        currentUserType = null;
    }
}
public static int getUserId() { return 1; } // temporaire
public static boolean isLoggedIn() { return true; }
public static boolean isCommercant() { return true; }