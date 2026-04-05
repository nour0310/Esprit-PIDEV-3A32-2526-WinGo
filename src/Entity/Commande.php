<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

use App\Entity\Utilisateur;

#[ORM\Entity]
class Commande
{

    #[ORM\Id]
    #[ORM\Column(type: "integer")]
    private int $id_commande;

        #[ORM\ManyToOne(targetEntity: Utilisateur::class, inversedBy: "commandes")]
    #[ORM\JoinColumn(name: 'id_user', referencedColumnName: 'id', onDelete: 'CASCADE')]
    private Utilisateur $id_user;

    #[ORM\Column(type: "string")]
    private string $status;

    #[ORM\Column(type: "float")]
    private float $total;

    #[ORM\Column(type: "text")]
    private string $items_json;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date_commande;

    public function getId_commande()
    {
        return $this->id_commande;
    }

    public function setId_commande($value)
    {
        $this->id_commande = $value;
    }

    public function getId_user()
    {
        return $this->id_user;
    }

    public function setId_user($value)
    {
        $this->id_user = $value;
    }

    public function getStatus()
    {
        return $this->status;
    }

    public function setStatus($value)
    {
        $this->status = $value;
    }

    public function getTotal()
    {
        return $this->total;
    }

    public function setTotal($value)
    {
        $this->total = $value;
    }

    public function getItems_json()
    {
        return $this->items_json;
    }

    public function setItems_json($value)
    {
        $this->items_json = $value;
    }

    public function getDate_commande()
    {
        return $this->date_commande;
    }

    public function setDate_commande($value)
    {
        $this->date_commande = $value;
    }
}
