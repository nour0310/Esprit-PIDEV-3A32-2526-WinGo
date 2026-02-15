package Tests;

import Entites.Event;
import Entites.Participation;
import Services.EventCRUD;
import Services.ParticipationCRUD;
import Utils.MyBD;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MainConnection {

    public static void main(String[] args) {

        // Test connexion
        MyBD myBD = MyBD.getInstance();
        System.out.println(" Connexion à la base de données établie");

        // ==================== TEST EVENT CRUD ====================
        System.out.println("\n========== TEST EVENT CRUD ==========");

        EventCRUD eventCRUD = new EventCRUD();

        // Créer des événements de test
        Event event1 = new Event();
        event1.setTitle("Festival de Carthage");
        event1.setDescription("Festival international de musique");
        event1.setLocation("Carthage");
        event1.setDateEvent(Date.valueOf(LocalDate.of(2024, 7, 15)));
        event1.setStartTime(Time.valueOf(LocalTime.of(20, 0)));
        event1.setCapacity(500);
        event1.setAvailablePlaces(500);
        event1.setSeason("Summer");
        event1.setEventType("Music");
        event1.setStatus("Planifié");
        event1.setImageEvent("carthage.jpg");

        Event event2 = new Event();
        event2.setTitle("Sahara Festival");
        event2.setDescription("Festival du désert");
        event2.setLocation("Douz");
        event2.setDateEvent(Date.valueOf(LocalDate.of(2024, 12, 20)));
        event2.setStartTime(Time.valueOf(LocalTime.of(15, 0)));
        event2.setCapacity(300);
        event2.setAvailablePlaces(300);
        event2.setSeason("Winter");
        event2.setEventType("Cultural");
        event2.setStatus("Planifié");
        event2.setImageEvent("douz.jpg");

        try {
            // Ajouter des événements
            System.out.println("\n Ajout des événements...");
            eventCRUD.ajouter(event1);
            eventCRUD.ajouter(event2);
            System.out.println(" Événements ajoutés avec succès");

            // Afficher tous les événements
            System.out.println("\n Liste des événements:");
            List<Event> events = eventCRUD.afficher();
            for (Event e : events) {
                System.out.println("   - " + e.getIdEvent() + ": " + e.getTitle() + " (" + e.getDateEvent() + ")");
            }

            // ==================== TEST PARTICIPATION CRUD ====================
            System.out.println("\n========== TEST PARTICIPATION CRUD ==========");

            ParticipationCRUD participationCRUD = new ParticipationCRUD();

            // Récupérer le premier événement pour les tests
            if (!events.isEmpty()) {
                Event firstEvent = events.get(0);
                System.out.println("\n Test pour l'événement: " + firstEvent.getTitle() + " (ID: " + firstEvent.getIdEvent() + ")");

                // Créer des participations de test
                Participation part1 = new Participation();
                part1.setIdEvent(firstEvent.getIdEvent());
                part1.setIdUser(1); // ID utilisateur de test
                part1.setDateParticipation(Date.valueOf(LocalDate.now()));
                part1.setStatut("Confirmé");
                part1.setNomParticipant("Ben Ali");
                part1.setPrenomParticipant("Ahmed");
                part1.setEmailParticipant("ahmed@test.com");
                part1.setTelephone("12345678");
                part1.setNombrePlaces(2);

                Participation part2 = new Participation();
                part2.setIdEvent(firstEvent.getIdEvent());
                part2.setIdUser(2);
                part2.setDateParticipation(Date.valueOf(LocalDate.now()));
                part2.setStatut("En attente");
                part2.setNomParticipant("Trabelsi");
                part2.setPrenomParticipant("Sarra");
                part2.setEmailParticipant("sarra@test.com");
                part2.setTelephone("87654321");
                part2.setNombrePlaces(1);

                // Ajouter des participations
                System.out.println("\n Ajout des participations...");
                participationCRUD.ajouter(part1);
                participationCRUD.ajouter(part2);
                System.out.println(" Participations ajoutées avec succès");

                // Afficher les participations pour cet événement
                System.out.println("\n Participations pour l'événement " + firstEvent.getIdEvent() + ":");
                List<Participation> participations = participationCRUD.afficherParEvent(firstEvent.getIdEvent());
                for (Participation p : participations) {
                    System.out.println("   - ID: " + p.getIdParticipation() +
                            ", Participant: " + p.getPrenomParticipant() + " " + p.getNomParticipant() +
                            ", Places: " + p.getNombrePlaces() +
                            ", Statut: " + p.getStatut());
                }

                // Tester la recherche par email
                System.out.println("\n Recherche par email 'ahmed@test.com':");
                List<Participation> searchByEmail = participationCRUD.afficherParClient("ahmed@test.com");
                for (Participation p : searchByEmail) {
                    System.out.println("   - Trouvé: " + p.getPrenomParticipant() + " " + p.getNomParticipant());
                }

                // Tester la recherche par nom
                System.out.println("\n Recherche par nom 'Ben Ali', 'Ahmed':");
                List<Participation> searchByName = participationCRUD.afficherParNomClient("Ben Ali", "Ahmed");
                for (Participation p : searchByName) {
                    System.out.println("   - Trouvé: " + p.getPrenomParticipant() + " " + p.getNomParticipant());
                }

                // Toutes les participations
                System.out.println("\n Toutes les participations:");
                List<Participation> allParticipations = participationCRUD.afficherTous();
                for (Participation p : allParticipations) {
                    System.out.println("   - ID: " + p.getIdParticipation() +
                            ", Event: " + p.getIdEvent() +
                            ", Participant: " + p.getPrenomParticipant() + " " + p.getNomParticipant());
                }

                // Supprimer une participation (optionnel)
                if (!participations.isEmpty()) {
                    System.out.println("\n🗑 Suppression de la participation ID: " + participations.get(0).getIdParticipation());
                    participationCRUD.supprimer(participations.get(0).getIdParticipation());
                    System.out.println(" Participation supprimée");
                }
            }

            // Supprimer les événements de test (optionnel)
            System.out.println("\n🗑 Nettoyage des données de test...");
            for (Event e : events) {
                eventCRUD.supprimer(e.getIdEvent());
                System.out.println("   - Événement supprimé: " + e.getTitle());
            }
            System.out.println(" Nettoyage terminé");

        } catch (Exception e) {
            System.err.println(" Erreur lors des tests:");
            e.printStackTrace();
        }
    }
}