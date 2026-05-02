<?php

namespace App\Controller;

use App\Entity\Transport;
use App\Entity\Utilisateur;
use App\Form\TransportType;
use App\Repository\ReservationRepository;
use App\Repository\TransportRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/transport')]
final class TransportController extends AbstractController
{
    #[Route('/affiche', name: 'displayTransport')]
    public function listTransportsFromDB(TransportRepository $repo): Response
    {
        return $this->render('transport/listFromDb.html.twig', [
            'list' => $repo->findAll(),
        ]);
    }

    #[Route('/add', name: 'addTransport')]
    public function addTransport(EntityManagerInterface $em, Request $request): Response
    {
        $newTransport = new Transport();

        $form = $this->createForm(TransportType::class, $newTransport);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $user = $this->getUser();

            if (!$user instanceof Utilisateur) {
                throw $this->createAccessDeniedException('Vous devez être connecté.');
            }

            $newTransport->setUser_id($user);

            $em->persist($newTransport);
            $em->flush();

            return $this->redirectToRoute('displayTransport');
        }

        return $this->render('transport/add.html.twig', [
            'f' => $form->createView(),
        ]);
    }

    #[Route('/delete/{id}', name: 'deleteTransport')]
    public function delete(Transport $transport, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        $owner = $transport->getUser_id();

        if (
            !$this->isGranted('ROLE_ADMIN')
            && (!$owner || $owner->getId() !== $user->getId())
        ) {
            throw $this->createAccessDeniedException('Access Denied: You do not own this record.');
        }

        $em->remove($transport);
        $em->flush();

        return $this->redirectToRoute('displayTransport');
    }

    #[Route('/update/{id}', name: 'updateTransport')]
    public function updateTransport(Transport $transport, EntityManagerInterface $em, Request $request): Response
    {
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        $owner = $transport->getUser_id();

        if (
            !$this->isGranted('ROLE_ADMIN')
            && (!$owner || $owner->getId() !== $user->getId())
        ) {
            throw $this->createAccessDeniedException('Access Denied: You do not own this record.');
        }

        $form = $this->createForm(TransportType::class, $transport);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();

            return $this->redirectToRoute('displayTransport');
        }

        return $this->render('transport/add.html.twig', [
            'f' => $form->createView(),
        ]);
    }

    #[Route('/transport/map-data', name: 'transport_map_data')]
    public function getMapData(TransportRepository $repo): JsonResponse
    {
        $transports = $repo->findAll();

        $coords = [
            'tunis' => [36.8065, 10.1815],
            'sousse' => [35.8256, 10.6084],
            'sfax' => [34.7406, 10.7603],
            'kairouan' => [35.6781, 10.0963],
            'kairaouen' => [35.6781, 10.0963],
            'integration' => [36.8065, 10.1815],
        ];

        $data = [];

        foreach ($transports as $transport) {
            $city = strtolower((string) $transport->getArrivee());

            if (isset($coords[$city])) {
                $data[] = [
                    'lat' => $coords[$city][0],
                    'lng' => $coords[$city][1],
                    'city' => $city,
                    'label' => $transport->getType() . ' (' . $transport->getTarif() . ' TND)',
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

        if (!is_array($data)) {
            return $this->json([
                'status' => 'error',
                'message' => 'Données invalides.',
            ]);
        }

        $type = strtolower(trim((string) ($data['type'] ?? '')));
        $depart = trim((string) ($data['depart'] ?? ''));
        $arrivee = trim((string) ($data['arrivee'] ?? ''));
        $dateString = $data['date'] ?? null;

        if ($type === '' || $depart === '' || $arrivee === '' || !$dateString) {
            return $this->json([
                'status' => 'error',
                'message' => 'Veuillez remplir tous les champs.',
            ]);
        }

        try {
            $date = new \DateTime((string) $dateString);
        } catch (\Exception) {
            return $this->json([
                'status' => 'error',
                'message' => 'Format de date invalide.',
            ]);
        }

        $transport = $transportRepo->findOneBy([
            'type' => $type,
            'depart' => $depart,
            'arrivee' => $arrivee,
        ]);

        if (!$transport) {
            return $this->json([
                'status' => 'no_transport',
                'message' => 'Aucun trajet trouvé. Tarif par défaut appliqué.',
                'price' => 50,
            ]);
        }

        $capacity = (int) $transport->getCapacite();
        $bookedSeats = (int) $reservationRepo->countBookedSeats($transport->getId(), $date);
        $remaining = $capacity - $bookedSeats;

        if ($remaining <= 0) {
            return $this->json([
                'status' => 'full',
                'message' => 'Ce trajet est complet.',
            ]);
        }

        return $this->json([
            'status' => 'available',
            'message' => "Places restantes: $remaining",
            'price' => $transport->getTarif(),
        ]);
    }
}