<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class AISummaryService
{
    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly string $ollamaBaseUrl,
        private readonly string $ollamaModel,
    ) {
    }

    public function summarize(string $text): ?string
    {
        $text = trim(strip_tags($text));
        if (mb_strlen($text) < 200) {
            return 'Le texte est trop court pour être résumé (minimum 200 caractères).';
        }

        $prompt = <<<PROMPT
Tu es un assistant expert en résumé de texte. Résume le texte suivant en français en 3 à 5 phrases maximum, en conservant uniquement les informations essentielles.

Texte à résumer :
{$text}
PROMPT;

        try {
            $url = rtrim($this->ollamaBaseUrl, '/').'/api/generate';

            $response = $this->client->request('POST', $url, [
                'json' => [
                    'model' => $this->ollamaModel,
                    'prompt' => $prompt,
                    'stream' => false,
                    'options' => [
                        'temperature' => 0.3,
                        'num_predict' => 200,
                    ],
                ],
                'timeout' => 120,
            ]);

            $data = $response->toArray();
            $out = trim($data['response'] ?? '');

            return $out === '' ? null : $out;
        } catch (\Throwable) {
            return null;
        }
    }
}
