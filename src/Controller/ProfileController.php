<?php

namespace App\Controller;

use App\Entity\Profil;
use App\Entity\Utilisateur;
use App\Repository\ProfilRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\DependencyInjection\ParameterBag\ContainerBagInterface;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_USER')]
#[Route('/profile')]
class ProfileController extends AbstractController
{
    private const ALLOWED_PHOTO_EXTENSIONS = ['jpg', 'jpeg', 'png', 'webp', 'gif'];

    #[Route('', name: 'app_profile')]
    public function index(ProfilRepository $profilRepo): Response
    {
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);

        return $this->render('profile/index.html.twig', [
            'user' => $user,
            'profil' => $profil,
        ]);
    }

    #[Route('/edit', name: 'app_profile_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, EntityManagerInterface $em, ProfilRepository $profilRepo): Response
    {
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);

        if ($request->isMethod('POST')) {
            $nom = (string) $request->request->get('nom', $user->getNom());
            $prenom = (string) $request->request->get('prenom', $user->getPrenom());
            $telephone = $request->request->get('telephone');
            $age = $request->request->get('age');

            $user->setNom(trim($nom));
            $user->setPrenom(trim($prenom));
            $user->setTelephone($telephone ? (int) $telephone : null);
            $user->setAge($age ? (int) $age : null);

            if (!$profil) {
                $profil = new Profil();
                $profil->setUtilisateur($user);
                $em->persist($profil);
            }

            $bio = $request->request->get('bio');
            $profil->setBio($bio !== null ? (string) $bio : null);

            $photoFile = $request->files->get('photo');

            if ($photoFile instanceof UploadedFile) {
                $extension = strtolower($photoFile->getClientOriginalExtension());

                if (!in_array($extension, self::ALLOWED_PHOTO_EXTENSIONS, true)) {
                    $this->addFlash('error', 'Format de photo invalide. Utilisez JPG, PNG, WEBP ou GIF.');

                    return $this->redirectToRoute('app_profile_edit');
                }

                if ($photoFile->getSize() > 2 * 1024 * 1024) {
                    $this->addFlash('error', 'La photo ne doit pas dépasser 2 Mo.');

                    return $this->redirectToRoute('app_profile_edit');
                }

                $uploadDir = $this->getUploadDir();

                if (!is_dir($uploadDir)) {
                    mkdir($uploadDir, 0755, true);
                }

                if ($profil->getPhoto()) {
                    $oldPath = $uploadDir . '/' . $profil->getPhoto();

                    if (file_exists($oldPath)) {
                        unlink($oldPath);
                    }
                }

                $filename = uniqid('photo_', true) . '.' . $extension;
                $photoFile->move($uploadDir, $filename);
                $profil->setPhoto($filename);
            }

            $em->flush();

            $this->addFlash('success', 'Profil mis à jour avec succès.');

            return $this->redirectToRoute('app_profile');
        }

        return $this->render('profile/edit.html.twig', [
            'user' => $user,
            'profil' => $profil,
        ]);
    }

    #[Route('/upload-photo', name: 'app_profile_upload_photo', methods: ['POST'])]
    public function uploadPhoto(Request $request, EntityManagerInterface $em, ProfilRepository $profilRepo): Response
    {
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);
        $photoFile = $request->files->get('photo');

        if (!$photoFile instanceof UploadedFile) {
            $this->addFlash('error', 'Aucun fichier sélectionné.');

            return $this->redirectToRoute('app_profile');
        }

        $extension = strtolower($photoFile->getClientOriginalExtension());

        if (!in_array($extension, self::ALLOWED_PHOTO_EXTENSIONS, true)) {
            $this->addFlash('error', 'Format invalide. Utilisez JPG, PNG, WEBP ou GIF.');

            return $this->redirectToRoute('app_profile');
        }

        if ($photoFile->getSize() > 2 * 1024 * 1024) {
            $this->addFlash('error', 'La photo ne doit pas dépasser 2 Mo.');

            return $this->redirectToRoute('app_profile');
        }

        $uploadDir = $this->getUploadDir();

        if (!is_dir($uploadDir)) {
            mkdir($uploadDir, 0755, true);
        }

        if (!$profil) {
            $profil = new Profil();
            $profil->setUtilisateur($user);
            $em->persist($profil);
        }

        if ($profil->getPhoto()) {
            $oldPath = $uploadDir . '/' . $profil->getPhoto();

            if (file_exists($oldPath)) {
                unlink($oldPath);
            }
        }

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
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);

        if ($profil && $profil->getPhoto()) {
            $uploadDir = $this->getUploadDir();
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
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        $error = null;

        if ($request->isMethod('POST')) {
            $current = (string) $request->request->get('current_password', '');
            $new = (string) $request->request->get('new_password', '');
            $confirm = (string) $request->request->get('confirm_password', '');

            if ($current !== $user->getMotDePasse()) {
                $error = 'Mot de passe actuel incorrect.';
            } elseif ($new === '' || strlen($new) < 4) {
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
            'user' => $user,
            'error' => $error,
        ]);
    }

    #[Route('/delete', name: 'app_profile_delete', methods: ['POST'])]
    public function deleteAccount(
        Request $request,
        EntityManagerInterface $em,
        TokenStorageInterface $tokenStorage
    ): Response {
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        $token = (string) $request->request->get('_token', '');

        if ($this->isCsrfTokenValid('delete_account', $token)) {
            if ($user->getProfil() && $user->getProfil()->getPhoto()) {
                $photoPath = $this->getUploadDir() . '/' . $user->getProfil()->getPhoto();

                if (file_exists($photoPath)) {
                    unlink($photoPath);
                }
            }

            $request->getSession()->invalidate();
            $tokenStorage->setToken(null);

            $em->remove($user);
            $em->flush();

            $this->addFlash('success', 'Votre compte a été définitivement supprimé.');

            return $this->redirectToRoute('home');
        }

        $this->addFlash('error', 'Token CSRF invalide.');

        return $this->redirectToRoute('app_profile');
    }

    private function getUploadDir(): string
    {
        $projectDir = $this->getParameter('kernel.project_dir');

        if (!is_string($projectDir)) {
            throw new \RuntimeException('kernel.project_dir must be a string.');
        }

        return $projectDir . '/public/uploads/photos';
    }
}