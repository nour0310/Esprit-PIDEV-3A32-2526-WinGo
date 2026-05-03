<?php

namespace App\Controller;

use App\Entity\ChatMessage;
use App\Entity\Event;
use App\Entity\Participation;
use App\Entity\Utilisateur;
use App\Service\StreamChatService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class ChatController extends AbstractController
{
    public function __construct(
        private StreamChatService $streamChat,
        private EntityManagerInterface $em
    ) {}

    #[Route('/event/{id}/chat', name: 'event_chat')]
    public function eventChat(Event $event): Response
    {
        /** @var Utilisateur|null $user */
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            return $this->redirectToRoute('app_login');
        }

        $participationRepository = $this->em->getRepository(Participation::class);

        // Vérifier si l'utilisateur connecté est bien participant confirmé.
        $participation = $participationRepository->findOneBy([
            'id_event' => $event,
            'id_user' => $user->getId(),
            'statut' => 'confirmed',
        ]);

        if (!$participation) {
            $this->addFlash('error', 'You must be a participant to join the chat.');

            return $this->redirectToRoute('event_show_front', [
                'id' => $event->getId_event(),
            ]);
        }

        // OPTIMISATION :
        // Avant, on récupérait les utilisateurs un par un dans une boucle avec find().
        // Cela provoquait un problème N+1 : 1 requête pour les participations + N requêtes pour les utilisateurs.
        // Maintenant, on récupère d'abord tous les ids, puis tous les utilisateurs en une seule requête.
        $participations = $participationRepository->findBy([
            'id_event' => $event,
            'statut' => 'confirmed',
        ]);

        $participantUserIds = [];

        foreach ($participations as $p) {
            $participantUserId = $p->getId_user();

            if ($participantUserId !== null) {
                $participantUserIds[] = (int) $participantUserId;
            }
        }

        $participantUserIds = array_values(array_unique($participantUserIds));

        // Les ids envoyés à Stream Chat doivent être sous forme de string.
        $participantIds = array_map('strval', $participantUserIds);

        // OPTIMISATION :
        // Une seule requête pour récupérer tous les utilisateurs participants.
        $participantUsers = [];

        if ($participantUserIds !== []) {
            $participantUsers = $this->em->getRepository(Utilisateur::class)->findBy([
                'id' => $participantUserIds,
            ]);
        }

        // Attention :
        // Cette partie peut encore prendre du temps si upsertUser() fait un appel externe à Stream.
        // Idéalement, il faut faire cet upsert au moment où l'utilisateur rejoint l'événement,
        // pas à chaque ouverture de la page chat.
        foreach ($participantUsers as $participantUser) {
            if ($participantUser instanceof Utilisateur) {
                $this->streamChat->upsertUser($participantUser);
            }
        }

        $channel = $this->streamChat->getOrCreateEventChannel($event, $participantIds);
        $token = $this->streamChat->generateToken($user);

        return $this->render('chat/event_room.html.twig', [
            'api_key' => $_ENV['STREAM_API_KEY'] ?? getenv('STREAM_API_KEY'),
            'user_token' => $token,
            'channel_type' => $channel['type'],
            'channel_id' => $channel['id'],
            'event' => $event,
        ]);
    }

    #[Route('/chat/inbox', name: 'chat_inbox')]
    public function inbox(): Response
    {
        /** @var Utilisateur|null $user */
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            return $this->redirectToRoute('app_login');
        }

        $participationRepository = $this->em->getRepository(Participation::class);

        // Récupérer les participations confirmées de l'utilisateur connecté.
        $participations = $participationRepository->findBy([
            'id_user' => $user->getId(),
            'statut' => 'confirmed',
        ]);

        $eventsById = [];

        foreach ($participations as $p) {
            $event = $p->getIdEvent();

            if ($event instanceof Event && $event->getId_event() !== null) {
                $eventsById[(int) $event->getId_event()] = $event;
            }
        }

        $events = array_values($eventsById);

        // OPTIMISATION :
        // Avant, on faisait une requête findOneBy() pour chaque event afin de récupérer le dernier message.
        // Cela provoquait aussi un problème N+1.
        // Maintenant, on récupère les derniers messages avec une seule requête DQL.
        $lastMessagesByEventId = [];

        if ($events !== []) {
            $lastMessages = $this->em->createQueryBuilder()
                ->select('m')
                ->from(ChatMessage::class, 'm')
                ->where('m.event IN (:events)')
                ->andWhere('m.createdAt = (
                    SELECT MAX(m2.createdAt)
                    FROM ' . ChatMessage::class . ' m2
                    WHERE m2.event = m.event
                )')
                ->setParameter('events', $events)
                ->getQuery()
                ->getResult();

            foreach ($lastMessages as $lastMessage) {
                if (!$lastMessage instanceof ChatMessage) {
                    continue;
                }

                $messageEvent = $lastMessage->getEvent();

                if (!$messageEvent instanceof Event || $messageEvent->getId_event() === null) {
                    continue;
                }

                $lastMessagesByEventId[(int) $messageEvent->getId_event()] = $lastMessage;
            }
        }

        $chatEvents = [];

        foreach ($events as $event) {
            $eventId = (int) $event->getId_event();
            $lastMessage = $lastMessagesByEventId[$eventId] ?? null;

            $chatEvents[] = [
                'event' => $event,
                'last_message' => $lastMessage instanceof ChatMessage ? $lastMessage->getMessage() : null,
                'last_message_time' => $lastMessage instanceof ChatMessage ? $lastMessage->getCreatedAt() : null,
            ];
        }

        return $this->render('chat/inbox.html.twig', [
            'events' => $chatEvents,
        ]);
    }
}