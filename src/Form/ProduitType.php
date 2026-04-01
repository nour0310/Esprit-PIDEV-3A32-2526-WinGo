<?php

namespace App\Form;

use App\Entity\Produit;
use App\Repository\UtilisateurRepository;
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
    private UtilisateurRepository $utilisateurRepository;

    public function __construct(UtilisateurRepository $utilisateurRepository)
    {
        $this->utilisateurRepository = $utilisateurRepository;
    }

    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $users = $this->utilisateurRepository->findAll();

        $choices = [];
        foreach ($users as $user) {
            $label = $user->getNom() . ' ' . $user->getPrenom() . ' (' . $user->getEmail() . ')';
            $choices[$label] = $user->getId();
        }

        $builder
            ->add('idUser', ChoiceType::class, [
                'label' => 'Utilisateur',
                'choices' => $choices,
                'placeholder' => 'Choisir un utilisateur'
            ])
            ->add('nom', TextType::class, [
                'label' => 'Nom'
            ])
            ->add('description', TextareaType::class, [
                'required' => false,
                'label' => 'Description'
            ])
            ->add('prix', MoneyType::class, [
                'label' => 'Prix',
                'currency' => false
            ])
            ->add('region', TextType::class, [
                'required' => false,
                'label' => 'Region'
            ])
            ->add('categorie', TextType::class, [
                'required' => false,
                'label' => 'Categorie'
            ])
            ->add('stock', IntegerType::class, [
                'label' => 'Stock'
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
                            'image/jpg'
                        ],
                        'mimeTypesMessage' => 'Veuillez choisir une image valide',
                    ])
                ]
            ])
            ->add('dateAjout', DateTimeType::class, [
                'required' => false,
                'widget' => 'single_text',
                'label' => 'Date ajout'
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Produit::class,
        ]);
    }
}