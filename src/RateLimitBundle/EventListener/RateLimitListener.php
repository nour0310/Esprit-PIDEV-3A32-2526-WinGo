<?php

namespace RateLimit\RateLimitBundle\EventListener;

use RateLimit\RateLimitBundle\Attribute\RateLimit;
use RateLimit\RateLimitBundle\Exception\RateLimitExceededException;
use Symfony\Component\EventDispatcher\EventSubscriberInterface;
use Symfony\Component\HttpKernel\Event\ControllerEvent;
use Symfony\Component\HttpKernel\KernelEvents;
use Symfony\Component\RateLimiter\RateLimiterFactory;
use Symfony\Component\DependencyInjection\ContainerInterface;
use Symfony\Component\HttpFoundation\RequestStack;

class RateLimitListener implements EventSubscriberInterface
{
    public function __construct(
        private ContainerInterface $container,
        private RequestStack $requestStack
    ) {}

    public static function getSubscribedEvents(): array
    {
        return [
            KernelEvents::CONTROLLER => 'onKernelController',
        ];
    }

    public function onKernelController(ControllerEvent $event): void
    {
        $controller = $event->getController();

        if (is_array($controller)) {
            $object = new \ReflectionObject($controller[0]);
            $method = $object->getMethod($controller[1]);
        } else {
            return;
        }

        $attribute = $method->getAttributes(RateLimit::class)[0] ?? null;

        if (!$attribute) {
            return;
        }

        /** @var RateLimit $rateLimit */
        $rateLimit = $attribute->newInstance();

        // We use the "comment_submit" limiter from our config
        // Or we could create one dynamically if we had the factory.
        // But the tutorial implies a specific config.
        
        try {
            /** @var RateLimiterFactory $limiterFactory */
            $limiterFactory = $this->container->get('limiter.comment_submit');
            
            $request = $this->requestStack->getCurrentRequest();
            $identifier = $request->getClientIp(); // default to IP

            if ($rateLimit->identifier === 'user') {
                $user = $this->container->get('security.helper')->getUser();
                $identifier = $user ? (string) $user->getUserIdentifier() : $request->getClientIp();
            }

            $limiter = $limiterFactory->create($identifier);

            if (false === $limiter->consume(1)->isAccepted()) {
                throw new RateLimitExceededException();
            }
        } catch (\Exception $e) {
            // Rethrow our specific exception if it's the one we want
            if ($e instanceof RateLimitExceededException) {
                throw $e;
            }
            // Otherwise, we might want to log or ignore configuration errors
            // For now, if the limiter is not configured, we just let it pass or log it.
        }
    }
}
