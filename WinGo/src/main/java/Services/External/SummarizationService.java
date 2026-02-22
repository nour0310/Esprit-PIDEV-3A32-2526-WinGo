package org.example.workshop3A9.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class SummarizationService {

    private static final String RAPIDAPI_HOST = "text-summarization13.p.rapidapi.com";
    private static final String RAPIDAPI_KEY = "VOTRE_NOUVELLE_CLE_API"; // Remplacez par votre clé regénérée
    private static final String API_URL = "https://" + RAPIDAPI_HOST + "/data";

    private final HttpClient client;

    public SummarizationService() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Envoie le texte à l'API et retourne le résumé.
     *
     * @param text le texte à résumer
     * @return la réponse brute de l'API (à parser selon le format de retour)
     * @throws Exception en cas d'erreur réseau ou de réponse invalide
     */
    public String summarize(String text) throws Exception {
        // Préparer le corps de la requête au format application/x-www-form-urlencoded
        // Le paramètre attendu s'appelle 'text' (vérifié sur l'endpoint)
        String formBody = "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

        // Construire la requête
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-RapidAPI-Key", RAPIDAPI_KEY)
                .header("X-RapidAPI-Host", RAPIDAPI_HOST)
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        // Envoyer et récupérer la réponse
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        String responseBody = response.body();

        // Vérifier le code de statut HTTP
        if (statusCode != 200) {
            throw new RuntimeException("Erreur API (" + statusCode + ") : " + responseBody);
        }

        return responseBody;
    }
}