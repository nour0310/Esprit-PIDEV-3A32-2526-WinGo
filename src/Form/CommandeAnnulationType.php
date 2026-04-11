<?php

namespace App\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class CommandeAnnulationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('cause_annulation', ChoiceType::class, [
                'label' => false,
                'placeholder' => 'Cause d’annulation',
                'choices' => [
                    'Informations de livraison insuffisantes' => 'Informations de livraison insuffisantes',
                    'Produit indisponible' => 'Produit indisponible',
                    'Rupture de stock' => 'Rupture de stock',
                    'Adresse invalide ou incomplète' => 'Adresse invalide ou incomplète',
                    'Paiement non confirmé' => 'Paiement non confirmé',
                    'Problème de contact avec le client' => 'Problème de contact avec le client',
                    'Zone de livraison non couverte' => 'Zone de livraison non couverte',
                    'Demande d’annulation par le client' => 'Demande d’annulation par le client',
                    'Autre' => 'Autre',
                ],
                'attr' => [
                    'class' => 'form-control',
                    'style' => 'padding:8px 10px; border-radius:8px;',
                ],
            ])
            ->add('submit', SubmitType::class, [
                'label' => 'Annuler',
                'attr' => [
                    'class' => 'topbar_btn',
                    'style' => 'padding:8px 14px; border:none; background:linear-gradient(135deg,#dc3545,#ff6b6b);',
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([]);
    }
}