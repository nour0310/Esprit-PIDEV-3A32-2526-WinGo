<?php

namespace App\Repository;

use App\Entity\Reservation;
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
    public function searchAndSortReservations(?string $search, ?string $sort,$user = null)
{
    $qb = $this->createQueryBuilder('r')
        ->leftJoin('r.user_id', 'u') // Join the relation
        ->addSelect('u');           // Load user data to avoid extra queries

    if ($search) {
        $qb->andWhere('r.statut LIKE :term OR u.nom LIKE :term OR u.prenom LIKE :term')
           ->setParameter('term', '%' . $search . '%');
    }
    if ($user) {
        $qb->andWhere('r.user_id = :user')
           ->setParameter('user', $user);
    }

    // Sorting logic
    if ($sort === 'date_asc') {
        $qb->orderBy('r.date', 'ASC');
    } elseif ($sort === 'user_asc') {
        $qb->orderBy('u.nom', 'ASC'); // Now you can sort by User Name!
    } else {
        $qb->orderBy('r.id', 'DESC');
    }

    return $qb->getQuery()->getResult();
}
public function countUserReservationsThisYear($user)
{
    // 1. On définit le 1er janvier de l'année en cours
    $startOfYear = new \DateTime(date('Y') . '-01-01 00:00:00');
    
    // 2. On définit le 31 décembre de l'année en cours
    $endOfYear = new \DateTime(date('Y') . '-12-31 23:59:59');

    return $this->createQueryBuilder('r')
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

    //    /**
    //     * @return Reservation[] Returns an array of Reservation objects
    //     */
    //    public function findByExampleField($value): array
    //    {
    //        return $this->createQueryBuilder('r')
    //            ->andWhere('r.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->orderBy('r.id', 'ASC')
    //            ->setMaxResults(10)
    //            ->getQuery()
    //            ->getResult()
    //        ;
    //    }

    //    public function findOneBySomeField($value): ?Reservation
    //    {
    //        return $this->createQueryBuilder('r')
    //            ->andWhere('r.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->getQuery()
    //            ->getOneOrNullResult()
    //        ;
    //    }
}
