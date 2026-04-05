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

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'utilisateur', referencedColumnName: 'id', nullable: true)]
    private ?Utilisateur $utilisateur = null;

    #[ORM\ManyToOne(targetEntity: Article::class, inversedBy: 'commentaires')]
    #[ORM\JoinColumn(name: 'article_id', referencedColumnName: 'id', nullable: true)]
    private ?Article $article = null;

    #[ORM\Column(name: 'parent_id', type: 'integer', nullable: true)]
    private ?int $parentId = null;

    public function __construct() { $this->dateCommentaire = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getContenu(): string { return $this->contenu; }
    public function setContenu(string $contenu): static { $this->contenu = $contenu; return $this; }
    public function getDateCommentaire(): ?\DateTimeInterface { return $this->dateCommentaire; }
    public function setDateCommentaire(?\DateTimeInterface $dateCommentaire): static { $this->dateCommentaire = $dateCommentaire; return $this; }
    public function getUtilisateur(): ?Utilisateur { return $this->utilisateur; }
    public function setUtilisateur(?Utilisateur $utilisateur): static { $this->utilisateur = $utilisateur; return $this; }
    public function getArticle(): ?Article { return $this->article; }
    public function setArticle(?Article $article): static { $this->article = $article; return $this; }
    public function getParentId(): ?int { return $this->parentId; }
    public function setParentId(?int $parentId): static { $this->parentId = $parentId; return $this; }
}
