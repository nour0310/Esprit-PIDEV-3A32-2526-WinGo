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
    #[ORM\Column(type: "integer", nullable: true)]
    private ?int $stars = null;
    #[ORM\Column(type: "text", nullable: true)]
    private ?string $comment = null;
    #[ORM\Column(type: "integer", nullable: true)]
    private ?int $price = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: "user_id", referencedColumnName: "id", nullable: true)]
    private ?Utilisateur $user_id = null; 
    public $dynamicRating; 
    public $totalReviews;
    private const MAX_PASSENGERS = 50;
    private ?float $basePriceDisplay = null;
    // Variables temporaires (non sauvegardées en base de données)
    private ?string $clientStatus = null;
    private ?int $userReservationCount = null;

    // --- GETTERS ET SETTERS ---
    public function getClientStatus(): ?string { return $this->clientStatus; }
    public function setClientStatus(?string $clientStatus): self { $this->clientStatus = $clientStatus; return $this; }

    public function getUserReservationCount(): ?int { return $this->userReservationCount; }
    public function setUserReservationCount(?int $count): self { $this->userReservationCount = $count; return $this; }
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
    public function getStars()
    {
        return $this->stars;
    }

    public function setStars($value)
    {
        $this->stars = $value;
    }
    public function getComment()
    {
        return $this->comment;
    }

    public function setComment($value)
    {
        $this->comment = $value;
    }
    public function getPrice()
    {
        return $this->price;
    }

    public function setPrice($value)
    {
        $this->price = $value;
    }
    public function getBasePriceDisplay(): ?float
    {
        return $this->basePriceDisplay;
    }

    public function setBasePriceDisplay(?float $basePriceDisplay): self
    {
        $this->basePriceDisplay = $basePriceDisplay;

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
