<?php

declare(strict_types=1);

require __DIR__ . '/../vendor/autoload.php';

echo "== TextRank ==\n";
try {
    $api = new \PhpScience\TextRank\TextRankFacade();
    $api->setStopWords(new \PhpScience\TextRank\Tool\StopWords\French());
    $sentences = $api->summarizeTextBasic(str_repeat("Bonjour. ", 120));
    echo "OK (sentences=" . \count($sentences) . ")\n";
} catch (\Throwable $e) {
    echo "FAIL: " . $e->getMessage() . "\n";
}

echo "\n== VADER Sentiment (davmixcool/php-sentiment-analyzer) ==\n";
try {
    $analyzer = new \Sentiment\Analyzer();
    $scores = $analyzer->getSentiment("C'est magnifique !");
    echo "OK " . \json_encode($scores, JSON_UNESCAPED_UNICODE) . "\n";
} catch (\Throwable $e) {
    echo "FAIL: " . $e->getMessage() . "\n";
}

echo "\n== Profanity Filter (devtrope/profanity-filter) ==\n";
try {
    $filter = new \ProfanityFilter\ProfanityFilter(\ProfanityFilter\ProfanityLevel::HIGH, 'fr');
    echo "OK contains=" . ($filter->containsProfanity("merde") ? 'yes' : 'no') . "\n";
    echo "OK cleaned=" . $filter->clean("c'est merde") . "\n";
} catch (\Throwable $e) {
    echo "FAIL: " . $e->getMessage() . "\n";
}

