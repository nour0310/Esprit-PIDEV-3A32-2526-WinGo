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
use Symfony\Component\Validator\Validator\ValidatorInterface;

class ParticipationController extends AbstractController
{
    // FRONT — new registration (standalone form)
    #[Route('/events/{id_event}/register', name: 'participation_front_new')]
    #[IsGranted('ROLE_USER')]
    public function frontNew(int $id_event, Request $request, EntityManagerInterface $em, ValidatorInterface $validator): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) {
            throw $this->createNotFoundException('Event not found');
        }

        $participation = new Participation();
        
        $user = $this->getUser();
        if ($user instanceof \App\Entity\Utilisateur) {
            $participation->setIdUser($user->getId());
        }
        $participation->setDateParticipation(new \DateTime());
        $participation->setStatut('pending');
        $participation->setIdEvent($event);

        if ($request->isMethod('POST')) {
            $this->hydrateParticipation($request, $participation);

            $errors = $validator->validate($participation);
            if (count($errors) === 0) {
                $places = $participation->getNombrePlaces();
                if ($places > $event->getAvailable_places()) {
                    $this->addFlash('error', "Only {$event->getAvailable_places()} seat(s) available.");
                } else {
                    $event->setAvailable_places($event->getAvailable_places() - $places);
                    $em->persist($participation);
                    $em->flush();
                    $this->addFlash('success', 'Registration successful!');
                    return $this->redirectToRoute('event_front');
                }
            } else {
                foreach ($errors as $error) {
                    $this->addFlash('error', $error->getMessage());
                }
            }
        }

        return $this->render('front/participation.html.twig', [
            'mode' => 'new',
            'event' => $event,
            'form' => null,
        ]);
    }

    #[Route('/events/{id_event}/register/{id_participation}/confirm', name: 'participation_front_show')]
    #[IsGranted('ROLE_USER')]
    public function frontShow(int $id_event, int $id_participation, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        $participation = $em->getRepository(Participation::class)->find($id_participation);
        if (!$event || !$participation) throw $this->createNotFoundException();

        return $this->render('front/participation.html.twig', [
            'mode' => 'show',
            'event' => $event,
            'participation' => $participation,
        ]);
    }

    // BACK — admin CRUD for participations
    #[Route('/admin/events/{id_event}/participations', name: 'participation_index')]
    #[IsGranted('ROLE_ADMIN')]
    public function index(int $id_event, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        $participations = $em->getRepository(Participation::class)->findBy(['id_event' => $event]);

        return $this->render('back/participation.html.twig', [
            'mode' => 'list',
            'event' => $event,
            'participations' => $participations,
        ]);
    }

    #[Route('/admin/events/{id_event}/participations/new', name: 'participation_new')]
    #[IsGranted('ROLE_ADMIN')]
    public function new(Request $request, int $id_event, EntityManagerInterface $em, ValidatorInterface $validator): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        $participation = new Participation();
        $participation->setIdEvent($event);
        $participation->setDateParticipation(new \DateTime());
        $participation->setIdUser(0);

        $errors = [];

        if ($request->isMethod('POST')) {
            $this->hydrateParticipation($request, $participation);

            $errors = $validator->validate($participation);
            if (count($errors) === 0) {
                $places = $participation->getNombrePlaces();
                if ($places > $event->getAvailable_places()) {
                    $this->addFlash('error', "Only {$event->getAvailable_places()} seat(s) available.");
                } else {
                    $event->setAvailable_places($event->getAvailable_places() - $places);
                    $em->persist($participation);
                    $em->flush();
                    $this->addFlash('success', 'Registration added.');
                    return $this->redirectToRoute('participation_index', ['id_event' => $event->getId_event()]);
                }
            }
        }

        return $this->render('back/participation.html.twig', [
            'mode' => 'new',
            'event' => $event,
            'participation' => $participation,
            'validationErrors' => $errors,
        ]);
    }

    #[Route('/admin/events/{id_event}/participations/{id_participation}/edit', name: 'participation_edit')]
    #[IsGranted('ROLE_ADMIN')]
    public function edit(Request $request, int $id_event, int $id_participation, EntityManagerInterface $em, ValidatorInterface $validator): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        $participation = $em->getRepository(Participation::class)->find($id_participation);
        if (!$event || !$participation) throw $this->createNotFoundException();

        $oldPlaces = $participation->getNombrePlaces();

        if ($request->isMethod('POST')) {
            $this->hydrateParticipation($request, $participation);

            $errors = $validator->validate($participation);
            if (count($errors) === 0) {
                $newPlaces = $participation->getNombrePlaces();
                $delta = $newPlaces - $oldPlaces;
                if ($delta > 0 && $delta > $event->getAvailable_places()) {
                    $this->addFlash('error', "Not enough seats (only {$event->getAvailable_places()} left).");
                } else {
                    $event->setAvailable_places($event->getAvailable_places() - $delta);
                    $em->flush();
                    $this->addFlash('success', 'Registration updated.');
                    return $this->redirectToRoute('participation_index', ['id_event' => $event->getId_event()]);
                }
            }
        }

        return $this->render('back/participation.html.twig', [
            'mode' => 'edit',
            'event' => $event,
            'participation' => $participation,
            'validationErrors' => $errors ?? [],
        ]);
    }

    #[Route('/admin/events/{id_event}/participations/{id_participation}', name: 'participation_show')]
    #[IsGranted('ROLE_ADMIN')]
    public function show(int $id_event, int $id_participation, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        $participation = $em->getRepository(Participation::class)->find($id_participation);
        if (!$event || !$participation) throw $this->createNotFoundException();

        return $this->render('back/participation.html.twig', [
            'mode' => 'show',
            'event' => $event,
            'participation' => $participation,
        ]);
    }

    #[Route('/admin/events/{id_event}/participations/{id_participation}/delete', name: 'participation_delete')]
    #[IsGranted('ROLE_ADMIN')]
    public function delete(int $id_event, int $id_participation, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        $participation = $em->getRepository(Participation::class)->find($id_participation);
        if (!$event || !$participation) throw $this->createNotFoundException();

        // Restore seats
        $event->setAvailable_places($event->getAvailable_places() + $participation->getNombrePlaces());
        $em->remove($participation);
        $em->flush();

        $this->addFlash('success', 'Registration deleted.');
        return $this->redirectToRoute('participation_index', ['id_event' => $event->getId_event()]);
    }

    // HELPER
    private function hydrateParticipation(Request $request, Participation $p): void
    {
        $p->setNomParticipant($request->request->get('nom_participant'));
        $p->setPrenomParticipant($request->request->get('prenom_participant'));
        $p->setEmailParticipant($request->request->get('email_participant'));
        $p->setTelephone($request->request->get('telephone'));
        $p->setNombrePlaces((int) $request->request->get('nombre_places'));
        $p->setStatut($request->request->get('statut'));
        $p->setIdUser((int) $request->request->get('id_user'));
        if ($request->request->get('date_participation')) {
            $p->setDateParticipation(new \DateTime($request->request->get('date_participation')));
        }
    }
}