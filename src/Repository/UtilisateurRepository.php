<?php

namespace App\Repository;

use App\Entity\Utilisateur;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Utilisateur>
 */
class UtilisateurRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Utilisateur::class);
    }

    /**
     * @return Utilisateur[]
     */
    public function searchAndSort(?string $query, string $sort, string $direction): array
    {
        $qb = $this->createQueryBuilder('u');

        if ($query) {
            $qb->andWhere('u.nom LIKE :q OR u.prenom LIKE :q OR u.email LIKE :q')
               ->setParameter('q', '%' . $query . '%');
        }

        $allowedSorts = ['id', 'nom', 'email', 'age', 'type'];
        if (in_array($sort, $allowedSorts, true)) {
            $qb->orderBy('u.' . $sort, strtoupper($direction) === 'DESC' ? 'DESC' : 'ASC');
        }

        return $qb->getQuery()->getResult();
    }

    public function calculateReliabilityScore(Utilisateur $user): int
    {
        $conn = $this->getEntityManager()->getConnection();

        $sql = "SELECT 
                    SUM(CASE WHEN status = 'livree' THEN 1 ELSE 0 END) as delivered,
                    SUM(CASE WHEN status = 'annulee' THEN 1 ELSE 0 END) as cancelled
                FROM commande 
                WHERE id_user = :userId";

        $result = $conn->executeQuery($sql, ['userId' => $user->getId()])->fetchAssociative();

        $delivered = is_array($result) ? (int) ($result['delivered'] ?? 0) : 0;
        $cancelled = is_array($result) ? (int) ($result['cancelled'] ?? 0) : 0;
        $total = $delivered + $cancelled;

        if ($total === 0) {
            return 80;
        }

        return (int) round(($delivered / $total) * 100);
    }

    /**
     * @return array<string, int>
     */
    public function getAgeStats(): array
    {
        $conn = $this->getEntityManager()->getConnection();

        $sql = "SELECT 
                    SUM(CASE WHEN age < 18 THEN 1 ELSE 0 END) as 'below_18',
                    SUM(CASE WHEN age >= 18 AND age <= 25 THEN 1 ELSE 0 END) as '18_25',
                    SUM(CASE WHEN age > 25 AND age <= 40 THEN 1 ELSE 0 END) as '26_40',
                    SUM(CASE WHEN age > 40 AND age <= 60 THEN 1 ELSE 0 END) as '41_60',
                    SUM(CASE WHEN age > 60 AND age <= 80 THEN 1 ELSE 0 END) as '61_80',
                    SUM(CASE WHEN age > 80 THEN 1 ELSE 0 END) as 'above_80'
                FROM utilisateur";

        $result = $conn->executeQuery($sql)->fetchAssociative();

        if (!is_array($result)) {
            $result = [];
        }

        return [
            '< 18' => (int) ($result['below_18'] ?? 0),
            '18 - 25' => (int) ($result['18_25'] ?? 0),
            '26 - 40' => (int) ($result['26_40'] ?? 0),
            '41 - 60' => (int) ($result['41_60'] ?? 0),
            '61 - 80' => (int) ($result['61_80'] ?? 0),
            '> 80' => (int) ($result['above_80'] ?? 0),
        ];
    }

    /**
     * @return array{
     *     total: int,
     *     admins: int,
     *     merchants: int,
     *     clients: int
     * }
     */
    public function getCountsByRole(): array
    {
        return [
            'total' => $this->count([]),
            'admins' => $this->count(['type' => 'ADMIN']),
            'merchants' => $this->count(['type' => 'COMMERCANT']),
            'clients' => $this->count(['type' => 'CLIENT']),
        ];
    }

    public function canBeSafelyDeleted(Utilisateur $user): bool
    {
        if (strtoupper($user->getType() ?? '') === 'ADMIN') {
            $adminCount = $this->count(['type' => 'ADMIN']);

            if ($adminCount <= 1) {
                return false;
            }
        }

        $conn = $this->getEntityManager()->getConnection();

        $sql = "SELECT COUNT(*) FROM commande WHERE id_user = :userId AND status NOT IN ('livree', 'annulee')";
        $ongoingOrdersCount = $conn->executeQuery($sql, ['userId' => $user->getId()])->fetchOne();

        if ((int) $ongoingOrdersCount > 0) {
            return false;
        }

        return true;
    }
}