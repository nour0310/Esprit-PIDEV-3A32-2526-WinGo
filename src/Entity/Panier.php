<?php

namespace App\Entity;

use App\Repository\PanierRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: PanierRepository::class)]
#[ORM\Table(name: 'panier')]
class Panier
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_panier', type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'id_user', type: 'integer')]
    private int $idUser = 0;

    #[ORM\Column(name: 'id_produit', type: 'integer')]
    private int $idProduit = 0;

    #[ORM\Column(type: 'integer', options: ['default' => 1])]
    private int $quantite = 1;

    #[ORM\Column(name: 'prix_unitaire', type: 'decimal', precision: 10, scale: 2)]
    private string $prixUnitaire = '0.00';

    #[ORM\Column(name: 'date_ajout', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateAjout = null;

    public function __construct() { $this->dateAjout = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getIdUser(): int { return $this->idUser; }
    public function setIdUser(int $idUser): static { $this->idUser = $idUser; return $this; }
    public function getIdProduit(): int { return $this->idProduit; }
    public function setIdProduit(int $idProduit): static { $this->idProduit = $idProduit; return $this; }
    public function getQuantite(): int { return $this->quantite; }
    public function setQuantite(int $quantite): static { $this->quantite = $quantite; return $this; }
    public function getPrixUnitaire(): string { return $this->prixUnitaire; }
    public function setPrixUnitaire(string $prixUnitaire): static { $this->prixUnitaire = $prixUnitaire; return $this; }
    public function getDateAjout(): ?\DateTimeInterface { return $this->dateAjout; }
    public function setDateAjout(?\DateTimeInterface $dateAjout): static { $this->dateAjout = $dateAjout; return $this; }
}
