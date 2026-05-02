<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class OpenAiService
{
    private HttpClientInterface $client;
    private string $apiKey;

    public function __construct(HttpClientInterface $client, string $geminiApiKey)
    {
        $this->client = $client;
        $this->apiKey = $geminiApiKey;
    }

    public function getTravelAdvice(string $userMessage): string
    {
        $context = "You are WinGo Assistant, a passionate and knowledgeable Tunisian travel expert. " .
            "Your goal is to recommend the best experiences in Tunisia including: " .
            "- Coastal towns like Sidi Bou Said, Hammamet, and Mahdia. " .
            "- Historical sites like the Carthage ruins, El Jem, and the Dougga ruins. " .
            "- Desert adventures in Tozeur, Douz, and Matmata. " .
            "- Local food recommendations like Brik, Couscous, and Bambalouni. " .
            "Be warm, professional, and use a few Tunisian words like 'Marhaba' or 'Aslema'. " .
            "Do not mention that you don't have access to a database; just give great advice based on your knowledge.";

        $url = 'https://api.groq.com/openai/v1/chat/completions';

        try {
            $response = $this->client->request('POST', $url, [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                ],
                'json' => [
                    'model' => 'llama-3.3-70b-versatile',
                    'messages' => [
                        ['role' => 'system', 'content' => $context],
                        ['role' => 'user', 'content' => $userMessage],
                    ],
                    'temperature' => 0.8,
                ],
            ]);

            $data = $response->toArray();

            return (string) ($data['choices'][0]['message']['content'] ?? "Désolé, je ne trouve pas d'idées pour le moment.");
        } catch (\Exception $e) {
            return "Désolé, j'ai un souci technique. " . $e->getMessage();
        }
    }
}