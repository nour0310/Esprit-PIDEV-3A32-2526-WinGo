package Controlles;

import Entites.Produit;
import Services.PanierCRUD;
import Services.ProduitCRUD;
import Utils.MyBD;
import Utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced Controller with DUAL MODE:
 * - CLIENT MODE: Shopping cards view with filters
 * - COMMERCANT MODE: Professional dashboard with table management
 */
public class WinGoShopController {

    // ==================== TOP BAR ====================
    @FXML private TextField searchField;
    @FXML private Label cartCountLabel;
    @FXML private HBox cartBadgeBox;
    @FXML private Label userNameLabel;
    @FXML private Label topSubtitleLabel;

    // ==================== NAVIGATION ====================
    @FXML private Button navHomeBtn;
    @FXML private VBox navAddBox;
    @FXML private VBox navDashboardBox;
    @FXML private VBox navCartBox;
    @FXML private VBox navBecomeCommercantBox;

    // ==================== SCREENS ====================
    @FXML private VBox loginPane;
    @FXML private VBox clientProductsPane;          // CLIENT: Cards view
    @FXML private VBox commercantProductsPane;      // COMMERCANT: Table view
    @FXML private ScrollPane formScrollPane;
    @FXML private VBox formPane;
    @FXML private VBox cartPane;
    @FXML private VBox becomeCommercantPane;
    @FXML private VBox dashboardPane;

    // ==================== LOGIN ====================
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label loginStatusLabel;

    // ==================== CLIENT MODE ====================
    @FXML private FlowPane clientProductsGrid;
    @FXML private ComboBox<String> clientCategoryFilter;
    @FXML private ComboBox<String> clientRegionFilter;

    // ==================== COMMERCANT MODE ====================
    @FXML private TableView<Produit> produitsTable;
    @FXML private TableColumn<Produit, String> colImage;
    @FXML private TableColumn<Produit, Integer> colId;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, Integer> colStock;
    @FXML private TableColumn<Produit, String> colCat;
    @FXML private TableColumn<Produit, String> colRegion;
    @FXML private Label statusLabel;
    @FXML private Label commercantProductCount;

    // ==================== FORM ====================
    @FXML private TextField idProduitHidden;
    @FXML private TextField nomTextField;
    @FXML private TextField descriptionField;
    @FXML private TextField prixTextField;
    @FXML private TextField stockField;
    @FXML private TextField regionField;
    @FXML private TextField categorieField;
    @FXML private TextField imageField;
    @FXML private Label formTitleLabel;
    @FXML private Button saveBtn;
    @FXML private Label welcomeLabel;

    // ==================== CART ====================
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartColNom;
    @FXML private TableColumn<CartItem, Double> cartColPrix;
    @FXML private TableColumn<CartItem, Integer> cartColQty;
    @FXML private TableColumn<CartItem, Double> cartColSub;
    @FXML private Label cartTotalLabel;

    // ==================== BECOME COMMERCANT ====================
    @FXML private TextField becomeCommercantNom;
    @FXML private TextField becomeCommercantPhone;
    @FXML private TextField becomeCommercantType;
    @FXML private TextArea becomeCommercantMotivation;
    @FXML private Label becomeCommercantStatusLabel;

    // ==================== DASHBOARD ====================
    @FXML private Label dashTotalProducts;
    @FXML private Label dashTotalStock;
    @FXML private Label dashStockValue;
    @FXML private TableView<?> dashboardRecentTable;

    // ==================== SERVICES ====================
    private final ProduitCRUD produitCRUD = new ProduitCRUD();
    private final PanierCRUD panierCRUD = new PanierCRUD();
    private final ObservableList<Produit> produitsData = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cartData = FXCollections.observableArrayList();

    // ==================== INITIALIZATION ====================
    @FXML
    public void initialize() {
        setupProductsTable();
        setupCartTable();
        setupFilters();
        refreshProducts();
        refreshCartUI();
        showLogin();
    }

    // ==================== SETUP ====================
    private void setupFilters() {
        // Categories
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Toutes", "Artisanat", "Gastronomie", "Textile", "Bijoux", "Art", "Souvenirs"
        );
        if (clientCategoryFilter != null) {
            clientCategoryFilter.setItems(categories);
            clientCategoryFilter.setValue("Toutes");
        }

        // Regions (24 gouvernorats tunisiens)
        ObservableList<String> regions = FXCollections.observableArrayList(
                "Toutes", "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan",
                "Bizerte", "Béja", "Jendouba", "Le Kef", "Siliana", "Sousse", "Monastir",
                "Mahdia", "Sfax", "Kairouan", "Kasserine", "Sidi Bouzid", "Gabès",
                "Médenine", "Tataouine", "Gafsa", "Tozeur", "Kebili"
        );
        if (clientRegionFilter != null) {
            clientRegionFilter.setItems(regions);
            clientRegionFilter.setValue("Toutes");
        }
    }

    private void setupProductsTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCat.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colRegion.setCellValueFactory(new PropertyValueFactory<>("region"));

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

    private void setupCartTable() {
        cartColNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        cartColPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        cartColQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        cartColSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        cartTable.setItems(cartData);
    }

    // ==================== NAVIGATION & UI MODE ====================
    private void hideAllScreens() {
        loginPane.setVisible(false); loginPane.setManaged(false);
        clientProductsPane.setVisible(false); clientProductsPane.setManaged(false);
        commercantProductsPane.setVisible(false); commercantProductsPane.setManaged(false);
        formScrollPane.setVisible(false); formScrollPane.setManaged(false);
        cartPane.setVisible(false); cartPane.setManaged(false);
        becomeCommercantPane.setVisible(false); becomeCommercantPane.setManaged(false);
        dashboardPane.setVisible(false); dashboardPane.setManaged(false);
    }

    private void updateUIForUserType() {
        boolean isCommercant = Session.isLoggedIn() && Session.isCommercant();
        boolean isClient = Session.isLoggedIn() && !Session.isCommercant();

        // Navigation visibility
        navAddBox.setVisible(isCommercant);
        navAddBox.setManaged(isCommercant);
        navDashboardBox.setVisible(isCommercant);
        navDashboardBox.setManaged(isCommercant);

        navCartBox.setVisible(!isCommercant);
        navCartBox.setManaged(!isCommercant);
        navBecomeCommercantBox.setVisible(isClient);
        navBecomeCommercantBox.setManaged(isClient);

        cartBadgeBox.setVisible(!isCommercant);
        cartBadgeBox.setManaged(!isCommercant);

        // Top subtitle
        if (topSubtitleLabel != null) {
            if (isCommercant) {
                topSubtitleLabel.setText("Espace Commerçant");
            } else {
                topSubtitleLabel.setText("Produits Locaux Tunisiens");
            }
        }

        // User name
        if (userNameLabel != null) {
            if (Session.isLoggedIn()) {
                String name = getNomPrenomUtilisateur(Session.getUserId());
                userNameLabel.setText(name);
            } else {
                userNameLabel.setText("Visiteur");
            }
        }
    }

    @FXML public void showLogin() {
        hideAllScreens();
        loginPane.setVisible(true);
        loginPane.setManaged(true);
    }

    @FXML
    public void showProducts() {
        hideAllScreens();

        boolean isCommercant = Session.isLoggedIn() && Session.isCommercant();

        if (isCommercant) {
            // COMMERCANT: Show table view with only their products
            commercantProductsPane.setVisible(true);
            commercantProductsPane.setManaged(true);
            refreshCommercantProducts();
        } else {
            // CLIENT: Show cards view with all products
            clientProductsPane.setVisible(true);
            clientProductsPane.setManaged(true);
            refreshClientProducts();
        }
    }

    @FXML public void showCart() {
        if (!Session.isLoggedIn()) {
            statusLabel.setText("⚠ Connecte-toi d'abord.");
            showLogin();
            return;
        }
        hideAllScreens();
        cartPane.setVisible(true);
        cartPane.setManaged(true);
        refreshCartUI();
    }

    @FXML
    public void showAddForm() {
        if (!Session.isLoggedIn() || !Session.isCommercant()) {
            if (statusLabel != null) statusLabel.setText("⚠ Réservé aux commerçants.");
            showLogin();
            return;
        }

        if (welcomeLabel != null) {
            String fullName = getNomPrenomUtilisateur(Session.getUserId());
            welcomeLabel.setText("Bienvenue " + fullName + " 👋");
        }

        clearForm();
        formTitleLabel.setText("➕ Ajouter Produit");
        saveBtn.setText("✅ Enregistrer");

        hideAllScreens();
        formScrollPane.setVisible(true);
        formScrollPane.setManaged(true);
    }

    @FXML
    public void showBecomeCommercant() {
        if (!Session.isLoggedIn()) {
            showLogin();
            return;
        }
        if (Session.isCommercant()) {
            if (statusLabel != null) statusLabel.setText("✅ Vous êtes déjà commerçant!");
            showProducts();
            return;
        }
        hideAllScreens();
        becomeCommercantPane.setVisible(true);
        becomeCommercantPane.setManaged(true);
    }

    @FXML
    public void showDashboard() {
        if (!Session.isLoggedIn() || !Session.isCommercant()) {
            showLogin();
            return;
        }
        hideAllScreens();
        dashboardPane.setVisible(true);
        dashboardPane.setManaged(true);
        updateDashboardStats();
    }

    // ==================== LOGIN ====================
    @FXML
    public void doLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            loginStatusLabel.setText("⚠ Remplis tous les champs.");
            return;
        }

        // TODO: Real DB authentication
        // For now: email with "shop" = COMMERCANT, else CLIENT
        int fakeId = 1;
        String fakeType = email.toLowerCase().contains("shop") ? "COMMERCANT" : "CLIENT";

        Session.setUser(fakeId, fakeType);
        loginStatusLabel.setText("✅ Connecté (" + fakeType + ").");

        updateUIForUserType();
        refreshCartUI();
        showProducts();
    }

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
                    return full.trim().isEmpty() ? "Utilisateur" : full.trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Utilisateur";
    }

    // ==================== CLIENT MODE: CARDS VIEW ====================
    private void refreshClientProducts() {
        if (clientProductsGrid == null) return;

        clientProductsGrid.getChildren().clear();

        for (Produit p : produitsData) {
            VBox card = createProductCard(p);
            clientProductsGrid.getChildren().add(card);
        }
    }

    private VBox createProductCard(Produit p) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(220);
        card.setStyle(
                "-fx-background-color: rgba(0,0,0,0.28);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: rgba(255,255,255,0.18);" +
                        "-fx-border-radius: 16;" +
                        "-fx-padding: 12;" +
                        "-fx-cursor: hand;"
        );

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(196);
        imageView.setFitHeight(196);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 12;");
        try {
            if (p.getImage() != null && !p.getImage().isBlank()) {
                imageView.setImage(new Image(p.getImage(), true));
            }
        } catch (Exception e) {
            // Placeholder
        }

        // Name
        Label nameLabel = new Label(p.getNom());
        nameLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-size: 14px;" +
                        "-fx-wrap-text: true;"
        );
        nameLabel.setMaxWidth(196);

        // Price
        Label priceLabel = new Label(String.format("%.2f TND", p.getPrix()));
        priceLabel.setStyle(
                "-fx-text-fill: #FFBD00;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-size: 16px;"
        );

        // Region
        Label regionLabel = new Label("📍 " + (p.getRegion() != null ? p.getRegion() : "Tunisie"));
        regionLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.70);" +
                        "-fx-font-size: 11px;"
        );

        // Stock indicator
        Label stockLabel = new Label(p.getStock() > 0 ? "✅ En stock" : "❌ Rupture");
        stockLabel.setStyle(
                "-fx-text-fill: " + (p.getStock() > 0 ? "#00FF9D" : "#FF0054") + ";" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: 800;"
        );

        // Add to cart button
        Button addBtn = new Button("🛒 Ajouter");
        addBtn.setStyle(
                "-fx-background-color: #FFBD00;" +
                        "-fx-text-fill: #390099;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 8 16;" +
                        "-fx-font-weight: 900;" +
                        "-fx-cursor: hand;"
        );
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addProductToCart(p));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, regionLabel, stockLabel, addBtn);

        return card;
    }

    private void addProductToCart(Produit p) {
        if (!Session.isLoggedIn()) {
            showAlert("⚠ Connexion requise", "Connecte-toi pour ajouter au panier.");
            showLogin();
            return;
        }

        if (p.getStock() <= 0) {
            showAlert("❌ Rupture de stock", "Ce produit n'est plus disponible.");
            return;
        }

        try {
            panierCRUD.addToCart(Session.getUserId(), p.getIdProduit(), p.getPrix(), 1);
            refreshCartUI();
            showAlert("✅ Ajouté!", p.getNom() + " ajouté au panier.");
        } catch (SQLException e) {
            showAlert("❌ Erreur", "Impossible d'ajouter au panier.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onClientFilter() {
        String catFilter = clientCategoryFilter.getValue();
        String regFilter = clientRegionFilter.getValue();

        List<Produit> filtered = produitsData.stream()
                .filter(p -> catFilter == null || catFilter.equals("Toutes") ||
                        (p.getCategorie() != null && p.getCategorie().equals(catFilter)))
                .filter(p -> regFilter == null || regFilter.equals("Toutes") ||
                        (p.getRegion() != null && p.getRegion().equals(regFilter)))
                .collect(Collectors.toList());

        clientProductsGrid.getChildren().clear();
        for (Produit p : filtered) {
            clientProductsGrid.getChildren().add(createProductCard(p));
        }
    }

    // ==================== COMMERCANT MODE: TABLE VIEW ====================
    private void refreshCommercantProducts() {
        try {
            // Show only products of current commercant
            List<Produit> myProducts = produitCRUD.afficherParUser(Session.getUserId());
            produitsData.setAll(myProducts);

            if (commercantProductCount != null) {
                commercantProductCount.setText(myProducts.size() + " produits");
            }
        } catch (SQLException e) {
            if (statusLabel != null) statusLabel.setText("❌ Erreur DB");
            e.printStackTrace();
        }
    }

    // ==================== PRODUCTS CRUD ====================
    private void refreshProducts() {
        try {
            List<Produit> list = produitCRUD.afficher();
            produitsData.setAll(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onSearch() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            if (Session.isLoggedIn() && Session.isCommercant()) {
                refreshCommercantProducts();
            } else {
                refreshProducts();
                refreshClientProducts();
            }
            return;
        }

        List<Produit> filtered = produitsData.stream()
                .filter(p -> (p.getNom() != null && p.getNom().toLowerCase().contains(q)) ||
                        (p.getCategorie() != null && p.getCategorie().toLowerCase().contains(q)) ||
                        (p.getRegion() != null && p.getRegion().toLowerCase().contains(q)))
                .collect(Collectors.toList());

        if (Session.isLoggedIn() && Session.isCommercant()) {
            produitsTable.setItems(FXCollections.observableArrayList(filtered));
        } else {
            clientProductsGrid.getChildren().clear();
            for (Produit p : filtered) {
                clientProductsGrid.getChildren().add(createProductCard(p));
            }
        }
    }

    @FXML
    public void addSelectedToCart() {
        if (!Session.isLoggedIn()) {
            showAlert("⚠ Connexion", "Connecte-toi d'abord.");
            showLogin();
            return;
        }

        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) return;
        addProductToCart(p);
    }

    @FXML
    public void editSelectedProduct() {
        if (!Session.isLoggedIn() || !Session.isCommercant()) {
            showAlert("⚠ Accès refusé", "Réservé aux commerçants.");
            return;
        }

        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) {
            if (statusLabel != null) statusLabel.setText("⚠ Sélectionne un produit.");
            return;
        }

        if (p.getIdUser() != Session.getUserId()) {
            if (statusLabel != null) statusLabel.setText("⚠ Pas ton produit.");
            return;
        }

        idProduitHidden.setText(String.valueOf(p.getIdProduit()));
        nomTextField.setText(nullSafe(p.getNom()));
        descriptionField.setText(nullSafe(p.getDescription()));
        prixTextField.setText(String.valueOf(p.getPrix()));
        stockField.setText(String.valueOf(p.getStock()));
        regionField.setText(nullSafe(p.getRegion()));
        categorieField.setText(nullSafe(p.getCategorie()));
        imageField.setText(nullSafe(p.getImage()));

        formTitleLabel.setText("✏ Modifier Produit");
        saveBtn.setText("💾 Mettre à jour");

        hideAllScreens();
        formScrollPane.setVisible(true);
        formScrollPane.setManaged(true);
    }

    @FXML
    public void deleteSelectedProduct() {
        if (!Session.isLoggedIn() || !Session.isCommercant()) return;

        Produit p = produitsTable.getSelectionModel().getSelectedItem();
        if (p == null) return;

        if (p.getIdUser() != Session.getUserId()) {
            if (statusLabel != null) statusLabel.setText("⚠ Pas ton produit.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer: " + p.getNom() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try {
                produitCRUD.supprimer(p.getIdProduit());
                if (statusLabel != null) statusLabel.setText("✅ Supprimé.");
                refreshCommercantProducts();
            } catch (SQLException e) {
                if (statusLabel != null) statusLabel.setText("❌ Erreur");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void saveProduct() {
        if (!Session.isLoggedIn() || !Session.isCommercant()) {
            showAlert("⚠ Accès refusé", "Réservé aux commerçants.");
            return;
        }

        try {
            Produit p = buildProduitFromForm();
            boolean isEdit = idProduitHidden.getText() != null && !idProduitHidden.getText().isBlank();

            if (isEdit) {
                p.setIdProduit(Integer.parseInt(idProduitHidden.getText().trim()));
                produitCRUD.modifier(p);
                if (statusLabel != null) statusLabel.setText("✅ Modifié.");
            } else {
                produitCRUD.ajouter(p);
                if (statusLabel != null) statusLabel.setText("✅ Ajouté.");
            }

            refreshProducts();
            showProducts();

        } catch (IllegalArgumentException ex) {
            showAlert("⚠ Validation", ex.getMessage());
        } catch (SQLException ex) {
            showAlert("❌ Erreur DB", ex.getMessage());
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

        if (prix < 0) throw new IllegalArgumentException("Prix >= 0.");
        if (stock < 0) throw new IllegalArgumentException("Stock >= 0.");

        Produit p = new Produit();
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

    // ==================== CART ====================
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
            e.printStackTrace();
        }
    }

    @FXML public void qtyPlus() {
        CartItem it = cartTable.getSelectionModel().getSelectedItem();
        if (it == null) return;
        try {
            panierCRUD.changeQty(Session.getUserId(), it.getIdProduit(), +1);
            refreshCartUI();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void qtyMinus() {
        CartItem it = cartTable.getSelectionModel().getSelectedItem();
        if (it == null) return;
        try {
            panierCRUD.changeQty(Session.getUserId(), it.getIdProduit(), -1);
            refreshCartUI();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void removeFromCart() {
        CartItem it = cartTable.getSelectionModel().getSelectedItem();
        if (it == null) return;
        try {
            panierCRUD.remove(Session.getUserId(), it.getIdProduit());
            refreshCartUI();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void clearCart() {
        if (!Session.isLoggedIn()) return;
        try {
            panierCRUD.clear(Session.getUserId());
            refreshCartUI();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void checkoutNow() {
        if (!Session.isLoggedIn()) {
            showLogin();
            return;
        }
        try {
            int idCmd = panierCRUD.checkout(Session.getUserId());
            showAlert("✅ Commande validée", "Commande #" + idCmd + " enregistrée!");
            refreshCartUI();
            showProducts();
        } catch (SQLException e) {
            showAlert("❌ Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== BECOME COMMERCANT ====================
    @FXML
    public void submitBecomeCommercant() {
        String nom = safeText(becomeCommercantNom);
        String phone = safeText(becomeCommercantPhone);
        String type = safeText(becomeCommercantType);

        if (nom.isEmpty() || phone.isEmpty() || type.isEmpty()) {
            becomeCommercantStatusLabel.setText("⚠ Remplis tous les champs requis.");
            return;
        }

        // TODO: Save request to DB, send to admin for validation
        // For demo: auto-upgrade to COMMERCANT

        try {
            // Update user type in DB
            String sql = "UPDATE utilisateur SET type='COMMERCANT' WHERE id=?";
            Connection conn = MyBD.getInstance().getConn();
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, Session.getUserId());
                pst.executeUpdate();
            }

            // Update session
            Session.setUser(Session.getUserId(), "COMMERCANT");

            showAlert("✅ Félicitations!", "Tu es maintenant commerçant! Tu peux commencer à vendre tes produits.");
            updateUIForUserType();
            showProducts();

        } catch (SQLException e) {
            becomeCommercantStatusLabel.setText("❌ Erreur lors de la mise à jour.");
            e.printStackTrace();
        }
    }

    // ==================== DASHBOARD ====================
    private void updateDashboardStats() {
        try {
            List<Produit> myProducts = produitCRUD.afficherParUser(Session.getUserId());

            int totalProducts = myProducts.size();
            int totalStock = myProducts.stream().mapToInt(Produit::getStock).sum();
            double stockValue = myProducts.stream()
                    .mapToDouble(p -> p.getPrix() * p.getStock())
                    .sum();

            if (dashTotalProducts != null) dashTotalProducts.setText(String.valueOf(totalProducts));
            if (dashTotalStock != null) dashTotalStock.setText(String.valueOf(totalStock));
            if (dashStockValue != null) dashStockValue.setText(String.format("%.2f TND", stockValue));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== HELPERS ====================
    private static String nullSafe(String s) { return s == null ? "" : s; }
    private static String safeText(TextField tf) {
        return tf == null || tf.getText() == null ? "" : tf.getText().trim();
    }
    private static String emptyToNull(TextField tf) {
        String v = safeText(tf);
        return v.isEmpty() ? null : v;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
