<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class TravelApiService
{
    private $client;
    private $weatherApiKey;
    private $routeApiKey;

    // Symfony will now pull these from your services.yaml/env
    public function __construct(HttpClientInterface $client, string $weatherApiKey, string $routeApiKey)
    {
        $this->client = $client;
        $this->weatherApiKey = $weatherApiKey;
        $this->routeApiKey = $routeApiKey;
    }

    public function getWeather(string $city): array
    {
        $response = $this->client->request('GET', "https://api.openweathermap.org/data/2.5/weather", [
            'query' => [
                'q' => $city . ',TN', 
                'appid' => $this->weatherApiKey,
                'units' => 'metric',
                'lang' => 'fr'
            ]
        ]);

        return $response->toArray();
    }

    public function getRoute(float $startLat, float $startLon, float $endLat, float $endLon): array
    {
        $url = "https://api.openrouteservice.org/v2/directions/driving-car";
        
        $response = $this->client->request('GET', $url, [
            'headers' => ['Authorization' => $this->routeApiKey],
            'query' => [
                'start' => "$startLon,$startLat",
                'end' => "$endLon,$endLat"
            ]
        ]);

        return $response->toArray();
    }
}