<?php

namespace App\Controller;

use App\Entity\Produit;
use App\Form\ProduitType;
use App\Repository\ProduitRepository;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Symfony\Component\String\Slugger\SluggerInterface;

#[Route('/produit')]
final class ProduitController extends AbstractController
{
    #[Route('/list', name: 'produit_list')]
    public function list(ProduitRepository $repo): Response
    {
        return $this->render('produit/list.html.twig', [
            'list' => $repo->findAll()
        ]);
    }

    #[Route('/details/{id}', name: 'produit_details')]
    public function details($id, ProduitRepository $repo): Response
    {
        return $this->render('produit/details.html.twig', [
            'produit' => $repo->find($id)
        ]);
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/add', name: 'produit_add')]
    public function add(
        ManagerRegistry $manager,
        Request $request,
        SluggerInterface $slugger
    ): Response {
        $em = $manager->getManager();
        $produit = new Produit();

        $form = $this->createForm(ProduitType::class, $produit);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            /** @var \App\Entity\Utilisateur $user */
            $user = $this->getUser();
            $produit->setIdUser($user->getId());

            $imageFile = $form->get('imageFile')->getData();

            if ($imageFile) {
                $originalFilename = pathinfo($imageFile->getClientOriginalName(), PATHINFO_FILENAME);
                $safeFilename = $slugger->slug($originalFilename);
                $newFilename = $safeFilename . '-' . uniqid() . '.' . $imageFile->guessExtension();

                try {
                    $imageFile->move(
                        $this->getParameter('images_directory'),
                        $newFilename
                    );
                } catch (FileException $e) {
                    throw new \Exception('Erreur lors de l upload de l image');
                }

                $produit->setImage($newFilename);
            }

            if ($produit->getDateAjout() === null) {
                $produit->setDateAjout(new \DateTime());
            }

            $em->persist($produit);
            $em->flush();

            if (($user->getType() ?? '') === 'COMMERCANT') {
                return $this->redirectToRoute('merchant_dashboard');
            }

            return $this->redirectToRoute('produit_list');
        }

        return $this->render('produit/add.html.twig', [
            'formProduit' => $form->createView()
        ]);
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/update/{id}', name: 'produit_update')]
    public function update(
        $id,
        ProduitRepository $repo,
        ManagerRegistry $manager,
        Request $request,
        SluggerInterface $slugger
    ): Response {
        $em = $manager->getManager();
        $produit = $repo->find($id);

        if (!$produit) {
            throw $this->createNotFoundException('Produit introuvable');
        }

        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        if (($user->getType() ?? '') === 'COMMERCANT' && $produit->getIdUser() !== $user->getId()) {
            throw $this->createAccessDeniedException('Vous ne pouvez modifier que vos propres produits.');
        }

        $form = $this->createForm(ProduitType::class, $produit);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $imageFile = $form->get('imageFile')->getData();

            if ($imageFile) {
                $originalFilename = pathinfo($imageFile->getClientOriginalName(), PATHINFO_FILENAME);
                $safeFilename = $slugger->slug($originalFilename);
                $newFilename = $safeFilename . '-' . uniqid() . '.' . $imageFile->guessExtension();

                try {
                    $imageFile->move(
                        $this->getParameter('images_directory'),
                        $newFilename
                    );
                } catch (FileException $e) {
                    throw new \Exception('Erreur lors de l upload de l image');
                }

                $produit->setImage($newFilename);
            }

            $em->flush();

            if (($user->getType() ?? '') === 'COMMERCANT') {
                return $this->redirectToRoute('merchant_dashboard');
            }

            return $this->redirectToRoute('produit_list');
        }

        return $this->render('produit/add.html.twig', [
            'formProduit' => $form->createView()
        ]);
    }

    #[IsGranted('ROLE_USER')]
    #[Route('/delete/{id}', name: 'produit_delete')]
    public function delete($id, ProduitRepository $repo, ManagerRegistry $manager): Response
    {
        $em = $manager->getManager();
        $produit = $repo->find($id);

        if (!$produit) {
            return $this->redirectToRoute('produit_list');
        }

        /** @var \App\Entity\Utilisateur $user */
        $user = $this->getUser();

        if (($user->getType() ?? '') === 'COMMERCANT' && $produit->getIdUser() !== $user->getId()) {
            throw $this->createAccessDeniedException('Vous ne pouvez supprimer que vos propres produits.');
        }

        $em->remove($produit);
        $em->flush();

        if (($user->getType() ?? '') === 'COMMERCANT') {
            return $this->redirectToRoute('merchant_dashboard');
        }

        return $this->redirectToRoute('produit_list');
    }

}