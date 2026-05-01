<?php

namespace App\Service;

use App\Entity\Promotion;
use App\Entity\Reward;
use App\Repository\RewardRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Lock\LockFactory;

class JeuService
{
    // Probabilités sur 100 — total doit = 100
    private const PROBABILITES = [
        Reward::REWARD_RIEN               => 0,
        Reward::REWARD_REDUCTION_POURCENT => 20,
        Reward::REWARD_REMISE_FIXE        => 70,
        Reward::REWARD_LIVRAISON_GRATUITE => 10,
    ];

    public function __construct(
        private EntityManagerInterface $em,
        private RewardRepository       $rewardRepository,
        private LockFactory            $lockFactory,
    ) {}

    /**
     * Retourne le reward du jour si le user a déjà joué, null sinon.
     * Le Twig utilise knp-time-bundle sur playedAt pour afficher
     * "Revenez dans X heures".
     */
    public function findTodayReward(int $idUser): ?Reward
    {
        return $this->rewardRepository->findTodayByUser($idUser);
    }

    /**
     * Lance le jeu.
     * symfony/lock évite qu'un user joue deux fois en même temps (double-clic).
     *
     * @throws \RuntimeException si déjà joué aujourd'hui
     */
    public function play(int $idUser, string $typeJeu): Reward
    {
        // symfony/lock : verrou unique par user, expire en 10 secondes
        $lock = $this->lockFactory->createLock('jeu_user_' . $idUser, ttl: 30);

        if (!$lock->acquire()) {
            throw new \RuntimeException('Une partie est déjà en cours.');
        }

        try {
            // Vérification après acquisition du verrou
            if ($this->findTodayReward($idUser) !== null) {
                throw new \RuntimeException('Vous avez déjà joué aujourd\'hui.');
            }

            $rewardType = $this->tirerAuSort();

            $reward = new Reward();
            $reward->setIdUser($idUser);
            $reward->setTypeJeu($typeJeu);
            $reward->setRewardType($rewardType);

            // Génère un code promo si le client a gagné
            if ($rewardType !== Reward::REWARD_RIEN) {
                $promotion = $this->creerPromotion($idUser, $rewardType);
                $reward->setPromotion($promotion);
            }

            $this->em->persist($reward);
            $this->em->flush();

            return $reward;

        } finally {
            $lock->release();
        }
    }

    /**
     * Crée et sauvegarde une Promotion pour le user.
     * Code format : WINGO-XXXX (PHP natif, pas de bundle externe)
     */
    private function creerPromotion(int $idUser, string $rewardType): Promotion
    {
        $promotion = new Promotion();
        $promotion->setCode('WINGO-' . strtoupper(substr(bin2hex(random_bytes(4)), 0, 4)));
        $promotion->setType($rewardType);
        $promotion->setIdUser($idUser);

        $this->em->persist($promotion);

        return $promotion;
    }

    /**
     * Tirage au sort pondéré selon PROBABILITES.
     */
    private function tirerAuSort(): string
    {
        assert(array_sum(self::PROBABILITES) === 100, 'Les probabilités doivent totaliser 100');
        $random = random_int(1, 100);
        $cumul  = 0;

        foreach (self::PROBABILITES as $rewardType => $probabilite) {
            $cumul += $probabilite;
            if ($random <= $cumul) {
                return $rewardType;
            }
        }

        return Reward::REWARD_RIEN;
    }
}