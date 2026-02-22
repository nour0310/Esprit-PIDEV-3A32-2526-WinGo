package Services.External;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class OpenWeatherService {

    private static final String API_KEY = "29760027ad011b56a3fde54646907eaa"; // Votre clé API
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";

    /**
     * Récupère la météo pour une ville donnée.
     * @param cityName Nom de la ville (ex: "Tunis")
     * @return Un CompletableFuture contenant une chaîne formatée (ex: "🌤 22°C")
     */
    public static CompletableFuture<String> getWeatherAsync(String cityName) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                // Encoder le nom de la ville
                String encodedCity = URLEncoder.encode(cityName + ",TN", StandardCharsets.UTF_8);
                String url = String.format("%s?q=%s&appid=%s&units=metric&lang=fr", API_URL, encodedCity, API_KEY);

                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Mozilla/5.0");

                try (CloseableHttpResponse response = client.execute(request)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

                    // Vérifier le code de retour
                    if (json.has("cod") && json.get("cod").getAsInt() != 200) {
                        return "❌";
                    }

                    // Extraire la température et la météo
                    JsonObject main = json.getAsJsonObject("main");
                    double temp = main.get("temp").getAsDouble();
                    JsonObject weather = json.getAsJsonArray("weather").get(0).getAsJsonObject();
                    String description = weather.get("description").getAsString();
                    String icon = weather.get("icon").getAsString();

                    // Retourner un texte formaté avec l'icône (on peut aussi retourner l'URL de l'icône)
                    // Pour l'icône, on peut utiliser un emoji ou une image. Pour simplifier, on utilise un texte.
                    // On peut aussi retourner l'URL de l'icône pour l'afficher avec un ImageView.
                    // Ici on retourne simplement la température et la description.
                    return String.format("%.0f°C %s", temp, description);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "❌";
            }
        });
    }

    /**
     * Version qui retourne un objet avec les détails (si besoin d'icône)
     */
    public static CompletableFuture<WeatherInfo> getWeatherInfoAsync(String cityName) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                String encodedCity = URLEncoder.encode(cityName + ",TN", StandardCharsets.UTF_8);
                String url = String.format("%s?q=%s&appid=%s&units=metric&lang=fr", API_URL, encodedCity, API_KEY);

                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Mozilla/5.0");

                try (CloseableHttpResponse response = client.execute(request)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

                    if (json.has("cod") && json.get("cod").getAsInt() != 200) {
                        return new WeatherInfo("❌", 0, "", "");
                    }

                    JsonObject main = json.getAsJsonObject("main");
                    double temp = main.get("temp").getAsDouble();
                    JsonObject weather = json.getAsJsonArray("weather").get(0).getAsJsonObject();
                    String description = weather.get("description").getAsString();
                    String icon = weather.get("icon").getAsString();

                    return new WeatherInfo(description, temp, icon, cityName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new WeatherInfo("❌", 0, "", "");
            }
        });
    }

    // Classe simple pour contenir les infos météo
    public static class WeatherInfo {
        private final String description;
        private final double temperature;
        private final String iconCode;
        private final String cityName;

        public WeatherInfo(String description, double temperature, String iconCode, String cityName) {
            this.description = description;
            this.temperature = temperature;
            this.iconCode = iconCode;
            this.cityName = cityName;
        }

        public String getDescription() { return description; }
        public double getTemperature() { return temperature; }
        public String getIconCode() { return iconCode; }
        public String getCityName() { return cityName; }

        public String getFormatted() {
            return String.format("%.0f°C %s", temperature, description);
        }
    }
}