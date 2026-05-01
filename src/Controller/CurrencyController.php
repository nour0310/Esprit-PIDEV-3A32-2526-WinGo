<?php

namespace App\Controller;

use App\Repository\ProduitRepository;
use App\Service\CurrencyConverterService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/api/currency', name: 'api_currency_')]
final class CurrencyController extends AbstractController
{
    public function __construct(
        private readonly CurrencyConverterService $currencyConverter,
        private readonly ProduitRepository        $produitRepository
    ) {}

    /**
     * GET /api/currency/convert/{id}/{currency}
     * Convertit le prix d'un produit TND → devise cible.
     * Accessible uniquement aux utilisateurs connectés.
     */
    #[IsGranted('ROLE_USER')]
    #[Route('/convert/{id}/{currency}', name: 'convert', methods: ['GET'],
        requirements: ['id' => '\d+', 'currency' => '[A-Za-z]{3}']
    )]
    public function convert(int $id, string $currency): JsonResponse
    {
        // 1. Valider la devise
        $currency = strtoupper($currency);

        if (!array_key_exists($currency, CurrencyConverterService::SUPPORTED_CURRENCIES)) {
            return $this->json([
                'success' => false,
                'message' => sprintf(
                    'Devise "%s" non supportée. Devises acceptées : %s',
                    $currency,
                    implode(', ', array_keys(CurrencyConverterService::SUPPORTED_CURRENCIES))
                ),
            ], Response::HTTP_BAD_REQUEST);
        }

        // 2. Récupérer le produit
        $produit = $this->produitRepository->find($id);

        if (!$produit) {
            return $this->json([
                'success' => false,
                'message' => "Produit #$id introuvable.",
            ], Response::HTTP_NOT_FOUND);
        }

        // 3. Convertir
        try {
            $result = $this->currencyConverter->convert(
                (float) $produit->getPrix(),
                $currency
            );
        } catch (\InvalidArgumentException $e) {
            return $this->json([
                'success' => false,
                'message' => $e->getMessage(),
            ], Response::HTTP_BAD_REQUEST);
        } catch (\RuntimeException $e) {
            return $this->json([
                'success' => false,
                'message' => 'Erreur lors de la conversion : ' . $e->getMessage(),
            ], Response::HTTP_INTERNAL_SERVER_ERROR);
        }

        // 4. Réponse JSON structurée
        return $this->json([
            'success'       => true,
            'produit_id'    => $produit->getId(),
            'produit_nom'   => $produit->getNom(),
            'prix_original' => $result['original'],
            'prix_converti' => $result['converted'],
            'devise'        => $result['currency'],
            'symbole'       => $result['symbol'],
            'label'         => $result['label'],
            'taux'          => $result['rate'],
        ]);
    }

    /**
     * GET /api/currency/currencies
     * Liste toutes les devises supportées.
     */
    #[Route('/currencies', name: 'list', methods: ['GET'])]
    public function currencies(): JsonResponse
    {
        return $this->json([
            'success'    => true,
            'currencies' => $this->currencyConverter->getSupportedCurrencies(),
        ]);
    }
}