<?php

namespace App\Controller;

use App\Entity\Transport;
use App\Form\TransportType;
use App\Repository\TransportRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use App\Repository\ReservationRepository;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\HttpFoundation\JsonResponse;

#[Route('/transport')]
final class TransportController extends AbstractController
{
    #[Route('/affiche', name: "displayTransport")]
    public function listTransportsFromDB(TransportRepository $repo): Response
    {
        return $this->render('transport/listFromDb.html.twig', ['list' => $repo->findAll()]);
    }

    #[Route('/add', name: "addTransport")]
    public function addTransport(EntityManagerInterface $em, Request $request): Response
    {
        $newTransport = new Transport();
        $form = $this->createForm(TransportType::class, $newTransport);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // CRITICAL: Set the user AFTER handleRequest to ensure the form doesn't overwrite it with NULL
            $newTransport->setUser_id($this->getUser());
            $transport->setCapacite(
                $transport->getCapacite() + $nbPlaces
            );
            $em->persist($newTransport);
            $em->flush();
            return $this->redirectToRoute('displayTransport');
        }

        return $this->render('transport/add.html.twig', [
            'f' => $form->createView()
        ]);
    }

    #[Route('/delete/{id}', name: "deleteTransport")]
    public function delete(Transport $transport, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();

        // Fix naming: $user instead of $currentUser
        // Check if user_id object exists before calling getId()
        $ownerId = $transport->getUser_id() ? $transport->getUser_id()->getId() : null;

        if ($ownerId !== $user->getId() && !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException('Access Denied: You do not own this record.');
        }
        
        $em->remove($transport);
        $em->flush();
        
        return $this->redirectToRoute('displayTransport');
    }

    #[Route('/update/{id}', name: "updateTransport")]
    public function updateTransport(Transport $transport, EntityManagerInterface $em, Request $request): Response
    {
        $user = $this->getUser();

        // Security Check
        $ownerId = $transport->getUser_id() ? $transport->getUser_id()->getId() : null;
        if ($ownerId !== $user->getId() && !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException('Access Denied: You do not own this record.');
        }
        
        $form = $this->createForm(TransportType::class, $transport);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush(); // persist is not needed for updates
            return $this->redirectToRoute('displayTransport');
        }

        return $this->render('transport/add.html.twig', ['f' => $form->createView()]);
    }

    // ... Keep your search functions here
    #[Route('/transport/map-data', name: 'transport_map_data')]
public function getMapData(TransportRepository $repo): JsonResponse
{
    $transports = $repo->findAll();
    
    $coords = [
    'tunis'       => [36.8065, 10.1815],
    'sousse'      => [35.8256, 10.6084],
    'sfax'        => [34.7406, 10.7603],
    'kairouan'    => [35.6781, 10.0963],
    'kairaouen'   => [35.6781, 10.0963], // Pour gérer ta donnée actuelle
    'integration' => [36.8065, 10.1815], // Ville de test
];

    $data = [];
    foreach ($transports as $t) {
        // On met tout en minuscule pour la comparaison
        $city = strtolower($t->getArrivee()); 
        
        if (isset($coords[$city])) {
            $data[] = [
                'lat' => $coords[$city][0],
                'lng' => $coords[$city][1],
                'city' => $city,
                'label' => $t->getType() . " (" . $t->getTarif() . " TND)"
            ];
        }
    }
    return new JsonResponse($data);
}
#[Route('/transport/live-check', name: 'app_transport_live_check', methods: ['POST'])]
public function liveCheck(
    Request $request,
    TransportRepository $transportRepo,
    ReservationRepository $reservationRepo
): JsonResponse {

    $data = json_decode($request->getContent(), true);

    $type = strtolower(trim($data['type'] ?? ''));
    $depart = trim($data['depart'] ?? '');
    $arrivee = trim($data['arrivee'] ?? '');
    $dateString = $data['date'] ?? null;

    if (!$type || !$depart || !$arrivee || !$dateString) {
        return $this->json([
            'status' => 'error',
            'message' => 'Veuillez remplir tous les champs.'
        ]);
    }

    try {
        $date = new \DateTime($dateString);
    } catch (\Exception $e) {
        return $this->json([
            'status' => 'error',
            'message' => 'Format de date invalide.'
        ]);
    }

    // Find transport (NO DATE in entity search)
    $transport = $transportRepo->findOneBy([
        'type' => $type,
        'depart' => $depart,
        'arrivee' => $arrivee
    ]);

    // NO transport → default price
    if (!$transport) {
        return $this->json([
            'status' => 'no_transport',
            'message' => 'Aucun trajet trouvé. Tarif par défaut appliqué.',
            'price' => 50
        ]);
    }

    // capacity
    $capacity = (int) $transport->getCapacite();

    // booked seats (SUM of passengers)
    $bookedSeats = (int) $reservationRepo->countBookedSeats($transport->getId(), $date);

    $remaining = $capacity - $bookedSeats;

    if ($remaining <= 0) {
        return $this->json([
            'status' => 'full',
            'message' => 'Ce trajet est complet.'
        ]);
    }

    return $this->json([
        'status' => 'available',
        'message' => "Places restantes: $remaining",
        'price' => $transport->getTarif()
    ]);
}
private function getMaxCapacity(string $type): int
{
    return match (strtolower($type)) {
        'bus' => 50,
        'train' => 200,
        'avion' => 180,
        'bateau' => 80,
        'taxi' => 4,
        default => 0,
    };
}
}