package Services;

import Entites.Event;
import Entites.Participation;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * MailService — envoie des emails transactionnels via l'API Mailjet.
 *
 * ETAPE OBLIGATOIRE AVANT UTILISATION :
 *  1. Allez sur https://app.mailjet.com/account/sender
 *  2. Ajoutez et VERIFIEZ votre adresse email (ex: votre Gmail)
 *  3. Remplacez SENDER_EMAIL ci-dessous par cette adresse verifiee
 */
public class MailService {

    private static final String MAILJET_API_KEY    = "4ad7d8149dbd9f43cf7bab62e0d6069e";
    private static final String MAILJET_SECRET_KEY = "f647870c524502b5b033c40f21de615c";
    private static final String GMAIL_APP_PASSWORD = "xxxx xxxx xxxx xxxx";
    // !! REMPLACEZ par votre email verifie sur Mailjet !!
    private static final String SENDER_EMAIL = "nourarrami310@gmail.com";
    private static final String SENDER_NAME  = "WinGO Events";

    private static final String MAILJET_URL = "https://api.mailjet.com/v3.1/send";
    /**
     * Call this from initialize() to verify email works:
     * MailService.sendTestEmail("nourarrami310@gmail.com");
     */
    public static void sendTestEmail(String toEmail) {
        new Thread(() -> {
            try {
                System.out.println("=== TEST EMAIL START ===");
                String credentials = MAILJET_API_KEY + ":" + MAILJET_SECRET_KEY;
                String encodedAuth = Base64.getEncoder()
                        .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

                String payload = "{\"Messages\":[{"
                        + "\"From\":{\"Email\":\"nourarrami310@gmail.com\",\"Name\":\"WinGO Events\"},"
                        + "\"To\":[{\"Email\":\"" + toEmail + "\",\"Name\":\"Test\"}],"
                        + "\"Subject\":\"Test Email WinGO\","
                        + "\"HTMLPart\":\"<h1>Test OK</h1><p>Le systeme email fonctionne correctement!</p>\""
                        + "}]}";

                URL url = new URL(MAILJET_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                System.out.println("TEST STATUS: " + status);
                java.io.InputStream is2 = (status >= 200 && status < 300)
                        ? conn.getInputStream() : conn.getErrorStream();
                if (is2 != null) {
                    String resp = new String(is2.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("TEST RESPONSE: " + resp);
                }
                conn.disconnect();
            } catch (Exception ex) {
                System.err.println("TEST EXCEPTION: " + ex.getMessage());
                ex.printStackTrace();
            }
        }, "TestMail-Thread").start();
    }


    // ── Methodes publiques ────────────────────────────────────────────

    public static void sendParticipationConfirmation(Participation participation, Event event) {
        String to     = participation.getEmail_participant();
        String toName = participation.getPrenom_participant() + " " + participation.getNom_participant();
        String subject = "Confirmation de participation - " + (event != null ? event.getTitle() : "Evenement");
        sendEmail(to, toName, subject, buildConfirmationHtml(participation, event));
    }

    public static void sendParticipationCancellation(Participation participation, Event event) {
        String to     = participation.getEmail_participant();
        String toName = participation.getPrenom_participant() + " " + participation.getNom_participant();
        String subject = "Annulation de participation - " + (event != null ? event.getTitle() : "Evenement");
        sendEmail(to, toName, subject, buildCancellationHtml(participation, event));
    }

    public static void sendStatusUpdate(Participation participation, Event event) {
        String to     = participation.getEmail_participant();
        String toName = participation.getPrenom_participant() + " " + participation.getNom_participant();
        String subject = "Mise a jour de votre participation - " + (event != null ? event.getTitle() : "Evenement");
        sendEmail(to, toName, subject, buildStatusUpdateHtml(participation, event));
    }

    // ── Envoi HTTP ────────────────────────────────────────────────────

    private static void sendEmail(String toEmail, String toName, String subject, String htmlBody) {
        new Thread(() -> {
            try {
                System.out.println("=== MailService: Envoi email ===");
                System.out.println("TO:      " + toEmail);
                System.out.println("FROM:    " + SENDER_EMAIL);
                System.out.println("SUBJECT: " + subject);

                String credentials = MAILJET_API_KEY + ":" + MAILJET_SECRET_KEY;
                String encodedAuth = Base64.getEncoder()
                        .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

                String payload = "{\"Messages\":[{"
                        + "\"From\":{\"Email\":\"" + escape(SENDER_EMAIL) + "\",\"Name\":\"" + escape(
                                SENDER_NAME) + "\"},"
                        + "\"To\":[{\"Email\":\"" + escape(toEmail) + "\",\"Name\":\"" + escape(toName) + "\"}],"
                        + "\"Subject\":\"" + escape(subject) + "\","
                        + "\"HTMLPart\":\"" + escapeHtmlForJson(htmlBody) + "\""
                        + "}]}";

                URL url = new URL(MAILJET_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                System.out.println("Mailjet HTTP Status: " + status);

                InputStream is = (status >= 200 && status < 300)
                        ? conn.getInputStream() : conn.getErrorStream();
                if (is != null) {
                    String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("Mailjet Response: " + response);
                }

                if (status == 200 || status == 201) {
                    System.out.println("SUCCESS: Email envoye a " + toEmail);
                } else {
                    System.err.println("ECHEC: Status=" + status);
                    System.err.println("CAUSE: SENDER_EMAIL non verifie sur Mailjet.");
                    System.err.println("FIX: https://app.mailjet.com/account/sender");
                }

                conn.disconnect();

            } catch (Exception ex) {
                System.err.println("Exception lors de l'envoi a " + toEmail + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }, "MailService-Thread").start();
    }

    // ── Templates HTML ────────────────────────────────────────────────

    private static String buildConfirmationHtml(Participation p, Event e) {
        String eventTitle    = e != null ? e.getTitle()                      : "[Nom de l'evenement]";
        String eventLocation = e != null ? e.getLocation()                   : "[Lieu]";
        String eventDate     = e != null ? String.valueOf(e.getDate_event())  : "[Date]";
        String eventTime     = e != null ? String.valueOf(e.getStart_time())  : "[Heure]";
        double price         = e != null ? e.getPrice()                      : 0.0;
        double total         = price * p.getNombre_places();
        String fullName      = htmlSafe(p.getPrenom_participant()) + " " + htmlSafe(p.getNom_participant());

        return "<!DOCTYPE html>"
                + "<html lang='fr'><head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:0;background-color:#f0f2f5;font-family:Georgia,serif'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f0f2f5;padding:40px 20px'>"
                + "<tr><td align='center'>"
                + "<table width='620' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:4px;border-top:4px solid #1a3c5e;box-shadow:0 2px 8px rgba(0,0,0,0.08)'>"

                // Header
                + "<tr><td style='padding:40px 50px 20px;text-align:center;border-bottom:1px solid #e8e8e8'>"
                + "<h2 style='margin:0;font-size:13px;letter-spacing:3px;text-transform:uppercase;color:#1a3c5e;font-weight:normal'>WinGO Events</h2>"
                + "</td></tr>"

                // Salutation
                + "<tr><td style='padding:40px 50px 0'>"
                + "<p style='margin:0 0 20px;font-size:15px;color:#222;line-height:1.7'>Madame, Monsieur,</p>"

                // Body
                + "<p style='margin:0 0 20px;font-size:15px;color:#333;line-height:1.9'>"
                + "Je vous ecris afin de confirmer officiellement ma participation a l'evenement "
                + "<strong>&laquo;&nbsp;" + htmlSafe(eventTitle) + "&nbsp;&raquo;</strong>"
                + " qui se tiendra le <strong>" + htmlSafe(eventDate) + "</strong>"
                + " a <strong>" + htmlSafe(eventLocation) + "</strong>."
                + "</p>"

                // Details box
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f8f9fb;border-left:3px solid #1a3c5e;margin:28px 0;border-radius:2px'>"
                + "<tr><td style='padding:24px 28px'>"
                + "<table width='100%' cellpadding='6' cellspacing='0' style='font-size:14px;color:#333'>"
                + detailRow("Evenement",       htmlSafe(eventTitle))
                + detailRow("Lieu",            htmlSafe(eventLocation))
                + detailRow("Date",            htmlSafe(eventDate))
                + detailRow("Heure",           htmlSafe(eventTime))
                + detailRow("Participant",     fullName)
                + detailRow("Nombre de places", String.valueOf(p.getNombre_places()))
                + detailRow("Prix par place",  String.format("%.2f DT", price))
                + detailRow("Total",           String.format("%.2f DT", total))
                + detailRow("Statut",          htmlSafe(p.getStatut()))
                + "</table>"
                + "</td></tr></table>"

                // Polite closing
                + "<p style='margin:0 0 16px;font-size:15px;color:#333;line-height:1.9'>"
                + "Je vous remercie pour cette opportunite et je reste a votre disposition pour toute "
                + "information complementaire ou formalite necessaire avant l'evenement."
                + "</p>"
                + "<p style='margin:0 0 30px;font-size:15px;color:#333;line-height:1.9'>"
                + "Dans l'attente de vous rencontrer, je vous prie d'agreer, Madame, Monsieur, "
                + "l'expression de mes salutations distinguees."
                + "</p>"

                // Signature
                + "<p style='margin:0 0 40px;font-size:15px;color:#1a3c5e;font-weight:bold'>WinGO</p>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='padding:20px 50px;background:#1a3c5e;border-radius:0 0 4px 4px;text-align:center'>"
                + "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.7);letter-spacing:1px'>"
                + "WinGO Events &mdash; " + htmlSafe(eventDate) + "</p>"
                + "</td></tr>"

                + "</table>"
                + "</td></tr></table>"
                + "</body></html>";
    }

    private static String buildCancellationHtml(Participation p, Event e) {
        String eventTitle    = e != null ? e.getTitle()                     : "[Nom de l'evenement]";
        String eventLocation = e != null ? e.getLocation()                  : "[Lieu]";
        String eventDate     = e != null ? String.valueOf(e.getDate_event()) : "[Date]";

        return "<!DOCTYPE html>"
                + "<html lang='fr'><head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:0;background-color:#f0f2f5;font-family:Georgia,serif'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f0f2f5;padding:40px 20px'>"
                + "<tr><td align='center'>"
                + "<table width='620' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:4px;border-top:4px solid #c0392b;box-shadow:0 2px 8px rgba(0,0,0,0.08)'>"
                + "<tr><td style='padding:40px 50px 20px;text-align:center;border-bottom:1px solid #e8e8e8'>"
                + "<h2 style='margin:0;font-size:13px;letter-spacing:3px;text-transform:uppercase;color:#c0392b;font-weight:normal'>WinGO Events</h2>"
                + "</td></tr>"
                + "<tr><td style='padding:40px 50px'>"
                + "<p style='margin:0 0 20px;font-size:15px;color:#222;line-height:1.7'>Madame, Monsieur,</p>"
                + "<p style='margin:0 0 20px;font-size:15px;color:#333;line-height:1.9'>"
                + "Nous vous informons que votre participation a l'evenement "
                + "<strong>&laquo;&nbsp;" + htmlSafe(eventTitle) + "&nbsp;&raquo;</strong>"
                + " prevu le <strong>" + htmlSafe(eventDate) + "</strong>"
                + " a <strong>" + htmlSafe(eventLocation) + "</strong> a ete annulee."
                + "</p>"
                + "<p style='margin:0 0 20px;font-size:15px;color:#333;line-height:1.9'>"
                + "Si cette annulation est une erreur ou si vous souhaitez vous reinscrire, "
                + "n'hesitez pas a nous contacter."
                + "</p>"
                + "<p style='margin:0 0 30px;font-size:15px;color:#333;line-height:1.9'>"
                + "Veuillez agreer, Madame, Monsieur, l'expression de nos salutations distinguees."
                + "</p>"
                + "<p style='margin:0 0 40px;font-size:15px;color:#c0392b;font-weight:bold'>WinGO</p>"
                + "</td></tr>"
                + "<tr><td style='padding:20px 50px;background:#c0392b;border-radius:0 0 4px 4px;text-align:center'>"
                + "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.7);letter-spacing:1px'>WinGO Events</p>"
                + "</td></tr>"
                + "</table></td></tr></table>"
                + "</body></html>";
    }

    private static String buildStatusUpdateHtml(Participation p, Event e) {
        String eventTitle    = e != null ? e.getTitle()                     : "[Nom de l'evenement]";
        String eventLocation = e != null ? e.getLocation()                  : "[Lieu]";
        String eventDate     = e != null ? String.valueOf(e.getDate_event()) : "[Date]";

        return "<!DOCTYPE html>"
                + "<html lang='fr'><head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:0;background-color:#f0f2f5;font-family:Georgia,serif'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f0f2f5;padding:40px 20px'>"
                + "<tr><td align='center'>"
                + "<table width='620' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:4px;border-top:4px solid #d4a017;box-shadow:0 2px 8px rgba(0,0,0,0.08)'>"
                + "<tr><td style='padding:40px 50px 20px;text-align:center;border-bottom:1px solid #e8e8e8'>"
                + "<h2 style='margin:0;font-size:13px;letter-spacing:3px;text-transform:uppercase;color:#d4a017;font-weight:normal'>WinGO Events</h2>"
                + "</td></tr>"
                + "<tr><td style='padding:40px 50px'>"
                + "<p style='margin:0 0 20px;font-size:15px;color:#222;line-height:1.7'>Madame, Monsieur,</p>"
                + "<p style='margin:0 0 20px;font-size:15px;color:#333;line-height:1.9'>"
                + "Nous vous informons que votre participation a l'evenement "
                + "<strong>&laquo;&nbsp;" + htmlSafe(eventTitle) + "&nbsp;&raquo;</strong>"
                + " prevu le <strong>" + htmlSafe(eventDate) + "</strong>"
                + " a <strong>" + htmlSafe(eventLocation) + "</strong> a ete mise a jour."
                + "</p>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f8f9fb;border-left:3px solid #d4a017;margin:28px 0;border-radius:2px'>"
                + "<tr><td style='padding:24px 28px'>"
                + "<table width='100%' cellpadding='6' cellspacing='0' style='font-size:14px;color:#333'>"
                + detailRow("Nouveau statut",   htmlSafe(p.getStatut()))
                + detailRow("Nombre de places", String.valueOf(p.getNombre_places()))
                + "</table></td></tr></table>"
                + "<p style='margin:0 0 30px;font-size:15px;color:#333;line-height:1.9'>"
                + "Pour toute question, n'hesitez pas a nous contacter. "
                + "Veuillez agreer, Madame, Monsieur, l'expression de nos salutations distinguees."
                + "</p>"
                + "<p style='margin:0 0 40px;font-size:15px;color:#d4a017;font-weight:bold'>WinGO</p>"
                + "</td></tr>"
                + "<tr><td style='padding:20px 50px;background:#d4a017;border-radius:0 0 4px 4px;text-align:center'>"
                + "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.85);letter-spacing:1px'>WinGO Events</p>"
                + "</td></tr>"
                + "</table></td></tr></table>"
                + "</body></html>";
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private static String detailRow(String label, String value) {
        return "<tr>"
                + "<td style='color:#666;width:45%;padding:5px 0;border-bottom:1px solid #ececec'>" + label + "</td>"
                + "<td style='color:#1a1a1a;font-weight:bold;padding:5px 0;border-bottom:1px solid #ececec'>" + value + "</td>"
                + "</tr>";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String escapeHtmlForJson(String html) {
        if (html == null) return "";
        return html.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "");
    }

    private static String htmlSafe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}