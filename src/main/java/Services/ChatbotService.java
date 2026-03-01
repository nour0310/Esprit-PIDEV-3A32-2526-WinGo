package Services;

import Entites.Reservation;
import Entites.Transport;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatbotService {
    // Remplace par ta vraie clé Groq ou OpenAI
    private static final String API_KEY = "gsk_votre_cle_ici";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public String getSmartAIResponse(String userMessage, List<Reservation> reservations, List<Transport> transports) {
        // Ajoute des vérifications de nullité au début de getSmartAIResponse
        if (reservations == null) reservations = new ArrayList<>();
        if (transports == null) transports = new ArrayList<>();
        try {
            // Construction du contexte à partir des listes passées en paramètres
            StringBuilder context = new StringBuilder("Tu es l'assistant TripLove. Voici les données du client :\n");

            context.append("Réservations : ");
            for (Reservation r : reservations) {
                context.append("- ").append(r.getUser()).append(" vers ").append(r.getExp()).append("\n");
            }

            context.append("Transports : ");
            for (Transport t : transports) {
                context.append("- ").append(t.getType()).append(" de ").append(t.getDepart()).append(" à ").append(t.getArrivee()).append("\n");
            }

            return callExternalAPI(context.toString(), userMessage);
        } catch (Exception e) {
            return "Désolé, j'ai un souci pour accéder à vos données. ✨";
        }
    }

    private String callExternalAPI(String systemContext, String userPrompt) throws Exception {
        JSONObject json = new JSONObject();
        json.put("model", "llama-3.3-70b-versatile");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", systemContext));
        messages.put(new JSONObject().put("role", "user").put("content", userPrompt));

        json.put("messages", messages);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject respJson = new JSONObject(response.body());

        return respJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }

}