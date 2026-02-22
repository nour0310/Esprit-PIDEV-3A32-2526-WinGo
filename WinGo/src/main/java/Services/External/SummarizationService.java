package Services.External;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class GPT5NanoService {

    private static final String API_KEY = "VOTRE_CLE_API_MARKET"; // À obtenir sur api.market
    private static final String BASE_URL = "https://prod.api.market/api/v1/magicapi/gpt-5-nano";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    /**
     * Soumet une demande de résumé et retourne un CompletableFuture avec le texte résumé.
     */
    public static CompletableFuture<String> summarizeAsync(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Créer la requête de soumission
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", "Résume le texte suivant : " + text);

                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", "gpt-5-nano");
                requestBody.add("messages", gson.toJsonTree(new JsonObject[]{message}));

                HttpRequest submitRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("x-api-market-key", API_KEY)
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                // 2. Envoyer la requête et obtenir l'ID de prédiction
                HttpResponse<String> submitResponse = client.send(submitRequest, HttpResponse.BodyHandlers.ofString());

                if (submitResponse.statusCode() != 201 && submitResponse.statusCode() != 200) {
                    throw new RuntimeException("Erreur soumission API: " + submitResponse.body());
                }

                JsonObject submitJson = gson.fromJson(submitResponse.body(), JsonObject.class);
                String predictionId = submitJson.get("id").getAsString();

                // 3. Poller le statut jusqu'à obtention du résultat
                String summary = pollForResult(predictionId);
                return summary;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String pollForResult(String predictionId) throws Exception {
        String status = "starting";
        int maxAttempts = 60; // 60 secondes max
        int attempt = 0;

        while (attempt < maxAttempts) {
            HttpRequest statusRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/predictions/" + predictionId))
                    .header("x-api-market-key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> statusResponse = client.send(statusRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject statusJson = gson.fromJson(statusResponse.body(), JsonObject.class);
            status = statusJson.get("status").getAsString();

            if ("succeeded".equals(status)) {
                return statusJson.getAsJsonArray("choices").get(0).getAsJsonObject()
                        .getAsJsonObject("message").get("content").getAsString();
            } else if ("failed".equals(status)) {
                throw new RuntimeException("La prédiction a échoué.");
            }

            attempt++;
            Thread.sleep(1000); // Attendre 1 seconde avant de réessayer
        }
        throw new RuntimeException("Délai d'attente dépassé pour la prédiction.");
    }
}