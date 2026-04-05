<?php

namespace App\Entity;

use App\Repository\ProduitRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ProduitRepository::class)]
#[ORM\Table(name: 'produit')]
class Produit
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_produit', type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'id_user', type: 'integer')]
    private int $idUser = 0;

    #[ORM\Column(type: 'string', length: 150)]
    private string $nom = '';

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $description = null;

    #[ORM\Column(type: 'decimal', precision: 10, scale: 2)]
    private string $prix = '0.00';

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $region = null;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $categorie = null;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $stock = 0;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $image = null;

    #[ORM\Column(name: 'date_ajout', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateAjout = null;

    public function __construct() { $this->dateAjout = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getIdUser(): int { return $this->idUser; }
    public function setIdUser(int $idUser): static { $this->idUser = $idUser; return $this; }
    public function getNom(): string { return $this->nom; }
    public function setNom(string $nom): static { $this->nom = $nom; return $this; }
    public function getDescription(): ?string { return $this->description; }
    public function setDescription(?string $description): static { $this->description = $description; return $this; }
    public function getPrix(): string { return $this->prix; }
    public function setPrix(string $prix): static { $this->prix = $prix; return $this; }
    public function getRegion(): ?string { return $this->region; }
    public function setRegion(?string $region): static { $this->region = $region; return $this; }
    public function getCategorie(): ?string { return $this->categorie; }
    public function setCategorie(?string $categorie): static { $this->categorie = $categorie; return $this; }
    public function getStock(): int { return $this->stock; }
    public function setStock(int $stock): static { $this->stock = $stock; return $this; }
    public function getImage(): ?string { return $this->image; }
    public function setImage(?string $image): static { $this->image = $image; return $this; }
    public function getDateAjout(): ?\DateTimeInterface { return $this->dateAjout; }
    public function setDateAjout(?\DateTimeInterface $dateAjout): static { $this->dateAjout = $dateAjout; return $this; }
}
