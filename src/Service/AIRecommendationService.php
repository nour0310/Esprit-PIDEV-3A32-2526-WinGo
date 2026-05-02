<?php

namespace App\Service;

class AIRecommendationService
{
    // 🎯 convertir type en score
    public function typeToScore(string $type): int
    {
        return match ($type) {
            'bus' => 1,
            'bateau' => 2,
            'louage' => 3,
            'avion' => 4,
            default => 0,
        };
    }

    /**
     * @param array{budget: float|int, type: float|int, confort: float|int, rapidite: float|int} $client
     * @param array{prix: float|int, type: float|int, confort: float|int, rapidite: float|int} $transport
     */
    public function calculSimilarite(array $client, array $transport): float
    {
        return sqrt(
            pow($client['budget'] - $transport['prix'], 2) +
            pow($client['type'] - $transport['type'], 2) +
            pow($client['confort'] - $transport['confort'], 2) +
            pow($client['rapidite'] - $transport['rapidite'], 2)
        );
    }

    /**
     * @param array<int, float|int> $data
     *
     * @return array{0: float, 1: float}
     */
    public function regressionLineaire(array $data): array
    {
        $n = count($data);

        $sumX = $sumY = $sumXY = $sumX2 = 0.0;

        for ($i = 0; $i < $n; $i++) {
            $x = $i + 1;
            $y = $data[$i];

            $sumX += $x;
            $sumY += $y;
            $sumXY += $x * $y;
            $sumX2 += $x * $x;
        }

        $a = ($n * $sumXY - $sumX * $sumY) / ($n * $sumX2 - $sumX * $sumX);
        $b = ($sumY - $a * $sumX) / $n;

        return [$a, $b];
    }

    /**
     * @param array<int, float|int> $historique
     */
    public function predictionRemplissage(string $type, string $arrivee, array $historique, float|int $capacite): float|int
    {
        if (count($historique) < 2) {
            return 0;
        }

        [$a, $b] = $this->regressionLineaire($historique);

        $next = count($historique) + 1;
        $prediction = $a * $next + $b;

        // pondération
        if ($type == 'avion') {
            $prediction *= 1.2;
        }

        if ($type == 'bus') {
            $prediction *= 1.05;
        }

        if ($type == 'bateau') {
            $prediction *= 1.1;
        }

        if ($type == 'louage') {
            $prediction *= 1.08;
        }

        if ($arrivee == 'Tunis') {
            $prediction *= 1.15;
        }

        $taux = ($prediction / $capacite) * 100;

        return min($taux, 100);
    }
}