<?php

namespace App\Form;

use App\Entity\Produit;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\MoneyType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\File;

class ProduitType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom', TextType::class, [
                'label' => 'Nom',
                'required' => false,
                'attr' => [
                    'minlength' => 3,
                    'maxlength' => 100,
                    'id' => 'produit_nom',
                    'placeholder' => 'Ex : Robe artisanale',
                ],
            ])

            ->add('prix', MoneyType::class, [
                'label' => 'Prix',
                'required' => false,
                'currency' => false,
                'attr' => [
                    'min' => 0.1,
                    'step' => 0.01,
                    'placeholder' => 'Ex : 25.500',
                ],
            ])

            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'required' => false,
                'attr' => [
                    'id' => 'produit_description',
                    'minlength' => 10,
                    'placeholder' => 'Décrivez votre produit...',
                ],
            ])

            ->add('region', ChoiceType::class, [
                'label' => 'Région',
                'required' => false,
                'placeholder' => 'Choisir un gouvernorat',
                'choices' => [
                    'Tunis' => 'Tunis',
                    'Ariana' => 'Ariana',
                    'Ben Arous' => 'Ben Arous',
                    'Manouba' => 'Manouba',
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
            ])

            ->add('categorie', ChoiceType::class, [
                'label' => 'Catégorie',
                'required' => false,
                'placeholder' => 'Choisir une catégorie',
                'choices' => [
                    'Artisanat' => 'Artisanat',
                    'Gastronomie' => 'Gastronomie',
                    'Textile' => 'Textile',
                    'Bijoux' => 'Bijoux',
                    'Art' => 'Art',
                    'Souvenirs' => 'Souvenirs',
                ],
            ])

            ->add('stock', IntegerType::class, [
                'label' => 'Stock',
                'required' => false,
                'attr' => [
                    'min' => 1,
                    'placeholder' => 'Ex : 10',
                ],
            ])

            ->add('imageFile', FileType::class, [
                'label' => 'Image',
                'mapped' => false,
                'required' => false,
                'constraints' => [
                    new File([
                        'maxSize' => '5M',
                        'mimeTypes' => [
                            'image/jpeg',
                            'image/png',
                            'image/webp',
                            'image/jpg',
                        ],
                        'mimeTypesMessage' => 'Veuillez choisir une image valide.',
                    ]),
                ],
            ])

            ->add('dateAjout', DateTimeType::class, [
                'label' => 'Date ajout',
                'required' => false,
                'widget' => 'single_text',
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Produit::class,
        ]);
    }
}