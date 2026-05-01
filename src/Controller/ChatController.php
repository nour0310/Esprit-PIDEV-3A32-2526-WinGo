<?php

namespace App\Controller;

use App\Entity\Event;
use App\Entity\Utilisateur;
use App\Entity\Participation;
use App\Service\StreamChatService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Entity\ChatMessage;


class ChatController extends AbstractController
{
    public function __construct(
        private StreamChatService $streamChat,
        private EntityManagerInterface $em
    ) {}

    #[Route('/event/{id}/chat', name: 'event_chat')]
    public function eventChat(Event $event): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        // Check if user is a confirmed participant
        $participation = $this->em->getRepository(Participation::class)->findOneBy([
            'id_event' => $event,
            'id_user' => $user->getId(),
            'statut' => 'confirmed'
        ]);

        if (!$participation) {
            $this->addFlash('error', 'You must be a participant to join the chat.');
            return $this->redirectToRoute('event_show_front', ['id' => $event->getId_event()]);
        }

        // Get all participant IDs
        $participations = $this->em->getRepository(Participation::class)->findBy([
            'id_event' => $event,
            'statut' => 'confirmed'
        ]);
        
        $participantIds = [];
        foreach ($participations as $p) {
            $participantUser = $this->em->getRepository(Utilisateur::class)->find($p->getId_user());
            if ($participantUser) {
                $this->streamChat->upsertUser($participantUser);
                $participantIds[] = (string) $participantUser->getId();
            }
        }

        // Get or create the event channel
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
public function inbox(EntityManagerInterface $em): Response
{
    $user = $this->getUser();
    if (!$user) {
        return $this->redirectToRoute('app_login');
    }

    // Get all events where the user is a confirmed participant
    $participations = $em->getRepository(Participation::class)->findBy([
        'id_user' => $user->getId(),
        'statut' => 'confirmed'
    ]);

    $events = [];
    foreach ($participations as $p) {
        $event = $p->getIdEvent();
        if ($event) {
            // Get last message for each event (optional)
            $lastMessage = $em->getRepository(ChatMessage::class)->findOneBy(
                ['event' => $event],
                ['createdAt' => 'DESC']
            );
            $events[] = [
                'event' => $event,
                'last_message' => $lastMessage ? $lastMessage->getMessage() : null,
                'last_message_time' => $lastMessage ? $lastMessage->getCreatedAt() : null,
            ];
        }
    }

    return $this->render('chat/inbox.html.twig', [
        'events' => $events,
    ]);
}
}