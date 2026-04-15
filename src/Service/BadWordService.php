<?php

namespace App\Service;

/**
 * BadWordFilterBundle — Service de détection de mots inappropriés.
 * 
 * Analyse le contenu des commentaires et détecte les mots vulgaires,
 * insultants ou inappropriés en français, anglais et arabe (translittéré).
 */
class BadWordService
{
    /**
     * Liste des mots interdits (français, anglais, arabe translittéré).
     * Chaque mot est en minuscule pour faciliter la comparaison.
     */
    private const BAD_WORDS = [
        // Français
        'merde', 'putain', 'connard', 'connasse', 'salaud', 'salope',
        'enculé', 'enculer', 'nique', 'niquer', 'ntm', 'fdp', 'pd',
        'batard', 'bâtard', 'pute', 'bordel', 'con', 'cul', 'bite',
        'couille', 'chier', 'foutre', 'baiser', 'pétasse', 'abruti',
        'débile', 'crétin', 'idiot', 'imbécile', 'taré', 'dégueulasse',
        'pouffiasse', 'trou du cul', 'va te faire', 'ferme ta gueule',
        'gueule', 'branleur', 'branlé', 'encule', 'niqueur',
        // Anglais
        'fuck', 'shit', 'bitch', 'asshole', 'bastard', 'dick',
        'damn', 'crap', 'stupid', 'idiot', 'moron', 'dumb',
        'retard', 'whore', 'slut', 'nigger', 'faggot',
        // Arabe (translittéré)
        'kol5ara', 'kelb', 'zebi', 'kahba', 'manyak', 'khra',
        'ahmar', 'hmar', 'tboun', 'ommok', 'ya kalb', 'ya hmar',
        'ta7an', 'zamel', 'miboun', 'nayek',
    ];

    /**
     * Vérifie si un texte contient des mots inappropriés.
     */
    public function containsBadWords(string $text): bool
    {
        return !empty($this->detectBadWords($text));
    }

    /**
     * Détecte et retourne les mots inappropriés trouvés dans le texte.
     *
     * @return string[] Liste des mots inappropriés trouvés
     */
    public function detectBadWords(string $text): array
    {
        $normalizedText = $this->normalize($text);
        $found = [];

        foreach (self::BAD_WORDS as $badWord) {
            $pattern = '/\b' . preg_quote($badWord, '/') . '\b/iu';
            if (preg_match($pattern, $normalizedText)) {
                $found[] = $badWord;
            }
        }

        return array_unique($found);
    }

    /**
     * Retourne un résumé d'analyse pour un commentaire.
     *
     * @return array{has_bad_words: bool, bad_words: string[], severity: string}
     */
    public function analyze(string $text): array
    {
        $badWords = $this->detectBadWords($text);
        $count = count($badWords);

        if ($count === 0) {
            $severity = 'clean';
        } elseif ($count <= 2) {
            $severity = 'warning';
        } else {
            $severity = 'danger';
        }

        return [
            'has_bad_words' => $count > 0,
            'bad_words' => $badWords,
            'severity' => $severity,
        ];
    }

    /**
     * Normalise le texte pour la détection (minuscules, suppression accents légers).
     */
    private function normalize(string $text): string
    {
        // Convertir en minuscules
        $text = mb_strtolower($text, 'UTF-8');

        // Remplacer les caractères leetspeak courants
        $text = strtr($text, [
            '0' => 'o', '1' => 'i', '3' => 'e', '4' => 'a',
            '5' => 's', '7' => 't', '@' => 'a', '$' => 's',
        ]);

        // Supprimer les caractères répétés (ex: "fuuuuck" -> "fuck")
        $text = (string) preg_replace('/(.)\1{2,}/u', '$1', $text);

        return $text;
    }
}
