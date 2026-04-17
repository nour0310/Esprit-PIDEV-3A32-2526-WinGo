<?php

namespace RateLimit\RateLimitBundle\DependencyInjection;

use Symfony\Component\DependencyInjection\ContainerBuilder;
use Symfony\Component\DependencyInjection\Extension\Extension;

class RateLimitExtension extends Extension
{
    public function load(array $configs, ContainerBuilder $container): void
    {
        // We just absorb the config to avoid "no extension able to load" error.
        // The actual logic is handled by our listener and official rate_limiter.yaml.
    }

    public function getAlias(): string
    {
        return 'rate_limit';
    }
}
