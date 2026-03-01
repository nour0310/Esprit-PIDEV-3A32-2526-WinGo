package Services.External;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GoogleTTSService {

    private static final String API_URL = "https://translate.google.com/translate_tts?ie=UTF-8&q=%s&tl=%s&client=tw-ob";

    public static CompletableFuture<byte[]> generateSpeechAsync(String text, String lang) {
        return CompletableFuture.supplyAsync(() -> {
            if (text == null || text.trim().isEmpty()) {
                return null;
            }

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                // Google TTS a une limite d'environ 200 caractÃ¨res par requÃªte.
                List<String> chunks = splitText(text, 180);
                java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

                for (String chunk : chunks) {
                    String encodedText = URLEncoder.encode(chunk, StandardCharsets.UTF_8);
                    String url = String.format(API_URL, encodedText, lang);
                    HttpGet request = new HttpGet(url);
                    request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                    
                    try (CloseableHttpResponse response = client.execute(request)) {
                        byte[] chunkData = EntityUtils.toByteArray(response.getEntity());
                        outputStream.write(chunkData);
                    }
                }
                return outputStream.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private static List<String> splitText(String text, int limit) {
        List<String> chunks = new java.util.ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + limit, text.length());
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

    public static void playAudio(byte[] audioData) {
        if (audioData == null) return;
        try {
            File tempFile = File.createTempFile("tts_", ".mp3");
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(audioData);
            }
            Media media = new Media(tempFile.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.dispose();
                tempFile.delete();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}