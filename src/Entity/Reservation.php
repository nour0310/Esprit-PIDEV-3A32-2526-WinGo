<?php

namespace App\Entity;

use App\Repository\ReservationRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ReservationRepository::class)]
#[ORM\Table(name: 'reservation')]
class Reservation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'user', type: 'string', length: 250)]
    private string $user = '';

    #[ORM\Column(type: 'string', length: 250)]
    private string $exp = '';

    #[ORM\Column(type: 'string', length: 250)]
    private string $statut = '';

    #[ORM\Column(type: 'datetime')]
    private \DateTimeInterface $date;

    public function __construct() { $this->date = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getUser(): string { return $this->user; }
    public function setUser(string $user): static { $this->user = $user; return $this; }
    public function getExp(): string { return $this->exp; }
    public function setExp(string $exp): static { $this->exp = $exp; return $this; }
    public function getStatut(): string { return $this->statut; }
    public function setStatut(string $statut): static { $this->statut = $statut; return $this; }
    public function getDate(): \DateTimeInterface { return $this->date; }
    public function setDate(\DateTimeInterface $date): static { $this->date = $date; return $this; }
}
