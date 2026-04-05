<?php

namespace App\Entity;

use App\Repository\SuggestionRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: SuggestionRepository::class)]
#[ORM\Table(name: 'suggestion')]
class Suggestion
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_suggestion', type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'id_user', type: 'integer')]
    private int $idUser = 0;

    #[ORM\Column(type: 'string', length: 100, nullable: true)]
    private ?string $sujet = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $description = null;

    #[ORM\Column(type: 'string', length: 50, nullable: true)]
    private ?string $categorie = null;

    #[ORM\Column(name: 'date_suggestion', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateSuggestion = null;

    #[ORM\Column(type: 'string', length: 30, options: ['default' => 'Recue'])]
    private string $statut = 'Recue';

    #[ORM\Column(name: 'reponse_admin', type: 'text', nullable: true)]
    private ?string $reponseAdmin = null;

    #[ORM\Column(name: 'date_reponse', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dateReponse = null;

    #[ORM\Column(name: 'id_reclamation', type: 'integer', nullable: true)]
    private ?int $idReclamation = null;

    public function __construct() { $this->dateSuggestion = new \DateTime(); }

    public function getId(): ?int { return $this->id; }
    public function getIdUser(): int { return $this->idUser; }
    public function setIdUser(int $idUser): static { $this->idUser = $idUser; return $this; }
    public function getSujet(): ?string { return $this->sujet; }
    public function setSujet(?string $sujet): static { $this->sujet = $sujet; return $this; }
    public function getDescription(): ?string { return $this->description; }
    public function setDescription(?string $description): static { $this->description = $description; return $this; }
    public function getCategorie(): ?string { return $this->categorie; }
    public function setCategorie(?string $categorie): static { $this->categorie = $categorie; return $this; }
    public function getDateSuggestion(): ?\DateTimeInterface { return $this->dateSuggestion; }
    public function setDateSuggestion(?\DateTimeInterface $dateSuggestion): static { $this->dateSuggestion = $dateSuggestion; return $this; }
    public function getStatut(): string { return $this->statut; }
    public function setStatut(string $statut): static { $this->statut = $statut; return $this; }
    public function getReponseAdmin(): ?string { return $this->reponseAdmin; }
    public function setReponseAdmin(?string $reponseAdmin): static { $this->reponseAdmin = $reponseAdmin; return $this; }
    public function getDateReponse(): ?\DateTimeInterface { return $this->dateReponse; }
    public function setDateReponse(?\DateTimeInterface $dateReponse): static { $this->dateReponse = $dateReponse; return $this; }
    public function getIdReclamation(): ?int { return $this->idReclamation; }
    public function setIdReclamation(?int $idReclamation): static { $this->idReclamation = $idReclamation; return $this; }
}
