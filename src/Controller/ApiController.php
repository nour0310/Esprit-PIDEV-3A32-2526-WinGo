<?php

namespace App\Controller;

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
        $data = json_decode($request->getContent(), true);
        $text = isset($data['text']) ? (string) $data['text'] : '';
        $lang = isset($data['lang']) ? (string) $data['lang'] : 'fr';

        $audioData = $ttsService->synthesize($text, $lang);

        if ($audioData !== null) {
            return $this->json(['audioContent' => base64_encode($audioData)]);
        }

        return $this->json(['error' => 'Impossible de générer l\'audio'], 500);
    }
}
