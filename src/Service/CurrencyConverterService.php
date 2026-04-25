<?php

namespace App\Service;

use Symfony\Component\DependencyInjection\Attribute\Autowire;
use Symfony\Contracts\Cache\CacheInterface;
use Symfony\Contracts\Cache\ItemInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class CurrencyConverterService
{
    private const API_URL = 'https://v6.exchangerate-api.com/v6/%s/latest/TND';

    public const SUPPORTED_CURRENCIES = [
        'TND' => ['symbol' => 'TND', 'label' => 'Dinar Tunisien',    'flag' => '🇹🇳'],
        'USD' => ['symbol' => '$',   'label' => 'Dollar américain',  'flag' => '🇺🇸'],
        'EUR' => ['symbol' => '€',   'label' => 'Euro',              'flag' => '🇪🇺'],
        'SAR' => ['symbol' => '﷼',  'label' => 'Riyal Saoudien',    'flag' => '🇸🇦'],
    ];

    public function __construct(
        private readonly HttpClientInterface $httpClient,
        private readonly CacheInterface      $cache,
        #[Autowire('%env(EXCHANGE_RATE_API_KEY)%')]
        private readonly string              $apiKey
    ) {}

    /**
     * Récupère les taux depuis l'API (cachés 1h).
     *
     * @return array<string, float>
     *
     * @throws \RuntimeException si l'API retourne une erreur
     */
    public function getRates(): array
    {
        return $this->cache->get('exchange_rates_tnd', function (ItemInterface $item): array {
            $item->expiresAfter(3600);

            $url      = sprintf(self::API_URL, $this->apiKey);
            $response = $this->httpClient->request('GET', $url);
            $data     = $response->toArray();

            if (($data['result'] ?? '') !== 'success') {
                throw new \RuntimeException(
                    'Erreur API de conversion : ' . ($data['error-type'] ?? 'inconnue')
                );
            }

            return $data['conversion_rates'];
        });
    }

    /**
     * Convertit un montant TND vers la devise cible.
     *
     * @throws \InvalidArgumentException si la devise n'est pas supportée
     * @throws \RuntimeException         si l'API échoue
     *
     * @return array{
     *     original: float,
     *     converted: float,
     *     currency: string,
     *     symbol: string,
     *     label: string,
     *     rate: float
     * }
     */
    public function convert(float $amount, string $targetCurrency): array
    {
        $targetCurrency = strtoupper(trim($targetCurrency));

        if (!array_key_exists($targetCurrency, self::SUPPORTED_CURRENCIES)) {
            throw new \InvalidArgumentException(
                sprintf(
                    'Devise "%s" non supportée. Devises acceptées : %s',
                    $targetCurrency,
                    implode(', ', array_keys(self::SUPPORTED_CURRENCIES))
                )
            );
        }

        if ($targetCurrency === 'TND') {
            return [
                'original'  => $amount,
                'converted' => $amount,
                'currency'  => 'TND',
                'symbol'    => 'TND',
                'label'     => 'Dinar Tunisien',
                'rate'      => 1.0,
            ];
        }

        $rates = $this->getRates();

        if (!isset($rates[$targetCurrency])) {
            throw new \RuntimeException(
                "Le taux pour \"$targetCurrency\" est absent de la réponse API."
            );
        }

        $rate      = (float) $rates[$targetCurrency];
        $converted = round($amount * $rate, 2);
        $meta      = self::SUPPORTED_CURRENCIES[$targetCurrency];

        return [
            'original'  => $amount,
            'converted' => $converted,
            'currency'  => $targetCurrency,
            'symbol'    => $meta['symbol'],
            'label'     => $meta['label'],
            'rate'      => $rate,
        ];
    }

    /**
     * Retourne les devises supportées (sans appel API).
     *
     * @return array<string, array{symbol: string, label: string, flag: string}>
     */
    public function getSupportedCurrencies(): array
    {
        return self::SUPPORTED_CURRENCIES;
    }
}