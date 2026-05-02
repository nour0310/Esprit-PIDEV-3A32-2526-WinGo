<?php

namespace App\Service;

use App\Entity\Produit;

class MerchantDashboardService
{
    /**
     * @param Produit[] $produits
     *
     * @return array{
     *     totalProduits: int,
     *     produitsEnStock: int,
     *     produitsRupture: int,
     *     stockFaible: int,
     *     valeurStock: float,
     *     nombreCategories: int,
     *     dernierProduit: Produit|null
     * }
     */
    public function buildStats(array $produits): array
    {
        $totalProduits = count($produits);
        $produitsEnStock = 0;
        $produitsRupture = 0;
        $stockFaible = 0;
        $valeurStock = 0.0;
        $categories = [];
        $dernierProduit = null;

        foreach ($produits as $produit) {
            $stock = (int) $produit->getStock();
            $prix = (float) $produit->getPrix();

            if ($stock > 0) {
                $produitsEnStock++;
            } else {
                $produitsRupture++;
            }

            if ($stock > 0 && $stock <= 5) {
                $stockFaible++;
            }

            $valeurStock += $prix * $stock;

            $categorie = $produit->getCategorie();

            if ($categorie !== null && trim((string) $categorie) !== '') {
                $categories[] = trim((string) $categorie);
            }

            $dateAjout = $produit->getDateAjout();

            if (
                $dateAjout !== null &&
                (
                    $dernierProduit === null ||
                    $dateAjout > $dernierProduit->getDateAjout()
                )
            ) {
                $dernierProduit = $produit;
            }
        }

        return [
            'totalProduits' => $totalProduits,
            'produitsEnStock' => $produitsEnStock,
            'produitsRupture' => $produitsRupture,
            'stockFaible' => $stockFaible,
            'valeurStock' => $valeurStock,
            'nombreCategories' => count(array_unique($categories)),
            'dernierProduit' => $dernierProduit,
        ];
    }
}