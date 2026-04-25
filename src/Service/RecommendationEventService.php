<?php

namespace App\Service;

use App\Entity\Event;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Contracts\Cache\CacheInterface;

class RecommendationEventService
{
    public function __construct(
        private EntityManagerInterface $em,
        private CacheInterface $cache
    ) {}

    /**
     * Retourne les événements recommandés (max 6).
     * Critères :
     * - Date dans les 7 prochains jours (à partir d'aujourd'hui)
     * - Trié par popularité décroissante (places réservées)
     * - Puis par date la plus proche
     */
    public function getRecommendedEvents(int $limit = 6): array
    {
        // Désactivation temporaire du cache pour déboguer
        // À réactiver après vérification du bon fonctionnement
        return $this->computeRecommendations($limit);

        /* Code avec cache (à décommenter une fois que tout fonctionne)
        $cacheKey = 'recommendation_event_popular_7days';
        return $this->cache->get($cacheKey, function () use ($limit) {
            return $this->computeRecommendations($limit);
        }, 600); // 10 minutes
        */
    }

    private function computeRecommendations(int $limit): array
    {
        $today = new \DateTime();
        $today->setTime(0, 0, 0);
        
        // Date limite : aujourd'hui + 7 jours
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