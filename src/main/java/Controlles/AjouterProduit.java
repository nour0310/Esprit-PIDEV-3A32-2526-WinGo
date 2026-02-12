package Controlles;

import java.net.URL;
import java.util.ResourceBundle;

import Entites.Produit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
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

    @FXML private TextField idCommercantField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField stockField;
    @FXML private TextField regionField;
    @FXML private TextField categorieField;
    @FXML private TextField imageField;
    @FXML private javafx.scene.control.Label statusLabel;

    @FXML
    void clearForm(ActionEvent event) {
        if (idCommercantField != null) idCommercantField.clear();
        if (nomTextField != null) nomTextField.clear();
        if (descriptionArea != null) descriptionArea.clear();
        if (prixTextField != null) prixTextField.clear();
        if (stockField != null) stockField.clear();
        if (regionField != null) regionField.clear();
        if (categorieField != null) categorieField.clear();
        if (imageField != null) imageField.clear();
        if (statusLabel != null) statusLabel.setText("");
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