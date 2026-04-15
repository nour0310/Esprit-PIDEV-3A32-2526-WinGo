<?php

namespace App\Service;

use Symfony\AI\Agent\AgentInterface;
use Symfony\Component\DependencyInjection\Attribute\Target;

class ArticleGeneratorService
{
    public function __construct(
        #[Target('article_generator')] private AgentInterface $agent
    ) {}

    public function generateArticle(string $topic): ?array
    {
        try {
            $response = $this->agent->call($topic);
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
