<?php

namespace App\Service;

use Symfony\Contracts\Cache\CacheInterface;
use Symfony\Contracts\Cache\ItemInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class WeatherService
{
    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly CacheInterface $cache,
        private readonly ?string $weatherApiKey = null
    ) {
    }

    public function getWeather(string $region): ?array
    {
        $region = trim($region);
        if ($region === '' || !$this->weatherApiKey) {
            return null;
        }

        $city = $this->normalizeRegionToCity($region);
        $cacheKey = 'weather_' . md5(strtolower($city));

        return $this->cache->get($cacheKey, function (ItemInterface $item) use ($city) {
            $item->expiresAfter(1800); // 30 minutes

            try {
                $response = $this->client->request('GET', 'https://api.openweathermap.org/data/2.5/weather', [
                    'query' => [
                        'q' => $city . ',tn',
                        'appid' => $this->weatherApiKey,
                        'units' => 'metric',
                        'lang' => 'fr',
                    ],
                ]);

                if (200 !== $response->getStatusCode()) {
                    return null;
                }

                $data = $response->toArray(false);
                if (!isset($data['main']['temp'], $data['weather'][0]['description'])) {
                    return null;
                }

                return [
                    'city' => $data['name'] ?? $city,
                    'temp' => (float) $data['main']['temp'],
                    'description' => (string) $data['weather'][0]['description'],
                    'icon' => isset($data['weather'][0]['icon'])
                        ? sprintf('https://openweathermap.org/img/wn/%s@2x.png', $data['weather'][0]['icon'])
                        : null,
                ];
            } catch (\Throwable) {
                return null;
            }
        });
    }

    private function normalizeRegionToCity(string $region): string
    {
        $map = [
            'Ariana' => 'Ariana',
            'Béja' => 'Beja',
            'Ben Arous' => 'Ben Arous',
            'Bizerte' => 'Bizerte',
            'Gabès' => 'Gabes',
            'Gafsa' => 'Gafsa',
            'Jendouba' => 'Jendouba',
            'Kairouan' => 'Kairouan',
            'Kasserine' => 'Kasserine',
            'Kébili' => 'Kebili',
            'Le Kef' => 'Kef',
            'La Manouba' => 'Manouba',
            'Mahdia' => 'Mahdia',
            'Médenine' => 'Medenine',
            'Monastir' => 'Monastir',
            'Nabeul' => 'Nabeul',
            'Sfax' => 'Sfax',
            'Sidi Bouzid' => 'Sidi Bouzid',
            'Siliana' => 'Siliana',
            'Sousse' => 'Sousse',
            'Tataouine' => 'Tataouine',
            'Tozeur' => 'Tozeur',
            'Tunis' => 'Tunis',
            'Zaghouan' => 'Zaghouan',
        ];

        return $map[$region] ?? $region;
    }
}
