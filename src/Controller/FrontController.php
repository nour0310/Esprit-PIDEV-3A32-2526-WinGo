<?php

namespace App\Controller;

use App\Entity\PromoCode;
use App\Entity\Reservation;
use App\Entity\Transport;
use App\Entity\Utilisateur;
use App\Form\ReservationType;
use App\Form\TransportType;
use App\Repository\ReservationRepository;
use App\Repository\TransportRepository;
use App\Service\OpenAiService;
use App\Service\SmsService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class FrontController extends AbstractController
{
    #[Route('/', name: 'home')]
    #[Route('/index', name: 'index_page')]
    public function index(): Response
    {
        return $this->render('index.html.twig');
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

    #[Route('/offers', name: 'offers')]
    public function offers(): Response
    {
        return $this->redirectToRoute('app_front_offers');
    }

    #[Route('/accueil', name: 'accueil')]
    public function accueil(EntityManagerInterface $em, ReservationRepository $reservationRepository): Response
    {
        $allStates = $this->getTunisiaStates();

        $stats = $em->createQueryBuilder()
            ->select('LOWER(r.exp) as name, AVG(r.stars) as avgRating, COUNT(r.id) as totalVisitors')
            ->from(Reservation::class, 'r')
            ->groupBy('name')
            ->getQuery()
            ->getResult();

        foreach ($allStates as &$state) {
            $state['averageRating'] = 0;
            $state['reviewCount'] = 0;

            foreach ($stats as $stat) {
                if (strtolower((string) $stat['name']) === strtolower($state['name'])) {
                    $state['averageRating'] = round((float) $stat['avgRating'], 1);
                    $state['reviewCount'] = (int) $stat['totalVisitors'];
                    break;
                }
            }
        }

        $allReviews = $reservationRepository->createQueryBuilder('r')
            ->innerJoin('r.user_id', 'u')
            ->addSelect('u')
            ->orderBy('r.id', 'DESC')
            ->getQuery()
            ->getResult();

        return $this->render('Reservation/accueil.html.twig', [
            'recommendations' => [],
            'trending' => [],
            'totalLikes' => '5.2k',
            'recent_reviews' => $allReviews,
            'states' => $allStates,
        ]);
    }

    #[Route('/Reservation/offerst/{id?}', name: 'app_front_offers')]
    public function offer(
        TransportRepository $repo,
        Request $request,
        EntityManagerInterface $em,
        ?int $id = null
    ): Response {
        $searchTerm = $request->query->get('search');
        $sortBy = $request->query->get('sort');

        $searchTerm = $searchTerm !== null ? (string) $searchTerm : null;
        $sortBy = $sortBy !== null ? (string) $sortBy : null;

        $connectedUser = $this->getUser();
        $owner = (!$this->isGranted('ROLE_ADMIN') && $connectedUser instanceof Utilisateur)
            ? $connectedUser
            : null;

        $list = $repo->searchAndSort($searchTerm, $sortBy, $owner);

        if (!$connectedUser && !$this->isGranted('ROLE_ADMIN')) {
            $list = [];
        }

        if ($id !== null) {
            $transport = $repo->find($id);

            if (!$transport instanceof Transport) {
                return $this->redirectToRoute('app_front_offers');
            }

            if (!$this->isGranted('ROLE_ADMIN')) {
                if (!$connectedUser instanceof Utilisateur || $transport->getUser_id() !== $connectedUser) {
                    throw $this->createAccessDeniedException('You cannot edit this transport.');
                }
            }
        } else {
            $transport = new Transport();
            $user = $this->getCurrentUtilisateur();
            $transport->setUser_id($user);
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
            'editMode' => $id !== null,
            'searchTerm' => $searchTerm,
            'currentSort' => $sortBy,
        ]);
    }

    #[Route('/Reservation/offerst/delete/{id}', name: 'app_front_offers_delete')]
    public function deleteTransport(Transport $transport, EntityManagerInterface $em): Response
    {
        $em->remove($transport);
        $em->flush();

        return $this->redirectToRoute('app_front_offers');
    }

    #[Route('/Reservation/offersr/{id?}', name: 'app_front_reservations')]
    public function reservations(
        ReservationRepository $repo,
        Request $request,
        EntityManagerInterface $em,
        SmsService $smsService,
        ?int $id = null
    ): Response {
        $searchTerm = $request->query->get('search');
        $sortBy = $request->query->get('sort');

        $searchTerm = $searchTerm !== null ? (string) $searchTerm : null;
        $sortBy = $sortBy !== null ? (string) $sortBy : null;

        $connectedUser = $this->getUser();
        $owner = (!$this->isGranted('ROLE_ADMIN') && $connectedUser instanceof Utilisateur)
            ? $connectedUser
            : null;

        $list = $repo->searchAndSortReservations($searchTerm, $sortBy, $owner);

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

        foreach ($list as $reservationItem) {
            $reservationExp = strtolower(trim($reservationItem->getExp()));
            $reservationItem->totalReviews = 0;
            $reservationItem->dynamicRating = 0.0;

            foreach ($stats as $stat) {
                if ($reservationExp === trim((string) $stat['destinationName'])) {
                    $reservationItem->totalReviews = (int) $stat['reviewCount'];
                    $reservationItem->dynamicRating = round((float) $stat['averageStars'], 1);
                    break;
                }
            }

            $resOwner = $reservationItem->getUser_id();

            if ($resOwner instanceof Utilisateur) {
                $count = $repo->countUserReservationsThisYear($resOwner);
                $reservationItem->setUserReservationCount($count);

                if ($count >= 10) {
                    $reservationItem->setClientStatus('Gold');
                } elseif ($count >= 5) {
                    $reservationItem->setClientStatus('Silver');
                } else {
                    $reservationItem->setClientStatus('Bronze');
                }
            }

            $destination = strtolower(trim($reservationItem->getExp()));
            $pricingTable = [
                'tunis' => 50.0,
                'sousse' => 150.0,
                'nabeul' => 100.0,
                'bizerte' => 110.0,
            ];

            $reservationItem->setBasePriceDisplay($pricingTable[$destination] ?? 100.0);

            if ($reservationItem->getDate() !== null && $reservationItem->getDate() >= $now) {
                $activeReservations[] = $reservationItem;
            } else {
                $historyReservations[] = $reservationItem;
            }
        }

        $editMode = $id !== null;

        if ($editMode) {
            $reservation = $repo->find($id);

            if (!$reservation instanceof Reservation) {
                throw $this->createNotFoundException('Réservation introuvable.');
            }
        } else {
            $reservation = new Reservation();
            $user = $this->getCurrentUtilisateur();
            $reservation->setUser_id($user);
            $reservation->setUser($user->getNom()); 
        }

        $form = $this->createForm(ReservationType::class, $reservation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $user = $this->getCurrentUtilisateur();

            $reservation->setUser_id($user);
            

            $destination = strtolower(trim($reservation->getExp()));
            $pricingTable = [
                'tunis' => 50.0,
                'sousse' => 150.0,
                'nabeul' => 100.0,
            ];

            $basePrice = $pricingTable[$destination] ?? 100.0;
            $enteredCode = trim((string) $request->request->get('promo_code_input', ''));
            $isFreeTrip = false;

            if ($enteredCode !== '') {
                $promo = $em->getRepository(PromoCode::class)->findOneBy([
                    'code' => $enteredCode,
                    'user_id' => $user,
                    'is_used' => 0,
                ]);

                if ($promo instanceof PromoCode) {
                    $reservation->setPrice(0);
                    $promo->setIsUsed(1);
                    $isFreeTrip = true;

                    $this->addFlash('success', 'Félicitations ! Voyage GRATUIT appliqué.');
                } else {
                    $this->addFlash('danger', 'Code invalide ou déjà utilisé.');
                }
            }

            if (!$isFreeTrip) {
                if (!$editMode) {
                    $countBefore = $repo->countUserReservationsThisYear($user);
                    $countAfter = $countBefore + 1;

                    if ($countAfter >= 10) {
                        $reservation->setPrice((int) round($basePrice * 0.70));

                        $promoCode = 'WINGO-GOLD-' . rand(100, 999);

                        $newPromo = new PromoCode();
                        $newPromo->setCode($promoCode);
                        $newPromo->setIsUsed(0);
                        $newPromo->setUser_id($user);

                        $em->persist($newPromo);

                        try {
                            $smsService->sendWelcomeGold('+21694910205', $user->getNom(), $promoCode);
                            $this->addFlash('success', 'BIENVENUE GOLD ! SMS envoyé.');
                        } catch (\Exception $e) {
                            $this->addFlash('warning', 'Statut GOLD ! Notez votre code : ' . $promoCode);
                        }
                    } elseif ($countAfter >= 5) {
                        $reservation->setPrice((int) round($basePrice * 0.85));
                        $this->addFlash('success', 'Statut Silver ! -15% appliqués.');
                    } else {
                        $reservation->setPrice((int) round($basePrice));
                    }
                } else {
                    $reservation->setPrice((int) round($basePrice));
                }
            }

            $em->persist($reservation);
            $em->flush();

            return $this->redirectToRoute('app_front_reservations');
        }

        $userCount = 0;

        if ($connectedUser instanceof Utilisateur) {
            $userCount = $repo->countUserReservationsThisYear($connectedUser);
        }

        return $this->render('Reservation/offersr.html.twig', [
            'userCount' => $userCount,
            'list' => $activeReservations,
            'history' => $historyReservations,
            'f' => $form->createView(),
            'editMode' => $editMode,
        ]);
    }

    #[Route('/autocomplete', name: 'app_front_transport_autocomplete')]
    public function autocomplete(Request $request, TransportRepository $repo): JsonResponse
    {
        $searchTerm = $request->query->get('q');
        $searchTerm = $searchTerm !== null ? (string) $searchTerm : null;

        $transports = $repo->searchAndSort($searchTerm, null);
        $results = [];

        foreach ($transports as $transport) {
            $results[] = $transport->getType() . ' to ' . $transport->getArrivee();
        }

        return new JsonResponse(array_unique($results));
    }

    #[Route('/chat/ask', name: 'app_chat_ask', methods: ['POST'])]
    public function ask(Request $request, OpenAiService $aiService, TransportRepository $repo): JsonResponse
    {
        $messageFromForm = $request->request->get('message');
        $userMessage = $messageFromForm !== null ? (string) $messageFromForm : '';

        if ($userMessage === '') {
            $data = json_decode($request->getContent(), true);

            if (is_array($data) && isset($data['message'])) {
                $userMessage = (string) $data['message'];
            }
        }

        if ($userMessage === '') {
            return new JsonResponse([
                'response' => 'Désolé, je n\'ai pas reçu votre message.',
            ], 400);
        }

        $aiResponse = $aiService->getTravelAdvice($userMessage);

        return new JsonResponse([
            'response' => $aiResponse,
        ]);
    }

    #[Route('/rate/{id}', name: 'app_reservation_rate', methods: ['POST'])]
    public function rateTrip(Reservation $reservation, Request $request, EntityManagerInterface $em): Response
    {
        $stars = $request->request->get('stars');
        $comment = $request->request->get('comment');
        $comment = $comment !== null ? (string) $comment : null;

        if ($stars !== null) {
            $reservation->setStars((int) $stars);
        }

        $reservation->setComment($comment);

        $em->persist($reservation);
        $em->flush();

        if ($request->isXmlHttpRequest()) {
            return new JsonResponse([
                'status' => 'success',
                'message' => 'Avis enregistré !',
            ]);
        }

        $this->addFlash('success', 'Avis enregistré avec succès !');

        return $this->redirectToRoute('app_front_reservations');
    }

    /**
     * @return array<int, array{name: string, image: string, season: string, type: string, desc: string}>
     */
    private function getTunisiaStates(): array
    {
        return [
            ['name' => 'Sousse', 'image' => 'sousse.jpg', 'season' => 'Été', 'type' => 'Plage', 'desc' => 'La perle du Sahel.'],
            ['name' => 'Monastir', 'image' => 'monastir.jpg', 'season' => 'Été', 'type' => 'Plage', 'desc' => 'Histoire et farniente.'],
            ['name' => 'Mahdia', 'image' => 'mahdia.jpg', 'season' => 'Été', 'type' => 'Plage', 'desc' => 'Eaux turquoise et calme.'],
            ['name' => 'Nabeul', 'image' => 'nabeul.jpg', 'season' => 'Été', 'type' => 'Artisanat', 'desc' => 'Poterie et soleil.'],
            ['name' => 'Bizerte', 'image' => 'bizerte.jpg', 'season' => 'Été', 'type' => 'Nord', 'desc' => 'Vieux port et plages.'],
            ['name' => 'Sfax', 'image' => 'sfax.jpg', 'season' => 'Été', 'type' => 'Économie', 'desc' => 'Capitale du Sud.'],

            ['name' => 'Zaghouan', 'image' => 'zaghwen.jpg', 'season' => 'Printemps', 'type' => 'Nature', 'desc' => 'Temple des eaux.'],
            ['name' => 'Béja', 'image' => 'beja.jpeg', 'season' => 'Printemps', 'type' => 'Vert', 'desc' => 'Cigognes et fromage.'],
            ['name' => 'Jendouba', 'image' => 'jandouba.jpg', 'season' => 'Printemps', 'type' => 'Forêt', 'desc' => 'Tabarka et Ain Draham.'],
            ['name' => 'Siliana', 'image' => 'siliana.jpg', 'season' => 'Printemps', 'type' => 'Montagne', 'desc' => 'Paysages sauvages.'],
            ['name' => 'Le Kef', 'image' => 'kef.jpg', 'season' => 'Printemps', 'type' => 'Culture', 'desc' => 'La citadelle byzantine.'],
            ['name' => 'Manouba', 'image' => 'manouba.jpg', 'season' => 'Printemps', 'type' => 'Histoire', 'desc' => 'Palais et vergers.'],

            ['name' => 'Tunis', 'image' => 'tunis.jpg', 'season' => 'Automne', 'type' => 'Capitale', 'desc' => 'La Médina et les musées.'],
            ['name' => 'Ariana', 'image' => 'ariana.jpg', 'season' => 'Automne', 'type' => 'Ville', 'desc' => 'Cité des roses.'],
            ['name' => 'Ben Arous', 'image' => 'benarous.jpg', 'season' => 'Automne', 'type' => 'Port', 'desc' => 'Radès et ses environs.'],
            ['name' => 'Kairouan', 'image' => 'kairouan.jpg', 'season' => 'Automne', 'type' => 'Religion', 'desc' => 'La ville sainte.'],
            ['name' => 'Sidi Bouzid', 'image' => 'sidibouzid.jpg', 'season' => 'Automne', 'type' => 'Agriculture', 'desc' => 'Terre de générosité.'],
            ['name' => 'Kassérine', 'image' => 'gassrin.jfif', 'season' => 'Automne', 'type' => 'Parc', 'desc' => 'Mont Chaambi.'],

            ['name' => 'Tozeur', 'image' => 'tozeur.jpg', 'season' => 'Hiver', 'type' => 'Désert', 'desc' => 'L’oasis de briques.'],
            ['name' => 'Kébili', 'image' => 'kebili.jpg', 'season' => 'Hiver', 'type' => 'Sahara', 'desc' => 'Dunes à perte de vue.'],
            ['name' => 'Tataouine', 'image' => 'tataouine.jpg', 'season' => 'Hiver', 'type' => 'Ksour', 'desc' => 'Décors de Star Wars.'],
            ['name' => 'Médenine', 'image' => 'medenine.jpg', 'season' => 'Hiver', 'type' => 'Berbère', 'desc' => 'Ghorfas uniques.'],
            ['name' => 'Gabès', 'image' => 'gabes.jpg', 'season' => 'Hiver', 'type' => 'Oasis', 'desc' => 'Oasis maritime.'],
            ['name' => 'Gafsa', 'image' => 'gafsa.jpg', 'season' => 'Hiver', 'type' => 'Phosphate', 'desc' => 'Piscines romaines.'],
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
}