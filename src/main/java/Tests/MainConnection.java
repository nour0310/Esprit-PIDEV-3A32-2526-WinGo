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
import java.util.stream.Collectors;

public class MainConnection {

    private static EventCRUD eventCRUD = new EventCRUD();
    private static ParticipationCRUD participationCRUD = new ParticipationCRUD();

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("🚀 DÉMARRAGE DES TESTS UNITAIRES - GESTION DES ÉVÉNEMENTS");
        System.out.println("=".repeat(100));

        // Test database connection
        testDatabaseConnection();

        // Test Event CRUD operations
        testAddEvents();
        testDisplayAllEvents();
        testFilterEventsBySeason();
        testEventStatistics();
        testGroupEventsByLocation();
        testUpdateEvent();
        testDeleteEvent();

        // Test Participation CRUD
        testParticipations();

        System.out.println("\n" + "=".repeat(100));
        System.out.println("✅ TOUS LES TESTS SONT TERMINÉS");
        System.out.println("=".repeat(100));
    }

    private static void testDatabaseConnection() {
        System.out.println("\n🔵 TEST: Connexion à la base de données");
        System.out.println("-".repeat(50));

        try {
            MyBD.getInstance();
            System.out.println("   ✅ Connexion établie avec succès");
        } catch (Exception e) {
            System.out.println("   ❌ Erreur de connexion: " + e.getMessage());
        }
    }

    private static void testAddEvents() {
        System.out.println("\n🔵 TEST: Ajout d'événements");
        System.out.println("-".repeat(50));

        try {
            // Create test event 1
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

            eventCRUD.ajouter(event1);
            System.out.println("   ✅ Événement 1 ajouté: " + event1.getTitle() + " (ID: " + event1.getIdEvent() + ")");

            // Create test event 2
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

            eventCRUD.ajouter(event2);
            System.out.println("   ✅ Événement 2 ajouté: " + event2.getTitle() + " (ID: " + event2.getIdEvent() + ")");

        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
    }

    private static void testDisplayAllEvents() {
        System.out.println("\n🔵 TEST: Affichage de tous les événements");
        System.out.println("-".repeat(50));

        try {
            List<Event> events = eventCRUD.afficher();
            System.out.println("   📊 Total: " + events.size() + " événements trouvés");

            if (!events.isEmpty()) {
                System.out.println("\n   📋 Liste des événements:");
                System.out.println("   ┌──────┬──────────────────────────────────┬──────────────────┬────────────┬──────────┐");
                System.out.println("   │ ID   │ Titre                            │ Lieu             │ Date       │ Saison   │");
                System.out.println("   ├──────┼──────────────────────────────────┼──────────────────┼────────────┼──────────┤");

                events.stream()
                        .limit(10)
                        .forEach(e -> {
                            String title = e.getTitle() != null ? e.getTitle() : "N/A";
                            String location = e.getLocation() != null ? e.getLocation() : "N/A";
                            String date = e.getDateEvent() != null ? e.getDateEvent().toString() : "N/A";
                            String season = e.getSeason() != null ? e.getSeason() : "N/A";

                            System.out.printf("   │ %-4d │ %-32s │ %-16s │ %-10s │ %-8s │\n",
                                    e.getIdEvent(),
                                    truncate(title, 32),
                                    truncate(location, 16),
                                    date,
                                    season);
                        });

                System.out.println("   └──────┴──────────────────────────────────┴──────────────────┴────────────┴──────────┘");
            }

        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
    }

    private static void testFilterEventsBySeason() {
        System.out.println("\n🔵 TEST: Filtrage des événements par saison (Stream API)");
        System.out.println("-".repeat(50));

        try {
            List<Event> events = eventCRUD.afficher();

            // Filter summer events
            List<Event> summerEvents = events.stream()
                    .filter(e -> "Summer".equals(e.getSeason()))
                    .collect(Collectors.toList());

            System.out.println("   🌞 Événements d'été: " + summerEvents.size());
            summerEvents.stream()
                    .limit(3)
                    .forEach(e -> System.out.println("      • " + e.getTitle() + " (" + e.getDateEvent() + ")"));

            // Filter winter events
            List<Event> winterEvents = events.stream()
                    .filter(e -> "Winter".equals(e.getSeason()))
                    .collect(Collectors.toList());

            System.out.println("   ❄️ Événements d'hiver: " + winterEvents.size());
            winterEvents.stream()
                    .limit(3)
                    .forEach(e -> System.out.println("      • " + e.getTitle() + " (" + e.getDateEvent() + ")"));

            // Filter spring events
            List<Event> springEvents = events.stream()
                    .filter(e -> "Spring".equals(e.getSeason()))
                    .collect(Collectors.toList());

            System.out.println("   🌸 Événements de printemps: " + springEvents.size());

            // Filter autumn events
            List<Event> autumnEvents = events.stream()
                    .filter(e -> "Autumn".equals(e.getSeason()) || "Fall".equals(e.getSeason()))
                    .collect(Collectors.toList());

            System.out.println("   🍂 Événements d'automne: " + autumnEvents.size());

        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
    }

    private static void testEventStatistics() {
        System.out.println("\n🔵 TEST: Statistiques des événements (Stream API)");
        System.out.println("-".repeat(50));

        try {
            List<Event> events = eventCRUD.afficher();

            if (events.isEmpty()) {
                System.out.println("   ⚠ Aucun événement trouvé pour les statistiques");
                return;
            }

            // Basic statistics
            long totalEvents = events.size();
            double avgCapacity = events.stream()
                    .mapToInt(Event::getCapacity)
                    .average()
                    .orElse(0);

            int totalCapacity = events.stream()
                    .mapToInt(Event::getCapacity)
                    .sum();

            int maxCapacity = events.stream()
                    .mapToInt(Event::getCapacity)
                    .max()
                    .orElse(0);

            int minCapacity = events.stream()
                    .mapToInt(Event::getCapacity)
                    .min()
                    .orElse(0);

            Event eventWithMaxCapacity = events.stream()
                    .max((e1, e2) -> Integer.compare(e1.getCapacity(), e2.getCapacity()))
                    .orElse(null);

            Event eventWithMinCapacity = events.stream()
                    .min((e1, e2) -> Integer.compare(e1.getCapacity(), e2.getCapacity()))
                    .orElse(null);

            // Display statistics
            System.out.println("   📊 STATISTIQUES:");
            System.out.println("      ┌─────────────────────────────────────┐");
            System.out.printf("      │ Total événements          : %14d │\n", totalEvents);
            System.out.printf("      │ Capacité totale           : %14d │\n", totalCapacity);
            System.out.printf("      │ Capacité moyenne          : %14.2f │\n", avgCapacity);
            System.out.printf("      │ Capacité maximale         : %14d │\n", maxCapacity);
            System.out.printf("      │ Capacité minimale         : %14d │\n", minCapacity);
            System.out.println("      └─────────────────────────────────────┘");

            if (eventWithMaxCapacity != null) {
                System.out.println("      🏆 Événement avec plus de places: " +
                        eventWithMaxCapacity.getTitle() + " (" + eventWithMaxCapacity.getCapacity() + " places)");
            }

            if (eventWithMinCapacity != null) {
                System.out.println("      📉 Événement avec moins de places: " +
                        eventWithMinCapacity.getTitle() + " (" + eventWithMinCapacity.getCapacity() + " places)");
            }

        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
    }

    private static void testGroupEventsByLocation() {
        System.out.println("\n🔵 TEST: Groupement des événements par lieu (Stream API)");
        System.out.println("-".repeat(50));

        try {
            List<Event> events = eventCRUD.afficher();

            if (events.isEmpty()) {
                System.out.println("   ⚠ Aucun événement trouvé");
                return;
            }

            // Group by location
            var eventsByLocation = events.stream()
                    .filter(e -> e.getLocation() != null && !e.getLocation().isEmpty())
                    .collect(Collectors.groupingBy(
                            Event::getLocation,
                            Collectors.counting()
                    ));

            System.out.println("   📍 ÉVÉNEMENTS PAR LIEU:");

            eventsByLocation.entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                    .limit(10)
                    .forEach(entry -> {
                        double percentage = (entry.getValue() * 100.0) / events.size();
                        System.out.printf("      • %-15s : %2d événements (%5.2f%%)\n",
                                entry.getKey(), entry.getValue(), percentage);
                    });

        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
    }

    private static void testUpdateEvent() {
        System.out.println("\n🔵 TEST: Mise à jour d'un événement");
        System.out.println("-".repeat(50));

        try {
            List<Event> events = eventCRUD.afficher();

            if (events.isEmpty()) {
                System.out.println("   ⚠ Aucun événement à modifier");
                return;
            }

            // Get the first event
            Event eventToUpdate = events.get(0);
            System.out.println("   📝 Événement sélectionné: " + eventToUpdate.getTitle());

            // Update fields
            String originalTitle = eventToUpdate.getTitle();
            String newTitle = originalTitle + " (MIS À JOUR)";
            int newCapacity = eventToUpdate.getCapacity() + 100;

            eventToUpdate.setTitle(newTitle);
            eventToUpdate.setCapacity(newCapacity);
            eventToUpdate.setAvailablePlaces(newCapacity);

            eventCRUD.modifier(eventToUpdate);
            System.out.println("   ✅ Événement mis à jour:");
            System.out.println("      • Ancien titre: " + originalTitle);
            System.out.println("      • Nouveau titre: " + newTitle);
            System.out.println("      • Nouvelle capacité: " + newCapacity);

        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
    }

    private static void testDeleteEvent() {
        System.out.println("\n🔵 TEST: Suppression d'un événement");
        System.out.println("-".repeat(50));

        try {
            List<Event> events = eventCRUD.afficher();

            if (events.size() < 2) {
                System.out.println("   ⚠ Pas assez d'événements pour tester la suppression");
                return;
            }

            // Get the last event
            Event eventToDelete = events.get(events.size() - 1);
            System.out.println("   🗑 Événement à supprimer: " + eventToDelete.getTitle() + " (ID: " + eventToDelete.getIdEvent() + ")");

            eventCRUD.supprimer(eventToDelete.getIdEvent());
            System.out.println("   ✅ Événement supprimé avec succès");

            // Verify deletion
            List<Event> updatedEvents = eventCRUD.afficher();
            boolean stillExists = updatedEvents.stream()
                    .anyMatch(e -> e.getIdEvent() == eventToDelete.getIdEvent());

            if (!stillExists) {
                System.out.println("   ✅ Vérification: L'événement n'existe plus dans la base");
            }

        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
    }

    private static void testParticipations() {
        System.out.println("\n🔵 TEST: Gestion des participations");
        System.out.println("-".repeat(50));

        try {
            List<Event> events = eventCRUD.afficher();

            if (events.isEmpty()) {
                System.out.println("   ⚠ Aucun événement trouvé pour tester les participations");
                return;
            }

            Event testEvent = events.get(0);
            System.out.println("   📝 Test avec l'événement: " + testEvent.getTitle() + " (ID: " + testEvent.getIdEvent() + ")");

            // Add a participation
            Participation part = new Participation();
            part.setIdEvent(testEvent.getIdEvent());
            part.setIdUser(1);
            part.setDateParticipation(Date.valueOf(LocalDate.now()));
            part.setStatut("Confirmé");
            part.setNomParticipant("Ben Ali");
            part.setPrenomParticipant("Ahmed");
            part.setEmailParticipant("ahmed@test.com");
            part.setTelephone("12345678");
            part.setNombrePlaces(2);

            participationCRUD.ajouter(part);
            System.out.println("   ✅ Participation ajoutée (ID: " + part.getIdParticipation() + ")");

            // Display participations for this event
            List<Participation> participations = participationCRUD.afficherParEvent(testEvent.getIdEvent());
            System.out.println("   📋 Participations pour cet événement: " + participations.size());

            participations.forEach(p -> {
                System.out.println("      • " + p.getPrenomParticipant() + " " + p.getNomParticipant() +
                        " - " + p.getNombrePlaces() + " places [" + p.getStatut() + "]");
            });

            // Test search by email
            String email = "ahmed@test.com";
            List<Participation> searchResults = participationCRUD.rechercherParticipations(email);
            System.out.println("   🔍 Recherche par email '" + email + "': " + searchResults.size() + " résultat(s)");

            // Clean up - delete the test participation
            if (part.getIdParticipation() > 0) {
                participationCRUD.supprimer(part.getIdParticipation());
                System.out.println("   🗑 Participation de test supprimée");
            }

        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
    }

    private static String truncate(String str, int length) {
        if (str == null) return "N/A";
        return str.length() <= length ? str : str.substring(0, length - 3) + "...";
    }
}