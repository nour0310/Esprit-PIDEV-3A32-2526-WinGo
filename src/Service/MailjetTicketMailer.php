<?php

namespace App\Service;

use App\Entity\Participation;
use App\Entity\Event;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class MailjetTicketMailer
{
    private HttpClientInterface $httpClient;
    private string $apiKey;
    private string $secretKey;
    private string $senderEmail;
    private string $senderName;
    private string $replyTo;

    public function __construct(HttpClientInterface $httpClient)
    {
        $this->httpClient = $httpClient;

        // Read credentials directly from environment variables
        $this->apiKey = $_ENV['MAILJET_API_KEY'] ?? getenv('MAILJET_API_KEY');
        $this->secretKey = $_ENV['MAILJET_SECRET_KEY'] ?? getenv('MAILJET_SECRET_KEY');
        $this->senderEmail = $_ENV['MAILJET_SENDER_EMAIL'] ?? getenv('MAILJET_SENDER_EMAIL');
        $this->senderName = $_ENV['MAILJET_SENDER_NAME'] ?? getenv('MAILJET_SENDER_NAME');
        $this->replyTo = $_ENV['MAILJET_REPLY_TO'] ?? getenv('MAILJET_REPLY_TO');
    }

    public function sendTicketEmail(Participation $participation, Event $event, string $qrCodePng): void
    {
        // Vérifier que le QR code n'est pas vide
        if (empty($qrCodePng)) {
            error_log('ERREUR: Le QR code généré est vide');
            return;
        }

        $subject = '🎫 Votre billet pour ' . $event->getTitle();
        $htmlBody = $this->getEmailHtml($participation, $event, $qrCodePng);

        $payload = [
            'Messages' => [
                [
                    'From' => [
                        'Email' => $this->senderEmail,
                        'Name' => $this->senderName,
                    ],
                    'To' => [
                        ['Email' => $participation->getEmailParticipant()],
                    ],
                    'ReplyTo' => [
                        'Email' => $this->replyTo,
                    ],
                    'Subject' => $subject,
                    'HTMLPart' => $htmlBody,
                    'Attachments' => [
                        [
                            'ContentType' => 'image/png',
                            'Filename' => 'billet_wingo.png',
                            'Base64Content' => base64_encode($qrCodePng),
                        ],
                    ],
                ],
            ],
        ];

        $response = $this->httpClient->request('POST', 'https://api.mailjet.com/v3.1/send', [
            'auth_basic' => [$this->apiKey, $this->secretKey],
            'json' => $payload,
        ]);

        if ($response->getStatusCode() !== 200) {
            error_log('Mailjet API error: ' . $response->getContent(false));
        }
    }

    private function getEmailHtml(Participation $p, Event $e, string $qrCodePng): string
    {
        $unitPrice = $p->getUnitPrice();
        $totalPrice = $p->getTotalPrice();
        
        if ($unitPrice > 0) {
            $priceText = number_format($unitPrice, 2) . ' €';
            $totalText = number_format($totalPrice, 2) . ' €';
        } else {
            $priceText = 'Gratuit';
            $totalText = 'Gratuit';
        }

        // Convertir le PNG en base64 pour l'intégration inline
        $qrCodeBase64 = base64_encode($qrCodePng);
        $qrCodeSrc = 'data:image/png;base64,' . $qrCodeBase64;

        return sprintf('
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px;">
                <h2 style="color: #1a1a2e;">Merci pour votre réservation, %s %s !</h2>
                <p><strong>🎉 Événement :</strong> %s</p>
                <p><strong>📅 Date :</strong> %s à %s</p>
                <p><strong>📍 Lieu :</strong> %s</p>
                <p><strong>🎟️ Places réservées :</strong> %d</p>
                <p><strong>💰 Prix par place :</strong> %s</p>
                <p><strong>💵 Prix total :</strong> %s</p>
                <hr style="margin: 20px 0;">
                <div style="text-align: center;">
                    <p><strong>📱 Votre QR code d’entrée :</strong></p>
                    <img src="%s" alt="QR Code" style="width: 200px; height: 200px; border: 1px solid #ddd; padding: 10px; border-radius: 12px;">
                    <p style="font-size: 12px; color: #666;">Scannez ce code à l’entrée (valide plusieurs fois).</p>
                </div>
                <hr style="margin: 20px 0;">
                <p style="color: #6c757d; font-size: 12px;">Ce billet est strictement personnel. Une pièce jointe identique est également disponible.</p>
            </div>
        ',
            htmlspecialchars($p->getPrenomParticipant()),
            htmlspecialchars($p->getNomParticipant()),
            htmlspecialchars($e->getTitle()),
            $e->getDate_event()->format('d/m/Y'),
            $e->getStart_time(),
            htmlspecialchars($e->getLocation()),
            $p->getNombrePlaces(),
            $priceText,
            $totalText,
            $qrCodeSrc   // Image inline en base64
        );
    }
}