<?php

namespace App\Controller;

use App\Repository\UtilisateurRepository;
use App\Service\AuthPageTranslationService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Address;
use Symfony\Component\Mime\Email;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;
use Symfony\Component\Validator\Validator\ValidatorInterface;
use SymfonyCasts\Bundle\ResetPassword\Controller\ResetPasswordControllerTrait;
use SymfonyCasts\Bundle\ResetPassword\Exception\ResetPasswordExceptionInterface;
use SymfonyCasts\Bundle\ResetPassword\ResetPasswordHelperInterface;

#[Route('/reset-password-link')]
class ResetPasswordController extends AbstractController
{
    use ResetPasswordControllerTrait;

    public function __construct(
        private ResetPasswordHelperInterface $resetPasswordHelper,
        private EntityManagerInterface $em
    ) {
    }

    #[Route('', name: 'app_forgot_password_link_request')]
    public function request(
        Request $request,
        MailerInterface $mailer,
        UtilisateurRepository $userRepo,
        AuthPageTranslationService $authPageTranslationService
    ): Response {
        $error = null;
        $lang = $authPageTranslationService->normalizeLang($request->query->get('lang', 'FR'));
        $ui = $authPageTranslationService->forForgotPassword($lang);

        if ($request->isMethod('POST')) {
            $email = trim((string) $request->request->get('email', ''));
            $user = $userRepo->findOneBy(['email' => $email]);

            if (!$user) {
                $this->addFlash('success', 'Si cet email existe, un lien de reinitialisation vous a ete envoye.');
                return $this->redirectToRoute('app_reset_password_link_check_email', ['lang' => $lang]);
            }

            try {
                $resetToken = $this->resetPasswordHelper->generateResetToken($user);
                $resetUrl = $this->generateUrl('app_reset_password_link', [
                    'token' => $resetToken->getToken(),
                    'lang' => $lang,
                ], UrlGeneratorInterface::ABSOLUTE_URL);

                $emailHtml = sprintf(
                    '<!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            body { font-family: Segoe UI, Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }
                            .container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px; }
                            .header { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #eee; }
                            .logo { font-size: 28px; font-weight: bold; color: #fa9e1b; }
                            .content { padding: 30px 0; }
                            .button { display: inline-block; padding: 12px 24px; background-color: #fa9e1b; color: #ffffff !important; text-decoration: none; border-radius: 5px; font-weight: bold; }
                            .footer { font-size: 12px; color: #777; text-align: center; padding-top: 20px; border-top: 1px solid #eee; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <div class="logo">WinGo</div>
                            </div>
                            <div class="content">
                                <h2>Bonjour %s,</h2>
                                <p>Vous avez demande la reinitialisation de votre mot de passe pour votre compte WinGo.</p>
                                <p>Cliquez sur le bouton ci-dessous pour choisir un nouveau mot de passe.</p>
                                <div style="text-align: center; margin: 30px 0;">
                                    <a href="%s" class="button">Reinitialiser mon mot de passe</a>
                                </div>
                                <p style="word-break: break-all; font-size: 13px; color: #666;">
                                    Si le bouton ne fonctionne pas, copiez ce lien dans votre navigateur :<br>
                                    %s
                                </p>
                                <p>Si vous n\'avez pas demande cette reinitialisation, vous pouvez ignorer cet e-mail en toute securite.</p>
                                <p>Cordialement,<br>L\'equipe WinGo</p>
                            </div>
                            <div class="footer">
                                Cet e-mail a ete envoye automatiquement, merci de ne pas y repondre.
                            </div>
                        </div>
                    </body>
                    </html>',
                    htmlspecialchars((string) $user->getPrenom(), ENT_QUOTES),
                    htmlspecialchars($resetUrl, ENT_QUOTES),
                    htmlspecialchars($resetUrl, ENT_QUOTES)
                );

                $emailMessage = (new Email())
                    ->from(new Address($_ENV['MAILER_FROM'] ?? 'noreply@wingo.local', 'WinGo'))
                    ->to($user->getEmail())
                    ->subject('Reinitialisation de votre mot de passe - WinGo')
                    ->html($emailHtml);

                $mailer->send($emailMessage);
                $this->setTokenObjectInSession($resetToken);
            } catch (ResetPasswordExceptionInterface $e) {
                $error = 'Erreur : '.$e->getReason();
            } catch (\Throwable) {
                $error = "Erreur d'envoi mail. Verifiez votre MAILER_DSN.";
            }

            if (!$error) {
                return $this->redirectToRoute('app_reset_password_link_check_email', ['lang' => $lang]);
            }
        }

        return $this->render('security/forgot_password.html.twig', [
            'error' => $error,
            'ui' => $ui,
            'lang' => $lang,
        ]);
    }

    #[Route('/check-email', name: 'app_reset_password_link_check_email')]
    public function checkEmail(Request $request, AuthPageTranslationService $authPageTranslationService): Response
    {
        $lang = $authPageTranslationService->normalizeLang($request->query->get('lang', 'FR'));

        return $this->render('security/check_email.html.twig', [
            'ui' => $authPageTranslationService->forCheckEmail($lang),
            'lang' => $lang,
        ]);
    }

    #[Route('/reset/{token}', name: 'app_reset_password_link')]
    public function reset(
        Request $request,
        UserPasswordHasherInterface $passwordHasher,
        ValidatorInterface $validator,
        AuthPageTranslationService $authPageTranslationService,
        ?string $token = null
    ): Response {
        $lang = $authPageTranslationService->normalizeLang($request->query->get('lang', 'FR'));
        $ui = $authPageTranslationService->forResetPassword($lang);

        if ($token) {
            $this->storeTokenInSession($token);

            return $this->redirectToRoute('app_reset_password_link', ['lang' => $lang]);
        }

        $token = $this->getTokenFromSession();
        if (!$token) {
            return $this->redirectToRoute('app_forgot_password_link_request', ['lang' => $lang]);
        }

        $error = null;

        try {
            /** @var \App\Entity\Utilisateur $user */
            $user = $this->resetPasswordHelper->validateTokenAndFetchUser($token);
        } catch (ResetPasswordExceptionInterface) {
            $this->addFlash('error', 'Le lien est invalide ou a expire. Veuillez recommencer.');

            return $this->redirectToRoute('app_forgot_password_link_request', ['lang' => $lang]);
        }

        if ($request->isMethod('POST')) {
            $password = (string) $request->request->get('password', '');

            if (trim($password) === '') {
                $error = 'Veuillez saisir votre nouveau mot de passe.';
            } else {
                $user->setPlainPassword($password);
                $errors = $validator->validate($user, null, ['registration']);

                if (count($errors) > 0) {
                    $firstError = $errors[0] ?? null;

                if ($firstError !== null) {
                    $error = $firstError->getMessage();
                }
                } else {
                    $this->resetPasswordHelper->removeResetRequest($token);
                    $hashedPassword = $passwordHasher->hashPassword($user, $password);
                    $user->setMotDePasse($hashedPassword);
                    $user->setPlainPassword(null);
                    $this->em->flush();
                    $this->cleanSessionAfterReset();
                    $this->addFlash('success', 'Mot de passe reinitialise avec succes !');

                    return $this->redirectToRoute('app_login', ['lang' => $lang]);
                }
            }
        }

        return $this->render('security/reset_password.html.twig', [
            'error' => $error,
            'ui' => $ui,
            'lang' => $lang,
        ]);
    }
}
