<?php
require 'vendor/autoload.php';

use Rubix\ML\PersistentModel;
use Rubix\ML\Persisters\Filesystem;

try {
    $model = PersistentModel::load(new Filesystem('var/ml/sentiment.model'));
    
    // Extrayons le pipeline
    // model = PersistentModel -> base() -> Pipeline
    $pipeline = $model->base();
    $transformers = $pipeline->transformers();
    var_dump(get_class($transformers[0]));
    
    $dataset = new \Rubix\ML\Datasets\Unlabeled([['nice']]);
    foreach ($transformers as $transformer) {
        $dataset->apply($transformer);
    }
    var_dump($dataset->samples()[0]);
    
} catch (\Exception $e) {
    echo "ERROR: " . $e->getMessage() . " at " . $e->getFile() . ":" . $e->getLine() . "\n";
}
