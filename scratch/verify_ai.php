<?php
require 'vendor/autoload.php';
$kernel = new App\Kernel('dev', true);
$kernel->boot();
try {
    // Manuel instantiation since service is private
    $client = $kernel->getContainer()->get('http_client');
    $logger = $kernel->getContainer()->get('logger');
    $apiKey = "VOTRE_CLE_ICI"; // Simuler la valeur du .env.local
    $svc = new App\Service\ArticleGeneratorService($client, $apiKey, $logger);
    
    echo "--- Testing with empty topic ---\n";
    try {
        $svc->generateArticle('');
    } catch (\App\Exception\ArticleGenerationException $e) {
        echo "CAUGHT (Empty Topic): " . $e->getPublicMessage() . " (" . $e->getStatusCode() . ")\n";
    }

    echo "\n--- Testing with valid topic + placeholder key ---\n";
    try {
        $svc->generateArticle('Sahara desert');
    } catch (\App\Exception\ArticleGenerationException $e) {
        echo "CAUGHT (Placeholder Key): " . $e->getPublicMessage() . " (" . $e->getStatusCode() . ")\n";
        echo "DETAIL: " . $e->getDetail() . "\n";
    }
} catch (\Throwable $e) {
    echo "GENERAL ERROR: " . $e->getMessage() . "\n";
}
