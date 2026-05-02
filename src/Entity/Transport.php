<?php

namespace App\Entity;

use App\Repository\TransportRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: TransportRepository::class)]
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
    private ?\DateTimeInterface $datedepart = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'user_id', referencedColumnName: 'id', nullable: true)]
    private ?Utilisateur $user_id = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function setId(int $value): self
    {
        $this->id = $value;

        return $this;
    }

    public function getType(): string
    {
        return $this->type;
    }

    public function setType(string $value): self
    {
        $this->type = $value;

        return $this;
    }

    public function getCapacite(): string
    {
        return $this->capacite;
    }

    public function setCapacite(string $value): self
    {
        $this->capacite = $value;

        return $this;
    }

    public function getTarif(): float
    {
        return $this->tarif;
    }

    public function setTarif(float $value): self
    {
        $this->tarif = $value;

        return $this;
    }

    public function getDepart(): string
    {
        return $this->depart;
    }

    public function setDepart(string $value): self
    {
        $this->depart = $value;

        return $this;
    }

    public function getArrivee(): string
    {
        return $this->arrivee;
    }

    public function setArrivee(string $value): self
    {
        $this->arrivee = $value;

        return $this;
    }

    public function getDatedepart(): ?\DateTimeInterface
    {
        return $this->datedepart;
    }

    public function setDatedepart(\DateTimeInterface $value): self
    {
        $this->datedepart = $value;

        return $this;
    }

    public function getUser_id(): ?Utilisateur
    {
        return $this->user_id;
    }

    public function setUser_id(?Utilisateur $user): self
    {
        $this->user_id = $user;

        return $this;
    }
}