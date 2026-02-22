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
import java.util.concurrent.CompletableFuture;

public class GoogleTTSService {

    private static final String API_URL = "https://translate.google.com/translate_tts?ie=UTF-8&q=%s&tl=%s&client=tw-ob";

    public static CompletableFuture<byte[]> generateSpeechAsync(String text, String lang) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
                String url = String.format(API_URL, encodedText, lang);
                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                try (CloseableHttpResponse response = client.execute(request)) {
                    return EntityUtils.toByteArray(response.getEntity());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
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