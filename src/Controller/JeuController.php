<?php

namespace App\Controller;

use App\Entity\Reward;
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

    /**
     * Page principale — affiche le choix roue/cartes.
     * knp-time-bundle est utilisé dans le Twig via :
     * {{ todayReward.playedAt | time_diff }} → "il y a 2 heures"
     */
    #[Route('', name: 'jeu_index', methods: ['GET'])]
    public function index(): Response
    {
        /** @var \App\Entity\Utilisateur $user */
        $user        = $this->getUser();
        $todayReward = $this->jeuService->findTodayReward($user->getId());

        return $this->render('jeu/index.html.twig', [
            'dejaJoue'    => $todayReward !== null,
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
    if (!$this->isCsrfTokenValid($csrfAction, $request->request->get('_token'))) {
        return $this->json(['error' => 'Token CSRF invalide.'], 403);
    }

    /** @var \App\Entity\Utilisateur $user */
    $user = $this->getUser();

    try {
        $reward = $this->jeuService->play($user->getId(), $typeJeu);

        return $this->json([
            'success'      => true,
            'rewardType'   => $reward->getRewardType(),
            'rewardLabel'  => $reward->getRewardLabel(),
            'hasPromotion' => $reward->hasPromotion(),
            'promoCode'    => $reward->getPromotion()?->getCode(),
        ]);

    } catch (\RuntimeException $e) {
        return $this->json(['error' => $e->getMessage()], 400);
    }
}

}