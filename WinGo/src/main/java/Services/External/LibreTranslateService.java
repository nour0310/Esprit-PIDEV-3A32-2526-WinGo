package Services.External;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.CompletableFuture;

public class LibreTranslateService {

    private static final String API_URL = "https://libretranslate.de/translate";
    private static final Gson gson = new Gson();

    public static CompletableFuture<String> translateAsync(String text, String sourceLang, String targetLang) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(API_URL);
                post.setHeader("Content-Type", "application/json");

                JsonObject json = new JsonObject();
                json.addProperty("q", text);
                json.addProperty("source", sourceLang);
                json.addProperty("target", targetLang);
                json.addProperty("format", "text");

                post.setEntity(new StringEntity(gson.toJson(json), "UTF-8"));

                try (CloseableHttpResponse response = client.execute(post)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                    return responseJson.get("translatedText").getAsString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return text;
            }
        });
    }
}