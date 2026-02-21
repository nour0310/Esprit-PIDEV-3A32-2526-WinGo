package Services.External;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class MyMemoryService {

    private static final String API_URL = "https://api.mymemory.translated.net/get";

    public static CompletableFuture<String> translateAsync(String text, String sourceLang, String targetLang) {
        return CompletableFuture.supplyAsync(() -> {
            if (text == null || text.trim().isEmpty()) {
                return text;
            }

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
                // Construction de l'URL : paire de langues source|target
                String url = String.format("%s?q=%s&langpair=%s|%s",
                        API_URL, encodedText, sourceLang, targetLang);

                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

                try (CloseableHttpResponse response = client.execute(request)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    // Le chemin JSON est responseData.translatedText
                    return json.getAsJsonObject("responseData").get("translatedText").getAsString();
                }
            } catch (Exception e) {
                System.err.println("Erreur MyMemory : " + e.getMessage());
                e.printStackTrace();
                return text; // fallback
            }
        });
    }
}