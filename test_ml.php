<?php
require 'vendor/autoload.php';

use Rubix\ML\PersistentModel;
use Rubix\ML\Persisters\Filesystem;

try {
    $model = PersistentModel::load(new Filesystem('var/ml/sentiment.model'));
    var_dump($model->predictSample(['nice']));
} catch (\Exception $e) {
    echo "ERROR: " . $e->getMessage() . " at " . $e->getFile() . ":" . $e->getLine() . "\n";
}
