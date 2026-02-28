package Services.External;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class HuggingFaceSummaryService {

    // 🔐 REMPLACEZ PAR VOTRE NOUVEAU TOKEN


    // ✅ Nouvelle URL avec /hf-inference/models/
    private static final String API_URL = "https://router.huggingface.co/hf-inference/models/facebook/bart-large-cnn";

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public static CompletableFuture<String> summarizeAsync(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("inputs", text);

                JsonObject parameters = new JsonObject();
                parameters.addProperty("max_length", 150);
                parameters.addProperty("min_length", 30);
                parameters.addProperty("do_sample", false);
                requestBody.add("parameters", parameters);

                System.out.println("📡 Appel à l'URL : " + API_URL);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Authorization", "Bearer " )
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(60))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                int statusCode = response.statusCode();
                String responseBody = response.body();

                System.out.println("📥 Statut : " + statusCode);
                System.out.println("📥 Réponse : " + responseBody);

                if (statusCode == 200) {
                    return extractSummaryFromJson(responseBody);
                } else if (statusCode == 503) {
                    System.out.println("⏳ Modèle en chargement, attente de 30 secondes...");
                    Thread.sleep(30000);
                    return summarizeAsync(text).join();
                } else {
                    throw new RuntimeException("Erreur API (" + statusCode + ") : " + responseBody);
                }

            } catch (Exception e) {
                throw new RuntimeException("Échec de la génération du résumé : " + e.getMessage(), e);
            }
        });
    }

    private static String extractSummaryFromJson(String jsonResponse) {
        JsonArray array = JsonParser.parseString(jsonResponse).getAsJsonArray();
        if (array != null && array.size() > 0) {
            JsonObject firstObject = array.get(0).getAsJsonObject();
            return firstObject.get("summary_text").getAsString();
        }
        return "Impossible d'extraire le résumé de la réponse.";
    }
}