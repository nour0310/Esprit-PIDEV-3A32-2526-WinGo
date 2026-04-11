<?php

namespace App\Command;

use Rubix\ML\Classifiers\NaiveBayes;
use Rubix\ML\Datasets\Labeled;
use Rubix\ML\PersistentModel;
use Rubix\ML\Persisters\Filesystem;
use Rubix\ML\Pipeline;
use Rubix\ML\Transformers\TextNormalizer;
use Rubix\ML\Transformers\WordCountVectorizer;
use Rubix\ML\Transformers\TfIdfTransformer;
use Rubix\ML\Tokenizers\Word;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;
use Symfony\Component\DependencyInjection\Attribute\Autowire;

#[AsCommand(name: 'app:train-sentiment')]
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
        $io = new SymfonyStyle($input, $output);
        $io->title('Entraînement du modèle de sentiment (MultinomialNB)');

        // Corpus équilibré avec autant de neutres que de positifs et négatifs
        $samples = [
            // Positifs (~18 exemples)
            "J'adore cet article, très utile",
            "Superbe expérience, merci beaucoup",
            "Excellent contenu, bravo",
            "Très intéressant, j'ai appris des choses",
            "Parfait, rien à redire",
            "joli",
            "nice",
            "beau",
            "magnifique",
            "top",
            "génial",
            "super",
            "merveilleux",
            "bravo",
            "bien",
            "agréable",
            "cool",
            "excellent",
            
            // Négatifs (~18 exemples)
            "Nul, perte de temps",
            "Déçu, ne correspond pas à mes attentes",
            "Mauvais article, sans intérêt",
            "Je n'ai pas aimé du tout",
            "À éviter absolument",
            "bad",
            "jaime pas",
            "j'aime pas",
            "nul",
            "horrible",
            "mauvais",
            "décevant",
            "inutile",
            "pas bon",
            "bof",
            "naze",
            "affreux",
            
            // Neutres (~18 exemples pour équilibrer)
            "Correct, sans plus",
            "Pas mal mais peut mieux faire",
            "Moyen, ni bon ni mauvais",
            "Bof, je m'attendais à mieux",
            "ok",
            "moyen",
            "passable",
            "quelconque",
            "sans avis",
            "rien de spécial",
            "ordinaire",
            "ça va",
            "comme ci comme ça",
            "peut mieux faire",
            "pas terrible mais pas mauvais",
            "je ne sais pas quoi en penser",
            "mitigé",
            "assez moyen",
            "bof bof",
            "sans plus",
        ];

        $labels = [
            // Positifs (18)
            'positif', 'positif', 'positif', 'positif', 'positif',
            'positif', 'positif', 'positif', 'positif', 'positif',
            'positif', 'positif', 'positif', 'positif', 'positif',
            'positif', 'positif', 'positif',
            
            // Négatifs (18)
            'negatif', 'negatif', 'negatif', 'negatif', 'negatif',
            'negatif', 'negatif', 'negatif', 'negatif', 'negatif',
            'negatif', 'negatif', 'negatif', 'negatif', 'negatif',
            'negatif', 'negatif', 'negatif',
            
            // Neutres (19)
            'neutre', 'neutre', 'neutre', 'neutre', 'neutre',
            'neutre', 'neutre', 'neutre', 'neutre', 'neutre',
            'neutre', 'neutre', 'neutre', 'neutre', 'neutre',
            'neutre', 'neutre', 'neutre', 'neutre',
        ];

        $dataset = new Labeled($samples, $labels);

        // Pipeline avec MultinomialNB (compatible avec les probabilités)
        $estimator = new Pipeline([
            new TextNormalizer(),
            new WordCountVectorizer(2000, 1, 0.8, new Word()),
            new TfIdfTransformer(),
        ], new MultinomialNB());

        $io->section('Entraînement du pipeline');
        $estimator->train($dataset);
        $io->success('Modèle entraîné avec succès');

        $io->section('Sauvegarde du modèle');
        $persister = new Filesystem($this->projectDir . '/var/ml/sentiment.model');
        $model = new PersistentModel($estimator, $persister);
        $model->save();
        $io->success('Pipeline sauvegardé dans var/ml/sentiment.model');

        return Command::SUCCESS;
    }
}
