<?php
namespace App\Controller;

use App\Form\ArticleType;
use App\Entity\Article;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class TestController extends AbstractController
{
    #[Route('/test-quill', name: 'test_quill')]
    public function test(): Response
    {
        $article = new Article();
        $form = $this->createForm(ArticleType::class, $article);
        
        $view = $form->createView();
        
        $html = $this->renderView('test_quill.html.twig', [
            'form' => $view,
        ]);
        
        return new Response($html);
    }
}
