<?php

namespace App\Twig;

use Symfony\Component\String\Slugger\AsciiSlugger;
use Twig\Extension\AbstractExtension;
use Twig\TwigFilter;

class SlugExtension extends AbstractExtension
{
    private AsciiSlugger $slugger;

    public function __construct()
    {
        $this->slugger = new AsciiSlugger();
    }

    public function getFilters(): array
    {
        return [
            new TwigFilter('slug', [$this, 'slugify']),
        ];
    }

    public function slugify(string $text): string
    {
        return $this->slugger->slug($text)->lower()->toString();
    }
}
