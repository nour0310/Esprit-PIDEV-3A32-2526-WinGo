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
        $io->title('Entraînement du modèle de sentiment (ComplementNB)');

        // Corpus enrichi pour couvrir les cas courts
        $samples = [
            // ===== POSITIFS =====
            "J'adore cet article, très utile",
            "Superbe expérience, merci beaucoup",
            "Excellent contenu, bravo",
            "Très intéressant, j'ai appris des choses",
            "Parfait, rien à redire",
            "joli",
            "jolie",
            "nice",
            "beau",
            "belle",
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
            "j'aime beaucoup",
            "très bon",
            "vraiment top",
            "c'est génial",
            "je suis fan",
            "j'adore",
            "super article",
            "contenu de qualité",

            // ===== NÉGATIFS =====
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
            "mauvaise",
            "décevant",
            "inutile",
            "pas bon",
            "bof",
            "naze",
            "affreux",
            "c'est nul",
            "très déçu",
            "je déteste",
            "vraiment mauvais",
            "à chier",
            "c'est de la merde",
            "aucun intérêt",

            // ===== NEUTRES =====
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
            "mouais",
            "pourquoi pas",
        ];

        // Étiquettes correspondantes (même nombre que les échantillons)
        $labels = array_merge(
            array_fill(0, 28, 'positif'),
            array_fill(0, 25, 'negatif'),
            array_fill(0, 22, 'neutre')
        );

        $dataset = new Labeled($samples, $labels);

        // Pipeline avec ComplementNB (robuste pour le texte)
        $estimator = new Pipeline([
            new TextNormalizer(),
            new WordCountVectorizer(5000, 1, 0.8, new Word()),
            new TfIdfTransformer(),
        ], new NaiveBayes());

        $io->section('Entraînement du modèle (cela peut prendre quelques secondes)...');
        $estimator->train($dataset);
        $io->success('Modèle entraîné avec succès');

        $persister = new Filesystem($this->projectDir . '/var/ml/sentiment.model');
        $model = new PersistentModel($estimator, $persister);
        $model->save();
        $io->success('Modèle sauvegardé dans var/ml/sentiment.model');

        return Command::SUCCESS;
    }
}
