<?php

namespace App\Security;

use App\Repository\UtilisateurRepository;
use Symfony\Component\Security\Core\Exception\BadCredentialsException;
use Symfony\Component\Security\Http\AccessToken\AccessTokenHandlerInterface;
use Symfony\Component\Security\Http\Authenticator\Passport\Badge\UserBadge;

class ApiTokenHandler implements AccessTokenHandlerInterface
{
    private $repository;

    public function __construct(UtilisateurRepository $repository)
    {
        $this->repository = $repository;
    }

    public function getUserBadgeFrom(string $accessToken): UserBadge
    {
        // cherche un utilisateur via son apiToken
        $user = $this->repository->findOneBy(['apiToken' => $accessToken]);

        if (null === $user) {
            throw new BadCredentialsException('Invalid API token.');
        }

        // Retourne un UserBadge avec l'identifiant de l'utilisateur (ici l'email)
        return new UserBadge($user->getUserIdentifier());
    }
}
