<?php

namespace App\Tests\Service;

use App\Entity\Reclamation;
use App\Service\ReclamationManager;
use PHPUnit\Framework\TestCase;

class ReclamationManagerTest extends TestCase
{
    /**
     * Test 1 : Une réclamation valide passe la validation
     */
    public function testValidReclamation(): void
    {
        $reclamation = new Reclamation();
        $reclamation->setSujet('Problème de livraison');
        $reclamation->setDescription('Ma commande n\'est jamais arrivée');
        $reclamation->setStatut('En attente');

        $manager = new ReclamationManager();
        $this->assertTrue($manager->validate($reclamation));
    }

    /**
     * Test 2 : Une réclamation sans sujet est rejetée
     */
    public function testReclamationWithoutSujet(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le sujet est obligatoire');

        $reclamation = new Reclamation();
        $reclamation->setDescription('Description test');
        $reclamation->setStatut('En attente');

        $manager = new ReclamationManager();
        $manager->validate($reclamation);
    }

    /**
     * Test 3 : Une réclamation sans description est rejetée
     */
    public function testReclamationWithoutDescription(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('La description est obligatoire');

        $reclamation = new Reclamation();
        $reclamation->setSujet('Sujet test');
        $reclamation->setStatut('En attente');

        $manager = new ReclamationManager();
        $manager->validate($reclamation);
    }

    /**
     * Test 4 : Une réclamation avec statut invalide est rejetée
     */
    public function testReclamationWithInvalidStatut(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le statut n\'est pas valide');

        $reclamation = new Reclamation();
        $reclamation->setSujet('Sujet test');
        $reclamation->setDescription('Description test');
        $reclamation->setStatut('Statut Invalide');

        $manager = new ReclamationManager();
        $manager->validate($reclamation);
    }
}
