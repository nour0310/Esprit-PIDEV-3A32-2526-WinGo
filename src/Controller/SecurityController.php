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
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

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
    public function register(
        Request $request,
        EntityManagerInterface $em,
        UtilisateurRepository $repo,
        UserPasswordHasherInterface $passwordHasher
    ): Response {
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
                $error = 'Veuillez remplir tous les champs obligatoires (*).';
            } elseif (!preg_match('/^[a-zA-Zàáâãäåçèéêëìíîïðòóôõöùúûüýÿ\s-]+$/u', $nom)) {
                $error = 'Le nom ne doit contenir que des lettres.';
            } elseif (!preg_match('/^[a-zA-Zàáâãäåçèéêëìíîïðòóôõöùúûüýÿ\s-]+$/u', $prenom)) {
                $error = 'Le prénom ne doit contenir que des lettres.';
            } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
                $error = 'L\'adresse email saisie n\'est pas valide.';
            } elseif (!preg_match('/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/', $password)) {
                $error = 'Le mot de passe doit contenir au moins 8 caractères, incluant une majuscule, une minuscule et un chiffre.';
            } elseif ($age !== null && $age !== '' && $age < 18) {
                $error = 'Vous devez avoir au moins 18 ans pour vous inscrire.';
            } elseif ($repo->findOneBy(['email' => $email])) {
                $error = 'Un compte avec cette adresse email existe déjà.';
            } else {
                $user = new Utilisateur();
                $user->setNom($nom);
                $user->setPrenom($prenom);
                $user->setEmail($email);
                $user->setType('CLIENT');
                $user->setTelephone($telephone ? (int) $telephone : null);
                $user->setAge($age ? (int) $age : null);

                $hashedPassword = $passwordHasher->hashPassword($user, $password);
                $user->setMotDePasse($hashedPassword);

                $em->persist($user);
                $em->flush();

                $this->addFlash('success', 'Votre compte a été créé avec succès ! Connectez-vous maintenant.');
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

        return $this->redirectToRoute('home');
    }
}