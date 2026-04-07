<?php

namespace App\Controller;

use App\Entity\Event;
use App\Entity\Participation;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Symfony\Component\Security\Core\Exception\AccessDeniedException;

class EventController extends AbstractController
{
    // ──────────────────────────────────────────────
    //  FRONT — public client-facing view
    // ──────────────────────────────────────────────
#[IsGranted('ROLE_USER')]  // Add this line
    #[Route('/events', name: 'event_front')]
    public function front(EntityManagerInterface $em): Response
    {
        $events = $em->getRepository(Event::class)->findAll();

        return $this->render('front/event.html.twig', [
            'events' => $events,
        ]);
    }

    // ──────────────────────────────────────────────
    //  FRONT — Participation (requires login)
    // ──────────────────────────────────────────────
    #[Route('/events/{id_event}/register', name: 'participation_front_new')]
    #[IsGranted('ROLE_USER')]
    public function register(Request $request, int $id_event, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        if (!$user) {
            throw $this->createAccessDeniedException('You must be logged in to register for events.');
        }

        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) {
            throw $this->createNotFoundException('Event not found');
        }

        if ($event->getAvailablePlaces() <= 0) {
            $this->addFlash('error', 'This event is sold out!');
            return $this->redirectToRoute('event_front');
        }

        if ($request->isMethod('POST')) {
            $participation = new Participation();
            
            // Get user info from logged-in user
            $participation->setIdUser($user->getId());
            $participation->setNomParticipant($request->request->get('nom_participant'));
            $participation->setPrenomParticipant($request->request->get('prenom_participant'));
            $participation->setEmailParticipant($request->request->get('email_participant'));
            $participation->setTelephone($request->request->get('telephone'));
            $participation->setNombrePlaces((int) $request->request->get('nombre_places'));
            $participation->setDateParticipation(new \DateTime($request->request->get('date_participation')));
            $participation->setStatut($request->request->get('statut'));
            $participation->setEvent($event);

            // Update available places
            $newAvailable = $event->getAvailablePlaces() - (int) $request->request->get('nombre_places');
            $event->setAvailablePlaces($newAvailable);

            $em->persist($participation);
            $em->flush();

            $this->addFlash('success', 'Registration successful! Check your email for confirmation.');
            return $this->redirectToRoute('event_front', ['registered' => 1]);
        }

        return $this->render('front/participation.html.twig', [
            'mode' => 'new',
            'event' => $event,
            'user' => $user,
        ]);
    }

    #[Route('/events/participation/{id_participation}', name: 'participation_front_show')]
    #[IsGranted('ROLE_USER')]
    public function showParticipation(int $id_participation, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        $participation = $em->getRepository(Participation::class)->find($id_participation);
        
        if (!$participation) {
            throw $this->createNotFoundException('Participation not found');
        }
        
        // Check if the participation belongs to the logged-in user
        if ($participation->getIdUser() != $user->getId()) {
            throw $this->createAccessDeniedException('You can only view your own registrations.');
        }

        $event = $participation->getEvent();

        return $this->render('front/participation.html.twig', [
            'mode' => 'show',
            'event' => $event,
            'participation' => $participation,
        ]);
    }

    // ──────────────────────────────────────────────
    //  BACK — admin CRUD
    // ──────────────────────────────────────────────

    #[Route('/admin/events', name: 'event_index')]
    #[IsGranted('ROLE_ADMIN')]
    public function index(EntityManagerInterface $em): Response
    {
        $events = $em->getRepository(Event::class)->findAll();

        return $this->render('back/event.html.twig', [
            'mode'   => 'list',
            'events' => $events,
        ]);
    }

    #[Route('/admin/events/new', name: 'event_new')]
    #[IsGranted('ROLE_ADMIN')]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $event = new Event();

        if ($request->isMethod('POST')) {
            $this->handleForm($request, $event);
            $em->persist($event);
            $em->flush();
            $this->addFlash('success', 'Event created successfully!');
            return $this->redirectToRoute('event_index');
        }

        return $this->render('back/event.html.twig', [
            'mode'  => 'new',
            'event' => $event,
        ]);
    }

    #[Route('/admin/events/{id_event}', name: 'event_show')]
    #[IsGranted('ROLE_ADMIN')]
    public function show(int $id_event, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        return $this->render('back/event.html.twig', [
            'mode'  => 'show',
            'event' => $event,
        ]);
    }

    #[Route('/admin/events/{id_event}/edit', name: 'event_edit')]
    #[IsGranted('ROLE_ADMIN')]
    public function edit(int $id_event, Request $request, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        if ($request->isMethod('POST')) {
            $this->handleForm($request, $event);
            $em->flush();
            $this->addFlash('success', 'Event updated successfully!');
            return $this->redirectToRoute('event_index');
        }

        return $this->render('back/event.html.twig', [
            'mode'  => 'edit',
            'event' => $event,
        ]);
    }

    #[Route('/admin/events/{id_event}/delete', name: 'event_delete')]
    #[IsGranted('ROLE_ADMIN')]
    public function delete(int $id_event, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        $em->remove($event);
        $em->flush();
        $this->addFlash('success', 'Event deleted successfully!');

        return $this->redirectToRoute('event_index');
    }

    private function handleForm(Request $request, Event $event): void
    {
        $event->setTitle($request->request->get('title'));
        $event->setDescription($request->request->get('description'));
        $event->setDate_event(new \DateTime($request->request->get('date_event')));
        $event->setStart_time($request->request->get('start_time'));
        $event->setLocation($request->request->get('location'));
        $event->setEvent_type($request->request->get('event_type'));
        $event->setSeason($request->request->get('season'));
        $event->setCapacity((int) $request->request->get('capacity'));
        $event->setAvailable_places((int) $request->request->get('available_places'));
        $event->setStatus($request->request->get('status'));
        $event->setImage_event($request->request->get('image_event', ''));
        $event->setPrice((float) $request->request->get('price'));
    }
}