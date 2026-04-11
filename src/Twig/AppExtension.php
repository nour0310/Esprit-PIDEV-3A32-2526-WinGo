<?php

namespace App\Twig;

use Twig\Extension\AbstractExtension;
use Twig\TwigFunction;

class AppExtension extends AbstractExtension
{
    public function getFunctions(): array
    {
        return [
            new TwigFunction('slugify', [$this, 'slugify']),
        ];
    }

    public function slugify(string $text): string
    {
        // Remplacer les caractères accentués
        $text = iconv('UTF-8', 'ASCII//TRANSLIT', $text);
        // Mettre en minuscules
        $text = strtolower($text);
        // Remplacer tout ce qui n'est pas alphanumérique par un tiret
        $text = preg_replace('/[^a-z0-9]+/', '-', $text);
        // Supprimer les tirets en début et fin
        return trim($text, '-');
    }
}
