<?php

namespace App\Service;

use Twilio\Rest\Client;
use Twilio\Http\CurlClient;

class SmsService {
    // src/Service/SmsService.php
public function sendWelcomeGold($to, $name, $code) {
    // REMPLACE DIRECTEMENT ICI AVEC TES CLÉS DE L'IMAGE b4a31d
    $sid = $_ENV['TWILIO_SID'];
    $token = $_ENV['TWILIO_TOKEN']; 
    $from = $_ENV['TWILIO_NUMBER'];

    $options = [
        \CURLOPT_SSL_VERIFYPEER => false,
        \CURLOPT_SSL_VERIFYHOST => false,
    ];
    
    $httpClient = new \Twilio\Http\CurlClient($options);

    try {
        // On passe le client HTTP en 5ème paramètre
        $client = new \Twilio\Rest\Client($sid, $token, null, null, $httpClient);
        
        return $client->messages->create($to, [
            'from' => $from,
            'body' => "WINGO GOLD 🏆 : Bravo $name ! Votre code promo exclusif est : $code."
        ]);
    } catch (\Exception $e) {
        throw new \Exception($e->getMessage());
    }
}
}