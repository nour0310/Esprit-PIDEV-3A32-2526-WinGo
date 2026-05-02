<?php

namespace App\Tests\Unit;

use App\Entity\Reservation;
use PHPUnit\Framework\TestCase;

class ReservationTest extends TestCase
{
    public function testReservationInstantiation(): void
    {
        $reservation = new Reservation();
        $this->assertInstanceOf(Reservation::class, $reservation);
    }

    public function testStatutGetterAndSetter(): void
    {
        $reservation = new Reservation();
        $statut = "Confirmée";
        $reservation->setStatut($statut);
        $this->assertEquals($statut, $reservation->getStatut());
    }
}
