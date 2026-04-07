<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;
use App\Entity\Event;

#[ORM\Entity]
class Participation
{

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private int $id_participation;

    #[ORM\ManyToOne(targetEntity: Event::class, inversedBy: "participations")]
    #[ORM\JoinColumn(name: 'id_event', referencedColumnName: 'id_event', onDelete: 'CASCADE')]
    #[Assert\NotNull(message: "L'événement est obligatoire")]
    private Event $id_event;

    #[ORM\Column(type: "integer")]
    #[Assert\NotBlank(message: "L'ID de l'utilisateur est obligatoire")]
    private int $id_user;

    #[ORM\Column(type: "datetime")]
    #[Assert\NotBlank(message: "La date de participation est obligatoire")]
    #[Assert\Type("\DateTimeInterface")]
    private \DateTimeInterface $date_participation;

    #[ORM\Column(type: "string", length: 50)]
    #[Assert\NotBlank(message: "Le statut est obligatoire")]
    private string $statut;

    #[ORM\Column(type: "string", length: 100)]
    #[Assert\NotBlank(message: "Le nom est obligatoire")]
    private string $nom_participant;

    #[ORM\Column(type: "string", length: 100)]
    #[Assert\NotBlank(message: "Le prénom est obligatoire")]
    private string $prenom_participant;

    #[ORM\Column(type: "string", length: 150)]
    #[Assert\NotBlank(message: "L'email est obligatoire")]
    #[Assert\Email(message: "L'email '{{ value }}' n'est pas un email valide.")]
    private string $email_participant;

    #[ORM\Column(type: "string", length: 30)]
    #[Assert\NotBlank(message: "Le téléphone est obligatoire")]
    #[Assert\Length(min: 8, minMessage: "Le téléphone doit avoir au moins {{ limit }} chiffres")]
    private string $telephone;

    #[ORM\Column(type: "integer")]
    #[Assert\NotBlank(message: "Le nombre de places est obligatoire")]
    #[Assert\Positive(message: "Le nombre de places doit être positif")]
    private int $nombre_places;

    public function getId_participation()
    {
        return $this->id_participation;
    }

    public function setId_participation($value)
    {
        $this->id_participation = $value;
    }

    public function getId_event()
    {
        return $this->id_event;
    }

    public function setId_event($value)
    {
        $this->id_event = $value;
    }

    public function getId_user()
    {
        return $this->id_user;
    }

    public function setId_user($value)
    {
        $this->id_user = $value;
    }

    public function getDate_participation()
    {
        return $this->date_participation;
    }

    public function setDate_participation($value)
    {
        $this->date_participation = $value;
    }

    public function getStatut()
    {
        return $this->statut;
    }

    public function setStatut($value)
    {
        $this->statut = $value;
    }

    public function getNom_participant()
    {
        return $this->nom_participant;
    }

    public function setNom_participant($value)
    {
        $this->nom_participant = $value;
    }

    public function getPrenom_participant()
    {
        return $this->prenom_participant;
    }

    public function setPrenom_participant($value)
    {
        $this->prenom_participant = $value;
    }

    public function getEmail_participant()
    {
        return $this->email_participant;
    }

    public function setEmail_participant($value)
    {
        $this->email_participant = $value;
    }

    public function getTelephone()
    {
        return $this->telephone;
    }

    public function setTelephone($value)
    {
        $this->telephone = $value;
    }

    public function getNombre_places()
    {
        return $this->nombre_places;
    }

    public function setNombre_places($value)
    {
        $this->nombre_places = $value;
    }
}
