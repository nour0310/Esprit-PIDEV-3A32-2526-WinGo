// FILE: src/main/java/Controlles/WinGoShopController.java
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
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    @FXML private TableColumn<Produit, String> colImage;
    @FXML private TableColumn<Produit, Integer> colId;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, Integer> colStock;
    @FXML private TableColumn<Produit, String> colCat;
    @FXML private TableColumn<Produit, String> colRegion;
    @FXML private Label statusLabel;

    // Form fields (ADD / EDIT)
    @FXML private TextField idProduitHidden;     // for edit (hidden)
    @FXML private TextField nomTextField;
    @FXML private TextField descriptionField;   // ✅ dans ton FXML tu utilises descriptionField (TextField)
    @FXML private TextField prixTextField;
    @FXML private TextField stockField;
    @FXML private TextField regionField;
    @FXML private TextField categorieField;
    @FXML private TextField imageField;
    @FXML private Label formTitleLabel;
    @FXML private Button saveBtn;

    // Welcome label (optionnel)
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
        loginPane.setVisible(false);     loginPane.setManaged(false);
        productsPane.setVisible(false);  productsPane.setManaged(false);
        formPane.setVisible(false);      formPane.setManaged(false);
        cartPane.setVisible(false);      cartPane.setManaged(false);

        pane.setVisible(true);
        pane.setManaged(true);
    }

    @FXML public void showLogin()    { showOnly(loginPane); }
    @FXML public void showProducts() { showOnly(productsPane); }
    @FXML public void showCart()     { showOnly(cartPane); refreshCartUI(); }

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
        if (welcomeLabel != null) {
            String fullName = getNomPrenomUtilisateur(Session.getUserId());
            welcomeLabel.setText("Bienvenue " + fullName + " 👋  •  Tu es commerçant ✅");
        }

        clearForm();
        formTitleLabel.setText("➕ Ajouter Produit");
        saveBtn.setText("✅ Enregistrer");
        showOnly(formPane);
    }

    // ------------------ LOGIN ------------------
    @FXML
    public void doLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass  = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            loginStatusLabel.setText("⚠ Remplis email + mot de passe.");
            return;
        }

        // ⚠️ TEMP: tu remplaces après par vrai login DB
        int fakeId = 1; // <-- mets un id موجود في utilisateur
        String fakeType = email.toLowerCase().contains("shop") ? "COMMERCANT" : "CLIENT";

        Session.setUser(fakeId, fakeType);

        loginStatusLabel.setText("✅ Connecté (" + fakeType + ").");
        refreshCartUI();
        showProducts();
    }

    // ✅ IMPORTANT: ne JAMAIS fermer la connexion globale MyBD ici (sinon "connection closed")
    private String getNomPrenomUtilisateur(int idUser) {
        String sql = "SELECT nom, prenom FROM utilisateur WHERE id=?";
        Connection conn = MyBD.getInstance().getConn();

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
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

        // ✅ Image column (URL -> ImageView)
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
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onSearch() {
        String q = (searchField == null || searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();
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
        nomTextField.setText(nullSafe(p.getNom()));
        descriptionField.setText(nullSafe(p.getDescription())); // ✅ plus de descriptionArea
        prixTextField.setText(String.valueOf(p.getPrix()));
        stockField.setText(String.valueOf(p.getStock()));
        regionField.setText(nullSafe(p.getRegion()));
        categorieField.setText(nullSafe(p.getCategorie()));
        imageField.setText(nullSafe(p.getImage()));

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

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer: " + p.getNom() + " ?",
                ButtonType.YES, ButtonType.NO);
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
        String nom = safeText(nomTextField);
        String prixStr = safeText(prixTextField);
        String stockStr = safeText(stockField);

        if (nom.isEmpty()) throw new IllegalArgumentException("Nom obligatoire.");
        if (prixStr.isEmpty()) throw new IllegalArgumentException("Prix obligatoire.");
        if (stockStr.isEmpty()) throw new IllegalArgumentException("Stock obligatoire.");

        double prix;
        int stock;

        try { prix = Double.parseDouble(prixStr); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Prix invalide."); }

        try { stock = Integer.parseInt(stockStr); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Stock invalide."); }

        if (prix < 0) throw new IllegalArgumentException("Prix doit être >= 0.");
        if (stock < 0) throw new IllegalArgumentException("Stock doit être >= 0.");

        Produit p = new Produit();

        // ✅ ID du commerçant connecté
        p.setIdUser(Session.getUserId());

        p.setNom(nom);
        p.setPrix(prix);
        p.setStock(stock);

        p.setRegion(emptyToNull(regionField));
        p.setCategorie(emptyToNull(categorieField));
        p.setDescription(emptyToNull(descriptionField));
        p.setImage(emptyToNull(imageField));

        return p;
    }

    @FXML
    private void clearForm() {
        if (nomTextField != null) nomTextField.clear();
        if (prixTextField != null) prixTextField.clear();
        if (stockField != null) stockField.clear();
        if (regionField != null) regionField.clear();
        if (categorieField != null) categorieField.clear();
        if (descriptionField != null) descriptionField.clear();
        if (imageField != null) imageField.clear();
        if (idProduitHidden != null) idProduitHidden.clear();
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
        if (!Session.isLoggedIn()) return;
        try {
            panierCRUD.clear(Session.getUserId());
            refreshCartUI();
        } catch (SQLException e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void checkoutNow() {
        if (!Session.isLoggedIn()) {
            statusLabel.setText("⚠ Connecte-toi d'abord.");
            showLogin();
            return;
        }

        try {
            int idCmd = panierCRUD.checkout(Session.getUserId());
            statusLabel.setText("✅ Commande validée (#" + idCmd + ")");
            refreshCartUI();
            showProducts();
        } catch (SQLException e) {
            statusLabel.setText("❌ " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------------------ HELPERS ------------------
    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static String safeText(TextField tf) {
        if (tf == null || tf.getText() == null) return "";
        return tf.getText().trim();
    }

    private static String emptyToNull(TextField tf) {
        String v = safeText(tf);
        return v.isEmpty() ? null : v;
    }
}