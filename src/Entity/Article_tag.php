<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

use App\Entity\Tag;

#[ORM\Entity]
class Article_tag
{

    #[ORM\Id]
        #[ORM\ManyToOne(targetEntity: Article::class, inversedBy: "article_tags")]
    #[ORM\JoinColumn(name: 'article_id', referencedColumnName: 'id', onDelete: 'CASCADE')]
    private Article $article_id;

    #[ORM\Id]
        #[ORM\ManyToOne(targetEntity: Tag::class, inversedBy: "article_tags")]
    #[ORM\JoinColumn(name: 'tag_id', referencedColumnName: 'id', onDelete: 'CASCADE')]
    private Tag $tag_id;

    public function getArticle_id()
    {
        return $this->article_id;
    }

    public function setArticle_id($value)
    {
        $this->article_id = $value;
    }

    public function getTag_id()
    {
        return $this->tag_id;
    }

    public function setTag_id($value)
    {
        $this->tag_id = $value;
    }
}
