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
class ReservationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $inputStyle = ['class' => 'form-control', 'style' => 'height: 46px; border: none; padding-left: 15px; border-radius: 0; box-shadow: none;'];

        $builder
        ->add('user') 
        ->add('exp')  
        ->add('statut') 
        ->add('date', DateType::class, [
    'widget' => 'single_text',
    'required' => true, // Adds client-side validation
]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Reservation::class,
        ]);
    }
}