<?php

namespace App\Service;

use App\DTO\ProductAiInput;
use Symfony\Component\DependencyInjection\Attribute\Autowire;
use Symfony\Contracts\HttpClient\HttpClientInterface;
use Symfony\Contracts\HttpClient\Exception\TransportExceptionInterface;

class ProductAiService
{
    public function __construct(
        private readonly HttpClientInterface $httpClient,

        #[Autowire(env: 'PRODUCT_AI_API_URL')]
        private readonly string $apiUrl,
    ) {}

    /**
     * Appelle le microservice Flask et retourne le résultat structuré.
     *
     * @return array{success: bool, titre_ameliore?: string, description?: string,
     *               tags?: list<string>, score_qualite?: int, conseils?: list<string>,
     *               error?: string}
     */
    public function generate(ProductAiInput $input): array
    {
        try {
            $response = $this->httpClient->request('POST', $this->apiUrl, [
                'json'    => $input->toArray(),
                'timeout' => 30,
            ]);

            $data = $response->toArray(throw: false);

            if (!isset($data['success']) || $data['success'] !== true) {
                return [
                    'success' => false,
                    'error'   => $data['error'] ?? 'Erreur inconnue du service IA.',
                ];
            }

            return $data;

        } catch (TransportExceptionInterface $e) {
            return [
                'success' => false,
                'error'   => 'Impossible de joindre le service IA : ' . $e->getMessage(),
            ];
        }
    }
}