<?php

namespace App\Controller;

use App\Entity\Commande;
use App\Repository\ArticleRepository;
use App\Repository\CommandeRepository;
use App\Repository\ProduitRepository;
use App\Repository\ReclamationRepository;
use App\Repository\ReservationRepository;
use App\Repository\SuggestionRepository;
use App\Repository\TransportRepository;
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_ADMIN')]
#[Route('/admin')]
class AdminController extends AbstractController
{
    #[Route('', name: 'admin_dashboard')]
    #[Route('/dashboard', name: 'admin_dashboard_page')]
    public function dashboard(
        UtilisateurRepository $userRepo,
        ArticleRepository $articleRepo,
        CommandeRepository $commandeRepo,
        ReservationRepository $reservationRepo,
        ReclamationRepository $reclamationRepo,
    ): Response {
        return $this->render('admin/dashboard.html.twig', [
            'total_users'         => count($userRepo->findAll()),
            'total_articles'      => count($articleRepo->findAll()),
            'total_commandes'     => count($commandeRepo->findAll()),
            'total_reservations'  => count($reservationRepo->findAll()),
            'total_reclamations'  => count($reclamationRepo->findAll()),
            'recent_users'        => $userRepo->findBy([], ['id' => 'DESC'], 5),
            'recent_reclamations' => $reclamationRepo->findBy([], ['id' => 'DESC'], 5),
        ]);
    }

    #[Route('/users', name: 'admin_users')]
    public function users(UtilisateurRepository $repo): Response
    {
        return $this->render('admin/users.html.twig', [
            'users' => $repo->findAll(),
        ]);
    }

    #[Route('/articles', name: 'admin_articles')]
    public function articles(ArticleRepository $repo): Response
    {
        return $this->render('admin/articles.html.twig', [
            'articles' => $repo->findAll(),
        ]);
    }

    #[Route('/reclamations', name: 'admin_reclamations')]
    public function reclamations(ReclamationRepository $repo): Response
    {
        return $this->render('admin/reclamations.html.twig', [
            'reclamations' => $repo->findAll(),
        ]);
    }

    #[Route('/produits', name: 'admin_produits')]
    public function produits(ProduitRepository $repo): Response
    {
        return $this->render('admin/produits.html.twig', [
            'produits' => $repo->findAll(),
        ]);
    }

    #[Route('/reservations', name: 'admin_reservations')]
    public function reservations(ReservationRepository $repo): Response
    {
        return $this->render('admin/reservations.html.twig', [
            'reservations' => $repo->findAll(),
        ]);
    }

    #[Route('/transports', name: 'admin_transports')]
    public function transports(TransportRepository $repo): Response
    {
        return $this->render('admin/transports.html.twig', [
            'transports' => $repo->findAll(),
        ]);
    }

    #[Route('/suggestions', name: 'admin_suggestions')]
    public function suggestions(SuggestionRepository $repo): Response
    {
        return $this->render('admin/suggestions.html.twig', [
            'suggestions' => $repo->findAll(),
        ]);
    }

    #[Route('/commandes', name: 'admin_commandes')]
    public function commandes(CommandeRepository $repo, UtilisateurRepository $userRepo): Response
    {
        $commandes = $repo->findBy([], ['id' => 'DESC']);
        return $this->render('admin/commandes.html.twig', [
            'commandes' => $commandes,
            'userRepo' => $userRepo,
        ]);
    }

    #[Route('/commande/{id}', name: 'admin_commande_details')]
    public function commandeDetails(Commande $commande, UtilisateurRepository $userRepo): Response
    {
        $items = json_decode($commande->getItemsJson() ?? '[]', true);

        return $this->render('admin/commande_details.html.twig', [
            'commande' => $commande,
            'items' => $items,
            'client' => $userRepo->find($commande->getIdUser()),
        ]);
    }

    #[Route('/commande/{id}/livrer', name: 'admin_commande_livrer', methods: ['POST'])]
    public function livrer(Commande $commande, EntityManagerInterface $em): Response
    {
        $commande->setStatus('livree');
        $em->flush();

        $this->addFlash('success', 'Commande marquée comme livrée.');

        return $this->redirectToRoute('admin_commandes');
    }

    #[Route('/commande/{id}/annuler', name: 'admin_commande_annuler', methods: ['POST'])]
    public function annuler(Commande $commande, EntityManagerInterface $em): Response
    {
        $commande->setStatus('annulee');
        $em->flush();

        $this->addFlash('success', 'Commande annulée.');

        return $this->redirectToRoute('admin_commandes');
    }
}