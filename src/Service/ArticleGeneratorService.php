<?php

namespace App\Service;

use Symfony\AI\Platform\PlatformInterface;
use Symfony\Component\DependencyInjection\Attribute\Target;

class ArticleGeneratorService
{
    public function __construct(
        #[Target('gemini')] private PlatformInterface $platform
    ) {}

    public function generateArticle(string $topic): ?array
    {
        try {
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
            
            // Créer un message simple
            $message = new \Symfony\AI\Platform\Message\TextMessage($prompt);
            $response = $this->platform->call($message);
            $content = $response->getContent();
            
            // Nettoyer la réponse (parfois l'IA ajoute des backticks ou du texte autour)
            $content = trim($content);
            $content = preg_replace('/^```json\s*/', '', $content);
            $content = preg_replace('/\s*```$/', '', $content);
            
            $data = json_decode($content, true);
            
            if (isset($data['titre'], $data['contenu'])) {
                return $data;
            }
        } catch (\Exception $e) {
            // Logguer l'erreur
            error_log('Article generation error: ' . $e->getMessage());
        }

        return null;
    }
}
