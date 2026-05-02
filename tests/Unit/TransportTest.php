<?php

namespace App\Tests\Unit;

use App\Entity\Transport;
use PHPUnit\Framework\TestCase;

class TransportTest extends TestCase
{
    public function testTransportInstantiation(): void
    {
        $transport = new Transport();
        $this->assertInstanceOf(Transport::class, $transport);
    }

    public function testTypeGetterAndSetter(): void
    {
        $transport = new Transport();
        $type = "Bus";
        $transport->setType($type);
        $this->assertEquals($type, $transport->getType());
    }
}
