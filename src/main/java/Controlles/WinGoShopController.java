package Controlles;

import Entites.Produit;
import Services.PanierCRUD;
import Services.ProduitCRUD;
import Utils.MyBD;
import Utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableCell;
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
    @FXML private TableColumn<Produit, String> colImage;
    @FXML private Label statusLabel;

    // Form fields (ADD / EDIT)
    @FXML private TextField idProduitHidden;     // for edit
    @FXML private TextField nomTextField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField prixTextField;
    @FXML private TextField stockField;
    @FXML private TextField regionField;
    @FXML private TextField categorieField;
    @FXML private TextField imageField;
    @FXML private Label formTitleLabel;
    @FXML private Button saveBtn;

    // ✅ Welcome label (dans formPane)
    @FXML private Label welcomeLabel;

    // Cart
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartColNom;
    @FXML private TableColumn<CartItem, Double> cartColPrix;
    @FXML private TableColumn<CartItem, Integer> cartColQty;
    @FXML private TableColumn<CartItem, Double> cartColSub;
    @FXML private Label cartTotalLabel;

    private final ProduitCRUD produitCRUD = new ProduitCRUD();
    private final PanierCRUD panierCRUD = new PanierCRUD();

    private final ObservableList<Produit> produitsData = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cartData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupProductsTable();
        setupCartTable();

        refreshProducts();
        refreshCartUI();

        showProducts();
    }

    // ------------------ NAVIGATION ------------------
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

    private boolean canManageProducts() {
        return Session.isLoggedIn() && Session.isCommercant();
    }

    @FXML
    public void showAddForm() {
        if (!canManageProducts()) {
            statusLabel.setText("⚠ Login commerçant requis pour ajouter/modifier.");
            showLogin();
            return;
        }

        // ✅ Welcome message
        String fullName = getNomPrenomUtilisateur(Session.getUserId());
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue " + fullName + " 👋  •  Tu es commerçant ✅");
        }

        clearForm(null);
        formTitleLabel.setText("➕ Ajouter Produit");
        saveBtn.setText("✅ Enregistrer");
        showOnly(formPane);
    }

    // ------------------ LOGIN ------------------
    @FXML
    public void doLogin() {
        String email = emailField.getText().trim();
        String pass = passwordField.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            loginStatusLabel.setText("⚠ Remplis email + mot de passe.");
            return;
        }

        // ✅ TEMPORAIRE: on fixe un userId existant dans DB
        // Change fakeId selon un id موجود في utilisateur
        int fakeId = 1;
        String fakeType = email.toLowerCase().contains("shop") ? "COMMERCANT" : "CLIENT";

        Session.setUser(fakeId, fakeType);

        loginStatusLabel.setText("✅ Connecté (" + fakeType + ").");
        refreshCartUI();
        showProducts();
    }

    // ✅ Récupérer nom/prenom depuis DB
    private String getNomPrenomUtilisateur(int idUser) {
        String sql = "SELECT nom, prenom FROM utilisateur WHERE id=?";
        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, idUser);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String full = (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
                    full = full.trim();
                    return full.isEmpty() ? "Utilisateur" : full;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Utilisateur";
    }

    // ------------------ PRODUCTS ------------------
    private void setupProductsTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCat.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colRegion.setCellValueFactory(new PropertyValueFactory<>("region"));

        // ✅ URL -> ImageView
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();

            {
                iv.setFitWidth(60);
                iv.setFitHeight(60);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
            }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);

                if (empty || url == null || url.isBlank()) {
                    setGraphic(null);
                } else {
                    try {
                        // backgroundLoading=true ✅
                        iv.setImage(new Image(url.trim(), true));
                        setGraphic(iv);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
                setText(null);
            }
        });

        produitsTable.setItems(produitsData);
    }

    private void refreshProducts() {
        try {
            List<Produit> list = produitCRUD.afficher();
            produitsData.setAll(list);
            produitsTable.setItems(produitsData);
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onSearch() {
        String q = (searchField == null) ? "" : searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            produitsTable.setItems(produitsData);
            return;
        }
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

    // ✅ AJOUT AU PANIER (DB)
    @FXML
    public void addSelectedToCart() {
        if (!Session.isLoggedIn()) {
            statusLabel.setText("⚠ Connecte-toi pour utiliser le panier.");
            showLogin();
            return;
        }

        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) { statusLabel.setText("⚠ Sélectionne un produit."); return; }
        if (p.getStock() <= 0) { statusLabel.setText("⚠ Stock vide."); return; }

        try {
            panierCRUD.addToCart(Session.getUserId(), p.getIdProduit(), p.getPrix(), 1);
            refreshCartUI();
            statusLabel.setText("✅ Ajouté au panier: " + p.getNom());
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur panier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void editSelectedProduct() {
        if (!canManageProducts()) {
            statusLabel.setText("⚠ Login commerçant requis pour modifier.");
            showLogin();
            return;
        }

        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) { statusLabel.setText("⚠ Sélectionne un produit."); return; }

        if (p.getIdUser() != Session.getUserId()) {
            statusLabel.setText("⚠ Tu ne peux pas modifier le produit d’un autre vendeur.");
            return;
        }

        idProduitHidden.setText(String.valueOf(p.getIdProduit()));
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
        if (!canManageProducts()) {
            statusLabel.setText("⚠ Login commerçant requis pour supprimer.");
            showLogin();
            return;
        }

        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) { statusLabel.setText("⚠ Sélectionne un produit."); return; }

        if (p.getIdUser() != Session.getUserId()) {
            statusLabel.setText("⚠ Tu ne peux pas supprimer le produit d’un autre vendeur.");
            return;
        }

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
        if (!canManageProducts()) {
            statusLabel.setText("⚠ Login commerçant requis.");
            showLogin();
            return;
        }

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
        String nom = nomTextField.getText().trim();
        String prixS = prixTextField.getText().trim().replace(",", ".");
        String stockS = stockField.getText().trim();

        if (nom.isEmpty() || prixS.isEmpty() || stockS.isEmpty())
            throw new IllegalArgumentException("Champs obligatoires: nom, prix, stock.");

        double prix = Double.parseDouble(prixS);
        int stock = Integer.parseInt(stockS);

        Produit p = new Produit();
        p.setIdUser(Session.getUserId());
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
        nomTextField.clear();
        descriptionArea.clear();
        prixTextField.clear();
        stockField.clear();
        regionField.clear();
        categorieField.clear();
        imageField.clear();
    }

    // ------------------ CART (DB) ------------------
    private void setupCartTable() {
        cartColNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        cartColPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        cartColQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        cartColSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        cartTable.setItems(cartData);
    }

    private void refreshCartUI() {
        if (!Session.isLoggedIn()) {
            cartData.clear();
            cartCountLabel.setText("0");
            cartTotalLabel.setText("Total: 0.00 TND");
            return;
        }

        try {
            cartData.setAll(panierCRUD.getActiveCart(Session.getUserId()));

            int totalQty = cartData.stream().mapToInt(CartItem::getQty).sum();
            double total = cartData.stream().mapToDouble(CartItem::getSubtotal).sum();

            cartCountLabel.setText(String.valueOf(totalQty));
            cartTotalLabel.setText(String.format("Total: %.2f TND", total));
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur panier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML public void qtyPlus() {
        CartItem it = cartTable.getSelectionModel().getSelectedItem();
        if (it == null) return;

        try {
            panierCRUD.changeQty(Session.getUserId(), it.getIdProduit(), +1);
            refreshCartUI();
        } catch (SQLException e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }

    @FXML public void qtyMinus() {
        CartItem it = cartTable.getSelectionModel().getSelectedItem();
        if (it == null) return;

        try {
            panierCRUD.changeQty(Session.getUserId(), it.getIdProduit(), -1);
            refreshCartUI();
        } catch (SQLException e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }

    @FXML public void removeFromCart() {
        CartItem it = cartTable.getSelectionModel().getSelectedItem();
        if (it == null) return;

        try {
            panierCRUD.remove(Session.getUserId(), it.getIdProduit());
            refreshCartUI();
        } catch (SQLException e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }

    @FXML public void clearCart() {
        try {
            panierCRUD.clear(Session.getUserId());
            refreshCartUI();
        } catch (SQLException e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }
}