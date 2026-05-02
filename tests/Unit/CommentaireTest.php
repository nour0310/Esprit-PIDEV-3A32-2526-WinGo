<?php

namespace App\Tests\Unit;

use App\Entity\Commentaire;
use PHPUnit\Framework\TestCase;

class CommentaireTest extends TestCase
{
    public function testCommentaireInstantiation(): void
    {
        $commentaire = new Commentaire();
        $this->assertInstanceOf(Commentaire::class, $commentaire);
    }

    public function testContenuGetterAndSetter(): void
    {
        $commentaire = new Commentaire();
        $contenu = "Ceci est un super commentaire";
        $commentaire->setContenu($contenu);
        $this->assertEquals($contenu, $commentaire->getContenu());
    }
}
