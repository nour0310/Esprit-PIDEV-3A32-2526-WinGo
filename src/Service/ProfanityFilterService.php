<?php

namespace App\Service;

use ProfanityFilter\ProfanityFilter;
use ProfanityFilter\ProfanityLevel;

/**
 * Service encapsulant le bundle devtrope/profanity-filter
 * pour la détection de mots inappropriés dans les commentaires.
 */
class ProfanityFilterService
{
    private ProfanityFilter $filter;

    public function __construct()
    {
        // Filtre français avec sensibilité HIGH
        $this->filter = new ProfanityFilter(ProfanityLevel::HIGH, 'fr');
    }

    /**
     * Vérifie si un texte contient des mots inappropriés.
     */
    public function containsProfanity(string $text): bool
    {
        return $this->filter->containsProfanity($text);
    }

    /**
     * Nettoie un texte en remplaçant les mots inappropriés par des astérisques.
     */
    public function clean(string $text): string
    {
        return $this->filter->clean($text);
    }
}
