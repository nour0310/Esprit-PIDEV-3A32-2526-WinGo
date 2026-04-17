<?php

namespace App\Service;

use App\Exception\ArticleGenerationException;
use Symfony\Component\HttpClient\Exception\ClientException;
use Symfony\Contracts\HttpClient\HttpClientInterface;
use Symfony\Component\DependencyInjection\Attribute\Autowire;
use Psr\Log\LoggerInterface;

class ArticleGeneratorService
{
    public function __construct(
        private readonly HttpClientInterface $client,
        #[Autowire('%gemini_api_key%')] private readonly string $apiKey,
        private readonly LoggerInterface $logger
    ) {}

    public function generateArticle(string $topic): array
    {
        if (empty(trim($topic))) {
            $this->logger->warning('Empty topic provided for article generation');
            throw new ArticleGenerationException(
                publicMessage: 'Sujet requis',
                detail: 'Le sujet est vide.',
                statusCode: 400,
            );
        }

        if (trim($this->apiKey) === '') {
            $this->logger->error('Missing GEMINI_API_KEY for article generation');
            throw new ArticleGenerationException(
                publicMessage: 'Configuration IA manquante',
                detail: 'La variable d’environnement GEMINI_API_KEY est absente ou vide.',
                statusCode: 500,
            );
        }

        $prompt = <<<PROMPT
Tu es un assistant expert en rédaction d'articles de blog de voyage pour "WinGo".
Génère un article complet en français sur le sujet : "$topic".

Réponds UNIQUEMENT avec un objet JSON valide, sans aucun texte avant ou après.

Format exact :
{
    "titre": "Titre accrocheur et attractif",
    "contenu": "Contenu HTML structuré avec paragraphes, titres et listes",
    "categorie": "Aventure ou Culture ou Gastronomie ou Détente",
    "region": "Nom de la région tunisienne concernée",
    "tags": ["mot-clé1", "mot-clé2", "mot-clé3"]
}
PROMPT;

        try {
            $this->logger->info('Generating article for topic: ' . $topic);
            
            $response = $this->client->request('POST', 
                'https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=' . $this->apiKey, [
                    'headers' => ['Content-Type' => 'application/json'],
                    'json' => [
                        'contents' => [
                            [
                                'parts' => [['text' => $prompt]]
                            ]
                        ],
                        'generationConfig' => [
                            'temperature' => 0.7,
                            'maxOutputTokens' => 2048,
                            'candidateCount' => 1,
                        ]
                    ],
                    'timeout' => 30,
                ]
            );

            try {
                $data = $response->toArray();
            } catch (ClientException $e) {
                $statusCode = $response->getStatusCode();
                $body = $response->getContent(false);

                $this->logger->error('Gemini API request failed', [
                    'status_code' => $statusCode,
                    'body' => $body,
                ]);

                if ($statusCode === 403) {
                    throw new ArticleGenerationException(
                        publicMessage: 'Accès refusé par le service IA',
                        detail: 'Clé API invalide/expirée ou API non activée (HTTP 403).',
                        statusCode: 502,
                        previous: $e,
                    );
                }

                throw new ArticleGenerationException(
                    publicMessage: 'Erreur du service IA',
                    detail: sprintf('Réponse HTTP %d reçue du service IA.', $statusCode),
                    statusCode: 502,
                    previous: $e,
                );
            }

            $content = $data['candidates'][0]['content']['parts'][0]['text'] ?? '';
            $this->logger->debug('Raw content from Gemini: ' . $content);
            
            // Nettoyer le contenu si enveloppé dans du markdown ```json ... ```
            if (preg_match('/```(?:json)?(.*?)```/s', $content, $matches)) {
                $content = trim($matches[1]);
            } else {
                $content = trim($content);
            }

            // Tenter de décoder le JSON
            $result = json_decode($content, true);
            
            if (json_last_error() !== JSON_ERROR_NONE) {
                $this->logger->warning('Failed to decode JSON from Gemini, raw content: ' . $content);
                throw new ArticleGenerationException(
                    publicMessage: 'Réponse IA invalide',
                    detail: 'Le service IA n’a pas renvoyé du JSON exploitable.',
                    statusCode: 502,
                );
            }

            $this->logger->info('Successfully generated article for topic: ' . $topic);
            
            return $result;
            
        } catch (ArticleGenerationException $e) {
            // Déjà une erreur "métier" avec un message public.
            throw $e;
        } catch (\Throwable $e) {
            $this->logger->error('Article generation failed: ' . $e->getMessage(), [
                'exception' => get_class($e),
                'trace' => $e->getTraceAsString(),
            ]);

            throw new ArticleGenerationException(
                publicMessage: 'Erreur lors de la génération',
                detail: $e->getMessage(),
                statusCode: 500,
                previous: $e,
            );
        }
    }
}
