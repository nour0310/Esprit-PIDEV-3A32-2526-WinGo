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

public class WinGoShopController {

    // ==================== TOP BAR ====================
    @FXML private TextField searchField;
    @FXML private Label cartCountLabel;
    @FXML private HBox cartBadgeBox;
    @FXML private Label userNameLabel;
    @FXML private Label topSubtitleLabel;
    @FXML private HBox modeSwitchBox;
    @FXML private ToggleButton modeToggle;
    @FXML private TableColumn<CartItem, Void> cartColActions;
    @FXML private HBox searchBox;
    // ==================== LEFT NAV ROOT ====================
    @FXML private VBox leftNav; // <-- AJOUT IMPORTANT (fx:id="leftNav" dans FXML)

    // mode d'affichage (sans changer le type en DB)
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

        updateUIForUserType();
        showLogin(); // démarrage sur login
    }

    // ==================== NAV ENABLE/DISABLE ====================
    private void setNavEnabled(boolean enabled) {
        if (leftNav == null) return;
        leftNav.setDisable(!enabled);
        leftNav.setOpacity(enabled ? 1.0 : 0.35);
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

    private void setupCartTable() {
        cartColNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        cartColPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        cartColQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        cartColSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        cartColActions.setCellFactory(col -> new TableCell<>() {
            private final Button minus = new Button("➖");
            private final Button plus  = new Button("➕");
            private final Button del   = new Button("🗑");
            private final HBox box = new HBox(8, minus, plus, del);

            {
                box.setAlignment(Pos.CENTER);

                String baseBtn =
                        "-fx-background-radius: 999;" +
                                "-fx-padding: 6 10;" +
                                "-fx-font-weight: 900;" +
                                "-fx-cursor: hand;";

                minus.setStyle(baseBtn + "-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: white;");
                plus.setStyle(baseBtn + "-fx-background-color: rgba(255,189,0,0.25); -fx-text-fill: #FFBD00;");
                del.setStyle(baseBtn + "-fx-background-color: rgba(255,0,84,0.25); -fx-text-fill: white;");

                minus.setOnAction(e -> {
                    CartItem it = getTableView().getItems().get(getIndex());
                    try { panierCRUD.changeQty(Session.getUserId(), it.getIdProduit(), -1); refreshCartUI(); }
                    catch (SQLException ex) { ex.printStackTrace(); }
                });

                plus.setOnAction(e -> {
                    CartItem it = getTableView().getItems().get(getIndex());
                    try { panierCRUD.changeQty(Session.getUserId(), it.getIdProduit(), +1); refreshCartUI(); }
                    catch (SQLException ex) { ex.printStackTrace(); }
                });

                del.setOnAction(e -> {
                    CartItem it = getTableView().getItems().get(getIndex());
                    try { panierCRUD.remove(Session.getUserId(), it.getIdProduit()); refreshCartUI(); }
                    catch (SQLException ex) { ex.printStackTrace(); }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
                setText(null);
            }
        });

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

    /**
     * Règles logiques:
     * - LOGIN SCREEN: menu désactivé + panier badge caché
     * - VISITEUR: Produits seulement (pas panier, pas vendre)
     * - CLIENT connecté: Produits + Panier + Vendre
     * - COMMERCANT connecté: Ajout + Stats, panier caché, switch visible
     */
    private void updateUIForUserType() {
        boolean logged = Session.isLoggedIn();
        boolean isCommercant = logged && Session.isCommercant();
        boolean isClient = logged && !Session.isCommercant();
        boolean isVisitor = !logged;

        // switch mode seulement pour vrai commerçant
        if (modeSwitchBox != null) {
            modeSwitchBox.setVisible(isCommercant);
            modeSwitchBox.setManaged(isCommercant);
        }

        // si pas commercant => forcer client view
        if (!isCommercant) viewAsCommercant = false;

        boolean commercantView = isCommercantView(); // true uniquement si commerçant + toggle ON
        boolean clientView = !commercantView;

        // NAV:
        // - Add + Dashboard uniquement si commerçantView
        navAddBox.setVisible(commercantView);
        navAddBox.setManaged(commercantView);

        navDashboardBox.setVisible(commercantView);
        navDashboardBox.setManaged(commercantView);

        // - Panier seulement si client connecté ET clientView
        boolean showCart = isClient && clientView;
        navCartBox.setVisible(showCart);
        navCartBox.setManaged(showCart);

        // - Vendre seulement si client connecté (pas visiteur, pas commerçant)
        navBecomeCommercantBox.setVisible(isClient);
        navBecomeCommercantBox.setManaged(isClient);

        // Top cart badge uniquement si client connecté ET clientView
        if (cartBadgeBox != null) {
            cartBadgeBox.setVisible(showCart);
            cartBadgeBox.setManaged(showCart);
        }

        // Search: autorisé pour tout le monde sauf quand loginPane affiché (géré dans showLogin)
        if (searchField != null) searchField.setDisable(false);

        // Top subtitle
        if (topSubtitleLabel != null) {
            if (commercantView) topSubtitleLabel.setText("Espace Commerçant");
            else topSubtitleLabel.setText("Produits Locaux Tunisiens");
        }

        // User name
        if (userNameLabel != null) {
            if (logged) userNameLabel.setText(getNomPrenomUtilisateur(Session.getUserId()));
            else userNameLabel.setText("Visiteur");
        }

        // Toggle text
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

        // 🔒 login = pas logique de naviguer
        setNavEnabled(false);

        if (cartBadgeBox != null) {
            cartBadgeBox.setVisible(false);
            cartBadgeBox.setManaged(false);
        }
        if (searchField != null) searchField.setDisable(true);

        updateUIForUserType();
    }

    @FXML
    public void showProducts() {
        // ✅ dès qu'on sort du login => menu actif
        setNavEnabled(true);
        if (searchField != null) searchField.setDisable(false);

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
        if (!Session.isLoggedIn()) {
            showLogin();
            return;
        }
        if (isCommercantView()) {
            showAlert("🛒 Panier", "Passe en mode Client pour accéder au panier.");
            return;
        }

        hideAllScreens();
        updateUIForUserType();

        cartPane.setVisible(true);
        cartPane.setManaged(true);
        refreshCartUI();
    }

    @FXML
    public void showAddForm() {
        if (!isCommercantView()) {
            showAlert("⚠ Mode commerçant", "Passe en mode Commerçant pour ajouter.");
            return;
        }
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
        updateUIForUserType();

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
        updateUIForUserType();

        becomeCommercantPane.setVisible(true);
        becomeCommercantPane.setManaged(true);
    }

    @FXML
    public void showDashboard() {
        if (!isCommercantView()) {
            showAlert("⚠ Mode commerçant", "Passe en mode Commerçant pour voir le dashboard.");
            return;
        }
        if (!Session.isLoggedIn() || !Session.isCommercant()) {
            showLogin();
            return;
        }

        hideAllScreens();
        updateUIForUserType();

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Utilisateur";
    }

    // ==================== CLIENT MODE: CARDS VIEW ====================
    private void refreshClientProducts() {
        if (clientProductsGrid == null) return;
        clientProductsGrid.getChildren().clear();
        for (Produit p : produitsData) clientProductsGrid.getChildren().add(createProductCard(p));
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

        ImageView imageView = new ImageView();
        imageView.setFitWidth(196);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(196, 160);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imageView.setClip(clip);

        try {
            if (p.getImage() != null && !p.getImage().isBlank()) {
                imageView.setImage(new Image(p.getImage().trim(), true));
            }
        } catch (Exception ignored) {}

        Label nameLabel = new Label(p.getNom());
        nameLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-size: 14px;" +
                        "-fx-wrap-text: true;"
        );
        nameLabel.setMaxWidth(196);

        Label priceLabel = new Label(String.format("%.2f TND", p.getPrix()));
        priceLabel.setStyle(
                "-fx-text-fill: #FFBD00;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-size: 16px;"
        );

        Label regionLabel = new Label("📍 " + (p.getRegion() != null ? p.getRegion() : "Tunisie"));
        regionLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.70); -fx-font-size: 11px;");

        Label stockLabel = new Label(p.getStock() > 0 ? "✅ En stock" : "❌ Rupture");
        stockLabel.setStyle(
                "-fx-text-fill: " + (p.getStock() > 0 ? "#00FF9D" : "#FF0054") + ";" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: 800;"
        );

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
        try { produitsData.setAll(produitCRUD.afficher()); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void onSearch() {
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

    @FXML
    public void editSelectedProduct() { /* ton code inchangé */ }

    @FXML
    public void deleteSelectedProduct() { /* ton code inchangé */ }

    @FXML
    public void saveProduct() { /* ton code inchangé */ }

    @FXML
    private void clearForm() { /* ton code inchangé */ }

    // ==================== CART ====================
    private void refreshCartUI() {
        if (!Session.isLoggedIn()) {
            cartData.clear();
            if (cartCountLabel != null) cartCountLabel.setText("0");
            if (cartTotalLabel != null) cartTotalLabel.setText("Total: 0.00 TND");
            return;
        }

        try {
            cartData.setAll(panierCRUD.getActiveCart(Session.getUserId()));
            int totalQty = cartData.stream().mapToInt(CartItem::getQty).sum();
            double total = cartData.stream().mapToDouble(CartItem::getSubtotal).sum();

            if (cartCountLabel != null) cartCountLabel.setText(String.valueOf(totalQty));
            if (cartTotalLabel != null) cartTotalLabel.setText(String.format("Total: %.2f TND", total));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML public void qtyPlus() { /* ton code inchangé */ }
    @FXML public void qtyMinus() { /* ton code inchangé */ }
    @FXML public void removeFromCart() { /* ton code inchangé */ }
    @FXML public void clearCart() { /* ton code inchangé */ }
    @FXML public void checkoutNow() { /* ton code inchangé */ }

    // ==================== BECOME COMMERCANT ====================
    @FXML
    public void submitBecomeCommercant() { /* ton code inchangé */ }

    // ==================== DASHBOARD ====================
    private void updateDashboardStats() { /* ton code inchangé */ }

    // ==================== HELPERS ====================
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
    }

    private boolean isCommercantView() {
        return Session.isLoggedIn() && Session.isCommercant() && viewAsCommercant;
    }
}