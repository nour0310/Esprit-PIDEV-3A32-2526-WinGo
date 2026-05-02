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
        $converted = iconv('UTF-8', 'ASCII//TRANSLIT', $text);

        if ($converted === false) {
            $converted = $text;
        }

        $converted = strtolower($converted);

        $converted = preg_replace('/[^a-z0-9]+/', '-', $converted);

        if ($converted === null) {
            return '';
        }

        return trim($converted, '-');
    }
}