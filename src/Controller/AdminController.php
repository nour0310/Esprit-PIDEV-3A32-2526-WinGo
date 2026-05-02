<?php

namespace App\Controller;

use App\Entity\Article;
use App\Entity\Commande;
use App\Entity\Profil;
use App\Entity\Utilisateur;
use App\Form\ArticleType;
use App\Form\CommandeAnnulationType;
use App\Form\UtilisateurType;
use App\Repository\ArticleRepository;
use App\Repository\CommandeRepository;
use App\Repository\LikesRepository;
use App\Repository\NotificationCommerceRepository;
use App\Repository\ProduitRepository;
use App\Repository\ProfilRepository;
use App\Repository\ReclamationRepository;
use App\Repository\ReservationRepository;
use App\Repository\SuggestionRepository;
use App\Repository\TransportRepository;
use App\Repository\UtilisateurRepository;
use App\Service\CommandeMailerService;
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

        foreach ($allUsers as $user) {
            $age = $user->getAge();

            if ($age === null) {
                $statsAge['N/A']++;
            } elseif ($age < 18) {
                $statsAge['< 18']++;
            } elseif ($age <= 25) {
                $statsAge['18 - 25']++;
            } elseif ($age <= 40) {
                $statsAge['26 - 40']++;
            } elseif ($age <= 60) {
                $statsAge['41 - 60']++;
            } elseif ($age <= 80) {
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
            'total_users' => $totalAgeCount,
            'total_articles' => count($articleRepo->findAll()),
            'total_commandes' => count($commandeRepo->findAll()),
            'total_reservations' => count($reservationRepo->findAll()),
            'total_reclamations' => count($reclamationRepo->findAll()),
            'recent_users' => $userRepo->findBy([], ['id' => 'DESC'], 5),
            'recent_reclamations' => $reclamationRepo->findBy([], ['id' => 'DESC'], 5),
            'stats_age' => $statsAge,
            'stats_age_percent' => $statsAgePercent,
        ]);
    }

    #[Route('/users', name: 'admin_users')]
    public function users(Request $request, UtilisateurRepository $repo): Response
    {
        $query = (string) $request->query->get('q', '');
        $sort = (string) $request->query->get('sort', 'id');
        $direction = (string) $request->query->get('direction', 'DESC');

        $allowedSorts = ['id', 'nom', 'prenom', 'email', 'type', 'age'];

        if (!in_array($sort, $allowedSorts, true)) {
            $sort = 'id';
        }

        if (!in_array(strtoupper($direction), ['ASC', 'DESC'], true)) {
            $direction = 'DESC';
        }

        $users = $repo->searchAndSort($query, $sort, $direction);

        $totalReliability = 0;

        foreach ($users as $user) {
            $user->reliabilityScore = $repo->calculateReliabilityScore($user);
            $totalReliability += $user->reliabilityScore;
        }

        $avgReliability = count($users) > 0 ? round($totalReliability / count($users)) : 100;

        return $this->render('admin/users.html.twig', [
            'users' => $users,
            'q' => $query,
            'current_sort' => $sort,
            'current_direction' => $direction,
            'stats' => [
                'total' => count($repo->findAll()),
                'admins' => count($repo->findBy(['type' => 'ADMIN'])),
                'merchants' => count($repo->findBy(['type' => 'COMMERCANT'])),
                'avg_reliability' => $avgReliability,
            ],
        ]);
    }

    #[Route('/user/{id}/photo', name: 'admin_user_photo', methods: ['POST'])]
    public function uploadUserPhoto(
        int $id,
        Request $request,
        UtilisateurRepository $userRepo,
        ProfilRepository $profilRepo,
        EntityManagerInterface $em,
    ): Response {
        $user = $userRepo->find($id);

        if (!$user instanceof Utilisateur) {
            throw $this->createNotFoundException('Utilisateur introuvable.');
        }

        $photoFile = $request->files->get('photo');

        if (!$photoFile instanceof UploadedFile) {
            $this->addFlash('error', 'Aucun fichier sélectionné.');

            return $this->redirectToRoute('admin_users');
        }

        if (!$photoFile->isValid()) {
            $errorMsg = match ($photoFile->getError()) {
                UPLOAD_ERR_INI_SIZE => 'Le fichier est trop volumineux pour le serveur (max. 2 Mo).',
                UPLOAD_ERR_PARTIAL => 'Le fichier n\'a été que partiellement téléchargé.',
                UPLOAD_ERR_NO_FILE => 'Aucun fichier n\'a été téléchargé.',
                default => 'Une erreur est survenue lors du téléchargement (' . $photoFile->getErrorMessage() . ').',
            };

            $this->addFlash('error', $errorMsg);

            return $this->redirectToRoute('admin_users');
        }

        $extension = strtolower($photoFile->getClientOriginalExtension());

        if (!in_array($extension, self::ALLOWED_PHOTO_EXTENSIONS, true)) {
            $this->addFlash('error', 'Format invalide. Utilisez JPG, PNG, WEBP ou GIF.');

            return $this->redirectToRoute('admin_users');
        }

        if ($photoFile->getSize() > 2 * 1024 * 1024) {
            $this->addFlash('error', 'La photo ne doit pas dépasser 2 Mo.');

            return $this->redirectToRoute('admin_users');
        }

        $uploadDir = $this->getUploadsDir('photos');

        if (!is_dir($uploadDir)) {
            mkdir($uploadDir, 0755, true);
        }

        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);

        if (!$profil instanceof Profil) {
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

        $this->addFlash('success', 'Photo mise à jour avec succès.');

        return $this->redirectToRoute('admin_users');
    }

    #[Route('/user/{id}/photo/delete', name: 'admin_user_photo_delete', methods: ['POST'])]
    public function deleteUserPhoto(
        int $id,
        UtilisateurRepository $userRepo,
        ProfilRepository $profilRepo,
        EntityManagerInterface $em,
    ): Response {
        $user = $userRepo->find($id);

        if (!$user instanceof Utilisateur) {
            throw $this->createNotFoundException('Utilisateur introuvable.');
        }

        $profil = $profilRepo->findOneBy(['utilisateur' => $user]);

        if ($profil instanceof Profil && $profil->getPhoto()) {
            $uploadDir = $this->getUploadsDir('photos');
            $oldPath = $uploadDir . '/' . $profil->getPhoto();

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

        if (!$user instanceof Utilisateur) {
            throw $this->createNotFoundException('Utilisateur introuvable.');
        }

        $newType = strtoupper((string) $request->request->get('type', ''));
        $allowedTypes = ['CLIENT', 'COMMERCANT', 'ADMIN'];

        if (!in_array($newType, $allowedTypes, true)) {
            $this->addFlash('error', 'Type d\'utilisateur invalide.');

            return $this->redirectToRoute('admin_users');
        }

        $user->setType($newType);
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
            $plainPassword = (string) $form->get('plainPassword')->getData();
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
            $plainPassword = (string) $form->get('plainPassword')->getData();

            if ($plainPassword !== '') {
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
    public function userDelete(
        Request $request,
        Utilisateur $user,
        EntityManagerInterface $em,
        UtilisateurRepository $repo
    ): Response {
        $token = (string) $request->request->get('_token', '');

        if ($this->isCsrfTokenValid('delete' . $user->getId(), $token)) {
            if (!$repo->canBeSafelyDeleted($user)) {
                $this->addFlash('error', 'Cet utilisateur ne peut pas être supprimé car il a des commandes en cours ou est le dernier administrateur.');

                return $this->redirectToRoute('admin_users');
            }

            if ($user->getProfil() && $user->getProfil()->getPhoto()) {
                $photoPath = $this->getUploadsDir('photos') . '/' . $user->getProfil()->getPhoto();

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

    #[Route('/articles', name: 'admin_articles')]
    public function articles(
        Request $request,
        ArticleRepository $repo,
        \App\Repository\CommentaireRepository $commentaireRepo,
        LikesRepository $likesRepo
    ): Response {
        $search = (string) $request->query->get('search', '');

        if ($search !== '') {
            $allArticles = $repo->findAll();
            $articles = array_filter($allArticles, static function (Article $article) use ($search): bool {
                return stripos($article->getTitre(), $search) !== false
    || stripos($article->getContenu(), $search) !== false;
            });
        } else {
            $articles = $repo->findAll();
        }

        $articleTop = null;
        $maxComments = -1;

        foreach ($articles as $article) {
            $commentsCount = count($article->getCommentaires());

            if ($commentsCount > $maxComments) {
                $maxComments = $commentsCount;
                $articleTop = $article;
            }
        }

        $categories = [];
        $totalForStats = count($articles) ?: 1;

        foreach ($articles as $article) {
            $category = $article->getCategorie() ?: 'Non classé';
            $categories[$category] = ($categories[$category] ?? 0) + 1;
        }

        $articlesParCategorie = [];
        $colors = ['#fa9e1b', '#8d4fff', '#4CAF50', '#2196F3', '#E91E63'];
        $i = 0;

        foreach ($categories as $name => $count) {
            $articlesParCategorie[] = [
                'name' => $name,
                'categorie' => $name,
                'nb' => $count,
                'percent' => round(($count / $totalForStats) * 100),
                'color' => $colors[$i % count($colors)],
            ];

            $i++;
        }

        usort($articlesParCategorie, static fn (array $a, array $b): int => $b['nb'] <=> $a['nb']);

        $articlesMoisMap = [];

        foreach ($articles as $article) {
            $date = $article->getDatePublication();

            if ($date) {
                $month = $date->format('M Y');
                $articlesMoisMap[$month] = ($articlesMoisMap[$month] ?? 0) + 1;
            }
        }

        $articlesParMois = [];

        foreach ($articlesMoisMap as $month => $count) {
            $articlesParMois[] = [
                'mois' => $month,
                'nb' => $count,
            ];
        }

        $commentairesMoisMap = [];

        foreach ($commentaireRepo->findAll() as $commentaire) {
            $date = $commentaire->getDateCommentaire();

            if ($date) {
                $month = $date->format('M Y');
                $commentairesMoisMap[$month] = ($commentairesMoisMap[$month] ?? 0) + 1;
            }
        }

        $commentairesParMois = [];

        foreach ($commentairesMoisMap as $month => $count) {
            $commentairesParMois[] = [
                'mois' => $month,
                'nb' => $count,
            ];
        }

        $likesMoisMap = [];

        foreach ($likesRepo->findAll() as $like) {
            $date = $like->getDateLike();

            if ($date) {
                $month = $date->format('M Y');
                $likesMoisMap[$month] = ($likesMoisMap[$month] ?? 0) + 1;
            }
        }

        $likesParMois = [];

        foreach ($likesMoisMap as $month => $count) {
            $likesParMois[] = [
                'mois' => $month,
                'nb' => $count,
            ];
        }

        return $this->render('admin/articles.html.twig', [
            'articles' => $articles,
            'total_articles' => count($articles),
            'total_commentaires' => count($commentaireRepo->findAll()),
            'article_top' => $articleTop,
            'search' => $search,
            'articlesParCategorie' => $articlesParCategorie,
            'articlesParMois' => $articlesParMois,
            'commentairesParMois' => $commentairesParMois,
            'likesParMois' => $likesParMois,
        ]);
    }

    #[Route('/article/{id}/show', name: 'admin_article_show')]
    public function articleShow(Article $article): Response
    {
        return $this->render('admin/article_show.html.twig', [
            'article' => $article,
        ]);
    }

    #[Route('/article/{id}/edit', name: 'admin_article_edit')]
    public function articleEdit(Request $request, Article $article, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(ArticleType::class, $article);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $imageFile = $form->get('image')->getData();

            if (!$imageFile instanceof UploadedFile) {
                $files = $request->files->get('article');

                if (is_array($files) && isset($files['image']) && $files['image'] instanceof UploadedFile) {
                    $imageFile = $files['image'];
                }
            }

            if ($imageFile instanceof UploadedFile && $imageFile->isValid()) {
                $uploadDir = $this->getUploadsDir('articles');

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

        if (UPLOAD_ERR_OK !== $imageFile->getError()) {
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
        if ($storedName === null || $storedName === '') {
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

    #[Route('/article/{id}/commentaires', name: 'admin_article_commentaires')]
    public function articleCommentaires(Article $article, \App\Service\ProfanityFilterService $profanityFilter): Response
    {
        $commentaires = $article->getCommentaires();

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
            'badWordResults' => $badWordResults,
            'badWordCount' => $badWordCount,
        ]);
    }

    #[Route('/commentaire/{id}/delete', name: 'admin_commentaire_delete', methods: ['POST'])]
    public function commentaireDelete(Request $request, \App\Entity\Commentaire $commentaire, EntityManagerInterface $em): Response
    {
        $article = $commentaire->getArticle();
        $articleId = $article ? $article->getId() : null;

        $token = (string) $request->request->get('_token', '');

        if ($this->isCsrfTokenValid('delete_admin_comment' . $commentaire->getId(), $token)) {
            $em->remove($commentaire);
            $em->flush();

            $this->addFlash('success', 'Commentaire supprimé.');
        } else {
            $this->addFlash('error', 'Token CSRF invalide.');
        }

        if ($articleId) {
            return $this->redirectToRoute('admin_article_commentaires', [
                'id' => $articleId,
            ]);
        }

        return $this->redirectToRoute('admin_articles');
    }

    #[Route('/article/{id}/delete', name: 'admin_article_delete', methods: ['POST'])]
    public function articleDelete(Request $request, Article $article, EntityManagerInterface $em): Response
    {
        $token = (string) $request->request->get('_token', '');

        if ($this->isCsrfTokenValid('delete_admin' . $article->getId(), $token)) {
            $em->remove($article);
            $em->flush();

            $this->addFlash('success', 'Article supprimé avec succès.');
        } else {
            $this->addFlash('error', 'Token CSRF invalide.');
        }

        return $this->redirectToRoute('admin_articles');
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
    public function reservations(ReservationRepository $repo): Response
    {
        return $this->render('admin/reservations.html.twig', [
            'reservations' => $repo->findAll(),
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
        $annulationForms = [];
        $clients = [];

        foreach ($commandes as $commande) {
            $id = $commande->getId();

            $clients[$id] = $userRepo->find($commande->getIdUser());

            $form = $this->createForm(CommandeAnnulationType::class, null, [
                'action' => $this->generateUrl('admin_commande_annuler', ['id' => $id]),
                'method' => 'POST',
            ]);

            $annulationForms[$id] = $form->createView();
        }

        return $this->render('admin/commandes.html.twig', [
            'commandes' => $commandes,
            'clients' => $clients,
            'annulationForms' => $annulationForms,
            'userRepo' => $userRepo,
        ]);
    }

    #[Route('/commande/{id}', name: 'admin_commande_details')]
    public function commandeDetails(Commande $commande, UtilisateurRepository $userRepo): Response
    {
     $itemsJson = $commande->getItemsJson() ?? '[]';

/** @var array<mixed> $items */
$items = json_decode($itemsJson, true);

        return $this->render('admin/commande_details.html.twig', [
            'commande' => $commande,
            'items' => $items,
            'client' => $userRepo->find($commande->getIdUser()),
        ]);
    }

    #[Route('/commande/{id}/livrer', name: 'admin_commande_livrer', methods: ['POST'])]
    public function livrer(
        Request $request,
        Commande $commande,
        EntityManagerInterface $em,
        UtilisateurRepository $userRepo,
        CommandeMailerService $commandeMailer
    ): Response {
        $token = (string) $request->request->get('_token', '');

        if (!$this->isCsrfTokenValid('livrer_commande_' . $commande->getId(), $token)) {
            $this->addFlash('error', 'Token CSRF invalide.');

            return $this->redirectToRoute('admin_commandes');
        }

        if ($commande->getStatus() !== 'en_cours') {
            $this->addFlash('error', 'Cette commande ne peut plus être livrée.');

            return $this->redirectToRoute('admin_commandes');
        }

        $client = $userRepo->find($commande->getIdUser());

        if (!$client instanceof Utilisateur) {
            $this->addFlash('error', 'Client introuvable.');

            return $this->redirectToRoute('admin_commandes');
        }

        $commande->setStatus('livree');
        $em->flush();

        try {
            $commandeMailer->sendCommandeLivreeEmail($client, $commande);
            $this->addFlash('success', 'Commande marquée comme livrée et email envoyé.');
        } catch (\Throwable $e) {
            $this->addFlash('warning', 'Commande livrée, mais l\'email n\'a pas pu être envoyé : ' . $e->getMessage());
        }

        return $this->redirectToRoute('admin_commandes');
    }

    #[Route('/commande/{id}/annuler', name: 'admin_commande_annuler', methods: ['POST'])]
    public function annuler(
        Request $request,
        Commande $commande,
        EntityManagerInterface $em,
        UtilisateurRepository $userRepo,
        CommandeMailerService $commandeMailer
    ): Response {
        if ($commande->getStatus() !== 'en_cours') {
            $this->addFlash('error', 'Cette commande ne peut plus être annulée.');

            return $this->redirectToRoute('admin_commandes');
        }

        $form = $this->createForm(CommandeAnnulationType::class, null, [
            'action' => $this->generateUrl('admin_commande_annuler', ['id' => $commande->getId()]),
            'method' => 'POST',
        ]);

        $form->handleRequest($request);

        if (!$form->isSubmitted() || !$form->isValid()) {
            $this->addFlash('error', 'Veuillez choisir une cause d’annulation.');

            return $this->redirectToRoute('admin_commandes');
        }

        $data = $form->getData();
        $cause = is_array($data) && isset($data['cause_annulation'])
            ? (string) $data['cause_annulation']
            : '';

        if (trim($cause) === '') {
            $this->addFlash('error', 'Cause d’annulation obligatoire.');

            return $this->redirectToRoute('admin_commandes');
        }

        $client = $userRepo->find($commande->getIdUser());

        if (!$client instanceof Utilisateur) {
            $this->addFlash('error', 'Client introuvable.');

            return $this->redirectToRoute('admin_commandes');
        }

        $commande->setStatus('annulee');
        $commande->setCauseAnnulation($cause);
        $em->flush();

        try {
            $commandeMailer->sendCommandeAnnuleeEmail($client, $commande);
            $this->addFlash('success', 'Commande annulée et email envoyé au client.');
        } catch (\Throwable $e) {
            $this->addFlash('warning', 'Commande annulée, mais l\'email n\'a pas pu être envoyé : ' . $e->getMessage());
        }

        return $this->redirectToRoute('admin_commandes');
    }

    private function getProjectDir(): string
    {
        /** @var string $projectDir */
        $projectDir = $this->getParameter('kernel.project_dir');

        return $projectDir;
    }

    private function getUploadsDir(string $folder): string
    {
        return $this->getProjectDir() . '/public/uploads/' . $folder;
    }
}