<?php

namespace App\Controller;

use App\Service\TravelApiService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;

class TravelApiController extends AbstractController
{
    #[Route('/api/weather/{city}', name: 'api_weather')]
    public function weather(string $city, TravelApiService $apiService): JsonResponse
    {
        try {
            $data = $apiService->getWeather($city);
            return new JsonResponse([
                'temp' => $data['main']['temp'],
                'desc' => $data['weather'][0]['description'],
                'icon' => $data['weather'][0]['icon']
            ]);
        } catch (\Exception $e) {
            return new JsonResponse(['error' => $e->getMessage()], 404);
        }
    }

    #[Route('/api/route/{sLat}/{sLon}/{eLat}/{eLon}', name: 'api_route')]
    public function route($sLat, $sLon, $eLat, $eLon, TravelApiService $apiService): JsonResponse
    {
        $data = $apiService->getRoute((float)$sLat, (float)$sLon, (float)$eLat, (float)$eLon);
        
        // Extracting summary (distance in meters, duration in seconds)
        $summary = $data['features'][0]['properties']['summary'];
        
        return new JsonResponse([
            'distance_km' => round($summary['distance'] / 1000, 2),
            'duration_min' => round($summary['duration'] / 60, 0)
        ]);
    }
}