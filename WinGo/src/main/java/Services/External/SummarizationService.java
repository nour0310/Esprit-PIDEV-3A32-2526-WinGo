package Services.External;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class SummarizationService {

    private static final String RAPIDAPI_HOST = "text-summarization13.p.rapidapi.com";
    private static final String RAPIDAPI_KEY = "VOTRE_NOUVELLE_CLE_API"; // Remplacez par votre clé
    private static final String API_URL = "https://" + RAPIDAPI_HOST + "/data";

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    /**
     * Version synchrone (bloquante)
     */
    public static SummaryResult summarize(String text) throws Exception {
        String formBody = "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-RapidAPI-Key", RAPIDAPI_KEY)
                .header("X-RapidAPI-Host", RAPIDAPI_HOST)
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur API (" + response.statusCode() + ") : " + response.body());
        }

        // Parser la réponse JSON
        try {
            return gson.fromJson(response.body(), SummaryResult.class);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Erreur de parsing JSON : " + response.body(), e);
        }
    }

    /**
     * Version asynchrone (recommandée pour ne pas bloquer l'UI)
     */
    public static CompletableFuture<SummaryResult> summarizeAsync(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return summarize(text);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}