<?php

namespace App\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;

class RecommendationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options)
    {
        $builder
            ->add('arrivee', ChoiceType::class, [
                'choices' => [
                    'Tunis' => 'Tunis',
                    'Sousse' => 'Sousse',
                    'Sfax' => 'Sfax',
                ]
            ])
            ->add('budget', ChoiceType::class, [
                'choices' => [
                    'Low' => 1,
                    'Medium' => 2,
                    'High' => 3,
                ]
            ])
            ->add('type', ChoiceType::class, [
                'choices' => [
                    'Bus' => 1,
                    'Bateau' => 2,
                    'Louage' => 3,
                    'Avion' => 4,
                ]
            ])
            ->add('confort', IntegerType::class)
            ->add('rapidite', IntegerType::class);
    }
}
