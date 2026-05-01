<?php

namespace App\Tests\Service;

use App\Entity\Commande;
use App\Service\CommandeManager;
use PHPUnit\Framework\TestCase;

class CommandeManagerTest extends TestCase
{
    /**
     * Test 1 : Une commande valide passe la validation
     */
    public function testValidCommande(): void
    {
        $commande = new Commande();
        $commande->setStatus('en_cours');
        $commande->setTotal('150.00');
        $commande->setIdUser(1);

        $manager = new CommandeManager();
        $this->assertTrue($manager->validate($commande));
    }

    /**
     * Test 2 : Une commande avec total négatif est rejetée
     */
    public function testCommandeWithNegativeTotal(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le total ne peut pas être négatif');

        $commande = new Commande();
        $commande->setStatus('en_cours');
        $commande->setTotal('-50.00');

        $manager = new CommandeManager();
        $manager->validate($commande);
    }

    /**
     * Test 3 : Une commande avec statut invalide est rejetée
     */
    public function testCommandeWithInvalidStatus(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le statut de la commande n\'est pas valide');

        $commande = new Commande();
        $commande->setStatus('invalide');
        $commande->setTotal('100.00');

        $manager = new CommandeManager();
        $manager->validate($commande);
    }

    /**
     * Test 4 : Une commande annulée sans cause est rejetée
     */
    public function testCommandeAnnuleeSansCause(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Une commande annulée doit avoir une cause d\'annulation');

        $commande = new Commande();
        $commande->setStatus('annulee');
        $commande->setTotal('100.00');

        $manager = new CommandeManager();
        $manager->validate($commande);
    }

    /**
     * Test 5 : Une commande annulée AVEC cause passe la validation
     */
    public function testCommandeAnnuleeAvecCause(): void
    {
        $commande = new Commande();
        $commande->setStatus('annulee');
        $commande->setTotal('100.00');
        $commande->setCauseAnnulation('Client a changé d\'avis');

        $manager = new CommandeManager();
        $this->assertTrue($manager->validate($commande));
    }
}
