package Services.External;

import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;
import java.io.IOException;

public class TranslationService {

    // Remplacez par votre ID de projet Google Cloud
    private static final String PROJECT_ID = "votre-projet-id";
    private static final String LOCATION = "global"; // ou "us-central1" selon votre configuration

    /**
     * Traduit un texte dans la langue cible.
     *
     * @param text           Le texte à traduire
     * @param targetLanguage Code de la langue cible (ex: "fr", "en", "es", "ar")
     * @return Le texte traduit, ou le texte original en cas d'erreur
     */
    public static String translateText(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Note : L'authentification se fait automatiquement via la variable d'environnement
        // GOOGLE_APPLICATION_CREDENTIALS pointant vers le fichier JSON de votre compte de service.
        try (TranslationServiceClient client = TranslationServiceClient.create()) {
            LocationName parent = LocationName.of(PROJECT_ID, LOCATION);

            TranslateTextRequest request = TranslateTextRequest.newBuilder()
                    .setParent(parent.toString())
                    .setMimeType("text/plain") // ou "text/html" si vous avez du HTML
                    .setTargetLanguageCode(targetLanguage)
                    .addContents(text)
                    .build();

            TranslateTextResponse response = client.translateText(request);
            if (response.getTranslationsCount() > 0) {
                Translation translation = response.getTranslations(0);
                return translation.getTranslatedText();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la traduction : " + e.getMessage());
            e.printStackTrace();
        }
        return text; // fallback
    }

    // Exemple d'utilisation simple (pour test)
    public static void main(String[] args) {
        String texte = "Bonjour le monde";
        String traduit = translateText(texte, "en");
        System.out.println("Original : " + texte);
        System.out.println("Traduit  : " + traduit);
    }
}