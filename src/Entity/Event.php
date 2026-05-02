<?php

namespace App\Entity;

use App\Entity\Participation;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity]
class Event
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private int $id_event;

    #[ORM\Column(type: "string", length: 255)]
    #[Assert\NotBlank(message: "Le titre est obligatoire")]
    #[Assert\Length(min: 3, max: 255, minMessage: "Le titre doit comporter au moins {{ limit }} caractères")]
    private string $title;

    #[ORM\Column(type: "text")]
    #[Assert\NotBlank(message: "La description est obligatoire")]
    private string $description;

    #[ORM\Column(type: "date")]
    #[Assert\NotBlank(message: "La date est obligatoire")]
    #[Assert\Type("\DateTimeInterface")]
    private \DateTimeInterface $date_event;

    #[ORM\Column(type: "string")]
    #[Assert\NotBlank(message: "L'heure de début est obligatoire")]
    private string $start_time;

    #[ORM\Column(type: "string", length: 255)]
    #[Assert\NotBlank(message: "L'emplacement est obligatoire")]
    private string $location;

    #[ORM\Column(type: "string", length: 100)]
    #[Assert\NotBlank(message: "Le type d'événement est obligatoire")]
    private string $event_type;

    #[ORM\Column(type: "string", length: 50)]
    #[Assert\NotBlank(message: "La saison est obligatoire")]
    private string $season;

    #[ORM\Column(type: "integer")]
    #[Assert\NotBlank(message: "La capacité est obligatoire")]
    #[Assert\Positive(message: "La capacité doit être positive")]
    private int $capacity;

    #[ORM\Column(type: "integer")]
    #[Assert\NotBlank(message: "Le nombre de places disponibles est obligatoire")]
    #[Assert\PositiveOrZero(message: "Le nombre de places doit être positif ou nul")]
    private int $available_places;

    #[ORM\Column(type: "string", length: 50)]
    #[Assert\NotBlank(message: "Le statut est obligatoire")]
    private string $status;

    #[ORM\Column(type: "string", length: 255)]
    #[Assert\NotBlank(message: "L'image est obligatoire")]
    private string $image_event;

    #[ORM\Column(type: "float")]
    #[Assert\NotBlank(message: "Le prix est obligatoire")]
    #[Assert\PositiveOrZero(message: "Le prix doit être positif ou nul")]
    private float $price;

    #[ORM\Column(type: "boolean", nullable: true)]
    private ?bool $isPassed = null;

    #[ORM\Column(type: "text", nullable: true)]
    private ?string $feedbacks = null;

    /**
     * Données temporaires utilisées dans Twig, non stockées en base.
     *
     * @var array<string, mixed>
     */
    public array $weatherData = [];

    public float $discountedPrice = 0.0;

    public bool $discountActive = false;

    /**
     * @var Collection<int, Participation>
     */
    #[ORM\OneToMany(mappedBy: "id_event", targetEntity: Participation::class)]
    private Collection $participations;

    public function __construct()
    {
        $this->participations = new ArrayCollection();
    }

    public function getId_event(): int
    {
        return $this->id_event;
    }

    public function setId_event(int $id_event): self
    {
        $this->id_event = $id_event;

        return $this;
    }

    public function getTitle(): string
    {
        return $this->title;
    }

    public function setTitle(string $title): self
    {
        $this->title = $title;

        return $this;
    }

    public function getDescription(): string
    {
        return $this->description;
    }

    public function setDescription(string $description): self
    {
        $this->description = $description;

        return $this;
    }

    public function getDate_event(): \DateTimeInterface
    {
        return $this->date_event;
    }

    public function setDate_event(\DateTimeInterface $date_event): self
    {
        $this->date_event = $date_event;

        return $this;
    }

    public function getStart_time(): string
    {
        return $this->start_time;
    }

    public function setStart_time(string $start_time): self
    {
        $this->start_time = $start_time;

        return $this;
    }

    public function getLocation(): string
    {
        return $this->location;
    }

    public function setLocation(string $location): self
    {
        $this->location = $location;

        return $this;
    }

    public function getEvent_type(): string
    {
        return $this->event_type;
    }

    public function setEvent_type(string $event_type): self
    {
        $this->event_type = $event_type;

        return $this;
    }

    public function getSeason(): string
    {
        return $this->season;
    }

    public function setSeason(string $season): self
    {
        $this->season = $season;

        return $this;
    }

    public function getCapacity(): int
    {
        return $this->capacity;
    }

    public function setCapacity(int $capacity): self
    {
        $this->capacity = $capacity;

        return $this;
    }

    public function getAvailable_places(): int
    {
        return $this->available_places;
    }

    public function setAvailable_places(int $available_places): self
    {
        $this->available_places = $available_places;

        return $this;
    }

    public function getStatus(): string
    {
        return $this->status;
    }

    public function setStatus(string $status): self
    {
        $this->status = $status;

        return $this;
    }

    public function getImage_event(): string
    {
        return $this->image_event;
    }

    public function setImage_event(string $image_event): self
    {
        $this->image_event = $image_event;

        return $this;
    }

    public function getPrice(): float
    {
        return $this->price;
    }

    public function setPrice(float $price): self
    {
        $this->price = $price;

        return $this;
    }

    public function getIsPassed(): ?bool
    {
        return $this->isPassed;
    }

    public function setIsPassed(?bool $isPassed): self
    {
        $this->isPassed = $isPassed;

        return $this;
    }

    /**
     * @return array<int, array<string, mixed>>
     */
    public function getFeedbacks(): array
    {
        if ($this->feedbacks === null) {
            return [];
        }

        $data = @unserialize($this->feedbacks);

        return is_array($data) ? $data : [];
    }

    /**
     * @param array<int, array<string, mixed>> $feedbacks
     */
    public function setFeedbacks(array $feedbacks): self
    {
        $this->feedbacks = serialize($feedbacks);

        return $this;
    }

    /**
     * @param array<string, mixed> $feedback
     */
    public function addFeedback(array $feedback): self
    {
        $feedbacks = $this->getFeedbacks();
        $feedbacks[] = $feedback;

        return $this->setFeedbacks($feedbacks);
    }

    public function hasUserFeedback(int $userId): bool
    {
        foreach ($this->getFeedbacks() as $fb) {
            if (isset($fb['userId']) && $fb['userId'] === $userId) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return Collection<int, Participation>
     */
    public function getParticipations(): Collection
    {
        return $this->participations;
    }

    public function addParticipation(Participation $participation): self
    {
        if (!$this->participations->contains($participation)) {
            $this->participations[] = $participation;
            $participation->setId_event($this);
        }

        return $this;
    }

    public function removeParticipation(Participation $participation): self
    {
        if ($this->participations->removeElement($participation)) {
            if ($participation->getId_event() === $this) {
                $participation->setId_event(null);
            }
        }

        return $this;
    }
}