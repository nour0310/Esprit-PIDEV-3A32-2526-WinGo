<?php

namespace App\Service;

use Twilio\Rest\Api\V2010\Account\MessageInstance;
use Twilio\Rest\Client;
use Twilio\Http\CurlClient;

class SmsService
{
    public function sendWelcomeGold(string $to, string $name, string $code): MessageInstance
    {
        $sid = $_ENV['TWILIO_SID'];
        $token = $_ENV['TWILIO_TOKEN'];
        $from = $_ENV['TWILIO_NUMBER'];

        $options = [
            \CURLOPT_SSL_VERIFYPEER => false,
            \CURLOPT_SSL_VERIFYHOST => false,
        ];

        $httpClient = new CurlClient($options);

        try {
            $client = new Client($sid, $token, null, null, $httpClient);

            return $client->messages->create($to, [
                'from' => $from,
                'body' => "WINGO GOLD 🏆 : Bravo $name ! Votre code promo exclusif est : $code.",
            ]);
        } catch (\Exception $e) {
            throw new \RuntimeException($e->getMessage(), 0, $e);
        }
    }
}