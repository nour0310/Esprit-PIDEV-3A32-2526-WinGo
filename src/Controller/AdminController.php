<?php

namespace App\Controller;

use App\Entity\Article;
use App\Entity\Commande;
use App\Entity\Commentaire;
use App\Entity\Profil;
use App\Entity\Utilisateur;
use App\Form\ArticleType;
use App\Form\UtilisateurType;
use App\Repository\ArticleRepository;
use App\Repository\CommandeRepository;
use App\Repository\CommentaireRepository;
use App\Repository\ProduitRepository;
use App\Repository\ProfilRepository;
use App\Repository\ReclamationRepository;
use App\Repository\ReservationRepository;
use App\Repository\SuggestionRepository;
use App\Repository\TransportRepository;
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\File\UploadedFile;
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
    #[Route('', name: 'admin_dashboard')]
    #[Route('/dashboard', name: 'admin_dashboard_page')]
    public function dashboard(
        UtilisateurRepository $userRepo,
        ArticleRepository $articleRepo,
        CommandeRepository $commandeRepo,
        ReservationRepository $reservationRepo,
        ReclamationRepository $reclamationRepo,
        CommentaireRepository $commentRepo,
        EntityManagerInterface $em
    ): Response {
        // Statistiques gÃ©nÃ©rales
        $totalArticles = count($articleRepo->findAll());
        $totalUsers = count($userRepo->findAll());
        $totalCommandes = count($commandeRepo->findAll());
        $totalReservations = count($reservationRepo->findAll());
        $totalReclamations = count($reclamationRepo->findAll());
        $totalCommentaires = count($commentRepo->findAll());

        // Statistiques dÃ©mographiques
        $statsAge = $userRepo->getAgeStats();
        $totalForPercent = array_sum($statsAge) ?: 1;
        $statsAgePercent = [];
        foreach ($statsAge as $key => $value) {
            $statsAgePercent[$key] = round(($value / $totalForPercent) * 100, 1);
        }

        $counts = $userRepo->getCountsByRole();

        return $this->render('admin/dashboard.html.twig', [
            'stats'               => $counts,
            'total_users'         => $totalUsers,
            'total_articles'      => $totalArticles,
            'total_commandes'     => $totalCommandes,
            'total_reservations'  => $totalReservations,
            'total_reclamations'  => $totalReclamations,
            'total_commentaires'  => $totalCommentaires,
            'stats_age'           => $statsAge,
            'stats_age_percent'   => $statsAgePercent,
            'recent_users'        => $userRepo->findBy([], ['id' => 'DESC'], 5),
            'recent_reclamations' => $reclamationRepo->findBy([], ['id' => 'DESC'], 5),
        ]);
    }

    #[Route('/dashboard-modern', name: 'admin_dashboard_modern')]
    public function dashboardModern(
        UtilisateurRepository $userRepo,
        ArticleRepository $articleRepo,
        CommandeRepository $commandeRepo,
        ReservationRepository $reservationRepo,
        ReclamationRepository $reclamationRepo,
        CommentaireRepository $commentRepo,
        EntityManagerInterface $em
    ): Response {
        // Statistiques générales
        $totalArticles = count($articleRepo->findAll());
        $totalUsers = count($userRepo->findAll());
        $totalCommandes = count($commandeRepo->findAll());
        $totalReservations = count($reservationRepo->findAll());
        $totalReclamations = count($reclamationRepo->findAll());
        $totalCommentaires = count($commentRepo->findAll());

        return $this->render('admin/dashboard_modern.html.twig', [
            'total_users'         => $totalUsers,
            'total_articles'      => $totalArticles,
            'total_commandes'     => $totalCommandes,
            'total_reservations'  => $totalReservations,
            'total_reclamations'  => $totalReclamations,
            'total_commentaires'  => $totalCommentaires,
            'recent_users'        => $userRepo->findBy([], ['id' => 'DESC'], 5),
            'recent_reclamations' => $reclamationRepo->findBy([], ['id' => 'DESC'], 5),
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

        // On injecte le score de fiabilitÃ© pour chaque utilisateur
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
            $this->addFlash('error', 'Aucun fichier sÃ©lectionnÃ©.');
            return $this->redirectToRoute('admin_users');
        }

        // VÃ©rification des erreurs de tÃ©lÃ©chargement
        if (!$photoFile->isValid()) {
            $errorMsg = match ($photoFile->getError()) {
                UPLOAD_ERR_INI_SIZE  => 'Le fichier est trop volumineux pour le serveur (max. 2 Mo).',
                UPLOAD_ERR_PARTIAL   => 'Le fichier n\'a Ã©tÃ© que partiellement tÃ©lÃ©chargÃ©.',
                UPLOAD_ERR_NO_FILE   => 'Aucun fichier n\'a Ã©tÃ© tÃ©lÃ©chargÃ©.',
                default              => 'Une erreur est survenue lors du tÃ©lÃ©chargement (' . $photoFile->getErrorMessage() . ').',
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
            $this->addFlash('error', 'La photo ne doit pas dÃ©passer 2 Mo.');
            return $this->redirectToRoute('admin_users');
        }

        $uploadDir = $this->getParameter('kernel.project_dir') . '/public/uploads/photos';
        if (!is_dir($uploadDir)) {
            mkdir($uploadDir, 0755, true);
        }

        // CrÃ©ation du profil si inexistant
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
        $this->addFlash('success', 'Photo mise Ã  jour avec succÃ¨s.');

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
            $this->addFlash('success', 'Photo supprimÃ©e.');
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

        $this->addFlash('success', sprintf('Le rÃ´le de %s a Ã©tÃ© mis Ã  jour.', $user->getFullName()));

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

            $this->addFlash('success', 'Utilisateur crÃ©Ã© avec succÃ¨s.');
            return $this->redirectToRoute('admin_users');
        }

        return $this->render('admin/user_form.html.twig', [
            'user' => $user,
            'form' => $form->createView(),
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

            $this->addFlash('success', 'Utilisateur mis Ã  jour avec succÃ¨s.');
            return $this->redirectToRoute('admin_users');
        }

        return $this->render('admin/user_form.html.twig', [
            'user' => $user,
            'form' => $form->createView(),
            'title' => 'Modifier ' . $user->getFullName(),
        ]);
    }

    #[Route('/user/{id}/delete', name: 'admin_user_delete', methods: ['POST'])]
    public function userDelete(Request $request, Utilisateur $user, EntityManagerInterface $em, UtilisateurRepository $repo): Response
    {
        if ($this->isCsrfTokenValid('delete' . $user->getId(), $request->request->get('_token'))) {
            
            // MÃ‰TIER AVANCÃ‰ : VÃ©rification avant suppression
            if (!$repo->canBeSafelyDeleted($user)) {
                $this->addFlash('error', 'Cet utilisateur ne peut pas Ãªtre supprimÃ© car il a des commandes en cours ou est le dernier administrateur.');
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
            $this->addFlash('success', 'Utilisateur supprimÃ©.');
        } else {
            $this->addFlash('error', 'Token CSRF invalide.');
        }

        return $this->redirectToRoute('admin_users');
    }

    #[Route('/articles', name: 'admin_articles')]
    public function articles(
        ArticleRepository $repo,
        CommentaireRepository $commentRepo,
        Request $request,
        EntityManagerInterface $em
    ): Response {
        $search = $request->query->get('search', '');
        $connection = $em->getConnection();

        if ($search) {
            $query = $em->createQuery("
                SELECT a 
                FROM App\Entity\Article a 
                WHERE a.titre LIKE :search 
                OR a.contenu LIKE :search 
                ORDER BY a.id DESC
            ");
            $query->setParameter('search', '%' . $search . '%');
            $articles = $query->getResult();
        } else {
            $articles = $repo->findBy([], ['id' => 'DESC']);
        }

        $totalCommentaires = $commentRepo->count([]);

        // Articles par mois (6 derniers mois) – SQL natif
        $articlesParMois = $connection->executeQuery("
            SELECT CONCAT(YEAR(a.date_publication), '-', LPAD(MONTH(a.date_publication), 2, '0')) as mois, COUNT(a.id) as nb
            FROM article a
            WHERE a.date_publication >= :date
            GROUP BY mois
            ORDER BY mois ASC
        ", ['date' => (new \DateTime('-6 months'))->format('Y-m-d')])->fetchAllAssociative();

        // Articles par catégorie – SQL natif
        $articlesParCategorie = $connection->executeQuery("
            SELECT a.categorie, COUNT(a.id) as nb
            FROM article a
            WHERE a.categorie IS NOT NULL AND a.categorie != ''
            GROUP BY a.categorie
        ")->fetchAllAssociative();

        // Commentaires par mois (6 derniers mois) – SQL natif
        $commentairesParMois = $connection->executeQuery("
            SELECT CONCAT(YEAR(c.date_commentaire), '-', LPAD(MONTH(c.date_commentaire), 2, '0')) as mois, COUNT(c.id) as nb
            FROM commentaire c
            WHERE c.date_commentaire >= :date
            GROUP BY mois
            ORDER BY mois ASC
        ", ['date' => (new \DateTime('-6 months'))->format('Y-m-d H:i:s')])->fetchAllAssociative();

        // Likes par mois (6 derniers mois) – SQL natif
        $likesParMois = $connection->executeQuery("
            SELECT CONCAT(YEAR(l.date_like), '-', LPAD(MONTH(l.date_like), 2, '0')) as mois, COUNT(l.id) as nb
            FROM likes l
            WHERE l.date_like >= :date
            GROUP BY mois
            ORDER BY mois ASC
        ", ['date' => (new \DateTime('-6 months'))->format('Y-m-d H:i:s')])->fetchAllAssociative();

        // Statistiques par région - SQL natif
        $regionStats = $connection->executeQuery("
            SELECT a.region, COUNT(a.id) as count
            FROM article a
            WHERE a.region IS NOT NULL AND a.region != ''
            GROUP BY a.region
            ORDER BY count DESC
        ")->fetchAllAssociative();

        // Article avec le plus de commentaires
        $articleMaxComs = null;
        $maxComs = -1;
        foreach ($articles as $a) {
            $count = count($a->getCommentaires());
            if ($count > $maxComs) {
                $maxComs = $count;
                $articleMaxComs = $a;
            }
        }

        return $this->render('admin/articles.html.twig', [
            'articles'             => $articles,
            'total_articles'       => count($articles),
            'total_commentaires'   => $totalCommentaires,
            'article_top'          => $articleMaxComs,
            'search'               => $search,
            'region_stats'         => $regionStats,
            'articlesParMois'      => $articlesParMois,
            'articlesParCategorie' => $articlesParCategorie,
            'commentairesParMois'  => $commentairesParMois,
            'likesParMois'         => $likesParMois,
        ]);
    }

    #[Route('/articles-with-charts', name: 'admin_articles_with_charts')]
    public function articlesWithCharts(
        ArticleRepository $repo,
        CommentaireRepository $commentRepo,
        Request $request,
        EntityManagerInterface $em
    ): Response {
        $search = $request->query->get('search', '');
        $connection = $em->getConnection();

        if ($search) {
            $query = $em->createQuery("
                SELECT a 
                FROM App\Entity\Article a 
                WHERE a.titre LIKE :search 
                OR a.contenu LIKE :search 
                ORDER BY a.id DESC
            ");
            $query->setParameter('search', '%' . $search . '%');
            $articles = $query->getResult();
        } else {
            $articles = $repo->findBy([], ['id' => 'DESC']);
        }

        $totalCommentaires = $commentRepo->count([]);

        // Articles par mois – SQL natif
        $articlesParMois = $connection->executeQuery("
            SELECT CONCAT(YEAR(a.date_publication), '-', LPAD(MONTH(a.date_publication), 2, '0')) as mois, COUNT(a.id) as nb
            FROM article a
            WHERE a.date_publication >= :date
            GROUP BY mois
            ORDER BY mois ASC
        ", ['date' => (new \DateTime('-6 months'))->format('Y-m-d')])->fetchAllAssociative();

        // Articles par catégorie – SQL natif
        $articlesParCategorie = $connection->executeQuery("
            SELECT a.categorie, COUNT(a.id) as nb
            FROM article a
            WHERE a.categorie IS NOT NULL AND a.categorie != ''
            GROUP BY a.categorie
        ")->fetchAllAssociative();

        // Commentaires par mois – SQL natif
        $commentairesParMois = $connection->executeQuery("
            SELECT CONCAT(YEAR(c.date_commentaire), '-', LPAD(MONTH(c.date_commentaire), 2, '0')) as mois, COUNT(c.id) as nb
            FROM commentaire c
            WHERE c.date_commentaire >= :date
            GROUP BY mois
            ORDER BY mois ASC
        ", ['date' => (new \DateTime('-6 months'))->format('Y-m-d H:i:s')])->fetchAllAssociative();

        // Likes par mois – SQL natif
        $likesParMois = $connection->executeQuery("
            SELECT CONCAT(YEAR(l.date_like), '-', LPAD(MONTH(l.date_like), 2, '0')) as mois, COUNT(l.id) as nb
            FROM likes l
            WHERE l.date_like >= :date
            GROUP BY mois
            ORDER BY mois ASC
        ", ['date' => (new \DateTime('-6 months'))->format('Y-m-d H:i:s')])->fetchAllAssociative();

        // Article avec le plus de commentaires
        $articleMaxComs = null;
        $maxComs = -1;
        foreach ($articles as $a) {
            $count = count($a->getCommentaires());
            if ($count > $maxComs) {
                $maxComs = $count;
                $articleMaxComs = $a;
            }
        }

        return $this->render('admin/articles_with_charts.html.twig', [
            'articles'             => $articles,
            'total_articles'       => count($articles),
            'total_commentaires'   => $totalCommentaires,
            'article_top'          => $articleMaxComs,
            'search'               => $search,
            'articlesParMois'      => $articlesParMois,
            'articlesParCategorie' => $articlesParCategorie,
            'commentairesParMois'  => $commentairesParMois,
            'likesParMois'         => $likesParMois,
        ]);
    }

    #[Route('/article/{id}/delete', name: 'admin_article_delete', methods: ['POST'])]
    public function deleteArticle(Request $request, Article $article, EntityManagerInterface $em): Response
    {
        // ... rest of the code remains the same ...
        if ($this->isCsrfTokenValid('delete_admin' . $article->getId(), $request->request->get('_token'))) {
            $em->remove($article);
            $em->flush();
            $this->addFlash('success', 'Article et ses commentaires supprimés avec succès.');
        }
        return $this->redirectToRoute('admin_articles');
    }

    #[Route('/article/{id}/commentaires', name: 'admin_article_commentaires')]
    public function articleCommentaires(Article $article, \App\Service\ProfanityFilterService $profanityFilter): Response
    {
        $commentaires = $article->getCommentaires();

        // Analyser chaque commentaire avec le bundle devtrope/profanity-filter
        $badWordResults = [];
        $badWordCount = 0;
        foreach ($commentaires as $commentaire) {
            $contenu = $commentaire->getContenu() ?? '';
            $isProfane = $profanityFilter->containsProfanity($contenu);
            $badWordResults[$commentaire->getId()] = [
                'has_bad_words' => $isProfane,
                'severity' => $isProfane ? 'danger' : 'clean',
                'cleaned' => $isProfane ? $profanityFilter->clean($contenu) : $contenu,
            ];
            if ($isProfane) {
                $badWordCount++;
            }
        }

        return $this->render('admin/article_commentaires.html.twig', [
            'article' => $article,
            'commentaires' => $commentaires,
            'badWordResults' => $badWordResults,
            'badWordCount' => $badWordCount,
        ]);
    }

    #[Route('/commentaire/{id}/delete-admin', name: 'admin_commentaire_delete', methods: ['POST'])]
    public function deleteCommentaireAdmin(Request $request, Commentaire $commentaire, EntityManagerInterface $em): Response
    {
        $articleId = $commentaire->getArticle()->getId();
        if ($this->isCsrfTokenValid('delete_comment_admin' . $commentaire->getId(), $request->request->get('_token'))) {
            $em->remove($commentaire);
            $em->flush();
            $this->addFlash('success', 'Commentaire supprimé.');
        }
        return $this->redirectToRoute('admin_article_commentaires', ['id' => $articleId]);
    }

    #[Route('/article/{id}/show', name: 'admin_article_show', methods: ['GET'])]
    public function showArticle(Article $article): Response
    {
        return $this->render('admin/article_show.html.twig', [
            'article' => $article,
        ]);
    }

    #[Route('/article/{id}/edit', name: 'admin_article_edit')]
    public function editArticle(Request $request, Article $article, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(ArticleType::class, $article);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $imageFile = $form->get('image')->getData();
            if (!$imageFile instanceof UploadedFile) {
                $files = $request->files->get('article');
                if (\is_array($files) && isset($files['image']) && $files['image'] instanceof UploadedFile) {
                    $imageFile = $files['image'];
                }
            }
            if ($imageFile instanceof UploadedFile && $imageFile->isValid()) {
                $uploadDir = $this->getParameter('kernel.project_dir') . DIRECTORY_SEPARATOR . 'public' . DIRECTORY_SEPARATOR . 'uploads' . DIRECTORY_SEPARATOR . 'articles';
                if (!is_dir($uploadDir)) {
                    mkdir($uploadDir, 0777, true);
                }
                $previousImage = $article->getImage();
                $originalName = $imageFile->getClientOriginalName();
                $extension = pathinfo($originalName, PATHINFO_EXTENSION) ?: 'jpg';
                $newFilename = uniqid('', true) . '.' . $extension;
                $this->storeUploadedImage($imageFile, $uploadDir, $newFilename);
                $this->removeStoredArticleImage($previousImage, $uploadDir);
                $article->setImage($newFilename);
            }
            $em->flush();
            $this->addFlash('success', 'Article modifié avec succès.');
            return $this->redirectToRoute('admin_articles');
        }

        return $this->render('admin/article_edit.html.twig', [
            'form' => $form->createView(),
            'article' => $article,
        ]);
    }

    private function storeUploadedImage(UploadedFile $imageFile, string $uploadDir, string $newFilename): void
    {
        $target = $uploadDir . DIRECTORY_SEPARATOR . $newFilename;
        if ($imageFile->isValid()) {
            $imageFile->move($uploadDir, $newFilename);
            return;
        }
        if (\UPLOAD_ERR_OK !== $imageFile->getError()) {
            throw new FileException($imageFile->getErrorMessage());
        }
        $tmp = $imageFile->getPathname();
        if (!is_readable($tmp)) {
            throw new FileException('Fichier uploadé illisible.');
        }
        if (!@copy($tmp, $target)) {
            throw new FileException('Impossible d\'enregistrer l\'image.');
        }
        @chmod($target, 0666 & ~umask());
    }

    private function removeStoredArticleImage(?string $storedName, string $uploadDir): void
    {
        if (!\is_string($storedName) || $storedName === '') {
            return;
        }
        $storedName = trim($storedName);
        if (filter_var($storedName, FILTER_VALIDATE_URL) || str_starts_with($storedName, '//')) {
            return;
        }
        $base = basename(str_replace('\\', '/', $storedName));
        if ($base === '' || str_contains($base, '..')) {
            return;
        }
        $path = $uploadDir . DIRECTORY_SEPARATOR . $base;
        if (is_file($path) && is_readable($path)) {
            @unlink($path);
        }
    }

    #[Route('/reclamations', name: 'admin_reclamations')]
    public function reclamations(ReclamationRepository $repo): Response
    {
        return $this->render('admin/reclamations.html.twig', [
            'reclamations' => $repo->findAll(),
        ]);
    }

    #[Route('/produits', name: 'admin_produits')]
    public function produits(ProduitRepository $repo): Response
    {
        return $this->render('admin/produits.html.twig', [
            'produits' => $repo->findAll(),
        ]);
    }

    #[Route('/reservations', name: 'admin_reservations')]
    public function reservations(ReservationRepository $repo, Request $request): Response
    {
        $searchTerm = $request->query->get('search');
        $sortBy = $request->query->get('sort');

        $list = $repo->searchAndSortReservations($searchTerm, $sortBy);

        return $this->render('admin/reservations.html.twig', [
            'list'          => $list,
            'reservations'  => $list,
            'searchTerm'    => $searchTerm,
            'currentSort'   => $sortBy
        ]);
    }

    #[Route('/transports', name: 'admin_transports')]
    public function transports(TransportRepository $repo): Response
    {
        return $this->render('admin/transports.html.twig', [
            'transports' => $repo->findAll(),
        ]);
    }

    #[Route('/suggestions', name: 'admin_suggestions')]
    public function suggestions(SuggestionRepository $repo): Response
    {
        return $this->render('admin/suggestions.html.twig', [
            'suggestions' => $repo->findAll(),
        ]);
    }

    #[Route('/commandes', name: 'admin_commandes')]
    public function commandes(CommandeRepository $repo, UtilisateurRepository $userRepo): Response
    {
        $commandes = $repo->findBy([], ['id' => 'DESC']);
        return $this->render('admin/commandes.html.twig', [
            'commandes' => $commandes,
            'userRepo' => $userRepo,
        ]);
    }

    #[Route('/commande/{id}', name: 'admin_commande_details')]
    public function commandeDetails(Commande $commande, UtilisateurRepository $userRepo): Response
    {
        $items = json_decode($commande->getItemsJson() ?? '[]', true);

        return $this->render('admin/commande_details.html.twig', [
            'commande' => $commande,
            'items' => $items,
            'client' => $userRepo->find($commande->getIdUser()),
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