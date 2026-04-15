<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class ArticleGeneratorService
{
    public function __construct(
        private readonly HttpClientInterface $client
    ) {}

    public function generateArticle(string $topic): ?array
    {
        try {
            $apiKey = $_ENV['GEMINI_API_KEY'] ?? null;
            if (!$apiKey) {
                throw new \Exception('Gemini API key not found');
            }

            $prompt = <<<PROMPT
Tu es un assistant expert en rédaction d'articles de blog de voyage pour le site "WinGo".
Ta mission est de générer un article de blog complet et structuré en français à partir du sujet fourni.

Format de réponse attendu (JSON strict, sans texte supplémentaire) :
{
    "titre": "Titre accrocheur de l'article",
    "contenu": "Contenu de l'article avec des paragraphes en HTML simple (<p>, <h2>, <ul>)",
    "categorie": "Aventure|Culture|Gastronomie|Détente",
    "region": "Nom de la région principale concernée",
    "tags": ["mot1", "mot2", "mot3"]
}

Sujet: {topic}
PROMPT;

            $prompt = str_replace('{topic}', $topic, $prompt);

            $response = $this->client->request('POST', 'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=' . $apiKey, [
                'json' => [
                    'contents' => [
                        [
                            'parts' => [
                                [
                                    'text' => $prompt
                                ]
                            ]
                        ]
                    ],
                    'generationConfig' => [
                        'temperature' => 0.7,
                        'topK' => 40,
                        'topP' => 0.95,
                        'maxOutputTokens' => 2048,
                    ]
                ]
            ]);

            $data = $response->toArray();
            
            if (isset($data['candidates'][0]['content']['parts'][0]['text'])) {
                $content = $data['candidates'][0]['content']['parts'][0]['text'];
                
                // Nettoyer la réponse (parfois l'IA ajoute des backticks ou du texte autour)
                $content = trim($content);
                $content = preg_replace('/^```json\s*/', '', $content);
                $content = preg_replace('/\s*```$/', '', $content);
                
                $articleData = json_decode($content, true);
                
                if (isset($articleData['titre'], $articleData['contenu'])) {
                    return $articleData;
                }
            }
        } catch (\Exception $e) {
            // Logguer l'erreur
            error_log('Article generation error: ' . $e->getMessage());
        }

        return null;
    }
}
