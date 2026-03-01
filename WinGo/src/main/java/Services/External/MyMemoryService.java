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
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MyMemoryService {

    private static final String API_URL = "https://api.mymemory.translated.net/get";

    public static CompletableFuture<String> translateAsync(String text, String sourceLang, String targetLang) {
        return CompletableFuture.supplyAsync(() -> {
            if (text == null || text.trim().isEmpty()) {
                return text;
            }

            try {
                // MyMemory a une limite de 500 caractÃ¨res par requÃªte gratuite.
                // On dÃ©coupe le texte en morceaux de ~450 caractÃ¨res pour Ãªtre sÃ»r.
                List<String> chunks = splitText(text, 450);
                StringBuilder translatedBody = new StringBuilder();

                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    for (String chunk : chunks) {
                        String encodedText = URLEncoder.encode(chunk, StandardCharsets.UTF_8);
                        String langpair = sourceLang + "|" + targetLang;
                        String encodedLangpair = URLEncoder.encode(langpair, StandardCharsets.UTF_8);

                        String url = String.format("%s?q=%s&langpair=%s", API_URL, encodedText, encodedLangpair);

                        HttpGet request = new HttpGet(url);
                        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

                        try (CloseableHttpResponse response = client.execute(request)) {
                            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                            String translatedChunk = json.getAsJsonObject("responseData").get("translatedText").getAsString();
                            translatedBody.append(translatedChunk).append(" ");
                        }
                    }
                }
                return translatedBody.toString().trim();
            } catch (Exception e) {
                System.err.println("Erreur MyMemory : " + e.getMessage());
                e.printStackTrace();
                return text; // fallback
            }
        });
    }

    private static List<String> splitText(String text, int limit) {
        List<String> chunks = new java.util.ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + limit, text.length());
            // Essayer de couper au dernier espace pour ne pas casser les mots
            if (end < text.length()) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }
            chunks.add(text.substring(start, end).trim());
            start = end;
        }
        return chunks;
    }
}