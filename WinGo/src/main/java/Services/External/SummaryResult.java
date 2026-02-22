package Services.External;

import java.util.Arrays;
import java.util.List;

/**
 * Représente la réponse structurée de l'API de résumé.
 * Adaptez les noms des champs selon la documentation de l'API.
 */
public class SummaryResult {
    private String summary;
    private String[] keyPhrases;
    private double readabilityScore;
    private String sentiment;

    // Constructeur par défaut nécessaire pour Gson
    public SummaryResult() {}

    public String getSummary() {
        return summary;
    }

    public String[] getKeyPhrases() {
        return keyPhrases;
    }

    public double getReadabilityScore() {
        return readabilityScore;
    }

    public String getSentiment() {
        return sentiment;
    }

    /**
     * Retourne les mots-clés formatés pour l'affichage.
     */
    public String getFormattedKeyPhrases() {
        if (keyPhrases == null || keyPhrases.length == 0) {
            return "";
        }
        return String.join(", ", keyPhrases);
    }

    @Override
    public String toString() {
        return "SummaryResult{" +
                "summary='" + summary + '\'' +
                ", keyPhrases=" + Arrays.toString(keyPhrases) +
                ", readabilityScore=" + readabilityScore +
                ", sentiment='" + sentiment + '\'' +
                '}';
    }
}