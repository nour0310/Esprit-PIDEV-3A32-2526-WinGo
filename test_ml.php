<?php
require 'vendor/autoload.php';

use Rubix\ML\PersistentModel;
use Rubix\ML\Persisters\Filesystem;
use Rubix\ML\Datasets\Unlabeled;

try {
    $modelPath = 'var/ml/sentiment.model';
    $model = PersistentModel::load(new Filesystem($modelPath));
    $texts = ['bad', 'jaime pas', 'joli', 'nice', 'horrible'];
    
    foreach ($texts as $text) {
        $prediction = $model->predict(new Unlabeled([[$text]]))[0];
        echo "Text: '$text' -> $prediction\n";
    }
} catch (\Exception $e) {
    echo "ERROR: " . $e->getMessage() . "\n";
}
