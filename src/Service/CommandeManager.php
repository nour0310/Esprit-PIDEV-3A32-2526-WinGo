<?php

namespace App\Service;

use App\Entity\Commande;

class CommandeManager
{
    private const VALID_STATUSES = ['panier', 'en_cours', 'livree', 'annulee'];

    /**
     * Valide les règles métier d'une commande.
     *
     * Règles :
     * 1. Le total doit être positif ou nul
     * 2. Le statut doit être valide (panier, en_cours, livree, annulee)
     * 3. Une commande annulée doit avoir une cause d'annulation
     */
    public function validate(Commande $commande): bool
    {
        // Règle 1 : Le total doit être positif ou nul
        if (floatval($commande->getTotal()) < 0) {
            throw new \InvalidArgumentException('Le total ne peut pas être négatif');
        }

        // Règle 2 : Le statut doit être valide
        if (!in_array($commande->getStatus(), self::VALID_STATUSES)) {
            throw new \InvalidArgumentException('Le statut de la commande n\'est pas valide');
        }

        // Règle 3 : Une commande annulée doit avoir une cause d'annulation
        if ($commande->getStatus() === 'annulee' && empty($commande->getCauseAnnulation())) {
            throw new \InvalidArgumentException('Une commande annulée doit avoir une cause d\'annulation');
        }

        return true;
    }
}
