<?php

namespace App\Service;

use App\Entity\Notification;
use Doctrine\ORM\EntityManagerInterface;

class NotificationService
{
    public function __construct(private readonly EntityManagerInterface $em)
    {
    }

    public function create(int $destinataireId, int $emetteurId, string $type, string $contenu, ?string $lien = null): void
    {
        $notif = new Notification();
        $notif->setUtilisateurId($destinataireId);
        $notif->setEmetteurId($emetteurId);
        $notif->setType($type);
        $notif->setContenu($contenu);
        $notif->setLien($lien);
        $this->em->persist($notif);
        $this->em->flush();
    }
}
