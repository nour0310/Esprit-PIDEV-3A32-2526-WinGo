<?php

namespace App\Controller;

use App\Entity\Utilisateur;
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;

class SecurityController extends AbstractController
{
    #[Route('/login', name: 'app_login')]
    public function login(AuthenticationUtils $authenticationUtils): Response
    {
        if ($this->getUser()) {
            return $this->redirectToRoute('app_dashboard_redirect');
        }

        $error = $authenticationUtils->getLastAuthenticationError();
        $lastUsername = $authenticationUtils->getLastUsername();

        return $this->render('security/login.html.twig', [
            'last_username' => $lastUsername,
            'error' => $error,
        ]);
    }

    #[Route('/register', name: 'app_register')]
    public function register(Request $request, EntityManagerInterface $em, UtilisateurRepository $repo): Response
    {
        if ($this->getUser()) {
            return $this->redirectToRoute('app_dashboard_redirect');
        }

        $error = null;

        if ($request->isMethod('POST')) {
            $email = trim($request->request->get('email', ''));
            $password = $request->request->get('password', '');
            $nom = trim($request->request->get('nom', ''));
            $prenom = trim($request->request->get('prenom', ''));
            $telephone = $request->request->get('telephone');
            $age = $request->request->get('age');

            if (empty($email) || empty($password) || empty($nom) || empty($prenom)) {
                $error = 'Tous les champs obligatoires doivent être remplis.';
            } elseif ($repo->findOneBy(['email' => $email])) {
                $error = 'Un compte avec cet email existe déjà.';
            } else {
                $user = new Utilisateur();
                $user->setNom($nom);
                $user->setPrenom($prenom);
                $user->setEmail($email);
                $user->setMotDePasse($password);
                $user->setType('CLIENT');
                $user->setTelephone($telephone ? (int) $telephone : null);
                $user->setAge($age ? (int) $age : null);

                $em->persist($user);
                $em->flush();

                $this->addFlash('success', 'Compte créé avec succès ! Vous pouvez maintenant vous connecter.');
                return $this->redirectToRoute('app_login');
            }
        }

        return $this->render('security/register.html.twig', ['error' => $error]);
    }

    #[Route('/logout', name: 'app_logout')]
    public function logout(): void {}

    #[Route('/dashboard-redirect', name: 'app_dashboard_redirect')]
    public function dashboardRedirect(): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        if (in_array('ROLE_ADMIN', $user->getRoles())) {
            return $this->redirectToRoute('admin_dashboard');
        }

        return $this->redirectToRoute('client_dashboard');
    }
}
