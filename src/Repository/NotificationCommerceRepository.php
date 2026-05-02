<?php

namespace App\Repository;

use App\Entity\NotificationCommerce;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<NotificationCommerce>
 */
class NotificationCommerceRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, NotificationCommerce::class);
    }

    /**
    * @return NotificationCommerce[]
    */
    public function findUnreadForRole(string $role, int $limit = 5): array
    {
        return $this->createQueryBuilder('n')
            ->andWhere('n.targetRole = :role')
            ->andWhere('n.isRead = :isRead')
            ->setParameter('role', $role)
            ->setParameter('isRead', false)
            ->orderBy('n.createdAt', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    public function countUnreadForRole(string $role): int
    {
        return (int) $this->createQueryBuilder('n')
            ->select('COUNT(n.id)')
            ->andWhere('n.targetRole = :role')
            ->andWhere('n.isRead = :isRead')
            ->setParameter('role', $role)
            ->setParameter('isRead', false)
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function markMerchantRequestNotificationsAsReadForAdmin(): void
    {
        $this->createQueryBuilder('n')
            ->update()
            ->set('n.isRead', ':isRead')
            ->where('n.targetRole = :role')
            ->andWhere('n.type = :type')
            ->andWhere('n.isRead = :current')
            ->setParameter('isRead', true)
            ->setParameter('role', 'ROLE_ADMIN')
            ->setParameter('type', 'merchant_request')
            ->setParameter('current', false)
            ->getQuery()
            ->execute();
    }
}