<?php

namespace App\Controller;

use App\Entity\Reward;
use App\Entity\Utilisateur;
use App\Service\JeuService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_USER')]
#[Route('/jeu')]
class JeuController extends AbstractController
{
    public function __construct(
        private JeuService $jeuService
    ) {}

    #[Route('', name: 'jeu_index', methods: ['GET'])]
    public function index(): Response
    {
        $user = $this->getCurrentUtilisateur();
        $userId = $this->getCurrentUtilisateurId($user);

        $todayReward = $this->jeuService->findTodayReward($userId);

        return $this->render('jeu/index.html.twig', [
            'dejaJoue' => $todayReward !== null,
            'todayReward' => $todayReward,
        ]);
    }

    #[Route('/roue', name: 'jeu_roue', methods: ['POST'])]
    public function roue(Request $request): JsonResponse
    {
        return $this->handlePlay($request, Reward::TYPE_JEU_ROUE, 'jeu_roue');
    }

    #[Route('/cartes', name: 'jeu_cartes', methods: ['POST'])]
    public function cartes(Request $request): JsonResponse
    {
        return $this->handlePlay($request, Reward::TYPE_JEU_CARTES, 'jeu_cartes');
    }

    private function handlePlay(Request $request, string $typeJeu, string $csrfAction): JsonResponse
    {
        $token = (string) $request->request->get('_token', '');

        if (!$this->isCsrfTokenValid($csrfAction, $token)) {
            return $this->json(['error' => 'Token CSRF invalide.'], 403);
        }

        $user = $this->getCurrentUtilisateur();
        $userId = $this->getCurrentUtilisateurId($user);

        try {
            $reward = $this->jeuService->play($userId, $typeJeu);

            return $this->json([
                'success' => true,
                'rewardType' => $reward->getRewardType(),
                'rewardLabel' => $reward->getRewardLabel(),
                'hasPromotion' => $reward->hasPromotion(),
                'promoCode' => $reward->getPromotion()?->getCode(),
            ]);
        } catch (\RuntimeException $e) {
            return $this->json(['error' => $e->getMessage()], 400);
        }
    }

    private function getCurrentUtilisateur(): Utilisateur
    {
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        return $user;
    }

    private function getCurrentUtilisateurId(Utilisateur $user): int
    {
        $userId = $user->getId();

        if ($userId === null) {
            throw $this->createAccessDeniedException('Utilisateur invalide.');
        }

        return $userId;
    }
}