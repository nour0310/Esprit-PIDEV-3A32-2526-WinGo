<?php

namespace App\Controller;

use App\Form\DevenirCommercantType;
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

final class CommercantController extends AbstractController
{
 #[IsGranted('ROLE_USER')]
    #[Route('/devenir-commercant', name: 'devenir_commercant', methods: ['GET', 'POST'])]
    public function devenirCommercant(
        Request $request,
        EntityManagerInterface $em,
        UtilisateurRepository $utilisateurRepository
    ): Response {
        /** @var \App\Entity\Utilisateur|null $sessionUser */
        $sessionUser = $this->getUser();

        if (!$sessionUser) {
            throw $this->createAccessDeniedException('Vous devez être connecté.');
        }

        $user = $utilisateurRepository->find($sessionUser->getId());

        if (!$user) {
            throw $this->createNotFoundException('Utilisateur introuvable.');
        }

        $typeActuel = strtoupper((string) ($user->getType() ?? ''));

        if ($typeActuel === 'COMMERCANT') {
            $this->addFlash('info', 'Votre compte est déjà commerçant.');
            return $this->redirectToRoute('merchant_dashboard');
        }

        $form = $this->createForm(DevenirCommercantType::class, $user);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            if ($typeActuel === 'EN_ATTENTE_COMMERCANT') {
                $this->addFlash('warning', 'Votre demande est déjà en attente de validation.');
                return $this->redirectToRoute('devenir_commercant');
            }

            $user->setType('EN_ATTENTE_COMMERCANT');
            $em->flush();

            $this->addFlash('success', 'Votre demande a bien été envoyée à l’administrateur.');
            return $this->redirectToRoute('devenir_commercant');
        }

        return $this->render('commercant/devenir.html.twig', [
            'userData' => $user,
            'form' => $form->createView(),
        ]);
    }

    #[IsGranted('ROLE_ADMIN')]
    #[Route('/admin/demandes-commercant', name: 'admin_demandes_commercant')]
    public function adminDemandes(UtilisateurRepository $repo): Response
    {
        $demandes = $repo->findBy(['type' => 'EN_ATTENTE_COMMERCANT'], ['id' => 'DESC']);
        $commercants = $repo->findBy(['type' => 'COMMERCANT'], ['id' => 'DESC']);
        $clients = $repo->findBy(['type' => 'CLIENT'], ['id' => 'DESC']);

        $totalDemandes = count($demandes);
        $totalCommercants = count($commercants);
        $totalClients = count($clients);
        $totalUtilisateurs = $totalDemandes + $totalCommercants + $totalClients;

        return $this->render('commercant/admin_demandes.html.twig', [
            'demandes' => $demandes,
            'commercants' => $commercants,
            'totalDemandes' => $totalDemandes,
            'totalCommercants' => $totalCommercants,
            'totalClients' => $totalClients,
            'totalUtilisateurs' => $totalUtilisateurs,
        ]);
    }

    #[IsGranted('ROLE_ADMIN')]
    #[Route('/admin/demande-commercant/accepter/{id}', name: 'admin_demande_commercant_accepter', methods: ['POST'])]
    public function accepter(
        int $id,
        UtilisateurRepository $repo,
        EntityManagerInterface $em
    ): Response {
        $user = $repo->find($id);

        if (!$user) {
            throw $this->createNotFoundException('Utilisateur introuvable.');
        }

        $user->setType('COMMERCANT');
        $em->flush();

        $this->addFlash('success', 'La demande a été acceptée. L’utilisateur est maintenant commerçant.');

        return $this->redirectToRoute('admin_demandes_commercant');
    }

    #[IsGranted('ROLE_ADMIN')]
    #[Route('/admin/demande-commercant/refuser/{id}', name: 'admin_demande_commercant_refuser', methods: ['POST'])]
    public function refuser(
        int $id,
        UtilisateurRepository $repo,
        EntityManagerInterface $em
    ): Response {
        $user = $repo->find($id);

        if (!$user) {
            throw $this->createNotFoundException('Utilisateur introuvable.');
        }

        $user->setType('CLIENT');
        $em->flush();

        $this->addFlash('success', 'La demande a été refusée.');

        return $this->redirectToRoute('admin_demandes_commercant');
    }
}