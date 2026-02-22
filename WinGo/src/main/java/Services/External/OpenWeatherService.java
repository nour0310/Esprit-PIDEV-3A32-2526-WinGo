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

    private static final String API_KEY = "29760027ad011b56a3fde54646907eaa";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";

    public static CompletableFuture<WeatherInfo> getWeatherAsync(String city) {
        return CompletableFuture.supplyAsync(() -> {
            WeatherInfo info = new WeatherInfo();
            info.setCity(city);
            if (city == null || city.trim().isEmpty()) {
                info.setError("Ville inconnue");
                return info;
            }

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                String encodedCity = URLEncoder.encode(city + ",tn", StandardCharsets.UTF_8);
                String url = String.format("%s?q=%s&appid=%s&units=metric&lang=fr", API_URL, encodedCity, API_KEY);

                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Mozilla/5.0");

                try (CloseableHttpResponse response = client.execute(request)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

                    if (json.has("cod") && json.get("cod").getAsInt() != 200) {
                        info.setError(json.get("message").getAsString());
                        return info;
                    }

                    JsonObject main = json.getAsJsonObject("main");
                    double temp = main.get("temp").getAsDouble();
                    JsonObject weather = json.getAsJsonArray("weather").get(0).getAsJsonObject();
                    String description = weather.get("description").getAsString();
                    String icon = weather.get("icon").getAsString();

                    info.setTemp(temp);
                    info.setDescription(description);
                    info.setIcon(icon);
                    info.setSuccess(true);
                }
            } catch (Exception e) {
                info.setError(e.getMessage());
            }
            return info;
        });
    }

    public static class WeatherInfo {
        private String city;
        private double temp;
        private String description;
        private String icon;
        private String error;
        private boolean success;

        public WeatherInfo() {
            this.success = false;
        }

        // Getters et setters
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public double getTemp() { return temp; }
        public void setTemp(double temp) { this.temp = temp; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getIconUrl() {
            return "http://openweathermap.org/img/wn/" + icon + "@2x.png";
        }
    }
}