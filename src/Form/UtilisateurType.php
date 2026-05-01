<?php

namespace App\Form;

use App\Entity\Utilisateur;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\RepeatedType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;

class UtilisateurType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $is_edit = $options['is_edit'] ?? false;

        $builder
            ->add('prenom', TextType::class, [
                'label' => 'Prénom',
                'attr' => ['placeholder' => 'Ex: Jean'],
            ])
            ->add('nom', TextType::class, [
                'label' => 'Nom',
                'attr' => ['placeholder' => 'Ex: Dupont'],
            ])
            ->add('email', EmailType::class, [
                'label' => 'Adresse Email',
                'attr' => ['placeholder' => 'email@exemple.com'],
            ])
            ->add('plainPassword', PasswordType::class, [
                'label' => $is_edit ? 'Nouveau mot de passe (laisser vide pour ne pas changer)' : 'Mot de passe',
                'mapped' => false,
                'required' => !$is_edit,
                'attr' => ['autocomplete' => 'new-password'],
                'constraints' => $is_edit ? [] : [
                    new NotBlank(['message' => 'Veuillez entrer un mot de passe']),
                    new Length([
                        'min' => 4,
                        'minMessage' => 'Le mot de passe doit faire au moins {{ limit }} caractères',
                        'max' => 4096,
                    ]),
                ],
            ])
            ->add('type', ChoiceType::class, [
                'label' => 'Rôle / Type',
                'choices' => [
                    'Client' => 'CLIENT',
                    'Commerçant' => 'COMMERCANT',
                    'Administrateur' => 'ADMIN',
                ],
            ])
            ->add('telephone', IntegerType::class, [
                'label' => 'Téléphone',
                'required' => false,
            ])
            ->add('age', IntegerType::class, [
                'label' => 'Âge',
                'required' => false,
            ])
            ->add('genre', ChoiceType::class, [
                'label' => 'Genre / Sexe',
                'choices' => [
                    'Homme (Garçon)' => 'Homme',
                    'Femme (Fille)' => 'Femme',
                ],
                'required' => false,
            ])
            ->add('isVerified', ChoiceType::class, [
                'label' => 'Compte vérifié ?',
                'choices' => [
                    'Oui' => true,
                    'Non' => false,
                ],
                'expanded' => true,
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Utilisateur::class,
            'is_edit' => false,
        ]);
    }
}
