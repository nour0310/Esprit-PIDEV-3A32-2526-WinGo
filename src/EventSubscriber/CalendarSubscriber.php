<?php
namespace App\EventSubscriber;

use App\Repository\ReservationRepository;
use App\Repository\TransportRepository; 
use CalendarBundle\Entity\Event;
use CalendarBundle\Event\CalendarEvent;
use Symfony\Component\EventDispatcher\EventSubscriberInterface;
use Symfony\Bundle\SecurityBundle\Security; 
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;

class CalendarSubscriber implements EventSubscriberInterface
{
    public function __construct(
        private ReservationRepository $reservationRepo,
        private TransportRepository $transportRepo,
        private UrlGeneratorInterface $router,
        private Security $security
    ) {}

    public static function getSubscribedEvents()
{
    return [
        CalendarEvent::class => 'onCalendarSetData',
        'calendar.set_data' => 'onCalendarSetData', // Add this fallback!
    ];
}

    public function onCalendarSetData(CalendarEvent $calendar)
    {
        $start = $calendar->getStart();
        $end = $calendar->getEnd();
        
        // 1. Get the currently logged-in user
        $user = $this->security->getUser();

        // 2. Build the query
        $queryBuilder = $this->reservationRepo->createQueryBuilder('r')
            ->where('r.date >= :start')
            ->andWhere('r.date <= :end');

        // Optional but recommended: Only show reservations for the connected user
        if ($user) {
            $queryBuilder->andWhere('r.user_id = :user')
                         ->setParameter('user', $user);
        }

        // 3. Format dates to strings to guarantee Doctrine matches them in the DB
        $reservations = $queryBuilder
            ->setParameter('start', $start->format('Y-m-d H:i:s'))
            ->setParameter('end', $end->format('Y-m-d H:i:s'))
            ->getQuery()
            ->getResult();

        // TEMPORARY DEBUG: Uncomment this line to check if the query finds anything!
        //dd($reservations);

        foreach ($reservations as $res) {
            $event = new Event(
                 $res->getExp(), 
                $res->getDate()
            );

            // 4. Send data to your FullCalendar & Tippy tooltips!
            $event->setOptions([
                'backgroundColor' => '#fa9e1b', // Matches your site's orange theme
                'borderColor' => '#fa9e1b',
                'textColor' => '#ffffff',
                'extendedProps' => [
                    'icon' => 'fa-suitcase', // Shows an icon in your tippy hover
                    'description' => 'Statut: ' . $res->getStatut() 
                ]
            ]);

            $calendar->addEvent($event);
        }
    }
}