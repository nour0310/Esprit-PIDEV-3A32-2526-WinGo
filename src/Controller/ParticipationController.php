<?php

namespace App\Controller;

use App\Entity\Event;
use App\Entity\Participation;
use App\Service\QrCodeService;
use App\Service\MailjetTicketMailer;
use App\Service\DiscountEventService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use App\Service\StreamChatService;



class ParticipationController extends AbstractController
{
   public function __construct(
    private EntityManagerInterface $em,
    private QrCodeService $qrCodeService,
    private MailjetTicketMailer $ticketMailer,
    private DiscountEventService $discountEventService,
    private StreamChatService $streamChatService   // add this
) {}

    /**
     * Génère le contenu texte du QR code (toutes les infos de la réservation)
     */
    private function getQrCodeContent(Participation $participation, Event $event): string
    {
        return sprintf(
            "🎫 RÉSERVATION WinGo\n\n" .
            "Événement : %s\n" .
            "Date : %s à %s\n" .
            "Lieu : %s\n\n" .
            "Participant : %s %s\n" .
            "Email : %s\n" .
            "Tél. : %s\n" .
            "Places : %d\n" .
            "Statut : %s\n" .
            "Réf. : %s\n\n" .
            "Présentez ce code à l'entrée.",
            $event->getTitle(),
            $event->getDate_event()->format('d/m/Y'),
            $event->getStart_time(),
            $event->getLocation(),
            $participation->getPrenomParticipant(),
            $participation->getNomParticipant(),
            $participation->getEmailParticipant(),
            $participation->getTelephone(),
            $participation->getNombrePlaces(),
            strtoupper($participation->getStatut()),
            $participation->getToken()
        );
    }

    // ---------- FRONT (client) ----------

    #[Route('/events/{id_event}/register', name: 'participation_front_new')]
    #[IsGranted('ROLE_USER')]
    public function frontNew(int $id_event, Request $request): Response
    {
        $event = $this->em->getRepository(Event::class)->find($id_event);
        if (!$event) {
            throw $this->createNotFoundException('Événement introuvable');
        }

        $today = new \DateTime();
        $today->setTime(0, 0, 0);
        $eventDate = $event->getDate_event();
        $eventDate->setTime(0, 0, 0);
        if ($eventDate < $today) {
            $this->addFlash('error', 'Cet événement est déjà passé. Vous ne pouvez pas vous inscrire.');
            return $this->redirectToRoute('event_front');
        }

        $user = $this->getUser();

        $existingActive = $this->em->getRepository(Participation::class)->findOneBy([
            'id_event' => $event,
            'id_user' => $user->getId(),
            'statut' => 'confirmed'
            
        ]);

        if ($existingActive) {
            $this->addFlash('error', 'Vous avez déjà une réservation active pour cet événement.');
            return $this->redirectToRoute('client_event_reservations');
        }

        if ($request->isMethod('POST')) {
            $places = (int) $request->request->get('nombre_places');
            $telephone = $request->request->get('telephone');

            $errors = [];
            if (empty($telephone)) {
                $errors[] = 'Le téléphone est obligatoire.';
            }
            if ($places <= 0) {
                $errors[] = 'Le nombre de places doit être supérieur à 0.';
            } elseif ($places > $event->getAvailable_places()) {
                $errors[] = "Seulement {$event->getAvailable_places()} place(s) restante(s).";
            }

            if (count($errors) > 0) {
                foreach ($errors as $error) {
                    $this->addFlash('error', $error);
                }
                return $this->redirectToRoute('participation_front_new', ['id_event' => $id_event]);
            }

            // 🔥 Calcul du prix réduit (si applicable)
            $unitPrice = $this->discountEventService->getDiscountedPrice($event);
            $totalPrice = $unitPrice * $places;

            $participation = new Participation();
            $participation->setIdEvent($event);
            $participation->setIdUser($user->getId());
            $participation->setNomParticipant($user->getNom());
            $participation->setPrenomParticipant($user->getPrenom());
            $participation->setEmailParticipant($user->getEmail());
            $participation->setTelephone($telephone);
            $participation->setNombrePlaces($places);
            $participation->setDateParticipation(new \DateTime());
            $participation->setStatut('confirmed');
            $participation->setToken(bin2hex(random_bytes(32)));
            $participation->setIsUsed(false);
            $participation->setUnitPrice($unitPrice);
            $participation->setTotalPrice($totalPrice);

            $event->setAvailable_places($event->getAvailable_places() - $places);
            $this->em->persist($participation);
            $this->em->flush();


            // Add user to the event group chat
$this->streamChatService->addUserToEventChannel($event, $user);

            // Générer le QR code avec le texte complet
            $qrContent = $this->getQrCodeContent($participation, $event);
            $qrCodeData = $this->qrCodeService->generateQrCode($qrContent);
            $this->ticketMailer->sendTicketEmail($participation, $event, $qrCodeData);

            $this->addFlash('success', 'Votre inscription est confirmée ! Un email avec votre billet vous a été envoyé.');
            return $this->redirectToRoute('client_event_reservations');
        }

        // 🔥 Variables pour l'affichage du formulaire (GET)
        $unitPrice = $this->discountEventService->getDiscountedPrice($event);
        $discountActive = $this->discountEventService->isDiscountActive($event);

        return $this->render('front/participation.html.twig', [
            'event' => $event,
            'user'  => $user,
            'discounted_price' => $unitPrice,
            'discount_active'  => $discountActive,
            'original_price'   => $event->getPrice(),
        ]);
    }

    #[Route('/client/mes-reservations-evenements', name: 'client_event_reservations')]
    #[IsGranted('ROLE_USER')]
    public function clientReservations(): Response
    {
        $user = $this->getUser();

        $participations = $this->em->getRepository(Participation::class)
            ->createQueryBuilder('p')
            ->innerJoin('p.id_event', 'e')
            ->where('p.id_user = :userId')
            ->andWhere('p.statut != :cancelled')
            ->setParameter('userId', $user->getId())
            ->setParameter('cancelled', 'cancelled')
            ->orderBy('p.date_participation', 'DESC')
            ->getQuery()
            ->getResult();

        return $this->render('client/reservations_events.html.twig', [
            'participations' => $participations,
        ]);
    }

    #[Route('/client/modifier-reservation/{id_participation}', name: 'client_edit_reservation')]
    #[IsGranted('ROLE_USER')]
    public function clientEdit(int $id_participation, Request $request): Response
    {
        $participation = $this->em->getRepository(Participation::class)->find($id_participation);
        $user = $this->getUser();

        if (!$participation || $participation->getIdUser() !== $user->getId()) {
            throw $this->createNotFoundException('Réservation introuvable');
        }

        if ($participation->getStatut() === 'cancelled') {
            $this->addFlash('error', 'Impossible de modifier une réservation annulée.');
            return $this->redirectToRoute('client_event_reservations');
        }

        $event = $participation->getIdEvent();
        if (!$event) {
            $this->addFlash('error', 'L\'événement associé à cette réservation n\'existe plus.');
            return $this->redirectToRoute('client_event_reservations');
        }

        $today = new \DateTime();
        $interval = $today->diff($event->getDate_event());
        $daysUntilEvent = (int) $interval->format('%r%a');
        $isWithin7Days = ($daysUntilEvent >= 0 && $daysUntilEvent <= 7);

        if ($request->isMethod('POST')) {
            $newPlaces = (int) $request->request->get('nombre_places');
            $oldPlaces = $participation->getNombrePlaces();
            $delta = $newPlaces - $oldPlaces;

            if ($newPlaces <= 0) {
                $this->addFlash('error', 'Le nombre de places doit être positif.');
                return $this->redirectToRoute('client_edit_reservation', ['id_participation' => $id_participation]);
            }

            if ($isWithin7Days && $newPlaces < $oldPlaces) {
                $this->addFlash('error', 'Vous ne pouvez pas réduire le nombre de places à moins de 7 jours de l\'événement.');
                return $this->redirectToRoute('client_edit_reservation', ['id_participation' => $id_participation]);
            }

            if ($delta > 0 && $delta > $event->getAvailable_places()) {
                $this->addFlash('error', "Pas assez de places disponibles (seulement {$event->getAvailable_places()} restantes).");
                return $this->redirectToRoute('client_edit_reservation', ['id_participation' => $id_participation]);
            }

            $participation->setNombrePlaces($newPlaces);
            $event->setAvailable_places($event->getAvailable_places() - $delta);
            $this->em->flush();

            $this->addFlash('success', 'Réservation mise à jour avec succès.');
            return $this->redirectToRoute('client_event_reservations');
        }

        return $this->render('client/edit_reservation.html.twig', [
            'participation' => $participation,
            'event' => $event,
            'isWithin7Days' => $isWithin7Days,
        ]);
    }

    #[Route('/client/annuler-reservation/{id_participation}', name: 'client_cancel_reservation')]
    #[IsGranted('ROLE_USER')]
    public function clientCancel(int $id_participation): Response
    {
        $participation = $this->em->getRepository(Participation::class)->find($id_participation);
        $user = $this->getUser();

        if (!$participation || $participation->getIdUser() !== $user->getId()) {
            throw $this->createNotFoundException('Réservation introuvable');
        }

        if ($participation->getStatut() === 'cancelled') {
            $this->addFlash('error', 'Cette réservation est déjà annulée.');
            return $this->redirectToRoute('client_event_reservations');
        }

        $event = $participation->getIdEvent();
        if (!$event) {
            $this->addFlash('error', 'L\'événement associé à cette réservation n\'existe plus.');
            return $this->redirectToRoute('client_event_reservations');
        }

        $today = new \DateTime();
        $interval = $today->diff($event->getDate_event());
        $daysUntilEvent = (int) $interval->format('%r%a');

        if ($daysUntilEvent >= 0 && $daysUntilEvent <= 7) {
            $this->addFlash('error', 'Impossible d’annuler : l’événement a lieu dans moins de 7 jours.');
            return $this->redirectToRoute('client_event_reservations');
        }

        $participation->setStatut('cancelled');
        $event->setAvailable_places($event->getAvailable_places() + $participation->getNombrePlaces());
        $this->em->flush();

        $this->addFlash('success', 'Réservation annulée. Vous pouvez à nouveau vous inscrire à cet événement si vous le souhaitez.');
        return $this->redirectToRoute('client_event_reservations');
    }

    // ---------- FEEDBACK SUBMISSION ----------
 // In ParticipationController.php, replace the submitFeedback method with this:

#[Route('/event/{id}/submit-feedback', name: 'submit_feedback', methods: ['POST'])]
#[IsGranted('ROLE_USER')]
public function submitFeedback(Event $event, Request $request): Response
{
    $user = $this->getUser();
    $now = new \DateTime();

    $participation = $this->em->getRepository(Participation::class)->findOneBy([
        'id_event' => $event,
        'id_user' => $user->getId(),
        'statut' => 'confirmed'
    ]);
    if (!$participation) {
        if ($request->isXmlHttpRequest()) {
            return $this->json(['error' => 'You did not participate in this event.'], 403);
        }
        $this->addFlash('error', 'You did not participate in this event.');
        return $this->redirectToRoute('event_front');
    }
    if ($event->getDate_event() > $now) {
        if ($request->isXmlHttpRequest()) {
            return $this->json(['error' => 'Feedback only after event date.'], 403);
        }
        $this->addFlash('error', 'Feedback only after event date.');
        return $this->redirectToRoute('event_front');
    }
    if ($event->hasUserFeedback($user->getId())) {
        if ($request->isXmlHttpRequest()) {
            return $this->json(['error' => 'You already submitted feedback.'], 400);
        }
        $this->addFlash('error', 'You already submitted feedback.');
        return $this->redirectToRoute('event_front');
    }

    $feeling = $request->request->get('feeling');
    $comment = $request->request->get('comment');
    $valid = ['like','love','haha','wow','sad','angry'];
    if (!in_array($feeling, $valid)) {
        if ($request->isXmlHttpRequest()) {
            return $this->json(['error' => 'Invalid feeling.'], 400);
        }
        $this->addFlash('error', 'Invalid feeling.');
        return $this->redirectToRoute('event_front');
    }

    $feedback = [
        'userId' => $user->getId(),
        'participantName' => $user->getPrenom() . ' ' . $user->getNom(),
        'feeling' => $feeling,
        'comment' => $comment,
        'createdAt' => $now->format('Y-m-d H:i:s'),
    ];
    $event->addFeedback($feedback);
    $this->em->flush();

    if ($request->isXmlHttpRequest()) {
        return $this->json([
            'success' => true,
            'feedback' => $feedback,
            'message' => 'Feedback submitted. Thank you!'
        ]);
    }
    $this->addFlash('success', 'Thank you for your feedback!');
    return $this->redirectToRoute('event_show_front', ['id' => $event->getId_event()]);
}
    // ---------- PUBLIC : afficher une réservation par token (optionnel, peut être supprimé) ----------
    #[Route('/reservation/{token}', name: 'reservation_show_by_token')]
    public function showReservationByToken(string $token): Response
    {
        $participation = $this->em->getRepository(Participation::class)->findOneBy(['token' => $token]);
        if (!$participation) {
            throw $this->createNotFoundException('Réservation introuvable.');
        }
        $event = $participation->getIdEvent();

        return $this->render('public/reservation_show.html.twig', [
            'participation' => $participation,
            'event' => $event,
        ]);
    }

    // ---------- BACKOFFICE (admin) ----------

    #[Route('/admin/events/{id_event}/participations', name: 'participation_index')]
    #[IsGranted('ROLE_ADMIN')]
    public function index(int $id_event, Request $request): Response
    {
        $event = $this->em->getRepository(Event::class)->find($id_event);
        if (!$event) {
            throw $this->createNotFoundException('Événement introuvable');
        }

        $qb = $this->em->getRepository(Participation::class)->createQueryBuilder('p')
            ->where('p.id_event = :event')
            ->setParameter('event', $event);

        $search = $request->query->get('search', '');
        if (!empty($search)) {
            $qb->andWhere('p.nom_participant LIKE :search OR p.prenom_participant LIKE :search OR p.email_participant LIKE :search')
               ->setParameter('search', '%' . $search . '%');
        }

        $status = $request->query->get('status', '');
        if (!empty($status) && $status !== 'all') {
            $qb->andWhere('p.statut = :status')->setParameter('status', $status);
        }

        $sort = $request->query->get('sort', 'date_desc');
        switch ($sort) {
            case 'date_asc':
                $qb->orderBy('p.date_participation', 'ASC');
                break;
            case 'places_asc':
                $qb->orderBy('p.nombre_places', 'ASC');
                break;
            case 'places_desc':
                $qb->orderBy('p.nombre_places', 'DESC');
                break;
            default:
                $qb->orderBy('p.date_participation', 'DESC');
                break;
        }

        $participations = $qb->getQuery()->getResult();

        return $this->render('back/participation.html.twig', [
            'mode' => 'list',
            'event' => $event,
            'participations' => $participations,
            'current_search' => $search,
            'current_status' => $status,
            'current_sort' => $sort,
        ]);
    }

    #[Route('/admin/events/{id_event}/participations/{id_participation}', name: 'participation_show')]
    #[IsGranted('ROLE_ADMIN')]
    public function show(int $id_event, int $id_participation): Response
    {
        $event = $this->em->getRepository(Event::class)->find($id_event);
        $participation = $this->em->getRepository(Participation::class)->find($id_participation);

        if (!$event || !$participation) {
            throw $this->createNotFoundException('Réservation ou événement introuvable');
        }

        return $this->render('back/participation.html.twig', [
            'mode' => 'show',
            'event' => $event,
            'participation' => $participation,
        ]);
    }

    // ---------- ADMIN : validation des tickets (sans vérification de is_used, scannable plusieurs fois) ----------
    #[Route('/admin/validate-ticket', name: 'admin_validate_ticket')]
    #[IsGranted('ROLE_ADMIN')]
    public function validateTicket(Request $request): Response
    {
        $error = null;
        $success = null;

        if ($request->isMethod('POST')) {
            $token = $request->request->get('token');
            $participation = $this->em->getRepository(Participation::class)->findOneBy(['token' => $token]);

            if (!$participation) {
                $error = '❌ Ticket invalide – aucune réservation trouvée.';
            } elseif ($participation->getStatut() !== 'confirmed') {
                $error = '⚠️ Ce ticket n’est plus valide (annulé).';
            } else {
                // Plus aucune modification de is_used → le ticket peut être scanné plusieurs fois
                $success = sprintf(
                    '✅ Ticket valide pour %s %s – %d place(s).',
                    $participation->getPrenomParticipant(),
                    $participation->getNomParticipant(),
                    $participation->getNombrePlaces()
                );
            }
        }

        return $this->render('admin/scan_ticket.html.twig', [
            'error' => $error,
            'success' => $success,
        ]);
    }

    // ---------- QR code image generation (texte complet, pas de page web) ----------
    #[Route('/participation/qrcode/{id_participation}', name: 'participation_qrcode')]
    #[IsGranted('ROLE_USER')]
    public function qrCode(int $id_participation): Response
    {
        $participation = $this->em->getRepository(Participation::class)->find($id_participation);
        $user = $this->getUser();
        if (!$participation || $participation->getIdUser() !== $user->getId()) {
            throw $this->createNotFoundException('Participation not found');
        }
        $event = $participation->getIdEvent();
        if (!$event) {
            throw $this->createNotFoundException('Event not found');
        }

        $qrContent = $this->getQrCodeContent($participation, $event);
        $pngData = $this->qrCodeService->generateQrCode($qrContent);
        return new Response($pngData, 200, ['Content-Type' => 'image/png']);
    }
}