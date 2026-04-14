<?php

namespace App\Entity;

use App\Repository\ReclamationRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ReclamationRepository::class)]
#[ORM\Table(name: 'reclamation')]
class Reclamation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_reclamation', type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'id_user', type: 'integer')]
    private int $idUser = 0;

    #[ORM\Column(name: 'type_reclamation', type: 'string', length: 50, nullable: true)]
    private ?string $typeReclamation = null;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $sujet = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $description = null;

    #[ORM\Column(name: 'date_reclamation', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateReclamation = null;

    #[ORM\Column(type: 'string', length: 30, options: ['default' => 'En attente'])]
    private string $statut = 'En attente';

    #[ORM\Column(type: 'string', length: 20, nullable: true)]
    private ?string $priorite = null;

    #[ORM\Column(name: 'piece_jointe', type: 'string', length: 255, nullable: true)]
    private ?string $pieceJointe = null;

    #[ORM\Column(name: 'reponse_admin', type: 'text', nullable: true)]
    private ?string $reponseAdmin = null;

    #[ORM\Column(name: 'date_reponse', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateReponse = null;

    public function __construct() { $this->dateReclamation = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getIdUser(): int { return $this->idUser; }
    public function setIdUser(int $idUser): static { $this->idUser = $idUser; return $this; }
    public function getTypeReclamation(): ?string { return $this->typeReclamation; }
    public function setTypeReclamation(?string $typeReclamation): static { $this->typeReclamation = $typeReclamation; return $this; }
    public function getSujet(): ?string { return $this->sujet; }
    public function setSujet(?string $sujet): static { $this->sujet = $sujet; return $this; }
    public function getDescription(): ?string { return $this->description; }
    public function setDescription(?string $description): static { $this->description = $description; return $this; }
    public function getDateReclamation(): ?\DateTimeInterface { return $this->dateReclamation; }
    public function setDateReclamation(?\DateTimeInterface $dateReclamation): static { $this->dateReclamation = $dateReclamation; return $this; }
    public function getStatut(): string { return $this->statut; }
    public function setStatut(string $statut): static { $this->statut = $statut; return $this; }
    public function getPriorite(): ?string { return $this->priorite; }
    public function setPriorite(?string $priorite): static { $this->priorite = $priorite; return $this; }
    public function getPieceJointe(): ?string { return $this->pieceJointe; }
    public function setPieceJointe(?string $pieceJointe): static { $this->pieceJointe = $pieceJointe; return $this; }
    public function getReponseAdmin(): ?string { return $this->reponseAdmin; }
    public function setReponseAdmin(?string $reponseAdmin): static { $this->reponseAdmin = $reponseAdmin; return $this; }
    public function getDateReponse(): ?\DateTimeInterface { return $this->dateReponse; }
    public function setDateReponse(?\DateTimeInterface $dateReponse): static { $this->dateReponse = $dateReponse; return $this; }
}
