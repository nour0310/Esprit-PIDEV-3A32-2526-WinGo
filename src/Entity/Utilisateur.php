<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

use Doctrine\Common\Collections\Collection;
use App\Entity\Produit;

#[ORM\Entity]
class Utilisateur
{

    #[ORM\Id]
    #[ORM\Column(type: "integer")]
    private int $id;

    #[ORM\Column(type: "string", length: 50)]
    private string $nom;

    #[ORM\Column(type: "string", length: 50)]
    private string $prenom;

    #[ORM\Column(type: "string", length: 100)]
    private string $email;

    #[ORM\Column(type: "string", length: 255)]
    private string $mot_de_passe;

    #[ORM\Column(type: "string", length: 50)]
    private string $type;

    #[ORM\Column(type: "bigint")]
    private string $telephone;

    #[ORM\Column(type: "integer")]
    private int $age;

    #[ORM\Column(type: "boolean")]
    private bool $is_verified;

    #[ORM\Column(type: "string", length: 10)]
    private string $verification_code;

    public function getId()
    {
        return $this->id;
    }

    public function setId($value)
    {
        $this->id = $value;
    }

    public function getNom()
    {
        return $this->nom;
    }

    public function setNom($value)
    {
        $this->nom = $value;
    }

    public function getPrenom()
    {
        return $this->prenom;
    }

    public function setPrenom($value)
    {
        $this->prenom = $value;
    }

    public function getEmail()
    {
        return $this->email;
    }

    public function setEmail($value)
    {
        $this->email = $value;
    }

    public function getMot_de_passe()
    {
        return $this->mot_de_passe;
    }

    public function setMot_de_passe($value)
    {
        $this->mot_de_passe = $value;
    }

    public function getType()
    {
        return $this->type;
    }

    public function setType($value)
    {
        $this->type = $value;
    }

    public function getTelephone()
    {
        return $this->telephone;
    }

    public function setTelephone($value)
    {
        $this->telephone = $value;
    }

    public function getAge()
    {
        return $this->age;
    }

    public function setAge($value)
    {
        $this->age = $value;
    }

    public function getIs_verified()
    {
        return $this->is_verified;
    }

    public function setIs_verified($value)
    {
        $this->is_verified = $value;
    }

    public function getVerification_code()
    {
        return $this->verification_code;
    }

    public function setVerification_code($value)
    {
        $this->verification_code = $value;
    }

    #[ORM\OneToMany(mappedBy: "id_user", targetEntity: Commande::class)]
    private Collection $commandes;

        public function getCommandes(): Collection
        {
            return $this->commandes;
        }
    
        public function addCommande(Commande $commande): self
        {
            if (!$this->commandes->contains($commande)) {
                $this->commandes[] = $commande;
                $commande->setId_user($this);
            }
    
            return $this;
        }
    
        public function removeCommande(Commande $commande): self
        {
            if ($this->commandes->removeElement($commande)) {
                // set the owning side to null (unless already changed)
                if ($commande->getId_user() === $this) {
                    $commande->setId_user(null);
                }
            }
    
            return $this;
        }

    #[ORM\OneToMany(mappedBy: "id_user", targetEntity: Produit::class)]
    private Collection $produits;
}
