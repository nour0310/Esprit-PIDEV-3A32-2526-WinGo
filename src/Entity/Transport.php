<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

use App\Entity\Utilisateur;
use App\Repository\TransportRepository;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: TransportRepository::class)]
class Transport
{

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private int $id;

    #[ORM\Column(type: "string", length: 250)]
    #[Assert\NotBlank(message: "Le type est obligatoire")] // [cite: 45]
    private ?string $type = null;

    #[ORM\Column(type: "string", length: 250)]
    #[Assert\NotBlank(message: "La capacité est obligatoire")] // [cite: 45]
    private ?string $capacite = null;

    #[ORM\Column(type: "float")]
    #[Assert\NotBlank(message: "Le tarif est obligatoire")] // [cite: 45]
    private ?float $tarif = null;

    #[ORM\Column(type: "string", length: 250)]
    #[Assert\NotBlank(message: "Le lieu de départ est obligatoire")] // [cite: 45]
    private ?string $depart = null;

    #[ORM\Column(type: "string", length: 250)]
    #[Assert\NotBlank(message: "Le lieu d'arrivée est obligatoire")] // [cite: 45]
    private ?string $arrivee = null;

    #[ORM\Column(type: "datetime")]
    #[Assert\NotBlank(message: "La date de départ est obligatoire")] // [cite: 45]
    private ?\DateTimeInterface $datedepart = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
#[ORM\JoinColumn(name: "user_id", referencedColumnName: "id", nullable: true)]
private ?Utilisateur $user_id = null;

    public function getId()
    {
        return $this->id;
    }

    public function setId($value)
    {
        $this->id = $value;
    }

    public function getType()
    {
        return $this->type;
    }

    public function setType(?string $type): self { $this->type = $type; return $this; }

    public function getCapacite()
    {
        return $this->capacite;
    }

    public function setCapacite(?string $capacite): self { $this->capacite = $capacite; return $this; }

    public function getTarif()
    {
        return $this->tarif;
    }

    public function setTarif(?float $tarif): self { $this->tarif = $tarif; return $this; }

    public function getDepart()
    {
        return $this->depart;
    }

    public function setDepart(?string $depart): self { $this->depart = $depart; return $this;}

    public function getArrivee()
    {
        return $this->arrivee;
    }

    public function setArrivee(?string $arrivee): self { $this->arrivee = $arrivee; return $this; }

    public function getDatedepart()
    {
        return $this->datedepart;
    }

    public function setDatedepart(?\DateTimeInterface $datedepart): self
    {
        $this->datedepart = $datedepart;
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
