<?php

namespace App\Twig;

use App\Service\ProfanityFilterService;
use Twig\Extension\AbstractExtension;
use Twig\TwigFilter;

/**
 * Extension Twig pour censurer les mots inappropriés dans le front-office.
 */
class ProfanityExtension extends AbstractExtension
{
    public function __construct(
        private ProfanityFilterService $profanityFilter,
    ) {}

    public function getFilters(): array
    {
        return [
            new TwigFilter('censor', [$this, 'censor']),
        ];
    }

    /**
     * Censure les mots inappropriés dans un texte (remplace par ****).
     */
    public function censor(string $text): string
    {
        return $this->profanityFilter->clean($text);
    }
}
