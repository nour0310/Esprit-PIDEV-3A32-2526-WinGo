package Services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import Entites.Reclamation;
import Entites.Suggestion;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WhatsAppService {

    // CONFIGURATION TWILIO - REMPLACEZ PAR VOS IDENTIFIANTS
    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String WHATSAPP_FROM = "whatsapp:+14155238886"; // Numéro Twilio

    private static final String NOM_SERVICE = "WinGo Support";
    private final Map<String, Conversation> conversations = new HashMap<>();
    private final ReclamationCRUD reclamationCRUD = new ReclamationCRUD();
    private final SuggestionCRUD suggestionCRUD = new SuggestionCRUD();

    public WhatsAppService() {
        try {
            if (!"VOTRE_ACCOUNT_SID".equals(ACCOUNT_SID)) {
                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                System.out.println("✅ WhatsApp Service initialisé");
            } else {
                System.out.println("⚠ Mode simulation WhatsApp (configurez Twilio)");
            }
        } catch (Exception e) {
            System.err.println("⚠ Erreur Twilio: " + e.getMessage());
        }
    }

    public String recevoirMessage(String from, String body) {
        String numero = normaliserNumero(from);
        String texte = body != null ? body.trim().toLowerCase() : "";

        System.out.printf("[WhatsApp] %s → %s%n", numero, texte);

        // Commandes simples
        if (texte.equals("menu") || texte.equals("bonjour") || texte.equals("aide")) {
            return menuPrincipal();
        }

        if (texte.equals("horaire") || texte.equals("horaires")) {
            return horaires();
        }

        if (texte.equals("contact")) {
            return contactInfo();
        }

        // Gestion des conversations
        Conversation conv = getOrCreate(numero);
        return conv.traiterMessage(texte);
    }

    public void envoyerMessage(String to, String message) {
        try {
            if (!"VOTRE_ACCOUNT_SID".equals(ACCOUNT_SID)) {
                Message.creator(
                        new PhoneNumber("whatsapp:" + to),
                        new PhoneNumber(WHATSAPP_FROM),
                        message
                ).create();
                System.out.println("✅ Message envoyé à " + to);
            } else {
                System.out.println("[SIMULATION] À " + to + " : " + message.substring(0, Math.min(50, message.length())) + "...");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi: " + e.getMessage());
        }
    }

    private String menuPrincipal() {
        return """
            🌍 *WinGo Support*
            
            1️⃣ Nouvelle réclamation
            2️⃣ Nouvelle suggestion
            3️⃣ Suivre une réclamation
            4️⃣ Horaires
            5️⃣ Contact
            
            Répondez avec le numéro.""";
    }

    private String horaires() {
        return """
            🕐 *Horaires*
            Lun-Ven: 08h-18h
            Sam: 09h-13h
            Dim: Fermé
            
            Bot disponible 24/7""";
    }

    private String contactInfo() {
        return """
            📞 *Contact*
            Email: support@wingo.tn
            WhatsApp: ce canal
            Web: www.wingo.tn""";
    }

    private Conversation getOrCreate(String numero) {
        return conversations.computeIfAbsent(numero, k -> {
            Conversation c = new Conversation();
            c.numeroTelephone = numero;
            return c;
        });
    }

    private String normaliserNumero(String from) {
        return from.replace("whatsapp:", "").replace("+", "").trim();
    }

    // Classe interne pour gérer les conversations
    class Conversation {
        String etape = "MENU";
        Map<String, String> donnees = new HashMap<>();
        String numeroTelephone = "";  // numéro WhatsApp du client

        String traiterMessage(String msg) {
            switch (etape) {
                case "MENU":
                    switch (msg) {
                        case "1": etape = "REC_TYPE"; return "📝 *Nouvelle réclamation*\n\nType? (1:Général, 2:Service, 3:Transport, 4:Hébergement, 5:Restauration, 6:Technique, 7:Facturation, 8:Autre)";
                        case "2": etape = "SUG_CATEGORIE"; return "💡 *Nouvelle suggestion*\n\nCatégorie? (1:Interface, 2:Technique, 3:Service, 4:Application, 5:Sécurité, 6:Autre)";
                        case "3": etape = "CONSULTATION"; return "🔍 *Suivi*\n\nEntrez le numéro de réclamation (#123)";
                        default: return menuPrincipal();
                    }

                case "REC_TYPE":
                    donnees.put("type", msg);
                    etape = "REC_SUJET";
                    return "Sujet (en quelques mots) :";

                case "REC_SUJET":
                    donnees.put("sujet", msg);
                    etape = "REC_DESC";
                    return "Description détaillée :";

                case "REC_DESC":
                    donnees.put("description", msg);
                    etape = "REC_PRIO";
                    return "Priorité? (1:Basse, 2:Moyenne, 3:Haute, 4:Critique)";

                case "REC_PRIO":
                    String[] prios = {"Basse", "Moyenne", "Haute", "Critique"};
                    try {
                        int idx = Integer.parseInt(msg) - 1;
                        if (idx >= 0 && idx < prios.length) {
                            donnees.put("priorite", prios[idx]);
                            return creerReclamation();
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer
                    }
                    donnees.put("priorite", "Moyenne");
                    return creerReclamation();

                case "SUG_CATEGORIE":
                    String[] cats = {"Interface", "Technique", "Service", "Application", "Sécurité", "Autre"};
                    try {
                        int idx = Integer.parseInt(msg) - 1;
                        if (idx >= 0 && idx < cats.length) {
                            donnees.put("categorie", cats[idx]);
                            etape = "SUG_SUJET";
                            return "Sujet de votre suggestion :";
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer
                    }
                    donnees.put("categorie", "Autre");
                    etape = "SUG_SUJET";
                    return "Sujet de votre suggestion :";

                case "SUG_SUJET":
                    donnees.put("sujet", msg);
                    etape = "SUG_DESC";
                    return "Description détaillée :";

                case "SUG_DESC":
                    donnees.put("description", msg);
                    return creerSuggestion();

                case "CONSULTATION":
                    return consulterReclamation(msg);

                default:
                    etape = "MENU";
                    return menuPrincipal();
            }
        }

        private String creerReclamation() {
            try {
                // Chercher l'utilisateur par son numéro de téléphone
                // Si non trouvé → id_user = 0 (réclamation "anonyme WhatsApp")
                int idUser = resolveUserId(numeroTelephone);

                Reclamation r = new Reclamation(
                        idUser, // ID résolu depuis le numéro WhatsApp
                        donnees.getOrDefault("type", "Général"),
                        donnees.getOrDefault("sujet", "Sans sujet"),
                        donnees.getOrDefault("description", ""),
                        donnees.getOrDefault("priorite", "Moyenne"),
                        null
                );
                reclamationCRUD.ajouter(r);

                String reponse = String.format("""
                    ✅ *Réclamation créée*
                    
                    N°%d
                    Priorité: %s
                    
                    Vous serez notifié dès réponse.""",
                        r.getId_reclamation(),
                        r.getPriorite()
                );

                reset();
                return reponse;

            } catch (Exception e) {
                reset();
                return "❌ Erreur technique. Veuillez réessayer.";
            }
        }

        private String creerSuggestion() {
            try {
                int idUser = resolveUserId(numeroTelephone);

                Suggestion s = new Suggestion(
                        idUser,
                        donnees.getOrDefault("sujet", "Sans sujet"),
                        donnees.getOrDefault("description", ""),
                        donnees.getOrDefault("categorie", "Autre"),
                        null
                );
                suggestionCRUD.ajouter(s);

                String reponse = String.format("""
                    ✅ *Suggestion enregistrée*
                    
                    N°%d
                    Catégorie: %s
                    
                    Merci pour votre contribution !""",
                        s.getId_suggestion(),
                        s.getCategorie()
                );

                reset();
                return reponse;

            } catch (Exception e) {
                reset();
                return "❌ Erreur technique. Veuillez réessayer.";
            }
        }

        private String consulterReclamation(String msg) {
            try {
                String idStr = msg.replace("#", "").trim();
                int id = Integer.parseInt(idStr);
                Reclamation r = reclamationCRUD.getById(id);

                if (r != null) {
                    String statut = r.getStatut();
                    String icone = switch (statut) {
                        case "Résolue" -> "✅";
                        case "En cours" -> "🔄";
                        case "Rejetée" -> "❌";
                        default -> "⏳";
                    };

                    String reponse = String.format("""
                        📋 *Réclamation #%d*
                        
                        Sujet: %s
                        Type: %s
                        Priorité: %s
                        %s Statut: %s
                        
                        %s
                        """,
                            r.getId_reclamation(),
                            r.getSujet(),
                            r.getType_reclamation(),
                            r.getPriorite(),
                            icone, statut,
                            r.getReponse_admin() != null ? "Réponse: " + r.getReponse_admin() : "Pas encore de réponse"
                    );

                    reset();
                    return reponse;
                }
            } catch (NumberFormatException e) {
                // Ignorer
            }

            reset();
            return "❓ Réclamation non trouvée.\n\n" + menuPrincipal();
        }

        private void reset() {
            etape = "MENU";
            donnees.clear();
        }

        /**
         * Résout l'id_user à partir du numéro WhatsApp.
         * Pour l'instant retourne 0 (anonyme) si non trouvé.
         * Quand votre table user aura un champ "telephone", remplacez par :
         *   UserCRUD userCRUD = new UserCRUD();
         *   User u = userCRUD.findByTelephone(numero);
         *   return u != null ? u.getId() : 0;
         */
        private int resolveUserId(String numero) {
            // TODO: remplacer par une vraie recherche en BDD quand le module User sera prêt
            // Ex: SELECT id FROM user WHERE telephone = ?
            System.out.println("[WhatsApp] Réclamation anonyme depuis " + numero + " → id_user=0");
            return 0;
        }
    }
}