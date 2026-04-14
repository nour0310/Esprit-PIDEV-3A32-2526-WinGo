<?php

namespace App\Entity;

use App\Repository\CommentaireRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: CommentaireRepository::class)]
#[ORM\Table(name: 'commentaire')]
class Commentaire
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'text')]
    private string $contenu = '';

    #[ORM\Column(name: 'date_commentaire', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateCommentaire = null;

    #[ORM\Column(name: 'utilisateur', type: 'integer', nullable: true)]
    private ?int $utilisateur = null;

    #[ORM\Column(name: 'article_id', type: 'integer', nullable: true)]
    private ?int $articleId = null;

    #[ORM\Column(name: 'parent_id', type: 'integer', nullable: true)]
    private ?int $parentId = null;

    public function __construct() { $this->dateCommentaire = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getContenu(): string { return $this->contenu; }
    public function setContenu(string $contenu): static { $this->contenu = $contenu; return $this; }
    public function getDateCommentaire(): ?\DateTimeInterface { return $this->dateCommentaire; }
    public function setDateCommentaire(?\DateTimeInterface $dateCommentaire): static { $this->dateCommentaire = $dateCommentaire; return $this; }
    public function getUtilisateur(): ?int { return $this->utilisateur; }
    public function setUtilisateur(?int $utilisateur): static { $this->utilisateur = $utilisateur; return $this; }
    public function getArticleId(): ?int { return $this->articleId; }
    public function setArticleId(?int $articleId): static { $this->articleId = $articleId; return $this; }
    public function getParentId(): ?int { return $this->parentId; }
    public function setParentId(?int $parentId): static { $this->parentId = $parentId; return $this; }
}
