<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

use App\Entity\Utilisateur;

#[ORM\Entity]
class Reservation
{

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private ?int $id=null;

    #[ORM\Column(type: "string", length: 250)]
    private string $user;

    #[ORM\Column(type: "string", length: 250)]
    private string $exp;

    #[ORM\Column(type: "string", length: 250)]
    private string $statut;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date;

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

   public function getUser_id(): ?Utilisateur
{
    return $this->user_id;
}

// The setter now expects a User object
public function setUser_id(?Utilisateur $user): self
{
    $this->user_id = $user;
    return $this;
}
}
