<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\Exception\TransportExceptionInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class DeepLTranslationService
{
    public function __construct(
        private readonly HttpClientInterface $httpClient,
        private readonly string $deepLApiKey,
        private readonly string $deepLApiUrl
    ) {
    }

    /**
     * @return array{text: string, detected_source_language: string|null}
     */
    public function translate(string $text, string $targetLang, ?string $sourceLang = null): array
    {
        $text = trim($text);
        $targetLang = strtoupper(trim($targetLang));
        $sourceLang = $sourceLang ? strtoupper(trim($sourceLang)) : null;

        if ($text === '') {
            throw new \InvalidArgumentException('Le texte a traduire est obligatoire.');
        }

        if ($this->deepLApiKey === '') {
            throw new \RuntimeException('DEEPL_API_KEY est vide. Ajoutez votre cle API dans .env.local.');
        }

        $payload = [
            'text' => [$text],
            'target_lang' => $targetLang,
        ];

        if ($sourceLang) {
            $payload['source_lang'] = $sourceLang;
        }

        try {
            $response = $this->httpClient->request('POST', $this->deepLApiUrl, [
                'headers' => [
                    'Authorization' => 'DeepL-Auth-Key '.$this->deepLApiKey,
                    'Content-Type' => 'application/json',
                ],
                'json' => $payload,
            ]);

            $data = $response->toArray(false);
        } catch (TransportExceptionInterface $exception) {
            throw new \RuntimeException('Impossible de contacter DeepL: '.$exception->getMessage(), 0, $exception);
        }

        if (isset($data['message'])) {
            throw new \RuntimeException('Erreur DeepL: '.$data['message']);
        }

        if (!isset($data['translations'][0]['text'])) {
            throw new \RuntimeException('Reponse DeepL invalide.');
        }

        return [
            'text' => $data['translations'][0]['text'],
            'detected_source_language' => $data['translations'][0]['detected_source_language'] ?? null,
        ];
    }

    /**
     * @return array<string, string>
     */
    public function getSupportedTargets(): array
    {
        return [
            'AR' => 'Arabe',
            'DE' => 'Allemand',
            'EN' => 'Anglais',
            'ES' => 'Espagnol',
            'FR' => 'Francais',
            'IT' => 'Italien',
            'NL' => 'Neerlandais',
            'PL' => 'Polonais',
            'PT-BR' => 'Portugais Bresilien',
            'RU' => 'Russe',
            'TR' => 'Turc',
        ];
    }
}
