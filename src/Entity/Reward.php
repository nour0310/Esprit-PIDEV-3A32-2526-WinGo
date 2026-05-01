<?php

namespace App\Entity;

use App\Repository\RewardRepository;
use Doctrine\ORM\Mapping as ORM;
// Ajouter sur la classe Reward
#[ORM\Entity(repositoryClass: RewardRepository::class)]
#[ORM\Table(name: 'reward')]
#[ORM\Index(columns: ['id_user', 'played_at'], name: 'idx_reward_user_date')] // ← ajouter
class Reward
{
    public const TYPE_JEU_ROUE   = 'roue';
    public const TYPE_JEU_CARTES = 'cartes';

    public const REWARD_RIEN               = 'rien';
    public const REWARD_REDUCTION_POURCENT = 'reduction_pourcent';
    public const REWARD_REMISE_FIXE        = 'remise_fixe';
    public const REWARD_LIVRAISON_GRATUITE = 'livraison_gratuite';

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(name: 'id_user', type: 'integer')]
    private int $idUser;

    #[ORM\Column(name: 'type_jeu', type: 'string', length: 20)]
    private string $typeJeu;

    #[ORM\Column(name: 'reward_type', type: 'string', length: 30)]
    private string $rewardType;

    #[ORM\Column(name: 'played_at', type: 'datetime')]
    private \DateTimeInterface $playedAt;

    #[ORM\ManyToOne(targetEntity: Promotion::class)]
    #[ORM\JoinColumn(name: 'promotion_id', nullable: true)]
    private ?Promotion $promotion = null;

    public function __construct()
    {
        $this->playedAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }

    public function getIdUser(): int { return $this->idUser; }
    public function setIdUser(int $idUser): static { $this->idUser = $idUser; return $this; }

    public function getTypeJeu(): string { return $this->typeJeu; }
    public function setTypeJeu(string $typeJeu): static { $this->typeJeu = $typeJeu; return $this; }

    public function getRewardType(): string { return $this->rewardType; }
    public function setRewardType(string $rewardType): static { $this->rewardType = $rewardType; return $this; }

    public function getPlayedAt(): \DateTimeInterface { return $this->playedAt; }

    public function getPromotion(): ?Promotion { return $this->promotion; }
    public function setPromotion(?Promotion $promotion): static { $this->promotion = $promotion; return $this; }

    public function hasPromotion(): bool
    {
        return $this->rewardType !== self::REWARD_RIEN && $this->promotion !== null;
    }
    public function getRewardLabel(): string
    {
        return match ($this->rewardType) {
            self::REWARD_REDUCTION_POURCENT => 'Réduction 5%',
            self::REWARD_REMISE_FIXE        => 'Remise 5 TND',
            self::REWARD_LIVRAISON_GRATUITE => 'Livraison gratuite',
            default                         => 'Rien cette fois',
        };
    }
}