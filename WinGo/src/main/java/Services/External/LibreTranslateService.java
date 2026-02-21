package Services.External;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class LibreTranslateService {

    // Utilisation de l'API MyMemory (gratuite, jusqu'à 1000 mots/jour sans clé)
    private static final String MYMEMORY_URL = "https://api.mymemory.translated.net/get";

    public static CompletableFuture<String> translateAsync(String text, String sourceLang, String targetLang) {
        return CompletableFuture.supplyAsync(() -> {
            if (text == null || text.trim().isEmpty()) {
                return text;
            }

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                // Encoder le texte pour l'URL
                String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
                String url = String.format("%s?q=%s&langpair=%s|%s", MYMEMORY_URL, encodedText, sourceLang, targetLang);

                HttpGet request = new HttpGet(url);
                request.setHeader("Accept", "application/json");

                System.out.println("Tentative de traduction avec MyMemory...");

                try (CloseableHttpResponse response = client.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

                    if (statusCode == 200) {
                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                        // La réponse contient une structure "responseData" avec "translatedText"
                        JsonObject responseData = json.getAsJsonObject("responseData");
                        if (responseData != null && responseData.has("translatedText")) {
                            return responseData.get("translatedText").getAsString();
                        }
                    } else {
                        System.err.println("Erreur MyMemory (code " + statusCode + ") : " + responseBody);
                    }
                }
            } catch (Exception e) {
                System.err.println("Exception avec MyMemory : " + e.getMessage());
            }

            // Si MyMemory échoue, retourner le texte original
            System.err.println("La traduction a échoué. Texte original conservé.");
            return text;
        });
    }
}