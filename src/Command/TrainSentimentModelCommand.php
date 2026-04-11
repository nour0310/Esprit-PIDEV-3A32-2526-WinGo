<?php

namespace App\Command;

use Rubix\ML\Classifiers\NaiveBayes;
use Rubix\ML\Datasets\Labeled;
use Rubix\ML\PersistentModel;
use Rubix\ML\Persisters\Filesystem;
use Rubix\ML\Pipeline;
use Rubix\ML\Transformers\TfIdfTransformer;
use Rubix\ML\Transformers\WordCountVectorizer;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

#[AsCommand(
    name: 'app:train-sentiment',
    description: 'Entraîne et sauvegarde le modèle d\'analyse de sentiment',
)]
class TrainSentimentModelCommand extends Command
{
    private string $projectDir;

    public function __construct(string $projectDir)
    {
        parent::__construct();
        $this->projectDir = $projectDir;
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $output->writeln('Démarrage de l\'entraînement du modèle...');

        // Exemples d'entraînement
        $samples = [
            ['Ce produit est fantastique et je l\'adore.'],
            ['C\'est le meilleur article que j\'ai jamais lu.'],
            ['Je suis très content de ce service.'],
            ['Excellent travail !'],
            ['Magnifique endroit, j\'ai adoré.'],

            ['C\'est correct, sans plus.'],
            ['Je n\'ai pas d\'avis particulier là-dessus.'],
            ['Un article informatif.'],
            ['Les informations sont factuelles.'],
            ['Comme ci comme ça.'],

            ['Je déteste vraiment ça.'],
            ['C\'est la pire expérience de ma vie.'],
            ['Nul, je ne recommande pas du tout.'],
            ['Très déçu par le service.'],
            ['C\'est une perte de temps complète.'],
        ];

        $labels = [
            'positif', 'positif', 'positif', 'positif', 'positif',
            'neutre', 'neutre', 'neutre', 'neutre', 'neutre',
            'negatif', 'negatif', 'negatif', 'negatif', 'negatif',
        ];

        $dataset = new Labeled($samples, $labels);

        $estimator = new PersistentModel(
            new Pipeline([
                new WordCountVectorizer(10000, 1, 2),
                new TfIdfTransformer(),
            ], new NaiveBayes()),
            new Filesystem($this->projectDir . '/var/ml/sentiment.model')
        );

        // Créer le dossier s'il n'existe pas
        if (!is_dir($this->projectDir . '/var/ml')) {
            mkdir($this->projectDir . '/var/ml', 0777, true);
        }

        $estimator->train($dataset);
        $estimator->save();

        $output->writeln('Modèle entraîné et sauvegardé avec succès dans var/ml/sentiment.model !');

        return Command::SUCCESS;
    }
}
