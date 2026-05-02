<?php

namespace App\Tests\Unit;

use App\Entity\Produit;
use PHPUnit\Framework\TestCase;

class ProduitTest extends TestCase
{
    public function testProduitInstantiation(): void
    {
        $produit = new Produit();
        $this->assertInstanceOf(Produit::class, $produit);
    }

    public function testNomGetterAndSetter(): void
    {
        $produit = new Produit();
        $nom = "Tente de Camping";
        $produit->setNom($nom);
        $this->assertEquals($nom, $produit->getNom());
    }
}
