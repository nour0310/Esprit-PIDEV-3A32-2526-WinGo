<?php

namespace App\Controller;

use App\Entity\Utilisateur;
use App\Repository\UtilisateurRepository;
use App\Service\AuthPageTranslationService;
use Doctrine\ORM\EntityManagerInterface;
use Psr\Log\LoggerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;
use Symfony\Component\Security\Core\Authentication\Token\UsernamePasswordToken;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;
use Symfony\Component\Validator\Validator\ValidatorInterface;
use Twig\Environment;

class SecurityController extends AbstractController
{
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
    public function login(
        AuthenticationUtils $authenticationUtils,
        Request $request,
        AuthPageTranslationService $authPageTranslationService
    ): Response {
        $error = $authenticationUtils->getLastAuthenticationError();
        $lastUsername = $authenticationUtils->getLastUsername();

        $langParam = (string) $request->query->get('lang', 'FR');
        $lang = $authPageTranslationService->normalizeLang($langParam);
        $ui = $authPageTranslationService->forLogin($lang);

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
        $code = substr(str_shuffle('ABCDEFGHJKLMNPQRSTUVWXYZ23456789'), 0, 5);
        $request->getSession()->set('captcha_code', $code);

        $svg = '<svg width="120" height="45" xmlns="http://www.w3.org/2000/svg">';
        $svg .= '<rect width="100%" height="100%" fill="#ffffff"/>';

        for ($i = 0; $i < 6; $i++) {
            $svg .= sprintf(
                '<line x1="%d" y1="%d" x2="%d" y2="%d" stroke="#fa9e1b" stroke-width="1" opacity="0.5"/>',
                rand(0, 120),
                rand(0, 45),
                rand(0, 120),
                rand(0, 45)
            );
        }

        $svg .= sprintf(
            '<text x="50%%" y="60%%" font-family="Arial, sans-serif" font-size="24" font-weight="bold" fill="#1d2140" text-anchor="middle" letter-spacing="3">%s</text>',
            $code
        );
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
        ValidatorInterface $validator,
        AuthPageTranslationService $authPageTranslationService
    ): Response {
        if ($this->getUser()) {
            return $this->redirectToRoute('app_dashboard_redirect');
        }

        $langParam = (string) $request->query->get('lang', 'FR');
        $lang = $authPageTranslationService->normalizeLang($langParam);
        $ui = $authPageTranslationService->forRegister($lang);
        $error = null;
        $lastData = [];

        if ($request->isMethod('POST')) {
            $userCaptcha = (string) $request->request->get('captcha', '');
            $sessionCaptcha = (string) $request->getSession()->get('captcha_code', '');

            if ($userCaptcha === '' || strtolower($userCaptcha) !== strtolower($sessionCaptcha)) {
                $error = 'Le code Captcha est incorrect.';
                $request->getSession()->remove('captcha_code');
                $lastData = $request->request->all();
            } else {
                $request->getSession()->remove('captcha_code');

                $nom = (string) $request->request->get('nom', '');
                $prenom = (string) $request->request->get('prenom', '');
                $email = (string) $request->request->get('email', '');
                $password = (string) $request->request->get('password', '');
                $telephone = $request->request->get('telephone');
                $age = $request->request->get('age');
                $genre = (string) $request->request->get('genre', '');

                $user = new Utilisateur();
                $user->setNom(trim($nom));
                $user->setPrenom(trim($prenom));
                $user->setEmail(trim($email));
                $user->setPlainPassword($password);
                $user->setTelephone($telephone ? (int) $telephone : null);
                $user->setAge($age ? (int) $age : null);
                $user->setGenre(trim($genre));
                $user->setType('CLIENT');
                $user->setIsVerified(true);
                $user->setVerificationCode(null);

                $violations = $validator->validate($user, null, ['Default', 'registration']);

                if (count($violations) > 0) {
                    $errorMessages = [];

                    foreach ($violations as $violation) {
                        $errorMessages[] = $violation->getMessage();
                    }

                    $error = implode('<br>', $errorMessages);
                    $lastData = $request->request->all();
                } else {
                    $plainPassword = $user->getPlainPassword();

                    if ($plainPassword === null || $plainPassword === '') {
                        $error = 'Mot de passe obligatoire.';
                        $lastData = $request->request->all();
                    } else {
                        $hashedPassword = $passwordHasher->hashPassword($user, $plainPassword);
                        $user->setMotDePasse($hashedPassword);
                        $user->setPlainPassword(null);

                        $em->persist($user);
                        $em->flush();

                        if ($this->isMailerConfigured()) {
                            try {
                                $adminEmail = $this->getEnvValue(
                                    'ADMIN_NOTIFICATION_EMAIL',
                                    $this->getEnvValue('MAILER_FROM', 'admin@wingo.local')
                                );

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
                            } catch (\Throwable $exception) {
                                $logger->error('Échec envoi notification admin : ' . $exception->getMessage(), [
                                    'exception' => $exception,
                                ]);
                            }
                        } else {
                            $logger->warning('MAILER_DSN non configuré – aucun email de notification envoyé.');
                        }

                        $this->addFlash('success', 'Votre compte a été créé avec succès !');

                        return $this->redirectToRoute('app_login', ['lang' => $lang]);
                    }
                }
            }
        }

        return $this->render('security/register.html.twig', [
            'error' => $error,
            'last_data' => $lastData ?: $request->request->all(),
            'ui' => $ui,
            'lang' => $lang,
        ]);
    }

    #[Route('/forgot-password', name: 'app_forgot_password_request')]
    public function forgotPasswordRequest(): Response
    {
        return $this->redirectToRoute('app_forgot_password_link_request');
    }

    #[Route('/reset-password', name: 'app_reset_password')]
    public function resetPassword(): Response
    {
        return $this->redirectToRoute('app_forgot_password_link_request');
    }

    #[Route('/face-id/save', name: 'app_face_id_save', methods: ['POST'])]
    public function saveFaceDescriptor(Request $request, EntityManagerInterface $em): JsonResponse
    {
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            return new JsonResponse(['error' => 'Non connecté'], 403);
        }

        $descriptor = (string) $request->request->get('descriptor', '');

        if ($descriptor === '') {
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
        $email = (string) $request->request->get('email', '');
        $descriptorJson = (string) $request->request->get('descriptor', '');

        if ($email === '' || $descriptorJson === '') {
            return new JsonResponse(['error' => 'Données incomplètes'], 400);
        }

        $user = $userRepository->findOneBy(['email' => $email]);

        if (!$user instanceof Utilisateur || !$user->getFaceDescriptor()) {
            return new JsonResponse(['error' => 'Visage non enregistré pour ce compte'], 404);
        }

        $currentDescriptor = json_decode($descriptorJson, true);
        $storedDescriptor = json_decode($user->getFaceDescriptor(), true);

        if (!is_array($currentDescriptor) || !is_array($storedDescriptor)) {
            return new JsonResponse(['error' => 'Erreur de lecture des signatures faciales'], 500);
        }

        $distance = $this->euclideanDistance($currentDescriptor, $storedDescriptor);
        $threshold = 0.6;

        if ($distance > $threshold) {
            return new JsonResponse([
                'error' => 'Reconnaissance échouée : Le visage ne correspond pas (' . round($distance, 3) . ')',
                'distance' => $distance,
            ], 403);
        }

        try {
            $token = new UsernamePasswordToken($user, 'main', $user->getRoles());
            $tokenStorage->setToken($token);
            $request->getSession()->set('_security_main', serialize($token));
        } catch (\Exception $e) {
            return new JsonResponse([
                'error' => 'Erreur lors de la création de la session : ' . $e->getMessage(),
            ], 500);
        }

        return new JsonResponse([
            'success' => true,
            'distance' => $distance,
            'redirect' => $this->generateUrl('app_dashboard_redirect'),
        ]);
    }

    /**
     * @param array<int, float|int> $v1
     * @param array<int, float|int> $v2
     */
    private function euclideanDistance(array $v1, array $v2): float
    {
        $sum = 0.0;
        $count = count($v1);

        for ($i = 0; $i < $count; $i++) {
            $diff = (float) $v1[$i] - (float) $v2[$i];
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

        if (!$user instanceof Utilisateur) {
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

        if (!$user instanceof Utilisateur) {
            return $this->redirectToRoute('app_login');
        }

        $roles = $user->getRoles();

        if (!in_array('ROLE_ADMIN', $roles, true) && !in_array('ROLE_COMMERCANT', $roles, true)) {
            return $this->redirectToRoute('home');
        }

        return $this->render('security/login_choice.html.twig');
    }
}