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
     */
    private const TIME_NOUVEAU_CLIENT = 343;
    private const TIME_FIDELISE = 480;
    private const NB_ACHATS_MAX = 5;

    public function __construct(
        private HttpClientInterface $httpClient,
        private PanierRepository $panierRepository,
        private CommandeRepository $commandeRepository,
        private LoggerInterface $logger,
        private string $flaskAiUrl
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
        $userId = $user->getId();

        if ($userId === null) {
            return null;
        }

        $panierItems = $this->panierRepository->findActiveByUser($userId);

        if (empty($panierItems)) {
            return null;
        }

        $nbLignes = count($panierItems);

        $total = array_sum(
            array_map(
                fn ($item) => (float) $item->getPrixUnitaire() * $item->getQuantite(),
                $panierItems
            )
        );

        $prixMoyenParLigne = round($total / $nbLignes, 2);

        $nbAchatsPasses = $this->commandeRepository->countLivreesByUser($userId);
        $timeOnSite = $this->estimateTimeOnSite($nbAchatsPasses);

        try {
            $response = $this->httpClient->request('POST', $this->flaskAiUrl . '/predict', [
                'headers' => [
                    'Content-Type' => 'application/json',
                ],
                'json' => [
                    'nb_produits' => $nbLignes,
                    'total_panier' => $prixMoyenParLigne,
                    'time_on_site' => $timeOnSite,
                ],
                'timeout' => 5.0,
            ]);

            $data = $response->toArray();

            if (isset($data['error'])) {
                $this->logger->warning(
                    'PanierPrediction: Flask erreur pour user {id}: {error}',
                    [
                        'id' => $userId,
                        'error' => $data['error'],
                    ]
                );

                return null;
            }

            return [
                'user' => $user,
                'nb_produits' => $nbLignes,
                'total' => round($total, 2),
                'prediction' => (int) $data['prediction'],
                'probabilite' => (float) $data['probabilite'],
                'label' => (string) $data['label'],
            ];
        } catch (\Throwable $e) {
            $this->logger->error(
                'PanierPrediction: Flask indisponible pour user {id}: {message}',
                [
                    'id' => $userId,
                    'message' => $e->getMessage(),
                ]
            );

            return null;
        }
    }

    private function estimateTimeOnSite(int $nbAchatsPasses): int
    {
        if ($nbAchatsPasses === 0) {
            return self::TIME_NOUVEAU_CLIENT;
        }

        $ratio = min($nbAchatsPasses, self::NB_ACHATS_MAX) / self::NB_ACHATS_MAX;

        return (int) (self::TIME_NOUVEAU_CLIENT + $ratio * (self::TIME_FIDELISE - self::TIME_NOUVEAU_CLIENT));
    }
}