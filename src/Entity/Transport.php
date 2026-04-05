<?php

namespace App\Entity;

use App\Repository\TransportRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: TransportRepository::class)]
#[ORM\Table(name: 'transport')]
class Transport
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 250)]
    private string $type = '';

    #[ORM\Column(type: 'string', length: 250)]
    private string $capacite = '';

    #[ORM\Column(type: 'float')]
    private float $tarif = 0.0;

    #[ORM\Column(type: 'string', length: 250)]
    private string $depart = '';

    #[ORM\Column(type: 'string', length: 250)]
    private string $arrivee = '';

    #[ORM\Column(type: 'datetime')]
    private \DateTimeInterface $datedepart;

    public function __construct() { $this->datedepart = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getType(): string { return $this->type; }
    public function setType(string $type): static { $this->type = $type; return $this; }
    public function getCapacite(): string { return $this->capacite; }
    public function setCapacite(string $capacite): static { $this->capacite = $capacite; return $this; }
    public function getTarif(): float { return $this->tarif; }
    public function setTarif(float $tarif): static { $this->tarif = $tarif; return $this; }
    public function getDepart(): string { return $this->depart; }
    public function setDepart(string $depart): static { $this->depart = $depart; return $this; }
    public function getArrivee(): string { return $this->arrivee; }
    public function setArrivee(string $arrivee): static { $this->arrivee = $arrivee; return $this; }
    public function getDatedepart(): \DateTimeInterface { return $this->datedepart; }
    public function setDatedepart(\DateTimeInterface $datedepart): static { $this->datedepart = $datedepart; return $this; }
}
