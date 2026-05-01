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
        $request = $this->requestStack->getCurrentRequest();
        
        // Only trigger rate limiting for POST requests (comment submissions)
        if ($request->getMethod() !== 'POST') {
            return;
        }

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

        try {
            // Use the public alias we created in services.yaml
            if (!$this->container->has('limiter.comment_submit.public')) {
                error_log('RateLimitListener Error: service limiter.comment_submit.public not found.');
                return;
            }

            /** @var RateLimiterFactory $limiterFactory */
            $limiterFactory = $this->container->get('limiter.comment_submit.public');
            
            $identifier = $request->getClientIp(); // default to IP

            if ($rateLimit->identifier === 'user') {
                $user = $this->container->get('security.helper')->getUser();
                $identifier = $user ? (string) $user->getUserIdentifier() : $request->getClientIp();
            }

            $limiter = $limiterFactory->create($identifier);

            if (false === $limiter->consume(1)->isAccepted()) {
                throw new RateLimitExceededException();
            }
        } catch (RateLimitExceededException $e) {
            throw $e;
        } catch (\Exception $e) {
            error_log('RateLimitListener Exception: ' . $e->getMessage());
        }
    }
}
