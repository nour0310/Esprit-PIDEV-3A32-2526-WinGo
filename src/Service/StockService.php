<?php

namespace App\Service;

use App\Entity\Panier;
use App\Repository\ProduitRepository;

class StockService
{
    public function __construct(
        private ProduitRepository $produitRepository,
    ) {}

    /**
     * @param Panier[] $lignesPanier
     */
    public function verifierStock(array $lignesPanier): ?string
    {
        foreach ($lignesPanier as $ligne) {
            $produit = $this->produitRepository->find($ligne->getIdProduit());

            if (!$produit) {
                continue;
            }

            if ($produit->getStock() < $ligne->getQuantite()) {
                return $produit->getNom();
            }
        }

        return null;
    }

    /**
     * @param Panier[] $lignesPanier
     */
    public function decrementerStock(array $lignesPanier): void
    {
        foreach ($lignesPanier as $ligne) {
            $produit = $this->produitRepository->find($ligne->getIdProduit());

            if (!$produit) {
                continue;
            }

            $nouveauStock = max(0, $produit->getStock() - $ligne->getQuantite());
            $produit->setStock($nouveauStock);
        }
    }
}