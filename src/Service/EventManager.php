<?php

namespace App\Service;

use App\Entity\Event;

class EventManager
{
    /**
     * Valide les règles métier d'un événement.
     *
     * Règles :
     * 1. Le titre est obligatoire et doit contenir au moins 3 caractères
     * 2. La capacité doit être positive
     * 3. Le prix doit être positif ou nul
     * 4. Les places disponibles ne doivent pas dépasser la capacité
     */
    public function validate(Event $event): bool
    {
        // Règle 1 : Le titre est obligatoire et minimum 3 caractères
        if (empty($event->getTitle())) {
            throw new \InvalidArgumentException('Le titre est obligatoire');
        }

        if (strlen($event->getTitle()) < 3) {
            throw new \InvalidArgumentException('Le titre doit contenir au moins 3 caractères');
        }

        // Règle 2 : La capacité doit être positive
        if ($event->getCapacity() <= 0) {
            throw new \InvalidArgumentException('La capacité doit être positive');
        }

        // Règle 3 : Le prix doit être positif ou nul
        if ($event->getPrice() < 0) {
            throw new \InvalidArgumentException('Le prix doit être positif ou nul');
        }

        // Règle 4 : Les places disponibles ne doivent pas dépasser la capacité
        if ($event->getAvailable_places() > $event->getCapacity()) {
            throw new \InvalidArgumentException('Les places disponibles ne peuvent pas dépasser la capacité');
        }

        return true;
    }
}
