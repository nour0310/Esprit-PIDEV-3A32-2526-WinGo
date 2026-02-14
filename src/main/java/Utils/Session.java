package Utils;

public final class Session {
    private static int currentUserId = -1;
    private static String currentUserType = null; // CLIENT / COMMERCANT / ADMIN

    private Session() {}

    public static void setUser(int userId, String type) {
        currentUserId = userId;
        currentUserType = type;
    }

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

    public static void clear() {
        currentUserId = -1;
        currentUserType = null;
    }
}