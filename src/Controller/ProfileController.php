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
    private const ALLOWED_PHOTO_EXTENSIONS = ['jpg', 'jpeg', 'png', 'webp', 'gif'];

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
            // Mise à jour des infos de base
            $user->setNom($request->request->get('nom', $user->getNom()));
            $user->setPrenom($request->request->get('prenom', $user->getPrenom()));
            $user->setTelephone($request->request->get('telephone') ? (int) $request->request->get('telephone') : null);
            $user->setAge($request->request->get('age') ? (int) $request->request->get('age') : null);

            // Création du profil si inexistant
            if (!$profil) {
                $profil = new Profil();
                $profil->setUtilisateur($user);
                $em->persist($profil);
            }

            // Mise à jour de la bio
            $bio = $request->request->get('bio');
            if ($bio !== null) {
                $profil->setBio($bio);
            }

            // Upload de la photo
            $photoFile = $request->files->get('photo');
            if ($photoFile) {
                $extension = strtolower($photoFile->getClientOriginalExtension());
                if (!in_array($extension, self::ALLOWED_PHOTO_EXTENSIONS, true)) {
                    $this->addFlash('error', 'Format de photo invalide. Utilisez JPG, PNG, WEBP ou GIF.');
                    return $this->redirectToRoute('app_profile_edit');
                }

                // Validation de la taille (max 2 Mo)
                if ($photoFile->getSize() > 2 * 1024 * 1024) {
                    $this->addFlash('error', 'La photo ne doit pas dépasser 2 Mo.');
                    return $this->redirectToRoute('app_profile_edit');
                }

                // Création du dossier si nécessaire
                $uploadDir = $this->getParameter('kernel.project_dir') . '/public/uploads/photos';
                if (!is_dir($uploadDir)) {
                    mkdir($uploadDir, 0755, true);
                }

                // Suppression de l'ancienne photo
                if ($profil->getPhoto()) {
                    $oldPath = $uploadDir . '/' . $profil->getPhoto();
                    if (file_exists($oldPath)) {
                        unlink($oldPath);
                    }
                }

                // Sauvegarde de la nouvelle photo
                $filename = uniqid('photo_', true) . '.' . $extension;
                $photoFile->move($uploadDir, $filename);
                $profil->setPhoto($filename);
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

    #[Route('/upload-photo', name: 'app_profile_upload_photo', methods: ['POST'])]
    public function uploadPhoto(Request $request, EntityManagerInterface $em, ProfilRepository $profilRepo): Response
    {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();
        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);

        $photoFile = $request->files->get('photo');
        if (!$photoFile) {
            $this->addFlash('error', 'Aucun fichier sélectionné.');
            return $this->redirectToRoute('app_profile');
        }

        $extension = strtolower($photoFile->getClientOriginalExtension());
        if (!in_array($extension, self::ALLOWED_PHOTO_EXTENSIONS, true)) {
            $this->addFlash('error', 'Format invalide. Utilisez JPG, PNG, WEBP ou GIF.');
            return $this->redirectToRoute('app_profile');
        }

        // Validation de la taille (max 2 Mo)
        if ($photoFile->getSize() > 2 * 1024 * 1024) {
            $this->addFlash('error', 'La photo ne doit pas dépasser 2 Mo.');
            return $this->redirectToRoute('app_profile');
        }

        // Création du dossier si nécessaire
        $uploadDir = $this->getParameter('kernel.project_dir') . '/public/uploads/photos';
        if (!is_dir($uploadDir)) {
            mkdir($uploadDir, 0755, true);
        }

        // Création du profil si inexistant
        if (!$profil) {
            $profil = new Profil();
            $profil->setUtilisateur($user);
            $em->persist($profil);
        }

        // Suppression de l'ancienne photo
        if ($profil->getPhoto()) {
            $oldPath = $uploadDir . '/' . $profil->getPhoto();
            if (file_exists($oldPath)) {
                unlink($oldPath);
            }
        }

        // Sauvegarde de la nouvelle photo
        $filename = uniqid('photo_', true) . '.' . $extension;
        $photoFile->move($uploadDir, $filename);
        $profil->setPhoto($filename);

        $em->flush();
        $this->addFlash('success', 'Photo de profil mise à jour.');
        return $this->redirectToRoute('app_profile');
    }

    #[Route('/delete-photo', name: 'app_profile_delete_photo', methods: ['POST'])]
    public function deletePhoto(EntityManagerInterface $em, ProfilRepository $profilRepo): Response
    {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();
        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);

        if ($profil && $profil->getPhoto()) {
            $uploadDir = $this->getParameter('kernel.project_dir') . '/public/uploads/photos';
            $oldPath = $uploadDir . '/' . $profil->getPhoto();
            if (file_exists($oldPath)) {
                unlink($oldPath);
            }
            $profil->setPhoto(null);
            $em->flush();
            $this->addFlash('success', 'Photo supprimée.');
        }

        return $this->redirectToRoute('app_profile');
    }

    #[Route('/change-password', name: 'app_profile_password', methods: ['GET', 'POST'])]
    public function changePassword(Request $request, EntityManagerInterface $em): Response
    {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();
        $error = null;

        if ($request->isMethod('POST')) {
            $current = $request->request->get('current_password');
            $new     = $request->request->get('new_password');
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

    #[Route('/delete', name: 'app_profile_delete', methods: ['POST'])]
    public function deleteAccount(Request $request, EntityManagerInterface $em): Response
    {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        if ($this->isCsrfTokenValid('delete_account', $request->request->get('_token'))) {
            // Delete associated photo if exists
            if ($user->getProfil() && $user->getProfil()->getPhoto()) {
                $photoPath = $this->getParameter('kernel.project_dir') . '/public/uploads/photos/' . $user->getProfil()->getPhoto();
                if (file_exists($photoPath)) {
                    unlink($photoPath);
                }
            }

            // Mettre à jour la session en déconnectant l'utilisateur (via injection de TokenStorage, ou plus simplement invalidation session)
            $request->getSession()->invalidate();
            $this->container->get('security.token_storage')->setToken(null);

            $em->remove($user);
            $em->flush();
            $this->addFlash('success', 'Votre compte a été définitivement supprimé.');

            return $this->redirectToRoute('home');
        }

        $this->addFlash('error', 'Token CSRF invalide.');
        return $this->redirectToRoute('app_profile');
    }
}