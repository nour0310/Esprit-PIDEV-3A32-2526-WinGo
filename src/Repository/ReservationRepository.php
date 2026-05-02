<?php

namespace App\Repository;

use App\Entity\Reservation;
use App\Entity\Utilisateur;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Reservation>
 */
class ReservationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Reservation::class);
    }

    /**
     * @return Reservation[]
     */
    public function searchAndSortReservations(?string $search, ?string $sort, ?Utilisateur $user = null): array
    {
        $qb = $this->createQueryBuilder('r')
            ->leftJoin('r.user_id', 'u')
            ->addSelect('u');

        if ($search) {
            $qb->andWhere('r.statut LIKE :term OR u.nom LIKE :term OR u.prenom LIKE :term')
                ->setParameter('term', '%' . $search . '%');
        }

        if ($user !== null) {
            $qb->andWhere('r.user_id = :user')
                ->setParameter('user', $user);
        }

        if ($sort === 'date_asc') {
            $qb->orderBy('r.date', 'ASC');
        } elseif ($sort === 'user_asc') {
            $qb->orderBy('u.nom', 'ASC');
        } else {
            $qb->orderBy('r.id', 'DESC');
        }

        return $qb->getQuery()->getResult();
    }

    public function countUserReservationsThisYear(Utilisateur $user): int
    {
        $startOfYear = new \DateTime(date('Y') . '-01-01 00:00:00');
        $endOfYear = new \DateTime(date('Y') . '-12-31 23:59:59');

        return (int) $this->createQueryBuilder('r')
            ->select('COUNT(r.id)')
            ->where('r.user_id = :user')
            ->andWhere('r.date >= :startOfYear')
            ->andWhere('r.date <= :endOfYear')
            ->setParameter('user', $user)
            ->setParameter('startOfYear', $startOfYear)
            ->setParameter('endOfYear', $endOfYear)
            ->getQuery()
            ->getSingleScalarResult();
    }
}