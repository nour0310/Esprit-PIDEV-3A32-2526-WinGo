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
use App\Service\WeatherService;
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

class ArticleController extends AbstractController
{
    // ===================== ROUTE POUR LES IMAGES =====================
    #[Route('/article/{id}/image', name: 'app_article_image', methods: ['GET'])]
    public function image(Article $article): Response
    {
        $raw = $article->getImage();
        $imageName = \is_string($raw) ? trim($raw) : '';

        if ($imageName === '') {
            return $this->defaultImage();
        }

        // URL absolue ou protocol-relative
        if (str_starts_with($imageName, '//')) {
            return new RedirectResponse('https:' . $imageName);
        }
        if (filter_var($imageName, FILTER_VALIDATE_URL)) {
            return new RedirectResponse($imageName);
        }

        // Extraire le nom du fichier (si chemin Windows)
        $cleanName = basename(str_replace('\\', '/', $imageName));

        $projectDir = $this->getParameter('kernel.project_dir');
        $mainDir = $projectDir . DIRECTORY_SEPARATOR . 'public' . DIRECTORY_SEPARATOR . 'uploads' . DIRECTORY_SEPARATOR . 'articles';
        $altDirs = [
            $projectDir . DIRECTORY_SEPARATOR . 'public' . DIRECTORY_SEPARATOR . 'images' . DIRECTORY_SEPARATOR . 'uploads',
            $projectDir . DIRECTORY_SEPARATOR . 'public' . DIRECTORY_SEPARATOR . 'images',
        ];

        $path = $mainDir . DIRECTORY_SEPARATOR . $cleanName;
        if (is_file($path) && is_readable($path)) {
            return new BinaryFileResponse($path);
        }

        foreach ($altDirs as $dir) {
            $altPath = $dir . DIRECTORY_SEPARATOR . $cleanName;
            if (is_file($altPath) && is_readable($altPath)) {
                return new BinaryFileResponse($altPath);
            }
        }

        $originalPath = $mainDir . DIRECTORY_SEPARATOR . $imageName;
        if (is_file($originalPath) && is_readable($originalPath)) {
            return new BinaryFileResponse($originalPath);
        }

        return $this->defaultImage();
    }

    private function defaultImage(): Response
    {
        $path = $this->getParameter('kernel.project_dir') . DIRECTORY_SEPARATOR . 'public' . DIRECTORY_SEPARATOR . 'images' . DIRECTORY_SEPARATOR . 'placeholder.svg';
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
    public function like(Article $article, EntityManagerInterface $em, LikesRepository $likesRepository): JsonResponse
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
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $article = new Article();
        $article->setDatePublication(new \DateTime());

        /** @var \App\Entity\Utilisateur $user */
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
                $uploadDir = $this->getParameter('kernel.project_dir') . DIRECTORY_SEPARATOR . 'public' . DIRECTORY_SEPARATOR . 'uploads' . DIRECTORY_SEPARATOR . 'articles';
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
        return $this->render('article/AjoutBlog.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    // ===================== AFFICHER UN ARTICLE =====================
    #[Route('/article/{id}', name: 'app_article_show')]
    public function show(
        Request $request,
        Article $article,
        EntityManagerInterface $em,
        CommentaireRepository $commentaireRepository,
        LikesRepository $likesRepository,
        UtilisateurRepository $utilisateurRepository,
        PaginatorInterface $paginator,
        WeatherService $weatherService
    ): Response
    {
        $commentaire = new Commentaire();
        $commentaire->setArticle($article);
        $commentaire->setDateCommentaire(new \DateTime());

        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();
        if ($user) {
            $commentaire->setUtilisateur($user);
        }

        $form = $this->createForm(CommentaireType::class, $commentaire);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($commentaire);
            $em->flush();
            return $this->redirectToRoute('app_article_show', ['id' => $article->getId()]);
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
        $weather = $weatherService->getWeather($article->getRegion());

        return $this->render('article/BlogDetails.html.twig', [
            'article' => $article,
            'commentForm' => $form->createView(),
            'likesCount' => $likesCount,
            'likersNames' => $likersNames,
            'commentsPagination' => $commentsPagination,
            'weather' => $weather,
        ]);
    }

    // ===================== MODIFIER UN ARTICLE =====================
    #[Route('/article/{id}/edit', name: 'app_article_edit')]
    public function edit(Request $request, Article $article, EntityManagerInterface $em): Response
    {
        if ($this->getUser() !== $article->getAuteur() && !$this->isGranted('ROLE_ADMIN')) {
            $this->addFlash('error', 'Vous n\'êtes pas autorisé à modifier cet article.');
            return $this->redirectToRoute('blog');
        }

        // Les vérifications de null ne sont plus nécessaires si l'entité gère correctement les valeurs par défaut.
        // (Les setters acceptent ?string et convertissent null en chaîne vide)
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
        return $this->render('article/EditBlog.html.twig', [
            'form' => $form->createView(),
            'article' => $article,
        ]);
    }

    // ===================== SUPPRIMER UN ARTICLE =====================
    #[Route('/article/{id}/delete', name: 'app_article_delete', methods: ['POST'])]
    public function delete(Request $request, Article $article, EntityManagerInterface $em): Response
    {
        if ($this->getUser() !== $article->getAuteur() && !$this->isGranted('ROLE_ADMIN')) {
            $this->addFlash('error', 'Action non autorisée.');
            return $this->redirectToRoute('blog');
        }

        if ($this->isCsrfTokenValid('delete' . $article->getId(), $request->request->get('_token'))) {
            $em->remove($article);
            $em->flush();
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
}