<?php

namespace App\Controller;

use App\Entity\Profil;
use App\Repository\ProfilRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_USER')]
#[Route('/profile')]
class ProfileController extends AbstractController
{
    #[Route('', name: 'app_profile')]
    public function index(ProfilRepository $profilRepo): Response
    {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();
        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);

        return $this->render('profile/index.html.twig', [
            'user'   => $user,
            'profil' => $profil,
        ]);
    }

    #[Route('/edit', name: 'app_profile_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, EntityManagerInterface $em, ProfilRepository $profilRepo): Response
    {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();
        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);

        if ($request->isMethod('POST')) {
            $user->setNom($request->request->get('nom', $user->getNom()));
            $user->setPrenom($request->request->get('prenom', $user->getPrenom()));
            $user->setTelephone($request->request->get('telephone') ? (int) $request->request->get('telephone') : null);
            $user->setAge($request->request->get('age') ? (int) $request->request->get('age') : null);
            $user->setGenre($request->request->get('genre', $user->getGenre()));

            $bio = $request->request->get('bio');
            if ($bio !== null) {
                if (!$profil) {
                    $profil = new Profil();
                    $profil->setUtilisateur($user);
                    $em->persist($profil);
                }
                $profil->setBio($bio);
            }

            $em->flush();
            $this->addFlash('success', 'Profil mis à jour avec succès.');
            return $this->redirectToRoute('app_profile');
        }

        return $this->render('profile/edit.html.twig', [
            'user'   => $user,
            'profil' => $profil,
        ]);
    }

    #[Route('/change-password', name: 'app_profile_password', methods: ['GET', 'POST'])]
    public function changePassword(Request $request, EntityManagerInterface $em): Response
    {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();
        $error = null;

        if ($request->isMethod('POST')) {
            $current = $request->request->get('current_password');
            $new = $request->request->get('new_password');
            $confirm = $request->request->get('confirm_password');

            if ($current !== $user->getMotDePasse()) {
                $error = 'Mot de passe actuel incorrect.';
            } elseif (empty($new) || strlen($new) < 4) {
                $error = 'Le nouveau mot de passe doit contenir au moins 4 caractères.';
            } elseif ($new !== $confirm) {
                $error = 'Les mots de passe ne correspondent pas.';
            } else {
                $user->setMotDePasse($new);
                $em->flush();
                $this->addFlash('success', 'Mot de passe modifié avec succès.');
                return $this->redirectToRoute('app_profile');
            }
        }

        return $this->render('profile/change_password.html.twig', [
            'user'  => $user,
            'error' => $error,
        ]);
    }
}
