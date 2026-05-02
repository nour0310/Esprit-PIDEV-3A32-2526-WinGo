<?php

namespace App\EventSubscriber;

use App\Repository\ReservationRepository;
use CalendarBundle\Entity\Event;
use CalendarBundle\Event\CalendarEvent;
use Symfony\Component\EventDispatcher\EventSubscriberInterface;
use Symfony\Bundle\SecurityBundle\Security;

class CalendarSubscriber implements EventSubscriberInterface
{
    public function __construct(
        private ReservationRepository $reservationRepo,
        private Security $security
    ) {}

    public static function getSubscribedEvents()
    {
        return [
            CalendarEvent::class => 'onCalendarSetData',
            'calendar.set_data' => 'onCalendarSetData',
        ];
    }

    public function onCalendarSetData(CalendarEvent $calendar): void
    {
        $start = $calendar->getStart();
        $end = $calendar->getEnd();

        $user = $this->security->getUser();

        $queryBuilder = $this->reservationRepo->createQueryBuilder('r')
            ->where('r.date >= :start')
            ->andWhere('r.date <= :end');

        if ($user) {
            $queryBuilder->andWhere('r.user_id = :user')
                ->setParameter('user', $user);
        }

        $reservations = $queryBuilder
            ->setParameter('start', $start->format('Y-m-d H:i:s'))
            ->setParameter('end', $end->format('Y-m-d H:i:s'))
            ->getQuery()
            ->getResult();

        foreach ($reservations as $res) {
            $event = new Event(
                $res->getExp(),
                $res->getDate()
            );

            $event->setOptions([
                'backgroundColor' => '#fa9e1b',
                'borderColor' => '#fa9e1b',
                'textColor' => '#ffffff',
                'extendedProps' => [
                    'icon' => 'fa-suitcase',
                    'description' => 'Statut: ' . $res->getStatut(),
                ],
            ]);

            $calendar->addEvent($event);
        }
    }
}