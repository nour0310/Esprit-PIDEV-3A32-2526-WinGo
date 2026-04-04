<?php

namespace App\Controller;

use App\Entity\Panier;
use App\Repository\PanierRepository;
use App\Repository\ProduitRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

final class PanierController extends AbstractController
{
    #[IsGranted('ROLE_USER')]
    #[Route('/panier', name: 'app_panier')]
    public function index(
        PanierRepository $panierRepository,
        ProduitRepository $produitRepository
    ): Response {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        $paniers = $panierRepository->findBy(
            ['idUser' => $user->getId()],
            ['dateAjout' => 'DESC']
        );

        $cartItems = [];
        $subtotal = 0.0;

        foreach ($paniers as $ligne) {
            $produit = $produitRepository->find($ligne->getIdProduit());

            if (!$produit) {
                continue;
            }

            $lineTotal = (float) $ligne->getPrixUnitaire() * $ligne->getQuantite();
            $subtotal += $lineTotal;

            $cartItems[] = [
                'panier' => $ligne,
                'produit' => $produit,
                'lineTotal' => $lineTotal,
            ];
        }

        $livraison = count($cartItems) > 0 ? 7.00 : 0.00;
        $reduction = 0.00;
        $total = $subtotal + $livraison - $reduction;

        return $this->render('panier/index.html.twig', [
            'cartItems' => $cartItems,
            'cartCount' => count($cartItems),
            'subtotal' => $subtotal,
            'livraison' => $livraison,
            'reduction' => $reduction,
            'total' => $total,
        ]);
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/panier/add/{id}', name: 'panier_add', methods: ['POST'])]
    public function add(
        int $id,
        ProduitRepository $produitRepository,
        EntityManagerInterface $em,
        PanierRepository $panierRepository
    ): Response {
        $produit = $produitRepository->find($id);

        if (!$produit) {
            throw $this->createNotFoundException('Produit introuvable.');
        }

        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        $existing = $panierRepository->findOneBy([
            'idUser' => $user->getId(),
            'idProduit' => $produit->getId(),
        ]);

        if ($existing) {
            $existing->setQuantite($existing->getQuantite() + 1);
            $em->flush();

            $this->addFlash('success', 'Quantité mise à jour dans le panier.');
        } else {
            $panier = new Panier();
            $panier->setIdUser($user->getId());
            $panier->setIdProduit($produit->getId());
            $panier->setQuantite(1);
            $panier->setPrixUnitaire((string) $produit->getPrix());
            $panier->setDateAjout(new \DateTime());

            $em->persist($panier);
            $em->flush();

            $this->addFlash('success', 'Produit ajouté au panier avec succès.');
        }

        return $this->redirectToRoute('produit_details', ['id' => $produit->getId()]);
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/panier/increase/{id}', name: 'panier_increase', methods: ['POST'])]
    public function increase(
        int $id,
        PanierRepository $panierRepository,
        EntityManagerInterface $em
    ): Response {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        $ligne = $panierRepository->find($id);

        if (!$ligne || $ligne->getIdUser() !== $user->getId()) {
            throw $this->createNotFoundException('Ligne panier introuvable.');
        }

        $ligne->setQuantite($ligne->getQuantite() + 1);
        $em->flush();

        return $this->redirectToRoute('app_panier');
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/panier/decrease/{id}', name: 'panier_decrease', methods: ['POST'])]
    public function decrease(
        int $id,
        PanierRepository $panierRepository,
        EntityManagerInterface $em
    ): Response {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        $ligne = $panierRepository->find($id);

        if (!$ligne || $ligne->getIdUser() !== $user->getId()) {
            throw $this->createNotFoundException('Ligne panier introuvable.');
        }

        if ($ligne->getQuantite() > 1) {
            $ligne->setQuantite($ligne->getQuantite() - 1);
            $em->flush();
        }

        return $this->redirectToRoute('app_panier');
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/panier/delete/{id}', name: 'panier_delete', methods: ['POST'])]
    public function delete(
        int $id,
        PanierRepository $panierRepository,
        EntityManagerInterface $em
    ): Response {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        $ligne = $panierRepository->find($id);

        if (!$ligne || $ligne->getIdUser() !== $user->getId()) {
            throw $this->createNotFoundException('Ligne panier introuvable.');
        }

        $em->remove($ligne);
        $em->flush();

        $this->addFlash('success', 'Produit supprimé du panier.');

        return $this->redirectToRoute('app_panier');
    }
}