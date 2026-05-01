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
use App\Service\OpenAiService;
use App\Service\SmsService;
use App\Entity\PromoCode;
class FrontController extends AbstractController
{
    #[Route('/', name: 'home')]
    #[Route('/index', name: 'index_page')]
    public function index(): Response
    {
        return $this->render('index.html.twig');
    }
    #[Route('/about', name: 'about')] // <--- Ensure this name is exactly 'about'
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
    
   // src/Controller/FrontController.php

// src/Controller/FrontController.php

#[Route('/accueil', name: 'accueil')]
public function accueil(EntityManagerInterface $em,ReservationRepository $reservationRepository): Response
{


   
    $allStates = $this->getTunisiaStates();

    // 2. Fetch real database stats (Corrected query using 'exp' and 'stars')
    $stats = $em->createQueryBuilder()
        ->select('LOWER(r.exp) as name, AVG(r.stars) as avgRating, COUNT(r.id) as totalVisitors')
        ->from(Reservation::class, 'r')
        ->groupBy('name')
        ->getQuery()
        ->getResult();

    // 3. Merge DB stats into our states array
    foreach ($allStates as &$state) {
        $state['averageRating'] = 0;
        $state['reviewCount'] = 0;
        
        foreach ($stats as $stat) {
            if (strtolower($stat['name']) === strtolower($state['name'])) {
                $state['averageRating'] = round($stat['avgRating'], 1);
                $state['reviewCount'] = $stat['totalVisitors'];
                break;
            }
        }
    }

    // In your Controller method:
$allReviews = $reservationRepository->createQueryBuilder('r')
    ->innerJoin('r.user_id', 'u') // Joins the User linked to the reservation
    ->addSelect('u')             // Selects user data (nom, etc.)
    ->orderBy('r.id', 'DESC')    // Keeps the newest ones at the top
    ->getQuery()
    ->getResult();
    

$recommendations = []; 
    $trending = [];
    $totalLikes = '5.2k';
return $this->render('Reservation/accueil.html.twig', [
    'recommendations' => $recommendations,
    'trending' => $trending,
    'totalLikes' => $totalLikes, 
    'recent_reviews' => $allReviews, 
    'states' => $allStates,
    
]);
}

    #[Route('/Reservation/offerst/{id?}', name: 'app_front_offers')]
    public function offer(TransportRepository $repo, Request $request, EntityManagerInterface $em, $id = null): Response
    {
        $searchTerm = $request->query->get('search'); 
        $sortBy = $request->query->get('sort');  
        $owner = $this->isGranted('ROLE_ADMIN') ? null : $this->getUser();    
        // Define the owner: Admin sees all, User sees only theirs
$owner = $this->isGranted('ROLE_ADMIN') ? null : $this->getUser(); 

// Fetch the list normally
$list = $repo->searchAndSort($searchTerm, $sortBy, $owner);

// ONLY wipe the list if the user is a guest (not logged in)
if (!$this->getUser() && !$this->isGranted('ROLE_ADMIN')) {
    $list = []; 
}

        if ($id) {
            $transport = $repo->find($id);
            if (!$transport) return $this->redirectToRoute('app_front_offers');
            
            // SECURITY: Prevent editing others' transport if not admin
            if ($transport->getUser_id() !== $this->getUser() && !$this->isGranted('ROLE_ADMIN')) {
                throw $this->createAccessDeniedException('You cannot edit this transport.');
            }
        } else {
            $transport = new Transport();
            // --- AUTOMATICALLY SET USER FOR NEW TRANSPORT ---
            $transport->setUser_id($this->getUser()); 
        }

        $form = $this->createForm(TransportType::class, $transport);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($transport);
            $em->flush();
            $this->addFlash('success', 'Transport enregistré !');
            return $this->redirectToRoute('app_front_offers');
        }

        return $this->render('Reservation/offerst.html.twig', [
            'list' => $list,
            'f' => $form->createView(),
            'editMode' => (bool)$id,
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
public function reservations(ReservationRepository $repo,  Request $request, EntityManagerInterface $em, SmsService $smsService, $id = null): Response {
    $searchTerm = $request->query->get('search'); 
    $sortBy = $request->query->get('sort');  
    $owner = $this->isGranted('ROLE_ADMIN') ? null : $this->getUser();    
    
    $list = $repo->searchAndSortReservations($searchTerm, $sortBy, $owner);
    
    // Stats pour les étoiles
    $stats = $repo->createQueryBuilder('r')
            ->select('LOWER(r.exp) as destinationName') 
            ->addSelect('AVG(r.stars) as averageStars')
            ->addSelect('COUNT(r.id) as reviewCount')
            ->where('r.stars IS NOT NULL') 
            ->groupBy('destinationName')
            ->getQuery()
            ->getResult();

    $now = new \DateTime('today');
    $activeReservations = [];
    $historyReservations = [];

    foreach ($list as $reservation) {
        $reservationExp = strtolower(trim($reservation->getExp())); // Use trim() to avoid mismatch
        $reservation->totalReviews = 0; 
        $reservation->dynamicRating = 0.0;

        foreach ($stats as $stat) {
            if ($reservationExp === trim($stat['destinationName'])) {
                $reservation->totalReviews = (int)$stat['reviewCount'];
                $reservation->dynamicRating = round((float)$stat['averageStars'], 1); // Round to 1 decimal
                break;
            }
        }

        $resOwner = $reservation->getUser_id();
        if ($resOwner) {
            $count = $repo->countUserReservationsThisYear($resOwner);
            $reservation->setUserReservationCount($count); 
            if ($count >= 10) $reservation->setClientStatus('Gold');
            elseif ($count >= 5) $reservation->setClientStatus('Silver');
            else $reservation->setClientStatus('Bronze');
        }

        $destination = strtolower(trim($reservation->getExp()));
        $pricingTable = ['tunis' => 50.0, 'sousse' => 150.0, 'nabeul' => 100.0, 'bizerte' => 110.0];
        $reservation->setBasePriceDisplay($pricingTable[$destination] ?? 100.0);

        if ($reservation->getDate() >= $now) { $activeReservations[] = $reservation; } 
        else { $historyReservations[] = $reservation; }
    }

    $editMode = ($id !== null);
    if ($editMode) {
        $reservation = $repo->find($id);
    } else {
        $reservation = new Reservation();
        $reservation->setUser_id($this->getUser()); 
    }

    $form = $this->createForm(ReservationType::class, $reservation);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
    $user = $this->getUser();
    $reservation->setUser_id($user); 
    
    $destination = strtolower(trim($reservation->getExp()));
    $pricingTable = ['tunis' => 50.0, 'sousse' => 150.0, 'nabeul' => 100.0];
    $basePrice = $pricingTable[$destination] ?? 100.0;
    
    $enteredCode = trim($request->request->get('promo_code_input')); 
    $isFreeTrip = false;

    // 1. VÉRIFICATION DU CODE PROMO
    if (!empty($enteredCode)) {
        
        $promo = $em->getRepository(PromoCode::class)->findOneBy([
            'code' => $enteredCode,
            'user_id' => $user,
            'is_used' => 0
        ]);
        //dd($promo, $enteredCode, $user->getId());
        if ($promo) {
            $reservation->setPrice(0.0); // Prix à GRATUIT
            $promo->setIsUsed(1);
            $isFreeTrip = true;
            $this->addFlash('success', 'Félicitations ! Voyage GRATUIT appliqué.');
        } else {
            $this->addFlash('danger', 'Code invalide ou déjà utilisé.');
        }
    }

    // 2. CALCUL DU PRIX SELON STATUT (Seulement si pas de code promo valide)
    if (!$isFreeTrip) {
        if (!$editMode && $user) {
            $countBefore = $repo->countUserReservationsThisYear($user);
            $countAfter = $countBefore + 1; 

            if ($countAfter >= 10) {
                $reservation->setPrice($basePrice * 0.70);
                
                // Génération d'un nouveau code pour le futur
                $promoCode = "WINGO-GOLD-" . rand(100, 999);
                $newPromo = new PromoCode();
                $newPromo->setCode($promoCode);
                $newPromo->setIsUsed(0);
                $newPromo->setUser_id($user);
                $em->persist($newPromo);
                
                try {
                    $smsService->sendWelcomeGold("+21694910205", $user->getNom(), $promoCode);
                    $this->addFlash('success', "BIENVENUE GOLD ! SMS envoyé.");
                } catch (\Exception $e) {
                    $this->addFlash('warning', "Statut GOLD ! Notez votre code : $promoCode");
                }
            } elseif ($countAfter >= 5) {
                $reservation->setPrice($basePrice * 0.85);
                $this->addFlash('success', 'Statut Silver ! -15% appliqués.');
            } else {
                $reservation->setPrice($basePrice);
            }
        } else {
            $reservation->setPrice($basePrice);
        }
    }

    $em->persist($reservation);
    $em->flush(); 

    return $this->redirectToRoute('app_front_reservations');
}

    $userCount = 0;
    if ($this->getUser()) {
        $userCount = $repo->countUserReservationsThisYear($this->getUser());
    }

    return $this->render('Reservation/offersr.html.twig', [
        'userCount' => $userCount,
        'list' => $activeReservations,
        'history' => $historyReservations,
        'f' => $form->createView(),
        'editMode' => $editMode
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
    #[Route('/chat/ask', name: 'app_chat_ask', methods: ['POST'])]
public function ask(Request $request, OpenAiService $aiService, TransportRepository $repo): JsonResponse
{
    // Try getting from Form Data first, then JSON body
    $userMessage = $request->request->get('message');
    if (!$userMessage) {
        $data = json_decode($request->getContent(), true);
        $userMessage = $data['message'] ?? null;
    }

    if (!$userMessage) {
        return new JsonResponse(['response' => 'Désolé, je n\'ai pas reçu votre message.'], 400);
    }
    
    // Fetch real data from your DB
    $transports = $repo->findAll();
    $transportData = [];
    foreach ($transports as $t) {
        $transportData[] = "{$t->getType()} to {$t->getArrivee()} for {$t->getTarif()} TND";
    }

    // Call the service (which we configured to use Gemini)
    $aiResponse = $aiService->getTravelAdvice($userMessage, $transportData);

    return new JsonResponse(['response' => $aiResponse]);
}
#[Route('/rate/{id}', name: 'app_reservation_rate', methods: ['POST'])]
public function rateTrip(Reservation $reservation, Request $request, EntityManagerInterface $em)
{
    // 1. Get data from the POST request
    $stars = $request->request->get('stars');
    $comment = $request->request->get('comment');

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
private function getTunisiaStates(): array
{
    return $states = [
    // --- ÉTÉ (SUMMER) ---
    ['name' => 'Sousse', 'image' => 'sousse.jpg', 'season' => 'Été', 'type' => 'Plage', 'desc' => 'La perle du Sahel.'],
    ['name' => 'Monastir', 'image' => 'monastir.jpg', 'season' => 'Été', 'type' => 'Plage', 'desc' => 'Histoire et farniente.'],
    ['name' => 'Mahdia', 'image' => 'mahdia.jpg', 'season' => 'Été', 'type' => 'Plage', 'desc' => 'Eaux turquoise et calme.'],
    ['name' => 'Nabeul', 'image' => 'nabeul.jpg', 'season' => 'Été', 'type' => 'Artisanat', 'desc' => 'Poterie et soleil.'],
    ['name' => 'Bizerte', 'image' => 'bizerte.jpg', 'season' => 'Été', 'type' => 'Nord', 'desc' => 'Vieux port et plages.'],
    ['name' => 'Sfax', 'image' => 'sfax.jpg', 'season' => 'Été', 'type' => 'Économie', 'desc' => 'Capitale du Sud.'],
    
    // --- PRINTEMPS (SPRING) ---
    ['name' => 'Zaghouan', 'image' => 'zaghwen.jpg', 'season' => 'Printemps', 'type' => 'Nature', 'desc' => 'Temple des eaux.'],
    ['name' => 'Béja', 'image' => 'beja.jpeg', 'season' => 'Printemps', 'type' => 'Vert', 'desc' => 'Cigognes et fromage.'],
    ['name' => 'Jendouba', 'image' => 'jandouba.jpg', 'season' => 'Printemps', 'type' => 'Forêt', 'desc' => 'Tabarka et Ain Draham.'],
    ['name' => 'Siliana', 'image' => 'siliana.jpg', 'season' => 'Printemps', 'type' => 'Montagne', 'desc' => 'Paysages sauvages.'],
    ['name' => 'Le Kef', 'image' => 'kef.jpg', 'season' => 'Printemps', 'type' => 'Culture', 'desc' => 'La citadelle byzantine.'],
    ['name' => 'Manouba', 'image' => 'manouba.jpg', 'season' => 'Printemps', 'type' => 'Histoire', 'desc' => 'Palais et vergers.'],

    // --- AUTOMNE (AUTUMN) ---
    ['name' => 'Tunis', 'image' => 'tunis.jpg', 'season' => 'Automne', 'type' => 'Capitale', 'desc' => 'La Médina et les musées.'],
    ['name' => 'Ariana', 'image' => 'ariana.jpg', 'season' => 'Automne', 'type' => 'Ville', 'desc' => 'Cité des roses.'],
    ['name' => 'Ben Arous', 'image' => 'benarous.jpg', 'season' => 'Automne', 'type' => 'Port', 'desc' => 'Radès et ses environs.'],
    ['name' => 'Kairouan', 'image' => 'kairouan.jpg', 'season' => 'Automne', 'type' => 'Religion', 'desc' => 'La ville sainte.'],
    ['name' => 'Sidi Bouzid', 'image' => 'sidibouzid.jpg', 'season' => 'Automne', 'type' => 'Agriculture', 'desc' => 'Terre de générosité.'],
    ['name' => 'Kassérine', 'image' => 'gassrin.jfif', 'season' => 'Automne', 'type' => 'Parc', 'desc' => 'Mont Chaambi.'],

    // --- HIVER (WINTER) ---
    ['name' => 'Tozeur', 'image' => 'tozeur.jpg', 'season' => 'Hiver', 'type' => 'Désert', 'desc' => 'L’oasis de briques.'],
    ['name' => 'Kébili', 'image' => 'kebili.jpg', 'season' => 'Hiver', 'type' => 'Sahara', 'desc' => 'Dunes à perte de vue.'],
    ['name' => 'Tataouine', 'image' => 'tataouine.jpg', 'season' => 'Hiver', 'type' => 'Ksour', 'desc' => 'Décors de Star Wars.'],
    ['name' => 'Médenine', 'image' => 'medenine.jpg', 'season' => 'Hiver', 'type' => 'Berbère', 'desc' => 'Ghorfas uniques.'],
    ['name' => 'Gabès', 'image' => 'gabes.jpg', 'season' => 'Hiver', 'type' => 'Oasis', 'desc' => 'Oasis maritime.'],
    ['name' => 'Gafsa', 'image' => 'gafsa.jpg', 'season' => 'Hiver', 'type' => 'Phosphate', 'desc' => 'Piscines romaines.'],
];
}
}