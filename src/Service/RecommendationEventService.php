<?php

namespace App\Service;

use App\Entity\Event;
use Doctrine\ORM\EntityManagerInterface;

class RecommendationEventService
{
    public function __construct(
        private EntityManagerInterface $em,
    ) {}

    /**
     * Retourne les événements recommandés.
     *
     * @return Event[]
     */
    public function getRecommendedEvents(int $limit = 6): array
    {
        return $this->computeRecommendations($limit);
    }

    /**
     * @return Event[]
     */
    private function computeRecommendations(int $limit): array
    {
        $today = new \DateTime();
        $today->setTime(0, 0, 0);

        $maxDate = clone $today;
        $maxDate->modify('+7 days');

        $qb = $this->em->getRepository(Event::class)->createQueryBuilder('e')
            ->addSelect('(e.capacity - e.available_places) AS HIDDEN popularity')
            ->where('e.date_event >= :today')
            ->andWhere('e.date_event <= :maxDate')
            ->setParameter('today', $today)
            ->setParameter('maxDate', $maxDate)
            ->orderBy('popularity', 'DESC')
            ->addOrderBy('e.date_event', 'ASC')
            ->setMaxResults($limit);

        return $qb->getQuery()->getResult();
    }
}