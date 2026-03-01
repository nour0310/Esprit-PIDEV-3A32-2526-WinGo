package Tests;

import Entites.Reclamation;
import Entites.Suggestion;
import Services.ReclamationCRUD;
import Services.SuggestionCRUD;
import Utils.MyBD;

import java.sql.SQLException;
import java.util.List;

public class MainConnection {

    public static void main(String[] args) {

        System.out.println("=== TEST DE CONNEXION À LA BASE DE DONNÉES ===\n");

        // Test connexion à la base de données
        try {
            MyBD myBD = MyBD.getInstance();
            System.out.println("✅ Connexion à la base de données établie avec succès !\n");
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion à la base de données : " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Test des opérations CRUD
        testReclamationCRUD();
        testSuggestionCRUD();
        testLiaisonReclamationSuggestion();
    }

    public static void testReclamationCRUD() {
        System.out.println("\n=== TEST DES OPÉRATIONS CRUD RÉCLAMATION ===\n");

        ReclamationCRUD reclamationCRUD = new ReclamationCRUD();

        // 1. TEST AJOUT
        System.out.println("--- Test d'ajout de réclamations ---");

        Reclamation r1 = new Reclamation();
        r1.setId_user(1); // À ajuster selon votre base
        r1.setType_reclamation("Technique");
        r1.setSujet("Problème de connexion");
        r1.setDescription("Impossible de se connecter à l'application");
        r1.setPriorite("Haute");
        r1.setPiece_jointe("capture1.png");

        Reclamation r2 = new Reclamation();
        r2.setId_user(1);
        r2.setType_reclamation("Service");
        r2.setSujet("Retard de livraison");
        r2.setDescription("Commande non reçue dans les délais");
        r2.setPriorite("Moyenne");
        r2.setPiece_jointe("facture.pdf");

        try {
            // Ajout des réclamations
            reclamationCRUD.ajouter(r1);
            System.out.println("✅ Réclamation 1 ajoutée avec ID: " + r1.getId_reclamation());

            reclamationCRUD.ajouter(r2);
            System.out.println("✅ Réclamation 2 ajoutée avec ID: " + r2.getId_reclamation());

            // 2. TEST AFFICHAGE
            System.out.println("\n--- Affichage de toutes les réclamations ---");
            List<Reclamation> reclamations = reclamationCRUD.afficherTous();
            if (reclamations.isEmpty()) {
                System.out.println("⚠️ Aucune réclamation trouvée");
            } else {
                for (Reclamation r : reclamations) {
                    afficherReclamation(r);
                }
            }

            // 3. TEST RECHERCHE PAR ID
            if (!reclamations.isEmpty()) {
                int premierId = reclamations.get(0).getId_reclamation();
                System.out.println("\n--- Recherche de la réclamation ID: " + premierId + " ---");
                Reclamation rTrouvee = reclamationCRUD.getById(premierId);
                if (rTrouvee != null) {
                    System.out.println("✅ Réclamation trouvée :");
                    afficherReclamation(rTrouvee);
                }
            }

            // 4. TEST RECHERCHE PAR UTILISATEUR
            System.out.println("\n--- Recherche des réclamations de l'utilisateur 1 ---");
            List<Reclamation> userReclamations = reclamationCRUD.getByUser(1);
            System.out.println("Trouvé " + userReclamations.size() + " réclamation(s)");

            // 5. TEST MODIFICATION
            if (!reclamations.isEmpty()) {
                System.out.println("\n--- Test de modification ---");
                Reclamation rAModifier = reclamations.get(0);
                rAModifier.setPriorite("Critique");
                rAModifier.setDescription(rAModifier.getDescription() + " [URGENT]");

                reclamationCRUD.modifier(rAModifier);
                System.out.println("✅ Réclamation modifiée (priorité et description)");
            }

            // 6. TEST RÉPONSE ADMIN
            if (!reclamations.isEmpty()) {
                System.out.println("\n--- Test de réponse admin ---");
                int id = reclamations.get(0).getId_reclamation();
                reclamationCRUD.repondre(id, "Votre problème a été résolu", "Résolue");
                System.out.println("✅ Réponse admin ajoutée");
            }

            // 7. TEST RECHERCHE
            System.out.println("\n--- Test de recherche avec mot-clé 'connexion' ---");
            List<Reclamation> recherche = reclamationCRUD.rechercher("connexion");
            System.out.println("Trouvé " + recherche.size() + " résultat(s)");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors des tests Réclamation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testSuggestionCRUD() {
        System.out.println("\n=== TEST DES OPÉRATIONS CRUD SUGGESTION ===\n");

        SuggestionCRUD suggestionCRUD = new SuggestionCRUD();

        // 1. TEST AJOUT
        System.out.println("--- Test d'ajout de suggestions ---");

        Suggestion s1 = new Suggestion();
        s1.setId_user(1);
        s1.setSujet("Nouvelle fonctionnalité");
        s1.setDescription("Ajouter un mode sombre");
        s1.setCategorie("Interface");

        Suggestion s2 = new Suggestion();
        s2.setId_user(1);
        s2.setSujet("Amélioration performance");
        s2.setDescription("Optimiser le chargement des images");
        s2.setCategorie("Technique");

        try {
            // Ajout des suggestions
            suggestionCRUD.ajouter(s1);
            System.out.println("✅ Suggestion 1 ajoutée avec ID: " + s1.getId_suggestion());

            suggestionCRUD.ajouter(s2);
            System.out.println("✅ Suggestion 2 ajoutée avec ID: " + s2.getId_suggestion());

            // 2. TEST AFFICHAGE
            System.out.println("\n--- Affichage de toutes les suggestions ---");
            List<Suggestion> suggestions = suggestionCRUD.afficherTous();
            if (suggestions.isEmpty()) {
                System.out.println("⚠️ Aucune suggestion trouvée");
            } else {
                for (Suggestion s : suggestions) {
                    afficherSuggestion(s);
                }
            }

            // 3. TEST RECHERCHE PAR CATÉGORIE
            System.out.println("\n--- Recherche par catégorie 'Interface' ---");
            List<Suggestion> suggestionsInterface = suggestionCRUD.getByCategorie("Interface");
            System.out.println("Trouvé " + suggestionsInterface.size() + " suggestion(s)");

            // 4. TEST RÉPONSE ADMIN
            if (!suggestions.isEmpty()) {
                System.out.println("\n--- Test de réponse admin ---");
                int id = suggestions.get(0).getId_suggestion();
                suggestionCRUD.repondre(id, "Merci pour votre suggestion, nous l'étudierons", "Acceptée");
                System.out.println("✅ Réponse admin ajoutée");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors des tests Suggestion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testLiaisonReclamationSuggestion() {
        System.out.println("\n=== TEST DE LIAISON RÉCLAMATION-SUGGESTION ===\n");

        SuggestionCRUD suggestionCRUD = new SuggestionCRUD();
        ReclamationCRUD reclamationCRUD = new ReclamationCRUD();

        try {
            // Récupérer les dernières réclamation et suggestion
            List<Reclamation> reclamations = reclamationCRUD.afficherTous();
            List<Suggestion> suggestions = suggestionCRUD.afficherTous();

            if (!reclamations.isEmpty() && !suggestions.isEmpty()) {
                int idReclamation = reclamations.get(0).getId_reclamation();
                int idSuggestion = suggestions.get(0).getId_suggestion();

                System.out.println("--- Liaison suggestion " + idSuggestion +
                        " à réclamation " + idReclamation + " ---");

                suggestionCRUD.lierAReclamation(idSuggestion, idReclamation);
                System.out.println("✅ Suggestion liée à la réclamation avec succès !");

                // Vérification
                Suggestion sVerif = suggestionCRUD.getById(idSuggestion);
                System.out.println("Vérification : ID réclamation lié = " + sVerif.getId_reclamation());
            } else {
                System.out.println("⚠️ Pas assez de données pour tester la liaison");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test de liaison : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthodes d'affichage
    private static void afficherReclamation(Reclamation r) {
        System.out.println("  ID: " + r.getId_reclamation());
        System.out.println("  Utilisateur: " + r.getId_user());
        System.out.println("  Type: " + r.getType_reclamation());
        System.out.println("  Sujet: " + r.getSujet());
        System.out.println("  Description: " + r.getDescription());
        System.out.println("  Date: " + r.getDate_reclamation());
        System.out.println("  Statut: " + r.getStatut());
        System.out.println("  Priorité: " + r.getPriorite());
        System.out.println("  Pièce jointe: " + r.getPiece_jointe());
        if (r.getReponse_admin() != null) {
            System.out.println("  Réponse: " + r.getReponse_admin());
            System.out.println("  Date réponse: " + r.getDate_reponse());
        }
        System.out.println("  --------------------");
    }

    private static void afficherSuggestion(Suggestion s) {
        System.out.println("  ID: " + s.getId_suggestion());
        System.out.println("  Utilisateur: " + s.getId_user());
        System.out.println("  Sujet: " + s.getSujet());
        System.out.println("  Description: " + s.getDescription());
        System.out.println("  Catégorie: " + s.getCategorie());
        System.out.println("  Date: " + s.getDate_suggestion());
        System.out.println("  Statut: " + s.getStatut());
        if (s.getReponse_admin() != null) {
            System.out.println("  Réponse: " + s.getReponse_admin());
            System.out.println("  Date réponse: " + s.getDate_reponse());
        }
        if (s.getId_reclamation() != null) {
            System.out.println("  Lié à réclamation: " + s.getId_reclamation());
        }
        System.out.println("  --------------------");
    }
}