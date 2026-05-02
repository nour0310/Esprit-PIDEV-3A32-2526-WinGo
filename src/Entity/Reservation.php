<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
class Reservation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 250)]
    private string $user = '';

    #[ORM\Column(type: 'string', length: 250)]
    private string $exp = '';

    #[ORM\Column(type: 'string', length: 250)]
    private string $statut = '';

    #[ORM\Column(type: 'datetime')]
    private ?\DateTimeInterface $date = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $stars = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $comment = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $price = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'user_id', referencedColumnName: 'id', nullable: true)]
    private ?Utilisateur $user_id = null;

    public float $dynamicRating = 0.0;

    public int $totalReviews = 0;

    private ?float $basePriceDisplay = null;

    private ?string $clientStatus = null;

    private ?int $userReservationCount = null;

    public function getClientStatus(): ?string
    {
        return $this->clientStatus;
    }

    public function setClientStatus(?string $clientStatus): self
    {
        $this->clientStatus = $clientStatus;

        return $this;
    }

    public function getUserReservationCount(): ?int
    {
        return $this->userReservationCount;
    }

    public function setUserReservationCount(?int $count): self
    {
        $this->userReservationCount = $count;

        return $this;
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function setId(int $value): self
    {
        $this->id = $value;

        return $this;
    }

    public function getUser(): string
    {
        return $this->user;
    }

    public function setUser(string $value): self
    {
        $this->user = $value;

        return $this;
    }

    public function getExp(): string
    {
        return $this->exp;
    }

    public function setExp(string $value): self
    {
        $this->exp = $value;

        return $this;
    }

    public function getStatut(): string
    {
        return $this->statut;
    }

    public function setStatut(string $value): self
    {
        $this->statut = $value;

        return $this;
    }

    public function getDate(): ?\DateTimeInterface
    {
        return $this->date;
    }

    public function setDate(\DateTimeInterface $value): self
    {
        $this->date = $value;

        return $this;
    }

    public function getStars(): ?int
    {
        return $this->stars;
    }

    public function setStars(?int $value): self
    {
        $this->stars = $value;

        return $this;
    }

    public function getComment(): ?string
    {
        return $this->comment;
    }

    public function setComment(?string $value): self
    {
        $this->comment = $value;

        return $this;
    }

    public function getPrice(): ?int
    {
        return $this->price;
    }

    public function setPrice(?int $value): self
    {
        $this->price = $value;

        return $this;
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

    public function setUser_id(?Utilisateur $user): self
    {
        $this->user_id = $user;

        return $this;
    }
}