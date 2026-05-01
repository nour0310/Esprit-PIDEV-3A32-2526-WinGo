<?php

namespace App\Service;

use App\Entity\Reclamation;

class ReclamationManager
{
    private const VALID_STATUTS = ['En attente', 'En cours', 'Résolue', 'Rejetée'];

    /**
     * Valide les règles métier d'une réclamation.
     *
     * Règles :
     * 1. Le sujet est obligatoire
     * 2. La description est obligatoire
     * 3. Le statut doit être valide
     */
    public function validate(Reclamation $reclamation): bool
    {
        // Règle 1 : Le sujet est obligatoire
        if (empty($reclamation->getSujet())) {
            throw new \InvalidArgumentException('Le sujet est obligatoire');
        }

        // Règle 2 : La description est obligatoire
        if (empty($reclamation->getDescription())) {
            throw new \InvalidArgumentException('La description est obligatoire');
        }

        // Règle 3 : Le statut doit être valide
        if (!in_array($reclamation->getStatut(), self::VALID_STATUTS)) {
            throw new \InvalidArgumentException('Le statut n\'est pas valide');
        }

        return true;
    }
}
