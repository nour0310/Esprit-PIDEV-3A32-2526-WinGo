<?php

namespace App\Controller;

use App\Entity\Article;
use App\Entity\Commentaire;
use App\Form\ArticleType;
use App\Form\CommentaireType;
use App\Repository\ArticleRepository;
use App\Repository\CommentaireRepository;
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\BinaryFileResponse;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\File\UploadedFile;
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

        // URL absolue ou protocol-relative (//cdn.example.com/...)
        if (str_starts_with($imageName, '//')) {
            return new RedirectResponse('https:' . $imageName);
        }
        if (filter_var($imageName, FILTER_VALIDATE_URL)) {
            return new RedirectResponse($imageName);
        }

        // Nettoyer le nom : si c'est un chemin Windows complet, extraire le nom du fichier
        $cleanName = basename(str_replace('\\', '/', $imageName));

        // Dossier principal où sont stockées les images (après upload)
        $projectDir = $this->getParameter('kernel.project_dir');
        $mainDir = $projectDir . DIRECTORY_SEPARATOR . 'public' . DIRECTORY_SEPARATOR . 'uploads' . DIRECTORY_SEPARATOR . 'articles';

        // Chemins alternatifs pour compatibilité avec l'ancien projet
        $altDirs = [
            $projectDir . DIRECTORY_SEPARATOR . 'public' . DIRECTORY_SEPARATOR . 'images' . DIRECTORY_SEPARATOR . 'uploads',
            $projectDir . DIRECTORY_SEPARATOR . 'public' . DIRECTORY_SEPARATOR . 'images',
        ];

        // Vérifier d'abord dans le dossier principal
        $path = $mainDir . DIRECTORY_SEPARATOR . $cleanName;
        if (is_file($path) && is_readable($path)) {
            return new BinaryFileResponse($path);
        }

        // Sinon, parcourir les dossiers alternatifs
        foreach ($altDirs as $dir) {
            $altPath = $dir . DIRECTORY_SEPARATOR . $cleanName;
            if (is_file($altPath) && is_readable($altPath)) {
                return new BinaryFileResponse($altPath);
            }
        }

        // Dernier recours : essayer de trouver le fichier avec son nom original (si différent du nom nettoyé)
        // Cela arrive si le nom en base contient des caractères spéciaux ou un chemin relatif
        $originalName = $imageName;
        $originalPath = $mainDir . DIRECTORY_SEPARATOR . $originalName;
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

    // ===================== LISTE DES ARTICLES (avec recherche, catégories, stats, articles populaires) =====================
    #[Route('/blog', name: 'blog')]
    public function blog(Request $request, ArticleRepository $articleRepository, CommentaireRepository $commentaireRepository): Response
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
        $articles = $qb->getQuery()->getResult();

        // Statistiques (globales, indépendantes des filtres)
        $totalArticles = $articleRepository->count([]);
        $totalCommentaires = $commentaireRepository->count([]);

        // Articles populaires (top 3 les plus commentés)
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
            'articles' => $articles,
            'searchQuery' => $searchQuery,
            'categoryFilter' => $categoryFilter,
            'categories' => $categories,
            'totalArticles' => $totalArticles,
            'totalCommentaires' => $totalCommentaires,
            'popularArticles' => $popularArticles,
        ]);
    }

    // ===================== LISTE DES ARTICLES (alternative, redirection) =====================
    #[Route('/articles', name: 'app_article_index')]
    public function index(ArticleRepository $articleRepository): Response
    {
        return $this->redirectToRoute('blog');
    }

    // ===================== AJOUTER UN ARTICLE =====================
    #[Route('/article/new', name: 'app_article_new')]
    public function new(Request $request, EntityManagerInterface $em, UtilisateurRepository $userRepo): Response
    {
        $article = new Article();
        $article->setDatePublication(new \DateTime());

        $auteur = $userRepo->find(1);
        if ($auteur) {
            $article->setAuteur($auteur);
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
                // Utilisation de getClientOriginalExtension() au lieu de guessExtension()
                $originalExtension = $imageFile->getClientOriginalExtension();
                $extension = $originalExtension ?: 'jpg';
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

    // ===================== AFFICHER UN ARTICLE ET SES COMMENTAIRES =====================
    #[Route('/article/{id}', name: 'app_article_show')]
    public function show(Request $request, Article $article, EntityManagerInterface $em, UtilisateurRepository $userRepo): Response
    {
        $commentaire = new Commentaire();
        $commentaire->setArticle($article);
        $commentaire->setDateCommentaire(new \DateTime());

        $utilisateur = $userRepo->find(1);
        if ($utilisateur) {
            $commentaire->setUtilisateur($utilisateur);
        }

        $form = $this->createForm(CommentaireType::class, $commentaire);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($commentaire);
            $em->flush();
            return $this->redirectToRoute('app_article_show', ['id' => $article->getId()]);
        }

        return $this->render('article/BlogDetails.html.twig', [
            'article' => $article,
            'commentForm' => $form->createView(),
        ]);
    }

    // ===================== MODIFIER UN ARTICLE =====================
    #[Route('/article/{id}/edit', name: 'app_article_edit')]
    public function edit(Request $request, Article $article, EntityManagerInterface $em): Response
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
                // Utilisation de getClientOriginalExtension()
                $originalExtension = $imageFile->getClientOriginalExtension();
                $extension = $originalExtension ?: 'jpg';
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
        if ($this->isCsrfTokenValid('delete' . $article->getId(), $request->request->get('_token'))) {
            $em->remove($article);
            $em->flush();
        }
        return $this->redirectToRoute('blog');
    }

    /**
     * Enregistre un upload : move() si possible, sinon copie depuis le temporaire
     * (certains environnements Windows font échouer is_uploaded_file() → isValid() false).
     */
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

    private function guessSafeImageExtension(UploadedFile $file): string
    {
        $ext = strtolower((string) $file->getClientOriginalExtension());
        if ($ext === '' && str_contains($file->getClientOriginalName(), '.')) {
            $ext = strtolower((string) pathinfo($file->getClientOriginalName(), PATHINFO_EXTENSION));
        }
        $ext = preg_replace('/[^a-z0-9]/', '', $ext) ?? '';
        return $ext !== '' ? $ext : 'jpg';
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