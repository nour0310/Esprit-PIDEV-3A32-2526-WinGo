<?php
require 'vendor/autoload.php';

use Rubix\ML\Datasets\Labeled;
use Rubix\ML\Transformers\WordCountVectorizer;
use Rubix\ML\Transformers\TextNormalizer;

$samples = [
    ['This is a string feature.']
];
$labels = ['positif'];
$dataset = new Labeled($samples, $labels);

$normalizer = new TextNormalizer();
$normalizer->fit($dataset);
$normalizer->transform($dataset->samples());

$vectorizer = new WordCountVectorizer();
$vectorizer->fit($dataset);
$vectorizer->transform($dataset->samples());

var_dump($dataset->samples()[0]);
