<?php

namespace App\Controller;

use App\Entity\Event;
use App\Entity\Participation;
use App\Entity\Utilisateur;
use App\Service\PollinationsImageService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Knp\Component\Pager\PaginatorInterface;
use App\Service\WeatherEventService;
use App\Service\RecommendationEventService;
use App\Service\DiscountEventService;

class EventController extends AbstractController
{
    // FRONT — public events listing with pagination (5 per page) + weather + recommendations + discount
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

        // Search filter
        $search = $request->query->get('search', '');
        if (!empty($search)) {
            $qb->andWhere('e.title LIKE :search OR e.description LIKE :search')
               ->setParameter('search', '%' . $search . '%');
        }

        // Type filter
        $type = $request->query->get('type', '');
        if (!empty($type) && $type !== 'all') {
            $qb->andWhere('e.event_type = :type')
               ->setParameter('type', $type);
        }

        // Sorting
        $sort = $request->query->get('sort', 'date_asc');
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

        // Pagination: 5 events per page
        $pagination = $paginator->paginate($qb, $request->query->getInt('page', 1), 5);
        $events = $pagination->getItems();

        // Attach weather data and discount data to each event
        foreach ($events as $event) {
            $event->weatherData = $weatherEventService->getWeatherForEvent($event->getLocation());
            $event->discountedPrice = $discountEventService->getDiscountedPrice($event);
            $event->discountActive = $discountEventService->isDiscountActive($event);
        }

        // Already registered events
        $user = $this->getUser();
        $registeredEventIds = [];
        if ($user) {
            $participations = $em->getRepository(Participation::class)
                ->createQueryBuilder('p')
                ->select('IDENTITY(p.id_event) as eventId')
                ->where('p.id_user = :userId')
                ->andWhere('p.statut = :status')
                ->setParameter('userId', $user->getId())
                ->setParameter('status', 'confirmed')
                ->getQuery()
                ->getScalarResult();
            $registeredEventIds = array_column($participations, 'eventId');
        }

        // Flag passed events
        $today = new \DateTime();
        $today->setTime(0, 0, 0);
        foreach ($events as $event) {
            $eventDate = $event->getDate_event();
            $eventDate->setTime(0, 0, 0);
            $event->setIsPassed($eventDate < $today);
        }

        // Recommendations (popular + next 7 days)
        $recommendedEvents = $recommendationEventService->getRecommendedEvents(6);
        // Attach weather and discount to recommended events as well
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

        if ($user) {
            $participation = $em->getRepository(Participation::class)->findOneBy([
                'id_event' => $event,
                'id_user' => $user->getId(),
                'statut' => 'confirmed'
            ]);
            $userParticipated = $participation !== null;
            $alreadyLeft = $event->hasUserFeedback($user->getId());
        }

        return $this->render('front/event_show.html.twig', [
            'event' => $event,
            'userParticipated' => $userParticipated,
            'alreadyLeft' => $alreadyLeft,
        ]);
    }

    // ========== NEW: List participants for an event (chat access) ==========
    #[Route('/event/{id}/participants', name: 'event_participants')]
    public function participants(Event $event, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        if (!$user) {
            $this->addFlash('error', 'You must be logged in to view participants.');
            return $this->redirectToRoute('app_login');
        }

        // Check if current user is a confirmed participant
        $participation = $em->getRepository(Participation::class)->findOneBy([
            'id_event' => $event,
            'id_user' => $user->getId(),
            'statut' => 'confirmed'
        ]);

        if (!$participation) {
            $this->addFlash('error', 'You must be a participant to view the participant list.');
            return $this->redirectToRoute('event_show_front', ['id' => $event->getId_event()]);
        }

        // Get all confirmed participants with user details
        $participations = $em->getRepository(Participation::class)->createQueryBuilder('p')
            ->where('p.id_event = :event')
            ->andWhere('p.statut = :status')
            ->setParameter('event', $event)
            ->setParameter('status', 'confirmed')
            ->getQuery()
            ->getResult();

        // Extract user objects from participations
        $participants = [];
        $userRepo = $em->getRepository(Utilisateur::class);
        foreach ($participations as $p) {
            // Try to get User via relation if exists; otherwise fetch by id_user
            $participantUser = null;
            if (method_exists($p, 'getUser') && $p->getUser() !== null) {
                $participantUser = $p->getUser();
            } else {
                $participantUser = $userRepo->find($p->getId_user());
            }
            if ($participantUser) {
                $participants[] = $participantUser;
            }
        }

        return $this->render('front/event_participants.html.twig', [
            'event' => $event,
            'participants' => $participants,
        ]);
    }

    // ---------- BACKOFFICE (admin) with AI image generation ----------
    #[Route('/admin/events/generate-image', name: 'event_generate_image', methods: ['POST'])]
    #[IsGranted('ROLE_ADMIN')]
    public function generateImage(Request $request, PollinationsImageService $imageService): Response
    {
        $prompt = $request->request->get('prompt');
        if (empty($prompt)) {
            return $this->json(['error' => 'Prompt is required'], 400);
        }
        
        // Optional: Get width/height/model from request
        $width = (int) $request->request->get('width', 1024);
        $height = (int) $request->request->get('height', 1024);
        $model = $request->request->get('model', 'flux');
        
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

        $search = $request->query->get('search', '');
        if (!empty($search)) {
            $qb->andWhere('e.title LIKE :search OR e.location LIKE :search')
               ->setParameter('search', '%' . $search . '%');
        }

        $status = $request->query->get('status', '');
        if (!empty($status) && $status !== 'all') {
            $qb->andWhere('e.status = :status')
               ->setParameter('status', ucfirst($status));
        }

        $sort = $request->query->get('sort', 'id_desc');
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

            // Get AI-generated image filename from hidden field
            $imageEvent = $request->request->get('image_event');
            if ($imageEvent) {
                $event->setImage_event($imageEvent);
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

            // Get AI-generated image filename from hidden field
            $newImage = $request->request->get('image_event');
            if ($newImage && $newImage !== $event->getImage_event()) {
                // Delete old image file
                $oldImage = $event->getImage_event();
                if ($oldImage && file_exists($this->getParameter('images_directory') . '/' . $oldImage)) {
                    unlink($this->getParameter('images_directory') . '/' . $oldImage);
                }
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

    /**
     * @deprecated No longer used – kept only for reference.
     */
    private function uploadImage(Request $request, ?string $oldImage = null): ?string
    {
        $file = $request->files->get('image_file');
        if (!$file) {
            return $oldImage;
        }

        $originalName = pathinfo($file->getClientOriginalName(), PATHINFO_FILENAME);
        $safeName = transliterator_transliterate('Any-Latin; Latin-ASCII; [^A-Za-z0-9_] remove; Lower()', $originalName);
        $newFilename = $safeName . '_' . uniqid() . '.' . $file->guessExtension();

        try {
            $file->move($this->getParameter('images_directory'), $newFilename);
        } catch (\Exception $e) {
            $this->addFlash('error', 'Could not upload image');
            return $oldImage;
        }

        if ($oldImage && file_exists($this->getParameter('images_directory') . '/' . $oldImage)) {
            unlink($this->getParameter('images_directory') . '/' . $oldImage);
        }

        return $newFilename;
    }
}