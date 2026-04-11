<?php

namespace App\Controller;

use App\Entity\Commentaire;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Rubix\ML\PersistentModel;
use Rubix\ML\Persisters\Filesystem;

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

    #[Route('/commentaire/{id}/update', name: 'app_commentaire_update', methods: ['POST'])]
    public function update(Request $request, Commentaire $commentaire, EntityManagerInterface $em): JsonResponse
    {
        if ($this->getUser() !== $commentaire->getUtilisateur() && !$this->isGranted('ROLE_ADMIN')) {
            return $this->json(['success' => false, 'message' => 'Non autorisé'], 403);
        }

        $data = json_decode($request->getContent(), true);
        if (isset($data['contenu'])) {
            $commentaire->setContenu($data['contenu']);
            $em->flush();
            return $this->json(['success' => true, 'contenu' => $commentaire->getContenu()]);
        }
        return $this->json(['success' => false], 400);
    }

    #[Route('/commentaire/{id}/delete', name: 'app_commentaire_delete', methods: ['POST'])]
    public function delete(Request $request, Commentaire $commentaire, EntityManagerInterface $em): Response
    {
        $articleId = $commentaire->getArticle()->getId();

        if ($this->getUser() !== $commentaire->getUtilisateur() && !$this->isGranted('ROLE_ADMIN')) {
             return $this->redirectToRoute('app_article_show', ['id' => $articleId]);
        }

        if ($this->isCsrfTokenValid('delete' . $commentaire->getId(), $request->request->get('_token'))) {
            $em->remove($commentaire);
            $em->flush();
        }
        return $this->redirectToRoute('app_article_show', ['id' => $articleId]);
    }

    #[Route('/commentaire/{id}/sentiment', name: 'app_commentaire_sentiment', methods: ['GET'])]
public function analyzeSentiment(Commentaire $commentaire): JsonResponse
{
    $contenu = trim($commentaire->getContenu());

    // 1. Règles absolues (force brute pour les cas simples)
    $positifsObligatoires = ['joli', 'jolie', 'nice', 'beau', 'belle', 'magnifique', 'top', 'génial', 'super', 'merveilleux', 'bravo', 'bien', 'agréable', 'cool', 'excellent'];
    $negatifsObligatoires = ['bad', 'jaime pas', 'j\'aime pas', 'nul', 'horrible', 'mauvais', 'décevant', 'inutile', 'pas bon', 'bof', 'naze', 'affreux'];
    $neutresObligatoires = ['ok', 'moyen', 'passable', 'quelconque', 'sans avis', 'mitigé', 'bof bof'];

    $contenuLower = mb_strtolower($contenu);

    foreach ($positifsObligatoires as $mot) {
        if (str_contains($contenuLower, $mot)) {
            return $this->json([
                'sentiment' => 'positif',
                'emoji'     => '😊',
                'text'      => 'Positif',
                'class'     => 'success',
            ]);
        }
    }
    foreach ($negatifsObligatoires as $mot) {
        if (str_contains($contenuLower, $mot)) {
            return $this->json([
                'sentiment' => 'negatif',
                'emoji'     => '😡',
                'text'      => 'Négatif',
                'class'     => 'danger',
            ]);
        }
    }
    foreach ($neutresObligatoires as $mot) {
        if (str_contains($contenuLower, $mot)) {
            return $this->json([
                'sentiment' => 'neutre',
                'emoji'     => '😐',
                'text'      => 'Neutre',
                'class'     => 'secondary',
            ]);
        }
    }

    // Mentions pures = neutre
    if (preg_match('/^@\w+$/', $contenu)) {
        return $this->json(['sentiment' => 'neutre', 'emoji' => '😐', 'text' => 'Neutre', 'class' => 'secondary']);
    }

    // 2. Modèle ML
    $modelPath = $this->getParameter('kernel.project_dir') . '/var/ml/sentiment.model';
    if (!file_exists($modelPath)) {
        return $this->json(['sentiment' => 'inconnu', 'label' => 'Modèle non disponible']);
    }

    $model = PersistentModel::load(new Filesystem($modelPath));
    $dataset = new \Rubix\ML\Datasets\Unlabeled([$contenu]);
    $prediction = $model->predict($dataset)[0];

    $labels = [
        'positif' => ['emoji' => '😊', 'text' => 'Positif', 'class' => 'success'],
        'negatif' => ['emoji' => '😡', 'text' => 'Négatif', 'class' => 'danger'],
        'neutre'  => ['emoji' => '😐', 'text' => 'Neutre', 'class' => 'secondary'],
    ];
    $result = $labels[$prediction] ?? $labels['neutre'];

    return $this->json(array_merge(['sentiment' => $prediction], $result));
}
}