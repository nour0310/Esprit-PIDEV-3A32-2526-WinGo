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

    public function findLatestForRole(string $role, int $limit = 5): array
    {
        return $this->createQueryBuilder('n')
            ->andWhere('n.targetRole = :role')
            ->setParameter('role', $role)
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
}
