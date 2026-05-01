<?php

namespace App\Repository;

use App\Entity\Notification;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class NotificationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Notification::class);
    }

    /**
     * @return Notification[]
     */
    public function findByUser(int $userId): array
    {
        return $this->createQueryBuilder('n')
            ->andWhere('n.utilisateurId = :uid')
            ->setParameter('uid', $userId)
            ->orderBy('n.dateCreation', 'DESC')
            ->setMaxResults(50)
            ->getQuery()
            ->getResult();
    }

    public function countUnread(int $userId): int
    {
        return (int) $this->createQueryBuilder('n')
            ->select('COUNT(n.id)')
            ->andWhere('n.utilisateurId = :uid')
            ->andWhere('n.lu = :lu')
            ->setParameter('uid', $userId)
            ->setParameter('lu', false)
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function markAsRead(int $notificationId): void
    {
        $this->createQueryBuilder('n')
            ->update()
            ->set('n.lu', ':lu')
            ->where('n.id = :id')
            ->setParameter('lu', true)
            ->setParameter('id', $notificationId)
            ->getQuery()
            ->execute();
    }

    public function markAllAsRead(int $userId): void
    {
        $this->createQueryBuilder('n')
            ->update()
            ->set('n.lu', ':lu')
            ->where('n.utilisateurId = :uid')
            ->andWhere('n.lu = :wasUnread')
            ->setParameter('lu', true)
            ->setParameter('uid', $userId)
            ->setParameter('wasUnread', false)
            ->getQuery()
            ->execute();
    }
}
