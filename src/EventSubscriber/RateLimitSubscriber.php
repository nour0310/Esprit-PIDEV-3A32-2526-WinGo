<?php

namespace App\EventSubscriber;

use RateLimit\RateLimitBundle\Exception\RateLimitExceededException;
use Symfony\Component\EventDispatcher\EventSubscriberInterface;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpKernel\Event\ExceptionEvent;
use Symfony\Component\HttpKernel\KernelEvents;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;

class RateLimitSubscriber implements EventSubscriberInterface
{
    public function __construct(
        private readonly UrlGeneratorInterface $urlGenerator
    ) {}

    public static function getSubscribedEvents(): array
    {
        return [
            KernelEvents::EXCEPTION => ['onKernelException', 10],
        ];
    }

    public function onKernelException(ExceptionEvent $event): void
    {
        $exception = $event->getThrowable();

        if ($exception instanceof RateLimitExceededException) {
            $request = $event->getRequest();
            
            // Récupérer l'ID de l'article depuis la route
            $articleId = $request->attributes->get('id');

            // Si c'est une requête AJAX (votre formulaire de commentaire est en POST normal)
            if ($request->isXmlHttpRequest()) {
                $response = new JsonResponse([
                    'error' => 'Trop de commentaires. Veuillez patienter une minute.',
                ], Response::HTTP_TOO_MANY_REQUESTS);
            } else {
                // Redirection avec message flash
                $this->addFlashToSession($request, 'error', 'Vous avez dépassé la limite de 5 commentaires par minute.');
                
                // Note: since our ArticleController::show expects article-{slug}, 
                // generating by 'id' only might fail if slug is not provided.
                // However, the original controller code handles redirection if slug is missing.
                $redirectUrl = $this->urlGenerator->generate('app_article_show', ['id' => $articleId, 'slug' => 'redirect']);
                $response = new RedirectResponse($redirectUrl);
            }

            $event->setResponse($response);
        }
    }

    // Méthode utilitaire pour ajouter un flash en dehors d'un contrôleur
    private function addFlashToSession(Request $request, string $type, string $message): void
    {
        $session = $request->getSession();
        $flashBag = $session->getFlashBag();
        $flashBag->add($type, $message);
    }
}
