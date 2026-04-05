<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;


#[ORM\Entity]
class Commentaire
{

    #[ORM\Id]
    #[ORM\Column(type: "integer")]
    private int $id;

    #[ORM\Column(type: "text")]
    private string $contenu;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date_commentaire;

    #[ORM\Column(type: "integer")]
    private int $utilisateur;

    #[ORM\Column(type: "integer")]
    private int $article_id;

    #[ORM\Column(type: "integer")]
    private int $parent_id;

    public function getId()
    {
        return $this->id;
    }

    public function setId($value)
    {
        $this->id = $value;
    }

    public function getContenu()
    {
        return $this->contenu;
    }

    public function setContenu($value)
    {
        $this->contenu = $value;
    }

    public function getDate_commentaire()
    {
        return $this->date_commentaire;
    }

    public function setDate_commentaire($value)
    {
        $this->date_commentaire = $value;
    }

    public function getUtilisateur()
    {
        return $this->utilisateur;
    }

    public function setUtilisateur($value)
    {
        $this->utilisateur = $value;
    }

    public function getArticle_id()
    {
        return $this->article_id;
    }

    public function setArticle_id($value)
    {
        $this->article_id = $value;
    }

    public function getParent_id()
    {
        return $this->parent_id;
    }

    public function setParent_id($value)
    {
        $this->parent_id = $value;
    }
}
