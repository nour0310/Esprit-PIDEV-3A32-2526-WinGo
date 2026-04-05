<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;


#[ORM\Entity]
class Reservation
{

    #[ORM\Column(type: "integer")]
    private int $id;

    #[ORM\Column(type: "string", length: 250)]
    private string $user;

    #[ORM\Column(type: "string", length: 250)]
    private string $exp;

    #[ORM\Column(type: "string", length: 250)]
    private string $statut;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date;

    public function getId()
    {
        return $this->id;
    }

    public function setId($value)
    {
        $this->id = $value;
    }

    public function getUser()
    {
        return $this->user;
    }

    public function setUser($value)
    {
        $this->user = $value;
    }

    public function getExp()
    {
        return $this->exp;
    }

    public function setExp($value)
    {
        $this->exp = $value;
    }

    public function getStatut()
    {
        return $this->statut;
    }

    public function setStatut($value)
    {
        $this->statut = $value;
    }

    public function getDate()
    {
        return $this->date;
    }

    public function setDate($value)
    {
        $this->date = $value;
    }
}
