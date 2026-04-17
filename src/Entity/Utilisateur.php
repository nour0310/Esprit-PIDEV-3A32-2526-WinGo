<?php

namespace App\Entity;

use App\Repository\UtilisateurRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Security\Core\User\UserInterface;
use Vich\UploaderBundle\Mapping\Annotation as Vich;
use Symfony\Component\HttpFoundation\File\File;

#[ORM\Entity(repositoryClass: UtilisateurRepository::class)]
#[ORM\Table(name: 'utilisateur')]
#[Vich\Uploadable]
class Utilisateur implements UserInterface, PasswordAuthenticatedUserInterface
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\OneToMany(mappedBy: 'user_id', targetEntity: Reservation::class)]
    private Collection $reservations;

    #[ORM\Column(type: 'string', length: 50)]
    private string $nom = '';

    #[ORM\Column(type: 'string', length: 50)]
    private string $prenom = '';

    #[ORM\Column(type: 'string', length: 100, unique: true)]
    private string $email = '';

    #[ORM\Column(name: 'mot_de_passe', type: 'string', length: 255)]
    private string $motDePasse = '';

    #[ORM\Column(type: 'string', length: 50, nullable: true)]
    private ?string $type = null;

    #[ORM\Column(type: 'bigint', nullable: true)]
    private ?int $telephone = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $age = null;

    #[ORM\Column(name: 'is_verified', type: 'boolean', options: ['default' => false])]
    private bool $isVerified = false;

    #[ORM\Column(name: 'verification_code', type: 'string', length: 10, nullable: true)]
    private ?string $verificationCode = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $faceDescriptor = null;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $photo = null;

    #[Vich\UploadableField(mapping: 'profile_photo', fileNameProperty: 'photo')]
    private ?File $imageFile = null;

    #[ORM\Column(type: 'datetime_immutable', nullable: true)]
    private ?\DateTimeImmutable $updatedAt = null;

    #[ORM\Column(type: 'string', length: 20, nullable: true)]
    private ?string $genre = null;

    private ?string $plainPassword = null;

    #[ORM\OneToOne(mappedBy: 'utilisateur', targetEntity: Profil::class, cascade: ['persist', 'remove'])]
    private ?Profil $profil = null;

    #[ORM\OneToMany(mappedBy: 'auteur', targetEntity: Article::class)]
    private Collection $articles;

    #[ORM\OneToMany(mappedBy: 'utilisateur', targetEntity: Commentaire::class)]
    private Collection $commentaires;

    public function __construct()
    {
        $this->articles = new ArrayCollection();
        $this->commentaires = new ArrayCollection();
        $this->reservations = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    /**
     * @return Collection<int, Article>
     */
    public function getArticles(): Collection
    {
        return $this->articles;
    }

    public function addArticle(Article $article): static
    {
        if (!$this->articles->contains($article)) {
            $this->articles->add($article);
            $article->setAuteur($this);
        }
        return $this;
    }

    public function removeArticle(Article $article): static
    {
        if ($this->articles->removeElement($article)) {
            if ($article->getAuteur() === $this) {
                $article->setAuteur(null);
            }
        }
        return $this;
    }

    public function getReservations(): Collection
{
    return $this->reservations;
}

public function addReservation(Reservation $reservation): static
{
    if (!$this->reservations->contains($reservation)) {
        $this->reservations->add($reservation);
        $reservation->setUser_id($this);
    }
    return $this;
}

public function removeReservation(Reservation $reservation): static
{
    if ($this->reservations->removeElement($reservation)) {
        if ($reservation->getUser_id() === $this) {
            $reservation->setUser_id(null);
        }
    }
    return $this;
}

    /**
     * @return Collection<int, Commentaire>
     */
    public function getCommentaires(): Collection
    {
        return $this->commentaires;
    }

    public function addCommentaire(Commentaire $commentaire): static
    {
        if (!$this->commentaires->contains($commentaire)) {
            $this->commentaires->add($commentaire);
            $commentaire->setUtilisateur($this);
        }
        return $this;
    }

    public function removeCommentaire(Commentaire $commentaire): static
    {
        if ($this->commentaires->removeElement($commentaire)) {
            if ($commentaire->getUtilisateur() === $this) {
                $commentaire->setUtilisateur(null);
            }
        }
        return $this;
    }

    public function getNom(): string
    {
        return $this->nom;
    }

    public function setNom(string $nom): static
    {
        $this->nom = $nom;
        return $this;
    }

    public function getPrenom(): string
    {
        return $this->prenom;
    }

    public function setPrenom(string $prenom): static
    {
        $this->prenom = $prenom;
        return $this;
    }

    public function getEmail(): string
    {
        return $this->email;
    }

    public function setEmail(string $email): static
    {
        $this->email = $email;
        return $this;
    }

    public function getMotDePasse(): string
    {
        return $this->motDePasse;
    }

    public function setMotDePasse(string $motDePasse): static
    {
        $this->motDePasse = $motDePasse;
        return $this;
    }

    public function getType(): ?string
    {
        return $this->type;
    }

    public function setType(?string $type): static
    {
        $this->type = $type;
        return $this;
    }

    public function getTelephone(): ?int
    {
        return $this->telephone;
    }

    public function setTelephone(?int $telephone): static
    {
        $this->telephone = $telephone;
        return $this;
    }

    public function getAge(): ?int
    {
        return $this->age;
    }

    public function setAge(?int $age): static
    {
        $this->age = $age;
        return $this;
    }

    public function isVerified(): bool
    {
        return $this->isVerified;
    }

    public function setIsVerified(bool $isVerified): static
    {
        $this->isVerified = $isVerified;
        return $this;
    }

    public function getVerificationCode(): ?string
    {
        return $this->verificationCode;
    }

    public function setVerificationCode(?string $verificationCode): static
    {
        $this->verificationCode = $verificationCode;
        return $this;
    }

    public function getProfil(): ?Profil
    {
        return $this->profil;
    }

    public function setProfil(?Profil $profil): static
    {
        $this->profil = $profil;
        return $this;
    }

    // --- UserInterface ---

    public function getUserIdentifier(): string
    {
        return $this->email;
    }

    public function getRoles(): array
{
    $type = strtoupper($this->type ?? 'CLIENT');

    if ($type === 'ADMIN') {
        return ['ROLE_ADMIN', 'ROLE_USER'];
    }

    if ($type === 'COMMERCANT') {
        return ['ROLE_COMMERCANT', 'ROLE_USER'];
    }

    return ['ROLE_CLIENT', 'ROLE_USER'];
}

public function isCommercant(): bool
{
    return strtoupper($this->type ?? '') === 'COMMERCANT';
}

public function isEnAttenteCommercant(): bool
{
    return strtoupper($this->type ?? '') === 'EN_ATTENTE_COMMERCANT';
}

    public function getPassword(): string
    {
        return $this->motDePasse;
    }

    public function eraseCredentials(): void {}

    public function getFullName(): string
    {
        return $this->prenom . ' ' . $this->nom;
    }

    public function isAdmin(): bool
    {
        return strtolower($this->type ?? '') === 'admin';
    }

    public function getFaceDescriptor(): ?string
    {
        return $this->faceDescriptor;
    }

    public function setFaceDescriptor(?string $faceDescriptor): static
    {
        $this->faceDescriptor = $faceDescriptor;
        return $this;
    }

    public function getPhoto(): ?string
    {
        return $this->photo;
    }

    public function setPhoto(?string $photo): static
    {
        $this->photo = $photo;
        return $this;
    }

    public function getImageFile(): ?File
    {
        return $this->imageFile;
    }

    public function setImageFile(?File $imageFile = null): void
    {
        $this->imageFile = $imageFile;

        if (null !== $imageFile) {
            $this->updatedAt = new \DateTimeImmutable();
        }
    }

    public function getUpdatedAt(): ?\DateTimeImmutable
    {
        return $this->updatedAt;
    }

    public function getInitials(): string
    {
        if (!$this->prenom || !$this->nom) return 'U';
        return strtoupper($this->prenom[0] . $this->nom[0]);
    }

    public function getAvatarUrl(): string
    {
        // 1. Photo locale dans l'entité Profil
        if ($this->profil && $this->profil->getPhoto()) {
            $photo = $this->profil->getPhoto();
            if (filter_var($photo, FILTER_VALIDATE_URL)) {
                return $photo;
            }
            return '/uploads/photos/' . $photo;
        }

        // 2. Photo dans l'entité Utilisateur (legacy)
        if ($this->photo) {
            return '/uploads/photos/' . $this->photo;
        }

        // 3. Avatar DiceBear automatique selon le genre
        $seed = urlencode($this->email ?: ($this->nom ?? 'user'));
        $genre = strtolower($this->genre ?? '');

        if (in_array($genre, ['femme', 'fille', 'f'])) {
            return "https://api.dicebear.com/8.x/adventurer/svg?seed={$seed}&hair=long01,long02,long03,long04&backgroundColor=b6e3f4,ffd5dc";
        } elseif (in_array($genre, ['homme', 'garcon', 'garçon', 'm'])) {
            return "https://api.dicebear.com/8.x/adventurer/svg?seed={$seed}&hair=short01,short02,short03&backgroundColor=b6e3f4,d1d4f9";
        }

        return "https://api.dicebear.com/8.x/avataaars/svg?seed={$seed}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc";
    }

    public function getAgeCategory(): string
    {
        if (!$this->age) return 'Non prÃ©cisÃ©';
        if ($this->age < 18) return 'Enfant/Ado';
        if ($this->age < 60) return 'Adulte';
        return 'SÃ©nior';
    }

    public function getTypeLabel(): string
    {
        return match(strtoupper($this->type ?? 'CLIENT')) {
            'ADMIN' => 'Administrateur',
            'COMMERCANT' => 'CommerÃ§ant',
            default => 'Client'
        };
    }

    public function getGenre(): ?string
    {
        return $this->genre;
    }

    public function setGenre(?string $genre): static
    {
        $this->genre = $genre;
        return $this;
    }

    public function getPlainPassword(): ?string
    {
        return $this->plainPassword;
    }

    public function setPlainPassword(?string $plainPassword): static
    {
        $this->plainPassword = $plainPassword;
        return $this;
    }
}
