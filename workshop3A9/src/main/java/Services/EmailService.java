package Services;

import Entites.Reclamation;
import Entites.Suggestion;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final String username = "wingo.support@gmail.com"; // User should provide real creds
    private final String password = "app_password_here";

    private Session createSession() {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); // TLS

        return Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void envoyerCodeVerification(String toEmail, String code) {
        String subject = "WinGo - Code de vérification";
        String content = "Bonjour,\n\nVotre code de vérification est : " + code + "\n\nBienvenue chez WinGo !";

        sendEmail(toEmail, subject, content);
    }

    private void sendEmail(String toEmail, String subject, String content) {
        // ALWAYS log to console for development/testing
        System.out.println("--------------------------------------");
        System.out.println("TESTING: Verification Email Simulation");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Content: " + content);
        System.out.println("--------------------------------------");

        try {
            Message message = new MimeMessage(createSession());
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    // Previous methods kept as stubs for now or removed if not needed anymore
    public boolean envoyerConfirmationReclamation(Reclamation r, String email, String nom) {
        System.out.println("Simulation: Email de confirmation envoyé à " + email);
        return true;
    }

    public boolean envoyerReponseReclamation(Reclamation r, String email, String nom) {
        System.out.println("Simulation: Notification de réponse envoyée à " + email);
        return true;
    }

    public boolean envoyerConfirmationSuggestion(Suggestion s, String email, String nom) {
        System.out.println("Simulation: Email de confirmation envoyé à " + email);
        return true;
    }

    public void notifierAdminNouvelleReclamation(Reclamation r) {
        System.out.println("Simulation: Admin notifié");
    }
}
