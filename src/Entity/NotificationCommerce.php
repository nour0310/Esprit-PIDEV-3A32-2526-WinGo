<?php

namespace App\Entity;

use App\Repository\NotificationCommerceRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: NotificationCommerceRepository::class)]
#[ORM\Table(name: 'notification_commerce')]
class NotificationCommerce
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    private ?string $title = null;

    #[ORM\Column(type: Types::TEXT)]
    private ?string $message = null;

    #[ORM\Column(length: 50)]
    private ?string $type = null;

    #[ORM\Column(name: 'is_read')]
    private ?bool $isRead = false;

    #[ORM\Column(name: 'created_at', type: Types::DATETIME_MUTABLE)]
    private ?\DateTimeInterface $createdAt = null;

    #[ORM\Column(name: 'target_role', length: 50, nullable: true)]
    private ?string $targetRole = null;

    #[ORM\Column(name: 'target_user_id', nullable: true)]
    private ?int $targetUserId = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $link = null;

    public function __construct()
    {
        $this->createdAt = new \DateTime();
        $this->isRead = false;
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getTitle(): ?string
    {
        return $this->title;
    }

    public function setTitle(string $title): static
    {
        $this->title = $title;
        return $this;
    }

    public function getMessage(): ?string
    {
        return $this->message;
    }

    public function setMessage(string $message): static
    {
        $this->message = $message;
        return $this;
    }

    public function getType(): ?string
    {
        return $this->type;
    }

    public function setType(string $type): static
    {
        $this->type = $type;
        return $this;
    }

    public function isRead(): ?bool
    {
        return $this->isRead;
    }

    public function setIsRead(bool $isRead): static
    {
        $this->isRead = $isRead;
        return $this;
    }

    public function getCreatedAt(): ?\DateTimeInterface
    {
        return $this->createdAt;
    }

    public function setCreatedAt(\DateTimeInterface $createdAt): static
    {
        $this->createdAt = $createdAt;
        return $this;
    }

    public function getTargetRole(): ?string
    {
        return $this->targetRole;
    }

    public function setTargetRole(?string $targetRole): static
    {
        $this->targetRole = $targetRole;
        return $this;
    }

    public function getTargetUserId(): ?int
    {
        return $this->targetUserId;
    }

    public function setTargetUserId(?int $targetUserId): static
    {
        $this->targetUserId = $targetUserId;
        return $this;
    }

    public function getLink(): ?string
    {
        return $this->link;
    }

    public function setLink(?string $link): static
    {
        $this->link = $link;
        return $this;
    }
}