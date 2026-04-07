<?php

namespace App\Repository;

use App\Entity\Produit;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Produit>
 */
class ProduitRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Produit::class);
    }

    public function findByFilters(
        ?string $q = null,
        ?string $categorie = null,
        ?string $region = null,
        ?string $sort = null
    ): array {
        $qb = $this->createQueryBuilder('p');

        if ($q !== null && trim($q) !== '') {
            $qb->andWhere('LOWER(p.nom) LIKE LOWER(:q)')
               ->setParameter('q', '%' . trim($q) . '%');
        }

        if ($categorie !== null && trim($categorie) !== '') {
            $qb->andWhere('p.categorie = :categorie')
               ->setParameter('categorie', trim($categorie));
        }

        if ($region !== null && trim($region) !== '') {
            $qb->andWhere('p.region = :region')
               ->setParameter('region', trim($region));
        }

        switch ($sort) {
            case 'recent':
                $qb->orderBy('p.dateAjout', 'DESC');
                break;
            case 'ancien':
                $qb->orderBy('p.dateAjout', 'ASC');
                break;
            default:
                $qb->orderBy('p.id', 'DESC');
                break;
        }

        return $qb->getQuery()->getResult();
    }
}