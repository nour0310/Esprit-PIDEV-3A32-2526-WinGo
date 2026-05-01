<?php

namespace App\Repository;

use App\Entity\Transport;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Transport>
 */
class TransportRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Transport::class);
    }
    public function searchAndSort(?string $term, ?string $sortBy,$user = null): array
{
    $qb = $this->createQueryBuilder('t');

    
    if ($term) {
        $qb->andWhere('t.type LIKE :term OR t.depart LIKE :term OR t.arrivee LIKE :term')
           ->setParameter('term', '%' . $term . '%');
    }
    if ($user) {
        // Use the property name from your Entity (e.g., user_id)
        $qb->andWhere('t.user_id = :user')
           ->setParameter('user', $user);
    }

    // 2. SORT: Use 'tarif' instead of 'price'
    switch ($sortBy) {
        case 'price_asc':
            $qb->orderBy('t.tarif', 'ASC'); // Match your $tarif property
            break;
        case 'price_desc':
            $qb->orderBy('t.tarif', 'DESC');
            break;
        case 'cap_high':
            $qb->orderBy('t.capacite', 'DESC');
            break;
        case 'cap_low':
            $qb->orderBy('t.capacite', 'ASC');
            break;
        default:
            $qb->orderBy('t.id', 'DESC');
    }

    return $qb->getQuery()->getResult();
}
// In your ReservationTransportRepository.php
public function countBookedSeats(int $transportId, \DateTimeInterface $date): int
{
    return (int) $this->createQueryBuilder('r')
        ->select('COALESCE(SUM(r.seats), 0)')
        ->where('r.transport = :transportId')
        ->andWhere('r.dateDepart BETWEEN :start AND :end')
        ->setParameter('transportId', $transportId)
        ->setParameter('start', (clone $date)->setTime(0,0))
        ->setParameter('end', (clone $date)->setTime(23,59,59))
        ->getQuery()
        ->getSingleScalarResult();
}

    //    /**
    //     * @return Transport[] Returns an array of Transport objects
    //     */
    //    public function findByExampleField($value): array
    //    {
    //        return $this->createQueryBuilder('t')
    //            ->andWhere('t.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->orderBy('t.id', 'ASC')
    //            ->setMaxResults(10)
    //            ->getQuery()
    //            ->getResult()
    //        ;
    //    }

    //    public function findOneBySomeField($value): ?Transport
    //    {
    //        return $this->createQueryBuilder('t')
    //            ->andWhere('t.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->getQuery()
    //            ->getOneOrNullResult()
    //        ;
    //    }
}
