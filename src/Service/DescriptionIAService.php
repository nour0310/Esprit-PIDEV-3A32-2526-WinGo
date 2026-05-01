<?php

namespace App\Service;

use Psr\Log\LoggerInterface;
use Symfony\Component\DependencyInjection\Attribute\Autowire;
use Symfony\Contracts\HttpClient\Exception\TransportExceptionInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class DescriptionIAService
{
    private const API_URL = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent';
    private const TIMEOUT = 20;
    private const MAX_RETRIES = 2;

    public function __construct(
        private readonly HttpClientInterface $httpClient,
        #[Autowire('%gemini_api_key%')] private readonly string $apiKey,
        private readonly LoggerInterface $logger,
    ) {
    }

    public function genererDescription(
        string $nom,
        string $categorie,
        string $region,
        ?string $prix = null
    ): string {
        if (trim($nom) === '' || trim($categorie) === '' || trim($region) === '') {
            $this->logger->warning('Champs obligatoires manquants pour la génération de description.', [
                'nom' => $nom,
                'categorie' => $categorie,
                'region' => $region,
            ]);

            throw new \RuntimeException('Les champs nom, catégorie et région sont obligatoires.');
        }

        if (trim($this->apiKey) === '') {
            $this->logger->error('GEMINI_API_KEY absente ou vide.');

            throw new \RuntimeException('Configuration IA manquante.');
        }

        $prompt = $this->construirePrompt($nom, $categorie, $region, $prix);
        $tentative = 0;

        while ($tentative <= self::MAX_RETRIES) {
            try {
                $this->logger->info('Génération de description produit via Gemini.', [
                    'nom' => $nom,
                    'categorie' => $categorie,
                    'region' => $region,
                ]);

                $response = $this->httpClient->request('POST', self::API_URL . '?key=' . $this->apiKey, [
                    'headers' => [
                        'Content-Type' => 'application/json',
                    ],
                    'json' => [
                        'contents' => [
                            [
                                'parts' => [
                                    ['text' => $prompt],
                                ],
                            ],
                        ],
                        'generationConfig' => [
                            'temperature' => 0.7,
                            'maxOutputTokens' => 220,
                            'candidateCount' => 1,
                        ],
                    ],
                    'timeout' => self::TIMEOUT,
                ]);

                $statusCode = $response->getStatusCode();

                if ($statusCode === 429) {
                    $this->logger->warning('Gemini a retourné 429 Too Many Requests.', [
                        'tentative' => $tentative + 1,
                    ]);

                    $tentative++;

                    if ($tentative > self::MAX_RETRIES) {
                        throw new \RuntimeException('RATE_LIMIT_GEMINI');
                    }

                    sleep($tentative + 1);
                    continue;
                }

                if ($statusCode === 403) {
                    $this->logger->error('Gemini a refusé l’accès à la requête.', [
                        'status_code' => $statusCode,
                    ]);

                    throw new \RuntimeException('FORBIDDEN_GEMINI');
                }

                if ($statusCode !== 200) {
                    $errorBody = $response->getContent(false);

                    $this->logger->error('Erreur HTTP retournée par Gemini.', [
                        'status_code' => $statusCode,
                        'body' => $errorBody,
                    ]);

                    throw new \RuntimeException('API_GEMINI_HTTP_' . $statusCode);
                }

                $data = $response->toArray(false);

                $description = trim($data['candidates'][0]['content']['parts'][0]['text'] ?? '');

                if ($description === '') {
                    $this->logger->warning('Gemini a retourné une description vide.', [
                        'response' => $data,
                    ]);

                    throw new \RuntimeException('EMPTY_GEMINI_RESPONSE');
                }

                $this->logger->info('Description générée avec succès.');

                return $description;
            } catch (TransportExceptionInterface $e) {
                $this->logger->error('Erreur réseau lors de l’appel à Gemini.', [
                    'message' => $e->getMessage(),
                ]);

                throw new \RuntimeException('NETWORK_GEMINI', previous: $e);
            }
        }

        throw new \RuntimeException('GENERATION_FAILED');
    }

    private function construirePrompt(
        string $nom,
        string $categorie,
        string $region,
        ?string $prix = null
    ): string {
        $prixInfo = $prix ? " au prix de {$prix} TND" : '';

        return <<<PROMPT
Tu es un expert en commerce de produits artisanaux et régionaux tunisiens.

Génère une description commerciale attrayante pour ce produit :
- Nom : {$nom}
- Catégorie : {$categorie}
- Région d'origine : {$region}{$prixInfo}

Consignes :
- Entre 50 et 90 mots
- Ton chaleureux et authentique
- Mets en valeur l'origine régionale et le savoir-faire local
- En français uniquement
- Pas de titre, juste le texte
PROMPT;
    }
}