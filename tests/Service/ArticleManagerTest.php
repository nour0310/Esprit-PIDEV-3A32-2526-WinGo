<?php

namespace App\Tests\Service;

use App\Entity\Article;
use App\Service\ArticleManager;
use PHPUnit\Framework\TestCase;

class ArticleManagerTest extends TestCase
{
    /**
     * Test 1 : Un article valide passe la validation
     */
    public function testValidArticle(): void
    {
        $article = new Article();
        $article->setTitre('Mon Voyage en Tunisie');
        $article->setContenu('Un superbe voyage à travers les régions de la Tunisie');

        $manager = new ArticleManager();
        $this->assertTrue($manager->validate($article));
    }

    /**
     * Test 2 : Un article sans titre est rejeté
     */
    public function testArticleWithoutTitre(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le titre est obligatoire');

        $article = new Article();
        $article->setTitre('');
        $article->setContenu('Contenu de test');

        $manager = new ArticleManager();
        $manager->validate($article);
    }

    /**
     * Test 3 : Un article avec titre trop court est rejeté
     */
    public function testArticleWithShortTitre(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le titre doit contenir au moins 3 caractères');

        $article = new Article();
        $article->setTitre('AB');
        $article->setContenu('Contenu de test');

        $manager = new ArticleManager();
        $manager->validate($article);
    }

    /**
     * Test 4 : Un article sans contenu est rejeté
     */
    public function testArticleWithoutContenu(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le contenu est obligatoire');

        $article = new Article();
        $article->setTitre('Titre valide');
        $article->setContenu('');

        $manager = new ArticleManager();
        $manager->validate($article);
    }

    /**
     * Test 5 : Un article avec titre contenant des chiffres est rejeté
     */
    public function testArticleWithDigitsInTitle(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le titre ne doit pas contenir de chiffres');

        $article = new Article();
        $article->setTitre('Voyage 2026');
        $article->setContenu('Contenu de test valide');

        $manager = new ArticleManager();
        $manager->validate($article);
    }
}
