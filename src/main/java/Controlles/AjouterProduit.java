package Controlles;

import java.net.URL;
import java.util.ResourceBundle;

import Entites.Produit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AjouterProduit {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private TextField nomTextField;
    @FXML private TextField prixTextField;

    @FXML
    void ajouterProduit(ActionEvent event) {
        Produit produit = new Produit();
        // TODO: produit.setNom(nomTextField.getText());
        // TODO: produit.setPrix(Double.parseDouble(prixTextField.getText()));
    }

    @FXML
    void clearForm(ActionEvent event) {
        nomTextField.clear();
        prixTextField.clear();
    }

    @FXML
    void initialize() { }

    @FXML private void goAjouter() {
        Navigator.goTo("AjouterProduit.fxml", "Ajouter Produit");
    }

    @FXML private void goListe() {
        Navigator.goTo("ListeProduits.fxml", "Liste Produits");
    }

    @FXML private void goDashboard() {
        Navigator.goTo("Dashboard.fxml", "Dashboard");
    }

    @FXML private void goSettings() {
        Navigator.goTo("Settings.fxml", "Paramètres");
    }
}