<?php
namespace App\Entity;

use App\Repository\PromoCodeRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: PromoCodeRepository::class)]
#[ORM\Table(name: "promo_code")] // Force le lien avec ta table manuelle
class PromoCode
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    private ?string $code = null;

    #[ORM\Column]
    private ?int $is_used = 0; // Correspond à ta colonne int(11)

    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: "user_id", referencedColumnName: "id")]
    private ?Utilisateur $user_id = null;

    // Getters et Setters
    public function getId(): ?int { return $this->id; }
    public function getCode(): ?string { return $this->code; }
    public function setCode(string $code): self { $this->code = $code; return $this; }
    public function getIsUsed(): ?int { return $this->is_used; }
    public function setIsUsed(int $is_used): self { $this->is_used = $is_used; return $this; }
    public function getUser_id(): ?Utilisateur { return $this->user_id; }
public function setUser_id(?Utilisateur $user_id): self 
{
    $this->user_id = $user_id;
    return $this;
}}