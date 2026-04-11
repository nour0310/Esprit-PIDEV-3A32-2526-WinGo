<?php

namespace App\Controller;

use App\Service\AISummaryService;
use App\Service\GoogleTranslateTtsService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Contracts\HttpClient\Exception\HttpExceptionInterface;
use Symfony\Contracts\HttpClient\Exception\TransportExceptionInterface;

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
        try {
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
                'error' => 'Le modèle a renvoyé un résumé vide. Vérifiez qu\'Ollama répond et que le modèle est bien téléchargé (ollama pull mistral).',
            ], 502);
        } catch (TransportExceptionInterface $e) {
            return $this->ollamaTransportErrorResponse($e);
        } catch (HttpExceptionInterface $e) {
            return $this->ollamaHttpErrorResponse($e);
        } catch (\Throwable $e) {
            for ($c = $e; $c instanceof \Throwable; $c = $c->getPrevious()) {
                if ($c instanceof TransportExceptionInterface) {
                    return $this->ollamaTransportErrorResponse($c);
                }
                if ($c instanceof HttpExceptionInterface) {
                    return $this->ollamaHttpErrorResponse($c);
                }
            }

            return $this->json([
                'error' => 'Erreur technique lors du résumé.',
                'detail' => $this->getParameter('kernel.debug') ? $e->getMessage() : null,
            ], 500);
        }
    }

    private function ollamaTransportErrorResponse(TransportExceptionInterface $e): JsonResponse
    {
        return $this->json([
            'error' => 'Impossible de joindre Ollama. Démarrez l\'application Ollama (ou la commande « ollama serve »), puis vérifiez OLLAMA_BASE_URL dans le fichier .env (ex. http://127.0.0.1:11434).',
            'detail' => $this->getParameter('kernel.debug') ? $e->getMessage() : null,
        ], 503);
    }

    private function ollamaHttpErrorResponse(HttpExceptionInterface $e): JsonResponse
    {
        $detail = '';
        if ($this->getParameter('kernel.debug')) {
            $detail = $e->getMessage();
            try {
                $body = $e->getResponse()->getContent(false);
                if (\is_string($body) && $body !== '') {
                    $detail = $detail !== '' ? $detail."\n".$body : $body;
                }
            } catch (\Throwable) {
            }
        }

        return $this->json([
            'error' => 'Ollama a renvoyé une erreur HTTP (modèle manquant, requête invalide, etc.). Vérifiez « ollama list » et la variable OLLAMA_MODEL.',
            'detail' => $detail !== '' ? $detail : null,
        ], 502);
    }
}
