<?php

namespace App\Controller;

use App\Entity\Transport;
use App\Form\TransportType;
use App\Entity\Reservation;
use App\Form\ReservationType;
use App\Repository\TransportRepository;
use App\Repository\ReservationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\HttpFoundation\JsonResponse;

use App\Entity\Event;
use App\Repository\ProduitRepository;
use App\Repository\CommentaireRepository;
use App\Repository\ArticleRepository;

class FrontController extends AbstractController
{
    #[Route('/', name: 'home')]
    #[Route('/index', name: 'index_page')]
    public function index(
        EntityManagerInterface $em,
        ProduitRepository $produitRepo,
        CommentaireRepository $commentaireRepo,
        ArticleRepository $articleRepo
    ): Response {
        // Fetch real data to replace static placeholders
        $events = $em->getRepository(Event::class)->findBy([], ['date_event' => 'DESC'], 3);
        $produits = $produitRepo->findBy([], ['id' => 'DESC'], 3);
        $eventsOffers = $em->getRepository(Event::class)->findBy([], ['price' => 'ASC'], 4); // The cheapest events for offers
        $commentaires = $commentaireRepo->findBy([], ['dateCommentaire' => 'DESC'], 3);
        $articles = $articleRepo->findBy([], ['datePublication' => 'DESC'], 8);

        // Get Top Destinations based on most frequent 'exp' in Reservations
        $topDestinations = $em->createQuery('
            SELECT r.exp as location, COUNT(r.id) as total
            FROM App\Entity\Reservation r
            GROUP BY r.exp
            ORDER BY total DESC
        ')->setMaxResults(6)->getResult();

        return $this->render('index.html.twig', [
            'events' => $events,
            'produits' => $produits,
            'events_offers' => $eventsOffers,
            'commentaires' => $commentaires,
            'articles' => $articles,
            'topDestinations' => $topDestinations,
        ]);
    }
    #[Route('/about', name: 'about')]
    public function about(): Response
    {
        return $this->render('about.html.twig');
    }
    #[Route('/contact', name: 'contact')]
    public function contact(): Response
    {
        return $this->render('contact.html.twig');
    }

    // --- TRANSPORT SECTION (OFFERST) ---

    #[Route('/offers', name: 'offers')]
    public function offers(): Response
    {
        // Redirects to the main list logic below
        return $this->redirectToRoute('app_front_offers');
    }

    #[Route('/Reservation/offerst/{id?}', name: 'app_front_offers')]
    public function offer(TransportRepository $repo, Request $request, EntityManagerInterface $em, $id = null): Response
    {
        $searchTerm = $request->query->get('search');
        $sortBy = $request->query->get('sort');
        $list = $repo->searchAndSort($searchTerm, $sortBy);

        if ($request->query->get('ajax')) {
            return $this->render('list.html.twig', ['list' => $list]);
        }

        if ($id) {
            $transport = $repo->find($id);
            if (!$transport)
                return $this->redirectToRoute('app_front_offers');
        } else {
            $transport = new Transport();
        }

        $form = $this->createForm(TransportType::class, $transport);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($transport);
            $em->flush();
            $this->addFlash('success', 'Operation réussie !');
            return $this->redirectToRoute('app_front_offers');
        }

        return $this->render('Reservation/offerst.html.twig', [
            'list' => $list,
            'f' => $form->createView(),
            'editMode' => (bool) $id,
            'searchTerm' => $searchTerm,
            'currentSort' => $sortBy
        ]);
    }

    #[Route('/Reservation/offerst/delete/{id}', name: 'app_front_offers_delete')]
    public function deleteTransport(Transport $transport, EntityManagerInterface $em): Response
    {
        $em->remove($transport);
        $em->flush();
        return $this->redirectToRoute('app_front_offers');
    }

    // --- RESERVATION SECTION (OFFERSR) ---

    #[Route('/Reservation/offersr/{id?}', name: 'app_front_reservations')]
    public function reservations(ReservationRepository $repo, Request $request, EntityManagerInterface $em, $id = null): Response
    {
        $searchTerm = $request->query->get('search');
        $sortBy = $request->query->get('sort');

        // Pass the currently logged in user to avoid showing other clients' reservations
        $user = $this->getUser();
        $list = $repo->searchAndSortReservations($searchTerm, $sortBy, $user instanceof \App\Entity\Utilisateur ? $user : null);

        if ($id) {
            $reservation = $repo->find($id);
        } else {
            $reservation = new Reservation();
        }

        $form = $this->createForm(ReservationType::class, $reservation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($reservation);
            $em->flush();
            return $this->redirectToRoute('app_front_reservations');
        }

        return $this->render('Reservation/offersr.html.twig', [
            'list' => $list,
            'f' => $form->createView(),
            'editMode' => $reservation->getId() !== null,
            'searchTerm' => $searchTerm,
            'currentSort' => $sortBy
        ]);
    }

    #[Route('/autocomplete', name: 'app_front_transport_autocomplete')]
    public function autocomplete(Request $request, TransportRepository $repo): JsonResponse
    {
        $searchTerm = $request->query->get('q');
        $transports = $repo->searchAndSort($searchTerm, null);
        $results = [];
        foreach ($transports as $t) {
            $results[] = $t->getType() . " to " . $t->getArrivee();
        }
        return new JsonResponse(array_unique($results));
    }
}