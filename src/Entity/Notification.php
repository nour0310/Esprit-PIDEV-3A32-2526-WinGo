<?php

namespace App\Entity;

use App\Repository\NotificationRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: NotificationRepository::class)]
#[ORM\Table(name: 'notification')]
class Notification
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'utilisateur_id', type: 'integer')]
    private int $utilisateurId = 0;

    #[ORM\Column(name: 'emetteur_id', type: 'integer')]
    private int $emetteurId = 0;

    #[ORM\Column(type: 'string', length: 50)]
    private string $type = '';

    #[ORM\Column(type: 'text')]
    private string $contenu = '';

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $lien = null;

    #[ORM\Column(type: 'boolean', options: ['default' => false])]
    private bool $lu = false;

    #[ORM\Column(name: 'date_creation', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateCreation = null;

    public function __construct() { $this->dateCreation = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getUtilisateurId(): int { return $this->utilisateurId; }
    public function setUtilisateurId(int $utilisateurId): static { $this->utilisateurId = $utilisateurId; return $this; }
    public function getEmetteurId(): int { return $this->emetteurId; }
    public function setEmetteurId(int $emetteurId): static { $this->emetteurId = $emetteurId; return $this; }
    public function getType(): string { return $this->type; }
    public function setType(string $type): static { $this->type = $type; return $this; }
    public function getContenu(): string { return $this->contenu; }
    public function setContenu(string $contenu): static { $this->contenu = $contenu; return $this; }
    public function getLien(): ?string { return $this->lien; }
    public function setLien(?string $lien): static { $this->lien = $lien; return $this; }
    public function isLu(): bool { return $this->lu; }
    public function setLu(bool $lu): static { $this->lu = $lu; return $this; }
    public function getDateCreation(): ?\DateTimeInterface { return $this->dateCreation; }
    public function setDateCreation(?\DateTimeInterface $dateCreation): static { $this->dateCreation = $dateCreation; return $this; }
}
