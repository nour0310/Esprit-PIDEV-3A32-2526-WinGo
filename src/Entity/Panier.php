<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;


#[ORM\Entity]
class Panier
{

    #[ORM\Id]
    #[ORM\Column(type: "integer")]
    private int $id_panier;

    #[ORM\Column(type: "integer")]
    private int $id_user;

    #[ORM\Column(type: "integer")]
    private int $id_produit;

    #[ORM\Column(type: "integer")]
    private int $quantite;

    #[ORM\Column(type: "float")]
    private float $prix_unitaire;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date_ajout;

    public function getId_panier()
    {
        return $this->id_panier;
    }

    public function setId_panier($value)
    {
        $this->id_panier = $value;
    }

    public function getId_user()
    {
        return $this->id_user;
    }

    public function setId_user($value)
    {
        $this->id_user = $value;
    }

    public function getId_produit()
    {
        return $this->id_produit;
    }

    public function setId_produit($value)
    {
        $this->id_produit = $value;
    }

    public function getQuantite()
    {
        return $this->quantite;
    }

    public function setQuantite($value)
    {
        $this->quantite = $value;
    }

    public function getPrix_unitaire()
    {
        return $this->prix_unitaire;
    }

    public function setPrix_unitaire($value)
    {
        $this->prix_unitaire = $value;
    }

    public function getDate_ajout()
    {
        return $this->date_ajout;
    }

    public function setDate_ajout($value)
    {
        $this->date_ajout = $value;
    }
}
