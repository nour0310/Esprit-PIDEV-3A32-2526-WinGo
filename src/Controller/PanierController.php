<?php

namespace App\Controller;

use App\Entity\Panier;
use App\Repository\ProduitRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

final class PanierController extends AbstractController
{
    #[Route('/panier', name: 'app_panier')]
    public function index(): Response
    {
        return $this->render('panier/index.html.twig', [
            'controller_name' => 'PanierController',
        ]);
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/panier/add/{id}', name: 'panier_add', methods: ['POST'])]
    public function add(
        int $id,
        ProduitRepository $produitRepository,
        EntityManagerInterface $em
    ): Response {
        $produit = $produitRepository->find($id);

        if (!$produit) {
            throw $this->createNotFoundException('Produit introuvable.');
        }

        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        $panier = new Panier();
        $panier->setIdUser($user->getId());
        $panier->setIdProduit($produit->getId());
        $panier->setQuantite(1);
        $panier->setPrixUnitaire((string) $produit->getPrix());
        $panier->setDateAjout(new \DateTime());

        $em->persist($panier);
        $em->flush();

        $this->addFlash('success', 'Produit ajouté au panier avec succès.');

        return $this->redirectToRoute('produit_details', ['id' => $produit->getId()]);
    }
}