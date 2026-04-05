<?php

namespace App\Controller;

use App\Entity\Commentaire;
use App\Form\CommentaireType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class CommentaireController extends AbstractController
{
    #[Route('/commentaire/{id}/edit', name: 'app_commentaire_edit', methods: ['POST'])]
    public function edit(Request $request, Commentaire $commentaire, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(CommentaireType::class, $commentaire);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
        }
        return $this->redirectToRoute('app_article_show', ['id' => $commentaire->getArticle()->getId()]);
    }

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