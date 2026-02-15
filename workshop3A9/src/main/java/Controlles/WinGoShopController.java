package Controlles;

import Entites.Produit;
import Entites.Profil;
import Entites.Utilisateur;
import Services.ProduitCRUD;
import Services.ProfilCRUD;
import Services.UtilisateurCRUD;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class WinGoShopController implements Initializable {

    // Cart item for display
    public static class CartItem {
        private final int produitId;
        private final String nom;
        private final double prix;
        private final IntegerProperty qty = new SimpleIntegerProperty(1);

        public CartItem(int produitId, String nom, double prix) {
            this.produitId = produitId;
            this.nom = nom;
            this.prix = prix;
        }
        public int getProduitId() { return produitId; }
        public String getNom() { return nom; }
        public double getPrix() { return prix; }
        public int getQty() { return qty.get(); }
        public void setQty(int q) { qty.set(q); }
        public double getSousTotal() { return prix * qty.get(); }
    }

    @FXML private TextField searchField;
    @FXML private Label cartCountLabel, loginStatusLabel, statusLabel, formTitleLabel, cartTotalLabel, profileStatusLabel;
    @FXML private StackPane screens;
    @FXML private VBox loginPane, productsPane, formPane, cartPane, profilePane;
    @FXML private TextArea profileBioField;
    @FXML private TextField profileImageField;
    @FXML private TextField emailField, passwordField, nomTextField, prixTextField, stockField;
    @FXML private TextField regionField, categorieField, imageField, idProduitHidden;
    @FXML private TextArea descriptionArea;
    @FXML private TableView<Produit> produitsTable;
    @FXML private TableColumn<Produit, Integer> colId;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, Integer> colStock;
    @FXML private TableColumn<Produit, String> colCat;
    @FXML private TableColumn<Produit, String> colRegion;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartColNom;
    @FXML private TableColumn<CartItem, Double> cartColPrix;
    @FXML private TableColumn<CartItem, Integer> cartColQty;
    @FXML private TableColumn<CartItem, Double> cartColSub;
    @FXML private Button dashboardButton, dashboardButtonLeft, dashboardButtonBottom;

    private ProduitCRUD produitCRUD = new ProduitCRUD();
    private UtilisateurCRUD userCRUD = new UtilisateurCRUD();
    private ProfilCRUD profilCRUD = new ProfilCRUD();
    private Utilisateur currentUser = null;
    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();
    private FilteredList<Produit> filteredProduits;
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colPrix.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrix()).asObject());
        colStock.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getStock()).asObject());
        colCat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategorie()));
        colRegion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegion()));

        cartColNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        cartColPrix.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrix()).asObject());
        cartColQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQty()).asObject());
        cartColSub.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSousTotal()).asObject());

        filteredProduits = new FilteredList<>(produitsList, p -> true);
        produitsTable.setItems(filteredProduits);
        cartTable.setItems(cartItems);

        if (searchField != null) searchField.textProperty().addListener((o, ov, nv) -> onSearch());
        if (dashboardButton != null) dashboardButton.managedProperty().bind(dashboardButton.visibleProperty());
        if (dashboardButtonLeft != null) dashboardButtonLeft.managedProperty().bind(dashboardButtonLeft.visibleProperty());
        if (dashboardButtonBottom != null) dashboardButtonBottom.managedProperty().bind(dashboardButtonBottom.visibleProperty());
        loadProduits();
        showLogin();
    }

    private void updateDashboardButtonVisibility() {
        boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getType());
        if (dashboardButton != null) dashboardButton.setVisible(isAdmin);
        if (dashboardButtonLeft != null) dashboardButtonLeft.setVisible(isAdmin);
        if (dashboardButtonBottom != null) dashboardButtonBottom.setVisible(isAdmin);
    }

    @FXML
    public void goToDashboard() {
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getType())) {
            loginStatusLabel.setText("Accès réservé aux administrateurs");
            loginStatusLabel.setStyle("-fx-text-fill: #FF0054;");
            showPane(loginPane);
            return;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AdminDashboard.fxml"));
            Stage stage = (Stage) (dashboardButton != null ? dashboardButton : dashboardButtonLeft).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            if (statusLabel != null) statusLabel.setText("Erreur chargement Dashboard");
        }
    }

    @FXML
    public void goToSignup() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Signup.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            if (loginStatusLabel != null) loginStatusLabel.setText("Erreur chargement inscription");
        }
    }

    private void loadProduits() {
        try {
            List<Produit> list = produitCRUD.afficher();
            produitsList.clear();
            produitsList.addAll(list);
        } catch (Exception e) {
            produitsList.clear();
        }
    }

    private void showPane(VBox pane) {
        loginPane.setVisible(pane == loginPane);
        productsPane.setVisible(pane == productsPane);
        formPane.setVisible(pane == formPane);
        cartPane.setVisible(pane == cartPane);
        profilePane.setVisible(pane == profilePane);
    }

    @FXML public void showLogin() { showPane(loginPane); }
    @FXML public void showProducts() { showPane(productsPane); loadProduits(); }
    @FXML public void showAddForm() {
        isEditMode = false;
        formTitleLabel.setText("➕ Ajouter Produit");
        clearForm();
        showPane(formPane);
    }
    @FXML public void showCart() {
        showPane(cartPane);
        updateCartTotal();
    }

    @FXML
    public void showProfile() {
        if (currentUser == null) {
            loginStatusLabel.setText("Connectez-vous pour modifier votre profil");
            loginStatusLabel.setStyle("-fx-text-fill: #FFBD00;");
            showPane(loginPane);
            return;
        }
        loadMyProfile();
        showPane(profilePane);
    }

    private void loadMyProfile() {
        try {
            Profil myProfil = null;
            for (Profil p : profilCRUD.afficher()) {
                if (p.getUtilisateurId() == currentUser.getId()) {
                    myProfil = p;
                    break;
                }
            }
            if (myProfil != null) {
                profileBioField.setText(myProfil.getBio() != null ? myProfil.getBio() : "");
                profileImageField.setText(myProfil.getImage() != null ? myProfil.getImage() : "");
            } else {
                profileBioField.clear();
                profileImageField.clear();
            }
            profileStatusLabel.setText("");
        } catch (Exception e) {
            profileStatusLabel.setText("⚠ Erreur chargement profil");
        }
    }

    @FXML
    public void saveProfile() {
        if (currentUser == null) {
            profileStatusLabel.setText("⚠ Connectez-vous d'abord");
            return;
        }
        try {
            String bio = profileBioField.getText() == null ? "" : profileBioField.getText().trim();
            String image = profileImageField.getText() == null ? "" : profileImageField.getText().trim();

            Profil existing = null;
            for (Profil p : profilCRUD.afficher()) {
                if (p.getUtilisateurId() == currentUser.getId()) {
                    existing = p;
                    break;
                }
            }
            if (existing != null) {
                existing.setBio(bio);
                existing.setImage(image);
                profilCRUD.modifier(existing);
                profileStatusLabel.setText("✅ Profil mis à jour !");
                profileStatusLabel.setStyle("-fx-text-fill: lightgreen;");
            } else {
                Profil p = new Profil(bio, image, currentUser.getId());
                profilCRUD.ajouter(p);
                profileStatusLabel.setText("✅ Profil créé !");
                profileStatusLabel.setStyle("-fx-text-fill: lightgreen;");
            }
        } catch (Exception e) {
            profileStatusLabel.setText("❌ Erreur: " + e.getMessage());
            profileStatusLabel.setStyle("-fx-text-fill: #FF0054;");
        }
    }

    @FXML
    public void doLogin() {
        String email = emailField.getText().trim();
        String pwd = passwordField.getText();
        if (email.isEmpty() || pwd.isEmpty()) {
            loginStatusLabel.setText("⚠ Remplis email et mot de passe");
            return;
        }
        try {
            for (Utilisateur u : userCRUD.afficher()) {
                if (u.getEmail().equals(email) && u.getMotDePasse().equals(pwd)) {
                    currentUser = u;
                    loginStatusLabel.setText("✅ Connecté !");
                    loginStatusLabel.setStyle("-fx-text-fill: lightgreen;");
                    updateDashboardButtonVisibility();
                    showProducts();
                    return;
                }
            }
            loginStatusLabel.setText("❌ Email ou mot de passe incorrect");
            loginStatusLabel.setStyle("-fx-text-fill: #FF0054;");
        } catch (Exception e) {
            loginStatusLabel.setText("⚠ Erreur connexion");
        }
    }

    @FXML public void onSearch() {
        String q = searchField == null || searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        filteredProduits.setPredicate(p ->
            (p.getNom() != null && p.getNom().toLowerCase().contains(q)) ||
            (p.getCategorie() != null && p.getCategorie().toLowerCase().contains(q)) ||
            (p.getRegion() != null && p.getRegion().toLowerCase().contains(q))
        );
    }

    @FXML
    public void addSelectedToCart() {
        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) {
            statusLabel.setText("⚠ Sélectionne un produit");
            return;
        }
        for (CartItem ci : cartItems) {
            if (ci.getProduitId() == p.getId()) {
                ci.setQty(ci.getQty() + 1);
                updateCartUI();
                return;
            }
        }
        cartItems.add(new CartItem(p.getId(), p.getNom(), p.getPrix()));
        updateCartUI();
        statusLabel.setText("✅ Ajouté au panier");
    }

    private void updateCartUI() {
        int total = 0;
        for (CartItem ci : cartItems) total += ci.getQty();
        cartCountLabel.setText(String.valueOf(total));
        cartTable.refresh();
    }

    private void updateCartTotal() {
        double tot = 0;
        for (CartItem ci : cartItems) tot += ci.getSousTotal();
        cartTotalLabel.setText(String.format("Total: %.2f TND", tot));
    }

    @FXML public void qtyMinus() {
        CartItem sel = cartTable.getSelectionModel().getSelectedItem();
        if (sel != null && sel.getQty() > 1) {
            sel.setQty(sel.getQty() - 1);
            cartTable.refresh();
            updateCartTotal();
            updateCartUI();
        }
    }

    @FXML public void qtyPlus() {
        CartItem sel = cartTable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.setQty(sel.getQty() + 1);
            cartTable.refresh();
            updateCartTotal();
            updateCartUI();
        }
    }

    @FXML public void removeFromCart() {
        CartItem sel = cartTable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            cartItems.remove(sel);
            updateCartUI();
            updateCartTotal();
        }
    }

    @FXML public void clearCart() {
        cartItems.clear();
        updateCartUI();
        updateCartTotal();
    }

    @FXML
    public void editSelectedProduct() {
        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) {
            statusLabel.setText("⚠ Sélectionne un produit");
            return;
        }
        isEditMode = true;
        formTitleLabel.setText("✏ Modifier Produit");
        idProduitHidden.setText(String.valueOf(p.getId()));
        nomTextField.setText(p.getNom());
        prixTextField.setText(String.valueOf(p.getPrix()));
        stockField.setText(String.valueOf(p.getStock()));
        regionField.setText(p.getRegion() != null ? p.getRegion() : "");
        categorieField.setText(p.getCategorie() != null ? p.getCategorie() : "");
        descriptionArea.setText(p.getDescription() != null ? p.getDescription() : "");
        imageField.setText(p.getImage() != null ? p.getImage() : "");
        showPane(formPane);
    }

    @FXML
    public void deleteSelectedProduct() {
        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) {
            statusLabel.setText("⚠ Sélectionne un produit");
            return;
        }
        try {
            produitCRUD.supprimer(p.getId());
            loadProduits();
            statusLabel.setText("✅ Supprimé");
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur suppression");
        }
    }

    @FXML
    public void saveProduct() {
        String nom = nomTextField.getText().trim();
        if (nom.isEmpty()) {
            statusLabel.setText("⚠ Nom requis");
            return;
        }
        String prixStr = prixTextField.getText().trim();
        String stockStr = stockField.getText().trim();
        if (prixStr.isEmpty() || stockStr.isEmpty()) {
            statusLabel.setText("⚠ Prix et Stock requis");
            return;
        }
        double prix;
        int stock;
        try {
            prix = Double.parseDouble(prixStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            statusLabel.setText("⚠ Prix et Stock doivent être des nombres valides");
            return;
        }
        try {
            if (isEditMode) {
                Produit p = new Produit();
                p.setId(Integer.parseInt(idProduitHidden.getText()));
                p.setNom(nom);
                p.setPrix(prix);
                p.setStock(stock);
                p.setRegion(regionField.getText().trim());
                p.setCategorie(categorieField.getText().trim());
                p.setDescription(descriptionArea.getText().trim());
                p.setImage(imageField.getText().trim());
                produitCRUD.modifier(p);
                statusLabel.setText("✅ Modifié");
            } else {
                Produit p = new Produit(
                    nom, prix, stock,
                    categorieField.getText().trim(),
                    regionField.getText().trim(),
                    descriptionArea.getText().trim(),
                    imageField.getText().trim()
                );
                produitCRUD.ajouter(p);
                statusLabel.setText("✅ Ajouté");
            }
            showProducts();
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: " + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        idProduitHidden.clear();
        nomTextField.clear();
        prixTextField.clear();
        stockField.clear();
        regionField.clear();
        categorieField.clear();
        descriptionArea.clear();
        imageField.clear();
    }
}
