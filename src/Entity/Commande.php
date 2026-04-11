<?php

namespace App\Entity;

use App\Repository\CommandeRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: CommandeRepository::class)]
#[ORM\Table(name: 'commande')]
class Commande
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_commande', type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'id_user', type: 'integer')]
    private int $idUser = 0;

    #[ORM\Column(type: 'string', length: 20, columnDefinition: "ENUM('panier','en_cours','livree','annulee') NOT NULL DEFAULT 'en_cours'")]
    private string $status = 'en_cours';

    #[ORM\Column(type: 'decimal', precision: 10, scale: 2, options: ['default' => 0.00])]
    private string $total = '0.00';

    #[ORM\Column(name: 'items_json', type: 'text', nullable: true)]
    private ?string $itemsJson = null;

    #[ORM\Column(name: 'date_commande', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateCommande = null;

    #[ORM\Column(name: 'cause_annulation', type: 'string', length: 255, nullable: true)]
    private ?string $causeAnnulation = null;


    public function __construct() { $this->dateCommande = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getIdUser(): int { return $this->idUser; }
    public function setIdUser(int $idUser): static { $this->idUser = $idUser; return $this; }
    public function getStatus(): string { return $this->status; }
    public function setStatus(string $status): static { $this->status = $status; return $this; }
    public function getTotal(): string { return $this->total; }
    public function setTotal(string $total): static { $this->total = $total; return $this; }
    public function getItemsJson(): ?string { return $this->itemsJson; }
    public function setItemsJson(?string $itemsJson): static { $this->itemsJson = $itemsJson; return $this; }
    public function getDateCommande(): ?\DateTimeInterface { return $this->dateCommande; }
    public function setDateCommande(?\DateTimeInterface $dateCommande): static { $this->dateCommande = $dateCommande; return $this; }
    public function getCauseAnnulation(): ?string
    {
        return $this->causeAnnulation;
    }

    public function setCauseAnnulation(?string $causeAnnulation): static
    {
        $this->causeAnnulation = $causeAnnulation;
        return $this;
    }
}
