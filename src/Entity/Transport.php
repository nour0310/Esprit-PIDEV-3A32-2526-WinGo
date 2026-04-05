<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;


#[ORM\Entity]
class Transport
{

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
}
