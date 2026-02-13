package Controlles;

import Entites.Produit;
import Services.ProduitCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class WinGoShopController {

    // Screens
    @FXML private VBox loginPane;
    @FXML private VBox productsPane;
    @FXML private VBox formPane;
    @FXML private VBox cartPane;

    // Top
    @FXML private TextField searchField;
    @FXML private Label cartCountLabel;

    // Login
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label loginStatusLabel;

    // Products table
    @FXML private TableView<Produit> produitsTable;
    @FXML private TableColumn<Produit, Integer> colId;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, Integer> colStock;
    @FXML private TableColumn<Produit, String> colCat;
    @FXML private TableColumn<Produit, String> colRegion;

    @FXML private Label statusLabel;

    // Form fields (same as your AjouterProduit)
    @FXML private TextField idProduitHidden;     // for edit
    @FXML private TextField idCommercantField;
    @FXML private TextField nomTextField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField prixTextField;
    @FXML private TextField stockField;
    @FXML private TextField regionField;
    @FXML private TextField categorieField;
    @FXML private TextField imageField;
    @FXML private Label formTitleLabel;
    @FXML private Button saveBtn;

    // Cart
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartColNom;
    @FXML private TableColumn<CartItem, Double> cartColPrix;
    @FXML private TableColumn<CartItem, Integer> cartColQty;
    @FXML private TableColumn<CartItem, Double> cartColSub;
    @FXML private Label cartTotalLabel;

    private final ProduitCRUD produitCRUD = new ProduitCRUD();
    private final ObservableList<Produit> produitsData = FXCollections.observableArrayList();

    private final CartService cartService = new CartService();
    private final ObservableList<CartItem> cartData = FXCollections.observableArrayList();

    private boolean loggedIn = false; // simple flag

    @FXML
    public void initialize() {
        setupProductsTable();
        setupCartTable();

        refreshProducts();
        refreshCartUI();

        // start screen
        showProducts();
    }

    // ------------------ NAVIGATION (ONE FXML) ------------------
    private void showOnly(VBox pane) {
        loginPane.setVisible(false);  loginPane.setManaged(false);
        productsPane.setVisible(false); productsPane.setManaged(false);
        formPane.setVisible(false); formPane.setManaged(false);
        cartPane.setVisible(false); cartPane.setManaged(false);

        pane.setVisible(true);
        pane.setManaged(true);
    }

    @FXML public void showLogin() { showOnly(loginPane); }
    @FXML public void showProducts() { showOnly(productsPane); }
    @FXML public void showCart() { showOnly(cartPane); refreshCartUI(); }

    @FXML
    public void showAddForm() {
        if (!loggedIn) { // tu peux enlever ça si tu veux ajouter sans login
            statusLabel.setText("⚠ Login requis pour ajouter/modifier.");
            showLogin();
            return;
        }
        clearForm(null);
        formTitleLabel.setText("➕ Ajouter Produit");
        saveBtn.setText("✅ Enregistrer");
        showOnly(formPane);
    }

    // ------------------ LOGIN (simple) ------------------
    @FXML
    public void doLogin() {
        String email = emailField.getText().trim();
        String pass = passwordField.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            loginStatusLabel.setText("⚠ Remplis email + mot de passe.");
            return;
        }

        // ✅ Ici tu branches ta vraie vérification DB.
        // Pour l'instant: accepte n'importe quoi (tu remplaces après par AuthService)
        loggedIn = true;
        loginStatusLabel.setText("✅ Connecté.");
        showProducts();
    }

    // ------------------ PRODUCTS ------------------
    private void setupProductsTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCat.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colRegion.setCellValueFactory(new PropertyValueFactory<>("region"));

        produitsTable.setItems(produitsData);
    }

    private void refreshProducts() {
        try {
            List<Produit> list = produitCRUD.afficher();
            produitsData.setAll(list);
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onSearch() {
        String q = (searchField == null) ? "" : searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            refreshProducts();
            return;
        }
        // simple filter in-memory
        ObservableList<Produit> filtered = FXCollections.observableArrayList();
        for (Produit p : produitsData) {
            if ((p.getNom() != null && p.getNom().toLowerCase().contains(q))
                    || (p.getCategorie() != null && p.getCategorie().toLowerCase().contains(q))
                    || (p.getRegion() != null && p.getRegion().toLowerCase().contains(q))) {
                filtered.add(p);
            }
        }
        produitsTable.setItems(filtered);
    }

    @FXML
    public void addSelectedToCart() {
        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) { statusLabel.setText("⚠ Sélectionne un produit."); return; }
        if (p.getStock() <= 0) { statusLabel.setText("⚠ Stock vide."); return; }

        cartService.add(p, 1);
        refreshCartUI();
        statusLabel.setText("✅ Ajouté au panier: " + p.getNom());
    }

    @FXML
    public void editSelectedProduct() {
        if (!loggedIn) { statusLabel.setText("⚠ Login requis pour modifier."); showLogin(); return; }

        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) { statusLabel.setText("⚠ Sélectionne un produit."); return; }

        // fill form
        idProduitHidden.setText(String.valueOf(p.getIdProduit()));
        idCommercantField.setText(String.valueOf(p.getIdCommercant()));
        nomTextField.setText(p.getNom());
        descriptionArea.setText(p.getDescription() == null ? "" : p.getDescription());
        prixTextField.setText(String.valueOf(p.getPrix()));
        stockField.setText(String.valueOf(p.getStock()));
        regionField.setText(p.getRegion() == null ? "" : p.getRegion());
        categorieField.setText(p.getCategorie() == null ? "" : p.getCategorie());
        imageField.setText(p.getImage() == null ? "" : p.getImage());

        formTitleLabel.setText("✏ Modifier Produit");
        saveBtn.setText("💾 Mettre à jour");
        showOnly(formPane);
    }

    @FXML
    public void deleteSelectedProduct() {
        if (!loggedIn) { statusLabel.setText("⚠ Login requis pour supprimer."); showLogin(); return; }

        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) { statusLabel.setText("⚠ Sélectionne un produit."); return; }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer: " + p.getNom() + " ?", ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.showAndWait();

        if (a.getResult() != ButtonType.YES) return;

        try {
            produitCRUD.supprimer(p.getIdProduit());
            statusLabel.setText("✅ Supprimé.");
            refreshProducts();
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------------------ SAVE (ADD / UPDATE) ------------------
    @FXML
    public void saveProduct() {
        if (!loggedIn) { statusLabel.setText("⚠ Login requis."); showLogin(); return; }

        try {
            Produit p = buildProduitFromForm();

            boolean isEdit = idProduitHidden.getText() != null && !idProduitHidden.getText().isBlank();
            if (isEdit) {
                p.setIdProduit(Integer.parseInt(idProduitHidden.getText().trim()));
                produitCRUD.modifier(p);
                statusLabel.setText("✅ Produit modifié.");
            } else {
                produitCRUD.ajouter(p);
                statusLabel.setText("✅ Produit ajouté.");
            }

            refreshProducts();
            showProducts();

        } catch (IllegalArgumentException ex) {
            statusLabel.setText("⚠ " + ex.getMessage());
        } catch (SQLException ex) {
            statusLabel.setText("❌ Erreur DB: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Produit buildProduitFromForm() {
        String idCom = idCommercantField.getText().trim();
        String nom = nomTextField.getText().trim();
        String prixS = prixTextField.getText().trim().replace(",", ".");
        String stockS = stockField.getText().trim();

        if (idCom.isEmpty() || nom.isEmpty() || prixS.isEmpty() || stockS.isEmpty())
            throw new IllegalArgumentException("Champs obligatoires: idCommercant, nom, prix, stock.");

        int idCommercant = Integer.parseInt(idCom);
        double prix = Double.parseDouble(prixS);
        int stock = Integer.parseInt(stockS);

        Produit p = new Produit();
        p.setIdCommercant(idCommercant);
        p.setNom(nom);
        p.setPrix(prix);
        p.setStock(stock);

        String desc = descriptionArea.getText().trim();
        String region = regionField.getText().trim();
        String cat = categorieField.getText().trim();
        String img = imageField.getText().trim();

        p.setDescription(desc.isEmpty() ? null : desc);
        p.setRegion(region.isEmpty() ? null : region);
        p.setCategorie(cat.isEmpty() ? null : cat);
        p.setImage(img.isEmpty() ? null : img);

        return p;
    }

    @FXML
    public void clearForm(javafx.event.ActionEvent e) {
        idProduitHidden.clear();
        idCommercantField.clear();
        nomTextField.clear();
        descriptionArea.clear();
        prixTextField.clear();
        stockField.clear();
        regionField.clear();
        categorieField.clear();
        imageField.clear();
    }

    // ------------------ CART ------------------
    private void setupCartTable() {
        cartColNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        cartColPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        cartColQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        cartColSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        cartTable.setItems(cartData);
    }

    private void refreshCartUI() {
        cartData.setAll(cartService.getItems());
        cartCountLabel.setText(String.valueOf(cartService.totalQty()));
        cartTotalLabel.setText(String.format("Total: %.2f TND", cartService.totalPrice()));
    }

    @FXML
    public void qtyPlus() {
        CartItem it = cartTable.getSelectionModel().getSelectedItem();
        if (it == null) return;
        cartService.changeQty(it.getIdProduit(), +1);
        refreshCartUI();
    }

    @FXML
    public void qtyMinus() {
        CartItem it = cartTable.getSelectionModel().getSelectedItem();
        if (it == null) return;
        cartService.changeQty(it.getIdProduit(), -1);
        refreshCartUI();
    }

    @FXML
    public void removeFromCart() {
        CartItem it = cartTable.getSelectionModel().getSelectedItem();
        if (it == null) return;
        cartService.remove(it.getIdProduit());
        refreshCartUI();
    }

    @FXML
    public void clearCart() {
        cartService.clear();
        refreshCartUI();
    }
}