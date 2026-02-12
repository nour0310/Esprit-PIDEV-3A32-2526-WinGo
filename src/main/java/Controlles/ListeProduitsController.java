package Controlles;

import javafx.fxml.FXML;

public class ListeProduitsController {
    @FXML private void goAjouter(){ Navigator.goTo("AjouterProduit.fxml","Ajouter Produit"); }
    @FXML private void goListe(){ Navigator.goTo("ListeProduits.fxml","Liste Produits"); }
    @FXML private void goDashboard(){ Navigator.goTo("Dashboard.fxml","Dashboard"); }
}