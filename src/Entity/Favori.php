<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;


#[ORM\Entity]
class Favori
{

    #[ORM\Id]
    #[ORM\Column(type: "integer")]
    private int $utilisateur_id;

    #[ORM\Id]
    #[ORM\Column(type: "integer")]
    private int $article_id;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date_ajout;

    public function getUtilisateur_id()
    {
        return $this->utilisateur_id;
    }

    public function setUtilisateur_id($value)
    {
        $this->utilisateur_id = $value;
    }

    public function getArticle_id()
    {
        return $this->article_id;
    }

    public function setArticle_id($value)
    {
        $this->article_id = $value;
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
