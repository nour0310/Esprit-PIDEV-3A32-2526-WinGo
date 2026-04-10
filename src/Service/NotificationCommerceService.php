<?php

namespace App\Service;

use App\Entity\NotificationCommerce;
use Doctrine\ORM\EntityManagerInterface;

class NotificationCommerceService
{
    public function __construct(
        private EntityManagerInterface $em
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