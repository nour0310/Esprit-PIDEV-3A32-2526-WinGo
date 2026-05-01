<?php

namespace App\Service;

use App\Entity\Produit;

class ProduitManager
{
    /**
     * Valide les règles métier d'un produit.
     *
     * Règles :
     * 1. Le nom du produit est obligatoire
     * 2. Le prix doit être supérieur à zéro
     * 3. Le stock ne peut pas être négatif
     */
    public function validate(Produit $produit): bool
    {
        // Règle 1 : Le nom du produit est obligatoire
        if (empty($produit->getNom())) {
            throw new \InvalidArgumentException('Le nom du produit est obligatoire');
        }

        // Règle 2 : Le prix doit être supérieur à zéro
        if (floatval($produit->getPrix()) <= 0) {
            throw new \InvalidArgumentException('Le prix doit être supérieur à zéro');
        }

        // Règle 3 : Le stock ne peut pas être négatif
        if ($produit->getStock() < 0) {
            throw new \InvalidArgumentException('Le stock ne peut pas être négatif');
        }

        return true;
    }
}
