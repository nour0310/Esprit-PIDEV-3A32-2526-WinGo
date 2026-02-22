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

                JsonObject json = new JsonObject();
                json.addProperty("text", text);
                json.addProperty("length", "medium");
                json.addProperty("format", "paragraph");

                post.setEntity(new StringEntity(json.toString(), "UTF-8"));

                try (CloseableHttpResponse response = client.execute(post)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    System.out.println("Réponse de l'API : " + responseBody); // DEBUG

                    JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();

                    SummaryResult result = new SummaryResult();

                    // Vérifier la présence des champs avant de les lire
                    JsonElement successElem = root.get("success");
                    result.success = successElem != null && successElem.getAsBoolean();

                    JsonElement summaryElem = root.get("summary");
                    if (summaryElem != null) {
                        result.summary = summaryElem.getAsString();
                    } else {
                        // Si pas de summary, essayer un autre champ possible
                        summaryElem = root.get("summarized_text");
                        if (summaryElem != null) {
                            result.summary = summaryElem.getAsString();
                        } else {
                            result.summary = "Résumé non disponible";
                        }
                    }

                    JsonElement keyPhrasesElem = root.get("key_phrases");
                    if (keyPhrasesElem != null && keyPhrasesElem.isJsonArray()) {
                        result.keyPhrases = gson.fromJson(keyPhrasesElem, String[].class);
                    }

                    JsonElement readabilityElem = root.get("readability_score");
                    if (readabilityElem != null) {
                        result.readabilityScore = readabilityElem.getAsInt();
                    }

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