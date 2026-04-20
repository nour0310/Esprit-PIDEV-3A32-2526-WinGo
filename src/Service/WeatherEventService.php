<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class WeatherEventService
{
    private string $apiKey;

    public function __construct(
        private HttpClientInterface $httpClient
    ) {
        // Lire la clé API directement depuis les variables d'environnement
        $this->apiKey = $_ENV['OPENWEATHER_API_KEY'] ?? getenv('OPENWEATHER_API_KEY');
        
        if (empty($this->apiKey)) {
            throw new \RuntimeException('OPENWEATHER_API_KEY environment variable is not set.');
        }
    }

    public function getWeatherForEvent(string $city): array
    {
        if (empty($city)) {
            return ['error' => 'No city provided'];
        }

        try {
            $response = $this->httpClient->request('GET', 'https://api.openweathermap.org/data/2.5/weather', [
                'query' => [
                    'q' => $city,
                    'units' => 'metric',
                    'appid' => $this->apiKey,
                ],
                'timeout' => 3.0,
            ]);

            $data = $response->toArray();

            return [
                'temp' => round($data['main']['temp']),
                'description' => ucfirst($data['weather'][0]['description']),
                'humidity' => $data['main']['humidity'],
                'wind_speed' => $data['wind']['speed'],
                'icon' => $data['weather'][0]['icon'],
            ];
        } catch (\Exception $e) {
            return ['error' => 'Weather unavailable'];
        }
    }
}