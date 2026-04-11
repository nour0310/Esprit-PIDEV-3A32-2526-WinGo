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

$dataset->apply(new TextNormalizer());

$dataset->apply(new WordCountVectorizer());

var_dump($dataset->samples()[0]);
