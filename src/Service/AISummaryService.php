<?php

namespace App\Service;

use Symfony\AI\Agent\AgentInterface;
use Symfony\AI\Platform\Message\Content\Text;
use Symfony\AI\Platform\Message\MessageBag;
use Symfony\AI\Platform\Message\UserMessage;

class AISummaryService
{
    public function __construct(private readonly AgentInterface $agent) {}

    public function summarize(string $text): ?string
    {
        $text = trim(strip_tags($text));
        if (mb_strlen($text) < 200) {
            return 'Texte trop court pour un résumé.';
        }

        $prompt = <<<PROMPT
Tu es un assistant qui résume des articles de blog en français.
Résume le texte suivant en 3-4 phrases maximum, en gardant l'essentiel.

Texte : {$text}
PROMPT;

        try {
            $result = $this->agent->call(
                new MessageBag(new UserMessage(new Text($prompt))),
                [
                    'temperature' => 0.3,
                    'num_predict' => 200,
                ],
            );
            $content = $result->getContent();
            if (!\is_string($content)) {
                return null;
            }

            return trim($content);
        } catch (\Throwable) {
            return null;
        }
    }
}
