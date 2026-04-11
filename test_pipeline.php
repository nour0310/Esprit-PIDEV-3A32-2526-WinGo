<?php
require 'vendor/autoload.php';

use Rubix\ML\Datasets\Labeled;
use Rubix\ML\Datasets\Unlabeled;
use Rubix\ML\Pipeline;
use Rubix\ML\Transformers\WordCountVectorizer;
use Rubix\ML\Transformers\TextNormalizer;
use Rubix\ML\Classifiers\GaussianNB;

$samples = [
    ['Ce produit est fantastique et je l\'adore.'],
    ['bravo super génial joli nice good excellent'],
    ['C\'est correct, sans plus.'],
    ['moyen bof passable normal ok okay'],
    ['Je déteste vraiment ça.'],
    ['bad mauvais horrible nul catastrophe terrible'],
    ['jaime pas j\'aime pas je n\'aime pas décevant'],
];

$labels = [
    'positif', 'positif',
    'neutre', 'neutre',
    'negatif', 'negatif', 'negatif'
];

$dataset = new Labeled($samples, $labels);

$pipeline = new Pipeline([
    new TextNormalizer(),
    new WordCountVectorizer(),
], new GaussianNB());

$pipeline->train($dataset);

$texts = ['bad', 'jaime pas', 'joli', 'nice', 'horrible'];
foreach ($texts as $text) {
    $prediction = $pipeline->predict(new Unlabeled([[$text]]))[0];
    echo "Text: '$text' -> $prediction\n";
}
