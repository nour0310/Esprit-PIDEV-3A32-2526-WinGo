<?php

namespace App\Controller;

use App\Service\GiphyService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class GiphyController extends AbstractController
{
    #[Route('/comment/gif-search', name: 'comment_gif_search', methods: ['GET'])]
    public function search(Request $request, GiphyService $giphyService): JsonResponse
    {
        $query = $request->query->get('q', '');

        if (strlen($query) < 2) {
            return $this->json([]);
        }

        $results = $giphyService->search($query, 12);

        return $this->json($results);
    }
}
