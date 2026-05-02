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
    private ProfanityFilter $filterFr;
    private ProfanityFilter $filterEn;

    public function __construct()
    {
        // Filtre français + anglais avec sensibilité HIGH
        $this->filterFr = new ProfanityFilter(ProfanityLevel::HIGH, 'fr');
        $this->filterEn = new ProfanityFilter(ProfanityLevel::HIGH, 'en');

        // Mots supplémentaires pour la démo
        $demoWords = ['stupid', 'idiot', 'nul', 'bete', 'bête', 'dumb', 'loser', 'moron', 'imbecile', 'imbécile', 'crétin', 'cretin', 'débile', 'debile'];
        foreach ($demoWords as $word) {
            $this->filterFr->addWord($word);
            $this->filterEn->addWord($word);
        }
    }

    /**
     * Vérifie si un texte contient des mots inappropriés (FR + EN).
     */
    public function containsProfanity(string $text): bool
    {
        return $this->filterFr->containsProfanity($text) 
            || $this->filterEn->containsProfanity($text);
    }

    /**
     * Nettoie un texte en remplaçant les mots inappropriés par des astérisques.
     */
    public function clean(string $text): string
{
    $text = $this->filterFr->clean($text) ?? $text;
    $text = $this->filterEn->clean($text) ?? $text;

    return $text;
}
}

