<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class GoogleTranslateTtsService
{
    private const API_URL = 'https://translate.google.com/translate_tts?ie=UTF-8&q=%s&tl=%s&client=tw-ob';

    public function __construct(
        private readonly HttpClientInterface $client
    ) {
    }

    public function synthesize(string $text, string $lang = 'fr'): ?string
    {
        $text = trim($text);
        if ($text === '') {
            return null;
        }

        $chunks = $this->splitText($text, 180);
        $audioData = '';

        foreach ($chunks as $chunk) {
            $url = sprintf(self::API_URL, urlencode($chunk), urlencode($lang));

            try {
                $response = $this->client->request('GET', $url, [
                    'headers' => [
                        'User-Agent' => 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
                    ],
                    'timeout' => 20,
                ]);

                $audioData .= $response->getContent();
            } catch (\Throwable) {
                return null;
            }
        }

        return $audioData !== '' ? $audioData : null;
    }

    private function splitText(string $text, int $limit): array
    {
        $chunks = [];
        $start = 0;
        $length = mb_strlen($text);

        while ($start < $length) {
            $end = min($start + $limit, $length);
            if ($end < $length) {
                $lastSpace = mb_strrpos(mb_substr($text, $start, $end - $start), ' ');
                if ($lastSpace !== false && $lastSpace > 0) {
                    $end = $start + $lastSpace;
                }
            }
            $chunks[] = trim(mb_substr($text, $start, $end - $start));
            $start = $end;
        }

        return array_values(array_filter($chunks, static fn (string $chunk) => $chunk !== ''));
    }
}
