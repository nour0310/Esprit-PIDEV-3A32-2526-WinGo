package Services.External;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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

    private static final String API_KEY = "3dfa6a93aamsh0e8d1bb47c280c6p199c88jsnff11d2cc9132";
    private static final String API_HOST = "text-summarization13.p.rapidapi.com";
    private static final String API_URL = "https://" + API_HOST + "/summarize";
    private static final Gson gson = new Gson();

    public static class SummaryResult {
        public String summary;
        public String[] keyPhrases;
        public int readabilityScore;
        public String sentiment;
        public boolean success;

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

                JsonObject json = new JsonObject();
                json.addProperty("text", text);
                json.addProperty("length", "medium");
                json.addProperty("format", "paragraph");

                post.setEntity(new StringEntity(json.toString(), "UTF-8"));

                try (CloseableHttpResponse response = client.execute(post)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    System.out.println("🔍 Réponse de l'API : " + responseBody); // DEBUG

                    JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
                    SummaryResult result = new SummaryResult();

                    // Chercher le résumé dans différents champs possibles
                    JsonElement summaryElem = root.get("summary");
                    if (summaryElem == null) summaryElem = root.get("summarized_text");
                    if (summaryElem == null) summaryElem = root.get("result");
                    result.summary = (summaryElem != null) ? summaryElem.getAsString() : "Résumé non disponible";

                    // Mots-clés
                    JsonElement keyElem = root.get("key_phrases");
                    if (keyElem != null && keyElem.isJsonArray()) {
                        result.keyPhrases = gson.fromJson(keyElem, String[].class);
                    }

                    // Score de lisibilité
                    JsonElement readabilityElem = root.get("readability_score");
                    if (readabilityElem != null) {
                        result.readabilityScore = readabilityElem.getAsInt();
                    }

                    // Sentiment
                    JsonElement sentimentElem = root.get("sentiment");
                    if (sentimentElem != null) {
                        result.sentiment = sentimentElem.getAsString();
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