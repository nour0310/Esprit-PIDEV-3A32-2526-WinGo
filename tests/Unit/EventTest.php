<?php

namespace App\Tests\Unit;

use App\Entity\Event;
use PHPUnit\Framework\TestCase;

class EventTest extends TestCase
{
    public function testEventInstantiation(): void
    {
        $event = new Event();
        $this->assertInstanceOf(Event::class, $event);
    }

    public function testTitleGetterAndSetter(): void
    {
        $event = new Event();
        $title = "Festival de Carthage";
        $event->setTitle($title);
        $this->assertEquals($title, $event->getTitle());
    }

    public function testPriceGetterAndSetter(): void
    {
        $event = new Event();
        $price = 45.5;
        $event->setPrice($price);
        $this->assertEquals($price, $event->getPrice());
    }
}
