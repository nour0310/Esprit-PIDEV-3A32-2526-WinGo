<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

use Doctrine\Common\Collections\Collection;
use App\Entity\Article_tag;

#[ORM\Entity]
class Tag
{

    #[ORM\Id]
    #[ORM\Column(type: "integer")]
    private int $id;

    #[ORM\Column(type: "string", length: 50)]
    private string $nom;

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

    #[ORM\OneToMany(mappedBy: "tag_id", targetEntity: Article_tag::class)]
    private Collection $article_tags;

        public function getArticle_tags(): Collection
        {
            return $this->article_tags;
        }
    
        public function addArticle_tag(Article_tag $article_tag): self
        {
            if (!$this->article_tags->contains($article_tag)) {
                $this->article_tags[] = $article_tag;
                $article_tag->setTag_id($this);
            }
    
            return $this;
        }
    
        public function removeArticle_tag(Article_tag $article_tag): self
        {
            if ($this->article_tags->removeElement($article_tag)) {
                // set the owning side to null (unless already changed)
                if ($article_tag->getTag_id() === $this) {
                    $article_tag->setTag_id(null);
                }
            }
    
            return $this;
        }
}
