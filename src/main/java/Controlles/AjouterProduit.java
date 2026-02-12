package Controlles;

import Entites.Produit;
import Services.ProduitCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AjouterProduit {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    // Champs (doivent matcher les fx:id du FXML)
    @FXML private TextField idCommercantField;
    @FXML private TextField nomTextField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField prixTextField;
    @FXML private TextField stockField;
    @FXML private TextField regionField;
    @FXML private TextField categorieField;
    @FXML private TextField imageField;
    @FXML private Label statusLabel;

    private ProduitCRUD produitCRUD;

    @FXML
    public void initialize() {
        produitCRUD = new ProduitCRUD();
        setupLiveValidation();
    }
    private void setupLiveValidation() {

        // listeners live
        idCommercantField.textProperty().addListener((obs, oldV, newV) -> validateIdCommercant());
        nomTextField.textProperty().addListener((obs, oldV, newV) -> validateNom());
        prixTextField.textProperty().addListener((obs, oldV, newV) -> validatePrix());
        stockField.textProperty().addListener((obs, oldV, newV) -> validateStock());

        // optionnel: validation simple (pas obligatoire)
        regionField.textProperty().addListener((obs, o, n) -> clearStatusIfOk());
        categorieField.textProperty().addListener((obs, o, n) -> clearStatusIfOk());
        imageField.textProperty().addListener((obs, o, n) -> clearStatusIfOk());
        if (descriptionArea != null)
            descriptionArea.textProperty().addListener((obs, o, n) -> clearStatusIfOk());
        private boolean validateIdCommercant() {
            String s = idCommercantField.getText().trim();
            if (s.isEmpty()) return setOk(idCommercantField); // vide = pas d’erreur immédiate

            try {
                int v = Integer.parseInt(s);
                if (v <= 0) return setError(idCommercantField, "ID commerçant invalide.");
                return setOk(idCommercantField);
            } catch (Exception e) {
                return setError(idCommercantField, "ID commerçant invalide.");
            }
        }

        private boolean validateNom() {
            String s = nomTextField.getText().trim();
            if (s.isEmpty()) return setOk(nomTextField);
            if (s.length() > 150) return setError(nomTextField, "Nom trop long (max 150).");
            return setOk(nomTextField);
        }

        private boolean validatePrix() {
            String s = prixTextField.getText().trim();
            if (s.isEmpty()) return setOk(prixTextField);

            try {
                double v = Double.parseDouble(s.replace(",", "."));
                if (v <= 0) return setError(prixTextField, "Prix invalide (ex: 15.50).");
                return setOk(prixTextField);
            } catch (Exception e) {
                return setError(prixTextField, "Prix invalide (ex: 15.50).");
            }
        }

        private boolean validateStock() {
            String s = stockField.getText().trim();
            if (s.isEmpty()) return setOk(stockField);

            try {
                int v = Integer.parseInt(s);
                if (v < 0) return setError(stockField, "Stock invalide (>= 0).");
                return setOk(stockField);
            } catch (Exception e) {
                return setError(stockField, "Stock invalide (>= 0).");
            }
        }
        private boolean setError(Control field, String msg) {
            field.setStyle(field.getStyle() + "; -fx-border-color: #FF5400; -fx-border-width: 2;");
            if (statusLabel != null) statusLabel.setText("⚠ " + msg);
            return false;
        }

        private boolean setOk(Control field) {
            // retire bordure rouge
            field.setStyle(field.getStyle().replaceAll("(?i)-fx-border-color:\\s*#FF5400;\\s*-fx-border-width:\\s*2;?", ""));
            return true;
        }

        private void clearStatusIfOk() {
            // si les champs principaux sont OK, on efface le message
            boolean ok = validateIdCommercant() & validateNom() & validatePrix() & validateStock();
            if (ok && statusLabel != null) statusLabel.setText("");
        }
        boolean ok = validateIdCommercant() & validateNom() & validatePrix() & validateStock();
        if (!ok) return;
    }

    @FXML
    void ajouterProduit(ActionEvent event) {
        try {
            Produit produit = validateAndBuildProduit();
            produitCRUD.ajouter(produit);

            statusLabel.setText("✅ Produit ajouté dans la base !");
            clearForm(null);

        } catch (IllegalArgumentException ex) {
            statusLabel.setText("⚠ " + ex.getMessage());
        } catch (SQLException ex) {
            statusLabel.setText("❌ Erreur DB: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Produit validateAndBuildProduit() {

        // id_commercant (obligatoire)
        int idCommercant;
        try {
            idCommercant = Integer.parseInt(idCommercantField.getText().trim());
            if (idCommercant <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            throw new IllegalArgumentException("ID commerçant invalide.");
        }

        // nom (obligatoire)
        String nom = nomTextField.getText().trim();
        if (nom.isEmpty()) throw new IllegalArgumentException("Nom obligatoire.");
        if (nom.length() > 150) throw new IllegalArgumentException("Nom trop long (max 150).");

        // prix (obligatoire)
        double prix;
        try {
            prix = Double.parseDouble(prixTextField.getText().trim().replace(",", "."));
            if (prix <= 0) throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("Prix invalide (ex: 15.50).");
        }

        // stock (obligatoire)
        int stock;
        try {
            stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("Stock invalide (>= 0).");
        }

        // optionnels
        String description = (descriptionArea == null) ? "" : descriptionArea.getText().trim();
        String region = regionField.getText().trim();
        String categorie = categorieField.getText().trim();
        String image = imageField.getText().trim();

        Produit p = new Produit();
        p.setIdCommercant(idCommercant);
        p.setNom(nom);
        p.setDescription(description.isEmpty() ? null : description);
        p.setPrix(prix);
        p.setStock(stock);
        p.setRegion(region.isEmpty() ? null : region);
        p.setCategorie(categorie.isEmpty() ? null : categorie);
        p.setImage(image.isEmpty() ? null : image);

        return p;
    }

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

    // Navigation
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