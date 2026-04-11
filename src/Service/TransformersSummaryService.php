<?php

namespace App\Service;

use Codewithkyrian\Transformers\Transformers;
use Codewithkyrian\Transformers\Utils\ImageDriver;

use function Codewithkyrian\Transformers\Pipelines\pipeline;

class TransformersSummaryService
{
    private bool $initialized = false;

    /** @var object|null pipeline instance (SummarizationPipeline) */
    private mixed $summarizer = null;

    public function __construct(
        private readonly string $projectDir,
        private readonly string $summaryModel = 'Xenova/distilbart-cnn-6-6'
    ) {
    }

    public function initialize(): void
    {
        if ($this->initialized) {
            return;
        }

        if (!\extension_loaded('ffi')) {
            return;
        }

        $cacheDir = $this->projectDir . \DIRECTORY_SEPARATOR . 'var' . \DIRECTORY_SEPARATOR . 'transformers-cache';
        if (!is_dir($cacheDir)) {
            @mkdir($cacheDir, 0775, true);
        }

        Transformers::setup()
            ->setCacheDir($cacheDir)
            ->setImageDriver(ImageDriver::GD)
            ->apply();

        $this->initialized = true;
    }

    public function isAvailable(): bool
    {
        return \extension_loaded('ffi');
    }

    public function summarize(string $text, int $maxLength = 150, int $minLength = 30): ?string
    {
        $text = $this->normalizeText($text);
        if ($text === '') {
            return null;
        }

        if (mb_strlen($text) < 80) {
            return null;
        }

        $text = mb_substr($text, 0, 6000);

        try {
            $this->initialize();

            if ($this->summarizer === null) {
                $this->summarizer = pipeline('summarization', $this->summaryModel);
            }

            $result = ($this->summarizer)(
                $text,
                maxNewTokens: $maxLength,
                minNewTokens: min($minLength, $maxLength - 1),
                doSample: false
            );

            $summary = $result[0]['summary_text'] ?? null;

            return \is_string($summary) && $summary !== '' ? trim($summary) : null;
        } catch (\Throwable) {
            return null;
        }
    }

    private function normalizeText(string $text): string
    {
        $text = strip_tags($text);
        $text = html_entity_decode($text, \ENT_QUOTES | \ENT_HTML5, 'UTF-8');
        $text = preg_replace('/\s+/u', ' ', $text) ?? '';

        return trim($text);
    }
}
