package Services.External;

import java.util.Arrays;

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

    public String getFormattedKeyPhrases() {
        if (keyPhrases == null || keyPhrases.length == 0) return "";
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