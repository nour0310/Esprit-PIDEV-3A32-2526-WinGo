<?php

namespace App\Repository;

use App\Entity\Reward;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Reward>
 */
class RewardRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Reward::class);
    }

    /**
     * Retourne le reward du jour pour un user, null si pas encore joué.
     */
    public function findTodayByUser(int $idUser): ?Reward
    {
        return $this->createQueryBuilder('r')
            ->andWhere('r.idUser = :idUser')
            ->andWhere('r.playedAt >= :today')
            ->setParameter('idUser', $idUser)
            ->setParameter('today', new \DateTime('today midnight'))
            ->orderBy('r.playedAt', 'DESC')
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();
    }
}