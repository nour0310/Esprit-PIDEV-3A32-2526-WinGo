<?php

namespace App\Entity;

use Symfony\Component\Validator\Constraints as Assert;
use Doctrine\ORM\Mapping as ORM;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use App\Repository\ArticleRepository;

#[ORM\Entity(repositoryClass: ArticleRepository::class)]
#[ORM\Table(name: 'article')]
class Article
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 255, nullable: false)]
    #[Assert\NotBlank(message: "Le titre est obligatoire")]
    #[Assert\Length(min: 3, minMessage: "Le titre doit contenir au moins {{ limit }} caractères")]
    #[Assert\Regex(pattern: '/^[a-zA-ZÀ-ÿ\s\.,!?\'-]+$/u', message: "Le titre ne doit pas contenir de chiffres")]
    private ?string $titre = null;

    #[ORM\Column(type: 'text', nullable: true)]
    #[Assert\NotBlank(message: "Le contenu est obligatoire")]
    #[Assert\Length(min: 3, minMessage: "Le contenu doit contenir au moins {{ limit }} caractères")]
    #[Assert\Regex(pattern: '/^[a-zA-ZÀ-ÿ\s\.,!?\'-]+$/u', message: "Le contenu ne doit pas contenir de chiffres")]
    private ?string $contenu = null;

    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $datePublication = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class, inversedBy: 'articles')]
    #[ORM\JoinColumn(name: 'auteur', referencedColumnName: 'id')]
    private ?Utilisateur $auteur = null;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    // Plus de contrainte NotBlank – l'image est facultative
    private ?string $image = null;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    #[Assert\NotBlank(message: "Veuillez choisir une région")]
    private ?string $region = null;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    #[Assert\NotBlank(message: "Veuillez choisir une catégorie")]
    private ?string $categorie = null;

    #[ORM\OneToMany(targetEntity: Commentaire::class, mappedBy: 'article', cascade: ['remove'])]
    private Collection $commentaires;

    public function __construct()
    {
        $this->commentaires = new ArrayCollection();
    }

    public function getId(): ?int { return $this->id; }
    public function setId(int $id): self { $this->id = $id; return $this; }

    public function getTitre(): ?string { return $this->titre; }
    public function setTitre(string $titre): self { $this->titre = $titre; return $this; }

    public function getContenu(): ?string { return $this->contenu; }
    public function setContenu(?string $contenu): self { $this->contenu = $contenu; return $this; }

    public function getDatePublication(): ?\DateTimeInterface { return $this->datePublication; }
    public function setDatePublication(?\DateTimeInterface $datePublication): self { $this->datePublication = $datePublication; return $this; }

    public function getAuteur(): ?Utilisateur { return $this->auteur; }
    public function setAuteur(?Utilisateur $auteur): self { $this->auteur = $auteur; return $this; }

    public function getImage(): ?string { return $this->image; }
    public function setImage(?string $image): self { $this->image = $image; return $this; }

    public function getRegion(): ?string { return $this->region; }
    public function setRegion(?string $region): self { $this->region = $region; return $this; }

    public function getCategorie(): ?string { return $this->categorie; }
    public function setCategorie(?string $categorie): self { $this->categorie = $categorie; return $this; }

    /**
     * @return Collection<int, Commentaire>
     */
    public function getCommentaires(): Collection { return $this->commentaires; }
    public function addCommentaire(Commentaire $commentaire): self {
        if (!$this->commentaires->contains($commentaire)) {
            $this->commentaires->add($commentaire);
            $commentaire->setArticle($this);
        }
        return $this;
    }
    public function removeCommentaire(Commentaire $commentaire): self {
        if ($this->commentaires->removeElement($commentaire)) {
            if ($commentaire->getArticle() === $this) {
                $commentaire->setArticle(null);
            }
        }
        return $this;
    }
}