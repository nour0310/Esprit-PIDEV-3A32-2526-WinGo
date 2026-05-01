<?php

namespace App\Entity;

use App\Repository\ProfilRepository;
use Doctrine\ORM\Mapping as ORM;
use Vich\UploaderBundle\Mapping\Annotation as Vich;
use Symfony\Component\HttpFoundation\File\File;

#[ORM\Entity(repositoryClass: ProfilRepository::class)]
#[ORM\Table(name: 'profil')]
#[ORM\HasLifecycleCallbacks]
#[Vich\Uploadable]
class Profil
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\OneToOne(inversedBy: 'profil', targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'utilisateur_id', referencedColumnName: 'id', nullable: true)]
    private ?Utilisateur $utilisateur = null;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $bio = null;

    // Champ BDD : stocke le nom du fichier
    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $image = null;

    // Champ NON mappé BDD : l'objet File pour VichUploader
    #[Vich\UploadableField(mapping: 'profile_photo', fileNameProperty: 'image')]
    private ?File $imageFile = null;

    // Nécessaire pour VichUploader (cache-busting)
    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $updatedAt = null;

    public function getId(): ?int { return $this->id; }

    public function getUtilisateur(): ?Utilisateur { return $this->utilisateur; }
    public function setUtilisateur(?Utilisateur $utilisateur): static { $this->utilisateur = $utilisateur; return $this; }

    public function getBio(): ?string { return $this->bio; }
    public function setBio(?string $bio): static { $this->bio = $bio; return $this; }

    public function getImage(): ?string { return $this->image; }
    public function setImage(?string $image): static { $this->image = $image; return $this; }

    // VichUploader : setter déclenche le traitement du fichier
    public function setImageFile(?File $imageFile = null): static
    {
        $this->imageFile = $imageFile;
        if ($imageFile !== null) {
            // Mise à jour de updatedAt pour forcer Doctrine à persister
            $this->updatedAt = new \DateTime();
        }
        return $this;
    }

    public function getImageFile(): ?File { return $this->imageFile; }

    public function getUpdatedAt(): ?\DateTimeInterface { return $this->updatedAt; }
    public function setUpdatedAt(?\DateTimeInterface $updatedAt): static { $this->updatedAt = $updatedAt; return $this; }

    // Compatibilité avec l'ancien code (getPhoto/setPhoto)
    public function getPhoto(): ?string { return $this->image; }
    public function setPhoto(?string $photo): static { $this->image = $photo; return $this; }
}