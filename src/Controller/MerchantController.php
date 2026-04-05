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

        return $this->render('merchant/dashboard.html.twig', [
            'mesProduits' => $mesProduits,
        ]);
    }
}