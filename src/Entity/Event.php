<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
class Event
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id_event = null;

    #[ORM\Column(length: 255)]
    private ?string $title = null;

    #[ORM\Column(type: 'text')]
    private ?string $description = null;

    #[ORM\Column(type: 'datetime')]
    private ?\DateTimeInterface $date_event = null;

    #[ORM\Column(length: 10)]
    private ?string $start_time = null;

    #[ORM\Column(length: 255)]
    private ?string $location = null;

    #[ORM\Column(length: 50)]
    private ?string $event_type = null;

    #[ORM\Column(length: 20)]
    private ?string $season = null;

    #[ORM\Column]
    private ?int $capacity = null;

    #[ORM\Column]
    private ?int $available_places = null;

    #[ORM\Column(length: 20)]
    private ?string $status = null;

    #[ORM\Column(type: 'float')]
    private ?float $price = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $image_event = null;

    public function __construct()
    {
        $this->image_event = null;
    }

    // --- Getters / Setters (camelCase) ---

    public function getIdEvent(): ?int
    {
        return $this->id_event;
    }

    public function getTitle(): ?string
    {
        return $this->title;
    }

    public function setTitle(?string $title): self
    {
        $this->title = $title;
        return $this;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(?string $description): self
    {
        $this->description = $description;
        return $this;
    }

    public function getDateEvent(): ?\DateTimeInterface
    {
        return $this->date_event;
    }

    public function setDateEvent(?\DateTimeInterface $date_event): self
    {
        $this->date_event = $date_event;
        return $this;
    }

    public function getStartTime(): ?string
    {
        return $this->start_time;
    }

    public function setStartTime(?string $start_time): self
    {
        $this->start_time = $start_time;
        return $this;
    }

    public function getLocation(): ?string
    {
        return $this->location;
    }

    public function setLocation(?string $location): self
    {
        $this->location = $location;
        return $this;
    }

    public function getEventType(): ?string
    {
        return $this->event_type;
    }

    public function setEventType(?string $event_type): self
    {
        $this->event_type = $event_type;
        return $this;
    }

    public function getSeason(): ?string
    {
        return $this->season;
    }

    public function setSeason(?string $season): self
    {
        $this->season = $season;
        return $this;
    }

    public function getCapacity(): ?int
    {
        return $this->capacity;
    }

    public function setCapacity(?int $capacity): self
    {
        $this->capacity = $capacity;
        return $this;
    }

    public function getAvailablePlaces(): ?int
    {
        return $this->available_places;
    }

    public function setAvailablePlaces(?int $available_places): self
    {
        $this->available_places = $available_places;
        return $this;
    }

    public function getStatus(): ?string
    {
        return $this->status;
    }

    public function setStatus(?string $status): self
    {
        $this->status = $status;
        return $this;
    }

    public function getPrice(): ?float
    {
        return $this->price;
    }

    public function setPrice(?float $price): self
    {
        $this->price = $price;
        return $this;
    }

    public function getImageEvent(): ?string
    {
        return $this->image_event;
    }

    public function setImageEvent(?string $image_event): self
    {
        $this->image_event = $image_event;
        return $this;
    }

    // --- Aliases snake_case pour compatibilité avec les templates ---

    public function getId_event(): ?int
    {
        return $this->getIdEvent();
    }

    public function getImage_event(): ?string
    {
        return $this->getImageEvent();
    }

    public function setImage_event(?string $image_event): self
    {
        return $this->setImageEvent($image_event);
    }

    public function getAvailable_places(): ?int
    {
        return $this->getAvailablePlaces();
    }

    public function setAvailable_places(?int $available_places): self
    {
        return $this->setAvailablePlaces($available_places);
    }

    public function getDate_event(): ?\DateTimeInterface
    {
        return $this->getDateEvent();
    }

    public function setDate_event(?\DateTimeInterface $date_event): self
    {
        return $this->setDateEvent($date_event);
    }

    public function getStart_time(): ?string
    {
        return $this->getStartTime();
    }

    public function setStart_time(?string $start_time): self
    {
        return $this->setStartTime($start_time);
    }

    public function getEvent_type(): ?string
    {
        return $this->getEventType();
    }

    public function setEvent_type(?string $event_type): self
    {
        return $this->setEventType($event_type);
    }
}