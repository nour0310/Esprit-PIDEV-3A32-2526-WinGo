<?php

namespace App\Controller\Api;

use App\DTO\ProductAiInput;
use App\Service\ProductAiService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/produit/ai', name: 'produit_ai_')]
class ProductAiController extends AbstractController
{
    public function __construct(
        private readonly ProductAiService $productAiService,
    ) {}

    #[Route('/generate', name: 'generate', methods: ['POST'])]
    public function generate(Request $request): JsonResponse
    {
        // Accepte JSON ou form-data
        $data = json_decode($request->getContent(), true)
            ?? $request->request->all();

        $nom       = trim($data['nom']       ?? '');
        $categorie = trim($data['categorie'] ?? '');
        $region    = trim($data['region']    ?? '');
        $prix      = $data['prix']            ?? '';
        $stock     = $data['stock']           ?? '';

        if ($nom === '') {
            return $this->json([
                'success' => false,
                'error'   => 'Le nom du produit est obligatoire.',
            ], JsonResponse::HTTP_BAD_REQUEST);
        }

        $input  = new ProductAiInput($nom, $categorie, $region, $prix, $stock);
        $result = $this->productAiService->generate($input);

        $status = $result['success']
            ? JsonResponse::HTTP_OK
            : JsonResponse::HTTP_BAD_GATEWAY;

        return $this->json($result, $status);
    }
}