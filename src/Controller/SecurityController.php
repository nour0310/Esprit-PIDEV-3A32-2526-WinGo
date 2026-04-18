<?php

namespace App\Controller;

use App\Entity\Utilisateur;
use App\Repository\UtilisateurRepository;
use App\Service\AuthPageTranslationService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Mailer\Exception\TransportExceptionInterface;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;
use Symfony\Component\Validator\Validator\ValidatorInterface;
use Symfony\Component\Security\Http\Attribute\CurrentUser;
use Symfony\Bundle\SecurityBundle\Security;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;
use Symfony\Component\Security\Core\Authentication\Token\UsernamePasswordToken;
use Psr\Log\LoggerInterface;
use Twig\Environment;

class SecurityController extends AbstractController
{
    private function buildMailerErrorMessage(\Throwable $exception): string
    {
        $message = $exception->getMessage();

        if (str_contains($message, '535') || str_contains($message, 'Authentication unsuccessful')) {
            return "Le compte a ete cree, mais l'envoi d'email a echoue. Le service SMTP actuel refuse l'authentification. Verifiez MAILER_DSN ou remplacez-le par un autre provider mail comme Brevo, Gmail ou Mailtrap.";
        }

        return sprintf(
            "Le compte a ete cree, mais l'envoi d'email a echoue : %s",
            $message
        );
    }

    private function getEnvValue(string $key, string $default = ''): string
    {
        return $_ENV[$key] ?? $_SERVER[$key] ?? getenv($key) ?: $default;
    }

    private function isMailerConfigured(): bool
    {
        $dsn = $this->getEnvValue('MAILER_DSN');

        return $dsn !== '' && !str_starts_with($dsn, 'null://');
    }

    #[Route('/login', name: 'app_login')]
    public function login(AuthenticationUtils $authenticationUtils, Request $request, AuthPageTranslationService $authPageTranslationService): Response
    {
        // get the login error if there is one
        $error = $authenticationUtils->getLastAuthenticationError();
        $lastUsername = $authenticationUtils->getLastUsername();
        $lang = $authPageTranslationService->normalizeLang($request->query->get('lang', 'FR'));
        $ui = $authPageTranslationService->forLogin($lang);

        // Vérification du Captcha si le formulaire est soumis
        if ($request->isMethod('POST')) {
            $userCaptcha = $request->request->get('captcha');
            $sessionCaptcha = $request->getSession()->get('captcha_code');
            
            if (!$userCaptcha || strtolower($userCaptcha) !== strtolower($sessionCaptcha)) {
                $this->addFlash('error', 'Le code Captcha est incorrect.');
                return $this->render('security/login.html.twig', [
                    'last_username' => $lastUsername,
                    'error' => null,
                    'ui' => $ui,
                    'lang' => $lang,
                ]);
            }
        }

        return $this->render('security/login.html.twig', [
            'last_username' => $lastUsername,
            'error' => $error,
            'ui' => $ui,
            'lang' => $lang,
        ]);
    }

    #[Route('/captcha-image', name: 'app_captcha')]
    public function captcha(Request $request): Response
    {
        $code = substr(str_shuffle("ABCDEFGHJKLMNPQRSTUVWXYZ23456789"), 0, 5);
        $request->getSession()->set('captcha_code', $code);

        // Génération d'un SVG (pas besoin de l'extension GD)
        $svg = '<svg width="120" height="45" xmlns="http://www.w3.org/2000/svg">';
        $svg .= '<rect width="100%" height="100%" fill="#ffffff"/>';
        
        // Ajout de lignes de bruit
        for ($i = 0; $i < 6; $i++) {
            $svg .= sprintf('<line x1="%d" y1="%d" x2="%d" y2="%d" stroke="#fa9e1b" stroke-width="1" opacity="0.5"/>', 
                rand(0, 120), rand(0, 45), rand(0, 120), rand(0, 45));
        }

        // Texte avec distorsion légère
        $svg .= sprintf('<text x="50%%" y="60%%" font-family="Arial, sans-serif" font-size="24" font-weight="bold" fill="#1d2140" text-anchor="middle" letter-spacing="3">%s</text>', $code);
        $svg .= '</svg>';

        return new Response($svg, 200, ['Content-Type' => 'image/svg+xml']);
    }

    #[Route('/register', name: 'app_register')]
    public function register(
        Request $request,
        EntityManagerInterface $em,
        UserPasswordHasherInterface $passwordHasher,
        MailerInterface $mailer,
        Environment $twig,
        LoggerInterface $logger,
        \Symfony\Component\Validator\Validator\ValidatorInterface $validator,
        AuthPageTranslationService $authPageTranslationService
    ): Response {
        if ($this->getUser()) {
            return $this->redirectToRoute('app_dashboard_redirect');
        }

        $error = null;
        $lang = $authPageTranslationService->normalizeLang($request->query->get('lang', 'FR'));
        $ui = $authPageTranslationService->forRegister($lang);

        if ($request->isMethod('POST')) {
            // Vérification Captcha
            $userCaptcha = $request->request->get('captcha');
            $sessionCaptcha = $request->getSession()->get('captcha_code');
            
            if (!$userCaptcha || strtolower($userCaptcha) !== strtolower($sessionCaptcha)) {
                $error = 'Le code Captcha est incorrect.';
            } else {
                $user = new Utilisateur();
                $user->setNom(trim($request->request->get('nom', '')));
                $user->setPrenom(trim($request->request->get('prenom', '')));
                $user->setEmail(trim($request->request->get('email', '')));
                $user->setPlainPassword($request->request->get('password', ''));
                $user->setTelephone($request->request->get('telephone') ? (int) $request->request->get('telephone') : null);
                $user->setAge($request->request->get('age') ? (int) $request->request->get('age') : null);
                $user->setGenre(trim($request->request->get('genre', ''))); // ADDED GENRE
                $user->setType('CLIENT');
                $user->setIsVerified(true);
                $user->setVerificationCode(null);

                // Validation via l'Entité
                $errors = $validator->validate($user, null, ['Default', 'registration']);

                if (count($errors) > 0) {
                    $error = $errors[0]->getMessage();
                } else {
                    $hashedPassword = $passwordHasher->hashPassword($user, $user->getPlainPassword());
                    $user->setMotDePasse($hashedPassword);
                    $user->setPlainPassword(null); // Protection

                    $em->persist($user);
                    $em->flush();

                    if ($this->isMailerConfigured()) {
                        try {
                            // Notifier l'admin
                            $adminEmail = $this->getEnvValue('ADMIN_NOTIFICATION_EMAIL', $this->getEnvValue('MAILER_FROM', 'admin@wingo.local'));
                            $notificationMessage = (new Email())
                                ->from($this->getEnvValue('MAILER_FROM', 'noreply@wingo.local'))
                                ->to($adminEmail)
                                ->subject('Nouvelle inscription client : ' . $user->getFullName())
                                ->text(sprintf(
                                    "Un nouvel utilisateur s'est inscrit sur WinGo :\nNom : %s %s\nEmail : %s\nType : %s\nDate : %s",
                                    $user->getPrenom(),
                                    $user->getNom(),
                                    $user->getEmail(),
                                    $user->getType(),
                                    (new \DateTime())->format('d/m/Y H:i')
                                ));
                            $mailer->send($notificationMessage);
                        } catch (TransportExceptionInterface|\Throwable $exception) {
                            $logger->error('Echec envoi notification admin.', [
                                'exception' => $exception,
                                'message' => $exception->getMessage(),
                            ]);
                        }
                    }

                    $this->addFlash('success', 'Votre compte a été créé avec succès !');
                    return $this->redirectToRoute('app_login');
                }
            }
        }

        return $this->render('security/register.html.twig', [
            'error' => $error,
            'last_data' => $request->request->all(),
            'ui' => $ui,
            'lang' => $lang,
        ]);
    }

    #[Route('/forgot-password', name: 'app_forgot_password_request')]
    public function forgotPasswordRequest(
        Request $request,
        UtilisateurRepository $userRepository,
        MailerInterface $mailer,
        EntityManagerInterface $em,
        LoggerInterface $logger
    ): Response {
        return $this->redirectToRoute('app_forgot_password_link_request');
    }

    #[Route('/reset-password', name: 'app_reset_password')]
    public function resetPassword(
        Request $request,
        UtilisateurRepository $userRepository,
        UserPasswordHasherInterface $passwordHasher,
        EntityManagerInterface $em,
        ValidatorInterface $validator
    ): Response {
        return $this->redirectToRoute('app_forgot_password_link_request');
    }

    #[Route('/face-id/save', name: 'app_face_id_save', methods: ['POST'])]
    public function saveFaceDescriptor(
        Request $request,
        EntityManagerInterface $em
    ): JsonResponse {
        /** @var Utilisateur $user */
        $user = $this->getUser();
        if (!$user) {
            return new JsonResponse(['error' => 'Non connecté'], 403);
        }

        $descriptor = $request->request->get('descriptor');
        if (!$descriptor) {
            return new JsonResponse(['error' => 'Pas de descripteur'], 400);
        }

        $user->setFaceDescriptor($descriptor);
        $em->flush();

        return new JsonResponse(['success' => true]);
    }

    #[Route('/face-id/login', name: 'app_face_id_login', methods: ['POST'])]
    public function faceIdLogin(
        Request $request,
        UtilisateurRepository $userRepository,
        TokenStorageInterface $tokenStorage
    ): JsonResponse {
        $email = $request->request->get('email');
        $descriptorJson = $request->request->get('descriptor');

        if (!$email || !$descriptorJson) {
            return new JsonResponse(['error' => 'Données incomplètes'], 400);
        }

        $user = $userRepository->findOneBy(['email' => $email]);
        if (!$user || !$user->getFaceDescriptor()) {
            return new JsonResponse(['error' => 'Visage non enregistré pour ce compte'], 404);
        }

        // --- VALIDATION PHP OBLIGATOIRE ---
        $currentDescriptor = json_decode($descriptorJson, true);
        $storedDescriptor = json_decode($user->getFaceDescriptor(), true);

        if (!is_array($currentDescriptor) || !is_array($storedDescriptor)) {
            return new JsonResponse(['error' => 'Erreur de lecture des signatures faciales'], 500);
        }

        // Calcul de la ressemblance mathématique
        $distance = $this->euclideanDistance($currentDescriptor, $storedDescriptor);
        
        // Seuil de sécurité : plus la distance est proche de 0, plus les visages sont identiques.
        // En général, 0.6 est le standard pour face-api.js
        $threshold = 0.6;

        if ($distance > $threshold) {
            return new JsonResponse([
                'error' => 'Reconnaissance échouée : Le visage ne correspond pas (' . round($distance, 3) . ')',
                'distance' => $distance
            ], 403);
        }

        // Authentification officielle par le serveur
        try {
            $token = new UsernamePasswordToken($user, 'main', $user->getRoles());
            $tokenStorage->setToken($token);
            $request->getSession()->set('_security_main', serialize($token));
        } catch (\Exception $e) {
            return new JsonResponse(['error' => 'Erreur lors de la création de la session : ' . $e->getMessage()], 500);
        }

        return new JsonResponse([
            'success' => true, 
            'distance' => $distance,
            'redirect' => $this->generateUrl('app_dashboard_redirect')
        ]);
    }

    /**
     * Calcul mathématique de la distance entre deux signatures faciales (128 dimensions).
     * Plus le résultat est proche de 0, plus la ressemblance est forte.
     */
    private function euclideanDistance(array $v1, array $v2): float
    {
        $sum = 0.0;
        $count = count($v1);
        
        // On s'assure que les deux bouquets ont la même taille
        for ($i = 0; $i < $count; $i++) {
            $diff = $v1[$i] - $v2[$i];
            $sum += $diff * $diff;
        }
        
        return sqrt($sum);
    }

    #[Route('/logout', name: 'app_logout')]
    public function logout(): void
    {
    }

    #[Route('/dashboard-redirect', name: 'app_dashboard_redirect')]
    public function dashboardRedirect(): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $roles = $user->getRoles();
        if (in_array('ROLE_ADMIN', $roles, true) || in_array('ROLE_COMMERCANT', $roles, true)) {
            return $this->redirectToRoute('app_login_choice');
        }

        return $this->redirectToRoute('home');
    }

    #[Route('/login-choice', name: 'app_login_choice')]
    public function loginChoice(): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $roles = $user->getRoles();
        if (!in_array('ROLE_ADMIN', $roles, true) && !in_array('ROLE_COMMERCANT', $roles, true)) {
            return $this->redirectToRoute('home');
        }

        return $this->render('security/login_choice.html.twig');
    }
}
