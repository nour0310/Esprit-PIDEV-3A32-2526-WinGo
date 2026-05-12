<?php

namespace App\Controller;

use App\Entity\Article;
use App\Entity\Commentaire;
use App\Entity\Likes;
use App\Form\ArticleType;
use App\Form\CommentaireType;
use App\Repository\ArticleRepository;
use App\Repository\CommentaireRepository;
use App\Repository\LikesRepository;
use App\Repository\UtilisateurRepository;
use App\Service\NotificationService;
use Doctrine\ORM\EntityManagerInterface;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\BinaryFileResponse;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Symfony\Component\String\Slugger\AsciiSlugger;
use RateLimit\RateLimitBundle\Attribute\RateLimit;  // 🆕 Import de l'attribut


class ArticleController extends AbstractController
{
    // ===================== ROUTE POUR LES IMAGES =====================
    #[Route('/article/{id}/image', name: 'app_article_image', methods: ['GET'])]
    #[IsGranted('PUBLIC_ACCESS')]
    public function image(Article $article): Response
    {
        $imageName = (string) $article->getImage();
        $imageName = trim($imageName);

        if ($imageName === '') {
            return $this->defaultImage();
        }

        // 1. Si c'est une URL
        if (str_starts_with($imageName, 'http') || str_starts_with($imageName, '//')) {
            $url = str_starts_with($imageName, '//') ? 'https:' . $imageName : $imageName;
            return new RedirectResponse($url);
        }

        // 2. Si c'est un chemin absolu valide (souvent le cas avec Java)
        if (is_file($imageName) && is_readable($imageName)) {
            return new BinaryFileResponse($imageName);
        }

        // 3. Nettoyer le nom pour ne garder que le fichier
        $cleanName = basename(str_replace('\\', '/', $imageName));
        $projectDir = $this->getProjectDir();
        
        // Liste des dossiers où chercher
        $searchDirs = [
            $this->getArticleUploadDir(),
            $projectDir . '/public/uploads/photos',
            $projectDir . '/public/images/uploads',
            $projectDir . '/public/images',
            'C:/Users/MSI-THIN/Downloads/Esprit-PIDEV-3A32-2526-WinGo-prodSymfonyy/Esprit-PIDEV-3A32-2526-WinGo-prodSymfonyy/public/uploads/articles',
            'C:/Users/MSI-THIN/Downloads/fastoumet (3)/fastoumet/web/public/uploads/articles',
            'C:/Users/MSI-THIN/Downloads/fastoumet/fastoumet/web/public/uploads/articles',
        ];

        foreach ($searchDirs as $dir) {
            $path = $dir . DIRECTORY_SEPARATOR . $cleanName;
            if (is_file($path) && is_readable($path)) {
                return new BinaryFileResponse($path);
            }
        }

        // 4. Fallback final
        return $this->defaultImage();
    }

    private function defaultImage(): Response
    {
        $path = $this->getProjectDir()
    . DIRECTORY_SEPARATOR . 'public'
    . DIRECTORY_SEPARATOR . 'images'
    . DIRECTORY_SEPARATOR . 'placeholder.svg';
        if (is_file($path) && is_readable($path)) {
            return new BinaryFileResponse($path);
        }
        return new Response(
            '<svg xmlns="http://www.w3.org/2000/svg" width="800" height="400"><rect width="100%" height="100%" fill="#f3efff"/></svg>',
            200,
            ['Content-Type' => 'image/svg+xml']
        );
    }

    // ===================== LISTE DES ARTICLES =====================
    #[Route('/blog', name: 'blog')]
    #[IsGranted('PUBLIC_ACCESS')]
    public function blog(
        Request $request,
        ArticleRepository $articleRepository,
        CommentaireRepository $commentaireRepository,
        LikesRepository $likesRepository,
        UtilisateurRepository $utilisateurRepository,
        PaginatorInterface $paginator
    ): Response
    {
        $searchQuery = $request->query->get('q');
        $categoryFilter = $request->query->get('category');

        $qb = $articleRepository->createQueryBuilder('a');
        if ($searchQuery) {
            $qb->andWhere('a.titre LIKE :query OR a.contenu LIKE :query OR a.categorie LIKE :query')
               ->setParameter('query', '%' . $searchQuery . '%');
        }
        if ($categoryFilter) {
            $qb->andWhere('a.categorie = :category')
               ->setParameter('category', $categoryFilter);
        }
        $qb->orderBy('a.datePublication', 'DESC');

        $pagination = $paginator->paginate(
            $qb,
            $request->query->getInt('page', 1),
            3
        );

        $articlesData = [];

        /** @var \App\Entity\Utilisateur|null $currentUser */
        $currentUser = $this->getUser();
        $currentUserId = $currentUser ? $currentUser->getId() : null;
        foreach ($pagination as $article) {
            $articleId = $article->getId();
            if ($articleId === null) {
                continue;
            }

            $likesCount = $likesRepository->countLikesForArticle($articleId);
            $userLiked = $currentUserId ? $likesRepository->hasUserLiked($currentUserId, $articleId) : false;

            $likers = $likesRepository->findBy(['articleId' => $articleId], ['dateLike' => 'DESC']);
            $likerNames = [];
            foreach ($likers as $like) {
                $liker = $utilisateurRepository->find($like->getUtilisateurId());
                if ($liker) {
                    $likerNames[] = trim($liker->getPrenom() . ' ' . $liker->getNom());
                }
            }
            $likerNames = array_values(array_unique(array_filter($likerNames)));
            $likersText = empty($likerNames) ? 'Aucun like pour le moment' : implode(', ', $likerNames);

            $articlesData[] = [
                'entity' => $article,
                'likesCount' => $likesCount,
                'userLiked' => $userLiked,
                'likersText' => $likersText,
            ];
        }

        $totalArticles = $articleRepository->count([]);
        $totalCommentaires = $commentaireRepository->count([]);

        $popularArticles = $articleRepository->createQueryBuilder('a')
            ->leftJoin('a.commentaires', 'c')
            ->groupBy('a.id')
            ->orderBy('COUNT(c.id)', 'DESC')
            ->setMaxResults(3)
            ->getQuery()
            ->getResult();

        $categories = [
            'Aventure' => 'Aventure',
            'Culture' => 'Culture',
            'Gastronomie' => 'Gastronomie',
            'Détente' => 'Détente',
        ];

        return $this->render('article/BlogList.html.twig', [
            'articlesData' => $articlesData,
            'pagination' => $pagination,
            'searchQuery' => $searchQuery,
            'categoryFilter' => $categoryFilter,
            'categories' => $categories,
            'totalArticles' => $totalArticles,
            'totalCommentaires' => $totalCommentaires,
            'popularArticles' => $popularArticles,
        ]);
    }

    #[Route('/article/{id}/like', name: 'app_article_like', methods: ['POST'])]
    public function like(Article $article, EntityManagerInterface $em, LikesRepository $likesRepository, NotificationService $notificationService): JsonResponse
    {
        /** @var \App\Entity\Utilisateur|null $user */
        $user = $this->getUser();
        if (!$user || $user->getId() === null) {
            return new JsonResponse(['success' => false, 'message' => 'Non authentifié'], Response::HTTP_UNAUTHORIZED);
        }

        $articleId = $article->getId();
        if ($articleId === null) {
            return new JsonResponse(['success' => false, 'message' => 'Article invalide'], Response::HTTP_BAD_REQUEST);
        }

        $alreadyLiked = $likesRepository->findOneByUserAndArticle($user->getId(), $articleId);
        if (!$alreadyLiked) {
            $like = new Likes();
            $like->setUtilisateurId($user->getId());
            $like->setArticleId($articleId);
            $em->persist($like);
            $em->flush();

            /** @var \App\Entity\Utilisateur|null $auteur */
            $auteur = $article->getAuteur();
            if ($auteur && $auteur->getId() !== null && $auteur->getId() !== $user->getId()) {
                    $slugger = new AsciiSlugger();
                    $slug = $slugger->slug($article->getTitre())->lower()->toString();
                    $notificationService->create(
                        $auteur->getId(),
                        $user->getId(),
                        'like',
                        trim($user->getPrenom() . ' ' . $user->getNom()) . ' a aimé votre article.',
                        $this->generateUrl('app_article_show', ['id' => $articleId, 'slug' => $slug])
                    );
            }
        }

        return new JsonResponse([
            'success' => true,
            'liked' => true,
            'count' => $likesRepository->countLikesForArticle($articleId),
        ]);
    }

    #[Route('/article/{id}/unlike', name: 'app_article_unlike', methods: ['POST'])]
    public function unlike(Article $article, EntityManagerInterface $em, LikesRepository $likesRepository): JsonResponse
    {
        /** @var \App\Entity\Utilisateur|null $user */
        $user = $this->getUser();
        if (!$user || $user->getId() === null) {
            return new JsonResponse(['success' => false, 'message' => 'Non authentifié'], Response::HTTP_UNAUTHORIZED);
        }

        $articleId = $article->getId();
        if ($articleId === null) {
            return new JsonResponse(['success' => false, 'message' => 'Article invalide'], Response::HTTP_BAD_REQUEST);
        }

        $like = $likesRepository->findOneByUserAndArticle($user->getId(), $articleId);
        if ($like) {
            $em->remove($like);
            $em->flush();
        }

        return new JsonResponse([
            'success' => true,
            'liked' => false,
            'count' => $likesRepository->countLikesForArticle($articleId),
        ]);
    }

    #[Route('/articles', name: 'app_article_index')]
    public function index(ArticleRepository $articleRepository): Response
    {
        return $this->redirectToRoute('blog');
    }

    // ===================== AJOUTER UN ARTICLE =====================
    #[Route('/article/new', name: 'app_article_new')]
    #[IsGranted('IS_AUTHENTICATED_FULLY')]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $article = new Article();
        $article->setDatePublication(new \DateTime());

        /** @var \App\Entity\Utilisateur|null $user */
        $user = $this->getUser();
        if ($user) {
            $article->setAuteur($user);
        }

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
$uploadDir = $this->getArticleUploadDir();
                if (!is_dir($uploadDir)) {
                    mkdir($uploadDir, 0777, true);
                }
                $originalName = $imageFile->getClientOriginalName();
                $extension = pathinfo($originalName, PATHINFO_EXTENSION);
                $extension = $extension ?: 'jpg';
                $newFilename = uniqid('', true) . '.' . $extension;
                $this->storeUploadedImage($imageFile, $uploadDir, $newFilename);
                $article->setImage($newFilename);
            }
            $em->persist($article);
            $em->flush();
            return $this->redirectToRoute('blog');
        }
        if ($form->isSubmitted() && !$form->isValid()) {
            return $this->render('article/AjoutBlog.html.twig', [
                'form' => $form->createView(),
            ], new Response(null, 422));
        }

        return $this->render('article/AjoutBlog.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    // ===================== AFFICHER UN ARTICLE =====================
    #[Route('/article/{id}-{slug}', name: 'app_article_show', requirements: ['id' => '\d+', 'slug' => '.+'])]
    #[RateLimit(limit: 5, period: 60, identifier: 'ip')]  // 🆕 Limitation
    #[IsGranted('PUBLIC_ACCESS')]

    public function show(
        Request $request,
        Article $article,
        string $slug,
        EntityManagerInterface $em,
        CommentaireRepository $commentaireRepository,
        LikesRepository $likesRepository,
        UtilisateurRepository $utilisateurRepository,
        PaginatorInterface $paginator,
        NotificationService $notificationService
    ): Response
    {
        $slugger = new AsciiSlugger();
        $expectedSlug = $slugger->slug($article->getTitre())->lower()->toString();

        if ($slug !== $expectedSlug) {
            return $this->redirectToRoute('app_article_show', [
                'id' => $article->getId(),
                'slug' => $expectedSlug,
            ], 301);
        }
        $commentaire = new Commentaire();
        $commentaire->setArticle($article);
        $commentaire->setDateCommentaire(new \DateTime());

        $responseStatus = 200;
        /** @var \App\Entity\Utilisateur|null $user */
        $user = $this->getUser();
        if ($user) {
            $commentaire->setUtilisateur($user);
        }

        $form = $this->createForm(CommentaireType::class, $commentaire);
        if ($user !== null) {
            $form->handleRequest($request);

            if ($form->isSubmitted()) {
                if ($form->isValid()) {
                    $parentId = $form->has('parent_id') ? $form->get('parent_id')->getData() : null;
                    if ($parentId !== null && $parentId !== '') {
                        $parentComment = $commentaireRepository->find((int) $parentId);
                        if ($parentComment instanceof Commentaire && $parentComment->getArticle()?->getId() === $article->getId()) {
                            $commentaire->setParent($parentComment);
                        }
                    }
                    $em->persist($commentaire);
                    $em->flush();

                    if ($user->getId() !== null) {
                        $slugger = new AsciiSlugger();
                        $slugExpected = $slugger->slug($article->getTitre())->lower()->toString();
                        $articleUrl = $this->generateUrl('app_article_show', ['id' => $article->getId(), 'slug' => $slugExpected]);

                        /** @var \App\Entity\Utilisateur|null $auteur */
                        $auteur = $article->getAuteur();
                        if ($auteur && $auteur->getId() !== null && $auteur->getId() !== $user->getId()) {
                            $notificationService->create(
                                $auteur->getId(),
                                $user->getId(),
                                'commentaire',
                                trim($user->getPrenom() . ' ' . $user->getNom()) . ' a commenté votre article.',
                                $articleUrl
                            );
                        }

                       $parent = $commentaire->getParent();

if ($parent instanceof Commentaire) {
    $parentAuthor = $parent->getUtilisateur();
    $parentId = $parent->getId();

    if (
        $parentAuthor instanceof \App\Entity\Utilisateur
        && $parentAuthor->getId() !== null
        && $parentAuthor->getId() !== $user->getId()
        && $parentId !== null
    ) {
        $notificationService->create(
            $parentAuthor->getId(),
            $user->getId(),
            'reponse',
            trim($user->getPrenom() . ' ' . $user->getNom()) . ' a répondu à votre commentaire.',
            $articleUrl . '#comment-card-' . $parentId
        );
    }
}   
                    }
                    $slugger = new AsciiSlugger();
                    $slugExpected = $slugger->slug($article->getTitre())->lower()->toString();
                    return $this->redirectToRoute('app_article_show', ['id' => $article->getId(), 'slug' => $slugExpected]);
                } else {
                    // Si le formulaire est invalide, on ne redirige pas !
                    // On laisse le code continuer jusqu'au render() plus bas pour afficher les erreurs.
                    $responseStatus = 422;
                }
            }
        }

        $commentsQb = $commentaireRepository->createQueryBuilder('c')
            ->andWhere('c.article = :article')
            ->andWhere('c.parent IS NULL')
            ->setParameter('article', $article)
            ->orderBy('c.dateCommentaire', 'DESC');

        $commentsPagination = $paginator->paginate(
            $commentsQb,
            $request->query->getInt('commentsPage', 1),
            8,
            ['pageParameterName' => 'commentsPage']
        );

        $articleId = $article->getId() ?? 0;
        $likesCount = $articleId > 0 ? $likesRepository->countLikesForArticle($articleId) : 0;
        $likersNames = [];
        if ($articleId > 0) {
            $likers = $likesRepository->findBy(['articleId' => $articleId], ['dateLike' => 'DESC']);
            foreach ($likers as $like) {
                $liker = $utilisateurRepository->find($like->getUtilisateurId());
                if ($liker) {
                    $likersNames[] = trim($liker->getPrenom() . ' ' . $liker->getNom());
                }
            }
            $likersNames = array_values(array_unique(array_filter($likersNames)));
        }

        return $this->render('article/BlogDetails.html.twig', [
            'article' => $article,
            'commentForm' => $form->createView(),
            'likesCount' => $likesCount,
            'likersNames' => $likersNames,
            'commentsPagination' => $commentsPagination,
        ], new Response(null, $responseStatus));
    }

    // ===================== MODIFIER UN ARTICLE =====================
    #[Route('/article/{id}/edit', name: 'app_article_edit')]
    #[IsGranted('IS_AUTHENTICATED_FULLY')]
    public function edit(Request $request, Article $article, EntityManagerInterface $em): Response
    {
        if ($this->getUser() !== $article->getAuteur() && !$this->isGranted('ROLE_ADMIN')) {
            $this->addFlash('error', 'Vous n\'êtes pas autorisé à modifier cet article.');
            return $this->redirectToRoute('blog');
        }

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
               $uploadDir = $this->getArticleUploadDir();
                if (!is_dir($uploadDir)) {
                    mkdir($uploadDir, 0777, true);
                }
                $previousImage = $article->getImage();
                $originalName = $imageFile->getClientOriginalName();
                $extension = pathinfo($originalName, PATHINFO_EXTENSION);
                $extension = $extension ?: 'jpg';
                $newFilename = uniqid('', true) . '.' . $extension;
                $this->storeUploadedImage($imageFile, $uploadDir, $newFilename);
                $this->removeStoredArticleImage($previousImage, $uploadDir);
                $article->setImage($newFilename);
            }
            $em->persist($article);
            $em->flush();
            return $this->redirectToRoute('blog');
        }
        if ($form->isSubmitted() && !$form->isValid()) {
            return $this->render('article/EditBlog.html.twig', [
                'form' => $form->createView(),
                'article' => $article,
            ], new Response(null, 422));
        }

        return $this->render('article/EditBlog.html.twig', [
            'form' => $form->createView(),
            'article' => $article,
        ]);
    }

    // ===================== SUPPRIMER UN ARTICLE =====================
    #[Route('/article/{id}/delete', name: 'app_article_delete', methods: ['POST'])]
    #[IsGranted('IS_AUTHENTICATED_FULLY')]
    public function delete(Request $request, Article $article, EntityManagerInterface $em): Response
    {
        if ($this->getUser() !== $article->getAuteur() && !$this->isGranted('ROLE_ADMIN')) {
            $this->addFlash('error', 'Action non autorisée.');
            return $this->redirectToRoute('blog');
        }

$token = (string) $request->request->get('_token', '');

        if ($this->isCsrfTokenValid('delete' . $article->getId(), $token)) {
            $articleId = $article->getId();
            $conn = $em->getConnection();
            
            // Supprimer manuellement les données liées non mappées
            $conn->executeStatement('DELETE FROM likes WHERE article_id = ?', [$articleId]);
            $conn->executeStatement('DELETE FROM rating WHERE article_id = ?', [$articleId]);
            $conn->executeStatement('DELETE FROM favori WHERE article_id = ?', [$articleId]);
            
            $em->remove($article);
            $em->flush();
            $this->addFlash('success', 'L\'article a été supprimé ainsi que toutes ses données liées.');
        }
        return $this->redirectToRoute('blog');
    }

    // ===================== FONCTIONS PRIVÉES =====================
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
private function getProjectDir(): string
{
    /** @var string $projectDir */
    $projectDir = $this->getParameter('kernel.project_dir');

    return $projectDir;
}

private function getArticleUploadDir(): string
{
    return $this->getProjectDir()
        . DIRECTORY_SEPARATOR . 'public'
        . DIRECTORY_SEPARATOR . 'uploads'
        . DIRECTORY_SEPARATOR . 'articles';
}
}