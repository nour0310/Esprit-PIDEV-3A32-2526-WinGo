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

class EventController extends AbstractController
{
    // FRONT — public events listing
    #[IsGranted('ROLE_USER')]
    #[Route('/events', name: 'event_front')]
    public function front(EntityManagerInterface $em): Response
    {
        $events = $em->getRepository(Event::class)->findAll();
        return $this->render('front/event.html.twig', ['events' => $events]);
    }

    // BACK — admin CRUD for events
    #[Route('/admin/events', name: 'event_index')]
    #[IsGranted('ROLE_ADMIN')]
    public function index(EntityManagerInterface $em): Response
    {
        $events = $em->getRepository(Event::class)->findAll();
        return $this->render('back/event.html.twig', ['mode' => 'list', 'events' => $events]);
    }

    #[Route('/admin/events/new', name: 'event_new')]
    #[IsGranted('ROLE_ADMIN')]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $event = new Event();
        if ($request->isMethod('POST')) {
            $this->handleForm($request, $event);
            $imageName = $this->uploadImage($request);
            if ($imageName) {
                $event->setImage_event($imageName);
            }
            $em->persist($event);
            $em->flush();
            $this->addFlash('success', 'Event created successfully!');
            return $this->redirectToRoute('event_index');
        }
        return $this->render('back/event.html.twig', ['mode' => 'new', 'event' => $event]);
    }

    #[Route('/admin/events/{id_event}', name: 'event_show')]
    #[IsGranted('ROLE_ADMIN')]
    public function show(int $id_event, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');
        return $this->render('back/event.html.twig', ['mode' => 'show', 'event' => $event]);
    }

    #[Route('/admin/events/{id_event}/edit', name: 'event_edit')]
    #[IsGranted('ROLE_ADMIN')]
    public function edit(int $id_event, Request $request, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);
        if (!$event) throw $this->createNotFoundException('Event not found');

        if ($request->isMethod('POST')) {
            $this->handleForm($request, $event);
            $newImage = $this->uploadImage($request, $event->getImage_event());
            if ($newImage !== $event->getImage_event()) {
                $event->setImage_event($newImage);
            }
            $em->flush();
            $this->addFlash('success', 'Event updated successfully!');
            return $this->redirectToRoute('event_index');
        }
        return $this->render('back/event.html.twig', ['mode' => 'edit', 'event' => $event]);
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

    // HELPERS
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
        $event->setPrice((float) $request->request->get('price'));
    }

    private function uploadImage(Request $request, ?string $oldImage = null): ?string
    {
        $file = $request->files->get('image_file');
        if (!$file) {
            return $oldImage;
        }

        // Récupérer l'extension depuis le nom original (ne nécessite pas l'extension fileinfo)
        $extension = $file->getClientOriginalExtension();
        if (empty($extension)) {
            $extension = 'jpg'; // extension par défaut
        }

        // Générer un nom de fichier unique
        $safeName = uniqid('event_', true) . '.' . $extension;

        try {
            $file->move($this->getParameter('images_directory'), $safeName);
        } catch (\Exception $e) {
            $this->addFlash('error', 'Could not upload image');
            return $oldImage;
        }

        // Supprimer l'ancienne image si elle existe
        if ($oldImage && file_exists($this->getParameter('images_directory') . '/' . $oldImage)) {
            unlink($this->getParameter('images_directory') . '/' . $oldImage);
        }

        return $safeName;
    }
}