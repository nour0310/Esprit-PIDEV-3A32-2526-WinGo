<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use App\Repository\UtilisateurRepository;

#[ORM\Entity(repositoryClass: UtilisateurRepository::class)]
#[ORM\Table(name: 'utilisateur')]
class Utilisateur
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 50, nullable: false)]
    private ?string $nom = null;

    #[ORM\Column(type: 'string', length: 50, nullable: false)]
    private ?string $prenom = null;

    #[ORM\Column(type: 'string', length: 100, nullable: false)]
    private ?string $email = null;

    #[ORM\Column(type: 'string', length: 255, nullable: false)]
    private ?string $motDePasse = null;

    #[ORM\Column(type: 'string', length: 50, nullable: true)]
    private ?string $type = null;

    #[ORM\Column(type: 'bigint', nullable: true)]
    private ?int $telephone = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $age = null;

    #[ORM\Column(type: 'boolean', nullable: true)]
    private ?bool $isVerified = false;

    #[ORM\Column(type: 'string', length: 10, nullable: true)]
    private ?string $verificationCode = null;

    #[ORM\OneToMany(targetEntity: Article::class, mappedBy: 'auteur')]
    private Collection $articles;

    #[ORM\OneToMany(targetEntity: Commentaire::class, mappedBy: 'utilisateur')]
    private Collection $commentaires;

    public function __construct()
    {
        $this->articles = new ArrayCollection();
        $this->commentaires = new ArrayCollection();
    }

    public function getId(): ?int { return $this->id; }
    public function setId(int $id): self { $this->id = $id; return $this; }

    public function getNom(): ?string { return $this->nom; }
    public function setNom(string $nom): self { $this->nom = $nom; return $this; }

    public function getPrenom(): ?string { return $this->prenom; }
    public function setPrenom(string $prenom): self { $this->prenom = $prenom; return $this; }

    public function getEmail(): ?string { return $this->email; }
    public function setEmail(string $email): self { $this->email = $email; return $this; }

    public function getMotDePasse(): ?string { return $this->motDePasse; }
    public function setMotDePasse(string $motDePasse): self { $this->motDePasse = $motDePasse; return $this; }

    public function getType(): ?string { return $this->type; }
    public function setType(?string $type): self { $this->type = $type; return $this; }

    public function getTelephone(): ?int { return $this->telephone; }
    public function setTelephone(?int $telephone): self { $this->telephone = $telephone; return $this; }

    public function getAge(): ?int { return $this->age; }
    public function setAge(?int $age): self { $this->age = $age; return $this; }

    public function isVerified(): ?bool { return $this->isVerified; }
    public function setIsVerified(?bool $isVerified): self { $this->isVerified = $isVerified; return $this; }

    public function getVerificationCode(): ?string { return $this->verificationCode; }
    public function setVerificationCode(?string $verificationCode): self { $this->verificationCode = $verificationCode; return $this; }

    /**
     * @return Collection<int, Article>
     */
    public function getArticles(): Collection { return $this->articles; }
    public function addArticle(Article $article): self {
        if (!$this->articles->contains($article)) {
            $this->articles->add($article);
            $article->setAuteur($this);
        }
        return $this;
    }
    public function removeArticle(Article $article): self {
        if ($this->articles->removeElement($article) && $article->getAuteur() === $this) {
            $article->setAuteur(null);
        }
        return $this;
    }

    /**
     * @return Collection<int, Commentaire>
     */
    public function getCommentaires(): Collection { return $this->commentaires; }
    public function addCommentaire(Commentaire $commentaire): self {
        if (!$this->commentaires->contains($commentaire)) {
            $this->commentaires->add($commentaire);
            $commentaire->setUtilisateur($this);
        }
        return $this;
    }
    public function removeCommentaire(Commentaire $commentaire): self {
        if ($this->commentaires->removeElement($commentaire) && $commentaire->getUtilisateur() === $this) {
            $commentaire->setUtilisateur(null);
        }
        return $this;
    }
}