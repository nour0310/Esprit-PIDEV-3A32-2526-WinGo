<?php

namespace App\Form;

use App\Entity\Article;
use Ehyiah\QuillJsBundle\Form\QuillType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class ArticleType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('titre', TextType::class, [
                'label' => 'Titre de votre blog',
                'attr' => ['class' => 'form-control wingo-input', 'placeholder' => 'Donnez un titre captivant...']
            ])
            ->add('contenu', QuillType::class, [
                'label' => 'Votre histoire',
                'attr' => ['placeholder' => 'Racontez votre expérience...'],
                'quill_options' => [
                    ['bold', 'italic', 'underline'],
                    [['list' => 'ordered'], ['list' => 'bullet']],
                    ['blockquote', 'code-block'],
                    [['header' => [1, 2, 3, false]]],
                    [['align' => []]],
                    ['clean'],
                ],
                'quill_extra_options' => [
                    'height' => '260px',
                    'placeholder' => 'Racontez votre expérience...',
                    'theme' => 'snow',
                ],
            ])
            ->add('region', ChoiceType::class, [
                'label' => 'Région',
                'choices' => [
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
                ],
                'placeholder' => 'Choisissez une région',
                'attr' => ['class' => 'form-control wingo-input']
            ])
            ->add('categorie', ChoiceType::class, [
                'label' => 'Catégorie',
                'choices' => [
                    'Aventure' => 'Aventure',
                    'Culture' => 'Culture',
                    'Gastronomie' => 'Gastronomie',
                    'Détente' => 'Détente',
                ],
                'placeholder' => 'Choisissez une catégorie',
                'attr' => ['class' => 'form-control wingo-input']
            ])
            ->add('image', FileType::class, [
                'label' => 'Image (laisser vide pour garder l\'actuelle)',
                'mapped' => false,
                'required' => false,
                'attr' => ['class' => 'form-control wingo-input']
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Article::class,
        ]);
    }
}