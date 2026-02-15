package Controlles;

import Entites.Produit;
import Services.PanierCRUD;
import Services.ProduitCRUD;
import Utils.MyBD;
import Utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WinGoShopController {

    // ==================== TOP BAR ====================
    @FXML private TextField searchField;
    @FXML private Label cartCountLabel;
    @FXML private HBox cartBadgeBox;
    @FXML private Label userNameLabel;
    @FXML private Label topSubtitleLabel;
    @FXML private HBox modeSwitchBox;
    @FXML private ToggleButton modeToggle;
    @FXML private HBox searchBox;

    @FXML private HBox topActionsBox;
    @FXML private HBox userBox;

    // ==================== CART (NEW DESIGN) ====================
    @FXML private VBox cartItemsBox;   // conteneur des cartes
    @FXML private Label cartTotalLabel;

    // ==================== LEFT NAV ROOT ====================
    @FXML private VBox leftNav;

    private boolean viewAsCommercant = false;

    // ==================== NAVIGATION ====================
    @FXML private Button navHomeBtn;
    @FXML private VBox navAddBox;
    @FXML private VBox navDashboardBox;
    @FXML private VBox navCartBox;
    @FXML private VBox navBecomeCommercantBox;

    // ==================== SCREENS ====================
    @FXML private VBox loginPane;
    @FXML private VBox clientProductsPane;
    @FXML private VBox commercantProductsPane;
    @FXML private ScrollPane formScrollPane;
    @FXML private VBox formPane;
    @FXML private HBox cartPane; // IMPORTANT: HBox (comme ton FXML)
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

    @FXML
    public void initialize() {
        setupProductsTable();
        setupFilters();
        refreshProducts();
        refreshCartUI();

        updateUIForUserType();
        showLogin();
    }

    // ==================== NAV ENABLE/DISABLE ====================
    private void setNavEnabled(boolean enabled) {
        if (leftNav == null) return;
        leftNav.setDisable(!enabled);
        leftNav.setOpacity(enabled ? 1.0 : 0.35);
    }

    private void setTopLoginMode(boolean isLogin) {
        if (topActionsBox != null) {
            topActionsBox.setVisible(!isLogin);
            topActionsBox.setManaged(!isLogin);
        }

        if (searchBox != null) {
            searchBox.setVisible(!isLogin);
            searchBox.setManaged(!isLogin);
        }
        if (cartBadgeBox != null) {
            cartBadgeBox.setVisible(!isLogin);
            cartBadgeBox.setManaged(!isLogin);
        }
        if (modeSwitchBox != null) {
            boolean showMode = !isLogin && Session.isLoggedIn() && Session.isCommercant();
            modeSwitchBox.setVisible(showMode);
            modeSwitchBox.setManaged(showMode);
        }
        if (userBox != null) {
            userBox.setVisible(!isLogin);
            userBox.setManaged(!isLogin);
        }

        if (searchField != null) searchField.setDisable(isLogin);
    }

    // ==================== SETUP ====================
    private void setupFilters() {
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Toutes", "Artisanat", "Gastronomie", "Textile", "Bijoux", "Art", "Souvenirs"
        );
        if (clientCategoryFilter != null) {
            clientCategoryFilter.setItems(categories);
            clientCategoryFilter.setValue("Toutes");
        }

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
        boolean logged = Session.isLoggedIn();
        boolean isCommercant = logged && Session.isCommercant();
        boolean isClient = logged && !Session.isCommercant();

        if (modeSwitchBox != null) {
            modeSwitchBox.setVisible(isCommercant);
            modeSwitchBox.setManaged(isCommercant);
        }

        if (!isCommercant) viewAsCommercant = false;

        boolean commercantView = isCommercantView();
        boolean clientView = !commercantView;

        navAddBox.setVisible(commercantView);
        navAddBox.setManaged(commercantView);

        navDashboardBox.setVisible(commercantView);
        navDashboardBox.setManaged(commercantView);

        boolean showCart = isClient && clientView;
        navCartBox.setVisible(showCart);
        navCartBox.setManaged(showCart);

        navBecomeCommercantBox.setVisible(isClient);
        navBecomeCommercantBox.setManaged(isClient);

        if (cartBadgeBox != null) {
            cartBadgeBox.setVisible(showCart);
            cartBadgeBox.setManaged(showCart);
        }

        if (topSubtitleLabel != null) {
            topSubtitleLabel.setText(commercantView ? "Espace Commerçant" : "Produits Locaux Tunisiens");
        }

        if (userNameLabel != null) {
            userNameLabel.setText(logged ? getNomPrenomUtilisateur(Session.getUserId()) : "Visiteur");
        }

        if (modeToggle != null && isCommercant) {
            modeToggle.setSelected(commercantView);
            modeToggle.setText(commercantView ? "Commerçant" : "Client");
        }
    }

    // ==================== SCREENS ====================
    @FXML
    public void showLogin() {
        hideAllScreens();
        loginPane.setVisible(true);
        loginPane.setManaged(true);

        setNavEnabled(false);
        setTopLoginMode(true);
        updateUIForUserType();
    }

    @FXML
    public void showProducts() {
        setNavEnabled(true);
        setTopLoginMode(false);

        hideAllScreens();
        updateUIForUserType();

        if (isCommercantView()) {
            commercantProductsPane.setVisible(true);
            commercantProductsPane.setManaged(true);
            refreshCommercantProducts();
        } else {
            clientProductsPane.setVisible(true);
            clientProductsPane.setManaged(true);
            refreshProducts();
            refreshClientProducts();
        }
    }

    @FXML
    public void showCart() {
        if (!Session.isLoggedIn()) { showLogin(); return; }
        if (isCommercantView()) { showAlert("🛒 Panier", "Passe en mode Client pour accéder au panier."); return; }

        hideAllScreens();
        updateUIForUserType();

        cartPane.setVisible(true);
        cartPane.setManaged(true);
        refreshCartUI();
    }

    // ==================== LOGIN ====================
    @FXML
    public void doLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass  = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            loginStatusLabel.setText("⚠ Remplis tous les champs.");
            return;
        }

        int fakeId = 1;
        String fakeType = email.toLowerCase().contains("shop") ? "COMMERCANT" : "CLIENT";

        Session.setUser(fakeId, fakeType);
        loginStatusLabel.setText("✅ Connecté (" + fakeType + ").");

        refreshCartUI();
        updateUIForUserType();
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
        } catch (Exception e) { e.printStackTrace(); }
        return "Utilisateur";
    }

    // ==================== CLIENT MODE: CARDS VIEW ====================
    private void refreshClientProducts() {
        if (clientProductsGrid == null) return;

        clientProductsGrid.getChildren().clear();
        System.out.println("🔄 Refreshing " + produitsData.size() + " product cards"); // Debug

        for (Produit p : produitsData) {
            VBox card = createProductCard(p);
            clientProductsGrid.getChildren().add(card);
        }

        System.out.println("✅ Created " + clientProductsGrid.getChildren().size() + " cards"); // Debug
    }

    private VBox createProductCard(Produit p) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(220);
        card.setPrefHeight(360);
        card.setMinHeight(360);
        card.setStyle(
                "-fx-background-color: rgba(0,0,0,0.28);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: rgba(255,255,255,0.18);" +
                        "-fx-border-radius: 16;" +
                        "-fx-padding: 12;" +
                        "-fx-cursor: hand;"
        );

        // IMAGE CONTAINER (StackPane pour éviter les problèmes de clip)
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefWidth(196);
        imageContainer.setPrefHeight(160);
        imageContainer.setMinHeight(160);
        imageContainer.setMaxHeight(160);
        imageContainer.setStyle(
                "-fx-background-color: rgba(255,189,0,0.10);" +
                        "-fx-background-radius: 12;"
        );

        ImageView imageView = new ImageView();
        imageView.setFitWidth(196);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        // ✅ CLIP appliqué sur le CONTAINER, pas sur l'ImageView
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(196, 160);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imageContainer.setClip(clip);

        // ✅ Chargement synchrone pour éviter les bugs de rendu
        String imageUrl = p.getImage();
        if (imageUrl != null && !imageUrl.isBlank()) {
            try {
                // ⚠️ backgroundLoading = FALSE pour forcer le rendu immédiat
                // Utilise un Thread séparé si tu veux l'async
                Image img = new Image(imageUrl.trim(), 196, 160, false, true, false);

                if (!img.isError()) {
                    imageView.setImage(img);
                } else {
                    // Image placeholder si erreur
                    imageContainer.setStyle(
                            "-fx-background-color: rgba(255,189,0,0.15);" +
                                    "-fx-background-radius: 12;"
                    );
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading image for: " + p.getNom());
            }
        }

        imageContainer.getChildren().add(imageView);

        // Labels
        Label nameLabel = new Label(p.getNom());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px;");
        nameLabel.setMaxWidth(196);
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("%.2f TND", p.getPrix()));
        priceLabel.setStyle("-fx-text-fill: #FFBD00; -fx-font-weight: 900; -fx-font-size: 16px;");

        Label regionLabel = new Label("📍 " + (p.getRegion() != null ? p.getRegion() : "Tunisie"));
        regionLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.70); -fx-font-size: 11px;");

        Label stockLabel = new Label(p.getStock() > 0 ? "✅ En stock" : "❌ Rupture");
        stockLabel.setStyle("-fx-text-fill: " + (p.getStock() > 0 ? "#00FF9D" : "#FF0054") +
                "; -fx-font-size: 10px; -fx-font-weight: 800;");

        Button addBtn = new Button("🛒 Ajouter");
        addBtn.setStyle("-fx-background-color: #FFBD00; -fx-text-fill: #390099; " +
                "-fx-background-radius: 999; -fx-padding: 8 16; " +
                "-fx-font-weight: 900; -fx-cursor: hand;");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addProductToCart(p));

        card.getChildren().addAll(imageContainer, nameLabel, priceLabel, regionLabel, stockLabel, addBtn);
        return card;
    }
    
    private void addProductToCart(Produit p) {
        if (!Session.isLoggedIn()) { showAlert("⚠ Connexion requise", "Connecte-toi pour ajouter au panier."); showLogin(); return; }
        if (p.getStock() <= 0) { showAlert("❌ Rupture de stock", "Ce produit n'est plus disponible."); return; }

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
        for (Produit p : filtered) clientProductsGrid.getChildren().add(createProductCard(p));
    }

    // ==================== COMMERCANT MODE: TABLE VIEW ====================
    private void refreshCommercantProducts() {
        try {
            List<Produit> myProducts = produitCRUD.afficherParUser(Session.getUserId());
            produitsData.setAll(myProducts);
            if (commercantProductCount != null) commercantProductCount.setText(myProducts.size() + " produits");
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
            System.out.println("✅ Loaded " + list.size() + " products from DB"); // Debug
        } catch (SQLException e) {
            System.err.println("❌ Error loading products");
            e.printStackTrace();
        }
    }

    @FXML
    public void onSearch() {
        if (searchField == null || searchField.isDisabled()) return;

        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        if (q.isEmpty()) {
            if (Session.isLoggedIn() && Session.isCommercant()) refreshCommercantProducts();
            else { refreshProducts(); refreshClientProducts(); }
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
            for (Produit p : filtered) clientProductsGrid.getChildren().add(createProductCard(p));
        }
    }

    // ==================== CART UI (CARDS) ====================
    private void refreshCartUI() {
        if (cartItemsBox == null) return;

        cartItemsBox.getChildren().clear();

        if (!Session.isLoggedIn()) {
            cartData.clear();
            if (cartCountLabel != null) cartCountLabel.setText("0");
            if (cartTotalLabel != null) cartTotalLabel.setText("0.00 TND");
            return;
        }

        try {
            cartData.setAll(panierCRUD.getActiveCart(Session.getUserId()));

            int totalQty = cartData.stream().mapToInt(CartItem::getQty).sum();
            double total = cartData.stream().mapToDouble(CartItem::getSubtotal).sum();

            if (cartCountLabel != null) cartCountLabel.setText(String.valueOf(totalQty));
            if (cartTotalLabel != null) cartTotalLabel.setText(String.format("%.2f TND", total));

            for (CartItem it : cartData) {
                cartItemsBox.getChildren().add(createCartItemCard(it));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createCartItemCard(CartItem it) {

        // ✅ IMAGE (vient de produit.image via panierCRUD)
        ImageView iv = new ImageView();
        iv.setFitWidth(56);
        iv.setFitHeight(56);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        // petit arrondi (optionnel)
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(56, 56);
        clip.setArcWidth(14);
        clip.setArcHeight(14);
        iv.setClip(clip);

        // ✅ CHANGER ICI : Utiliser le chargement async aussi
        String imageUrl = it.getImage();
        if (imageUrl != null && !imageUrl.isBlank()) {
            try {
                Image img = new Image(imageUrl.trim(), 56, 56, true, true, true);
                iv.setImage(img);
            } catch (Exception e) {
                // Image par défaut si erreur
            }
        }

        // INFO
        Label name = new Label(it.getNom());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 13px;");

        Label price = new Label(String.format("%.2f TND", it.getPrix()));
        price.setStyle("-fx-text-fill: #FFBD00; -fx-font-weight: 900;");

        Label qty = new Label("x" + it.getQty());
        qty.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-weight: 800;");

        VBox info = new VBox(4, name, price, qty);

        // ACTIONS
        Button minus = new Button("➖");
        Button plus  = new Button("➕");
        Button del   = new Button("🗑");

        minus.setOnAction(e -> {
            try { panierCRUD.changeQty(Session.getUserId(), it.getIdProduit(), -1); refreshCartUI(); }
            catch (Exception ignored) {}
        });
        plus.setOnAction(e -> {
            try { panierCRUD.changeQty(Session.getUserId(), it.getIdProduit(), +1); refreshCartUI(); }
            catch (Exception ignored) {}
        });
        del.setOnAction(e -> {
            try { panierCRUD.remove(Session.getUserId(), it.getIdProduit()); refreshCartUI(); }
            catch (Exception ignored) {}
        });

        HBox actions = new HBox(8, minus, plus, del);
        actions.setAlignment(Pos.CENTER_RIGHT);

        String baseBtn = "-fx-background-radius: 999; -fx-padding: 6 10; -fx-font-weight: 900; -fx-cursor: hand;";
        minus.setStyle(baseBtn + "-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: white;");
        plus.setStyle(baseBtn + "-fx-background-color: rgba(255,189,0,0.25); -fx-text-fill: #FFBD00;");
        del.setStyle(baseBtn + "-fx-background-color: rgba(255,0,84,0.25); -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, iv, info, spacer, actions);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(0,0,0,0.22); -fx-background-radius: 16; -fx-padding: 10;");

        return row;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void toggleMode() {
        if (!Session.isLoggedIn() || !Session.isCommercant()) return;

        viewAsCommercant = modeToggle.isSelected();
        modeToggle.setText(viewAsCommercant ? "Commerçant" : "Client");

        updateUIForUserType();
        showProducts();
        if (isCommercantView()) refreshCommercantProducts();
        else { refreshProducts(); refreshClientProducts(); }
    }

    private boolean isCommercantView() {
        return Session.isLoggedIn() && Session.isCommercant() && viewAsCommercant;
    }

    // ==================== OTHER ACTIONS ====================
    @FXML
    public void showAddForm() {
        if (!Session.isLoggedIn() || !Session.isCommercant()) {
            showAlert("⚠ Accès refusé", "Connecte-toi en tant que commerçant.");
            showLogin();
            return;
        }
        if (!isCommercantView()) {
            showAlert("⚠ Mode Commerçant", "Active le mode Commerçant pour ajouter.");
            return;
        }

        hideAllScreens();
        updateUIForUserType();

        formScrollPane.setVisible(true);
        formScrollPane.setManaged(true);

        clearForm();
        if (formTitleLabel != null) formTitleLabel.setText("➕ Ajouter Produit");
        if (saveBtn != null) saveBtn.setText("✅ Enregistrer");
    }

    @FXML
    public void showDashboard() {
        if (!Session.isLoggedIn() || !Session.isCommercant()) {
            showLogin();
            return;
        }
        if (!isCommercantView()) {
            showAlert("⚠ Mode Commerçant", "Active le mode Commerçant pour voir le dashboard.");
            return;
        }

        hideAllScreens();
        updateUIForUserType();

        dashboardPane.setVisible(true);
        dashboardPane.setManaged(true);

        updateDashboardStats();
    }

    @FXML
    public void showBecomeCommercant() {
        if (!Session.isLoggedIn()) {
            showLogin();
            return;
        }
        if (Session.isCommercant()) {
            showAlert("✅ Info", "Vous êtes déjà commerçant.");
            return;
        }

        hideAllScreens();
        updateUIForUserType();

        becomeCommercantPane.setVisible(true);
        becomeCommercantPane.setManaged(true);
    }

    @FXML
    public void editSelectedProduct() {
        Produit selected = produitsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("✏ Modifier", "Sélectionne un produit d'abord.");
            return;
        }

        hideAllScreens();
        updateUIForUserType();

        formScrollPane.setVisible(true);
        formScrollPane.setManaged(true);

        idProduitHidden.setText(String.valueOf(selected.getIdProduit()));
        nomTextField.setText(selected.getNom());
        prixTextField.setText(String.valueOf(selected.getPrix()));
        stockField.setText(String.valueOf(selected.getStock()));
        categorieField.setText(selected.getCategorie());
        regionField.setText(selected.getRegion());
        imageField.setText(selected.getImage());
        descriptionField.setText(selected.getDescription());

        formTitleLabel.setText("✏ Modifier Produit");
        saveBtn.setText("💾 Mettre à jour");
    }

    @FXML
    public void deleteSelectedProduct() {
        Produit selected = produitsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("🗑 Supprimer", "Sélectionne un produit d'abord.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer : " + selected.getNom() + " ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            produitCRUD.supprimer(selected.getIdProduit());
            refreshCommercantProducts();
            showAlert("✅ Supprimé", "Produit supprimé.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("❌ Erreur", "Impossible de supprimer.");
        }
    }

    @FXML
    public void saveProduct() {
        if (!Session.isLoggedIn() || !Session.isCommercant() || !isCommercantView()) {
            showAlert("⚠ Accès refusé", "Mode commerçant requis.");
            return;
        }

        String nom = nomTextField.getText() == null ? "" : nomTextField.getText().trim();
        String cat = categorieField.getText() == null ? "" : categorieField.getText().trim();
        String region = regionField.getText() == null ? "" : regionField.getText().trim();
        String img = imageField.getText() == null ? "" : imageField.getText().trim();
        String desc = descriptionField.getText() == null ? "" : descriptionField.getText().trim();

        double prix;
        int stock;

        try {
            prix = Double.parseDouble(prixTextField.getText().trim());
            stock = Integer.parseInt(stockField.getText().trim());
        } catch (Exception ex) {
            showAlert("⚠ Champs invalides", "Prix/Stock doivent être numériques.");
            return;
        }

        if (nom.isEmpty() || cat.isEmpty()) {
            showAlert("⚠ Champs obligatoires", "Nom et Catégorie sont obligatoires.");
            return;
        }

        try {
            String idTxt = idProduitHidden.getText() == null ? "" : idProduitHidden.getText().trim();

            if (idTxt.isEmpty()) {
                // AJOUT
                Produit p = new Produit(Session.getUserId(), nom, desc, prix, region, cat, stock, img);
                produitCRUD.ajouter(p);
                showAlert("✅ Ajout", "Produit ajouté.");
            } else {
                // MODIFICATION
                int id = Integer.parseInt(idTxt);
                Produit p = new Produit(id, Session.getUserId(), nom, desc, prix, region, cat, stock, img);
                produitCRUD.modifier(p);
                showAlert("✅ Modifié", "Produit mis à jour.");
            }

            // ✅ Refresh toutes les vues (SANS imageCache.clear())
            refreshProducts();
            refreshCommercantProducts();
            refreshClientProducts();

            clearForm();
            showProducts();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("❌ Erreur", "Impossible d'enregistrer.");
        }
    }

    @FXML
    public void clearForm() {
        idProduitHidden.clear();
        nomTextField.clear();
        descriptionField.clear();
        prixTextField.clear();
        stockField.clear();
        regionField.clear();
        categorieField.clear();
        imageField.clear();
    }

    @FXML
    public void clearCart() {
        try { panierCRUD.clear(Session.getUserId()); refreshCartUI(); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void checkoutNow() {
        showAlert("✅ Commande", "Checkout (à implémenter).");
    }

    @FXML
    public void submitBecomeCommercant() {

        if (!Session.isLoggedIn()) { showLogin(); return; }
        if (Session.isCommercant()) {
            showAlert("✅ Info", "Vous êtes déjà commerçant.");
            return;
        }

        // (optionnel) validation simple des champs
        String nom = becomeCommercantNom.getText() == null ? "" : becomeCommercantNom.getText().trim();
        String phone = becomeCommercantPhone.getText() == null ? "" : becomeCommercantPhone.getText().trim();
        String typeP = becomeCommercantType.getText() == null ? "" : becomeCommercantType.getText().trim();
        String mot  = becomeCommercantMotivation.getText() == null ? "" : becomeCommercantMotivation.getText().trim();

        if (nom.isEmpty() || phone.isEmpty() || typeP.isEmpty() || mot.isEmpty()) {
            if (becomeCommercantStatusLabel != null) {
                becomeCommercantStatusLabel.setText("⚠ Remplis tous les champs.");
            } else {
                showAlert("⚠ Champs manquants", "Remplis tous les champs.");
            }
            return;
        }

        String sql = "UPDATE utilisateur SET type='COMMERCANT' WHERE id=?";

        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.getUserId());
            int updated = ps.executeUpdate();

            if (updated == 1) {
                // ✅ mettre à jour la session
                Session.setUser(Session.getUserId(), "COMMERCANT");

                // ✅ passer directement en mode commerçant (view)
                viewAsCommercant = true;
                if (modeToggle != null) {
                    modeToggle.setSelected(true);
                    modeToggle.setText("Commerçant");
                }

                // ✅ reset formulaire + message
                if (becomeCommercantStatusLabel != null) becomeCommercantStatusLabel.setText("");
                showAlert("🎉 Bienvenue !", "Votre compte est maintenant Commerçant ✅");

                // ✅ refresh UI + redirection
                refreshCartUI();
                updateUIForUserType();

                // tu peux choisir où l’envoyer :
                showAddForm(); // direct page ajouter produit
                // ou: showProducts();
                // ou: showDashboard();

            } else {
                showAlert("❌ Erreur", "Impossible de changer le type utilisateur.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("❌ Erreur", "Erreur DB: " + e.getMessage());
        }
    }

    private void updateDashboardStats() {
        if (dashTotalProducts != null) dashTotalProducts.setText(String.valueOf(produitsData.size()));
        if (dashTotalStock != null) {
            int totalStock = produitsData.stream().mapToInt(Produit::getStock).sum();
            dashTotalStock.setText(String.valueOf(totalStock));
        }
        if (dashStockValue != null) {
            double val = produitsData.stream().mapToDouble(p -> p.getPrix() * p.getStock()).sum();
            dashStockValue.setText(String.format("%.2f TND", val));
        }
    }


    private Image loadImageSmart(String path) {
        if (path == null || path.isBlank()) {
            System.out.println("⚠️ Image path is null or blank");
            return null;
        }

        String p = path.trim();
        System.out.println("🔍 Trying to load image: " + p); // Debug

        try {
            // 1) URL web (http/https)
            if (p.startsWith("http://") || p.startsWith("https://")) {
                Image img = new Image(p, true); // backgroundLoading = true
                System.out.println("✅ Loaded from URL: " + p);
                return img;
            }

            // 2) Resource JavaFX (commence par /)
            if (p.startsWith("/")) {
                var stream = getClass().getResourceAsStream(p);
                if (stream != null) {
                    Image img = new Image(stream);
                    System.out.println("✅ Loaded from resources: " + p);
                    return img;
                }
            }

            // 3) Fichier local (C:\..., file:/, etc.)
            java.io.File file = new java.io.File(p);
            if (file.exists() && file.isFile()) {
                String fileUrl = file.toURI().toString();
                Image img = new Image(fileUrl, true);
                System.out.println("✅ Loaded from file: " + fileUrl);
                return img;
            }

            // 4) Dernier essai : forcer file:/ si ce n'est pas déjà le cas
            if (!p.startsWith("file:")) {
                String fileUrl = "file:///" + p.replace("\\", "/");
                Image img = new Image(fileUrl, true);
                System.out.println("✅ Loaded with file:/// prefix: " + fileUrl);
                return img;
            }

            System.out.println("❌ Could not load image: " + p);
            return null;

        } catch (Exception e) {
            System.err.println("❌ Error loading image: " + p);
            e.printStackTrace();
            return null;
        }
    }
}