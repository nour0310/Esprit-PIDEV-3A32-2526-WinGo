<?php

namespace App\Tests\Unit;

use App\Entity\Utilisateur;
use PHPUnit\Framework\TestCase;

class UtilisateurTest extends TestCase
{
    public function testUserInstantiation(): void
    {
        $user = new Utilisateur();
        $this->assertInstanceOf(Utilisateur::class, $user);
    }

    public function testEmailGetterAndSetter(): void
    {
        $user = new Utilisateur();
        $email = "test@wingo.tn";
        $user->setEmail($email);
        $this->assertEquals($email, $user->getEmail());
    }

    public function testNomPrenomGetterAndSetter(): void
    {
        $user = new Utilisateur();
        $nom = "Zaghdoud";
        $prenom = "Balkis";
        
        $user->setNom($nom);
        $user->setPrenom($prenom);
        
        $this->assertEquals($nom, $user->getNom());
        $this->assertEquals($prenom, $user->getPrenom());
    }

    public function testRolesDefaultAndSetting(): void
    {
        $user = new Utilisateur();
        
        // Par défaut, le type est null, mais UserInterface impose ROLE_USER
        $this->assertContains('ROLE_USER', $user->getRoles());
        
        // Si type est ADMIN
        $user->setType('ADMIN');
        $this->assertContains('ROLE_ADMIN', $user->getRoles());
    }

    public function testPasswordGetterAndSetter(): void
    {
        $user = new Utilisateur();
        $password = "hashed_password_123";
        $user->setMotDePasse($password);
        $this->assertEquals($password, $user->getPassword());
    }

    public function testVerificationStatus(): void
    {
        $user = new Utilisateur();
        $this->assertFalse($user->isVerified());
        
        $user->setIsVerified(true);
        $this->assertTrue($user->isVerified());
    }
}
