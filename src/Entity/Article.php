<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

use Doctrine\Common\Collections\Collection;
use App\Entity\Article_tag;

#[ORM\Entity]
class Article
{

    #[ORM\Id]
    #[ORM\Column(type: "integer")]
    private int $id;

    #[ORM\Column(type: "string", length: 255)]
    private string $titre;

    #[ORM\Column(type: "text")]
    private string $contenu;

    #[ORM\Column(type: "datetime")]
    private \DateTimeInterface $date_publication;

    #[ORM\Column(type: "integer")]
    private int $auteur;

    #[ORM\Column(type: "string", length: 255)]
    private string $image;

    #[ORM\Column(type: "string", length: 100)]
    private string $region;

    #[ORM\Column(type: "string", length: 100)]
    private string $categorie;

    public function getId()
    {
        return $this->id;
    }

    public function setId($value)
    {
        $this->id = $value;
    }

    public function getTitre()
    {
        return $this->titre;
    }

    public function setTitre($value)
    {
        $this->titre = $value;
    }

    public function getContenu()
    {
        return $this->contenu;
    }

    public function setContenu($value)
    {
        $this->contenu = $value;
    }

    public function getDate_publication()
    {
        return $this->date_publication;
    }

    public function setDate_publication($value)
    {
        $this->date_publication = $value;
    }

    public function getAuteur()
    {
        return $this->auteur;
    }

    public function setAuteur($value)
    {
        $this->auteur = $value;
    }

    public function getImage()
    {
        return $this->image;
    }

    public function setImage($value)
    {
        $this->image = $value;
    }

    public function getRegion()
    {
        return $this->region;
    }

    public function setRegion($value)
    {
        $this->region = $value;
    }

    public function getCategorie()
    {
        return $this->categorie;
    }

    public function setCategorie($value)
    {
        $this->categorie = $value;
    }

    #[ORM\OneToMany(mappedBy: "article_id", targetEntity: Article_tag::class)]
    private Collection $article_tags;

        public function getArticle_tags(): Collection
        {
            return $this->article_tags;
        }
    
        public function addArticle_tag(Article_tag $article_tag): self
        {
            if (!$this->article_tags->contains($article_tag)) {
                $this->article_tags[] = $article_tag;
                $article_tag->setArticle_id($this);
            }
    
            return $this;
        }
    
        public function removeArticle_tag(Article_tag $article_tag): self
        {
            if ($this->article_tags->removeElement($article_tag)) {
                // set the owning side to null (unless already changed)
                if ($article_tag->getArticle_id() === $this) {
                    $article_tag->setArticle_id(null);
                }
            }
    
            return $this;
        }
}
