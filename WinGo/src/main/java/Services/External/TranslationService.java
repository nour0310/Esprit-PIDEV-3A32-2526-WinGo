package Services.External;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.CompletableFuture;

public class LibreTranslateService {

    private static final String API_URL = "https://libretranslate.de/translate";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Traduit un texte de manière asynchrone (ne bloque pas le thread JavaFX).
     * @param text Le texte à traduire.
     * @param sourceLang Code de la langue source (ex: "fr", "en", "auto" pour détection auto).
     * @param targetLang Code de la langue cible (ex: "en", "es", "ar").
     * @return Un CompletableFuture qui contiendra le texte traduit.
     */
    public static CompletableFuture<String> translateAsync(String text, String sourceLang, String targetLang) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(API_URL);
                post.setHeader("Content-Type", "application/json");

                // Construire le corps de la requête JSON
                String jsonBody = String.format(
                        "{\"q\":\"%s\", \"source\":\"%s\", \"target\":\"%s\", \"format\":\"text\"}",
                        escapeJson(text), sourceLang, targetLang
                );
                post.setEntity(new StringEntity(jsonBody, "UTF-8"));

                // Exécuter la requête
                try (CloseableHttpResponse response = client.execute(post)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JsonNode root = objectMapper.readTree(responseBody);
                    return root.get("translatedText").asText();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return text; // En cas d'erreur, retourner le texte original
            }
        });
    }

    // Méthode utilitaire simple pour échapper les guillemets dans le JSON
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Version synchrone simple pour les tests
    public static String translate(String text, String sourceLang, String targetLang) {
        try {
            return translateAsync(text, sourceLang, targetLang).get();
        } catch (Exception e) {
            e.printStackTrace();
            return text;
        }
    }
}