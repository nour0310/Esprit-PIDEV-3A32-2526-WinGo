<?php

namespace App\Controller;

use App\Repository\ProduitRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

final class MerchantController extends AbstractController
{
    #[IsGranted('ROLE_USER')]
    #[Route('/merchant/dashboard', name: 'merchant_dashboard')]
    public function dashboard(ProduitRepository $produitRepository): Response
    {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        if (($user->getType() ?? '') !== 'COMMERCANT') {
            $this->addFlash('success', 'Cette page est réservée aux commerçants.');
            return $this->redirectToRoute('client_produits');
        }

        $mesProduits = $produitRepository->findBy(
            ['idUser' => $user->getId()],
            ['id' => 'DESC']
        );

        $totalProduits = count($mesProduits);
        $produitsEnStock = 0;
        $produitsRupture = 0;
        $stockFaible = 0;
        $valeurStock = 0;
        $categories = [];
        $dernierProduit = null;

        foreach ($mesProduits as $produit) {
            if ($produit->getStock() > 0) {
                $produitsEnStock++;
            } else {
                $produitsRupture++;
            }

            if ($produit->getStock() > 0 && $produit->getStock() <= 5) {
                $stockFaible++;
            }

            $valeurStock += ((float) $produit->getPrix()) * ((int) $produit->getStock());

            if ($produit->getCategorie()) {
                $categories[] = $produit->getCategorie();
            }

            if (
                $produit->getDateAjout() !== null &&
                (
                    $dernierProduit === null ||
                    $produit->getDateAjout() > $dernierProduit->getDateAjout()
                )
            ) {
                $dernierProduit = $produit;
            }
        }

        $nombreCategories = count(array_unique($categories));

        return $this->render('merchant/dashboard.html.twig', [
            'mesProduits' => $mesProduits,
            'totalProduits' => $totalProduits,
            'produitsEnStock' => $produitsEnStock,
            'produitsRupture' => $produitsRupture,
            'stockFaible' => $stockFaible,
            'valeurStock' => $valeurStock,
            'nombreCategories' => $nombreCategories,
            'dernierProduit' => $dernierProduit,
        ]);
    }
}