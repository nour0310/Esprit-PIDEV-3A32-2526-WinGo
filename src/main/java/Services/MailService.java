package Services;

import Entites.Event;
import Entites.Participation;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * MailService — envoie des emails transactionnels via l'API Mailjet avec QR codes.
 */
public class MailService {

    // IMPORTANT: Replace with your verified domain email (e.g., noreply@wingo.com)
    private static final String MAILJET_API_KEY    = "4ad7d8149dbd9f43cf7bab62e0d6069e";
    private static final String MAILJET_SECRET_KEY = "f647870c524502b5b033c40f21de615c";
    private static final String SENDER_EMAIL = "nourarrami310@gmail.com"; // CHANGE THIS to your domain email
    private static final String SENDER_NAME  = "WinGO Events";
    private static final String REPLY_TO_EMAIL = "support@wingo.com"; // Add reply-to address

    private static final String MAILJET_URL = "https://api.mailjet.com/v3.1/send";
    private static final String QR_API_URL = "https://api.qrserver.com/v1/create-qr-code/";

    // Thread-local storage for the CID attachment data
    private static final ThreadLocal<String> qrPngBase64Store = new ThreadLocal<>();

    // ==================== PUBLIC EMAIL METHODS ====================

    public static void sendTestEmail(String toEmail) {
        new Thread(() -> {
            try {
                System.out.println("=== TEST EMAIL START ===");
                String credentials = MAILJET_API_KEY + ":" + MAILJET_SECRET_KEY;
                String encodedAuth = Base64.getEncoder()
                        .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

                String payload = "{\"Messages\":[{"
                        + "\"From\":{\"Email\":\"" + SENDER_EMAIL + "\",\"Name\":\"" + SENDER_NAME + "\"},"
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
                InputStream is2 = (status >= 200 && status < 300)
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

    public static void sendParticipationConfirmation(Participation participation, Event event) {
        String to     = participation.getEmailParticipant();
        String toName = participation.getPrenomParticipant() + " " + participation.getNomParticipant();
        String subject = "Confirmation de votre participation - " + (event != null ? event.getTitle() : "Evenement");
        sendEmail(to, toName, subject, buildConfirmationHtml(participation, event));
    }

    public static void sendParticipationCancellation(Participation participation, Event event) {
        String to     = participation.getEmailParticipant();
        String toName = participation.getPrenomParticipant() + " " + participation.getNomParticipant();
        String subject = "Annulation de votre participation - " + (event != null ? event.getTitle() : "Evenement");
        sendEmail(to, toName, subject, buildCancellationHtml(participation, event));
    }

    public static void sendStatusUpdate(Participation participation, Event event) {
        String to     = participation.getEmailParticipant();
        String toName = participation.getPrenomParticipant() + " " + participation.getNomParticipant();
        String subject = "Mise à jour de votre participation - " + (event != null ? event.getTitle() : "Evenement");
        sendEmail(to, toName, subject, buildStatusUpdateHtml(participation, event));
    }

    // ==================== QR CODE API METHODS ====================

    /**
     * Generate QR code using QR Server API
     * @param data The text/data to encode in the QR code
     * @return Base64 encoded PNG image
     */
    public static String generateQRCodeViaAPI(String data) {
        try {
            // Encode the data for URL
            String encodedData = URLEncoder.encode(data, StandardCharsets.UTF_8.toString());

            // Build API URL with parameters
            String apiUrl = QR_API_URL +
                    "?size=300x300" +
                    "&data=" + encodedData +
                    "&margin=10" +
                    "&qzone=2" +
                    "&format=png";

            System.out.println("QR API URL: " + apiUrl);

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.err.println("QR API returned error code: " + responseCode);
                return null;
            }

            // Read the image data
            byte[] imageBytes = connection.getInputStream().readAllBytes();

            // Convert to Base64
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            System.err.println("Error generating QR code via API: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Format ticket data as a structured string for QR code
     */
    private static String formatTicketData(Participation p, Event e) {
        double total = e.getPrice() * p.getNombrePlaces();

        // Format as a readable text with clear labels
        return String.format(
                "=== WINGO TICKET ===\n" +
                        "Ticket ID: WGO-%d-%d\n" +
                        "Event: %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n" +
                        "Location: %s\n" +
                        "Participant: %s %s\n" +
                        "Places: %d\n" +
                        "Price per place: %.2f DT\n" +
                        "Total: %.2f DT\n" +
                        "Status: %s\n" +
                        "==================",
                e.getIdEvent(),
                p.getIdParticipation(),
                e.getTitle(),
                e.getDateEvent() != null ? e.getDateEvent().toString() : "N/A",
                e.getStartTime() != null ? e.getStartTime().toString().substring(0, 5) : "N/A",
                e.getLocation() != null ? e.getLocation() : "N/A",
                p.getPrenomParticipant(),
                p.getNomParticipant(),
                p.getNombrePlaces(),
                e.getPrice(),
                total,
                p.getStatut()
        );
    }

    // ==================== QR CODE FALLBACK METHODS ====================

    /**
     * Generates a JavaFX Image from QR code data (fallback method)
     */
    public static Image getQRCodeImage(String text) {
        try {
            boolean[][] matrix = buildQrMatrix(text);
            int size = matrix.length;
            int scale = 8;

            WritableImage writableImage = new WritableImage(size * scale, size * scale);
            PixelWriter pixelWriter = writableImage.getPixelWriter();

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    Color color = matrix[y][x] ? Color.BLACK : Color.WHITE;
                    for (int dy = 0; dy < scale; dy++) {
                        for (int dx = 0; dx < scale; dx++) {
                            pixelWriter.setColor(x * scale + dx, y * scale + dy, color);
                        }
                    }
                }
            }
            return writableImage;
        } catch (Exception e) {
            System.err.println("QR Code generation error: " + e.getMessage());
            WritableImage errorImage = new WritableImage(100, 100);
            PixelWriter pw = errorImage.getPixelWriter();
            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    pw.setColor(x, y, Color.RED);
                }
            }
            return errorImage;
        }
    }

    // ==================== EMAIL SENDING WITH IMPROVED DELIVERABILITY ====================

    private static void sendEmail(String toEmail, String toName, String subject, String htmlBody) {
        String capturedQrB64 = qrPngBase64Store.get();
        qrPngBase64Store.remove();

        new Thread(() -> {
            try {
                System.out.println("=== MailService: Envoi email ===");
                System.out.println("TO:      " + toEmail);
                System.out.println("FROM:    " + SENDER_EMAIL);
                System.out.println("SUBJECT: " + subject);

                String credentials = MAILJET_API_KEY + ":" + MAILJET_SECRET_KEY;
                String encodedAuth = Base64.getEncoder()
                        .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

                String inlinedAttachments = "";
                if (capturedQrB64 != null) {
                    inlinedAttachments = ",\"InlinedAttachments\":[{"
                            + "\"ContentType\":\"image/png\","
                            + "\"Filename\":\"qrcode.png\","
                            + "\"ContentID\":\"qrcode@wingo\","
                            + "\"Base64Content\":\"" + capturedQrB64 + "\""
                            + "}]";
                }

                // Add proper headers for better deliverability
                String payload = "{\"Messages\":[{"
                        + "\"From\":{\"Email\":\"" + escape(SENDER_EMAIL) + "\",\"Name\":\"" + escape(SENDER_NAME) + "\"},"
                        + "\"To\":[{\"Email\":\"" + escape(toEmail) + "\",\"Name\":\"" + escape(toName) + "\"}],"
                        + "\"ReplyTo\":{\"Email\":\"" + escape(REPLY_TO_EMAIL) + "\"},"
                        + "\"Subject\":\"" + escape(subject) + "\","
                        + "\"HTMLPart\":\"" + escapeHtmlForJson(htmlBody) + "\","
                        + "\"Headers\":{"
                        + "\"List-Unsubscribe\":\"<mailto:unsubscribe@wingo.com?subject=unsubscribe>\","
                        + "\"Precedence\":\"bulk\","
                        + "\"X-Priority\":\"3\""
                        + "}"
                        + inlinedAttachments
                        + "}]}";

                URL url = new URL(MAILJET_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

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
                    System.out.println("✅ SUCCESS: Email envoyé à " + toEmail);
                } else {
                    System.err.println("❌ ÉCHEC: Status=" + status);
                    System.err.println("Vérifiez votre configuration Mailjet et vos DNS");
                }

                conn.disconnect();

            } catch (Exception ex) {
                System.err.println("❌ Exception lors de l'envoi à " + toEmail + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }, "MailService-Thread").start();
    }

    // ==================== HTML TEMPLATES ====================

    private static String buildConfirmationHtml(Participation p, Event e) {
        String eventTitle    = e != null ? e.getTitle()                      : "[Nom de l'evenement]";
        String eventLocation = e != null ? e.getLocation()                   : "[Lieu]";
        String eventDate     = e != null && e.getDateEvent() != null ? e.getDateEvent().toString() : "[Date]";
        String eventTime     = e != null && e.getStartTime() != null ? e.getStartTime().toString() : "[Heure]";
        double price         = e != null ? e.getPrice()                      : 0.0;
        double total         = price * p.getNombrePlaces();
        String fullName      = htmlSafe(p.getPrenomParticipant()) + " " + htmlSafe(p.getNomParticipant());
        String ticketId = "WGO-" + p.getIdParticipation() + "-" + (e != null ? e.getIdEvent() : "0");

        // Generate QR code using API
        String qrData = formatTicketData(p, e);
        String qrImgTag = fetchQrAsBase64(qrData);

        return "<!DOCTYPE html>"
                + "<html lang='fr'><head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:0;background:#f0f2f5;font-family:Arial,sans-serif'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f0f2f5;padding:30px 15px'>"
                + "<tr><td align='center'>"
                + "<table width='620' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:8px;"
                + "box-shadow:0 4px 20px rgba(0,0,0,0.10);overflow:hidden'>"

                // Header
                + "<tr><td style='background:linear-gradient(135deg,#1a3c5e 0%,#2d6a9f 100%);padding:32px 40px;text-align:center'>"
                + "<h1 style='margin:0 0 4px;color:#ffffff;font-size:26px;letter-spacing:2px;font-weight:700'>WinGO Events</h1>"
                + "<p style='margin:0;color:rgba(255,255,255,0.75);font-size:13px;letter-spacing:1px'>BILLET DE CONFIRMATION</p>"
                + "</td></tr>"

                // Greeting
                + "<tr><td style='padding:32px 40px 0'>"
                + "<p style='margin:0 0 12px;font-size:15px;color:#222;line-height:1.7'>Madame, Monsieur,</p>"
                + "<p style='margin:0 0 24px;font-size:15px;color:#444;line-height:1.8'>"
                + "Nous avons le plaisir de confirmer votre participation à l'événement "
                + "<strong>&laquo;&nbsp;" + htmlSafe(eventTitle) + "&nbsp;&raquo;</strong>"
                + " qui se tiendra le <strong>" + htmlSafe(eventDate) + "</strong>"
                + " à <strong>" + htmlSafe(eventLocation) + "</strong>."
                + "</p>"
                + "</td></tr>"

                // TICKET
                + "<tr><td style='padding:0 40px 32px'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='"
                + "border:2px dashed #1a3c5e;border-radius:12px;overflow:hidden;background:#f8fbff'>"

                // Ticket top bar
                + "<tr><td colspan='2' style='background:#1a3c5e;padding:14px 24px'>"
                + "<table width='100%' cellpadding='0' cellspacing='0'>"
                + "<tr>"
                + "<td style='color:#fff;font-size:18px;font-weight:bold;letter-spacing:1px'>" + htmlSafe(eventTitle) + "</td>"
                + "<td align='right' style='color:rgba(255,255,255,0.7);font-size:12px'>N° " + ticketId + "</td>"
                + "</tr></table>"
                + "</td></tr>"

                // Ticket body
                + "<tr>"
                + "<td style='padding:22px 24px;vertical-align:top;width:65%'>"
                + "<table cellpadding='8' cellspacing='0' style='width:100%;font-size:13px'>"
                + ticketRow("📅", "Date",             htmlSafe(eventDate))
                + ticketRow("⏰",   "Heure",            htmlSafe(eventTime))
                + ticketRow("📍", "Lieu",             htmlSafe(eventLocation))
                + ticketRow("👤", "Participant",      fullName)
                + ticketRow("🎫", "Places",           String.valueOf(p.getNombrePlaces()))
                + ticketRow("💰", "Prix par place",   String.format("%.2f DT", price))
                + ticketRow("✅",   "Statut",           htmlSafe(p.getStatut()))
                + "</table>"
                + "</td>"

                // QR code
                + "<td style='padding:22px 20px;text-align:center;vertical-align:middle;"
                + "border-left:2px dashed #c0cfe0;width:35%'>"
                + qrImgTag
                + "<p style='margin:12px 0 0;font-size:22px;font-weight:bold;color:#1a3c5e'>"
                + String.format("%.2f DT", total) + "</p>"
                + "<p style='margin:2px 0 0;font-size:11px;color:#888'>TOTAL</p>"
                + "</td>"
                + "</tr>"

                // Footer
                + "<tr><td colspan='2' style='background:#1a3c5e;padding:10px 24px;text-align:center'>"
                + "<p style='margin:0;color:rgba(255,255,255,0.6);font-size:11px;letter-spacing:2px'>"
                + "PRÉSENTEZ CE BILLET À L'ENTRÉE DE L'ÉVÉNEMENT"
                + "</p>"
                + "</td></tr>"
                + "</table>"
                + "</td></tr>"

                // Closing
                + "<tr><td style='padding:0 40px 32px'>"
                + "<p style='margin:0 0 16px;font-size:15px;color:#444;line-height:1.8'>"
                + "Nous vous remercions pour votre confiance et restons à votre disposition pour toute "
                + "information complémentaire."
                + "</p>"
                + "<p style='margin:0 0 24px;font-size:15px;color:#444;line-height:1.8'>"
                + "Dans l'attente de vous rencontrer, nous vous prions d'agréer, Madame, Monsieur, "
                + "l'expression de nos salutations distinguées."
                + "</p>"
                + "<p style='margin:0 0 32px;font-size:16px;color:#1a3c5e;font-weight:bold'>L'équipe WinGO</p>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='background:#1a3c5e;padding:18px 40px;text-align:center'>"
                + "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.6);letter-spacing:1px'>"
                + "WinGO Events — " + htmlSafe(eventDate) + " — <a href='mailto:unsubscribe@wingo.com' style='color:rgba(255,255,255,0.8)'>Se désabonner</a>"
                + "</p>"
                + "</td></tr>"
                + "</table>"
                + "</td></tr></table>"
                + "</body></html>";
    }

    private static String buildCancellationHtml(Participation p, Event e) {
        String eventTitle    = e != null ? e.getTitle()                     : "[Nom de l'evenement]";
        String eventLocation = e != null ? e.getLocation()                  : "[Lieu]";
        String eventDate     = e != null && e.getDateEvent() != null ? e.getDateEvent().toString() : "[Date]";

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
                + "Nous vous informons que votre participation à l'événement "
                + "<strong>&laquo;&nbsp;" + htmlSafe(eventTitle) + "&nbsp;&raquo;</strong>"
                + " prévu le <strong>" + htmlSafe(eventDate) + "</strong>"
                + " à <strong>" + htmlSafe(eventLocation) + "</strong> a été annulée."
                + "</p>"
                + "<p style='margin:0 0 30px;font-size:15px;color:#333;line-height:1.9'>"
                + "Veuillez agréer, Madame, Monsieur, l'expression de nos salutations distinguées."
                + "</p>"
                + "<p style='margin:0 0 40px;font-size:15px;color:#c0392b;font-weight:bold'>L'équipe WinGO</p>"
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
        String eventDate     = e != null && e.getDateEvent() != null ? e.getDateEvent().toString() : "[Date]";

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
                + "Nous vous informons que votre participation à l'événement "
                + "<strong>&laquo;&nbsp;" + htmlSafe(eventTitle) + "&nbsp;&raquo;</strong>"
                + " prévu le <strong>" + htmlSafe(eventDate) + "</strong>"
                + " à <strong>" + htmlSafe(eventLocation) + "</strong> a été mise à jour."
                + "</p>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f8f9fb;border-left:3px solid #d4a017;margin:28px 0;border-radius:2px'>"
                + "<tr><td style='padding:24px 28px'>"
                + "<table width='100%' cellpadding='6' cellspacing='0' style='font-size:14px;color:#333'>"
                + detailRow("Nouveau statut",   htmlSafe(p.getStatut()))
                + detailRow("Nombre de places", String.valueOf(p.getNombrePlaces()))
                + "</table></td></tr></table>"
                + "<p style='margin:0 0 30px;font-size:15px;color:#333;line-height:1.9'>"
                + "Pour toute question, n'hésitez pas à nous contacter."
                + "</p>"
                + "<p style='margin:0 0 40px;font-size:15px;color:#d4a017;font-weight:bold'>L'équipe WinGO</p>"
                + "</td></tr>"
                + "<tr><td style='padding:20px 50px;background:#d4a017;border-radius:0 0 4px 4px;text-align:center'>"
                + "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.85);letter-spacing:1px'>WinGO Events</p>"
                + "</td></tr>"
                + "</table></td></tr></table>"
                + "</body></html>";
    }

    // ==================== QR CODE FETCHING ====================

    private static String fetchQrAsBase64(String text) {
        try {
            // Try to generate QR using API
            String base64QR = generateQRCodeViaAPI(text);

            if (base64QR != null && !base64QR.isEmpty()) {
                // Store for email attachment
                qrPngBase64Store.set(base64QR);

                // Return CID reference for HTML
                return "<img src=\"cid:qrcode@wingo\""
                        + " width=\"180\" height=\"180\""
                        + " alt=\"QR Code\" style=\"display:block;margin:0 auto;"
                        + "border:4px solid white;border-radius:8px;box-shadow:0 2px 10px rgba(0,0,0,0.1);\"/>";
            }
        } catch (Exception e) {
            System.err.println("QR API failed, falling back to local generation: " + e.getMessage());
        }

        // Fallback to local generation if API fails
        return fallbackFetchQrAsBase64(text);
    }

    private static String fallbackFetchQrAsBase64(String text) {
        try {
            boolean[][] matrix = buildQrMatrix(text);
            int N = matrix.length;
            int cell = 8;
            int quiet = 16;
            int total = N * cell + quiet * 2;

            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                    total, total, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = img.createGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, total, total);
            g.setColor(new java.awt.Color(0x1a, 0x3c, 0x5e));

            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    if (matrix[r][c]) {
                        g.fillRect(quiet + c * cell, quiet + r * cell, cell, cell);
                    }
                }
            }
            g.dispose();

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "PNG", baos);
            String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            qrPngBase64Store.set(b64);

            return "<img src=\"cid:qrcode@wingo\""
                    + " width=\"" + total + "\" height=\"" + total + "\""
                    + " alt=\"QR Code\" style=\"display:block;margin:0 auto;"
                    + "border:6px solid white;border-radius:8px;\"/>";

        } catch (Exception e) {
            System.err.println("Fallback QR generation failed: " + e.getMessage());
            qrPngBase64Store.set(null);
            return "<div style=\"width:160px;height:160px;border:4px solid #1a3c5e;"
                    + "border-radius:8px;margin:0 auto;background:#eef2f7;"
                    + "text-align:center;padding-top:60px\">"
                    + "<p style=\"color:#1a3c5e;font-size:11px;margin:0\">QR indisponible</p></div>";
        }
    }

    // ==================== HELPER METHODS ====================

    private static String ticketRow(String icon, String label, String value) {
        return "<tr>"
                + "<td style='padding:5px 8px;color:#888;font-size:12px;white-space:nowrap'>"
                + icon + "&nbsp;<span style='font-weight:600;color:#555'>" + label + "</span></td>"
                + "<td style='padding:5px 8px;color:#1a1a1a;font-weight:bold;font-size:13px'>" + value + "</td>"
                + "</tr>";
    }

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

    // ==================== ORIGINAL QR MATRIX GENERATOR (KEPT FOR FALLBACK) ====================

    private static boolean[][] buildQrMatrix(String text) throws Exception {
        // GF(256) tables
        int[] EXP = new int[512];
        int[] LOG  = new int[256];
        int x = 1;
        for (int i = 0; i < 255; i++) {
            EXP[i] = x;
            LOG[x] = i;
            x <<= 1;
            if ((x & 0x100) != 0) x ^= 0x11D;
        }
        for (int i = 255; i < 512; i++) EXP[i] = EXP[i - 255];

        java.util.function.BiFunction<Integer,Integer,Integer> gfMul = (a, b) -> {
            if (a == 0 || b == 0) return 0;
            return EXP[LOG[a] + LOG[b]];
        };

        int nEcc = 26;
        int[] gen = {1};
        for (int i = 0; i < nEcc; i++) {
            int[] g2 = new int[gen.length + 1];
            for (int j = 0; j < gen.length; j++) {
                g2[j]   ^= gfMul.apply(gen[j], 1);
                g2[j+1] ^= gfMul.apply(gen[j], EXP[i]);
            }
            gen = g2;
        }

        byte[] raw = text.getBytes("ISO-8859-1");
        int dataLen = Math.min(raw.length, 64);
        int dataCw  = 86;

        java.util.ArrayList<Integer> bitList = new java.util.ArrayList<>();
        java.util.function.BiConsumer<Integer,Integer> push = (v, n) -> {
            for (int i = n-1; i >= 0; i--) bitList.add((v >> i) & 1);
        };
        push.accept(0b0100, 4);
        push.accept(dataLen, 8);
        for (int i = 0; i < dataLen; i++) push.accept(raw[i] & 0xFF, 8);
        push.accept(0, 4);
        int[] PAD = {0xEC, 0x11};
        while (bitList.size() < dataCw * 8) {
            int pb = PAD[(bitList.size()/8) % 2];
            for (int i = 7; i >= 0; i--) bitList.add((pb >> i) & 1);
        }

        int[] cw = new int[dataCw];
        for (int i = 0; i < dataCw; i++) {
            for (int j = 0; j < 8; j++) cw[i] = (cw[i] << 1) | bitList.get(i*8+j);
        }

        int[] msg = new int[dataCw + nEcc];
        System.arraycopy(cw, 0, msg, 0, dataCw);
        for (int i = 0; i < dataCw; i++) {
            int c2 = msg[i];
            if (c2 != 0) {
                for (int j = 0; j < gen.length; j++) msg[i+j] ^= gfMul.apply(gen[j], c2);
            }
        }
        int[] allCw = new int[dataCw + nEcc];
        System.arraycopy(cw, 0, allCw, 0, dataCw);
        System.arraycopy(msg, dataCw, allCw, dataCw, nEcc);

        int N = 37;
        boolean[][] m  = new boolean[N][N];
        boolean[][] mf = new boolean[N][N];

        int[][] finderStarts = {{0,0},{0,N-7},{N-7,0}};
        for (int[] fs : finderStarts) {
            int r0=fs[0], c0=fs[1];
            for (int r=0;r<7;r++) for (int c=0;c<7;c++) {
                m[r0+r][c0+c]  = r==0||r==6||c==0||c==6||(r>=2&&r<=4&&c>=2&&c<=4);
                mf[r0+r][c0+c] = true;
            }
        }

        for (int i=0;i<8;i++) {
            safeSet(m,mf,7,i,false); safeSet(m,mf,i,7,false);
            safeSet(m,mf,7,N-8+i,false); safeSet(m,mf,i,N-8,false);
            safeSet(m,mf,N-8,i,false); safeSet(m,mf,N-8+i,7,false);
        }

        for (int i=8;i<N-8;i++) {
            m[6][i]=mf[6][i]=(i%2==0);
            m[i][6]=mf[i][6]=(i%2==0);
        }

        m[N-8][8]=mf[N-8][8]=true;

        int[] ap = {6, 30};
        for (int ar : ap) for (int ac : ap) {
            if ((ar<=8&&ac<=8)||(ar<=8&&ac>=N-9)||(ar>=N-9&&ac<=8)) continue;
            for (int r=-2;r<=2;r++) for (int c=-2;c<=2;c++) {
                if (!mf[ar+r][ac+c]) {
                    m[ar+r][ac+c]  = Math.abs(r)==2||Math.abs(c)==2||(r==0&&c==0);
                    mf[ar+r][ac+c] = true;
                }
            }
        }

        java.util.ArrayList<Integer> dataStream = new java.util.ArrayList<>();
        for (int cw2 : allCw) for (int i=7;i>=0;i--) dataStream.add((cw2>>i)&1);
        int idx=0; boolean up=true; int col=N-1;
        while (col >= 1) {
            if (col == 6) col = 5;
            for (int ri = 0; ri < N; ri++) {
                int r = up ? N-1-ri : ri;
                for (int dc = 0; dc < 2; dc++) {
                    int c = col - dc;
                    if (c>=0 && !mf[r][c] && idx < dataStream.size()) {
                        m[r][c] = dataStream.get(idx++)==1;
                    }
                }
            }
            up = !up; col -= 2;
        }

        for (int r=0;r<N;r++) for (int c=0;c<N;c++) {
            if (!mf[r][c] && (r+c)%2==0) m[r][c]=!m[r][c];
        }

        int fmt = 0b101010000010010;
        int[][] fi1 = {{0,8},{1,8},{2,8},{3,8},{4,8},{5,8},{7,8},{8,8},
                {8,7},{8,5},{8,4},{8,3},{8,2},{8,1},{8,0}};
        int[][] fi2 = {{8,N-1},{8,N-2},{8,N-3},{8,N-4},{8,N-5},{8,N-6},{8,N-7},
                {N-8,8},{N-7,8},{N-6,8},{N-5,8},{N-4,8},{N-3,8},{N-2,8},{N-1,8}};
        for (int i=0;i<15;i++) {
            boolean b = ((fmt>>(14-i))&1)==1;
            m[fi1[i][0]][fi1[i][1]] = b; mf[fi1[i][0]][fi1[i][1]] = true;
            m[fi2[i][0]][fi2[i][1]] = b; mf[fi2[i][0]][fi2[i][1]] = true;
        }

        return m;
    }

    private static void safeSet(boolean[][] m, boolean[][] mf, int r, int c, boolean v) {
        if (r>=0&&r<m.length&&c>=0&&c<m.length) { m[r][c]=v; mf[r][c]=true; }
    }
}