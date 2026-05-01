<?php

namespace App\Security;

use App\Entity\Utilisateur;
use Symfony\Component\Security\Core\Exception\CustomUserMessageAccountStatusException;
use Symfony\Component\Security\Core\User\UserCheckerInterface;
use Symfony\Component\Security\Core\User\UserInterface;

class UserChecker implements UserCheckerInterface
{
    public function checkPreAuth(UserInterface $user): void
    {
        if (!$user instanceof Utilisateur) {
            return;
        }

        // La vérification par email a été supprimée à la demande de l'utilisateur.
        // Tous les comptes sont désormais considérés comme actifs dès leur création.
    }

    public function checkPostAuth(UserInterface $user): void
    {
        // No post-auth checks needed
    }
}