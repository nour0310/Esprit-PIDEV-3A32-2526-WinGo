<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;


#[ORM\Entity]
class Notification
{

    #[ORM\Id]
    #[ORM\Column(type: "integer")]
    private int $id;

    #[ORM\Column(type: "integer")]
    private int $utilisateur_id;

    #[ORM\Column(type: "integer")]
    private int $emetteur_id;

    #[ORM\Column(type: "string", length: 50)]
    private string $type;

    #[ORM\Column(type: "text")]
    private string $contenu;

    #[ORM\Column(type: "string", length: 255)]
    private string $lien;

    #[ORM\Column(type: "boolean")]
    private bool $lu;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date_creation;

    public function getId()
    {
        return $this->id;
    }

    public function setId($value)
    {
        $this->id = $value;
    }

    public function getUtilisateur_id()
    {
        return $this->utilisateur_id;
    }

    public function setUtilisateur_id($value)
    {
        $this->utilisateur_id = $value;
    }

    public function getEmetteur_id()
    {
        return $this->emetteur_id;
    }

    public function setEmetteur_id($value)
    {
        $this->emetteur_id = $value;
    }

    public function getType()
    {
        return $this->type;
    }

    public function setType($value)
    {
        $this->type = $value;
    }

    public function getContenu()
    {
        return $this->contenu;
    }

    public function setContenu($value)
    {
        $this->contenu = $value;
    }

    public function getLien()
    {
        return $this->lien;
    }

    public function setLien($value)
    {
        $this->lien = $value;
    }

    public function getLu()
    {
        return $this->lu;
    }

    public function setLu($value)
    {
        $this->lu = $value;
    }

    public function getDate_creation()
    {
        return $this->date_creation;
    }

    public function setDate_creation($value)
    {
        $this->date_creation = $value;
    }
}
