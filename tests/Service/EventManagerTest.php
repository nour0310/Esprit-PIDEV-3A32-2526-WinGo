<?php

namespace App\Tests\Service;

use App\Entity\Event;
use App\Service\EventManager;
use PHPUnit\Framework\TestCase;

class EventManagerTest extends TestCase
{
    /**
     * Test 1 : Un événement valide passe la validation
     */
    public function testValidEvent(): void
    {
        $event = new Event();
        $event->setTitle('Concert de Jazz');
        $event->setDescription('Un super concert');
        $event->setCapacity(100);
        $event->setAvailable_places(50);
        $event->setPrice(25.0);
        $event->setLocation('Paris');
        $event->setEvent_type('concert');
        $event->setSeason('été');
        $event->setStatus('actif');
        $event->setImage_event('image.jpg');
        $event->setDate_event(new \DateTime('+1 month'));
        $event->setStart_time('20:00');

        $manager = new EventManager();
        $this->assertTrue($manager->validate($event));
    }

    /**
     * Test 2 : Un événement sans titre est rejeté
     */
    public function testEventWithoutTitle(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le titre est obligatoire');

        $event = new Event();
        $event->setTitle('');
        $event->setCapacity(100);
        $event->setAvailable_places(50);
        $event->setPrice(25.0);

        $manager = new EventManager();
        $manager->validate($event);
    }

    /**
     * Test 3 : Un événement avec titre trop court est rejeté
     */
    public function testEventWithShortTitle(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le titre doit contenir au moins 3 caractères');

        $event = new Event();
        $event->setTitle('AB');
        $event->setCapacity(100);
        $event->setAvailable_places(50);
        $event->setPrice(25.0);

        $manager = new EventManager();
        $manager->validate($event);
    }

    /**
     * Test 4 : Un événement avec capacité négative est rejeté
     */
    public function testEventWithNegativeCapacity(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('La capacité doit être positive');

        $event = new Event();
        $event->setTitle('Concert Test');
        $event->setCapacity(-10);
        $event->setAvailable_places(0);
        $event->setPrice(25.0);

        $manager = new EventManager();
        $manager->validate($event);
    }

    /**
     * Test 5 : Un événement avec prix négatif est rejeté
     */
    public function testEventWithNegativePrice(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le prix doit être positif ou nul');

        $event = new Event();
        $event->setTitle('Concert Test');
        $event->setCapacity(100);
        $event->setAvailable_places(50);
        $event->setPrice(-5.0);

        $manager = new EventManager();
        $manager->validate($event);
    }

    /**
     * Test 6 : Les places disponibles ne dépassent pas la capacité
     */
    public function testEventWithTooManyAvailablePlaces(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Les places disponibles ne peuvent pas dépasser la capacité');

        $event = new Event();
        $event->setTitle('Concert Test');
        $event->setCapacity(50);
        $event->setAvailable_places(100);
        $event->setPrice(25.0);

        $manager = new EventManager();
        $manager->validate($event);
    }
}
