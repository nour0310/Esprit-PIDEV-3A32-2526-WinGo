<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

use App\Entity\Utilisateur;
use App\Repository\TransportRepository;

#[ORM\Entity(repositoryClass: TransportRepository::class)]
class Transport
{

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private int $id;

    #[ORM\Column(type: "string", length: 250)]
    private string $type;

    #[ORM\Column(type: "string", length: 250)]
    private string $capacite;

    #[ORM\Column(type: "float")]
    private float $tarif;

    #[ORM\Column(type: "string", length: 250)]
    private string $depart;

    #[ORM\Column(type: "string", length: 250)]
    private string $arrivee;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $datedepart;

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

    public function setType($value)
    {
        $this->type = $value;
    }

    public function getCapacite()
    {
        return $this->capacite;
    }

    public function setCapacite($value)
    {
        $this->capacite = $value;
    }

    public function getTarif()
    {
        return $this->tarif;
    }

    public function setTarif($value)
    {
        $this->tarif = $value;
    }

    public function getDepart()
    {
        return $this->depart;
    }

    public function setDepart($value)
    {
        $this->depart = $value;
    }

    public function getArrivee()
    {
        return $this->arrivee;
    }

    public function setArrivee($value)
    {
        $this->arrivee = $value;
    }

    public function getDatedepart()
    {
        return $this->datedepart;
    }

    public function setDatedepart($value)
    {
        $this->datedepart = $value;
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
