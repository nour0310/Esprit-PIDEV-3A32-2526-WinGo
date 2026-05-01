<?php

namespace App\Controller;

use App\Repository\UtilisateurRepository;
use App\Service\PanierPredictionService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_ADMIN')]
#[Route('/admin')]
class PanierPredictionController extends AbstractController
{
    public function __construct(
        private PanierPredictionService $predictionService,
        private UtilisateurRepository   $utilisateurRepository
    ) {}

    #[Route('/predictions', name: 'admin_predictions')]
    public function index(): Response
    {
        $users = $this->utilisateurRepository->findAll();

        // Pour chaque utilisateur → demande une prédiction au Service
        // array_map  : applique predictForUser() à chaque user
        // array_filter : supprime les null (users sans panier ou erreur Flask)
        // array_values : réindexe le tableau pour Twig
        $predictions = array_values(
            array_filter(
                array_map(
                    fn(object $user) => $this->predictionService->predictForUser($user),
                    $users
                )
            )
        );

        // Tri par probabilité croissante → les plus à risque d'abandon apparaissent en premier
        usort($predictions, fn(array $a, array $b) => $a['probabilite'] <=> $b['probabilite']);

        return $this->render('admin/predictions.html.twig', [
            'predictions' => $predictions,
        ]);
    }
}