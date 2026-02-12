package Controlles;

import Entites.Produit;
import Services.ProduitCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class ListeProduitsController {

    @FXML private TableView<Produit> table;
    @FXML private TableColumn<Produit, Integer> idCol;
    @FXML private TableColumn<Produit, String> nomCol;
    @FXML private TableColumn<Produit, Double> prixCol;
    @FXML private TableColumn<Produit, Integer> stockCol;
    @FXML private TableColumn<Produit, String> catCol;
    @FXML private TableColumn<Produit, String> regionCol;

    private ProduitCRUD produitCRUD;

    @FXML
    public void initialize() {
        produitCRUD = new ProduitCRUD();

        idCol.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        catCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        regionCol.setCellValueFactory(new PropertyValueFactory<>("region"));

        refresh();
    }

    @FXML
    private void refresh() {
        try {
            List<Produit> list = produitCRUD.afficher();
            ObservableList<Produit> data = FXCollections.observableArrayList(list);
            table.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void goAjouter() { Navigator.goTo("AjouterProduit.fxml", "Ajouter Produit"); }
    @FXML private void goListe() { Navigator.goTo("ListeProduits.fxml", "Liste Produits"); }
    @FXML private void goDashboard() { Navigator.goTo("Dashboard.fxml", "Dashboard"); }
}