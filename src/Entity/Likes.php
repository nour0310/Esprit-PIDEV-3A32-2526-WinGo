<?php

namespace App\Entity;

use App\Repository\LikesRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: LikesRepository::class)]
#[ORM\Table(name: 'likes')]
class Likes
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'utilisateur_id', type: 'integer')]
    private int $utilisateurId = 0;

    #[ORM\Column(name: 'article_id', type: 'integer')]
    private int $articleId = 0;

    #[ORM\Column(name: 'date_like', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateLike = null;

    public function __construct() { $this->dateLike = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getUtilisateurId(): int { return $this->utilisateurId; }
    public function setUtilisateurId(int $utilisateurId): static { $this->utilisateurId = $utilisateurId; return $this; }
    public function getArticleId(): int { return $this->articleId; }
    public function setArticleId(int $articleId): static { $this->articleId = $articleId; return $this; }
    public function getDateLike(): ?\DateTimeInterface { return $this->dateLike; }
    public function setDateLike(?\DateTimeInterface $dateLike): static { $this->dateLike = $dateLike; return $this; }
}
