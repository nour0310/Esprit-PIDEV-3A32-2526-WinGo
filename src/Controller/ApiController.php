<?php

namespace App\Controller;

use App\Service\GoogleTranslateTtsService;
use App\Service\TransformersSummaryService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class ApiController extends AbstractController
{
    #[Route('/api/tts', name: 'api_tts', methods: ['POST'])]
    public function tts(Request $request, GoogleTranslateTtsService $ttsService): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $text = isset($data['text']) ? (string) $data['text'] : '';
        $lang = isset($data['lang']) ? (string) $data['lang'] : 'fr';

        $audioData = $ttsService->synthesize($text, $lang);

        if ($audioData !== null) {
            return $this->json(['audioContent' => base64_encode($audioData)]);
        }

        return $this->json(['error' => 'Impossible de générer l\'audio'], 500);
    }

    #[Route('/api/summary/ai', name: 'api_summary_ai', methods: ['POST'])]
    public function aiSummary(Request $request, TransformersSummaryService $summaryService): JsonResponse
    {
        if (!$summaryService->isAvailable()) {
            return $this->json([
                'error' => 'Résumé IA indisponible : activez l\'extension PHP FFI (php.ini : extension=ffi et ffi.enable=true).',
            ], 503);
        }

        $data = json_decode($request->getContent(), true);
        $text = isset($data['text']) ? (string) $data['text'] : '';

        if (trim($text) === '') {
            return $this->json(['error' => 'Texte vide'], 400);
        }

        $summary = $summaryService->summarize($text);

        if ($summary !== null) {
            return $this->json(['summary' => $summary]);
        }

        return $this->json(['error' => 'Impossible de générer le résumé'], 500);
    }
}
