<?php

namespace App\Exception;

final class ArticleGenerationException extends \RuntimeException
{
    public function __construct(
        private readonly string $publicMessage,
        private readonly ?string $detail = null,
        private readonly int $statusCode = 502,
        ?\Throwable $previous = null,
    ) {
        parent::__construct($publicMessage, 0, $previous);
    }

    public function getPublicMessage(): string
    {
        return $this->publicMessage;
    }

    public function getDetail(): ?string
    {
        return $this->detail;
    }

    public function getStatusCode(): int
    {
        return $this->statusCode;
    }
}

