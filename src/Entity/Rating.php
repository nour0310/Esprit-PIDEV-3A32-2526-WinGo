<?php

namespace App\Entity;

use App\Repository\RatingRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: RatingRepository::class)]
#[ORM\Table(name: 'rating')]
class Rating
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'utilisateur_id', type: 'integer')]
    private int $utilisateurId = 0;

    #[ORM\Column(name: 'article_id', type: 'integer')]
    private int $articleId = 0;

    #[ORM\Column(type: 'integer')]
    private int $note = 1;

    #[ORM\Column(name: 'date_rating', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateRating = null;

    public function __construct() { $this->dateRating = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getUtilisateurId(): int { return $this->utilisateurId; }
    public function setUtilisateurId(int $utilisateurId): static { $this->utilisateurId = $utilisateurId; return $this; }
    public function getArticleId(): int { return $this->articleId; }
    public function setArticleId(int $articleId): static { $this->articleId = $articleId; return $this; }
    public function getNote(): int { return $this->note; }
    public function setNote(int $note): static { $this->note = $note; return $this; }
    public function getDateRating(): ?\DateTimeInterface { return $this->dateRating; }
    public function setDateRating(?\DateTimeInterface $dateRating): static { $this->dateRating = $dateRating; return $this; }
}
