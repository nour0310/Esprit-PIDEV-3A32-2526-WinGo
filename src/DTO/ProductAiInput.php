<?php

namespace App\DTO;

class ProductAiInput
{
    public function __construct(
        public readonly string $nom,
        public readonly string $categorie,
        public readonly string $region,
        public readonly string|float $prix,
        public readonly string|int   $stock,
    ) {}

    public function toArray(): array
    {
        return [
            'nom'       => $this->nom,
            'categorie' => $this->categorie,
            'region'    => $this->region,
            'prix'      => $this->prix,
            'stock'     => $this->stock,
        ];
    }
}