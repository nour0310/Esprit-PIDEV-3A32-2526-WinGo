<?php

namespace App\Tests\Service;

use App\Entity\Reservation;
use App\Service\ReservationManager;
use PHPUnit\Framework\TestCase;

class ReservationManagerTest extends TestCase
{
    /**
     * Test 1 : Une réservation valide passe la validation
     */
    public function testValidReservation(): void
    {
        $reservation = new Reservation();
        $reservation->setUser('Jean Dupont');
        $reservation->setDate(new \DateTime('+1 week'));
        $reservation->setPrice(100);
        $reservation->setExp('Tunis - Paris');
        $reservation->setStatut('confirmee');

        $manager = new ReservationManager();
        $this->assertTrue($manager->validate($reservation));
    }

    /**
     * Test 2 : Une réservation sans utilisateur est rejetée
     */
    public function testReservationWithoutUser(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le nom d\'utilisateur est obligatoire');

        $reservation = new Reservation();
        $reservation->setUser('');
        $reservation->setDate(new \DateTime('+1 week'));
        $reservation->setPrice(100);

        $manager = new ReservationManager();
        $manager->validate($reservation);
    }

    /**
     * Test 3 : Une réservation avec date passée est rejetée
     */
    public function testReservationWithPastDate(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('La date de réservation ne peut pas être dans le passé');

        $reservation = new Reservation();
        $reservation->setUser('Jean Dupont');
        $reservation->setDate(new \DateTime('-1 month'));
        $reservation->setPrice(100);

        $manager = new ReservationManager();
        $manager->validate($reservation);
    }

    /**
     * Test 4 : Une réservation avec prix négatif est rejetée
     */
    public function testReservationWithNegativePrice(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le prix ne peut pas être négatif');

        $reservation = new Reservation();
        $reservation->setUser('Jean Dupont');
        $reservation->setDate(new \DateTime('+1 week'));
        $reservation->setPrice(-50);

        $manager = new ReservationManager();
        $manager->validate($reservation);
    }
}
