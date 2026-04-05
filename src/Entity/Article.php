<?php

namespace App\Entity;

use App\Repository\ArticleRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ArticleRepository::class)]
#[ORM\Table(name: 'article')]
class Article
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 255)]
    private string $titre = '';

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $contenu = null;

    #[ORM\Column(name: 'date_publication', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $datePublication = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'auteur', referencedColumnName: 'id', nullable: true)]
    private ?Utilisateur $auteur = null;

    #[ORM\OneToMany(mappedBy: 'article', targetEntity: Commentaire::class, cascade: ['remove'])]
    private Collection $commentaires;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $image = null;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $region = null;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $categorie = null;

    #[ORM\ManyToMany(targetEntity: Tag::class, inversedBy: 'articles')]
    #[ORM\JoinTable(name: 'article_tag',
        joinColumns: [new ORM\JoinColumn(name: 'article_id', referencedColumnName: 'id')],
        inverseJoinColumns: [new ORM\JoinColumn(name: 'tag_id', referencedColumnName: 'id')]
    )]
    private Collection $tags;

    public function __construct()
    {
        $this->tags = new ArrayCollection();
        $this->commentaires = new ArrayCollection();
        $this->datePublication = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function getTitre(): string { return $this->titre; }
    public function setTitre(string $titre): static { $this->titre = $titre; return $this; }
    public function getContenu(): ?string { return $this->contenu; }
    public function setContenu(?string $contenu): static { $this->contenu = $contenu; return $this; }
    public function getDatePublication(): ?\DateTimeInterface { return $this->datePublication; }
    public function setDatePublication(?\DateTimeInterface $datePublication): static { $this->datePublication = $datePublication; return $this; }
    public function getAuteur(): ?Utilisateur { return $this->auteur; }
    public function setAuteur(?Utilisateur $auteur): static { $this->auteur = $auteur; return $this; }

    /** @return Collection<int, Commentaire> */
    public function getCommentaires(): Collection { return $this->commentaires; }
    public function addCommentaire(Commentaire $commentaire): static {
        if (!$this->commentaires->contains($commentaire)) {
            $this->commentaires->add($commentaire);
            $commentaire->setArticle($this);
        }
        return $this;
    }
    public function removeCommentaire(Commentaire $commentaire): static {
        if ($this->commentaires->removeElement($commentaire)) {
            if ($commentaire->getArticle() === $this) {
                $commentaire->setArticle(null);
            }
        }
        return $this;
    }
    public function getImage(): ?string { return $this->image; }
    public function setImage(?string $image): static { $this->image = $image; return $this; }
    public function getRegion(): ?string { return $this->region; }
    public function setRegion(?string $region): static { $this->region = $region; return $this; }
    public function getCategorie(): ?string { return $this->categorie; }
    public function setCategorie(?string $categorie): static { $this->categorie = $categorie; return $this; }

    /** @return Collection<int, Tag> */
    public function getTags(): Collection { return $this->tags; }
    public function addTag(Tag $tag): static { if (!$this->tags->contains($tag)) { $this->tags->add($tag); } return $this; }
    public function removeTag(Tag $tag): static { $this->tags->removeElement($tag); return $this; }
}
