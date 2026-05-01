<?php

namespace App\Service;

use App\Entity\Utilisateur;
use App\Repository\CommandeRepository;
use App\Repository\PanierRepository;
use Psr\Log\LoggerInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class PanierPredictionService
{
    /**
     * Mapping vers le dataset wingo_v3.csv.
     *
     * Features envoyées à Flask :
     *   pages_visited = nb lignes distinctes dans le panier (1-10)
     *   time_on_site  = estimé selon fidélité client (30-600s)
     *   cart_value    = PRIX MOYEN PAR LIGNE TND
     *                   → total / nb_lignes, clampé [5, 500] par Flask
     *
     * Pourquoi prix moyen par ligne et non total ?
     *   1 ligne × quantité=100 × prix=100 TND → total=10 000 TND
     *   → prix moyen = 10 000 TND/ligne → clampé à 500 → abandon ✅
     *
     *   4 lignes × total=200 TND
     *   → prix moyen = 50 TND/ligne → achat probable ✅
     *
     * Statistiques du dataset wingo_v3.csv :
     *   Prix > 300 TND/ligne → 90.8% abandon
     *   Prix < 50 TND/ligne  → 8.4% abandon
     *   Accuracy modèle      → 90.2%
     */
    private const TIME_NOUVEAU_CLIENT = 343;
    private const TIME_FIDELISE       = 480;
    private const NB_ACHATS_MAX       = 5;

    public function __construct(
        private HttpClientInterface $httpClient,
        private PanierRepository    $panierRepository,
        private CommandeRepository  $commandeRepository,
        private LoggerInterface     $logger,
        private string              $flaskAiUrl
    ) {}

    /**
     * Prédit si un utilisateur va acheter son panier.
     *
     * @return array{
     *     user: Utilisateur,
     *     nb_produits: int,
     *     total: float,
     *     prediction: int,
     *     probabilite: float,
     *     label: string
     * }|null
     */
    public function predictForUser(Utilisateur $user): ?array
    {
        // 1. Récupère les lignes du panier
        $panierItems = $this->panierRepository->findActiveByUser($user->getId());

        if (empty($panierItems)) {
            return null;
        }

        // 2. Calcule les features

        // Nb lignes distinctes dans le panier (1 produit distinct = 1 ligne)
        $nbLignes = count($panierItems);

        // Valeur totale du panier en TND
        // getPrixUnitaire() retourne string (decimal Doctrine) → cast float obligatoire
        $total = array_sum(
            array_map(
                fn($item) => (float) $item->getPrixUnitaire() * $item->getQuantite(),
                $panierItems
            )
        );

        // Prix moyen par ligne TND → envoyé comme cart_value à Flask
        // Le clamping [5, 500] est fait côté Flask
        $prixMoyenParLigne = round($total / $nbLignes, 2);

        // Fidélité client → estime le time_on_site
        $nbAchatsPasses = $this->commandeRepository->countLivreesByUser($user->getId());
        $timeOnSite     = $this->estimateTimeOnSite($nbAchatsPasses);

        // 3. Appelle l'API Flask
        try {
            $response = $this->httpClient->request('POST', $this->flaskAiUrl . '/predict', [
                'headers' => ['Content-Type' => 'application/json'],
                'json'    => [
                    'nb_produits'  => $nbLignes,           // pages_visited
                    'total_panier' => $prixMoyenParLigne,  // cart_value : prix moyen/ligne TND
                    'time_on_site' => $timeOnSite,         // estimé selon fidélité client
                ],
                'timeout' => 5.0,
            ]);

            $data = $response->toArray();

            if (isset($data['error'])) {
                $this->logger->warning(
                    'PanierPrediction: Flask erreur pour user {id}: {error}',
                    ['id' => $user->getId(), 'error' => $data['error']]
                );
                return null;
            }

            return [
                'user'        => $user,
                'nb_produits' => $nbLignes,
                'total'       => round($total, 2),
                'prediction'  => (int)    $data['prediction'],
                'probabilite' => (float)  $data['probabilite'],
                'label'       => (string) $data['label'],
            ];

        } catch (\Throwable $e) {
            $this->logger->error(
                'PanierPrediction: Flask indisponible pour user {id}: {message}',
                ['id' => $user->getId(), 'message' => $e->getMessage()]
            );
            return null;
        }
    }

    /**
     * Estime le time_on_site selon la fidélité du client.
     *
     * Basé sur les statistiques du dataset wingo_v3.csv :
     *   Achat moyen   : 343s
     *   Abandon moyen : 260s
     *   Achat 75e pct : 480s
     *
     * Interpolation linéaire entre 343s (nouveau) et 480s (fidèle 5+ achats).
     */
    private function estimateTimeOnSite(int $nbAchatsPasses): int
    {
        if ($nbAchatsPasses === 0) {
            return self::TIME_NOUVEAU_CLIENT;
        }

        $ratio = min($nbAchatsPasses, self::NB_ACHATS_MAX) / self::NB_ACHATS_MAX;

        return (int) (self::TIME_NOUVEAU_CLIENT + $ratio * (self::TIME_FIDELISE - self::TIME_NOUVEAU_CLIENT));
    }
}