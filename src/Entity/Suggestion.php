<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;


#[ORM\Entity]
class Suggestion
{

    #[ORM\Column(type: "integer")]
    private int $id_suggestion;

    #[ORM\Column(type: "integer")]
    private int $id_user;

    #[ORM\Column(type: "string", length: 100)]
    private string $sujet;

    #[ORM\Column(type: "text")]
    private string $description;

    #[ORM\Column(type: "string", length: 50)]
    private string $categorie;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date_suggestion;

    #[ORM\Column(type: "string", length: 30)]
    private string $statut;

    #[ORM\Column(type: "text")]
    private string $reponse_admin;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date_reponse;

    #[ORM\Column(type: "integer")]
    private int $id_reclamation;

    public function getId_suggestion()
    {
        return $this->id_suggestion;
    }

    public function setId_suggestion($value)
    {
        $this->id_suggestion = $value;
    }

    public function getId_user()
    {
        return $this->id_user;
    }

    public function setId_user($value)
    {
        $this->id_user = $value;
    }

    public function getSujet()
    {
        return $this->sujet;
    }

    public function setSujet($value)
    {
        $this->sujet = $value;
    }

    public function getDescription()
    {
        return $this->description;
    }

    public function setDescription($value)
    {
        $this->description = $value;
    }

    public function getCategorie()
    {
        return $this->categorie;
    }

    public function setCategorie($value)
    {
        $this->categorie = $value;
    }

    public function getDate_suggestion()
    {
        return $this->date_suggestion;
    }

    public function setDate_suggestion($value)
    {
        $this->date_suggestion = $value;
    }

    public function getStatut()
    {
        return $this->statut;
    }

    public function setStatut($value)
    {
        $this->statut = $value;
    }

    public function getReponse_admin()
    {
        return $this->reponse_admin;
    }

    public function setReponse_admin($value)
    {
        $this->reponse_admin = $value;
    }

    public function getDate_reponse()
    {
        return $this->date_reponse;
    }

    public function setDate_reponse($value)
    {
        $this->date_reponse = $value;
    }

    public function getId_reclamation()
    {
        return $this->id_reclamation;
    }

    public function setId_reclamation($value)
    {
        $this->id_reclamation = $value;
    }
}
