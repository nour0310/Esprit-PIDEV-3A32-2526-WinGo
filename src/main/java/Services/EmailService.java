package Services;

import Entites.Reclamation;
import Entites.Suggestion;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    // CONFIGURATION GMAIL
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String EMAIL_SENDER = "fourtinourelhouda@gmail.com";
    private static final String EMAIL_PASSWORD = "wxnutgyeaomzwhwr"; // VOTRE MOT DE PASSE SANS ESPACES
    private static final String NOM_EXPEDITEUR = "WinGo Support";

    private static final String EMAIL_ADMIN = "fourtinourelhouda@gmail.com"; // Email pour les notifications admin

    /**
     * Envoie un email de confirmation pour une réclamation
     */
    public boolean envoyerConfirmationReclamation(Reclamation reclamation, String emailDestinataire, String nomDestinataire) {
        if (emailDestinataire == null || emailDestinataire.isBlank()) {
            System.out.println("⚠ Aucun email fourni");
            return false;
        }

        String sujet = "✅ [WinGo] Réclamation #" + reclamation.getId_reclamation() + " confirmée";

        String corps = "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
                "<div style='text-align: center; margin-bottom: 20px;'>" +
                "<h1 style='color: #4A3F8F; margin: 0;'>WinGO</h1>" +
                "<p style='color: #666; margin: 0;'>Travel Agency</p>" +
                "</div>" +
                "<h2 style='color: #4A3F8F; border-bottom: 2px solid #4A3F8F; padding-bottom: 10px;'>✅ Réclamation reçue</h2>" +
                "<p>Bonjour <strong>" + nomDestinataire + "</strong>,</p>" +
                "<p>Votre réclamation a été enregistrée avec succès dans notre système.</p>" +
                "<div style='background: #F3F4F6; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                "<p><strong>N° de réclamation :</strong> " + reclamation.getId_reclamation() + "</p>" +
                "<p><strong>Sujet :</strong> " + reclamation.getSujet() + "</p>" +
                "<p><strong>Type :</strong> " + reclamation.getType_reclamation() + "</p>" +
                "<p><strong>Priorité :</strong> " + reclamation.getPriorite() + "</p>" +
                "<p><strong>Description :</strong><br>" + reclamation.getDescription() + "</p>" +
                "</div>" +
                "<p>Notre équipe traitera votre demande dans les plus brefs délais.</p>" +
                "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #999; font-size: 12px;'>" +
                "<p>WinGo Support</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";

        return envoyerEmail(emailDestinataire, sujet, corps);
    }

    /**
     * Envoie un email de réponse pour une réclamation
     */
    public boolean envoyerReponseReclamation(Reclamation reclamation, String emailDestinataire, String nomDestinataire) {
        if (emailDestinataire == null || emailDestinataire.isBlank()) {
            return false;
        }

        String sujet = "📬 [WinGo] Réponse à votre réclamation #" + reclamation.getId_reclamation();

        String corps = "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
                "<div style='text-align: center; margin-bottom: 20px;'>" +
                "<h1 style='color: #4A3F8F; margin: 0;'>WinGO</h1>" +
                "<p style='color: #666; margin: 0;'>Travel Agency</p>" +
                "</div>" +
                "<h2 style='color: #4A3F8F; border-bottom: 2px solid #4A3F8F; padding-bottom: 10px;'>📬 Réponse à votre réclamation</h2>" +
                "<p>Bonjour <strong>" + nomDestinataire + "</strong>,</p>" +
                "<div style='background: #EFF6FF; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                "<p><strong>Réclamation #" + reclamation.getId_reclamation() + "</strong> - " + reclamation.getSujet() + "</p>" +
                "<p><strong>Nouveau statut :</strong> <span style='color: #4A3F8F; font-weight: bold;'>" + reclamation.getStatut() + "</span></p>" +
                "<div style='border-left: 4px solid #4A3F8F; padding-left: 15px; margin: 15px 0;'>" +
                "<p><strong>Réponse de notre équipe :</strong></p>" +
                "<p>" + reclamation.getReponse_admin() + "</p>" +
                "</div>" +
                "</div>" +
                "<p>Cordialement,<br>L'équipe WinGo</p>" +
                "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #999; font-size: 12px;'>" +
                "<p>WinGo Support</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";

        return envoyerEmail(emailDestinataire, sujet, corps);
    }

    /**
     * Notifie l'admin pour une réclamation urgente (Haute ou Critique)
     */
    public boolean notifierAdminNouvelleReclamation(Reclamation reclamation) {
        if (!"Haute".equals(reclamation.getPriorite()) && !"Critique".equals(reclamation.getPriorite())) {
            return true; // Pas de notification pour les priorités basses
        }

        String sujet = "🚨 URGENT - Nouvelle réclamation #" + reclamation.getId_reclamation();

        String corps = "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #fef2f2;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 30px; border: 2px solid #EF4444;'>" +
                "<h2 style='color: #EF4444; margin-top: 0;'>🚨 NOUVELLE RÉCLAMATION URGENTE</h2>" +
                "<div style='background: #FEF2F2; padding: 15px; border-radius: 8px;'>" +
                "<p><strong>Réclamation #" + reclamation.getId_reclamation() + "</strong></p>" +
                "<p><strong>Sujet :</strong> " + reclamation.getSujet() + "</p>" +
                "<p><strong>Type :</strong> " + reclamation.getType_reclamation() + "</p>" +
                "<p><strong>Priorité :</strong> <span style='color: #EF4444; font-weight: bold;'>" + reclamation.getPriorite() + "</span></p>" +
                "<p><strong>Utilisateur :</strong> " + reclamation.getId_user() + "</p>" +
                "<p><strong>Description :</strong><br>" + reclamation.getDescription() + "</p>" +
                "</div>" +
                "<p style='margin-top: 20px;'><a href='http://localhost:8080' style='background: #4A3F8F; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; display: inline-block;'>TRAITER MAINTENANT</a></p>" +
                "</div>" +
                "</body></html>";

        return envoyerEmail(EMAIL_ADMIN, sujet, corps);
    }

    /**
     * Envoie un email de confirmation pour une suggestion
     */
    public boolean envoyerConfirmationSuggestion(Suggestion suggestion, String emailDestinataire, String nomDestinataire) {
        if (emailDestinataire == null || emailDestinataire.isBlank()) {
            return false;
        }

        String sujet = "💡 [WinGo] Suggestion #" + suggestion.getId_suggestion() + " reçue";

        String corps = "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #fffbeb;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 30px;'>" +
                "<h2 style='color: #F59E0B;'>💡 Merci pour votre suggestion</h2>" +
                "<p>Bonjour <strong>" + nomDestinataire + "</strong>,</p>" +
                "<div style='background: #FFFBEB; padding: 15px; border-radius: 8px;'>" +
                "<p><strong>Suggestion #" + suggestion.getId_suggestion() + "</strong></p>" +
                "<p><strong>Sujet :</strong> " + suggestion.getSujet() + "</p>" +
                "<p><strong>Catégorie :</strong> " + suggestion.getCategorie() + "</p>" +
                "<p><strong>Description :</strong><br>" + suggestion.getDescription() + "</p>" +
                "</div>" +
                "<p>Notre équipe examinera votre idée et vous tiendra informé(e) de sa décision.</p>" +
                "<p>Cordialement,<br>L'équipe WinGo</p>" +
                "</div>" +
                "</body></html>";

        return envoyerEmail(emailDestinataire, sujet, corps);
    }

    /**
     * Méthode privée pour envoyer un email (utilisée par toutes les autres méthodes)
     */
    private boolean envoyerEmail(String destinataire, String sujet, String corps) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_SENDER, EMAIL_PASSWORD);
            }
        });

        // Activer les logs pour le debug
        session.setDebug(true);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_SENDER, NOM_EXPEDITEUR));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet, "UTF-8");
            message.setContent(corps, "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès à " + destinataire);
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Erreur d'envoi: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}