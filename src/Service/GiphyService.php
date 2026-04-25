<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

/**
 * Service pour rechercher des GIFs via l'API GIPHY.
 */
class GiphyService
{
    private string $apiKey;
    private HttpClientInterface $httpClient;

    public function __construct(HttpClientInterface $httpClient, string $giphyApiKey)
    {
        $this->httpClient = $httpClient;
        $this->apiKey = $giphyApiKey;
    }

    /**
     * Recherche des GIFs sur GIPHY.
     *
     * @return array<int, array{url: string, title: string}>
     */
    public function search(string $query, int $limit = 12): array
    {
        $response = $this->httpClient->request('GET', 'https://api.giphy.com/v1/gifs/search', [
            'query' => [
                'api_key' => $this->apiKey,
                'q' => $query,
                'limit' => $limit,
                'rating' => 'g',
                'lang' => 'fr',
            ],
        ]);

        $data = $response->toArray();
        $results = [];

        foreach ($data['data'] ?? [] as $gif) {
            $results[] = [
                'url' => $gif['images']['fixed_height_small']['url'] ?? '',
                'title' => $gif['title'] ?? '',
            ];
        }

        return $results;
    }
}
