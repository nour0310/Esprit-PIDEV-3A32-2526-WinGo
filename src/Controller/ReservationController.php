<?php

namespace App\Controller;

use App\Entity\Reservation;
use App\Entity\Utilisateur;
use App\Form\ReservationType;
use App\Repository\ReservationRepository;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/reservation')]
final class ReservationController extends AbstractController
{
    #[Route('/affiche', name: "displayReservation")]
public function listReservationsFromDB(ReservationRepository $repo): Response
{
    // 1. Get the data for the list
    $reservations = $repo->findAll();

    // 2. Create an empty form so Twig doesn't crash on line 41
    // We use a new Reservation entity for an empty "Add" form
    $form = $this->createForm(ReservationType::class, new \App\Entity\Reservation());

    // 3. Pass EVERYTHING to the template
    return $this->render('Reservation/offersr.html.twig', [
        'list' => $reservations,
        'f' => $form->createView(),
        'editMode' => false // Since we are listing/adding, not editing
    ]);
}

    #[Route('/details/{id}', name: "reservationDetails")]
    public function reservationDetails($id, ReservationRepository $repo): Response
    {
        return $this->render("Reservation/offersr.html.twig", ['id' => $id, "reservation" => $repo->find($id)]);
    }

    #[Route('/add', name: "addReservation")]
    public function addReservation(ManagerRegistry $manager, Request $request): Response
{
    $em = $manager->getManager();
    $newReservation = new Reservation();
    
    $form = $this->createForm(ReservationType::class, $newReservation);
    $form->handleRequest($request);

    // isValid() now checks the #[Assert] tags in your Entity automatically 
    if ($form->isSubmitted() && $form->isValid()) {
        
        $user = $this->getUser(); 
        if ($user) {
            $newReservation->setUser_id($user); 
        } else {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        $em->persist($newReservation);
        $em->flush(); // [cite: 59]
        return $this->redirectToRoute('displayReservation'); // [cite: 60]
    }

    // Use render() to pass the form to Twig [cite: 62]
    return $this->render('reservation/add.html.twig', [
        'f' => $form->createView()
    ]);
}

    #[Route('/delete/{id}', name: "deleteReservation")]
    public function delete($id, ManagerRegistry $manager, ReservationRepository $repo): Response
    {
        $em = $manager->getManager();
        $reservation = $repo->find($id);
        /** @var \App\Entity\Utilisateur $currentUser */
        $currentUser = $this->getUser();

    // Compare the integer user_id from the entity to the ID of the logged-in user
   if ($reservation->getUser_id()->getId() !== $currentUser->getId() && !$this->isGranted('ROLE_ADMIN')) {
    throw $this->createAccessDeniedException('Access Denied: You do not own this record.');
}
        
        $em->remove($reservation);
        $em->flush();
        
        return $this->redirectToRoute('displayReservation');
    }

    #[Route('/update/{id}', name: "updateReservation")]
    public function updateReservation($id, ReservationRepository $repo, ManagerRegistry $manager, Request $request): Response
    {
        $em = $manager->getManager();
        $reservation = $repo->find($id);
        /** @var \App\Entity\Utilisateur $currentUser */
        $currentUser = $this->getUser();

    // Compare the integer user_id from the entity to the ID of the logged-in user
 $ownerId = $reservation->getUser_id() ? $reservation->getUser_id()->getId() : null;

if ($ownerId !== $currentUser->getId() && !$this->isGranted('ROLE_ADMIN')) {
    throw $this->createAccessDeniedException('Access Denied: You do not own this record.');
}
        
        $form = $this->createForm(ReservationType::class, $reservation);
        
        $form->handleRequest($request);
        if ($form->isSubmitted()) {
            $em->persist($reservation);
            $em->flush();
            return $this->redirectToRoute('displayReservation');
        }

        return $this->render('Reservation/offersr.html.twig', ['f' => $form, 'editMode' => true,'list' => $repo->findAll()]);
    }

    /*#[Route('/search', name: "searchReservationStatut")]
    public function searchAndSortReservations(?string $search, ?string $sort)
    {
        $qb = $this->createQueryBuilder('r');

        // Filter by Status or User Name
        if ($search) {
            $qb->andWhere('r.statut LIKE :term OR r.user LIKE :term')
            ->setParameter('term', '%'.$search.'%');
        }

        // Sorting Logic
        if ($sort === 'date_asc') {
            $qb->orderBy('r.date', 'ASC');
        } elseif ($sort === 'date_desc') {
            $qb->orderBy('r.date', 'DESC');
        } else {
            $qb->orderBy('r.id', 'DESC'); // Default: newest entries first
        }

        return $qb->getQuery()->getResult();
    }*/
}