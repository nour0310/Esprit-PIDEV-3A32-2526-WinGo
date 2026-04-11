<?php

namespace App\Command;

use Rubix\ML\Classifiers\KNearestNeighbors;
use Rubix\ML\Datasets\Labeled;
use Rubix\ML\PersistentModel;
use Rubix\ML\Persisters\Filesystem;
use Rubix\ML\Pipeline;
use Rubix\ML\Transformers\TokenHashingVectorizer;
use Rubix\ML\Transformers\TextNormalizer;
use Rubix\ML\Transformers\TfIdfTransformer;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\DependencyInjection\Attribute\Autowire;

#[AsCommand(
    name: 'app:train-sentiment',
    description: 'Entraîne et sauvegarde le modèle d\'analyse de sentiment',
)]
class TrainSentimentModelCommand extends Command
{
    private string $projectDir;

    public function __construct(
        #[Autowire('%kernel.project_dir%')] string $projectDir
    ) {
        parent::__construct();
        $this->projectDir = $projectDir;
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $output->writeln('Démarrage de l\'entraînement du modèle...');

        // Exemples d'entraînement
        $samples = [
            // POSITIFS
            ['Ce produit est fantastique et je l\'adore.'],
            ['C\'est le meilleur article que j\'ai jamais lu.'],
            ['Je suis très content de ce service.'],
            ['Excellent travail !'],
            ['Magnifique endroit, j\'ai adoré.'],
            ['bravo super génial joli nice good excellent'],
            ['J\'aime beaucoup, c\'est très beau.'],
            ['parfait merci top incroyable cool bien'],
            ['très bien c\'est bien je recommande belle expérience'],
            ['c\'est joli vraiment super extra formidable'],

            // NEUTRES
            ['C\'est correct, sans plus.'],
            ['Je n\'ai pas d\'avis particulier là-dessus.'],
            ['Un article informatif.'],
            ['Les informations sont factuelles.'],
            ['Comme ci comme ça.'],
            ['moyen bof passable normal ok okay'],
            ['Ça peut aller. Je ne sais pas. Peut-être.'],
            ['Rien à signaler. C\'est juste là. classique'],

            // NEGATIFS
            ['Je déteste vraiment ça.'],
            ['C\'est la pire expérience de ma vie.'],
            ['Nul, je ne recommande pas du tout.'],
            ['Très déçu par le service.'],
            ['C\'est une perte de temps complète.'],
            ['bad mauvais horrible nul catastrophe terrible'],
            ['jaime pas j\'aime pas je n\'aime pas décevant'],
            ['c\'est moche zéro inutile arnaque honteux'],
            ['pire pas terrible très mauvais je hais'],
            ['vraiment nul n\'importe quoi fuyez'],
        ];

        $labels = [
            // Positifs (10)
            'positif', 'positif', 'positif', 'positif', 'positif',
            'positif', 'positif', 'positif', 'positif', 'positif',
            
            // Neutres (8)
            'neutre', 'neutre', 'neutre', 'neutre', 'neutre',
            'neutre', 'neutre', 'neutre',
            
            // Négatifs (10)
            'negatif', 'negatif', 'negatif', 'negatif', 'negatif',
            'negatif', 'negatif', 'negatif', 'negatif', 'negatif',
        ];

        $dataset = new Labeled($samples, $labels);

        $estimator = new PersistentModel(
            new Pipeline([
                new TextNormalizer(),
                new TokenHashingVectorizer(100),
                new TfIdfTransformer(),
            ], new KNearestNeighbors(3)),
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
