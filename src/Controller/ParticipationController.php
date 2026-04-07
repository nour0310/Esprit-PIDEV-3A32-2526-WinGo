<?php

namespace App\Controller;

use App\Entity\Event;
use App\Entity\Participation;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class ParticipationController extends AbstractController
{
    // ──────────────────────────────────────────────
    //  FRONT — client registration flow
    // ──────────────────────────────────────────────

    #[Route('/events/{id_event}/register', name: 'participation_front_new')]
    public function frontNew(int $id_event, Request $request, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        $participation = new Participation();

        if ($request->isMethod('POST')) {
            $nombrePlaces = (int) $request->request->get('nombre_places', 1);

            // Check enough places available
            if ($nombrePlaces > $event->getAvailable_places()) {
                return $this->render('front/participation.html.twig', [
                    'mode'          => 'new',
                    'event'         => $event,
                    'participation' => $participation,
                    'error'         => 'Not enough available places. Only ' . $event->getAvailable_places() . ' left.',
                ]);
            }

            $this->handleForm($request, $participation, $event);

            // Decrease available places
            $event->setAvailable_places($event->getAvailable_places() - $nombrePlaces);

            $em->persist($participation);
            $em->flush();

            return $this->redirectToRoute('participation_front_show', [
                'id_event'         => $id_event,
                'id_participation' => $participation->getId_participation(),
            ]);
        }

        return $this->render('front/participation.html.twig', [
            'mode'          => 'new',
            'event'         => $event,
            'participation' => $participation,
        ]);
    }

    /**
     * Booking confirmation page shown to the client after registering.
     */
    #[Route('/events/{id_event}/register/{id_participation}/confirm', name: 'participation_front_show')]
    public function frontShow(int $id_event, int $id_participation, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        $participation = $em->getRepository(Participation::class)->find($id_participation);
        if (!$participation) throw $this->createNotFoundException('Participation not found');

        return $this->render('front/participation.html.twig', [
            'mode'          => 'show',
            'event'         => $event,
            'participation' => $participation,
        ]);
    }

    // ──────────────────────────────────────────────
    //  BACK — admin CRUD for participations
    // ──────────────────────────────────────────────

    #[Route('/admin/events/{id_event}/participations', name: 'participation_index')]
    public function index(int $id_event, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        $participations = $em->getRepository(Participation::class)->findBy(['id_event' => $event]);

        return $this->render('back/participation.html.twig', [
            'mode'           => 'list',
            'event'          => $event,
            'participations' => $participations,
        ]);
    }

    /**
     * Admin — create a participation manually.
     */
    #[Route('/admin/events/{id_event}/participations/new', name: 'participation_new')]
    public function new(int $id_event, Request $request, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        $participation = new Participation();

        if ($request->isMethod('POST')) {
            $nombrePlaces = (int) $request->request->get('nombre_places', 1);

            // Check enough places available
            if ($nombrePlaces > $event->getAvailable_places()) {
                return $this->render('back/participation.html.twig', [
                    'mode'          => 'new',
                    'event'         => $event,
                    'participation' => $participation,
                    'error'         => 'Not enough available places. Only ' . $event->getAvailable_places() . ' left.',
                ]);
            }

            $this->handleForm($request, $participation, $event);

            // Decrease available places
            $event->setAvailable_places($event->getAvailable_places() - $nombrePlaces);

            $em->persist($participation);
            $em->flush();

            return $this->redirectToRoute('participation_index', ['id_event' => $id_event]);
        }

        return $this->render('back/participation.html.twig', [
            'mode'          => 'new',
            'event'         => $event,
            'participation' => $participation,
        ]);
    }

    /**
     * Admin — view a single participation.
     */
    #[Route('/admin/events/{id_event}/participations/{id_participation}', name: 'participation_show')]
    public function show(int $id_event, int $id_participation, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        $participation = $em->getRepository(Participation::class)->find($id_participation);
        if (!$participation) throw $this->createNotFoundException('Participation not found');

        return $this->render('back/participation.html.twig', [
            'mode'          => 'show',
            'event'         => $event,
            'participation' => $participation,
        ]);
    }

    /**
     * Admin — edit a participation.
     */
    #[Route('/admin/events/{id_event}/participations/{id_participation}/edit', name: 'participation_edit')]
    public function edit(int $id_event, int $id_participation, Request $request, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        $participation = $em->getRepository(Participation::class)->find($id_participation);
        if (!$participation) throw $this->createNotFoundException('Participation not found');

        if ($request->isMethod('POST')) {
            $oldPlaces = $participation->getNombre_places();
            $newPlaces = (int) $request->request->get('nombre_places', 1);
            $diff = $newPlaces - $oldPlaces;

            // Check enough places for the difference
            if ($diff > 0 && $diff > $event->getAvailable_places()) {
                return $this->render('back/participation.html.twig', [
                    'mode'          => 'edit',
                    'event'         => $event,
                    'participation' => $participation,
                    'error'         => 'Not enough available places. Only ' . $event->getAvailable_places() . ' left.',
                ]);
            }

            $this->handleForm($request, $participation, $event);

            // Adjust available places by the difference
            $event->setAvailable_places($event->getAvailable_places() - $diff);

            $em->flush();

            return $this->redirectToRoute('participation_index', ['id_event' => $id_event]);
        }

        return $this->render('back/participation.html.twig', [
            'mode'          => 'edit',
            'event'         => $event,
            'participation' => $participation,
        ]);
    }

    /**
     * Admin — delete a participation.
     */
    #[Route('/admin/events/{id_event}/participations/{id_participation}/delete', name: 'participation_delete')]
    public function delete(int $id_event, int $id_participation, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        $participation = $em->getRepository(Participation::class)->find($id_participation);
        if (!$participation) throw $this->createNotFoundException('Participation not found');

        // Restore the places back to available
        $event->setAvailable_places($event->getAvailable_places() + $participation->getNombre_places());

        $em->remove($participation);
        $em->flush();

        return $this->redirectToRoute('participation_index', ['id_event' => $id_event]);
    }

    // ──────────────────────────────────────────────
    //  Shared form handler
    // ──────────────────────────────────────────────

    private function handleForm(Request $request, Participation $participation, Event $event): void
    {
        $participation->setId_event($event);
        $participation->setId_user((int) $request->request->get('id_user', 0));
        $participation->setDate_participation(new \DateTime($request->request->get('date_participation')));
        $participation->setStatut($request->request->get('statut'));
        $participation->setNom_participant($request->request->get('nom_participant'));
        $participation->setPrenom_participant($request->request->get('prenom_participant'));
        $participation->setEmail_participant($request->request->get('email_participant'));
        $participation->setTelephone($request->request->get('telephone'));
        $participation->setNombre_places((int) $request->request->get('nombre_places'));
    }
}