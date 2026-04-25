<?php

namespace App\Service;

use App\Entity\NotificationCommerce;
use Doctrine\ORM\EntityManagerInterface;
use Psr\Log\LoggerInterface;
use Symfony\Component\Mercure\HubInterface;
use Symfony\Component\Mercure\Update;

class NotificationCommerceService
{
    private const ADMIN_TOPIC = 'https://wingo.local/notifications/admin';

    public function __construct(
        private EntityManagerInterface $em,
        private HubInterface $hub,
        private LoggerInterface $logger
    ) {
    }

    public function notifyRole(
        string $role,
        string $title,
        string $message,
        string $type,
        ?string $link = null
    ): void {
        $notification = new NotificationCommerce();
        $notification->setTitle($title);
        $notification->setMessage($message);
        $notification->setType($type);
        $notification->setTargetRole($role);
        $notification->setTargetUserId(null);
        $notification->setLink($link);
        $notification->setIsRead(false);

        $this->em->persist($notification);
        $this->em->flush();

        if ($role === 'ROLE_ADMIN') {
            $payload = [
                'id' => $notification->getId(),
                'title' => $notification->getTitle(),
                'message' => $notification->getMessage(),
                'type' => $notification->getType(),
                'link' => $notification->getLink(),
                'createdAt' => $notification->getCreatedAt()?->format('Y-m-d H:i:s'),
                'targetRole' => $notification->getTargetRole(),
                'isRead' => $notification->isRead(),
            ];

            try {
                $update = new Update(
                    self::ADMIN_TOPIC,
                    json_encode($payload, JSON_UNESCAPED_UNICODE),
                    true
                );

                $this->hub->publish($update);
            } catch (\Throwable $e) {
                $this->logger->error('Publication Mercure échouée', [
                    'message' => $e->getMessage(),
                ]);
            }
        }
    }

    public function notifyUser(
        int $userId,
        string $title,
        string $message,
        string $type,
        ?string $link = null
    ): void {
        $notification = new NotificationCommerce();
        $notification->setTitle($title);
        $notification->setMessage($message);
        $notification->setType($type);
        $notification->setTargetUserId($userId);
        $notification->setTargetRole(null);
        $notification->setLink($link);
        $notification->setIsRead(false);

        $this->em->persist($notification);
        $this->em->flush();
    }
}