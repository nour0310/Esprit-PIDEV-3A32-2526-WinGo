<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;


#[ORM\Entity]
class Rating
{

    #[ORM\Id]
    #[ORM\Column(type: "integer")]
    private int $id;

    #[ORM\Column(type: "integer")]
    private int $utilisateur_id;

    #[ORM\Column(type: "integer")]
    private int $article_id;

    #[ORM\Column(type: "integer")]
    private int $note;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date_rating;

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

    public function getArticle_id()
    {
        return $this->article_id;
    }

    public function setArticle_id($value)
    {
        $this->article_id = $value;
    }

    public function getNote()
    {
        return $this->note;
    }

    public function setNote($value)
    {
        $this->note = $value;
    }

    public function getDate_rating()
    {
        return $this->date_rating;
    }

    public function setDate_rating($value)
    {
        $this->date_rating = $value;
    }
}
