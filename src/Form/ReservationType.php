<?php

namespace App\Form;
use App\Entity\Transport;
use App\Entity\Reservation;
use App\Entity\Utilisateur;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
class ReservationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $inputStyle = ['class' => 'form-control', 'style' => 'height: 46px; border: none; padding-left: 15px; border-radius: 0; box-shadow: none;'];

        $builder
        ->add('user', TextType::class, [
                'label' => 'Client',
                'disabled' => true,  // Make it read-only
                'attr' => [
                    'class' => 'form-control',
                    'readonly' => true,
                ]
            ]) 
        ->add('exp', ChoiceType::class, [
                'label' => 'Expérience (Gouvernorat)',
                'choices' => [
                    'Ariana' => 'Ariana',
                    'Béja' => 'Béja',
                    'Ben Arous' => 'Ben Arous',
                    'Bizerte' => 'Bizerte',
                    'Gabès' => 'Gabès',
                    'Gafsa' => 'Gafsa',
                    'Jendouba' => 'Jendouba',
                    'Kairouan' => 'Kairouan',
                    'Kasserine' => 'Kasserine',
                    'Kebili' => 'Kebili',
                    'Kef' => 'Kef',
                    'Mahdia' => 'Mahdia',
                    'Manouba' => 'Manouba',
                    'Medenine' => 'Medenine',
                    'Monastir' => 'Monastir',
                    'Nabeul' => 'Nabeul',
                    'Sfax' => 'Sfax',
                    'Sidi Bouzid' => 'Sidi Bouzid',
                    'Siliana' => 'Siliana',
                    'Sousse' => 'Sousse',
                    'Tataouine' => 'Tataouine',
                    'Tozeur' => 'Tozeur',
                    'Tunis' => 'Tunis',
                    'Zaghouan' => 'Zaghouan',
                ],
                'placeholder' => 'Choisissez une destination...',
                'attr' => ['class' => 'form-control modern-input'],
            ])  
        ->add('statut') 
        ->add('date', DateType::class, [
            'widget' => 'single_text', // This is ONLY allowed on Date/Time types
        ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Reservation::class,
        ]);
    }
}