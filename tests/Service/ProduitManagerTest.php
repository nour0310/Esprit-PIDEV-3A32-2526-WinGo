<?php

namespace App\Tests\Service;

use App\Entity\Produit;
use App\Service\ProduitManager;
use PHPUnit\Framework\TestCase;

class ProduitManagerTest extends TestCase
{
    /**
     * Test 1 : Un produit valide passe la validation
     */
    public function testValidProduit(): void
    {
        $produit = new Produit();
        $produit->setNom('Huile Olive');
        $produit->setPrix('15.99');
        $produit->setStock(50);

        $manager = new ProduitManager();
        $this->assertTrue($manager->validate($produit));
    }

    /**
     * Test 2 : Un produit sans nom est rejeté
     */
    public function testProduitWithoutNom(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le nom du produit est obligatoire');

        $produit = new Produit();
        $produit->setNom('');
        $produit->setPrix('15.99');
        $produit->setStock(50);

        $manager = new ProduitManager();
        $manager->validate($produit);
    }

    /**
     * Test 3 : Un produit avec prix négatif est rejeté
     */
    public function testProduitWithNegativePrice(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le prix doit être supérieur à zéro');

        $produit = new Produit();
        $produit->setNom('Produit Test');
        $produit->setPrix('-5.00');
        $produit->setStock(10);

        $manager = new ProduitManager();
        $manager->validate($produit);
    }

    /**
     * Test 4 : Un produit avec prix zéro est rejeté
     */
    public function testProduitWithZeroPrice(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le prix doit être supérieur à zéro');

        $produit = new Produit();
        $produit->setNom('Produit Gratuit');
        $produit->setPrix('0.00');
        $produit->setStock(10);

        $manager = new ProduitManager();
        $manager->validate($produit);
    }

    /**
     * Test 5 : Un produit avec stock négatif est rejeté
     */
    public function testProduitWithNegativeStock(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le stock ne peut pas être négatif');

        $produit = new Produit();
        $produit->setNom('Produit Test');
        $produit->setPrix('10.00');
        $produit->setStock(-5);

        $manager = new ProduitManager();
        $manager->validate($produit);
    }
}
