package Services.External;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
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

                System.out.println("Envoi de la requête TTS vers " + API_URL);
                try (CloseableHttpResponse response = client.execute(post)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    byte[] data = EntityUtils.toByteArray(response.getEntity());
                    System.out.println("Réponse reçue, status: " + statusCode + ", taille: " + data.length + " octets");
                    if (statusCode != 200) {
                        System.err.println("Erreur API TTS: " + new String(data));
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
            System.err.println("Aucune donnée audio à jouer");
            return;
        }
        try {
            File tempFile = File.createTempFile("tts_", ".mp3");
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(audioData);
            }
            System.out.println("Fichier temporaire créé: " + tempFile.getAbsolutePath() + ", taille: " + audioData.length);

            Media media = new Media(tempFile.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(1.0); // volume max
            mediaPlayer.play();
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.dispose();
                tempFile.delete();
                System.out.println("Lecture terminée, fichier supprimé");
            });
            mediaPlayer.setOnError(() -> {
                System.err.println("Erreur MediaPlayer: " + mediaPlayer.getError());
                tempFile.delete();
            });
            mediaPlayer.setOnPlaying(() -> System.out.println("Lecture démarrée"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}