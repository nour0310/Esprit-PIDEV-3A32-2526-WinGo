<?php

namespace App\Service;

use Psr\Log\LoggerInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class AISummaryService
{
    private const OLLAMA_URL = 'http://localhost:11434/api/generate';

    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly LoggerInterface $logger,
        private readonly string $model = 'mistral'
    ) {
    }

    public function summarize(string $text): ?string
    {
        $text = trim(strip_tags($text));
        if (mb_strlen($text) < 200) {
            return "Le texte est trop court pour être résumé (minimum 200 caractères).";
        }

        $prompt = <<<PROMPT
Tu es un assistant expert en résumé de texte. Résume le texte suivant en français en 3 à 5 phrases maximum, en conservant uniquement les informations essentielles.

Texte à résumer :
$text
PROMPT;

        try {
            $response = $this->client->request('POST', self::OLLAMA_URL, [
                'json' => [
                    'model'  => $this->model,
                    'prompt' => $prompt,
                    'stream' => false,
                    'options' => [
                        'temperature' => 0.3,
                        'num_predict' => 200,
                    ],
                ],
                'timeout' => 90, // augmenté pour laisser le temps au modèle
            ]);

            $data = $response->toArray(false);
            $this->logger->info('Ollama response', ['data' => $data]);

            // Vérifier si la réponse contient le champ attendu
            if (isset($data['response']) && is_string($data['response']) && trim($data['response']) !== '') {
                return trim($data['response']);
            }

            // Parfois la réponse est dans 'message' ou autre (selon version)
            if (isset($data['message']['content']) && is_string($data['message']['content'])) {
                return trim($data['message']['content']);
            }

            // Si on a un champ 'error', le logger
            if (isset($data['error'])) {
                $this->logger->error('Ollama returned error', ['error' => $data['error']]);
            }

            return null;
        } catch (\Throwable $e) {
            $this->logger->error('Ollama request failed', ['exception' => $e->getMessage()]);
            return null;
        }
    }
}
