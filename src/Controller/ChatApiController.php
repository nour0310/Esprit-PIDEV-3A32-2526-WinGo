<?php

namespace App\Controller;

use App\Entity\ChatMessage;
use App\Entity\Event;
use App\Entity\Participation;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;


class ChatApiController extends AbstractController
{
    #[Route('/api/chat/{id}/messages', name: 'api_chat_messages', methods: ['GET'])]
    #[IsGranted('ROLE_USER')]
    public function getMessages(Event $event, EntityManagerInterface $em): JsonResponse
    {
        $user = $this->getUser();
        // Verify participation
        $participation = $em->getRepository(Participation::class)->findOneBy([
            'id_event' => $event,
            'id_user' => $user->getId(),
            'statut' => 'confirmed'
        ]);
        if (!$participation) {
            return $this->json(['error' => 'Not a participant'], 403);
        }

        $messages = $em->getRepository(ChatMessage::class)->findBy(
            ['event' => $event],
            ['createdAt' => 'ASC'],
            50
        );

        $data = [];
        foreach ($messages as $msg) {
            $data[] = [
                'id' => $msg->getId(),
                'user_id' => $msg->getUser()->getId(),
                'user_name' => $msg->getUser()->getUserIdentifier(),
                'text' => $msg->getMessage(),
                'created_at' => $msg->getCreatedAt()->format('Y-m-d H:i:s'),
            ];
        }
        return $this->json(['messages' => $data]);
    }

    #[Route('/api/chat/{id}/send', name: 'api_chat_send', methods: ['POST'])]
    #[IsGranted('ROLE_USER')]
    public function sendMessage(Event $event, Request $request, EntityManagerInterface $em): JsonResponse
    {
        $user = $this->getUser();
        $participation = $em->getRepository(Participation::class)->findOneBy([
            'id_event' => $event,
            'id_user' => $user->getId(),
            'statut' => 'confirmed'
        ]);
        if (!$participation) {
            return $this->json(['error' => 'Not a participant'], 403);
        }

        $text = trim($request->request->get('message'));
        if (empty($text)) {
            return $this->json(['error' => 'Message cannot be empty'], 400);
        }

        $message = new ChatMessage();
        $message->setEvent($event);
        $message->setUser($user);
        $message->setMessage($text);
        $em->persist($message);
        $em->flush();

        return $this->json(['success' => true]);
    }
}