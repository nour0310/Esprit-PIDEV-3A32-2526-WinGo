<?php

namespace App\Repository;

use App\Entity\Promotion;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Promotion>
 */
class PromotionRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Promotion::class);
    }

    /**
     * Valide un code promo : non utilisé, non expiré, appartient au user.
     */
    public function findValidByCodeAndUser(string $code, int $idUser): ?Promotion
    {
        return $this->createQueryBuilder('p')
            ->andWhere('p.code = :code')
            ->andWhere('p.idUser = :idUser')
            ->andWhere('p.isUsed = false')
            ->andWhere('p.dateExpiration > :now')
            ->setParameter('code', $code)
            ->setParameter('idUser', $idUser)
            ->setParameter('now', new \DateTime())
            ->getQuery()
            ->getOneOrNullResult();
    }
}