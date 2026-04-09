<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

use App\Entity\Utilisateur;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity]
class Reservation
{

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private ?int $id=null;

    #[ORM\Column(type: "string", length: 250)]
    #[Assert\NotBlank(message: "votre nom est obligatoire")] // [cite: 45]
    private ?string $user = null;
    #[ORM\Column(type: "string", length: 250)]
    #[Assert\NotBlank(message: "Le lieu d'expédition est obligatoire")] // [cite: 45]
    private ?string $exp = null;

    #[ORM\Column(type: "string", length: 250)]
    #[Assert\NotBlank(message: "Le statut est obligatoire")] // [cite: 45, 129]
    private ?string $statut = null;

    #[ORM\Column(type: "datetime")]
    #[Assert\NotBlank(message: "La date est obligatoire")] 
    private ?\DateTimeInterface $date = null;

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

    public function setDate(?\DateTimeInterface $date): self
    {
        $this->date = $date;
        return $this;
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