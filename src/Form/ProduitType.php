<?php

namespace App\Form;

use App\Entity\Produit;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\MoneyType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class ProduitType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('idUser', IntegerType::class)
            ->add('nom', TextType::class)
            ->add('description', TextareaType::class, [
                'required' => false
            ])
            ->add('prix', MoneyType::class, [
                'currency' => false
            ])
            ->add('region', TextType::class, [
                'required' => false
            ])
            ->add('categorie', TextType::class, [
                'required' => false
            ])
            ->add('stock', IntegerType::class)
            ->add('image', TextType::class, [
                'required' => false
            ])
            ->add('dateAjout', DateTimeType::class, [
                'required' => false,
                'widget' => 'single_text'
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Produit::class,
        ]);
    }
}