<?php

namespace App\Entity;

use App\Repository\PromotionRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: PromotionRepository::class)]
#[ORM\Table(name: 'promotion')]
class Promotion
{
    public const TYPE_REDUCTION_POURCENT = 'reduction_pourcent';
    public const TYPE_REMISE_FIXE        = 'remise_fixe';
    public const TYPE_LIVRAISON_GRATUITE = 'livraison_gratuite';

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 20, unique: true)]
    private string $code;

    #[ORM\Column(type: 'string', length: 30)]
    private string $type;

    #[ORM\Column(name: 'id_user', type: 'integer')]
    private int $idUser;

    #[ORM\Column(name: 'is_used', type: 'boolean', options: ['default' => false])]
    private bool $isUsed = false;

    #[ORM\Column(name: 'date_expiration', type: 'datetime')]
    private \DateTimeInterface $dateExpiration;

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    public function __construct()
    {
        $this->createdAt      = new \DateTime();
        $this->dateExpiration = new \DateTime('+30 days');
    }

    public function getId(): ?int { return $this->id; }

    public function getCode(): string { return $this->code; }
    public function setCode(string $code): static { $this->code = $code; return $this; }

    public function getType(): string { return $this->type; }
    public function setType(string $type): static { $this->type = $type; return $this; }

    public function getIdUser(): int { return $this->idUser; }
    public function setIdUser(int $idUser): static { $this->idUser = $idUser; return $this; }

    public function isUsed(): bool { return $this->isUsed; }
    public function setIsUsed(bool $isUsed): static { $this->isUsed = $isUsed; return $this; }

    public function getDateExpiration(): \DateTimeInterface { return $this->dateExpiration; }

    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }

    public function isValid(): bool
    {
        return !$this->isUsed && $this->dateExpiration > new \DateTime();
    }

    public function getLabel(): string
    {
        return match ($this->type) {
            self::TYPE_REDUCTION_POURCENT => 'Réduction 5%',
            self::TYPE_REMISE_FIXE        => 'Remise 5 TND',
            self::TYPE_LIVRAISON_GRATUITE => 'Livraison gratuite',
            default                       => 'Promotion',
        };
    }
}