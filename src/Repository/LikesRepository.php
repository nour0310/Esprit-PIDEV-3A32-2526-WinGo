<?php

namespace App\Repository;

use App\Entity\Likes;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class LikesRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Likes::class);
    }

    /**
     * Compte le nombre de likes pour un article donné.
     */
    public function countLikesForArticle(int $articleId): int
    {
        return $this->createQueryBuilder('l')
            ->select('COUNT(l.id)')
            ->where('l.articleId = :articleId')
            ->setParameter('articleId', $articleId)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Vérifie si un utilisateur a déjà liké un article.
     */
    public function hasUserLiked(int $userId, int $articleId): bool
    {
        $result = $this->createQueryBuilder('l')
            ->select('COUNT(l.id)')
            ->where('l.utilisateurId = :userId')
            ->andWhere('l.articleId = :articleId')
            ->setParameter('userId', $userId)
            ->setParameter('articleId', $articleId)
            ->getQuery()
            ->getSingleScalarResult();

        return $result > 0;
    }

    /**
     * Trouve un like spécifique d'un utilisateur sur un article.
     */
    public function findOneByUserAndArticle(int $userId, int $articleId): ?Likes
    {
        return $this->findOneBy([
            'utilisateurId' => $userId,
            'articleId'     => $articleId,
        ]);
    }
}