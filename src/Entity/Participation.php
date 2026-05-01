<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity]
class Participation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private ?int $id_participation = null;

    #[ORM\ManyToOne(targetEntity: Event::class, inversedBy: "participations")]
    #[ORM\JoinColumn(name: 'id_event', referencedColumnName: 'id_event', onDelete: 'CASCADE')]
    #[Assert\NotNull(message: "Event is required")]
    private ?Event $id_event = null;

    #[ORM\Column(type: "integer")]
    #[Assert\NotBlank(message: "User ID is required")]
    private int $id_user = 0;

    #[ORM\Column(type: "datetime")]
    #[Assert\NotBlank(message: "Date is required")]
    private ?\DateTimeInterface $date_participation = null;

    #[ORM\Column(type: "string", length: 50)]
    #[Assert\NotBlank(message: "Status is required")]
    private string $statut = 'pending';

    #[ORM\Column(type: "string", length: 100)]
    #[Assert\NotBlank(message: "Last name is required")]
    #[Assert\Length(min: 2, minMessage: "Last name must have at least {{ limit }} characters")]
    private string $nom_participant = '';

    #[ORM\Column(type: "string", length: 100)]
    #[Assert\NotBlank(message: "First name is required")]
    #[Assert\Length(min: 2, minMessage: "First name must have at least {{ limit }} characters")]
    private string $prenom_participant = '';

    #[ORM\Column(type: "string", length: 150)]
    #[Assert\NotBlank(message: "Email is required")]
    #[Assert\Email(message: "The email '{{ value }}' is not valid")]
    private string $email_participant = '';

    #[ORM\Column(type: "string", length: 30)]
    #[Assert\NotBlank(message: "Phone number is required")]
    private string $telephone = '';

    #[ORM\Column(type: "integer")]
    #[Assert\NotBlank(message: "Number of seats is required")]
    #[Assert\Positive(message: "Number of seats must be positive")]
    private int $nombre_places = 0;

    #[ORM\Column(type: "string", length: 255, unique: true, nullable: true)]
    private ?string $token = null;

    #[ORM\Column(type: "boolean", options: ["default" => false])]
    private bool $is_used = false;

    // NEW: price fields
    #[ORM\Column(type: "float", nullable: true)]
    private ?float $unit_price = null;

    #[ORM\Column(type: "float", nullable: true)]
    private ?float $total_price = null;

    // ──────────────────────────────────────────────
    // Getters / Setters (camelCase)
    // ──────────────────────────────────────────────
    public function getIdParticipation(): ?int { return $this->id_participation; }
    public function setIdParticipation(?int $id): self { $this->id_participation = $id; return $this; }

    public function getIdEvent(): ?Event { return $this->id_event; }
    public function setIdEvent(?Event $event): self { $this->id_event = $event; return $this; }

    public function getIdUser(): int { return $this->id_user; }
    public function setIdUser(int $id): self { $this->id_user = $id; return $this; }

    public function getDateParticipation(): ?\DateTimeInterface { return $this->date_participation; }
    public function setDateParticipation(?\DateTimeInterface $date): self { $this->date_participation = $date; return $this; }

    public function getStatut(): string { return $this->statut; }
    public function setStatut(string $statut): self { $this->statut = $statut; return $this; }

    public function getNomParticipant(): string { return $this->nom_participant; }
    public function setNomParticipant(?string $nom): self { $this->nom_participant = $nom ?? ''; return $this; }

    public function getPrenomParticipant(): string { return $this->prenom_participant; }
    public function setPrenomParticipant(?string $prenom): self { $this->prenom_participant = $prenom ?? ''; return $this; }

    public function getEmailParticipant(): string { return $this->email_participant; }
    public function setEmailParticipant(?string $email): self { $this->email_participant = $email ?? ''; return $this; }

    public function getTelephone(): string { return $this->telephone; }
    public function setTelephone(?string $tel): self { $this->telephone = $tel ?? ''; return $this; }

    public function getNombrePlaces(): int { return $this->nombre_places; }
    public function setNombrePlaces(int $nb): self { $this->nombre_places = $nb; return $this; }

    public function getToken(): ?string { return $this->token; }
    public function setToken(?string $token): self { $this->token = $token; return $this; }

    public function getIsUsed(): bool { return $this->is_used; }
    public function setIsUsed(bool $used): self { $this->is_used = $used; return $this; }

    public function getUnitPrice(): ?float { return $this->unit_price; }
    public function setUnitPrice(?float $price): self { $this->unit_price = $price; return $this; }

    public function getTotalPrice(): ?float { return $this->total_price; }
    public function setTotalPrice(?float $price): self { $this->total_price = $price; return $this; }

    // ──────────────────────────────────────────────
    // Aliases underscore (for legacy templates)
    // ──────────────────────────────────────────────
    public function getId_participation(): ?int { return $this->id_participation; }
    public function setId_participation($value): self { $this->id_participation = $value; return $this; }

    public function getId_event(): ?Event { return $this->id_event; }
    public function setId_event($value): self { $this->id_event = $value; return $this; }

    public function getId_user(): int { return $this->id_user; }
    public function setId_user($value): self { $this->id_user = $value; return $this; }

    public function getDate_participation(): ?\DateTimeInterface { return $this->date_participation; }
    public function setDate_participation($value): self { $this->date_participation = $value; return $this; }

    public function getNom_participant(): string { return $this->nom_participant; }
    public function setNom_participant($value): self { $this->nom_participant = $value ?? ''; return $this; }

    public function getPrenom_participant(): string { return $this->prenom_participant; }
    public function setPrenom_participant($value): self { $this->prenom_participant = $value ?? ''; return $this; }

    public function getEmail_participant(): string { return $this->email_participant; }
    public function setEmail_participant($value): self { $this->email_participant = $value ?? ''; return $this; }

    public function getNombre_places(): int { return $this->nombre_places; }
    public function setNombre_places($value): self { $this->nombre_places = $value; return $this; }

    public function getUnit_price(): ?float { return $this->unit_price; }
    public function setUnit_price($value): self { $this->unit_price = $value; return $this; }

    public function getTotal_price(): ?float { return $this->total_price; }
    public function setTotal_price($value): self { $this->total_price = $value; return $this; }
}