<?php

namespace App\Repository;

use App\Entity\Panier;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Panier>
 */
class PanierRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Panier::class);
    }

    /**
     * @return Panier[]
     */
    public function findActiveByUser(int $idUser): array
    {
        return $this->createQueryBuilder('p')
            ->andWhere('p.idUser = :idUser')
            ->setParameter('idUser', $idUser)
            ->orderBy('p.dateAjout', 'DESC')
            ->getQuery()
            ->getResult();
    }
}