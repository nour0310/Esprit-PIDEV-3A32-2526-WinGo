<?php

namespace App\Service;

use PhpScience\TextRank\TextRankFacade;
use PhpScience\TextRank\Tool\StopWords\French;

class AISummaryService
{
    public function summarize(string $text, int $sentenceCount = 3): ?string
    {
        $text = trim(strip_tags($text));
        if (mb_strlen($text) < 200) {
            return "Le texte est trop court pour être résumé (minimum 200 caractères).";
        }

        try {
            $api = new TextRankFacade();
            $api->setStopWords(new French());

            $sentences = $api->summarizeTextBasic($text);
            $selected = array_slice($sentences, 0, $sentenceCount);

            return implode(' ', $selected);
        } catch (\Throwable) {
            return null;
        }
    }
}
