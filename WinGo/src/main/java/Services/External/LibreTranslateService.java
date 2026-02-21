package Services.External;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.CompletableFuture;

public class LibreTranslateService {

    // Utilisation d'une instance publique alternative plus stable
    private static final String API_URL = "https://translate.argosopentech.com/translate";
    private static final Gson gson = new Gson();

    public static CompletableFuture<String> translateAsync(String text, String sourceLang, String targetLang) {
        return CompletableFuture.supplyAsync(() -> {
            // Vérifier que le texte n'est pas vide
            if (text == null || text.trim().isEmpty()) {
                return text;
            }

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(API_URL);
                post.setHeader("Content-Type", "application/json");

                JsonObject json = new JsonObject();
                json.addProperty("q", text);
                json.addProperty("source", sourceLang);
                json.addProperty("target", targetLang);
                json.addProperty("format", "text");

                post.setEntity(new StringEntity(gson.toJson(json), "UTF-8"));

                // Log pour déboguer (optionnel)
                System.out.println("Envoi requête de traduction...");

                try (CloseableHttpResponse response = client.execute(post)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

                    if (statusCode != 200) {
                        System.err.println("Erreur API (code " + statusCode + ") : " + responseBody);
                        return text; // retourne le texte original en cas d'erreur
                    }

                    JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
                    return responseJson.get("translatedText").getAsString();
                }
            } catch (Exception e) {
                System.err.println("Exception lors de la traduction : " + e.getMessage());
                e.printStackTrace();
                return text;
            }
        });
    }
}