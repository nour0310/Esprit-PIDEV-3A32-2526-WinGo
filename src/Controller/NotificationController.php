<?php

namespace App\Controller;

use App\Repository\NotificationRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Attribute\Route;

class NotificationController extends AbstractController
{
    #[Route('/api/notifications', name: 'api_notifications', methods: ['GET'])]
    public function getNotifications(NotificationRepository $repo): JsonResponse
    {
        $user = $this->getUser();
        if (!$user || !method_exists($user, 'getId') || $user->getId() === null) {
            return $this->json(['error' => 'Non authentifié'], 401);
        }

        $userId = $user->getId();
        $notifs = $repo->findByUser($userId);
        $unreadCount = $repo->countUnread($userId);

        $items = [];
        foreach ($notifs as $n) {
            $date = $n->getDateCreation();
            $items[] = [
                'id' => $n->getId(),
                'type' => $n->getType(),
                'contenu' => $n->getContenu(),
                'lien' => $n->getLien(),
                'lu' => $n->isLu(),
                'dateCreation' => $date ? $date->format(\DateTimeInterface::ATOM) : null,
            ];
        }

        return $this->json([
            'notifications' => $items,
            'unreadCount' => $unreadCount,
        ]);
    }

    #[Route('/api/notifications/mark-read/{id}', name: 'api_notifications_mark_read', methods: ['POST'])]
    public function markAsRead(int $id, NotificationRepository $repo): JsonResponse
    {
        $user = $this->getUser();
        if (!$user || !method_exists($user, 'getId') || $user->getId() === null) {
            return $this->json(['success' => false], 401);
        }

        $notif = $repo->find($id);
        if (!$notif || $notif->getUtilisateurId() !== $user->getId()) {
            return $this->json(['success' => false], 403);
        }

        $repo->markAsRead($id);

        return $this->json(['success' => true]);
    }

    #[Route('/api/notifications/mark-all-read', name: 'api_notifications_mark_all_read', methods: ['POST'])]
    public function markAllAsRead(NotificationRepository $repo): JsonResponse
    {
        $user = $this->getUser();
        if (!$user || !method_exists($user, 'getId') || $user->getId() === null) {
            return $this->json(['success' => false], 401);
        }

        $repo->markAllAsRead($user->getId());

        return $this->json(['success' => true]);
    }
}
