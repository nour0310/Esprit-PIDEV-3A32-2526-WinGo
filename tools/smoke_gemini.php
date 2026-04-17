<?php

declare(strict_types=1);

require __DIR__ . '/../vendor/autoload.php';

$key = $_ENV['GEMINI_API_KEY'] ?? getenv('GEMINI_API_KEY') ?: '';
if (trim($key) === '') {
    fwrite(STDERR, "GEMINI_API_KEY manquante.\n");
    exit(2);
}

$client = \Symfony\Component\HttpClient\HttpClient::create([
    'timeout' => 30,
]);

$url = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=' . $key;

$payload = [
    'contents' => [[
        'parts' => [[
            'text' => 'Réponds uniquement: {"ok":true}.',
        ]],
    ]],
    'generationConfig' => [
        'temperature' => 0,
        'maxOutputTokens' => 32,
        'candidateCount' => 1,
    ],
];

try {
    $response = $client->request('POST', $url, [
        'headers' => ['Content-Type' => 'application/json'],
        'json' => $payload,
    ]);

    $status = $response->getStatusCode();
    $body = $response->getContent(false);

    echo "HTTP $status\n";
    echo substr($body, 0, 400) . (strlen($body) > 400 ? "\n...truncated...\n" : "\n");

    exit($status === 200 ? 0 : 1);
} catch (\Throwable $e) {
    fwrite(STDERR, "Erreur: " . $e->getMessage() . "\n");
    exit(3);
}

