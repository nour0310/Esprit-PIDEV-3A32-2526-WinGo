<?php

namespace App\Service;

use App\Entity\Promotion;
use App\Repository\PromotionRepository;
use Symfony\Component\HttpFoundation\RequestStack;

class PromoService
{
    public function __construct(
        private PromotionRepository $promotionRepository,
        private RequestStack        $requestStack,
    ) {}

    /**
     * Valide et stocke le code promo en session.
     * Retourne la Promotion si valide, null sinon.
     */
    public function appliquer(string $code, int $idUser): ?Promotion
    {
        $promotion = $this->promotionRepository->findValidByCodeAndUser(
            strtoupper(trim($code)),
            $idUser
        );

        if (!$promotion) {
            return null;
        }

        $session = $this->requestStack->getSession();
        $session->set('promo_code',  $promotion->getCode());
        $session->set('promo_type',  $promotion->getType());
        $session->set('promo_label', $promotion->getLabel());

        return $promotion;
    }

    /**
     * Calcule la réduction selon le type de promo et le subtotal réel.
     */
    public function calculerReduction(float $subtotal, float $livraison): float
    {
        $type = $this->requestStack->getSession()->get('promo_type');

        return match ($type) {
            'reduction_pourcent' => round($subtotal * 0.05, 2),
            'remise_fixe'        => 5.00,
            'livraison_gratuite' => $livraison,
            default              => 0.00,
        };
    }

    public function getCode(): ?string
    {
        return $this->requestStack->getSession()->get('promo_code');
    }

    public function getLabel(): ?string
    {
        return $this->requestStack->getSession()->get('promo_label');
    }

    /**
     * Marque la promo comme utilisée en BDD et vide la session.
     */
    public function consommer(int $idUser, \Doctrine\ORM\EntityManagerInterface $em): void
    {
        $code = $this->getCode();

        if ($code) {
            $promotion = $this->promotionRepository->findValidByCodeAndUser($code, $idUser);
            if ($promotion) {
                $promotion->setIsUsed(true);
                $em->flush();
            }
        }

        $this->vider();
    }

    public function vider(): void
    {
        $session = $this->requestStack->getSession();
        $session->remove('promo_code');
        $session->remove('promo_type');
        $session->remove('promo_label');
    }
}