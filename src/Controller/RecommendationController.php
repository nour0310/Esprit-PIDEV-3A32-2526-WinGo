<?php

namespace App\Controller;

use App\Form\RecommendationType;
use App\Repository\TransportRepository;
use App\Service\AIRecommendationService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class RecommendationController extends AbstractController
{
    #[Route('/recommendation', name: 'app_recommendation')]
public function index(
    Request $request,
    AIRecommendationService $ai,
    TransportRepository $repository
): Response {

    $form = $this->createForm(RecommendationType::class);
    $form->handleRequest($request);

    $top3 = [];

    if ($form->isSubmitted() && $form->isValid()) {

        $data = $form->getData();

        $budget = $data['budget'];
        $destination = $data['arrivee'];
        $typePreference = $data['type'];

        // 🇹🇳 STATIC TUNISIA TRANSPORT SYSTEM (NO DATABASE)
        $transports = [
            [
                'type' => 'Bus',
                'arrivee' => $destination,
                'tarif' => 20,
                'capacite' => 50
            ],
            [
                'type' => 'Louage',
                'arrivee' => $destination,
                'tarif' => 35,
                'capacite' => 8
            ],
            [
                'type' => 'Avion',
                'arrivee' => $destination,
                'tarif' => 200,
                'capacite' => 180
            ],
            [
                'type' => 'Bateau',
                'arrivee' => $destination,
                'tarif' => 120,
                'capacite' => 300
            ]
        ];

        $results = [];

        foreach ($transports as $t) {

            $score = 0;

            // 💰 Budget logic
            if ($budget == 'Low' && $t['tarif'] <= 30) $score += 5;
            if ($budget == 'Medium' && $t['tarif'] <= 100) $score += 5;
            if ($budget == 'High') $score += 5;

            // 🚗 Type preference logic
            if ($t['type'] == $typePreference) $score += 4;

            // 🇹🇳 Tunisia smart rule bonus
            if ($destination == 'Djerba' && $t['type'] == 'Bateau') $score += 2;

            // 📊 Capacity bonus (bigger transport = more stable)
            if ($t['capacite'] > 100) $score += 1;

            $results[] = [
                'transport' => $t,
                'score' => $score
            ];
        }

        // 🏆 SORT BY BEST MATCH
        usort($results, function ($a, $b) {
            return $b['score'] <=> $a['score'];
        });

        // TOP 3 RESULTS
        $top3 = array_slice($results, 0, 3);
    }

    return $this->render('recommendation/index.html.twig', [
        'form' => $form->createView(),
        'top3' => $top3
    ]);
}
public function applyTunisianContext($score, $type, $destination) {
    $hour = (int) date('H');
    
    // Rush hour penalty for Louages/Buses leaving Tunis
    if (($hour >= 16 && $hour <= 19) && $type == 'Louage') {
        $score *= 0.8; // Harder to find a seat
    }
    
    return $score;
}
}
