<?php

namespace App\Form;

use App\Entity\Article;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\FormEvent;
use Symfony\Component\Form\FormEvents;
use Symfony\Component\OptionsResolver\OptionsResolver;

class ArticleType extends AbstractType
{
    /** @return array<string, string> */
    private static function regionChoices(): array
    {
        return [
            'Tunis' => 'Tunis',
            'Ariana' => 'Ariana',
            'Ben Arous' => 'Ben Arous',
            'La Manouba' => 'La Manouba',
            'Nabeul' => 'Nabeul',
            'Zaghouan' => 'Zaghouan',
            'Bizerte' => 'Bizerte',
            'Béja' => 'Béja',
            'Jendouba' => 'Jendouba',
            'Le Kef' => 'Le Kef',
            'Siliana' => 'Siliana',
            'Sousse' => 'Sousse',
            'Monastir' => 'Monastir',
            'Mahdia' => 'Mahdia',
            'Sfax' => 'Sfax',
            'Kairouan' => 'Kairouan',
            'Kasserine' => 'Kasserine',
            'Sidi Bouzid' => 'Sidi Bouzid',
            'Gabès' => 'Gabès',
            'Médenine' => 'Médenine',
            'Tataouine' => 'Tataouine',
            'Gafsa' => 'Gafsa',
            'Tozeur' => 'Tozeur',
            'Kébili' => 'Kébili',
        ];
    }

    /** @return array<string, string> */
    private static function categorieChoices(): array
    {
        return [
            'Aventure' => 'Aventure',
            'Culture' => 'Culture',
            'Gastronomie' => 'Gastronomie',
            'Détente' => 'Détente',
        ];
    }

    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('titre', TextType::class, [
                'label' => 'Titre',
                'attr' => ['placeholder' => 'Titre de l\'article'],
            ])
            ->add('contenu', TextareaType::class, [
                'label' => 'Contenu',
                'attr' => ['rows' => 10],
            ])
            ->add('region', ChoiceType::class, [
                'label' => 'Région',
                'choices' => self::regionChoices(),
                'placeholder' => 'Choisissez une région',
            ])
            ->add('categorie', ChoiceType::class, [
                'label' => 'Catégorie',
                'choices' => self::categorieChoices(),
                'placeholder' => 'Choisissez une catégorie',
            ])
            ->add('image', FileType::class, [
                'label' => 'Image (fichier)',
                'mapped' => false,
                'required' => false,
            ])
        ;

        // Valeurs déjà en base mais absentes des listes → évite "The selected choice is invalid" à l'édition
        $builder->addEventListener(FormEvents::PRE_SET_DATA, function (FormEvent $event): void {
            $article = $event->getData();
            if (!$article instanceof Article) {
                return;
            }

            $regionChoices = self::regionChoices();
            $r = $article->getRegion();
            if (\is_string($r) && $r !== '' && !\in_array($r, $regionChoices, true)) {
                $regionChoices[$r] = $r;
            }

            $catChoices = self::categorieChoices();
            $c = $article->getCategorie();
            if (\is_string($c) && $c !== '' && !\in_array($c, $catChoices, true)) {
                $catChoices[$c] = $c;
            }

            $form = $event->getForm();
            $form->remove('region');
            $form->remove('categorie');
            $form->add('region', ChoiceType::class, [
                'label' => 'Région',
                'choices' => $regionChoices,
                'placeholder' => 'Choisissez une région',
            ]);
            $form->add('categorie', ChoiceType::class, [
                'label' => 'Catégorie',
                'choices' => $catChoices,
                'placeholder' => 'Choisissez une catégorie',
            ]);
        });
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Article::class,
        ]);
    }
}
