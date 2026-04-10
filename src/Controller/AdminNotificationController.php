<?php

namespace App\Controller;

use App\Repository\NotificationCommerceRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

final class AdminNotificationController extends AbstractController
{
    #[IsGranted('ROLE_ADMIN')]
    #[Route('/admin/_notifications/bell', name: 'admin_notifications_bell', methods: ['GET'])]
    public function bell(NotificationCommerceRepository $notificationCommerceRepo): Response
    {
       $adminNotifications = $notificationCommerceRepo->findUnreadForRole('ROLE_ADMIN', 5);
        $adminUnreadCount = $notificationCommerceRepo->countUnreadForRole('ROLE_ADMIN');

        $adminUnreadCount = $notificationCommerceRepo->count([
            'targetRole' => 'ROLE_ADMIN',
            'isRead' => false,
        ]);

        return $this->render('admin/_notification_bell.html.twig', [
            'adminNotifications' => $adminNotifications,
            'adminUnreadCount' => $adminUnreadCount,
        ]);
    }
}