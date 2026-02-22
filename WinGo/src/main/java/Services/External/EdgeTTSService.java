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

    // URL de votre worker Cloudflare
    private static final String API_URL = "https://fancy-bird-3b32.balkis-zaghdoud.workers.dev/v1/audio/speech";

    /**
     * Génère un fichier audio à partir d'un texte.
     * @param text Le texte à synthétiser
     * @param voiceId L'identifiant de la voix (ex: "fr-FR-DeniseNeural")
     * @return Un CompletableFuture contenant les données audio (byte[])
     */
    public static CompletableFuture<byte[]> generateSpeechAsync(String text, String voiceId) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(API_URL);
                post.setHeader("Content-Type", "application/json");

                // Échapper les guillemets dans le texte
                String escapedText = text.replace("\"", "\\\"");
                String jsonBody = String.format("{\"input\": \"%s\", \"voice\": \"%s\"}", escapedText, voiceId);
                post.setEntity(new StringEntity(jsonBody, "UTF-8"));

                try (CloseableHttpResponse response = client.execute(post)) {
                    return EntityUtils.toByteArray(response.getEntity());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Joue un flux audio à partir des données.
     * @param audioData Les données audio (format MP3)
     */
    public static void playAudio(byte[] audioData) {
        if (audioData == null) return;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bais);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            // Optionnel : libérer les ressources quand la lecture est terminée
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}