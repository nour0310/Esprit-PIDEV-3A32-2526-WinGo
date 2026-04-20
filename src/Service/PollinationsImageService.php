<?php

namespace App\Service;

use Symfony\Component\DependencyInjection\ParameterBag\ParameterBagInterface;
use Psr\Log\LoggerInterface;

class PollinationsImageService
{
    private string $imageDirectory;
    private LoggerInterface $logger;
    private string $baseUrl = 'https://image.pollinations.ai/prompt';

    public function __construct(
        ParameterBagInterface $params,
        LoggerInterface $logger
    ) {
        $this->imageDirectory = $params->get('images_directory');
        $this->logger = $logger;
    }

    /**
     * Generate an image from a text prompt using Pollinations.AI
     * 
     * @param string $prompt The image description
     * @param int $width Image width (default: 1024)
     * @param int $height Image height (default: 1024)
     * @param string $model Model to use: 'flux', 'turbo', or 'any' (default: 'flux')
     * @return string The generated image filename
     * @throws \RuntimeException
     */
    public function generateImage(string $prompt, int $width = 1024, int $height = 1024, string $model = 'flux'): string
    {
        // URL encode the prompt
        $encodedPrompt = urlencode($prompt);
        
        // Build the URL with parameters
        $url = $this->baseUrl . '/' . $encodedPrompt;
        $url .= "?width={$width}&height={$height}&nologo=true";
        
        // Add model parameter (if not 'any')
        if ($model !== 'any') {
            $url .= "&model={$model}";
        }
        
        try {
            // Download the image
            $imageData = @file_get_contents($url);
            
            if ($imageData === false) {
                throw new \RuntimeException('Failed to download image from Pollinations.AI');
            }
            
            // Check if we got an HTML error page instead of an image
            if (strlen($imageData) < 100 && strpos($imageData, '<!DOCTYPE') !== false) {
                throw new \RuntimeException('Pollinations.AI returned an error page');
            }
            
            // Generate unique filename
            $safePrompt = preg_replace('/[^a-z0-9]+/i', '_', $prompt);
            $safePrompt = substr($safePrompt, 0, 50);
            $filename = $safePrompt . '_' . uniqid() . '.png';
            $filePath = $this->imageDirectory . '/' . $filename;
            
            // Save the image
            file_put_contents($filePath, $imageData);
            
            return $filename;
            
        } catch (\Exception $e) {
            $this->logger->error('Pollinations API error: ' . $e->getMessage());
            throw new \RuntimeException('Image generation failed: ' . $e->getMessage());
        }
    }
}