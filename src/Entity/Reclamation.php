<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;


#[ORM\Entity]
class Reclamation
{

    #[ORM\Column(type: "integer")]
    private int $id_reclamation;

    #[ORM\Column(type: "integer")]
    private int $id_user;

    #[ORM\Column(type: "string", length: 50)]
    private string $type_reclamation;

    #[ORM\Column(type: "string", length: 100)]
    private string $sujet;

    #[ORM\Column(type: "text")]
    private string $description;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date_reclamation;

    #[ORM\Column(type: "string", length: 30)]
    private string $statut;

    #[ORM\Column(type: "string", length: 20)]
    private string $priorite;

    #[ORM\Column(type: "string", length: 255)]
    private string $piece_jointe;

    #[ORM\Column(type: "text")]
    private string $reponse_admin;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date_reponse;

    public function getId_reclamation()
    {
        return $this->id_reclamation;
    }

    public function setId_reclamation($value)
    {
        $this->id_reclamation = $value;
    }

    public function getId_user()
    {
        return $this->id_user;
    }

    public function setId_user($value)
    {
        $this->id_user = $value;
    }

    public function getType_reclamation()
    {
        return $this->type_reclamation;
    }

    public function setType_reclamation($value)
    {
        $this->type_reclamation = $value;
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

    public function getDate_reclamation()
    {
        return $this->date_reclamation;
    }

    public function setDate_reclamation($value)
    {
        $this->date_reclamation = $value;
    }

    public function getStatut()
    {
        return $this->statut;
    }

    public function setStatut($value)
    {
        $this->statut = $value;
    }

    public function getPriorite()
    {
        return $this->priorite;
    }

    public function setPriorite($value)
    {
        $this->priorite = $value;
    }

    public function getPiece_jointe()
    {
        return $this->piece_jointe;
    }

    public function setPiece_jointe($value)
    {
        $this->piece_jointe = $value;
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
}
