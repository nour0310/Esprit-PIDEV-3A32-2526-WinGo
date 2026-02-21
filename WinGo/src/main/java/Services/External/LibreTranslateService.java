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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LibreTranslateService {

    // Liste d'instances : locale en premier, puis publiques
    private static final List<String> API_URLS = Arrays.asList(
            "http://localhost:5000/translate",
            "https://libretranslate.de/translate",
            "https://translate.terraprint.co/translate",
            "https://lt.vern.cc/translate"
    );

    private static final Gson gson = new Gson();

    public static CompletableFuture<String> translateAsync(String text, String sourceLang, String targetLang) {
        return CompletableFuture.supplyAsync(() -> {
            if (text == null || text.trim().isEmpty()) {
                return text;
            }

            for (String apiUrl : API_URLS) {
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    HttpPost post = new HttpPost(apiUrl);
                    post.setHeader("Content-Type", "application/json");

                    JsonObject json = new JsonObject();
                    json.addProperty("q", text);
                    json.addProperty("source", sourceLang);
                    json.addProperty("target", targetLang);
                    json.addProperty("format", "text");

                    post.setEntity(new StringEntity(gson.toJson(json), "UTF-8"));

                    System.out.println("Tentative de traduction avec : " + apiUrl);

                    try (CloseableHttpResponse response = client.execute(post)) {
                        int statusCode = response.getStatusLine().getStatusCode();
                        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

                        if (statusCode == 200) {
                            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
                            return responseJson.get("translatedText").getAsString();
                        } else {
                            System.err.println("Erreur API " + apiUrl + " (code " + statusCode + ") : " + responseBody);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Exception avec l'URL " + apiUrl + " : " + e.getMessage());
                }
            }

            System.err.println("Toutes les instances de traduction ont échoué. Texte original conservé.");
            return text;
        });
    }
}