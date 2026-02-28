package Services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TransportAPI {

    private static final HttpClient client = HttpClient.newHttpClient();

    // 1. Méthode pour obtenir la Distance et le Temps (API OSRM) + La Météo
    public static String getInfosTrajet(String depart, String arrivee) {
        System.out.println("🔍 Recherche des coordonnées...");
        double[] coordsDepart = getCoordinates(depart);
        double[] coordsArrivee = getCoordinates(arrivee);

        if (coordsArrivee == null) {
            return "❌ Destination introuvable.";
        }

        // Appel de la météo avec le correctif
        String meteo = getDestinationWeather(coordsArrivee[0], coordsArrivee[1]);
        String trajet = "";

        if (coordsDepart != null) {
            // Appel de l'API de Routage GPS
            trajet = getDistanceAndTime(coordsDepart[0], coordsDepart[1], coordsArrivee[0], coordsArrivee[1]);
        }

        return trajet + " | ⛅ Météo: " + meteo;
    }

    // --- SOUS-MÉTHODES (Appels aux différentes APIs) ---

    // Récupère la Latitude et Longitude d'une ville
    private static double[] getCoordinates(String city) {
        try {
            // On encode le nom de la ville pour les espaces et accents
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);

            // LA MAGIE EST ICI : On ajoute &countrycodes=tn à la fin de l'URL !
            String url = "https://nominatim.openstreetmap.org/search?q=" + encodedCity + "&format=json&limit=1&countrycodes=tn&featureType=city";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "TripLove-App/1.0")
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            String json = res.body();

            // Si la réponse est "[]", ça veut dire que la ville n'existe pas en Tunisie
            if (json.equals("[]")) {
                System.out.println("❌ " + city + " introuvable en Tunisie !");
                return null;
            }
            String nomComplet = extractJsonValue(json, "\"display_name\":\"");
            System.out.println("📍 Le GPS a interprété '" + city + "' comme : " + nomComplet);

            String latStr = extractJsonValue(json, "\"lat\":\"");
            String lonStr = extractJsonValue(json, "\"lon\":\"");

            return new double[]{Double.parseDouble(latStr), Double.parseDouble(lonStr)};

        } catch (Exception e) {
            System.out.println("❌ Erreur API pour " + city + " : " + e.getMessage());
            return null;
        }
    }

    // Récupère la météo exacte (Corrigée !)
    private static String getDestinationWeather(double lat, double lon) {
        try {
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current_weather=true";
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            String json = res.body();

            // CORRECTIF : On cherche le bloc "current_weather" pour éviter les unités
            int blocIndex = json.indexOf("\"current_weather\":");
            String sousJson = json.substring(blocIndex);

            String tempStr = extractJsonValue(sousJson, "\"temperature\":");
            // On nettoie la chaîne pour ne garder que les chiffres et le point
            tempStr = tempStr.replaceAll("[^0-9.]", "");

            return tempStr + " °C";
        } catch(Exception e) {
            return "Indisponible";
        }
    }

    // Récupère la distance via GPS OSRM
    private static String getDistanceAndTime(double lat1, double lon1, double lat2, double lon2) {
        try {
            // Attention: OSRM demande Longitude puis Latitude
            String url = "http://router.project-osrm.org/route/v1/driving/" + lon1 + "," + lat1 + ";" + lon2 + "," + lat2 + "?overview=false";
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            String json = res.body();

            String distanceStr = extractJsonValue(json, "\"distance\":").replaceAll("[^0-9.]", "");
            String durationStr = extractJsonValue(json, "\"duration\":").replaceAll("[^0-9.]", "");

            double distanceKm = Double.parseDouble(distanceStr) / 1000.0;
            double durationSec = Double.parseDouble(durationStr);

            int heures = (int) (durationSec / 3600);
            int minutes = (int) ((durationSec % 3600) / 60);

            return String.format("%.1f km (Environ %dh %02dmin)", distanceKm, heures, minutes);
        } catch (Exception e) {
            return "Indisponible";
        }
    }

    // Outil pour lire le JSON proprement
    private static String extractJsonValue(String json, String key) {
        try {
            if (json == null || !json.contains(key)) return "0";

            int startIndex = json.indexOf(key) + key.length();
            // Look for the next quote, comma, or closing bracket
            int endIndex = json.indexOf("\"", startIndex);

            if (endIndex == -1 || json.charAt(startIndex - 1) == ':') {
                int endComma = json.indexOf(",", startIndex);
                int endBracket = json.indexOf("}", startIndex);
                if (endComma != -1 && endBracket != -1) endIndex = Math.min(endComma, endBracket);
                else if (endComma != -1) endIndex = endComma;
                else endIndex = endBracket;
            }

            if (endIndex == -1) return "0";

            return json.substring(startIndex, endIndex).replace("\"", "").trim();
        } catch (Exception e) {
            return "0";
        }
    }
}