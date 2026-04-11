<?php

namespace App\Service;

use App\Entity\Commande;
use App\Entity\Utilisateur;
use Symfony\Bridge\Twig\Mime\TemplatedEmail;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Address;

class CommandeMailerService
{
    public function __construct(
        private MailerInterface $mailer
    ) {}

    public function sendCommandeLivreeEmail(Utilisateur $client, Commande $commande): void
    {
        $email = (new TemplatedEmail())
            ->from(new Address('yomnahelali60@gmail.com', 'WinGo'))
            ->to(new Address($client->getEmail(), $client->getFullName()))
            ->subject('Votre commande a été livrée')
            ->htmlTemplate('emails/commande_livree.html.twig')
            ->context([
                'client' => $client,
                'commande' => $commande,
            ]);

        $this->mailer->send($email);
    }

    public function sendCommandeAnnuleeEmail(Utilisateur $client, Commande $commande): void
    {
        $email = (new TemplatedEmail())
            ->from(new Address('yomnahelali60@gmail.com', 'WinGo'))
            ->to(new Address($client->getEmail(), $client->getFullName()))
            ->subject('Votre commande a été annulée')
            ->htmlTemplate('emails/commande_annulee.html.twig')
            ->context([
                'client' => $client,
                'commande' => $commande,
                'cause' => $commande->getCauseAnnulation(),
            ]);

        $this->mailer->send($email);
    }
}