<?php

namespace App\Command;

use Rubix\ML\Classifiers\KNearestNeighbors;
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
        $io->title('Entraînement du modèle de sentiment (KNearestNeighbors)');

        // Corpus enrichi
        $samples = [
            // POSITIFS
            "J'adore cet article, très utile",
            "Superbe expérience, merci beaucoup",
            "Excellent contenu, bravo",
            "Très intéressant, j'ai appris des choses",
            "Parfait, rien à redire",
            "joli", "jolie", "nice", "beau", "belle", "magnifique", "top", "génial",
            "super", "merveilleux", "bravo", "bien", "agréable", "cool", "excellent",
            "j'aime beaucoup", "très bon", "vraiment top", "c'est génial",

            // NÉGATIFS
            "Nul, perte de temps",
            "Déçu, ne correspond pas à mes attentes",
            "Mauvais article, sans intérêt",
            "Je n'ai pas aimé du tout",
            "À éviter absolument",
            "bad", "jaime pas", "j'aime pas", "nul", "horrible", "mauvais",
            "décevant", "inutile", "pas bon", "bof", "naze", "affreux",
            "c'est nul", "très déçu", "je déteste", "vraiment mauvais",

            // NEUTRES
            "Correct, sans plus",
            "Pas mal mais peut mieux faire",
            "Moyen, ni bon ni mauvais",
            "Bof, je m'attendais à mieux",
            "ok", "moyen", "passable", "quelconque", "sans avis",
            "rien de spécial", "ordinaire", "ça va", "comme ci comme ça",
            "pas terrible mais pas mauvais", "mitigé", "bof bof", "sans plus",
        ];

        $labels = array_merge(
            array_fill(0, 24, 'positif'),
            array_fill(0, 20, 'negatif'),
            array_fill(0, 18, 'neutre')
        );

        $dataset = new Labeled($samples, $labels);

        $estimator = new Pipeline([
            new TextNormalizer(),
            new WordCountVectorizer(3000, 1, 0.8, new Word()),
            new TfIdfTransformer(),
        ], new KNearestNeighbors(3));

        $io->section('Entraînement du modèle...');
        $estimator->train($dataset);
        $io->success('Modèle entraîné.');

        // Sauvegarde
        $persister = new Filesystem($this->projectDir . '/var/ml/sentiment.model');
        $model = new PersistentModel($estimator, $persister);
        $model->save();

        return Command::SUCCESS;
    }
}