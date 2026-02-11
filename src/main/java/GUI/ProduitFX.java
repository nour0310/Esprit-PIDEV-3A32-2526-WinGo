package GUI;

import Entites.Commercant;
import Entites.Produit;
import Services.CommercantCRUD;
import Services.ProduitCRUD;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class ProduitFX extends Application {

    private TableView<Produit> table;

    // ✅ Commerçant = ComboBox (pas TextField)
    private ComboBox<Commercant> commercantCombo;

    private TextField nomField, descriptionField, prixField,
            regionField, categorieField, stockField, imageField;

    private ProduitCRUD service;
    private CommercantCRUD commercantService;

    @Override
    public void start(Stage primaryStage) {
        service = new ProduitCRUD();
        commercantService = new CommercantCRUD();

        // --- TableView ---
        table = new TableView<>();

        TableColumn<Produit, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("idProduit"));

        TableColumn<Produit, Integer> commercantCol = new TableColumn<>("Commerçant");
        commercantCol.setCellValueFactory(new PropertyValueFactory<>("idCommercant"));

        TableColumn<Produit, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Produit, String> catCol = new TableColumn<>("Catégorie");
        catCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        TableColumn<Produit, Double> prixCol = new TableColumn<>("Prix");
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));

        TableColumn<Produit, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        TableColumn<Produit, String> regionCol = new TableColumn<>("Région");
        regionCol.setCellValueFactory(new PropertyValueFactory<>("region"));

        table.getColumns().addAll(idCol, commercantCol, nomCol, catCol, prixCol, stockCol, regionCol);
        table.setPrefHeight(250);

        // --- Form Fields ---
        commercantCombo = new ComboBox<>();
        commercantCombo.setPromptText("Choisir un commerçant");

        // Charger commerçants depuis MySQL
        loadCommercants();

        nomField = new TextField();
        nomField.setPromptText("Nom");

        descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        prixField = new TextField();
        prixField.setPromptText("Prix (ex: 15.5)");

        regionField = new TextField();
        regionField.setPromptText("Région");

        categorieField = new TextField();
        categorieField.setPromptText("Catégorie");

        stockField = new TextField();
        stockField.setPromptText("Stock (ex: 10)");

        imageField = new TextField();
        imageField.setPromptText("Image (ex: mug.jpg)");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        form.add(new Label("Commerçant:"), 0, 0);
        form.add(commercantCombo, 1, 0);

        form.add(new Label("Nom:"), 0, 1);
        form.add(nomField, 1, 1);

        form.add(new Label("Description:"), 0, 2);
        form.add(descriptionField, 1, 2);

        form.add(new Label("Prix:"), 0, 3);
        form.add(prixField, 1, 3);

        form.add(new Label("Région:"), 0, 4);
        form.add(regionField, 1, 4);

        form.add(new Label("Catégorie:"), 0, 5);
        form.add(categorieField, 1, 5);

        form.add(new Label("Stock:"), 0, 6);
        form.add(stockField, 1, 6);

        form.add(new Label("Image:"), 0, 7);
        form.add(imageField, 1, 7);

        // --- Buttons ---
        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button refreshBtn = new Button("Refresh");

        HBox buttons = new HBox(10, addBtn, updateBtn, deleteBtn, refreshBtn);
        buttons.setPadding(new Insets(10));

        // --- Layout ---
        VBox root = new VBox(10, table, form, buttons);
        root.setPadding(new Insets(10));

        // --- Load initial data ---
        loadData();

        // --- Event Handlers ---
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                // sélectionner commerçant dans la combo
                for (Commercant c : commercantCombo.getItems()) {
                    if (c.getIdCommercant() == newSel.getIdCommercant()) {
                        commercantCombo.getSelectionModel().select(c);
                        break;
                    }
                }

                nomField.setText(newSel.getNom());
                descriptionField.setText(newSel.getDescription());
                prixField.setText(String.valueOf(newSel.getPrix()));
                regionField.setText(newSel.getRegion());
                categorieField.setText(newSel.getCategorie());
                stockField.setText(String.valueOf(newSel.getStock()));
                imageField.setText(newSel.getImage());
            }
        });

        addBtn.setOnAction(e -> addProduit());
        updateBtn.setOnAction(e -> updateProduit());
        deleteBtn.setOnAction(e -> deleteProduit());
        refreshBtn.setOnAction(e -> {
            loadCommercants();
            loadData();
        });

        // --- Scene ---
        Scene scene = new Scene(root, 900, 550);
        primaryStage.setTitle("Produits Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadCommercants() {
        try {
            List<Commercant> list = commercantService.afficher();
            commercantCombo.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les commerçants : " + e.getMessage());
        }
    }

    // --- Methods ---
    private void loadData() {
        try {
            List<Produit> produits = service.afficher();
            ObservableList<Produit> data = FXCollections.observableArrayList(produits);
            table.setItems(data);
        } catch (SQLException e) {
            showError("Error loading data", e.getMessage());
        }
    }

    private void addProduit() {
        try {
            Produit p = readFormToProduit(null);
            service.ajouter(p);
            loadData();
            clearForm();
        } catch (IllegalArgumentException ex) {
            showError("Validation", ex.getMessage());
        } catch (SQLException e) {
            showError("Error adding produit", e.getMessage());
        }
    }

    private void updateProduit() {
        Produit selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Veuillez sélectionner un produit à modifier.");
            return;
        }

        try {
            Produit p = readFormToProduit(selected.getIdProduit());
            service.modifier(p);
            loadData();
            clearForm();
        } catch (IllegalArgumentException ex) {
            showError("Validation", ex.getMessage());
        } catch (SQLException e) {
            showError("Error updating produit", e.getMessage());
        }
    }

    private void deleteProduit() {
        Produit selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Veuillez sélectionner un produit à supprimer.");
            return;
        }

        try {
            service.supprimer(selected.getIdProduit());
            loadData();
            clearForm();
        } catch (SQLException e) {
            showError("Error deleting produit", e.getMessage());
        }
    }

    private Produit readFormToProduit(Integer idProduit) {
        Commercant selectedC = commercantCombo.getSelectionModel().getSelectedItem();
        if (selectedC == null) throw new IllegalArgumentException("Veuillez choisir un commerçant.");

        if (nomField.getText().trim().isEmpty()) throw new IllegalArgumentException("Nom obligatoire.");

        int stock;
        double prix;

        try { prix = Double.parseDouble(prixField.getText().trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Prix invalide (ex: 15.5)."); }

        try { stock = Integer.parseInt(stockField.getText().trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Stock invalide (ex: 10)."); }

        Produit p = new Produit();
        if (idProduit != null) p.setIdProduit(idProduit);

        p.setIdCommercant(selectedC.getIdCommercant());
        p.setNom(nomField.getText().trim());
        p.setDescription(descriptionField.getText().trim());
        p.setPrix(prix);
        p.setRegion(regionField.getText().trim());
        p.setCategorie(categorieField.getText().trim());
        p.setStock(stock);
        p.setImage(imageField.getText().trim());

        return p;
    }

    private void clearForm() {
        commercantCombo.getSelectionModel().clearSelection();
        nomField.clear();
        descriptionField.clear();
        prixField.clear();
        regionField.clear();
        categorieField.clear();
        stockField.clear();
        imageField.clear();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}