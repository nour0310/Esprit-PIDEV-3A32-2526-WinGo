<?php

namespace App\Controller;

use App\Repository\ProduitRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use App\Service\MerchantDashboardService;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

final class MerchantController extends AbstractController
{
    #[IsGranted('ROLE_USER')]
    #[Route('/merchant/dashboard', name: 'merchant_dashboard')]
    public function dashboard(
        ProduitRepository $produitRepository,
        MerchantDashboardService $dashboardService
    ): Response {
        /** @var \App\Entity\Utilisateur|null $user */
        $user = $this->getUser();

        if (!$user || strtoupper((string) ($user->getType() ?? '')) !== 'COMMERCANT') {
            $this->addFlash('warning', 'Cette page est réservée aux commerçants.');
            return $this->redirectToRoute('client_produits');
        }

        $mesProduits = $produitRepository->findBy(
            ['idUser' => $user->getId()],
            ['id' => 'DESC']
        );

        $stats = $dashboardService->buildStats($mesProduits);

        return $this->render('merchant/dashboard.html.twig', [
            'mesProduits' => $mesProduits,
            'totalProduits' => $stats['totalProduits'],
            'produitsEnStock' => $stats['produitsEnStock'],
            'produitsRupture' => $stats['produitsRupture'],
            'stockFaible' => $stats['stockFaible'],
            'valeurStock' => $stats['valeurStock'],
            'nombreCategories' => $stats['nombreCategories'],
            'dernierProduit' => $stats['dernierProduit'],
        ]);
    }
}