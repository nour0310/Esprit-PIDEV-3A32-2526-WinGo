<?php

namespace App\Controller;

use App\Entity\Event;
use App\Entity\Participation;
use App\Entity\Utilisateur;
use App\Service\DiscountEventService;
use App\Service\PollinationsImageService;
use App\Service\RecommendationEventService;
use App\Service\WeatherEventService;
use Doctrine\ORM\EntityManagerInterface;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

class EventController extends AbstractController
{
    #[IsGranted('ROLE_USER')]
    #[Route('/events', name: 'event_front')]
    public function front(
        Request $request,
        EntityManagerInterface $em,
        PaginatorInterface $paginator,
        WeatherEventService $weatherEventService,
        RecommendationEventService $recommendationEventService,
        DiscountEventService $discountEventService
    ): Response {
        $qb = $em->getRepository(Event::class)->createQueryBuilder('e');

        $search = (string) $request->query->get('search', '');
        if ($search !== '') {
            $qb->andWhere('e.title LIKE :search OR e.description LIKE :search')
                ->setParameter('search', '%' . $search . '%');
        }

        $type = (string) $request->query->get('type', '');
        if ($type !== '' && $type !== 'all') {
            $qb->andWhere('e.event_type = :type')
                ->setParameter('type', $type);
        }

        $sort = (string) $request->query->get('event_sort', 'date_asc');
        switch ($sort) {
            case 'date_desc':
                $qb->orderBy('e.date_event', 'DESC');
                break;
            case 'price_asc':
                $qb->orderBy('e.price', 'ASC');
                break;
            case 'price_desc':
                $qb->orderBy('e.price', 'DESC');
                break;
            case 'title_asc':
                $qb->orderBy('e.title', 'ASC');
                break;
            default:
                $qb->orderBy('e.date_event', 'ASC');
                break;
        }

        $pagination = $paginator->paginate($qb, $request->query->getInt('page', 1), 5);
        $events = $pagination->getItems();

        foreach ($events as $event) {
            if (!$event instanceof Event) {
                continue;
            }

            $event->weatherData = $weatherEventService->getWeatherForEvent($event->getLocation());
            $event->discountedPrice = $discountEventService->getDiscountedPrice($event);
            $event->discountActive = $discountEventService->isDiscountActive($event);
        }

        $user = $this->getCurrentUtilisateur();
        $userId = $this->getCurrentUtilisateurId($user);

        $participations = $em->getRepository(Participation::class)
            ->createQueryBuilder('p')
            ->select('IDENTITY(p.id_event) as eventId')
            ->where('p.id_user = :userId')
            ->andWhere('p.statut = :status')
            ->setParameter('userId', $userId)
            ->setParameter('status', 'confirmed')
            ->getQuery()
            ->getScalarResult();

        $registeredEventIds = array_column($participations, 'eventId');

        $today = new \DateTime();
        $today->setTime(0, 0, 0);

        foreach ($events as $event) {
            if (!$event instanceof Event) {
                continue;
            }

            $eventDate = $event->getDate_event();

            if ($eventDate instanceof \DateTime) {
                $eventDate->setTime(0, 0, 0);
            }

            $event->setIsPassed($eventDate < $today);
        }

        $recommendedEvents = $recommendationEventService->getRecommendedEvents(6);

        foreach ($recommendedEvents as $event) {

            $event->weatherData = $weatherEventService->getWeatherForEvent($event->getLocation());
            $event->discountedPrice = $discountEventService->getDiscountedPrice($event);
            $event->discountActive = $discountEventService->isDiscountActive($event);
        }

        return $this->render('front/event.html.twig', [
            'pagination' => $pagination,
            'events' => $events,
            'current_search' => $search,
            'current_type' => $type,
            'current_sort' => $sort,
            'registered_event_ids' => $registeredEventIds,
            'recommended_events' => $recommendedEvents,
        ]);
    }

    #[Route('/event/{id}', name: 'event_show_front')]
    public function showFront(Event $event, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        $userParticipated = false;
        $alreadyLeft = false;

        if ($user instanceof Utilisateur) {
            $userId = $this->getCurrentUtilisateurId($user);

            $participation = $em->getRepository(Participation::class)->findOneBy([
                'id_event' => $event,
                'id_user' => $userId,
                'statut' => 'confirmed',
            ]);

            $userParticipated = $participation !== null;
            $alreadyLeft = $event->hasUserFeedback($userId);
        }

        return $this->render('front/event_show.html.twig', [
            'event' => $event,
            'userParticipated' => $userParticipated,
            'alreadyLeft' => $alreadyLeft,
        ]);
    }

    #[Route('/event/{id}/participants', name: 'event_participants')]
    public function participants(Event $event, EntityManagerInterface $em): Response
    {
        $user = $this->getCurrentUtilisateur();
        $userId = $this->getCurrentUtilisateurId($user);

        $participation = $em->getRepository(Participation::class)->findOneBy([
            'id_event' => $event,
            'id_user' => $userId,
            'statut' => 'confirmed',
        ]);

        if (!$participation instanceof Participation) {
            $this->addFlash('error', 'You must be a participant to view the participant list.');

            return $this->redirectToRoute('event_show_front', [
                'id' => $event->getId_event(),
            ]);
        }

        $participations = $em->getRepository(Participation::class)->createQueryBuilder('p')
            ->where('p.id_event = :event')
            ->andWhere('p.statut = :status')
            ->setParameter('event', $event)
            ->setParameter('status', 'confirmed')
            ->getQuery()
            ->getResult();

        $participants = [];
        $userRepo = $em->getRepository(Utilisateur::class);

        foreach ($participations as $p) {
            if (!$p instanceof Participation) {
                continue;
            }

            $participantUser = $userRepo->find($p->getIdUser());

            if ($participantUser instanceof Utilisateur) {
                $participants[] = $participantUser;
            }
        }

        return $this->render('front/event_participants.html.twig', [
            'event' => $event,
            'participants' => $participants,
        ]);
    }

    #[Route('/admin/events/generate-image', name: 'event_generate_image', methods: ['POST'])]
    #[IsGranted('ROLE_ADMIN')]
    public function generateImage(Request $request, PollinationsImageService $imageService): Response
    {
        $prompt = (string) $request->request->get('prompt', '');

        if ($prompt === '') {
            return $this->json(['error' => 'Prompt is required'], 400);
        }

        $width = (int) $request->request->get('width', 1024);
        $height = (int) $request->request->get('height', 1024);
        $model = (string) $request->request->get('model', 'flux');

        try {
            $filename = $imageService->generateImage($prompt, $width, $height, $model);

            return $this->json([
                'success' => true,
                'filename' => $filename,
                'url' => $this->generateUrl('home') . 'images/' . $filename,
            ]);
        } catch (\Exception $e) {
            return $this->json(['error' => $e->getMessage()], 500);
        }
    }

    #[Route('/admin/events', name: 'event_index')]
    #[IsGranted('ROLE_ADMIN')]
    public function index(Request $request, EntityManagerInterface $em): Response
    {
        $qb = $em->getRepository(Event::class)->createQueryBuilder('e');

        $search = (string) $request->query->get('search', '');
        if ($search !== '') {
            $qb->andWhere('e.title LIKE :search OR e.location LIKE :search')
                ->setParameter('search', '%' . $search . '%');
        }

        $status = (string) $request->query->get('status', '');
        if ($status !== '' && $status !== 'all') {
            $qb->andWhere('e.status = :status')
                ->setParameter('status', ucfirst($status));
        }

        $sort = (string) $request->query->get('sort', 'id_desc');
        switch ($sort) {
            case 'title_asc':
                $qb->orderBy('e.title', 'ASC');
                break;
            case 'title_desc':
                $qb->orderBy('e.title', 'DESC');
                break;
            case 'date_asc':
                $qb->orderBy('e.date_event', 'ASC');
                break;
            case 'date_desc':
                $qb->orderBy('e.date_event', 'DESC');
                break;
            default:
                $qb->orderBy('e.id_event', 'DESC');
                break;
        }

        $events = $qb->getQuery()->getResult();

        return $this->render('back/event.html.twig', [
            'mode' => 'list',
            'events' => $events,
            'current_search' => $search,
            'current_status' => $status,
            'current_sort' => $sort,
        ]);
    }

    #[Route('/admin/events/new', name: 'event_new')]
    #[IsGranted('ROLE_ADMIN')]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $event = new Event();

        if ($request->isMethod('POST')) {
            $this->handleForm($request, $event);

            $imageEvent = (string) $request->request->get('image_event', '');

            if ($imageEvent !== '') {
                $event->setImage_event($imageEvent);
            }

            $em->persist($event);
            $em->flush();

            $this->addFlash('success', 'Event created successfully!');

            return $this->redirectToRoute('event_index');
        }

        return $this->render('back/event.html.twig', [
            'mode' => 'new',
            'event' => $event,
        ]);
    }

    #[Route('/admin/events/{id_event}', name: 'event_show')]
    #[IsGranted('ROLE_ADMIN')]
    public function show(int $id_event, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);

        if (!$event instanceof Event) {
            throw $this->createNotFoundException('Event not found');
        }

        return $this->render('back/event.html.twig', [
            'mode' => 'show',
            'event' => $event,
        ]);
    }

    #[Route('/admin/events/{id_event}/edit', name: 'event_edit')]
    #[IsGranted('ROLE_ADMIN')]
    public function edit(int $id_event, Request $request, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);

        if (!$event instanceof Event) {
            throw $this->createNotFoundException('Event not found');
        }

        if ($request->isMethod('POST')) {
            $this->handleForm($request, $event);

            $newImage = (string) $request->request->get('image_event', '');

            if ($newImage !== '' && $newImage !== $event->getImage_event()) {
                $oldImage = $event->getImage_event();
                $imagesDirectory = $this->getImagesDirectory();

                if ($oldImage !== '') {
                    $oldImagePath = $imagesDirectory . '/' . $oldImage;

                    if (file_exists($oldImagePath)) {
                        unlink($oldImagePath);
                    }
                }

                $event->setImage_event($newImage);
            }

            $em->flush();

            $this->addFlash('success', 'Event updated successfully!');

            return $this->redirectToRoute('event_index');
        }

        return $this->render('back/event.html.twig', [
            'mode' => 'edit',
            'event' => $event,
        ]);
    }

    #[Route('/admin/events/{id_event}/delete', name: 'event_delete')]
    #[IsGranted('ROLE_ADMIN')]
    public function delete(int $id_event, EntityManagerInterface $em): Response
    {
        $event = $em->getRepository(Event::class)->find($id_event);

        if (!$event instanceof Event) {
            throw $this->createNotFoundException('Event not found');
        }

        $em->remove($event);
        $em->flush();

        $this->addFlash('success', 'Event deleted successfully!');

        return $this->redirectToRoute('event_index');
    }

    private function handleForm(Request $request, Event $event): void
    {
        $title = (string) $request->request->get('title', '');
        $description = (string) $request->request->get('description', '');
        $dateEvent = (string) $request->request->get('date_event', 'now');
        $startTime = (string) $request->request->get('start_time', '');
        $location = (string) $request->request->get('location', '');
        $eventType = (string) $request->request->get('event_type', '');
        $season = (string) $request->request->get('season', '');
        $status = (string) $request->request->get('status', '');

        $event->setTitle($title);
        $event->setDescription($description);
        $event->setDate_event(new \DateTime($dateEvent));
        $event->setStart_time($startTime);
        $event->setLocation($location);
        $event->setEvent_type($eventType);
        $event->setSeason($season);
        $event->setCapacity((int) $request->request->get('capacity', 0));
        $event->setAvailable_places((int) $request->request->get('available_places', 0));
        $event->setStatus($status);
        $event->setPrice((float) $request->request->get('price', 0));
    }

    private function getImagesDirectory(): string
    {
        $imagesDirectory = $this->getParameter('images_directory');

        if (!is_string($imagesDirectory)) {
            throw new \RuntimeException('Parameter images_directory must be a string.');
        }

        return $imagesDirectory;
    }

    private function getCurrentUtilisateur(): Utilisateur
    {
        $user = $this->getUser();

        if (!$user instanceof Utilisateur) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        return $user;
    }

    private function getCurrentUtilisateurId(Utilisateur $user): int
    {
        $userId = $user->getId();

        if ($userId === null) {
            throw $this->createAccessDeniedException('Utilisateur invalide.');
        }

        return $userId;
    }
}