<?php

namespace App\Controller;

use App\Entity\Commentaire;
use Doctrine\ORM\EntityManagerInterface;
use Sentiment\Analyzer;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Symfony\Component\String\Slugger\AsciiSlugger;

class CommentaireController extends AbstractController
{
    #[Route('/commentaire/{id}/edit', name: 'app_commentaire_edit', methods: ['GET'])]
    public function getCommentaire(Commentaire $commentaire): JsonResponse
    {
        if ($this->getUser() !== $commentaire->getUtilisateur() && !$this->isGranted('ROLE_ADMIN')) {
            return $this->json(['success' => false, 'message' => 'Non autorisé'], 403);
        }
        return $this->json(['contenu' => $commentaire->getContenu()]);
    }

    #[Route('/commentaire/{id}/delete', name: 'app_commentaire_delete', methods: ['POST'])]
    public function delete(Request $request, Commentaire $commentaire, EntityManagerInterface $em): Response
    {
        $article = $commentaire->getArticle();
        $articleId = $article->getId();
        $slugger = new AsciiSlugger();
        $slug = $slugger->slug($article->getTitre())->lower()->toString();

        if ($this->getUser() !== $commentaire->getUtilisateur() && !$this->isGranted('ROLE_ADMIN')) {
             return $this->redirectToRoute('app_article_show', ['id' => $articleId, 'slug' => $slug]);
        }

        if ($this->isCsrfTokenValid('delete' . $commentaire->getId(), $request->request->get('_token'))) {
            $em->remove($commentaire);
            $em->flush();
        }
        return $this->redirectToRoute('app_article_show', ['id' => $articleId, 'slug' => $slug]);
    }

    #[Route('/commentaire/{id}/sentiment', name: 'app_commentaire_sentiment', methods: ['GET'])]
    #[IsGranted('PUBLIC_ACCESS')]   // Rendre public pour l'affichage du sentiment
    public function analyzeSentiment(Commentaire $commentaire): JsonResponse
    {
        $contenu = trim($commentaire->getContenu());
        
        $analyzer = new Analyzer();
        $scores = $analyzer->getSentiment($contenu);
        
        $compound = $scores['compound'];
        
        if ($compound >= 0.05) {
            $sentiment = 'positif';
            $emoji = '😊';
            $text = 'Positif';
            $class = 'success';
        } elseif ($compound <= -0.05) {
            $sentiment = 'negatif';
            $emoji = '😡';
            $text = 'Négatif';
            $class = 'danger';
        } else {
            $sentiment = 'neutre';
            $emoji = '😐';
            $text = 'Neutre';
            $class = 'secondary';
        }
        
        return $this->json([
            'sentiment' => $sentiment,
            'emoji' => $emoji,
            'text' => $text,
            'class' => $class,
        ]);
    }
}