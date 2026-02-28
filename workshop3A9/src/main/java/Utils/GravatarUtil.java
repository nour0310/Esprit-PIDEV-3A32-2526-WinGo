package Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GravatarUtil {

    public static String getGravatarURL(String email, int size) {
        String hash = md5Hex(email.trim().toLowerCase());
        return "https://www.gravatar.com/avatar/" + hash + "?s=" + size + "&d=identicon";
    }

    private static String md5Hex(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(message.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
