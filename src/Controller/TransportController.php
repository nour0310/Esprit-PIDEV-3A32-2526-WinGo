<?php

namespace App\Controller;

use App\Entity\Transport;
use App\Form\TransportType;
use App\Repository\TransportRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/transport')]
final class TransportController extends AbstractController
{
    #[Route('/affiche', name: "displayTransport")]
    public function listTransportsFromDB(TransportRepository $repo): Response
    {
        return $this->render('transport/listFromDb.html.twig', ['list' => $repo->findAll()]);
    }

    #[Route('/add', name: "addTransport")]
    public function addTransport(EntityManagerInterface $em, Request $request): Response
    {
        $newTransport = new Transport();
        $form = $this->createForm(TransportType::class, $newTransport);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // CRITICAL: Set the user AFTER handleRequest to ensure the form doesn't overwrite it with NULL
            $newTransport->setUser_id($this->getUser());

            $em->persist($newTransport);
            $em->flush();
            return $this->redirectToRoute('displayTransport');
        }

        return $this->render('transport/add.html.twig', [
            'f' => $form->createView()
        ]);
    }

    #[Route('/delete/{id}', name: "deleteTransport")]
    public function delete(Transport $transport, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();

        // Fix naming: $user instead of $currentUser
        // Check if user_id object exists before calling getId()
        $ownerId = $transport->getUser_id() ? $transport->getUser_id()->getId() : null;

        if ($ownerId !== $user->getId() && !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException('Access Denied: You do not own this record.');
        }
        
        $em->remove($transport);
        $em->flush();
        
        return $this->redirectToRoute('displayTransport');
    }

    #[Route('/update/{id}', name: "updateTransport")]
    public function updateTransport(Transport $transport, EntityManagerInterface $em, Request $request): Response
    {
        $user = $this->getUser();

        // Security Check
        $ownerId = $transport->getUser_id() ? $transport->getUser_id()->getId() : null;
        if ($ownerId !== $user->getId() && !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException('Access Denied: You do not own this record.');
        }
        
        $form = $this->createForm(TransportType::class, $transport);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush(); // persist is not needed for updates
            return $this->redirectToRoute('displayTransport');
        }

        return $this->render('transport/add.html.twig', ['f' => $form->createView()]);
    }

    // ... Keep your search functions here
}