<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;

use App\Repository\CommentaireRepository;

#[ORM\Entity(repositoryClass: CommentaireRepository::class)]
#[ORM\Table(name: 'commentaire')]
class Commentaire
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function setId(int $id): self
    {
        $this->id = $id;
        return $this;
    }

    #[ORM\Column(type: 'text', nullable: false)]
    private ?string $contenu = null;

    public function getContenu(): ?string
    {
        return $this->contenu;
    }

    public function setContenu(string $contenu): self
    {
        $this->contenu = $contenu;
        return $this;
    }

    #[ORM\Column(type: 'datetime', nullable: false)]
    private ?\DateTimeInterface $date_commentaire = null;

    public function getDate_commentaire(): ?\DateTimeInterface
    {
        return $this->date_commentaire;
    }

    public function setDate_commentaire(\DateTimeInterface $date_commentaire): self
    {
        $this->date_commentaire = $date_commentaire;
        return $this;
    }

    #[ORM\Column(type: 'integer', nullable: false)]
    private ?int $utilisateur = null;

    public function getUtilisateur(): ?int
    {
        return $this->utilisateur;
    }

    public function setUtilisateur(int $utilisateur): self
    {
        $this->utilisateur = $utilisateur;
        return $this;
    }

    #[ORM\Column(type: 'integer', nullable: false)]
    private ?int $article_id = null;

    public function getArticle_id(): ?int
    {
        return $this->article_id;
    }

    public function setArticle_id(int $article_id): self
    {
        $this->article_id = $article_id;
        return $this;
    }

    #[ORM\Column(type: 'integer', nullable: false)]
    private ?int $parent_id = null;

    public function getParent_id(): ?int
    {
        return $this->parent_id;
    }

    public function setParent_id(int $parent_id): self
    {
        $this->parent_id = $parent_id;
        return $this;
    }

}
