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
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TextField categorieAutreField;
    // ==================== CART ====================
    @FXML private VBox cartItemsBox;
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
    @FXML private HBox cartPane;
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

    // ==================== VALIDATION LABELS (inline, temps réel) ====================
    // Ces labels sont créés dynamiquement à côté de chaque champ
    private Label errNom       = new Label();
    private Label errCat       = new Label();
    private Label errPrix      = new Label();
    private Label errStock     = new Label();
    private Label errImage     = new Label();
    private Label errRegion    = new Label();
    private Label errDesc      = new Label();
    private Label errEmail     = new Label();
    private Label errPassword  = new Label();
    private Label errBcNom     = new Label();
    private Label errBcPhone   = new Label();
    private Label errBcType    = new Label();
    private Label errBcMotiv   = new Label();

    // ==================== STYLE CONSTANTS ====================
    private static final String STYLE_FIELD_OK =
            "-fx-background-color: rgba(255,255,255,0.10);" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: rgba(255,255,255,0.18);" +
                    "-fx-border-radius: 12;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 12 14;";

    private static final String STYLE_FIELD_ERROR =
            "-fx-background-color: rgba(255,0,84,0.13);" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: #FF0054;" +
                    "-fx-border-width: 1.5;" +
                    "-fx-border-radius: 12;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 12 14;";

    private static final String STYLE_FIELD_OK_GREEN =
            "-fx-background-color: rgba(0,255,157,0.08);" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: #00FF9D;" +
                    "-fx-border-width: 1.5;" +
                    "-fx-border-radius: 12;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 12 14;";

    private static final String STYLE_ERR_LABEL =
            "-fx-text-fill: #FF0054;" +
                    "-fx-font-size: 10px;" +
                    "-fx-font-weight: 800;" +
                    "-fx-padding: 2 0 0 4;";

    // ==================== INITIALIZE ====================
    @FXML
    public void initialize() {
        setupProductsTable();
        setupFilters();
        refreshProducts();
        refreshCartUI();
        setupRealtimeValidation();   // ← validation temps réel
        updateUIForUserType();
        showLogin();
    }

    // ==================== REAL-TIME VALIDATION SETUP ====================
    /**
     * Configure tous les listeners de validation instantanée.
     * Chaque champ affiche un message rouge dès que la saisie est invalide.
     */
    private void setupRealtimeValidation() {
        styleErrorLabel(errNom);
        styleErrorLabel(errCat);
        styleErrorLabel(errPrix);
        styleErrorLabel(errStock);
        styleErrorLabel(errImage);
        styleErrorLabel(errRegion);
        styleErrorLabel(errDesc);
        styleErrorLabel(errEmail);
        styleErrorLabel(errPassword);
        styleErrorLabel(errBcNom);
        styleErrorLabel(errBcPhone);
        styleErrorLabel(errBcType);
        styleErrorLabel(errBcMotiv);

        // ── NOM PRODUIT ──────────────────────────────────────
        if (nomTextField != null) {
            injectErrorLabel(nomTextField, errNom);
            nomTextField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) {
                    showInlineError(nomTextField, errNom, "⚡ Le nom est obligatoire");
                } else if (!val.trim().matches("^[a-zA-ZÀ-ÿ0-9\\s\\-'()]+$")) {
                    // ✅ Bloque les caractères spéciaux type @, #, $, !, etc.
                    showInlineError(nomTextField, errNom, "⚡ Caractères spéciaux non autorisés (@, #, $...)");
                } else if (val.trim().matches("^\\d+$")) {
                    // ✅ Bloque les noms 100% numériques (ex: "123", "456")
                    showInlineError(nomTextField, errNom, "⚡ Le nom ne peut pas être uniquement des chiffres");
                } else if (val.trim().length() < 2) {
                    showInlineError(nomTextField, errNom, "⚡ Minimum 2 caractères");
                } else if (val.trim().length() > 100) {
                    showInlineError(nomTextField, errNom, "⚡ Maximum 100 caractères");
                } else {
                    clearInlineError(nomTextField, errNom);
                }
            });
        }

        // ── CATEGORIE ──────────────────────────────────────
        if (categorieField != null) {
            injectErrorLabel(categorieField, errCat);
            List<String> cats = List.of("Artisanat","Gastronomie","Textile","Bijoux","Art","Souvenirs");
            categorieField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) {
                    showInlineError(categorieField, errCat, "⚡ La catégorie est obligatoire");
                } else if (!cats.contains(val.trim())) {
                    showInlineError(categorieField, errCat, "⚡ Valeurs: Artisanat, Gastronomie, Textile, Bijoux, Art, Souvenirs");
                } else {
                    clearInlineError(categorieField, errCat);
                }
            });
        }

        // ── CATEGORIE AUTRE (champ libre) ──────────────────────────────
        if (categorieAutreField != null) {
            injectErrorLabel(categorieAutreField, errCat);
            categorieAutreField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) {
                    showInlineError(categorieAutreField, errCat, "⚡ Précisez votre catégorie");
                } else if (val.trim().length() < 2) {
                    showInlineError(categorieAutreField, errCat, "⚡ Minimum 2 caractères");
                } else if (!val.trim().matches("^[a-zA-ZÀ-ÿ\\s\\-']+$")) {
                    showInlineError(categorieAutreField, errCat, "⚡ Lettres uniquement");
                } else {
                    clearInlineError(categorieAutreField, errCat);
                }
            });
        }

        // ── PRIX ──────────────────────────────────────
        if (prixTextField != null) {
            injectErrorLabel(prixTextField, errPrix);
            prixTextField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) {
                    showInlineError(prixTextField, errPrix, "⚡ Le prix est obligatoire");
                } else {
                    try {
                        double prix = Double.parseDouble(val.trim());
                        if (prix <= 0)       showInlineError(prixTextField, errPrix, "⚡ Le prix doit être > 0");
                        else if (prix > 99999.99) showInlineError(prixTextField, errPrix, "⚡ Maximum 99 999.99 TND");
                        else                 clearInlineError(prixTextField, errPrix);
                    } catch (NumberFormatException e) {
                        showInlineError(prixTextField, errPrix, "⚡ Chiffres uniquement (ex: 12.50)");
                    }
                }
            });
        }

        // ── STOCK ──────────────────────────────────────
        if (stockField != null) {
            injectErrorLabel(stockField, errStock);
            stockField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) {
                    showInlineError(stockField, errStock, "⚡ Le stock est obligatoire");
                } else {
                    try {
                        int stock = Integer.parseInt(val.trim());
                        if (stock < 0)      showInlineError(stockField, errStock, "⚡ Stock ne peut pas être négatif");
                        else if (stock > 99999) showInlineError(stockField, errStock, "⚡ Maximum 99 999");
                        else                clearInlineError(stockField, errStock);
                    } catch (NumberFormatException e) {
                        showInlineError(stockField, errStock, "⚡ Entier uniquement (ex: 10)");
                    }
                }
            });
        }

        // ── IMAGE URL ──────────────────────────────────────
        if (imageField != null) {
            injectErrorLabel(imageField, errImage);
            imageField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) {
                    clearInlineError(imageField, errImage); // optionnel
                } else if (val.contains("encrypted-tbn") || val.contains("gstatic.com/images")) {
                    showInlineError(imageField, errImage, "🚫 URL Google Images non supportée → utilisez Unsplash ou Wikipedia");
                } else if (!val.trim().startsWith("http://") && !val.trim().startsWith("https://")
                        && !val.trim().startsWith("file:") && !new java.io.File(val.trim()).exists()) {
                    showInlineError(imageField, errImage, "⚡ URL invalide → doit commencer par https://");
                } else {
                    clearInlineError(imageField, errImage);
                }
            });
        }

        // ── DESCRIPTION ──────────────────────────────────────
        if (descriptionField != null) {
            injectErrorLabel(descriptionField, errDesc);
            descriptionField.textProperty().addListener((obs, old, val) -> {
                if (val.length() > 500) {
                    showInlineError(descriptionField, errDesc, "⚡ Maximum 500 caractères (" + val.length() + "/500)");
                } else {
                    clearInlineError(descriptionField, errDesc);
                }
            });
        }

        // ── EMAIL (LOGIN) ──────────────────────────────────────
        if (emailField != null) {
            injectErrorLabel(emailField, errEmail);
            emailField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) {
                    clearInlineError(emailField, errEmail);
                } else if (!val.trim().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                    showInlineError(emailField, errEmail, "⚡ Format invalide (ex: nom@email.com)");
                } else {
                    clearInlineError(emailField, errEmail);
                }
            });
        }

        // ── MOT DE PASSE (LOGIN) ──────────────────────────────────────
        if (passwordField != null) {
            injectErrorLabel(passwordField, errPassword);
            passwordField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) {
                    clearInlineError(passwordField, errPassword);
                } else if (val.length() < 4) {
                    showInlineError(passwordField, errPassword, "⚡ Minimum 4 caractères");
                } else {
                    clearInlineError(passwordField, errPassword);
                }
            });
        }

        // ── BECOME COMMERCANT: NOM ──────────────────────────────────────
        if (becomeCommercantNom != null) {
            injectErrorLabel(becomeCommercantNom, errBcNom);
            becomeCommercantNom.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) {
                    clearInlineError(becomeCommercantNom, errBcNom);
                } else if (!val.trim().matches("^[a-zA-ZÀ-ÿ\\s\\-']+$")) {
                    // ✅ Bloque les chiffres et caractères spéciaux
                    showInlineError(becomeCommercantNom, errBcNom, "⚡ Lettres uniquement (pas de chiffres)");
                } else if (val.trim().length() < 3) {
                    showInlineError(becomeCommercantNom, errBcNom, "⚡ Minimum 3 caractères");
                } else if (val.trim().length() > 60) {
                    showInlineError(becomeCommercantNom, errBcNom, "⚡ Maximum 60 caractères");
                } else {
                    clearInlineError(becomeCommercantNom, errBcNom);
                }
            });
        }

        // ── BECOME COMMERCANT: TELEPHONE ──────────────────────────────────────
        if (becomeCommercantPhone != null) {
            injectErrorLabel(becomeCommercantPhone, errBcPhone);
            becomeCommercantPhone.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) {
                    clearInlineError(becomeCommercantPhone, errBcPhone);
                } else if (!val.replaceAll("[\\s\\-+]","").matches("^(\\+?216)?[2-9]\\d{7}$")) {
                    showInlineError(becomeCommercantPhone, errBcPhone, "⚡ Format tunisien invalide (ex: +216 22 123 456)");
                } else {
                    clearInlineError(becomeCommercantPhone, errBcPhone);
                }
            });
        }

        // ── BECOME COMMERCANT: TYPE ──────────────────────────────────────
        if (becomeCommercantType != null) {
            injectErrorLabel(becomeCommercantType, errBcType);
            becomeCommercantType.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) {
                    clearInlineError(becomeCommercantType, errBcType);
                } else if (val.trim().length() < 3) {
                    showInlineError(becomeCommercantType, errBcType, "⚡ Minimum 3 caractères");
                } else {
                    clearInlineError(becomeCommercantType, errBcType);
                }
            });
        }

        // ── BECOME COMMERCANT: MOTIVATION ──────────────────────────────────────
        if (becomeCommercantMotivation != null) {
            // Pour TextArea on injecte différemment
            becomeCommercantMotivation.textProperty().addListener((obs, old, val) -> {
                if (!val.isBlank() && val.trim().length() < 20) {
                    errBcMotiv.setText("⚡ Minimum 20 caractères (" + val.trim().length() + "/20)");
                    errBcMotiv.setVisible(true);
                    becomeCommercantMotivation.setStyle(
                            "-fx-background-radius: 12; -fx-border-color: #FF0054;" +
                                    "-fx-border-width: 1.5; -fx-border-radius: 12;" +
                                    "-fx-text-fill: white; -fx-padding: 10 12;"
                    );
                } else {
                    errBcMotiv.setText("");
                    errBcMotiv.setVisible(false);
                    becomeCommercantMotivation.setStyle(
                            "-fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.18);" +
                                    "-fx-border-radius: 12; -fx-text-fill: white; -fx-padding: 10 12;"
                    );
                }
            });
        }
    }

    // ==================== HELPERS VALIDATION ====================

    /** Style un label d'erreur inline */
    private void styleErrorLabel(Label lbl) {
        lbl.setStyle(STYLE_ERR_LABEL);
        lbl.setWrapText(true);
        lbl.setMaxWidth(400);
        lbl.setVisible(false);
        lbl.setManaged(false);
    }

    /**
     * Injecte le label d'erreur juste sous le TextField dans son parent VBox.
     * Si le parent n'est pas un VBox, on ajoute quand même (best effort).
     */
    private void injectErrorLabel(TextField field, Label errLabel) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.Parent parent = field.getParent();
            if (parent instanceof VBox vbox) {
                int idx = vbox.getChildren().indexOf(field);
                if (idx >= 0 && !vbox.getChildren().contains(errLabel)) {
                    vbox.getChildren().add(idx + 1, errLabel);
                }
            }
        });
    }

    /** Affiche le message d'erreur rouge sous le champ */
    private void showInlineError(TextField field, Label errLabel, String message) {
        field.setStyle(STYLE_FIELD_ERROR);
        errLabel.setText(message);
        errLabel.setVisible(true);
        errLabel.setManaged(true);
    }

    /** Efface l'erreur et passe le champ en vert (valide) */
    private void clearInlineError(TextField field, Label errLabel) {
        field.setStyle(STYLE_FIELD_OK_GREEN);
        errLabel.setText("");
        errLabel.setVisible(false);
        errLabel.setManaged(false);
    }

    /** Vérifie si un champ a une erreur visible */
    private boolean hasError(Label errLabel) {
        return errLabel.isVisible() && !errLabel.getText().isBlank();
    }

    // ==================== NAV ENABLE/DISABLE ====================
    private void setNavEnabled(boolean enabled) {
        if (leftNav == null) return;
        leftNav.setDisable(!enabled);
        leftNav.setOpacity(enabled ? 1.0 : 0.35);
    }

    private void setTopLoginMode(boolean isLogin) {
        if (topActionsBox != null) { topActionsBox.setVisible(!isLogin); topActionsBox.setManaged(!isLogin); }
        if (searchBox != null) { searchBox.setVisible(!isLogin); searchBox.setManaged(!isLogin); }
        if (cartBadgeBox != null) { cartBadgeBox.setVisible(!isLogin); cartBadgeBox.setManaged(!isLogin); }
        if (modeSwitchBox != null) {
            boolean showMode = !isLogin && Session.isLoggedIn() && Session.isCommercant();
            modeSwitchBox.setVisible(showMode); modeSwitchBox.setManaged(showMode);
        }
        if (userBox != null) { userBox.setVisible(!isLogin); userBox.setManaged(!isLogin); }
        if (searchField != null) searchField.setDisable(isLogin);
    }

    // ==================== SETUP ====================
    private void setupFilters() {
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Toutes","Artisanat","Gastronomie","Textile","Bijoux","Art","Souvenirs"
        );
        if (clientCategoryFilter != null) { clientCategoryFilter.setItems(categories); clientCategoryFilter.setValue("Toutes"); }

        ObservableList<String> regions = FXCollections.observableArrayList(
                "Toutes","Tunis","Ariana","Ben Arous","Manouba","Nabeul","Zaghouan",
                "Bizerte","Béja","Jendouba","Le Kef","Siliana","Sousse","Monastir",
                "Mahdia","Sfax","Kairouan","Kasserine","Sidi Bouzid","Gabès",
                "Médenine","Tataouine","Gafsa","Tozeur","Kebili"
        );
        if (clientRegionFilter != null) { clientRegionFilter.setItems(regions); clientRegionFilter.setValue("Toutes");
        }
        // ✅ ComboBox catégorie du formulaire produit
        if (categorieCombo != null) {
            ObservableList<String> cats = FXCollections.observableArrayList(
                    "Artisanat", "Gastronomie", "Textile",
                    "Bijoux", "Art", "Souvenirs", "✏ Autre..."
            );
            categorieCombo.setItems(cats);

            // Style des items de la liste déroulante
            categorieCombo.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.10);" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: rgba(255,255,255,0.18);" +
                            "-fx-border-radius: 12;" +
                            "-fx-text-fill: white;"
            );
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
            { iv.setFitWidth(60); iv.setFitHeight(60); iv.setPreserveRatio(true); iv.setSmooth(true); }
            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isBlank()) { setGraphic(null); }
                else {
                    try { iv.setImage(new Image(url.trim(), true)); setGraphic(iv); }
                    catch (Exception e) { setGraphic(null); }
                }
                setText(null);
            }
        });
        produitsTable.setItems(produitsData);
    }

    // ==================== NAVIGATION & UI MODE ====================
    private void hideAllScreens() {
        loginPane.setVisible(false);               loginPane.setManaged(false);
        clientProductsPane.setVisible(false);      clientProductsPane.setManaged(false);
        commercantProductsPane.setVisible(false);  commercantProductsPane.setManaged(false);
        formScrollPane.setVisible(false);          formScrollPane.setManaged(false);
        cartPane.setVisible(false);                cartPane.setManaged(false);
        becomeCommercantPane.setVisible(false);    becomeCommercantPane.setManaged(false);
        dashboardPane.setVisible(false);           dashboardPane.setManaged(false);
    }

    private void updateUIForUserType() {
        boolean logged = Session.isLoggedIn();
        boolean isCommercant = logged && Session.isCommercant();
        boolean isClient = logged && !Session.isCommercant();

        if (modeSwitchBox != null) { modeSwitchBox.setVisible(isCommercant); modeSwitchBox.setManaged(isCommercant); }
        if (!isCommercant) viewAsCommercant = false;

        boolean commercantView = isCommercantView();

        navAddBox.setVisible(commercantView);          navAddBox.setManaged(commercantView);
        navDashboardBox.setVisible(commercantView);    navDashboardBox.setManaged(commercantView);

        boolean showCart = isClient && !commercantView;
        navCartBox.setVisible(showCart);               navCartBox.setManaged(showCart);
        navBecomeCommercantBox.setVisible(isClient);   navBecomeCommercantBox.setManaged(isClient);

        if (cartBadgeBox != null) { cartBadgeBox.setVisible(showCart); cartBadgeBox.setManaged(showCart); }
        if (topSubtitleLabel != null) topSubtitleLabel.setText(commercantView ? "Espace Commerçant" : "Produits Locaux Tunisiens");
        if (userNameLabel != null) userNameLabel.setText(logged ? getNomPrenomUtilisateur(Session.getUserId()) : "Visiteur");
        if (modeToggle != null && isCommercant) { modeToggle.setSelected(commercantView); modeToggle.setText(commercantView ? "Commerçant" : "Client"); }
    }

    // ==================== SCREENS ====================
    @FXML
    public void showLogin() {
        hideAllScreens();
        loginPane.setVisible(true); loginPane.setManaged(true);
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
            commercantProductsPane.setVisible(true); commercantProductsPane.setManaged(true);
            refreshCommercantProducts();
        } else {
            clientProductsPane.setVisible(true); clientProductsPane.setManaged(true);
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
        cartPane.setVisible(true); cartPane.setManaged(true);
        refreshCartUI();
    }
    @FXML
    private void onCategorieChanged() {
        if (categorieCombo == null) return;

        String selected = categorieCombo.getValue();
        boolean isAutre = "✏ Autre...".equals(selected);

        // Afficher/masquer le champ "Autre"
        categorieAutreField.setVisible(isAutre);
        categorieAutreField.setManaged(isAutre);

        if (isAutre) {
            // Focus automatique sur le champ libre
            categorieAutreField.requestFocus();
            categorieAutreField.setPromptText("✏ Ex: Poterie, Parfums, Épices...");
            errCat.setText("");
            errCat.setVisible(false);
            errCat.setManaged(false);
        } else if (selected != null && !selected.isBlank()) {
            // Sync avec categorieField caché
            categorieField.setText(selected);
            errCat.setText("");
            errCat.setVisible(false);
            errCat.setManaged(false);
        }
    }
    // ==================== LOGIN ====================
    @FXML
    public void doLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass  = passwordField.getText() == null ? "" : passwordField.getText().trim();

        boolean valid = true;

        // Email
        if (email.isEmpty()) {
            showInlineError(emailField, errEmail, "⚡ L'email est obligatoire");
            valid = false;
        } else if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showInlineError(emailField, errEmail, "⚡ Format invalide (ex: nom@email.com)");
            valid = false;
        }

        // Mot de passe
        if (pass.isEmpty()) {
            showInlineError(passwordField, errPassword, "⚡ Le mot de passe est obligatoire");
            valid = false;
        } else if (pass.length() < 4) {
            showInlineError(passwordField, errPassword, "⚡ Minimum 4 caractères");
            valid = false;
        }

        if (!valid) {
            loginStatusLabel.setStyle("-fx-text-fill: #FF0054; -fx-font-weight: 800;");
            loginStatusLabel.setText("⚠ Corrige les erreurs ci-dessus.");
            return;
        }

        int fakeId = 1;
        String fakeType = email.toLowerCase().contains("shop") ? "COMMERCANT" : "CLIENT";
        Session.setUser(fakeId, fakeType);

        loginStatusLabel.setStyle("-fx-text-fill: #00FF9D; -fx-font-weight: 800;");
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
                    String nom    = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String full   = (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
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
        System.out.println("🔄 Refreshing " + produitsData.size() + " product cards");
        for (Produit p : produitsData) {
            System.out.println("🖼 Produit: " + p.getNom() + " | Image URL: [" + p.getImage() + "]");
            clientProductsGrid.getChildren().add(createProductCard(p));
        }
        System.out.println("✅ Created " + clientProductsGrid.getChildren().size() + " cards");
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

        // ── IMAGE CONTAINER ──────────────────────────────────────
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefWidth(196);
        imageContainer.setPrefHeight(160);
        imageContainer.setMinHeight(160);
        imageContainer.setMaxHeight(160);
        imageContainer.setStyle("-fx-background-color: rgba(255,189,0,0.10); -fx-background-radius: 12;");

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(196, 160);
        clip.setArcWidth(16); clip.setArcHeight(16);
        imageContainer.setClip(clip);

        final ImageView imageView = new ImageView();
        imageView.setFitWidth(196);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageContainer.getChildren().add(imageView);

        // ── CHARGEMENT IMAGE ROBUSTE ──────────────────────────────
        final String imageUrl = p.getImage();
        if (imageUrl != null && !imageUrl.isBlank()) {
            Thread t = new Thread(() -> {
                try {
                    String finalUrl = imageUrl.trim();

                    // Bloquer URLs Google Images
                    if (finalUrl.contains("encrypted-tbn") || finalUrl.contains("gstatic.com/images")) {
                        System.err.println("⛔ URL Google Images bloquée: " + finalUrl);
                        return;
                    }

                    // Chemin local → file:///
                    if (!finalUrl.startsWith("http") && !finalUrl.startsWith("file:")) {
                        java.io.File f = new java.io.File(finalUrl);
                        finalUrl = f.exists() ? f.toURI().toString()
                                : "file:///" + finalUrl.replace("\\", "/");
                    }

                    System.out.println("🔍 [" + p.getNom() + "] Loading: " + finalUrl);
                    Image img = new Image(finalUrl, 196, 160, false, true, false);

                    if (!img.isError()) {
                        final Image done = img;
                        javafx.application.Platform.runLater(() -> imageView.setImage(done));
                        System.out.println("✅ [" + p.getNom() + "] OK");
                    } else {
                        System.err.println("❌ [" + p.getNom() + "] Error for: " + finalUrl);
                    }
                } catch (Exception ex) {
                    System.err.println("❌ [" + p.getNom() + "] Exception: " + ex.getMessage());
                }
            });
            t.setDaemon(true);
            t.start();
        }

        // ── LABELS ───────────────────────────────────────────────
        Label nameLabel = new Label(p.getNom());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px;");
        nameLabel.setMaxWidth(196); nameLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("%.2f TND", p.getPrix()));
        priceLabel.setStyle("-fx-text-fill: #FFBD00; -fx-font-weight: 900; -fx-font-size: 16px;");

        Label regionLabel = new Label("📍 " + (p.getRegion() != null ? p.getRegion() : "Tunisie"));
        regionLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.70); -fx-font-size: 11px;");

        Label stockLabel = new Label(p.getStock() > 0 ? "✅ En stock" : "❌ Rupture");
        stockLabel.setStyle("-fx-text-fill: " + (p.getStock() > 0 ? "#00FF9D" : "#FF0054") +
                "; -fx-font-size: 10px; -fx-font-weight: 800;");

        Button addBtn = new Button("🛒 Ajouter");
        addBtn.setStyle("-fx-background-color: #FFBD00; -fx-text-fill: #390099;" +
                "-fx-background-radius: 999; -fx-padding: 8 16;" +
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
                .filter(p -> catFilter == null || catFilter.equals("Toutes") || (p.getCategorie() != null && p.getCategorie().equals(catFilter)))
                .filter(p -> regFilter == null || regFilter.equals("Toutes") || (p.getRegion() != null && p.getRegion().equals(regFilter)))
                .collect(Collectors.toList());
        clientProductsGrid.getChildren().clear();
        for (Produit p : filtered) clientProductsGrid.getChildren().add(createProductCard(p));
    }

    // ==================== COMMERCANT MODE ====================
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

    private void refreshProducts() {
        try {
            List<Produit> list = produitCRUD.afficher();
            produitsData.setAll(list);
            System.out.println("✅ Loaded " + list.size() + " products from DB");
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

    // ==================== CART UI ====================
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
            for (CartItem it : cartData) cartItemsBox.getChildren().add(createCartItemCard(it));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private HBox createCartItemCard(CartItem it) {
        ImageView iv = new ImageView();
        iv.setFitWidth(56); iv.setFitHeight(56);
        iv.setPreserveRatio(true); iv.setSmooth(true);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(56, 56);
        clip.setArcWidth(14); clip.setArcHeight(14);
        iv.setClip(clip);

        String imageUrl = it.getImage();
        if (imageUrl != null && !imageUrl.isBlank()) {
            try {
                Image img = new Image(imageUrl.trim(), 56, 56, true, true, true);
                iv.setImage(img);
            } catch (Exception ignored) {}
        }

        Label name  = new Label(it.getNom());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 13px;");
        Label price = new Label(String.format("%.2f TND", it.getPrix()));
        price.setStyle("-fx-text-fill: #FFBD00; -fx-font-weight: 900;");
        Label qty   = new Label("x" + it.getQty());
        qty.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-weight: 800;");

        VBox info = new VBox(4, name, price, qty);

        Button minus = new Button("➖");
        Button plus  = new Button("➕");
        Button del   = new Button("🗑");

        minus.setOnAction(e -> { try { panierCRUD.changeQty(Session.getUserId(), it.getIdProduit(), -1); refreshCartUI(); } catch (Exception ignored) {} });
        plus.setOnAction(e  -> { try { panierCRUD.changeQty(Session.getUserId(), it.getIdProduit(), +1); refreshCartUI(); } catch (Exception ignored) {} });
        del.setOnAction(e   -> { try { panierCRUD.remove(Session.getUserId(), it.getIdProduit()); refreshCartUI(); } catch (Exception ignored) {} });

        String base = "-fx-background-radius: 999; -fx-padding: 6 10; -fx-font-weight: 900; -fx-cursor: hand;";
        minus.setStyle(base + "-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: white;");
        plus.setStyle(base  + "-fx-background-color: rgba(255,189,0,0.25); -fx-text-fill: #FFBD00;");
        del.setStyle(base   + "-fx-background-color: rgba(255,0,84,0.25); -fx-text-fill: white;");

        HBox actions = new HBox(8, minus, plus, del);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, iv, info, spacer, actions);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(0,0,0,0.22); -fx-background-radius: 16; -fx-padding: 10;");
        return row;
    }

    // ==================== OTHER SCREENS ====================
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

    @FXML
    public void showAddForm() {
        if (!Session.isLoggedIn() || !Session.isCommercant()) { showAlert("⚠ Accès refusé", "Connecte-toi en tant que commerçant."); showLogin(); return; }
        if (!isCommercantView()) { showAlert("⚠ Mode Commerçant", "Active le mode Commerçant pour ajouter."); return; }
        hideAllScreens();
        updateUIForUserType();
        formScrollPane.setVisible(true); formScrollPane.setManaged(true);
        clearForm();
        if (formTitleLabel != null) formTitleLabel.setText("➕ Ajouter Produit");
        if (saveBtn != null) saveBtn.setText("✅ Enregistrer");
    }

    @FXML
    public void showDashboard() {
        if (!Session.isLoggedIn() || !Session.isCommercant()) { showLogin(); return; }
        if (!isCommercantView()) { showAlert("⚠ Mode Commerçant", "Active le mode Commerçant pour voir le dashboard."); return; }
        hideAllScreens();
        updateUIForUserType();
        dashboardPane.setVisible(true); dashboardPane.setManaged(true);
        updateDashboardStats();
    }

    @FXML
    public void showBecomeCommercant() {
        if (!Session.isLoggedIn()) { showLogin(); return; }
        if (Session.isCommercant()) { showAlert("✅ Info", "Vous êtes déjà commerçant."); return; }
        hideAllScreens();
        updateUIForUserType();
        becomeCommercantPane.setVisible(true); becomeCommercantPane.setManaged(true);
    }

    @FXML
    public void editSelectedProduct() {
        Produit selected = produitsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("✏ Modifier", "Sélectionne un produit d'abord."); return; }
        hideAllScreens();
        updateUIForUserType();
        formScrollPane.setVisible(true); formScrollPane.setManaged(true);
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
        if (selected == null) { showAlert("🗑 Supprimer", "Sélectionne un produit d'abord."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation"); confirm.setHeaderText(null);
        confirm.setContentText("Supprimer : " + selected.getNom() + " ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            produitCRUD.supprimer(selected.getIdProduit());
            refreshCommercantProducts();
            showAlert("✅ Supprimé", "Produit supprimé.");
        } catch (SQLException e) { e.printStackTrace(); showAlert("❌ Erreur", "Impossible de supprimer."); }
    }

    // ==================== SAVE PRODUCT (avec contrôles complets) ====================
    @FXML
    public void saveProduct() {
        if (!Session.isLoggedIn() || !Session.isCommercant() || !isCommercantView()) {
            showAlert("⚠ Accès refusé", "Mode commerçant requis.");
            return;
        }

        String nom   = nomTextField.getText()    == null ? "" : nomTextField.getText().trim();
// ✅ Récupère la catégorie : combo ou champ libre "Autre"
        String selectedCat = categorieCombo != null ? categorieCombo.getValue() : "";
        String cat;
        if ("✏ Autre...".equals(selectedCat)) {
            cat = categorieAutreField.getText() == null ? "" : categorieAutreField.getText().trim();
            if (cat.isEmpty()) {
                showInlineError(categorieAutreField, errCat, "⚡ Précisez votre catégorie");
                return;
            }
        } else {
            cat = selectedCat == null ? "" : selectedCat.trim();
            if (cat.isEmpty()) {
                errCat.setText("⚡ Choisissez une catégorie");
                errCat.setVisible(true);
                errCat.setManaged(true);
                categorieCombo.setStyle(
                        "-fx-background-color: rgba(255,0,84,0.13);" +
                                "-fx-background-radius: 12;" +
                                "-fx-border-color: #FF0054;" +
                                "-fx-border-width: 1.5;" +
                                "-fx-border-radius: 12;"
                );
                return;
            }
        }
        String region= regionField.getText()     == null ? "" : regionField.getText().trim();
        String img   = imageField.getText()      == null ? "" : imageField.getText().trim();
        String desc  = descriptionField.getText()== null ? "" : descriptionField.getText().trim();

        boolean valid = true;

        // 1. Nom
        if (nom.isEmpty()) {
            showInlineError(nomTextField, errNom, "⚡ Le nom est obligatoire"); valid = false;
        } else if (nom.length() < 2) {
            showInlineError(nomTextField, errNom, "⚡ Minimum 2 caractères"); valid = false;
        } else if (nom.length() > 100) {
            showInlineError(nomTextField, errNom, "⚡ Maximum 100 caractères"); valid = false;
        } else {
            clearInlineError(nomTextField, errNom);
        }

        // 2. Catégorie
        List<String> catsValides = List.of("Artisanat","Gastronomie","Textile","Bijoux","Art","Souvenirs");
        if (cat.isEmpty()) {
            showInlineError(categorieField, errCat, "⚡ La catégorie est obligatoire"); valid = false;
        } else if (!catsValides.contains(cat)) {
            showInlineError(categorieField, errCat, "⚡ Valeurs: Artisanat, Gastronomie, Textile, Bijoux, Art, Souvenirs"); valid = false;
        } else {
            clearInlineError(categorieField, errCat);
        }

        // 3. Prix
        double prix = 0;
        try {
            prix = Double.parseDouble(prixTextField.getText().trim());
            if (prix <= 0)          { showInlineError(prixTextField, errPrix, "⚡ Le prix doit être > 0"); valid = false; }
            else if (prix > 99999)  { showInlineError(prixTextField, errPrix, "⚡ Maximum 99 999 TND"); valid = false; }
            else                      clearInlineError(prixTextField, errPrix);
        } catch (NumberFormatException ex) {
            showInlineError(prixTextField, errPrix, "⚡ Chiffres uniquement (ex: 12.50)"); valid = false;
        }

        // 4. Stock
        int stock = 0;
        try {
            stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0)          { showInlineError(stockField, errStock, "⚡ Stock ne peut pas être négatif"); valid = false; }
            else if (stock > 99999) { showInlineError(stockField, errStock, "⚡ Maximum 99 999"); valid = false; }
            else                      clearInlineError(stockField, errStock);
        } catch (NumberFormatException ex) {
            showInlineError(stockField, errStock, "⚡ Entier uniquement (ex: 10)"); valid = false;
        }

        // 5. Image URL
        if (!img.isEmpty()) {
            if (img.contains("encrypted-tbn") || img.contains("gstatic.com/images")) {
                showInlineError(imageField, errImage, "🚫 URL Google Images non supportée → Unsplash ou Wikipedia"); valid = false;
            } else if (!img.startsWith("http://") && !img.startsWith("https://")
                    && !img.startsWith("file:") && !new java.io.File(img).exists()) {
                showInlineError(imageField, errImage, "⚡ URL invalide → doit commencer par https://"); valid = false;
            } else {
                clearInlineError(imageField, errImage);
            }
        }

        // 6. Description
        if (desc.length() > 500) {
            showInlineError(descriptionField, errDesc, "⚡ Maximum 500 caractères"); valid = false;
        }

        if (!valid) return; // Stoppe si erreurs

        // ── SAUVEGARDE ─────────────────────────────────────────
        try {
            String idTxt = idProduitHidden.getText() == null ? "" : idProduitHidden.getText().trim();
            if (idTxt.isEmpty()) {
                Produit p = new Produit(Session.getUserId(), nom, desc, prix, region, cat, stock, img);
                produitCRUD.ajouter(p);
                showAlert("✅ Ajout", "Produit ajouté avec succès.");
            } else {
                int id = Integer.parseInt(idTxt);
                Produit p = new Produit(id, Session.getUserId(), nom, desc, prix, region, cat, stock, img);
                produitCRUD.modifier(p);
                showAlert("✅ Modifié", "Produit mis à jour avec succès.");
            }
            refreshProducts();
            refreshCommercantProducts();
            refreshClientProducts();
            clearForm();
            showProducts();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("❌ Erreur DB", "Impossible d'enregistrer : " + e.getMessage());
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
        imageField.clear();

        // ✅ Reset ComboBox catégorie
        if (categorieCombo != null) categorieCombo.setValue(null);
        if (categorieAutreField != null) {
            categorieAutreField.clear();
            categorieAutreField.setVisible(false);
            categorieAutreField.setManaged(false);
        }
        if (categorieField != null) categorieField.clear();

        // Reset styles...
        for (TextField f : new TextField[]{nomTextField, descriptionField,
                prixTextField, stockField, regionField, imageField}) {
            if (f != null) f.setStyle(STYLE_FIELD_OK);
        }
        for (Label l : new Label[]{errNom, errCat, errPrix, errStock, errImage, errRegion, errDesc}) {
            l.setVisible(false); l.setManaged(false); l.setText("");
        }
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

    // ==================== BECOME COMMERCANT ====================
    @FXML
    public void submitBecomeCommercant() {
        if (!Session.isLoggedIn()) { showLogin(); return; }
        if (Session.isCommercant()) { showAlert("✅ Info", "Vous êtes déjà commerçant."); return; }

        String nom   = becomeCommercantNom.getText()       == null ? "" : becomeCommercantNom.getText().trim();
        String phone = becomeCommercantPhone.getText()     == null ? "" : becomeCommercantPhone.getText().trim();
        String typeP = becomeCommercantType.getText()      == null ? "" : becomeCommercantType.getText().trim();
        String mot   = becomeCommercantMotivation.getText()== null ? "" : becomeCommercantMotivation.getText().trim();

        boolean valid = true;

        if (nom.isEmpty()) {
            showInlineError(becomeCommercantNom, errBcNom, "⚡ Le nom complet est obligatoire"); valid = false;
        } else if (!nom.matches("^[a-zA-ZÀ-ÿ\\s\\-']+$")) {
            // ✅ Bloque les chiffres à la soumission aussi
            showInlineError(becomeCommercantNom, errBcNom, "⚡ Lettres uniquement (pas de chiffres)"); valid = false;
        } else if (nom.length() < 3) {
            showInlineError(becomeCommercantNom, errBcNom, "⚡ Minimum 3 caractères"); valid = false;
        } else {
            clearInlineError(becomeCommercantNom, errBcNom);
        }

        if (phone.isEmpty()) {
            showInlineError(becomeCommercantPhone, errBcPhone, "⚡ Le téléphone est obligatoire"); valid = false;
        } else if (!phone.replaceAll("[\\s\\-+]","").matches("^(\\+?216)?[2-9]\\d{7}$")) {
            showInlineError(becomeCommercantPhone, errBcPhone, "⚡ Format tunisien invalide (ex: +216 22 123 456)"); valid = false;
        } else {
            clearInlineError(becomeCommercantPhone, errBcPhone);
        }

        if (typeP.isEmpty()) {
            showInlineError(becomeCommercantType, errBcType, "⚡ Le type de produits est obligatoire"); valid = false;
        } else if (typeP.length() < 3) {
            showInlineError(becomeCommercantType, errBcType, "⚡ Minimum 3 caractères"); valid = false;
        } else {
            clearInlineError(becomeCommercantType, errBcType);
        }

        if (mot.isEmpty()) {
            if (becomeCommercantStatusLabel != null) becomeCommercantStatusLabel.setText("⚡ La motivation est obligatoire.");
            valid = false;
        } else if (mot.length() < 20) {
            if (becomeCommercantStatusLabel != null) becomeCommercantStatusLabel.setText("⚡ Minimum 20 caractères (" + mot.length() + "/20).");
            valid = false;
        } else {
            if (becomeCommercantStatusLabel != null) becomeCommercantStatusLabel.setText("");
        }

        if (!valid) return;

        String sql = "UPDATE utilisateur SET type='COMMERCANT' WHERE id=?";
        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());
            int updated = ps.executeUpdate();
            if (updated == 1) {
                Session.setUser(Session.getUserId(), "COMMERCANT");
                viewAsCommercant = true;
                if (modeToggle != null) { modeToggle.setSelected(true); modeToggle.setText("Commerçant"); }
                if (becomeCommercantStatusLabel != null) becomeCommercantStatusLabel.setText("");
                showAlert("🎉 Bienvenue !", "Votre compte est maintenant Commerçant ✅");
                refreshCartUI();
                updateUIForUserType();
                showAddForm();
            } else {
                showAlert("❌ Erreur", "Impossible de changer le type utilisateur.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("❌ Erreur", "Erreur DB: " + e.getMessage());
        }
    }

    // ==================== DASHBOARD ====================
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

    // ==================== ALERT ====================
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
