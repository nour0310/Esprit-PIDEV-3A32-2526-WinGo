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

public class SummarizationService {

    // 🔁 REMPLACE CES 2 LIGNES AVEC TES INFORMATIONS RAPIDAPI
    private static final String API_KEY = "3dfa6a93aamsh0e8d1bb47c280c6p199c88jsnff11d2cc9132"; // Ta clé
    private static final String API_HOST = "text-summarization13.p.rapidapi.com"; // L'hôte

    private static final String API_URL = "https://" + API_HOST + "/summarize";
    private static final Gson gson = new Gson();

    // Classe pour stocker le résultat complet
    public static class SummaryResult {
        public boolean success;
        public String summary;
        public String[] keyPhrases;
        public int readabilityScore;
        public String sentiment;

        public String getFormattedKeyPhrases() {
            if (keyPhrases == null) return "";
            return String.join(", ", keyPhrases);
        }
    }

    public static CompletableFuture<SummaryResult> summarizeAsync(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(API_URL);
                post.setHeader("Content-Type", "application/json");
                post.setHeader("X-RapidAPI-Key", API_KEY);
                post.setHeader("X-RapidAPI-Host", API_HOST);

                // Corps de la requête
                JsonObject json = new JsonObject();
                json.addProperty("text", text);
                json.addProperty("length", "medium");  // short, medium, long
                json.addProperty("format", "paragraph"); // paragraph ou bullet

                post.setEntity(new StringEntity(json.toString(), "UTF-8"));

                try (CloseableHttpResponse response = client.execute(post)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();

                    SummaryResult result = new SummaryResult();
                    result.success = root.get("success").getAsBoolean();
                    result.summary = root.get("summary").getAsString();

                    // Extraction des key_phrases
                    if (root.has("key_phrases")) {
                        result.keyPhrases = gson.fromJson(root.getAsJsonArray("key_phrases"), String[].class);
                    }

                    if (root.has("readability_score")) {
                        result.readabilityScore = root.get("readability_score").getAsInt();
                    }

                    if (root.has("sentiment")) {
                        result.sentiment = root.get("sentiment").getAsString();
                    }

                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}