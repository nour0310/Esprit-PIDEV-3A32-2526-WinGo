<?php

namespace App\Security;

use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;
use Symfony\Component\Security\Core\Authentication\Token\TokenInterface;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Security\Http\Authentication\AuthenticationSuccessHandlerInterface;

class LoginSuccessHandler implements AuthenticationSuccessHandlerInterface
{
    public function __construct(
        private UrlGeneratorInterface $urlGenerator
    ) {}

    public function onAuthenticationSuccess(Request $request, TokenInterface $token): Response
    {
        $userCaptcha = (string) $request->request->get('captcha', '');
        $sessionCaptcha = (string) $request->getSession()->get('captcha_code', '');

        if ($userCaptcha === '' || strtolower($userCaptcha) !== strtolower($sessionCaptcha)) {
            $request->getSession()->invalidate();
            $request->getSession()->getFlashBag()->add('error', 'Le code Captcha est incorrect.');

            return new RedirectResponse($this->urlGenerator->generate('app_login'));
        }

        $user = $token->getUser();

        if (!$user instanceof UserInterface) {
            return new RedirectResponse($this->urlGenerator->generate('app_login'));
        }

        $roles = $user->getRoles();

        if (in_array('ROLE_ADMIN', $roles, true) || in_array('ROLE_COMMERCANT', $roles, true)) {
            return new RedirectResponse($this->urlGenerator->generate('app_login_choice'));
        }

        return new RedirectResponse($this->urlGenerator->generate('home'));
    }
}