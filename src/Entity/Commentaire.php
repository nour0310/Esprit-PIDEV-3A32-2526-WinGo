<?php

namespace App\Entity;

use Symfony\Component\Validator\Constraints as Assert;
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

    #[ORM\Column(type: 'text', nullable: false)]
    #[Assert\NotBlank(message: "Veuillez remplir ce champ.")]
    private ?string $contenu = null;

    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateCommentaire = null;

    #[ORM\ManyToOne(targetEntity: Utilisateur::class, inversedBy: 'commentaires')]
    #[ORM\JoinColumn(name: 'utilisateur', referencedColumnName: 'id')]
    private ?Utilisateur $utilisateur = null;

    #[ORM\ManyToOne(targetEntity: Article::class, inversedBy: 'commentaires')]
    #[ORM\JoinColumn(name: 'article_id', referencedColumnName: 'id')]
    private ?Article $article = null;

    #[ORM\ManyToOne(targetEntity: Commentaire::class, inversedBy: 'reponses')]
    #[ORM\JoinColumn(name: 'parent_id', referencedColumnName: 'id')]
    private ?Commentaire $parent = null;

    /**
     * @var Collection<int, Commentaire>
     */
    #[ORM\OneToMany(targetEntity: Commentaire::class, mappedBy: 'parent')]
    private Collection $reponses;

    public function __construct()
    {
        $this->reponses = new ArrayCollection();
        $this->dateCommentaire = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function setId(int $id): self { $this->id = $id; return $this; }

    public function getContenu(): ?string { return $this->contenu; }
    public function setContenu(string $contenu): self { $this->contenu = $contenu; return $this; }

    public function getDateCommentaire(): ?\DateTimeInterface { return $this->dateCommentaire; }
    public function setDateCommentaire(?\DateTimeInterface $dateCommentaire): self { $this->dateCommentaire = $dateCommentaire; return $this; }

    public function getUtilisateur(): ?Utilisateur { return $this->utilisateur; }
    public function setUtilisateur(?Utilisateur $utilisateur): self { $this->utilisateur = $utilisateur; return $this; }

    public function getArticle(): ?Article { return $this->article; }
    public function setArticle(?Article $article): self { $this->article = $article; return $this; }

    public function getParent(): ?Commentaire { return $this->parent; }
    public function setParent(?Commentaire $parent): self { $this->parent = $parent; return $this; }

    /**
     * @return Collection<int, Commentaire>
     */
    public function getReponses(): Collection { return $this->reponses; }
    public function addReponse(Commentaire $reponse): self {
        if (!$this->reponses->contains($reponse)) {
            $this->reponses->add($reponse);
            $reponse->setParent($this);
        }
        return $this;
    }
    public function removeReponse(Commentaire $reponse): self {
        if ($this->reponses->removeElement($reponse)) {
            if ($reponse->getParent() === $this) {
                $reponse->setParent(null);
            }
        }
        return $this;
    }
}
