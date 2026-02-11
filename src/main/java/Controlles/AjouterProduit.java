package Controlles;

import java.net.URL;
import java.util.ResourceBundle;

import Entites.Produit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AjouterProduit {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField nomTextField;

    @FXML
    private TextField prixTextField;

    @FXML
    void ajouterProduit(ActionEvent event) {
        Produit produit=new Produit();
    }

    @FXML
    void initialize() {
    }

}
