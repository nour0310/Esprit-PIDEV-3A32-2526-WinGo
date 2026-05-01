<?php

namespace App\Service;

use App\Entity\Reservation;

class ReservationManager
{
    /**
     * Valide les règles métier d'une réservation.
     *
     * Règles :
     * 1. Le nom d'utilisateur est obligatoire
     * 2. La date de réservation ne peut pas être dans le passé
     * 3. Le prix doit être positif ou nul
     */
    public function validate(Reservation $reservation): bool
    {
        // Règle 1 : Le nom d'utilisateur est obligatoire
        if (empty($reservation->getUser())) {
            throw new \InvalidArgumentException('Le nom d\'utilisateur est obligatoire');
        }

        // Règle 2 : La date de réservation ne peut pas être dans le passé
        $now = new \DateTime();
        $now->setTime(0, 0, 0);
        if ($reservation->getDate() < $now) {
            throw new \InvalidArgumentException('La date de réservation ne peut pas être dans le passé');
        }

        // Règle 3 : Le prix doit être positif ou nul
        if ($reservation->getPrice() !== null && $reservation->getPrice() < 0) {
            throw new \InvalidArgumentException('Le prix ne peut pas être négatif');
        }

        return true;
    }
}
