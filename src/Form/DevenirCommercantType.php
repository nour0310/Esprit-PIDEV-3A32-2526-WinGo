<?php

namespace App\Form;

use App\Entity\Utilisateur;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints as Assert;

class DevenirCommercantType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom', TextType::class, [
                'label' => 'Nom',
                'trim' => true,
                'constraints' => [
                    new Assert\NotBlank(message: 'Le nom est obligatoire.'),
                    new Assert\Length(
                        min: 2,
                        max: 100,
                        minMessage: 'Le nom doit contenir au moins {{ limit }} caractères.',
                        maxMessage: 'Le nom ne doit pas dépasser {{ limit }} caractères.'
                    ),
                ],
                'attr' => [
                    'placeholder' => 'Votre nom',
                ],
            ])
            ->add('prenom', TextType::class, [
                'label' => 'Prénom',
                'trim' => true,
                'constraints' => [
                    new Assert\NotBlank(message: 'Le prénom est obligatoire.'),
                    new Assert\Length(
                        min: 2,
                        max: 100,
                        minMessage: 'Le prénom doit contenir au moins {{ limit }} caractères.',
                        maxMessage: 'Le prénom ne doit pas dépasser {{ limit }} caractères.'
                    ),
                ],
                'attr' => [
                    'placeholder' => 'Votre prénom',
                ],
            ])
            ->add('email', EmailType::class, [
                'label' => 'Email',
                'trim' => true,
                'constraints' => [
                    new Assert\NotBlank(message: 'L’email est obligatoire.'),
                    new Assert\Email(message: 'Veuillez saisir un email valide.'),
                    new Assert\Length(
                        max: 180,
                        maxMessage: 'L’email ne doit pas dépasser {{ limit }} caractères.'
                    ),
                ],
                'attr' => [
                    'placeholder' => 'Votre email',
                ],
            ])
            ->add('telephone', IntegerType::class, [
                'label' => 'Téléphone',
                'required' => false,
                'invalid_message' => 'Le téléphone doit être un nombre.',
                'constraints' => [
                    new Assert\Range(
                        min: 10000000,
                        max: 99999999,
                        notInRangeMessage: 'Le téléphone doit contenir 8 chiffres.'
                    ),
                ],
                'attr' => [
                    'placeholder' => 'Votre téléphone',
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Utilisateur::class,
        ]);
    }
}