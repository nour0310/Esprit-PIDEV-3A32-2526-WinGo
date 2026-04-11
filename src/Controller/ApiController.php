<?php

namespace App\Controller;

use App\Service\AISummaryService;
use App\Service\GoogleTranslateTtsService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class ApiController extends AbstractController
{
    #[Route('/api/tts', name: 'api_tts', methods: ['POST'])]
    public function tts(Request $request, GoogleTranslateTtsService $ttsService): JsonResponse
    {
        try {
            @set_time_limit(300);

            $payload = json_decode($request->getContent(), true);
            if (!\is_array($payload)) {
                return $this->json(['error' => 'Corps de requête JSON invalide'], 400);
            }

            $text = isset($payload['text']) ? (string) $payload['text'] : '';
            $lang = isset($payload['lang']) ? (string) $payload['lang'] : 'fr';

            $audioData = $ttsService->synthesize($text, $lang);

            if ($audioData !== null) {
                return $this->json(['audioContent' => base64_encode($audioData)]);
            }

            return $this->json(['error' => 'Impossible de générer l\'audio (service externe ou réseau).'], 500);
        } catch (\Throwable $e) {
            return $this->json([
                'error' => 'Erreur technique lors de la synthèse vocale.',
                'detail' => $this->getParameter('kernel.debug') ? $e->getMessage() : null,
            ], 500);
        }
    }

    #[Route('/api/summary/ai', name: 'api_summary_ai', methods: ['POST'])]
    public function aiSummary(Request $request, AISummaryService $service): JsonResponse
    {
        @set_time_limit(300);

        $payload = json_decode($request->getContent(), true);
        if (!\is_array($payload)) {
            return $this->json(['error' => 'Corps de requête JSON invalide'], 400);
        }

        $text = isset($payload['text']) ? (string) $payload['text'] : '';
        if (trim($text) === '') {
            return $this->json(['error' => 'Texte vide'], 400);
        }

        $summary = $service->summarize($text);

        if ($summary !== null && $summary !== '') {
            return $this->json(['summary' => $summary]);
        }

        return $this->json([
            'error' => 'Résumé impossible. Le texte soumis est peut-être trop court ou ne peut pas être analysé.',
            'detail' => $this->getParameter('kernel.debug')
                ? 'Aucune phrase clé n\'a pu être extraite via l\'algorithme TextRank.'
                : null,
        ], 500);
    }
}
