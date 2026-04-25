<?php

namespace App\Service;

use App\Entity\Event;

class DiscountEventService
{
    private const DISCOUNT_PERCENT = 20; // 20%
    private const DAYS_THRESHOLD = 4;    // 4 jours ou moins

    /**
     * Retourne le prix réduit si applicable, sinon le prix original.
     */
    public function getDiscountedPrice(Event $event): float
    {
        $daysUntil = $this->getDaysUntilEvent($event);
        if ($daysUntil >= 0 && $daysUntil <= self::DAYS_THRESHOLD) {
            return $event->getPrice() * (1 - self::DISCOUNT_PERCENT / 100);
        }
        return $event->getPrice();
    }

    /**
     * Indique si une réduction est active pour cet événement.
     */
    public function isDiscountActive(Event $event): bool
    {
        $daysUntil = $this->getDaysUntilEvent($event);
        return $daysUntil >= 0 && $daysUntil <= self::DAYS_THRESHOLD;
    }

    /**
     * Retourne le pourcentage de réduction.
     */
    public function getDiscountPercent(): int
    {
        return self::DISCOUNT_PERCENT;
    }

    private function getDaysUntilEvent(Event $event): int
    {
        $today = new \DateTime();
        $today->setTime(0, 0, 0);
        $eventDate = clone $event->getDate_event();
        $eventDate->setTime(0, 0, 0);
        $interval = $today->diff($eventDate);
        return (int) $interval->format('%r%a'); // nombre de jours (négatif si passé)
    }
}