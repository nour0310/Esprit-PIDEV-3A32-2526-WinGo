<?php

namespace App\Controller;

use App\Service\DeepLTranslationService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class TranslationController extends AbstractController
{
    #[Route('/translation', name: 'app_translation')]
    public function index(Request $request, DeepLTranslationService $translationService): Response
    {
        $error = null;
        $result = null;

        $formData = [
            'text' => (string) $request->request->get('text', ''),
            'target_lang' => (string) $request->request->get('target_lang', 'EN'),
            'source_lang' => (string) $request->request->get('source_lang', ''),
        ];

        if ($request->isMethod('POST')) {
            try {
                $result = $translationService->translate(
                    $formData['text'],
                    $formData['target_lang'],
                    $formData['source_lang'] !== '' ? $formData['source_lang'] : null
                );
            } catch (\Throwable $exception) {
                $error = $exception->getMessage();
            }
        }

        return $this->render('translation/index.html.twig', [
            'error' => $error,
            'result' => $result,
            'form_data' => $formData,
            'target_languages' => $translationService->getSupportedTargets(),
        ]);
    }
}
