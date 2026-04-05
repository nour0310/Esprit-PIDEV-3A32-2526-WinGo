<?php

namespace App\Controller;

use App\Repository\ArticleRepository;
use App\Repository\CommandeRepository;
use App\Repository\ProduitRepository;
use App\Repository\ReclamationRepository;
use App\Repository\ReservationRepository;
use App\Repository\TransportRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_USER')]
#[Route('/client')]
class ClientController extends AbstractController
{
    #[Route('', name: 'client_dashboard')]
    #[Route('/dashboard', name: 'client_dashboard_page')]
    public function dashboard(
        ArticleRepository $articleRepo,
        ReservationRepository $reservationRepo,
        CommandeRepository $commandeRepo,
        ReclamationRepository $reclamationRepo,
    ): Response {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        return $this->render('client/dashboard.html.twig', [
            'recent_articles' => $articleRepo->findBy([], ['id' => 'DESC'], 6),
            'my_reservations' => $reservationRepo->findBy(['user' => $user->getEmail()], ['id' => 'DESC'], 5),
            'my_commandes' => $commandeRepo->findBy(['idUser' => $user->getId()], ['id' => 'DESC'], 5),
            'my_reclamations' => $reclamationRepo->findBy(['idUser' => $user->getId()], ['id' => 'DESC']),
        ]);
    }

    #[Route('/articles', name: 'client_articles')]
    public function articles(ArticleRepository $repo): Response
    {
        return $this->render('client/articles.html.twig', [
            'articles' => $repo->findBy([], ['datePublication' => 'DESC']),
        ]);
    }

    #[Route('/produits', name: 'client_produits')]
    public function produits(Request $request, ProduitRepository $repo): Response
    {
        $searchTerm = trim($request->query->get('q', ''));

        if ($searchTerm !== '') {
            $produits = $repo->searchByNom($searchTerm);
        } else {
            $produits = $repo->findAll();
        }

        return $this->render('client/produits.html.twig', [
            'produits' => $produits,
            'searchTerm' => $searchTerm,
        ]);
    }

    #[Route('/reservations', name: 'client_reservations')]
    public function reservations(ReservationRepository $repo, TransportRepository $transportRepo): Response
    {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        return $this->render('client/reservations.html.twig', [
            'reservations' => $repo->findBy(['user' => $user->getEmail()]),
            'transports' => $transportRepo->findAll(),
        ]);
    }

    #[Route('/reclamations', name: 'client_reclamations')]
    public function reclamations(ReclamationRepository $repo): Response
    {
        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        return $this->render('client/reclamations.html.twig', [
            'reclamations' => $repo->findBy(['idUser' => $user->getId()], ['id' => 'DESC']),
        ]);
    }
}