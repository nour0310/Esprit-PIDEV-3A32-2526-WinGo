package Services.External;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class EdgeTTSService {

    private static final String API_URL = "https://fancy-bird-3b32.balkis-zaghdoud.workers.dev/v1/audio/speech";

    public static CompletableFuture<byte[]> generateSpeechAsync(String text, String voiceId) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(API_URL);
                post.setHeader("Content-Type", "application/json");

                String escapedText = text.replace("\"", "\\\"");
                String jsonBody = String.format("{\"input\": \"%s\", \"voice\": \"%s\"}", escapedText, voiceId);
                post.setEntity(new StringEntity(jsonBody, "UTF-8"));

                System.out.println("📤 Envoi requête TTS vers " + API_URL);
                try (CloseableHttpResponse response = client.execute(post)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    byte[] data = EntityUtils.toByteArray(response.getEntity());
                    System.out.println("📥 Réponse reçue, code: " + statusCode + ", taille: " + data.length + " octets");
                    if (statusCode != 200) {
                        String errorMsg = new String(data);
                        System.err.println("❌ Erreur API: " + errorMsg);
                        return null;
                    }
                    return data;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static void playAudio(byte[] audioData) {
        if (audioData == null) {
            System.err.println("❌ Aucune donnée audio à jouer");
            return;
        }
        try {
            // Utiliser AudioSystem avec le service provider mp3spi
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bais);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            System.out.println("▶️ Lecture audio démarrée");
        } catch (UnsupportedAudioFileException e) {
            System.err.println("❌ Format audio non supporté. Assurez-vous que la dépendance mp3spi est présente.");
            e.printStackTrace();
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}