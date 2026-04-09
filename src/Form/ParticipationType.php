<?php

namespace App\Form;

use App\Entity\Participation;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\HiddenType;
use Symfony\Component\Form\Extension\Core\Type\TelType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class ParticipationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom_participant', TextType::class, [
                'label' => 'LAST NAME',
                'attr' => [
                    'placeholder' => 'Doe',
                    'autocomplete' => 'off'
                ]
            ])
            ->add('prenom_participant', TextType::class, [
                'label' => 'FIRST NAME',
                'attr' => [
                    'placeholder' => 'John',
                    'autocomplete' => 'off'
                ]
            ])
            ->add('email_participant', EmailType::class, [
                'label' => 'EMAIL',
                'attr' => [
                    'placeholder' => 'john@email.com',
                    'autocomplete' => 'off'
                ]
            ])
            ->add('telephone', TelType::class, [
                'label' => 'PHONE',
                'attr' => [
                    'placeholder' => '+216 XX XXX XXX',
                    'autocomplete' => 'off'
                ]
            ])
            ->add('nombre_places', IntegerType::class, [
                'label' => 'NUMBER OF SEATS',
                'attr' => [
                    'placeholder' => '1',
                    'autocomplete' => 'off'
                ]
            ])
            ->add('statut', ChoiceType::class, [
                'label' => 'STATUS',
                'choices' => [
                    'Pending' => 'pending',
                    'Confirmed' => 'confirmed',
                    'Cancelled' => 'cancelled',
                ]
            ])
            ->add('id_user', HiddenType::class)
            // Les champs date_participation et id_event sont supprimés
            // Ils sont définis automatiquement dans le contrôleur
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Participation::class,
            'csrf_protection' => true,
        ]);
    }
}