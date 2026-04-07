<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

use Doctrine\Common\Collections\Collection;
use App\Entity\Participation;

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

    public function getId_event()
    {
        return $this->id_event;
    }

    public function setId_event($value)
    {
        $this->id_event = $value;
    }

    public function getTitle()
    {
        return $this->title;
    }

    public function setTitle($value)
    {
        $this->title = $value;
    }

    public function getDescription()
    {
        return $this->description;
    }

    public function setDescription($value)
    {
        $this->description = $value;
    }

    public function getDate_event()
    {
        return $this->date_event;
    }

    public function setDate_event($value)
    {
        $this->date_event = $value;
    }

    public function getStart_time()
    {
        return $this->start_time;
    }

    public function setStart_time($value)
    {
        $this->start_time = $value;
    }

    public function getLocation()
    {
        return $this->location;
    }

    public function setLocation($value)
    {
        $this->location = $value;
    }

    public function getEvent_type()
    {
        return $this->event_type;
    }

    public function setEvent_type($value)
    {
        $this->event_type = $value;
    }

    public function getSeason()
    {
        return $this->season;
    }

    public function setSeason($value)
    {
        $this->season = $value;
    }

    public function getCapacity()
    {
        return $this->capacity;
    }

    public function setCapacity($value)
    {
        $this->capacity = $value;
    }

    public function getAvailable_places()
    {
        return $this->available_places;
    }

    public function setAvailable_places($value)
    {
        $this->available_places = $value;
    }

    public function getStatus()
    {
        return $this->status;
    }

    public function setStatus($value)
    {
        $this->status = $value;
    }

    public function getImage_event()
    {
        return $this->image_event;
    }

    public function setImage_event($value)
    {
        $this->image_event = $value;
    }

    public function getPrice()
    {
        return $this->price;
    }

    public function setPrice($value)
    {
        $this->price = $value;
    }

    #[ORM\OneToMany(mappedBy: "id_event", targetEntity: Participation::class)]
    private Collection $participations;

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
                // set the owning side to null (unless already changed)
                if ($participation->getId_event() === $this) {
                    $participation->setId_event(null);
                }
            }
    
            return $this;
        }
}
