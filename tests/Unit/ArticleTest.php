<?php

namespace App\Tests\Unit;

use App\Entity\Article;
use PHPUnit\Framework\TestCase;

class ArticleTest extends TestCase
{
    public function testArticleInstantiation(): void
    {
        $article = new Article();
        $this->assertInstanceOf(Article::class, $article);
    }

    public function testTitreGetterAndSetter(): void
    {
        $article = new Article();
        $titre = "Titre de test";
        $article->setTitre($titre);
        $this->assertEquals($titre, $article->getTitre());
    }
}
