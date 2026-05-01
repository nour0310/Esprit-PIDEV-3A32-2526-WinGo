<?php

namespace App\Service;

use App\Entity\Panier;
use App\Entity\Produit;
use App\Repository\ProduitRepository;
use Doctrine\ORM\EntityManagerInterface;

class StockService
{
    public function __construct(
        private ProduitRepository      $produitRepository,
        private EntityManagerInterface $em,
    ) {}

    /**
     * Vérifie si le stock est suffisant pour toutes les lignes du panier.
     * Retourne le nom du premier produit en rupture, null si tout est ok.
     *
     * Appelé AVANT de créer la commande — si insuffisant on bloque.
     *
     * @param Panier[] $lignesPanier
     */
    public function verifierStock(array $lignesPanier): ?string
    {
        foreach ($lignesPanier as $ligne) {
            $produit = $this->produitRepository->find($ligne->getIdProduit());

            if (!$produit) {
                continue;
            }

            if ($produit->getStock() < $ligne->getQuantite()) {
                return $produit->getNom();
            }
        }

        return null;
    }

    /**
     * Décrémente le stock de chaque produit commandé.
     * Utilise les entités Doctrine (portable, testable) — pas de SQL natif.
     * Le stock ne peut jamais descendre sous 0 grâce à max().
     *
     * Appelé APRÈS validation de commande, dans la même transaction que le flush().
     *
     * @param Panier[] $lignesPanier
     */
    public function decrementerStock(array $lignesPanier): void
    {
        foreach ($lignesPanier as $ligne) {
            $produit = $this->produitRepository->find($ligne->getIdProduit());

            if (!$produit) {
                continue;
            }

            $nouveauStock = max(0, $produit->getStock() - $ligne->getQuantite());
            $produit->setStock($nouveauStock);

            // Pas de flush ici — le flush est fait UNE SEULE FOIS dans le controller
            // après toutes les modifications (commande + stock + suppression panier)
        }
    }
}