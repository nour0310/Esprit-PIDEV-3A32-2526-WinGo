<?php

namespace App\Controller;

use App\Entity\Reservation;
use App\Form\ReservationType;
use App\Repository\ReservationRepository;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Doctrine\ORM\EntityManagerInterface;
use App\Entity\PromoCode;
use App\Entity\Utilisateur;
use Symfony\Component\HttpFoundation\JsonResponse;

#[Route('/reservation')]
final class ReservationController extends AbstractController
{
   #[Route('/affiche', name: "displayReservation")]
public function listReservationsFromDB(
    ReservationRepository $repo, 
    Request $request, 
    ManagerRegistry $manager,
    \App\Service\SmsService $smsService // --- 1. Injection du service SMS ---
): Response
{
    $em = $manager->getManager();
    /** @var Utilisateur|null $user */
    $user = $this->getUser();
    $allReservations = $repo->findAll();

    // --- 2. CALCUL DU COMPTEUR (Pour la vue Twig et la progression) ---
    $userCount = 0;
    if ($user) {
        $userCount = $repo->countUserReservationsThisYear($user);
    }

    $now = new \DateTime('today');
    $activeReservations = [];
    $historyReservations = [];

    foreach ($allReservations as $reservation) {
        // Optionnel : On peut aussi calculer le badge ici pour l'affichage
        if ($reservation->getDate() >= $now) {
            $activeReservations[] = $reservation;
        } else {
            $historyReservations[] = $reservation;
        }
    }

    $newReservation = new Reservation();
    $form = $this->createForm(ReservationType::class, $newReservation);
    $form->handleRequest($request);
    
    if ($form->isSubmitted() && $form->isValid()) {
        $newReservation->setUser_id($user); 

        $destination = strtolower(trim($newReservation->getExp()));
        $pricingTable = [
            'tunis' => 50.0, 'ariana' => 55.0, 'ben arous' => 55.0,
            'manouba' => 60.0, 'zaghouan' => 120.0, 'nabeul' => 100.0,
            'bizerte' => 110.0, 'beja' => 130.0, 'sousse' => 150.0,
        ];
        
        $basePrice = $pricingTable[$destination] ?? 100.0;
        $newReservation->setBasePriceDisplay($basePrice);

        

    if ($user) {
    // 1. RÉCUPÉRATION DU CODE DEPUIS LE FORMULAIRE
    $enteredCode = $request->request->get('promo_code_input'); 
    $isFreeTrip = false;

    // 2. VÉRIFICATION DU CODE PROMO
    if (!empty($enteredCode)) {
        $promo = $em->getRepository(PromoCode::class)->findOneBy([
            'code' => $enteredCode,
            'user_id' => $user,
            'is_used' => 0 // On vérifie qu'il n'est pas déjà consommé
        ]);

        if ($promo) {
            $newReservation->setPrice(0); // BINGO : Prix à zéro
            $promo->setIsUsed(1);           // On le marque comme "utilisé"
            $isFreeTrip = true;
            $this->addFlash('success', 'Félicitations ! Votre code promo a été appliqué : Voyage GRATUIT !');
        } else {
            $this->addFlash('danger', 'Code invalide ou déjà utilisé.');
        }
    }

    // 3. LOGIQUE DE PRIX NORMALE (Uniquement si pas de voyage gratuit)
    if (!$isFreeTrip) {
        $currentCount = $repo->countUserReservationsThisYear($user);
        $nextCount = $currentCount + 1;

        if ($nextCount >= 10) {
            // Prix Gold (-30%)
            $newReservation->setPrice((int) round($basePrice * 0.70));
            
            // --- ON GÉNÈRE LE CODE POUR LA PROCHAINE FOIS ---
            $promoCode = "WINGO-GOLD-" . rand(100, 999);
            
            $newPromo = new PromoCode();
            $newPromo->setCode($promoCode);
            $newPromo->setIsUsed(0);
            $newPromo->setUser_id($user);
            
            $em->persist($newPromo);
            

            try {
                $smsService->sendWelcomeGold("+21694910205", $user->getNom(), $promoCode);
                $this->addFlash('success', 'Statut GOLD ! Code de voyage gratuit envoyé par SMS.');
            } catch (\Exception $e) {
                $this->addFlash('warning', 'Statut GOLD ! Notez votre code : ' . $promoCode);
            }
        } elseif ($nextCount >= 5) {
            $newReservation->setPrice((int) round($basePrice * 0.85)); // Silver (-15%)
            $this->addFlash('success', 'Statut Silver appliqué !');
        } else {
            $newReservation->setPrice((int) round($basePrice)); // Prix normal
        }
    }
}

        $em->persist($newReservation);
        $em->flush();
        
    
    }
     
    return $this->render('Reservation/offersr.html.twig', [
        'userCount' => $userCount, // --- 4. Maintenant la variable existe bien ! ---
        'list' => $activeReservations,
        'history' => $historyReservations,
        'f' => $form->createView(),
        'editMode' => false
    ]);
}
    #[Route('/details/{id}', name: "reservationDetails")]
    public function reservationDetails(int $id, ReservationRepository $repo): Response
    {
        return $this->render("Reservation/offersr.html.twig", ['id' => $id, "reservation" => $repo->find($id)]);
    }

#[Route('/add', name: "addReservation")]
public function addReservation(ManagerRegistry $manager, Request $request): Response
{
    $em = $manager->getManager();
    $newReservation = new Reservation();

    $currentUser = $this->getUser();

    if (!$currentUser instanceof Utilisateur) {
        throw $this->createAccessDeniedException('Vous devez être connecté.');
    }

    $newReservation->setUser_id($currentUser);

    $form = $this->createForm(ReservationType::class, $newReservation);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        $em->persist($newReservation);
        $em->flush();

        return $this->redirectToRoute('displayReservation');
    }

    return $this->render('reservation/add.html.twig', [
        'f' => $form->createView(),
    ]);
}

    #[Route('/delete/{id}', name: "deleteReservation")]
public function delete(int $id, ManagerRegistry $manager, ReservationRepository $repo): Response
{
    $em = $manager->getManager();
    $reservation = $repo->find($id);
    $currentUser = $this->getUser();

    // 1. SAFETY CHECK: Does this reservation actually exist?
    // If someone clicks a dead link or old data, just send them back safely.
    if (!$reservation) {
        $this->addFlash('error', 'Cette réservation n\'existe pas ou a déjà été supprimée.');
        return $this->redirectToRoute('app_front_reservations');
    }

    // 2. SAFETY CHECK: Verify ownership without crashing
    $owner = $reservation->getUser_id();
    
    // If the person deleting isn't an admin, we need to enforce the rules
    if (!$this->isGranted('ROLE_ADMIN')) {
        // We ensure a user is logged in, the reservation HAS an owner, and the IDs match
        if (!$currentUser || !$owner || $owner->getId() !== $currentUser->getId()) {
            throw $this->createAccessDeniedException('Access Denied: You do not own this record.');
        }
    }
        
    // 3. If we survived the checks, it's safe to delete!
    $em->remove($reservation);
    $em->flush();
    
    return $this->redirectToRoute('app_front_reservations');
}
    #[Route('/update/{id}', name: "updateReservation")]
public function updateReservation(int $id, ReservationRepository $repo, ManagerRegistry $manager, Request $request): Response
{
    $em = $manager->getManager();
    $reservation = $repo->find($id);
    $currentUser = $this->getUser();

    // Security check
   if (!$reservation) {
    throw $this->createNotFoundException('Réservation introuvable.');
}

if (!$currentUser instanceof Utilisateur) {
    throw $this->createAccessDeniedException('Vous devez être connecté.');
}

$owner = $reservation->getUser_id();

if (!$this->isGranted('ROLE_ADMIN') && ($owner === null || $owner->getId() !== $currentUser->getId())) {
    throw $this->createAccessDeniedException('Access Denied.');
}
        
    $form = $this->createForm(ReservationType::class, $reservation);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        $em->flush(); // Just flush changes
        // CRITICAL: Redirect back to the display page
        return $this->redirectToRoute('app_front_reservations');
    }

    // 1. Get the user count for the loyalty progress bar
   $userCount = $repo->countUserReservationsThisYear($currentUser);

    // 2. Fetch all reservations and split them
    // (Note: You could also use your $repo->searchAndSortReservations() here if you prefer)
    $allReservations = $repo->findAll();
    $activeReservations = [];
    $historyReservations = [];
    $now = new \DateTime('today');

    foreach ($allReservations as $res) {
        if ($res->getDate() >= $now) { 
            // Add to active reservations
            $activeReservations[] = $res; 
        } else { 
            // --- FIX: HISTORY FILTER ---
            // Only add to history if the reservation belongs to the currently logged-in user
           $resOwner = $res->getUser_id();

if ($resOwner !== null && $resOwner->getId() === $currentUser->getId()) {
    $historyReservations[] = $res;
}
        }
    }

    return $this->render('Reservation/offersr.html.twig', [
        'f' => $form->createView(), 
        'editMode' => true,
        'userCount' => $userCount,          
        'list' => $activeReservations,      
        'history' => $historyReservations   
    ]);
}

    #[Route('/search', name: "searchReservationStatut")]
   /**
 * @return Reservation[]
 */
public function searchAndSortReservations(?string $search, ?string $sort, ?Utilisateur $owner = null): Response
    {
        $qb = $this->createQueryBuilder('r');
        if ($owner !== null) {
        $qb->andWhere('t.user_id = :owner') // Ensure 'user_id' matches your entity property name
           ->setParameter('owner', $owner);
        }

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
    }
    #[Route('/rate/{id}', name: 'app_reservation_rate', methods: ['POST'])]
public function rateTrip(Reservation $reservation, Request $request, EntityManagerInterface $em): Response
{
    // 1. Get data from the POST request
    $stars = $request->request->get('stars');
 $comment = $request->request->get('comment');
$comment = $comment !== null ? (string) $comment : null;
    // 2. Update the entity (Symfony already found $reservation via the {id} in the URL)
    if ($stars !== null) {
        $reservation->setStars((int)$stars);
    }

    // Update comment (it's okay if it's null/empty since it's optional)
    $reservation->setComment($comment);

    // 3. Save to database
    $em->persist($reservation);
    $em->flush();

    // 4. Handle Response
    if ($request->isXmlHttpRequest()) {
        return new JsonResponse(['status' => 'success', 'message' => 'Avis enregistré !']);
    }

    $this->addFlash('success', 'Avis enregistré avec succès !');
    return $this->redirectToRoute('app_front_reservations'); 
}
}