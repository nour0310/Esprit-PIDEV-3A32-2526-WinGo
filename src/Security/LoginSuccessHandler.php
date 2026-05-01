<?php

namespace App\Security;

use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;
use Symfony\Component\Security\Core\Authentication\Token\TokenInterface;
use Symfony\Component\Security\Http\Authentication\AuthenticationSuccessHandlerInterface;

class LoginSuccessHandler implements AuthenticationSuccessHandlerInterface
{
    public function __construct(
        private UrlGeneratorInterface $urlGenerator
    ) {}

    public function onAuthenticationSuccess(Request $request, TokenInterface $token): Response
    {
        // Récupérer le captcha soumis et celui en session
        $userCaptcha = $request->request->get('captcha');
        $sessionCaptcha = $request->getSession()->get('captcha_code');

        // Vérifier la validité du captcha
        if (!$userCaptcha || strtolower($userCaptcha) !== strtolower($sessionCaptcha)) {
            // Invalider la session pour forcer la déconnexion
            $request->getSession()->invalidate();
            $request->getSession()->getFlashBag()->add('error', 'Le code Captcha est incorrect.');
            return new RedirectResponse($this->urlGenerator->generate('app_login'));
        }

        // Captcha valide → redirection selon le rôle de l'utilisateur
        $user = $token->getUser();
        $roles = $user->getRoles();

        if (in_array('ROLE_ADMIN', $roles, true) || in_array('ROLE_COMMERCANT', $roles, true)) {
            return new RedirectResponse($this->urlGenerator->generate('app_login_choice'));
        }

        return new RedirectResponse($this->urlGenerator->generate('home'));
    }
}