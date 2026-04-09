<?php

namespace App\Controller;

use App\Entity\Article;
use App\Entity\Commentaire;
use App\Entity\Likes;                     // 🆕 SYSTEME DE LIKES
use App\Form\ArticleType;
use App\Form\CommentaireType;
use App\Repository\ArticleRepository;
use App\Repository\CommentaireRepository;
use App\Repository\LikesRepository;       // 🆕 SYSTEME DE LIKES
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\BinaryFileResponse;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\JsonResponse;  // 🆕 SYSTEME DE LIKES
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
        LikesRepository $likesRepo                  // 🆕 SYSTEME DE LIKES
    ): Response {
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
        $articles = $qb->getQuery()->getResult();

        // 🆕 SYSTEME DE LIKES : enrichir chaque article avec les infos de likes
        $user = $this->getUser();
        $articlesData = [];
        foreach ($articles as $article) {
            $likeCount = $likesRepo->countLikesForArticle($article->getId());
            $userLiked = $user ? $likesRepo->hasUserLiked($user->getId(), $article->getId()) : false;
            $articlesData[] = [
                'entity'      => $article,
                'likesCount'  => $likeCount,
                'userLiked'   => $userLiked,
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
            'articlesData'       => $articlesData,   // 🆕 on passe le tableau enrichi
            'searchQuery'        => $searchQuery,
            'categoryFilter'     => $categoryFilter,
            'categories'         => $categories,
            'totalArticles'      => $totalArticles,
            'totalCommentaires'  => $totalCommentaires,
            'popularArticles'    => $popularArticles,
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
        LikesRepository $likesRepo                  // 🆕 SYSTEME DE LIKES
    ): Response {
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

        // 🆕 SYSTEME DE LIKES : récupération des données pour la vue
        $likesCount = $likesRepo->countLikesForArticle($article->getId());
        $userLiked = $user ? $likesRepo->hasUserLiked($user->getId(), $article->getId()) : false;

        return $this->render('article/BlogDetails.html.twig', [
            'article'      => $article,
            'commentForm'  => $form->createView(),
            'likesCount'   => $likesCount,   // 🆕
            'userLiked'    => $userLiked,    // 🆕
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

    // ===================== 🆕 SYSTEME DE LIKES : ROUTES LIKE / UNLIKE =====================
    #[Route('/article/{id}/like', name: 'app_article_like', methods: ['POST'])]
    public function like(Article $article, EntityManagerInterface $em, LikesRepository $likesRepo): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->json(['success' => false, 'message' => 'Vous devez être connecté.'], 401);
        }

        // Vérifier si l'utilisateur a déjà liké
       if ($likesRepo->hasUserLiked($user->getId(), $article->getId())) {
            return $this->json(['success' => false, 'message' => 'Vous avez déjà liké cet article.']);
        }

        $like = new Likes();
        $like->setUtilisateurId($user->getId());
        $like->setArticleId($article->getId());
        $like->setDateLike(new \DateTime());

        $em->persist($like);
        $em->flush();

        $newCount = $likesRepo->countLikesForArticle($article->getId());

        return $this->json([
            'success' => true,
            'liked'   => true,
            'count'   => $newCount,
        ]);
    }

   #[Route('/article/{id}/unlike', name: 'app_article_unlike', methods: ['POST'])]
     public function unlike(Article $article, EntityManagerInterface $em, LikesRepository $likesRepo): JsonResponse
{
    $user = $this->getUser();
    if (!$user) {
        return $this->json(['success' => false, 'message' => 'Vous devez être connecté.'], 401);
    }

    $like = $likesRepo->findOneByUserAndArticle($user->getId(), $article->getId());
    if (!$like) {
        return $this->json(['success' => false, 'message' => 'Vous n\'avez pas liké cet article.']);
    }

    $em->remove($like);
    $em->flush();

    $newCount = $likesRepo->countLikesForArticle($article->getId());

    return $this->json([
        'success' => true,
        'liked'   => false,
        'count'   => $newCount,
    ]);
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