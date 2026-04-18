<?php

namespace App\Controller;

use App\Entity\Commande;
use App\Entity\Profil;
use App\Entity\Utilisateur;
use App\Form\UtilisateurType;
use App\Repository\ArticleRepository;
use App\Repository\CommandeRepository;
use App\Repository\ProduitRepository;
use App\Repository\ProfilRepository;
use App\Repository\ReclamationRepository;
use App\Repository\ReservationRepository;
use App\Repository\SuggestionRepository;
use App\Repository\TransportRepository;
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_ADMIN')]
#[Route('/admin')]
class AdminController extends AbstractController
{
    private const ALLOWED_PHOTO_EXTENSIONS = ['jpg', 'jpeg', 'png', 'webp', 'gif'];

    // ─────────────────────────────────────────────
    //  DASHBOARD
    // ─────────────────────────────────────────────

    #[Route('', name: 'admin_dashboard')]
    #[Route('/dashboard', name: 'admin_dashboard_page')]
    public function dashboard(
        UtilisateurRepository $userRepo,
        ArticleRepository $articleRepo,
        CommandeRepository $commandeRepo,
        ReservationRepository $reservationRepo,
        ReclamationRepository $reclamationRepo,
    ): Response {
        $allUsers = $userRepo->findAll();
        
        $statsAge = [
            '< 18' => 0,
            '18 - 25' => 0,
            '26 - 40' => 0,
            '41 - 60' => 0,
            '61 - 80' => 0,
            '> 80' => 0,
            'N/A' => 0,
        ];
        
        foreach ($allUsers as $u) {
            $age = $u->getAge();
            if ($age === null) {
                $statsAge['N/A']++;
            } elseif ($age < 18) {
                $statsAge['< 18']++;
            } elseif ($age >= 18 && $age <= 25) {
                $statsAge['18 - 25']++;
            } elseif ($age >= 26 && $age <= 40) {
                $statsAge['26 - 40']++;
            } elseif ($age >= 41 && $age <= 60) {
                $statsAge['41 - 60']++;
            } elseif ($age >= 61 && $age <= 80) {
                $statsAge['61 - 80']++;
            } else {
                $statsAge['> 80']++;
            }
        }
        
        $totalAgeCount = count($allUsers);
        $totalAgeFixed = $totalAgeCount > 0 ? $totalAgeCount : 1;
        
        $statsAgePercent = [];
        foreach ($statsAge as $key => $count) {
            $statsAgePercent[$key] = round(($count / $totalAgeFixed) * 100);
        }

        return $this->render('admin/dashboard.html.twig', [
            'total_users'         => $totalAgeCount,
            'total_articles'      => count($articleRepo->findAll()),
            'total_commandes'     => count($commandeRepo->findAll()),
            'total_reservations'  => count($reservationRepo->findAll()),
            'total_reclamations'  => count($reclamationRepo->findAll()),
            'recent_users'        => $userRepo->findBy([], ['id' => 'DESC'], 5),
            'recent_reclamations' => $reclamationRepo->findBy([], ['id' => 'DESC'], 5),
            'stats_age'           => $statsAge,
            'stats_age_percent'   => $statsAgePercent,
        ]);
    }

    // ─────────────────────────────────────────────
    //  UTILISATEURS
    // ─────────────────────────────────────────────

    #[Route('/users', name: 'admin_users')]
    public function users(Request $request, UtilisateurRepository $repo): Response
    {
        $query = $request->query->get('q', '');
        $sort = $request->query->get('sort', 'id');
        $direction = $request->query->get('direction', 'DESC');

        // Validation simple du tri
        $allowedSorts = ['id', 'nom', 'prenom', 'email', 'type', 'age'];
        if (!in_array($sort, $allowedSorts)) {
            $sort = 'id';
        }
        if (!in_array(strtoupper($direction), ['ASC', 'DESC'])) {
            $direction = 'DESC';
        }

        $users = $repo->searchAndSort($query, $sort, $direction);

        // On injecte le score de fiabilité pour chaque utilisateur
        $totalReliability = 0;
        foreach ($users as $user) {
            $user->reliabilityScore = $repo->calculateReliabilityScore($user);
            $totalReliability += $user->reliabilityScore;
        }

        $avgReliability = count($users) > 0 ? round($totalReliability / count($users)) : 100;

        return $this->render('admin/users.html.twig', [
            'users'             => $users,
            'q'                 => $query,
            'current_sort'      => $sort,
            'current_direction' => $direction,
            'stats' => [
                'total' => count($repo->findAll()),
                'admins' => count($repo->findBy(['type' => 'ADMIN'])),
                'merchants' => count($repo->findBy(['type' => 'COMMERCANT'])),
                'avg_reliability' => $avgReliability,
            ]
        ]);
    }

    /**
     * Upload ou remplacement de la photo d'un utilisateur par l'admin.
     */
    #[Route('/user/{id}/photo', name: 'admin_user_photo', methods: ['POST'])]
    public function uploadUserPhoto(
        int $id,
        Request $request,
        UtilisateurRepository $userRepo,
        ProfilRepository $profilRepo,
        EntityManagerInterface $em,
    ): Response {
        $user = $userRepo->find($id);
        if (!$user) {
            throw $this->createNotFoundException('Utilisateur introuvable.');
        }

        $photoFile = $request->files->get('photo');
        if (!$photoFile) {
            $this->addFlash('error', 'Aucun fichier sélectionné.');
            return $this->redirectToRoute('admin_users');
        }

        // Vérification des erreurs de téléchargement (ex: fichier trop gros pour le serveur)
        if (!$photoFile->isValid()) {
            $errorMsg = match ($photoFile->getError()) {
                UPLOAD_ERR_INI_SIZE  => 'Le fichier est trop volumineux pour le serveur (max. 2 Mo).',
                UPLOAD_ERR_PARTIAL   => 'Le fichier n\'a été que partiellement téléchargé.',
                UPLOAD_ERR_NO_FILE   => 'Aucun fichier n\'a été téléchargé.',
                default              => 'Une erreur est survenue lors du téléchargement (' . $photoFile->getErrorMessage() . ').',
            };
            $this->addFlash('error', $errorMsg);
            return $this->redirectToRoute('admin_users');
        }

        $extension = strtolower($photoFile->getClientOriginalExtension());
        if (!in_array($extension, self::ALLOWED_PHOTO_EXTENSIONS, true)) {
            $this->addFlash('error', 'Format invalide. Utilisez JPG, PNG, WEBP ou GIF.');
            return $this->redirectToRoute('admin_users');
        }

        // Validation taille (max 2 Mo)
        if ($photoFile->getSize() > 2 * 1024 * 1024) {
            $this->addFlash('error', 'La photo ne doit pas dépasser 2 Mo.');
            return $this->redirectToRoute('admin_users');
        }

        $uploadDir = $this->getParameter('kernel.project_dir') . '/public/uploads/photos';
        if (!is_dir($uploadDir)) {
            mkdir($uploadDir, 0755, true);
        }

        // Création du profil si inexistant
        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);
        if (!$profil) {
            $profil = new Profil();
            $profil->setUtilisateur($user);
            $em->persist($profil);
        }

        // Suppression ancienne photo
        if ($profil->getPhoto()) {
            $oldPath = $uploadDir . '/' . $profil->getPhoto();
            if (file_exists($oldPath)) {
                unlink($oldPath);
            }
        }

        // Sauvegarde nouvelle photo
        $filename = uniqid('photo_', true) . '.' . $extension;
        $photoFile->move($uploadDir, $filename);
        $profil->setPhoto($filename);

        $em->flush();
        $this->addFlash('success', 'Photo mise à jour avec succès.');

        return $this->redirectToRoute('admin_users');
    }

    /**
     * Suppression de la photo d'un utilisateur par l'admin.
     */
    #[Route('/user/{id}/photo/delete', name: 'admin_user_photo_delete', methods: ['POST'])]
    public function deleteUserPhoto(
        int $id,
        UtilisateurRepository $userRepo,
        ProfilRepository $profilRepo,
        EntityManagerInterface $em,
    ): Response {
        $user = $userRepo->find($id);
        if (!$user) {
            throw $this->createNotFoundException('Utilisateur introuvable.');
        }

        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);
        if ($profil && $profil->getPhoto()) {
            $uploadDir = $this->getParameter('kernel.project_dir') . '/public/uploads/photos';
            $oldPath   = $uploadDir . '/' . $profil->getPhoto();
            if (file_exists($oldPath)) {
                unlink($oldPath);
            }
            $profil->setPhoto(null);
            $em->flush();
            $this->addFlash('success', 'Photo supprimée.');
        }

        return $this->redirectToRoute('admin_users');
    }

    #[Route('/user/{id}/change-type', name: 'admin_change_user_type', methods: ['POST'])]
    public function changeType(
        int $id,
        Request $request,
        UtilisateurRepository $repo,
        EntityManagerInterface $em,
    ): Response {
        $user = $repo->find($id);
        if (!$user) {
            throw $this->createNotFoundException('Utilisateur introuvable.');
        }

        $newType = $request->request->get('type');
        $allowedTypes = ['CLIENT', 'COMMERCANT', 'ADMIN'];

        if (!in_array(strtoupper($newType), $allowedTypes, true)) {
            $this->addFlash('error', 'Type d\'utilisateur invalide.');
            return $this->redirectToRoute('admin_users');
        }

        $user->setType(strtoupper($newType));
        $em->flush();

        $this->addFlash('success', sprintf('Le rôle de %s a été mis à jour.', $user->getFullName()));

        return $this->redirectToRoute('admin_users');
    }

    #[Route('/user/new', name: 'admin_user_new', methods: ['GET', 'POST'])]
    public function userNew(
        Request $request,
        EntityManagerInterface $em,
        UserPasswordHasherInterface $passwordHasher
    ): Response {
        $user = new Utilisateur();
        $form = $this->createForm(UtilisateurType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $plainPassword = $form->get('plainPassword')->getData();
            $user->setMotDePasse($passwordHasher->hashPassword($user, $plainPassword));

            $em->persist($user);
            $em->flush();

            $this->addFlash('success', 'Utilisateur créé avec succès.');
            return $this->redirectToRoute('admin_users');
        }

        return $this->render('admin/user_form.html.twig', [
            'user' => $user,
            'form' => $form,
            'title' => 'Nouveau Utilisateur',
        ]);
    }

    #[Route('/user/{id}/edit', name: 'admin_user_edit', methods: ['GET', 'POST'])]
    public function userEdit(
        Utilisateur $user,
        Request $request,
        EntityManagerInterface $em,
        UserPasswordHasherInterface $passwordHasher
    ): Response {
        $form = $this->createForm(UtilisateurType::class, $user, ['is_edit' => true]);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $plainPassword = $form->get('plainPassword')->getData();
            if ($plainPassword) {
                $user->setMotDePasse($passwordHasher->hashPassword($user, $plainPassword));
            }

            $em->flush();

            $this->addFlash('success', 'Utilisateur mis à jour avec succès.');
            return $this->redirectToRoute('admin_users');
        }

        return $this->render('admin/user_form.html.twig', [
            'user' => $user,
            'form' => $form,
            'title' => 'Modifier ' . $user->getFullName(),
        ]);
    }

    #[Route('/user/{id}/delete', name: 'admin_user_delete', methods: ['POST'])]
    public function userDelete(Request $request, Utilisateur $user, EntityManagerInterface $em, UtilisateurRepository $repo): Response
    {
        if ($this->isCsrfTokenValid('delete' . $user->getId(), $request->request->get('_token'))) {
            
            // MÉTIER AVANCÉ : Vérification avant suppression
            if (!$repo->canBeSafelyDeleted($user)) {
                $this->addFlash('error', 'Cet utilisateur ne peut pas être supprimé car il a des commandes en cours ou est le dernier administrateur.');
                return $this->redirectToRoute('admin_users');
            }

            // Delete associated photo if exists
            if ($user->getProfil() && $user->getProfil()->getPhoto()) {
                $photoPath = $this->getParameter('kernel.project_dir') . '/public/uploads/photos/' . $user->getProfil()->getPhoto();
                if (file_exists($photoPath)) {
                    unlink($photoPath);
                }
            }

            $em->remove($user);
            $em->flush();
            $this->addFlash('success', 'Utilisateur supprimé.');
        } else {
            $this->addFlash('error', 'Token CSRF invalide.');
        }

        return $this->redirectToRoute('admin_users');
    }

    // ─────────────────────────────────────────────
    //  ARTICLES
    // ─────────────────────────────────────────────

    #[Route('/articles', name: 'admin_articles')]
    public function articles(ArticleRepository $repo, \App\Repository\CommentaireRepository $commentaireRepo): Response
    {
        $articles = $repo->findAll();
        
        return $this->render('admin/articles.html.twig', [
            'articles'           => $articles,
            'total_articles'     => count($articles),
            'total_commentaires' => count($commentaireRepo->findAll()),
        ]);
    }

    // ─────────────────────────────────────────────
    //  RÉCLAMATIONS
    // ─────────────────────────────────────────────

    #[Route('/reclamations', name: 'admin_reclamations')]
    public function reclamations(ReclamationRepository $repo): Response
    {
        return $this->render('admin/reclamations.html.twig', [
            'reclamations' => $repo->findAll(),
        ]);
    }

    // ─────────────────────────────────────────────
    //  PRODUITS
    // ─────────────────────────────────────────────

    #[Route('/produits', name: 'admin_produits')]
    public function produits(ProduitRepository $repo): Response
    {
        return $this->render('admin/produits.html.twig', [
            'produits' => $repo->findAll(),
        ]);
    }

    // ─────────────────────────────────────────────
    //  RÉSERVATIONS
    // ─────────────────────────────────────────────

    #[Route('/reservations', name: 'admin_reservations')]
    public function reservations(ReservationRepository $repo): Response
    {
        return $this->render('admin/reservations.html.twig', [
            'reservations' => $repo->findAll(),
        ]);
    }

    // ─────────────────────────────────────────────
    //  TRANSPORTS
    // ─────────────────────────────────────────────

    #[Route('/transports', name: 'admin_transports')]
    public function transports(TransportRepository $repo): Response
    {
        return $this->render('admin/transports.html.twig', [
            'transports' => $repo->findAll(),
        ]);
    }

    // ─────────────────────────────────────────────
    //  SUGGESTIONS
    // ─────────────────────────────────────────────

    #[Route('/suggestions', name: 'admin_suggestions')]
    public function suggestions(SuggestionRepository $repo): Response
    {
        return $this->render('admin/suggestions.html.twig', [
            'suggestions' => $repo->findAll(),
        ]);
    }

    // ─────────────────────────────────────────────
    //  COMMANDES
    // ─────────────────────────────────────────────

    #[Route('/commandes', name: 'admin_commandes')]
    public function commandes(CommandeRepository $repo, UtilisateurRepository $userRepo): Response
    {
        $commandes = $repo->findBy([], ['id' => 'DESC']);

        return $this->render('admin/commandes.html.twig', [
            'commandes' => $commandes,
            'userRepo'  => $userRepo,
        ]);
    }

    #[Route('/commande/{id}', name: 'admin_commande_details')]
    public function commandeDetails(Commande $commande, UtilisateurRepository $userRepo): Response
    {
        $items = json_decode($commande->getItemsJson() ?? '[]', true);

        return $this->render('admin/commande_details.html.twig', [
            'commande' => $commande,
            'items'    => $items,
            'client'   => $userRepo->find($commande->getIdUser()),
        ]);
    }

    #[Route('/commande/{id}/livrer', name: 'admin_commande_livrer', methods: ['POST'])]
    public function livrer(Commande $commande, EntityManagerInterface $em): Response
    {
        $commande->setStatus('livree');
        $em->flush();

        $this->addFlash('success', 'Commande marquée comme livrée.');

        return $this->redirectToRoute('admin_commandes');
    }

    #[Route('/commande/{id}/annuler', name: 'admin_commande_annuler', methods: ['POST'])]
    public function annuler(Commande $commande, EntityManagerInterface $em): Response
    {
        $commande->setStatus('annulee');
        $em->flush();

        $this->addFlash('success', 'Commande annulée.');

        return $this->redirectToRoute('admin_commandes');
    }
}