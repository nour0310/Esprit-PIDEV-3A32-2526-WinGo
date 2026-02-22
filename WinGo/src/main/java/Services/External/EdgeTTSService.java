package Services.External;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.awt.Desktop;
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
            File tempFile = File.createTempFile("tts_", ".mp3");
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(audioData);
            }
            System.out.println("💾 Fichier temporaire créé: " + tempFile.getAbsolutePath());

            // Essayer avec MediaPlayer JavaFX
            try {
                Media media = new Media(tempFile.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setVolume(1.0);
                mediaPlayer.play();
                mediaPlayer.setOnEndOfMedia(() -> {
                    mediaPlayer.dispose();
                    tempFile.delete();
                    System.out.println("✅ Lecture terminée, fichier supprimé");
                });
                mediaPlayer.setOnError(() -> {
                    System.err.println("⚠️ Erreur MediaPlayer: " + mediaPlayer.getError());
                    // Fallback : ouvrir avec le lecteur par défaut
                    openWithDefaultPlayer(tempFile);
                });
                mediaPlayer.setOnPlaying(() -> System.out.println("▶️ Lecture démarrée"));
            } catch (Exception e) {
                e.printStackTrace();
                openWithDefaultPlayer(tempFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void openWithDefaultPlayer(File file) {
        try {
            Desktop.getDesktop().open(file);
            System.out.println("📂 Ouverture avec le lecteur par défaut");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}