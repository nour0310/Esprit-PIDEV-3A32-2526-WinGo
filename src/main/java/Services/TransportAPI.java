package Services;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
public class TransportAPI {




        private static final HttpClient client = HttpClient.newHttpClient();

        /**
         * API 1 & 2 Combined: Gets the city coordinates, then fetches the weather.
         * Uses your Transport's `arrivee` attribute.
         */
        public static String getDestinationWeather(String arrivee) {
            try {
                // --- API 1: OPENSTREETMAP NOMINATIM (Get Coordinates from City Name) ---
                System.out.println("🔍 Searching coordinates for: " + arrivee);
                String encodedCity = URLEncoder.encode(arrivee, StandardCharsets.UTF_8);
                String geoUrl = "https://nominatim.openstreetmap.org/search?q=" + encodedCity + "&format=json&limit=1";

                HttpRequest geoRequest = HttpRequest.newBuilder()
                        .uri(URI.create(geoUrl))
                        .header("User-Agent", "TripLove-StudentProject/1.0") // Required by Nominatim
                        .GET()
                        .build();

                HttpResponse<String> geoResponse = client.send(geoRequest, HttpResponse.BodyHandlers.ofString());
                String geoJson = geoResponse.body();

                if (geoJson.equals("[]")) {
                    return "City not found!";
                }

                // Simple string extraction to find lat and lon without heavy JSON libraries
                String latStr = extractJsonValue(geoJson, "\"lat\":\"");
                String lonStr = extractJsonValue(geoJson, "\"lon\":\"");

                // --- API 2: OPEN-METEO (Get Weather from Coordinates) ---
                System.out.println("⛅ Fetching weather for Lat: " + latStr + ", Lon: " + lonStr);
                String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + latStr + "&longitude=" + lonStr + "&current_weather=true";

                HttpRequest weatherRequest = HttpRequest.newBuilder()
                        .uri(URI.create(weatherUrl))
                        .GET()
                        .build();

                HttpResponse<String> weatherResponse = client.send(weatherRequest, HttpResponse.BodyHandlers.ofString());
                String weatherJson = weatherResponse.body();

                // Extract the temperature from the current_weather object
                String tempStr = extractJsonValue(weatherJson, "\"temperature\":");

                // Clean up the string (remove trailing commas or brackets)
                tempStr = tempStr.split(",")[0].replace("}", "");

                return tempStr + " °C";

            } catch (Exception e) {
                System.out.println("❌ API Error: " + e.getMessage());
                return "N/A";
            }
        }

        // Helper method to extract values from JSON strings simply
        private static String extractJsonValue(String json, String key) {
            int startIndex = json.indexOf(key) + key.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex == -1 || key.endsWith(":")) { // Handle numbers
                endIndex = json.indexOf(",", startIndex);
                if(endIndex == -1) endIndex = json.indexOf("}", startIndex);
            }
            return json.substring(startIndex, endIndex).trim();
        }

        // --- TEST IT YOURSELF ---
        public static void main(String[] args) {
            // Imagine this comes from: myTransport.getArrivee();
            String destination = "Tunis";

            String weather = getDestinationWeather(destination);
            System.out.println("✅ Result for your JavaFX interface: " + destination + " -> " + weather);
        }

}
