<?php

namespace App\Controller;

use App\Entity\Commentaire;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class CommentaireController extends AbstractController
{
    // Récupérer le contenu d'un commentaire (GET)
    #[Route('/commentaire/{id}/edit', name: 'app_commentaire_edit', methods: ['GET'])]
    public function getCommentaire(Commentaire $commentaire): JsonResponse
    {
        return $this->json(['contenu' => $commentaire->getContenu()]);
    }

    // Mettre à jour un commentaire (AJAX POST)
    #[Route('/commentaire/{id}/update', name: 'app_commentaire_update', methods: ['POST'])]
    public function update(Request $request, Commentaire $commentaire, EntityManagerInterface $em): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        if (isset($data['contenu'])) {
            $commentaire->setContenu($data['contenu']);
            $em->flush();
            return $this->json(['success' => true, 'contenu' => $commentaire->getContenu()]);
        }
        return $this->json(['success' => false], 400);
    }

    // Supprimer un commentaire (POST avec redirection)
    #[Route('/commentaire/{id}/delete', name: 'app_commentaire_delete', methods: ['POST'])]
    public function delete(Request $request, Commentaire $commentaire, EntityManagerInterface $em): Response
    {
        $articleId = $commentaire->getArticle()->getId();
        if ($this->isCsrfTokenValid('delete' . $commentaire->getId(), $request->request->get('_token'))) {
            $em->remove($commentaire);
            $em->flush();
        }
        return $this->redirectToRoute('app_article_show', ['id' => $articleId]);
    }
}