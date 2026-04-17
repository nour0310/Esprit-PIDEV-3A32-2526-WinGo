<?php

namespace RateLimit\RateLimitBundle\Attribute;

use Attribute;

#[Attribute(Attribute::TARGET_METHOD)]
class RateLimit
{
    public function __construct(
        public int $limit,
        public int $period,
        public string $identifier = 'ip'
    ) {}
}
