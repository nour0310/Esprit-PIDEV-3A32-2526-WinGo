<?php

namespace App\Controller;

use App\Entity\Commande;
use App\Entity\Panier;
use App\Entity\Utilisateur;
use App\Repository\PanierRepository;
use App\Repository\ProduitRepository;
use App\Service\PromoService;
use App\Service\StockService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

final class PanierController extends AbstractController
{
    #[IsGranted('ROLE_USER')]
    #[Route('/panier', name: 'app_panier')]
    public function index(
        PanierRepository $panierRepository,
        ProduitRepository $produitRepository,
        PromoService $promoService
    ): Response {
        $user = $this->getCurrentUtilisateur();
        $userId = $this->getCurrentUtilisateurId($user);

        $paniers = $panierRepository->findBy(['idUser' => $userId], ['dateAjout' => 'DESC']);
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
        $reduction = $promoService->calculerReduction($subtotal, $livraison);
        $total = $subtotal + $livraison - $reduction;

        return $this->render('panier/index.html.twig', [
            'cartItems' => $cartItems,
            'cartCount' => count($cartItems),
            'subtotal' => $subtotal,
            'livraison' => $livraison,
            'reduction' => $reduction,
            'total' => $total,
            'promoCode' => $promoService->getCode(),
            'promoLabel' => $promoService->getLabel(),
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

        $produitId = $produit->getId();

        if ($produitId === null) {
            throw $this->createNotFoundException('Produit invalide.');
        }

        $user = $this->getCurrentUtilisateur();
        $userId = $this->getCurrentUtilisateurId($user);

        $existing = $panierRepository->findOneBy([
            'idUser' => $userId,
            'idProduit' => $produitId,
        ]);

        if ($existing) {
            $existing->setQuantite($existing->getQuantite() + 1);
            $em->flush();

            $this->addFlash('success', 'Quantité mise à jour dans le panier.');
        } else {
            $panier = new Panier();
            $panier->setIdUser($userId);
            $panier->setIdProduit($produitId);
            $panier->setQuantite(1);
            $panier->setPrixUnitaire((string) $produit->getPrix());
            $panier->setDateAjout(new \DateTime());

            $em->persist($panier);
            $em->flush();

            $this->addFlash('success', 'Produit ajouté au panier avec succès.');
        }

        return $this->redirectToRoute('produit_details', [
            'id' => $produitId,
        ]);
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/panier/increase/{id}', name: 'panier_increase', methods: ['POST'])]
    public function increase(
        int $id,
        PanierRepository $panierRepository,
        EntityManagerInterface $em
    ): JsonResponse {
        $user = $this->getCurrentUtilisateur();
        $userId = $this->getCurrentUtilisateurId($user);

        $ligne = $panierRepository->find($id);

        if (!$ligne instanceof Panier || $ligne->getIdUser() !== $userId) {
            return $this->json([
                'success' => false,
                'message' => 'Ligne panier introuvable.',
            ], 404);
        }

        $ligne->setQuantite($ligne->getQuantite() + 1);
        $em->flush();

        return $this->buildCartJson($panierRepository, $userId, $ligne);
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/panier/decrease/{id}', name: 'panier_decrease', methods: ['POST'])]
    public function decrease(
        int $id,
        PanierRepository $panierRepository,
        EntityManagerInterface $em
    ): JsonResponse {
        $user = $this->getCurrentUtilisateur();
        $userId = $this->getCurrentUtilisateurId($user);

        $ligne = $panierRepository->find($id);

        if (!$ligne instanceof Panier || $ligne->getIdUser() !== $userId) {
            return $this->json([
                'success' => false,
                'message' => 'Ligne panier introuvable.',
            ], 404);
        }

        if ($ligne->getQuantite() > 1) {
            $ligne->setQuantite($ligne->getQuantite() - 1);
            $em->flush();

            return $this->buildCartJson($panierRepository, $userId, $ligne);
        }

        return $this->json([
            'success' => true,
            'removed' => false,
            'quantity' => $ligne->getQuantite(),
            'lineTotal' => number_format((float) $ligne->getPrixUnitaire() * $ligne->getQuantite(), 2, '.', ''),
            'summary' => $this->buildSummary($panierRepository, $userId),
        ]);
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/panier/delete/{id}', name: 'panier_delete', methods: ['POST'])]
    public function delete(
        int $id,
        PanierRepository $panierRepository,
        EntityManagerInterface $em
    ): Response {
        $user = $this->getCurrentUtilisateur();
        $userId = $this->getCurrentUtilisateurId($user);

        $ligne = $panierRepository->find($id);

        if (!$ligne instanceof Panier || $ligne->getIdUser() !== $userId) {
            throw $this->createNotFoundException('Ligne panier introuvable.');
        }

        $em->remove($ligne);
        $em->flush();

        $this->addFlash('success', 'Produit supprimé du panier.');

        return $this->redirectToRoute('app_panier');
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/panier/promo', name: 'panier_appliquer_promo', methods: ['POST'])]
    public function appliquerPromo(
        Request $request,
        PromoService $promoService,
        PanierRepository $panierRepository
    ): JsonResponse {
        $token = (string) $request->request->get('_token', '');

        if (!$this->isCsrfTokenValid('appliquer_promo', $token)) {
            return $this->json([
                'success' => false,
                'message' => 'Token CSRF invalide.',
            ], 403);
        }

        $user = $this->getCurrentUtilisateur();
        $userId = $this->getCurrentUtilisateurId($user);

        $code = trim((string) $request->request->get('code', ''));

        if ($code === '') {
            return $this->json([
                'success' => false,
                'message' => 'Veuillez saisir un code promo.',
            ]);
        }

        $promotion = $promoService->appliquer($code, $userId);

        if (!$promotion) {
            return $this->json([
                'success' => false,
                'message' => 'Code invalide, expiré ou déjà utilisé.',
            ]);
        }

        $lignes = $panierRepository->findBy(['idUser' => $userId]);
        $subtotal = 0.0;

        foreach ($lignes as $ligne) {
            $subtotal += (float) $ligne->getPrixUnitaire() * $ligne->getQuantite();
        }

        $livraison = count($lignes) > 0 ? 7.00 : 0.00;
        $reduction = $promoService->calculerReduction($subtotal, $livraison);
        $total = $subtotal + $livraison - $reduction;

        return $this->json([
            'success' => true,
            'label' => $promotion->getLabel(),
            'reduction' => number_format($reduction, 2, '.', ''),
            'total' => number_format($total, 2, '.', ''),
        ]);
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/commande/valider', name: 'commande_valider', methods: ['POST'])]
    public function commander(
        PanierRepository $panierRepository,
        ProduitRepository $produitRepository,
        EntityManagerInterface $em,
        PromoService $promoService,
        StockService $stockService
    ): Response {
        $user = $this->getCurrentUtilisateur();
        $userId = $this->getCurrentUtilisateurId($user);

        $lignesPanier = $panierRepository->findBy(['idUser' => $userId]);

        if (!$lignesPanier) {
            $this->addFlash('error', 'Votre panier est vide.');

            return $this->redirectToRoute('app_panier');
        }

        $produitEnRupture = $stockService->verifierStock($lignesPanier);

        if ($produitEnRupture !== null) {
            $this->addFlash('error', 'Stock insuffisant pour "' . $produitEnRupture . '". Modifiez votre panier.');

            return $this->redirectToRoute('app_panier');
        }

        $items = [];
        $subtotal = 0.0;

        foreach ($lignesPanier as $ligne) {
            $produit = $produitRepository->find($ligne->getIdProduit());

            if (!$produit) {
                continue;
            }

            $prixUnitaire = (float) $ligne->getPrixUnitaire();
            $quantite = $ligne->getQuantite();
            $sousTotal = $prixUnitaire * $quantite;
            $subtotal += $sousTotal;

            $items[] = [
                'id_produit' => $produit->getId(),
                'nom' => $produit->getNom(),
                'prix_unitaire' => $prixUnitaire,
                'quantite' => $quantite,
                'sous_total' => $sousTotal,
                'image' => $produit->getImage(),
            ];
        }

        $livraison = 7.00;
        $reduction = $promoService->calculerReduction($subtotal, $livraison);
        $total = $subtotal + $livraison - $reduction;

        $itemsJson = json_encode($items, JSON_UNESCAPED_UNICODE);

        if ($itemsJson === false) {
            throw new \RuntimeException('Erreur lors de l’encodage JSON de la commande.');
        }

        $commande = new Commande();
        $commande->setIdUser($userId);
        $commande->setStatus('en_cours');
        $commande->setTotal(number_format($total, 2, '.', ''));
        $commande->setItemsJson($itemsJson);

        $em->persist($commande);

        $stockService->decrementerStock($lignesPanier);

        foreach ($lignesPanier as $ligne) {
            $em->remove($ligne);
        }

        $promoService->consommer($userId, $em);

        $em->flush();

        $this->addFlash('success', 'Commande enregistrée avec succès.');

        return $this->redirectToRoute('app_panier');
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/panier/promo/supprimer', name: 'panier_supprimer_promo', methods: ['GET'])]
    public function supprimerPromo(PromoService $promoService): Response
    {
        $promoService->vider();

        return $this->redirectToRoute('app_panier');
    }

    private function buildCartJson(PanierRepository $panierRepository, int $userId, Panier $ligne): JsonResponse
    {
        return $this->json([
            'success' => true,
            'removed' => false,
            'quantity' => $ligne->getQuantite(),
            'lineTotal' => number_format((float) $ligne->getPrixUnitaire() * $ligne->getQuantite(), 2, '.', ''),
            'summary' => $this->buildSummary($panierRepository, $userId),
        ]);
    }

    /**
     * @return array{
     *     cartCount: int,
     *     subtotal: string,
     *     livraison: string,
     *     reduction: string,
     *     total: string
     * }
     */
    private function buildSummary(PanierRepository $panierRepository, int $userId): array
    {
        $lignes = $panierRepository->findBy(['idUser' => $userId]);

        $subtotal = 0.0;
        $cartCount = count($lignes);

        foreach ($lignes as $ligne) {
            $subtotal += (float) $ligne->getPrixUnitaire() * $ligne->getQuantite();
        }

        $livraison = $cartCount > 0 ? 7.00 : 0.00;
        $reduction = 0.00;
        $total = $subtotal + $livraison - $reduction;

        return [
            'cartCount' => $cartCount,
            'subtotal' => number_format($subtotal, 2, '.', ''),
            'livraison' => number_format($livraison, 2, '.', ''),
            'reduction' => number_format($reduction, 2, '.', ''),
            'total' => number_format($total, 2, '.', ''),
        ];
    }

    private function getCurrentUtilisateur(): Utilisateur
    {
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        return $user;
    }

    private function getCurrentUtilisateurId(Utilisateur $user): int
    {
        $userId = $user->getId();

        if ($userId === null) {
            throw $this->createAccessDeniedException('Utilisateur invalide.');
        }

        return $userId;
    }
}