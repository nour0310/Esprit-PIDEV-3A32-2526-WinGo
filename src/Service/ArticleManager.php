<?php

namespace App\Service;

use App\Entity\Article;

class ArticleManager
{
    /**
     * Valide les règles métier d'un article.
     *
     * Règles :
     * 1. Le titre est obligatoire et doit contenir au moins 3 caractères
     * 2. Le contenu est obligatoire et doit contenir au moins 3 caractères
     * 3. Le titre ne doit pas contenir de chiffres
     */
    public function validate(Article $article): bool
    {
        // Règle 1 : Le titre est obligatoire
        if (empty($article->getTitre())) {
            throw new \InvalidArgumentException('Le titre est obligatoire');
        }

        if (strlen($article->getTitre()) < 3) {
            throw new \InvalidArgumentException('Le titre doit contenir au moins 3 caractères');
        }

        // Règle 2 : Le contenu est obligatoire
        if (empty($article->getContenu())) {
            throw new \InvalidArgumentException('Le contenu est obligatoire');
        }

        if (strlen($article->getContenu()) < 3) {
            throw new \InvalidArgumentException('Le contenu doit contenir au moins 3 caractères');
        }

        // Règle 3 : Le titre ne doit pas contenir de chiffres
        if (preg_match('/\d/', $article->getTitre())) {
            throw new \InvalidArgumentException('Le titre ne doit pas contenir de chiffres');
        }

        return true;
    }
}
