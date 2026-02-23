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

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;

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

    // ==================== LEFT NAV ====================
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

    // ==================== CLIENT ====================
    @FXML private VBox clientProductsGrid;
    @FXML private ComboBox<String> clientCategoryFilter;
    @FXML private ComboBox<String> clientRegionFilter;

    // ==================== COMMERCANT TABLE ====================
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

    // ==================== BECOME COMMERCANT (champs FXML cachés conservés) ====================
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
    private final PanierCRUD panierCRUD   = new PanierCRUD();

    private final ObservableList<Produit> produitsData = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cartData    = FXCollections.observableArrayList();
    @FXML private Button switchModeBtn;
    @FXML private Button becomeCommercantBtn;

    // Variable pour savoir si on est en mode commerçant
    private boolean enModeCommercant = false;
    // ==================== VALIDATION LABELS ====================
    private Label errNom = new Label(), errCat = new Label(), errPrix = new Label();
    private Label errStock = new Label(), errImage = new Label(), errRegion = new Label();
    private Label errDesc = new Label(), errEmail = new Label(), errPassword = new Label();
    private Label errBcNom = new Label(), errBcPhone = new Label();
    private Label errBcType = new Label(), errBcMotiv = new Label();

    private StackPane overlayContainer;

    // ==================== STYLE CONSTANTS ====================
    private static final String STYLE_FIELD_OK =
            "-fx-background-color: rgba(255,255,255,0.10); -fx-background-radius: 12;" +
                    "-fx-border-color: rgba(255,255,255,0.18); -fx-border-radius: 12;" +
                    "-fx-text-fill: white; -fx-padding: 12 14;";
    private static final String STYLE_FIELD_ERROR =
            "-fx-background-color: rgba(255,0,84,0.13); -fx-background-radius: 12;" +
                    "-fx-border-color: #FF0054; -fx-border-width: 1.5; -fx-border-radius: 12;" +
                    "-fx-text-fill: white; -fx-padding: 12 14;";
    private static final String STYLE_FIELD_OK_GREEN =
            "-fx-background-color: rgba(0,255,157,0.08); -fx-background-radius: 12;" +
                    "-fx-border-color: #00FF9D; -fx-border-width: 1.5; -fx-border-radius: 12;" +
                    "-fx-text-fill: white; -fx-padding: 12 14;";
    private static final String STYLE_ERR_LABEL =
            "-fx-text-fill: #FF0054; -fx-font-size: 10px; -fx-font-weight: 800; -fx-padding: 2 0 0 4;";

    @FXML private StackPane overlayContainerFxml;

    // ==================== INIT ====================
    @FXML
    public void initialize() {
        // ✅ OVERLAY EN PREMIER
        if (overlayContainerFxml != null) {
            this.overlayContainer = overlayContainerFxml;
            overlayContainerFxml.setPickOnBounds(false); // ✅ ne bloque pas les clics quand vide
        }

        // 🔍 DEBUG — à supprimer après
        System.out.println("=== DEBUG INIT ===");
        System.out.println("overlayContainer null? " + (overlayContainer == null));
        System.out.println("Session logged? " + Session.isLoggedIn());
        System.out.println("Session userId? " + Session.getUserId());
        System.out.println("clientProductsGrid null? " + (clientProductsGrid == null));
        System.out.println("overlayContainerFxml null? " + (overlayContainerFxml == null));

        setupProductsTable();
        setupFilters();
        refreshProducts();
        refreshCartUI();
        setupRealtimeValidation();
        updateUIForUserType();
        showProducts();
    }



    // ==================== REALTIME VALIDATION ====================
    private void setupRealtimeValidation() {
        for (Label l : new Label[]{errNom,errCat,errPrix,errStock,errImage,errRegion,errDesc,errEmail,errPassword,errBcNom,errBcPhone,errBcType,errBcMotiv})
            styleErrorLabel(l);

        if (nomTextField != null) {
            injectErrorLabel(nomTextField, errNom);
            nomTextField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) showInlineError(nomTextField, errNom, "⚡ Le nom est obligatoire");
                else if (!val.trim().matches("^[a-zA-ZÀ-ÿ0-9\\s\\-'()]+$")) showInlineError(nomTextField, errNom, "⚡ Caractères spéciaux non autorisés (@, #, $...)");
                else if (val.trim().matches("^\\d+$")) showInlineError(nomTextField, errNom, "⚡ Le nom ne peut pas être uniquement des chiffres");
                else if (val.trim().length() < 2) showInlineError(nomTextField, errNom, "⚡ Minimum 2 caractères");
                else if (val.trim().length() > 100) showInlineError(nomTextField, errNom, "⚡ Maximum 100 caractères");
                else clearInlineError(nomTextField, errNom);
            });
        }
        if (categorieField != null) {
            injectErrorLabel(categorieField, errCat);
            List<String> cats = List.of("Artisanat","Gastronomie","Textile","Bijoux","Art","Souvenirs");
            categorieField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) showInlineError(categorieField, errCat, "⚡ La catégorie est obligatoire");
                else if (!cats.contains(val.trim())) showInlineError(categorieField, errCat, "⚡ Valeurs: Artisanat, Gastronomie, Textile, Bijoux, Art, Souvenirs");
                else clearInlineError(categorieField, errCat);
            });
        }
        if (categorieAutreField != null) {
            injectErrorLabel(categorieAutreField, errCat);
            categorieAutreField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) showInlineError(categorieAutreField, errCat, "⚡ Précisez votre catégorie");
                else if (val.trim().length() < 2) showInlineError(categorieAutreField, errCat, "⚡ Minimum 2 caractères");
                else if (!val.trim().matches("^[a-zA-ZÀ-ÿ\\s\\-']+$")) showInlineError(categorieAutreField, errCat, "⚡ Lettres uniquement");
                else clearInlineError(categorieAutreField, errCat);
            });
        }
        if (prixTextField != null) {
            injectErrorLabel(prixTextField, errPrix);
            prixTextField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) { showInlineError(prixTextField, errPrix, "⚡ Le prix est obligatoire"); return; }
                try { double p = Double.parseDouble(val.trim());
                    if (p <= 0) showInlineError(prixTextField, errPrix, "⚡ Le prix doit être > 0");else if (p > 99999.99) showInlineError(prixTextField, errPrix, "⚡ Maximum 99 999.99 TND");
                    else clearInlineError(prixTextField, errPrix);
                } catch (NumberFormatException e) { showInlineError(prixTextField, errPrix, "⚡ Chiffres uniquement (ex: 12.50)"); }
            });
        }
        if (stockField != null) {
            injectErrorLabel(stockField, errStock);
            stockField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) { showInlineError(stockField, errStock, "⚡ Le stock est obligatoire"); return; }
                try { int s = Integer.parseInt(val.trim());
                    if (s < 0) showInlineError(stockField, errStock, "⚡ Stock ne peut pas être négatif");
                    else if (s > 99999) showInlineError(stockField, errStock, "⚡ Maximum 99 999");
                    else clearInlineError(stockField, errStock);
                } catch (NumberFormatException e) { showInlineError(stockField, errStock, "⚡ Entier uniquement (ex: 10)"); }
            });
        }
        if (imageField != null) {
            injectErrorLabel(imageField, errImage);
            imageField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) { clearInlineError(imageField, errImage); return; }
                if (val.contains("encrypted-tbn") || val.contains("gstatic.com/images"))
                    showInlineError(imageField, errImage, "🚫 URL Google Images → Unsplash ou Wikipedia");
                else if (!val.trim().startsWith("http://") && !val.trim().startsWith("https://")
                        && !val.trim().startsWith("file:") && !new java.io.File(val.trim()).exists())
                    showInlineError(imageField, errImage, "⚡ URL invalide → doit commencer par https://");
                else clearInlineError(imageField, errImage);
            });
        }
        if (descriptionField != null) {
            injectErrorLabel(descriptionField, errDesc);
            descriptionField.textProperty().addListener((obs, old, val) -> {
                if (val.length() > 500) showInlineError(descriptionField, errDesc, "⚡ Maximum 500 caractères (" + val.length() + "/500)");
                else clearInlineError(descriptionField, errDesc);
            });
        }
        if (emailField != null) {
            injectErrorLabel(emailField, errEmail);
            emailField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) { clearInlineError(emailField, errEmail); return; }
                if (!val.trim().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
                    showInlineError(emailField, errEmail, "⚡ Format invalide (ex: nom@email.com)");
                else clearInlineError(emailField, errEmail);
            });
        }
        if (passwordField != null) {
            injectErrorLabel(passwordField, errPassword);
            passwordField.textProperty().addListener((obs, old, val) -> {
                if (val.isBlank()) { clearInlineError(passwordField, errPassword); return; }
                if (val.length() < 4) showInlineError(passwordField, errPassword, "⚡ Minimum 4 caractères");
                else clearInlineError(passwordField, errPassword);
            });
        }
    }

    // ==================== HELPERS VALIDATION ====================
    private void styleErrorLabel(Label lbl) {
        lbl.setStyle(STYLE_ERR_LABEL); lbl.setWrapText(true); lbl.setMaxWidth(400);
        lbl.setVisible(false); lbl.setManaged(false);
    }
    private void injectErrorLabel(TextField field, Label errLabel) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.Parent parent = field.getParent();
            if (parent instanceof VBox vbox) {
                int idx = vbox.getChildren().indexOf(field);
                if (idx >= 0 && !vbox.getChildren().contains(errLabel))
                    vbox.getChildren().add(idx + 1, errLabel);
            }
        });
    }
    private void showInlineError(TextField field, Label errLabel, String message) {
        field.setStyle(STYLE_FIELD_ERROR); errLabel.setText(message);
        errLabel.setVisible(true); errLabel.setManaged(true);
    }
    private void clearInlineError(TextField field, Label errLabel) {
        field.setStyle(STYLE_FIELD_OK_GREEN); errLabel.setText("");
        errLabel.setVisible(false); errLabel.setManaged(false);
    }
    private void setNavEnabled(boolean enabled) {
        if (leftNav == null) return;
        leftNav.setDisable(!enabled); leftNav.setOpacity(enabled ? 1.0 : 0.35);
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
                "Toutes","Artisanat","Gastronomie","Textile","Bijoux","Art","Souvenirs");
        if (clientCategoryFilter != null) { clientCategoryFilter.setItems(categories); clientCategoryFilter.setValue("Toutes"); }

        ObservableList<String> regions = FXCollections.observableArrayList(
                "Toutes","Tunis","Ariana","Ben Arous","Manouba","Nabeul","Zaghouan",
                "Bizerte","Béja","Jendouba","Le Kef","Siliana","Sousse","Monastir",
                "Mahdia","Sfax","Kairouan","Kasserine","Sidi Bouzid","Gabès",
                "Médenine","Tataouine","Gafsa","Tozeur","Kebili");
        if (clientRegionFilter != null) { clientRegionFilter.setItems(regions); clientRegionFilter.setValue("Toutes"); }

        if (categorieCombo != null) {
            ObservableList<String> cats = FXCollections.observableArrayList(
                    "Artisanat","Gastronomie","Textile","Bijoux","Art","Souvenirs","✏ Autre...");
            categorieCombo.setItems(cats);
            categorieCombo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle("-fx-background-color: #1a1a2e;"); return; }
                    setText(item);
                    boolean isAutre = item.startsWith("✏");
                    setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: "+(isAutre?"#FFBD00":"white")+
                            "; -fx-font-weight: "+(isAutre?"900":"700")+"; -fx-font-size: 12px; -fx-padding: 10 14;");
                    setOnMouseEntered(e -> setStyle("-fx-background-color: rgba(255,189,0,0.20); -fx-text-fill: "+(isAutre?"#FFBD00":"white")+
                            "; -fx-font-weight: "+(isAutre?"900":"700")+"; -fx-font-size: 12px; -fx-padding: 10 14;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: "+(isAutre?"#FFBD00":"white")+
                            "; -fx-font-weight: "+(isAutre?"900":"700")+"; -fx-font-size: 12px; -fx-padding: 10 14;"));
                }
            });
            categorieCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText("Choisir une catégorie..."); setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 12px;"); }
                    else { setText(item); setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 12px;"); }
                }
            });
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
            @Override protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isBlank()) { setGraphic(null); }
                else { try { iv.setImage(new Image(url.trim(), true)); setGraphic(iv); } catch (Exception e) { setGraphic(null); } }
                setText(null);
            }
        });
        produitsTable.setItems(produitsData);
    }

    private void hideAllScreens() {
        if (loginPane != null) { loginPane.setVisible(false); loginPane.setManaged(false); }
        if (clientProductsPane != null) { clientProductsPane.setVisible(false); clientProductsPane.setManaged(false); }
        if (commercantProductsPane != null) { commercantProductsPane.setVisible(false); commercantProductsPane.setManaged(false); }
        if (formScrollPane != null) { formScrollPane.setVisible(false); formScrollPane.setManaged(false); }
        if (cartPane != null) { cartPane.setVisible(false); cartPane.setManaged(false); }
        if (becomeCommercantPane != null) { becomeCommercantPane.setVisible(false); becomeCommercantPane.setManaged(false); }
        if (dashboardPane != null) { dashboardPane.setVisible(false); dashboardPane.setManaged(false); }
    }

    private void updateUIForUserType() {
        boolean logged = Session.isLoggedIn();
        boolean isCommercant = logged && Session.isCommercant();
        boolean isClient = logged && !Session.isCommercant();

        if (modeSwitchBox != null) { modeSwitchBox.setVisible(isCommercant); modeSwitchBox.setManaged(isCommercant); }
        if (!isCommercant) viewAsCommercant = false;

        boolean commercantView = isCommercantView();
        if (navAddBox != null) { navAddBox.setVisible(commercantView); navAddBox.setManaged(commercantView); }
        if (navDashboardBox != null) { navDashboardBox.setVisible(commercantView); navDashboardBox.setManaged(commercantView); }

        boolean showCart = isClient && !commercantView;
        if (navCartBox != null) { navCartBox.setVisible(showCart); navCartBox.setManaged(showCart); }
        if (navBecomeCommercantBox != null) { navBecomeCommercantBox.setVisible(isClient); navBecomeCommercantBox.setManaged(isClient); }

        if (cartBadgeBox != null) { cartBadgeBox.setVisible(showCart); cartBadgeBox.setManaged(showCart); }
        if (topSubtitleLabel != null) topSubtitleLabel.setText(commercantView ? "Espace Commerçant" : "Produits Locaux Tunisiens");
        if (userNameLabel != null) userNameLabel.setText(logged ? getNomPrenomUtilisateur(Session.getUserId()) : "Visiteur");
        if (modeToggle != null && isCommercant) { modeToggle.setSelected(commercantView); modeToggle.setText(commercantView ? "Commerçant" : "Client"); }
    }

    // ==================== SCREENS ====================
    @FXML public void showLogin() {
        hideAllScreens();
        loginPane.setVisible(true); loginPane.setManaged(true);
        setNavEnabled(false); setTopLoginMode(true); updateUIForUserType();
    }

    @FXML public void showProducts() {
        setNavEnabled(true); setTopLoginMode(false); hideAllScreens(); updateUIForUserType();
        if (isCommercantView()) {
            commercantProductsPane.setVisible(true); commercantProductsPane.setManaged(true);
            refreshCommercantProducts();
        } else {
            clientProductsPane.setVisible(true); clientProductsPane.setManaged(true);
            refreshProducts(); refreshClientProducts();
        }
    }

    @FXML public void showCart() {
        if (!Session.isLoggedIn()) { showLogin(); return; }
        if (isCommercantView()) { showAlert("🛒 Panier", "Passe en mode Client pour accéder au panier."); return; }
        hideAllScreens(); updateUIForUserType();
        cartPane.setVisible(true); cartPane.setManaged(true);
        refreshCartUI();
    }

    @FXML private void onCategorieChanged() {
        if (categorieCombo == null) return;
        String selected = categorieCombo.getValue();
        boolean isAutre = "✏ Autre...".equals(selected);
        categorieAutreField.setVisible(isAutre); categorieAutreField.setManaged(isAutre);
        if (isAutre) {
            categorieAutreField.requestFocus();
            categorieAutreField.setPromptText("✏ Ex: Poterie, Parfums, Épices...");
            errCat.setText(""); errCat.setVisible(false); errCat.setManaged(false);
        } else if (selected != null && !selected.isBlank()) {
            categorieField.setText(selected);
            errCat.setText(""); errCat.setVisible(false); errCat.setManaged(false);
        }
    }

    // ==================== LOGIN ====================
    @FXML public void doLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass  = passwordField.getText() == null ? "" : passwordField.getText().trim();
        boolean valid = true;
        if (email.isEmpty()) { showInlineError(emailField, errEmail, "⚡ L'email est obligatoire"); valid = false; }
        else if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) { showInlineError(emailField, errEmail, "⚡ Format invalide (ex: nom@email.com)"); valid = false; }
        if (pass.isEmpty()) { showInlineError(passwordField, errPassword, "⚡ Le mot de passe est obligatoire"); valid = false; }
        else if (pass.length() < 4) { showInlineError(passwordField, errPassword, "⚡ Minimum 4 caractères"); valid = false; }
        if (!valid) { loginStatusLabel.setStyle("-fx-text-fill: #FF0054; -fx-font-weight: 800;"); loginStatusLabel.setText("⚠ Corrige les erreurs ci-dessus."); return; }

        int fakeId = 1;
        String fakeType = email.toLowerCase().contains("shop") ? "COMMERCANT" : "CLIENT";
        Session.setUser(fakeId, fakeType);
        loginStatusLabel.setStyle("-fx-text-fill: #00FF9D; -fx-font-weight: 800;");
        loginStatusLabel.setText("✅ Connecté (" + fakeType + ").");
        refreshCartUI(); updateUIForUserType(); showProducts();
    }

    private String getNomPrenomUtilisateur(int idUser) {
        try (PreparedStatement pst = MyBD.getInstance().getConn().prepareStatement(
                "SELECT nom, prenom FROM utilisateur WHERE id=?")) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String full = (rs.getString("prenom")!=null?rs.getString("prenom"):"") + " " + (rs.getString("nom")!=null?rs.getString("nom"):"");
                return full.trim().isEmpty() ? "Utilisateur" : full.trim();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "Utilisateur";
    }

    // ==================== CLIENT MODE ====================
    private void refreshClientProducts() {
        if (clientProductsGrid == null) return;
        clientProductsGrid.getChildren().clear();
        for (Produit p : produitsData) clientProductsGrid.getChildren().add(createProductCard(p));
    }

    private HBox createProductCard(Produit p) {
        HBox card = new HBox(0);
        card.setPrefHeight(185); card.setMaxHeight(185);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 20, 0, 0, 6);");
        StackPane imageBlock = new StackPane();
        imageBlock.setPrefWidth(230); imageBlock.setMinWidth(230); imageBlock.setMaxWidth(230); imageBlock.setMinHeight(185);
        imageBlock.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 20 0 0 20;");
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(230,185); clip.setArcWidth(40); clip.setArcHeight(40); imageBlock.setClip(clip);
        ImageView iv = new ImageView(); iv.setFitWidth(230); iv.setFitHeight(185); iv.setPreserveRatio(false); iv.setSmooth(true);
        imageBlock.getChildren().add(iv);
        final String rawUrl = p.getImage();
        if (rawUrl != null && !rawUrl.isBlank()) {
            Thread t = new Thread(() -> {
                try {
                    String url = rawUrl.trim();
                    if (url.contains("encrypted-tbn") || url.contains("gstatic.com/images")) return;
                    if (!url.startsWith("http") && !url.startsWith("file:")) { java.io.File f = new java.io.File(url); url = f.exists() ? f.toURI().toString() : "file:///"+url.replace("\\","/"); }
                    Image img = new Image(url, 230, 185, false, true, false);
                    if (!img.isError()) { final Image done = img; javafx.application.Platform.runLater(() -> iv.setImage(done)); }
                } catch (Exception ex) {}
            }); t.setDaemon(true); t.start();
        }
        VBox right = new VBox(8); right.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        right.setStyle("-fx-padding: 18 24 18 24; -fx-background-color: transparent;"); HBox.setHgrow(right, javafx.scene.layout.Priority.ALWAYS);
        Label nom = new Label(p.getNom()); nom.setStyle("-fx-text-fill: #87CEEB; -fx-font-weight: 900; -fx-font-size: 19px;"); nom.setWrapText(true);
        HBox locRow = new HBox(5); locRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label pin = new Label("📍"); pin.setStyle("-fx-font-size: 11px; -fx-text-fill: #3B82F6;");
        Label region = new Label(p.getRegion()!=null?p.getRegion():"Tunisie"); region.setStyle("-fx-text-fill: #64748B; -fx-font-weight: 700; -fx-font-size: 13px;");
        locRow.getChildren().addAll(pin, region);
        Label catBadge = new Label(p.getCategorie()!=null?p.getCategorie():""); catBadge.setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #6366F1; -fx-font-weight: 800; -fx-font-size: 10px; -fx-background-radius: 6; -fx-padding: 3 8;");
        HBox priceBadge = new HBox(6); priceBadge.setAlignment(javafx.geometry.Pos.CENTER_LEFT); priceBadge.setMaxWidth(170); priceBadge.setStyle("-fx-background-color: #FFF7ED; -fx-background-radius: 10; -fx-padding: 5 14;");
        Label prix = new Label(String.format("%.2f TND", p.getPrix())); prix.setStyle("-fx-text-fill: #C2410C; -fx-font-weight: 900; -fx-font-size: 17px;");
        priceBadge.getChildren().addAll(new Label("🎫"), prix);
        String desc = p.getDescription()!=null&&!p.getDescription().isBlank()?p.getDescription():"Produit artisanal tunisien de qualité authentique.";
        if (desc.length()>110) desc=desc.substring(0,110)+"…";
        Label descLbl = new Label(desc); descLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;"); descLbl.setMaxWidth(500); descLbl.setWrapText(true);
        HBox bottomRow = new HBox(14); bottomRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label stockInfo = new Label("📦 "+p.getStock()+" en stock"); stockInfo.setStyle("-fx-text-fill: #6366F1; -fx-font-weight: 700; -fx-font-size: 11px;");
        Label statusLbl = new Label(p.getStock()>0?"✅ Disponible":"❌ Rupture"); statusLbl.setStyle("-fx-text-fill: "+(p.getStock()>0?"#10B981":"#EF4444")+"; -fx-font-weight: 700; -fx-font-size: 11px;");
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region(); HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Button addBtn = new Button("🛒  Ajouter au panier");
        addBtn.setStyle("-fx-background-color: #87CEEB; -fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 9 22; -fx-cursor: hand; -fx-effect: dropshadow(gaussian,rgba(99,102,241,0.25),10,0,0,4);");
        addBtn.setOnMouseEntered(e -> addBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 9 22; -fx-cursor: hand;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle("-fx-background-color: #87CEEB; -fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 9 22; -fx-cursor: hand; -fx-effect: dropshadow(gaussian,rgba(99,102,241,0.25),10,0,0,4);"));
        addBtn.setOnAction(e -> addProductToCart(p));
        bottomRow.getChildren().addAll(stockInfo, statusLbl, spacer, addBtn);
        right.getChildren().addAll(nom, locRow, catBadge, priceBadge, descLbl, bottomRow);
        card.getChildren().addAll(imageBlock, right);
        return card;
    }

    private void addProductToCart(Produit p) {
        if (!Session.isLoggedIn()) { showAlert("⚠ Connexion requise","Connecte-toi pour ajouter au panier."); showLogin(); return; }
        if (p.getStock()<=0) { showAlert("❌ Rupture de stock","Ce produit n'est plus disponible."); return; }
        try { panierCRUD.addToCart(Session.getUserId(), p.getIdProduit(), p.getPrix(), 1); refreshCartUI(); showAlert("✅ Ajouté!", p.getNom()+" ajouté au panier."); }
        catch (SQLException e) { showAlert("❌ Erreur","Impossible d'ajouter au panier."); e.printStackTrace(); }
    }

    @FXML private void onClientFilter() {
        String catFilter = clientCategoryFilter!=null?clientCategoryFilter.getValue():null;
        String regFilter = clientRegionFilter!=null?clientRegionFilter.getValue():null;
        List<Produit> filtered = produitsData.stream()
                .filter(p -> catFilter==null||catFilter.equals("Toutes")||(p.getCategorie()!=null&&p.getCategorie().equals(catFilter)))
                .filter(p -> regFilter==null||regFilter.equals("Toutes")||(p.getRegion()!=null&&p.getRegion().equals(regFilter)))
                .collect(Collectors.toList());
        clientProductsGrid.getChildren().clear();
        for (Produit p : filtered) clientProductsGrid.getChildren().add(createProductCard(p));
    }

    // ==================== COMMERCANT MODE ====================
    private void refreshCommercantProducts() {
        try {
            List<Produit> myProducts = produitCRUD.afficherParUser(Session.getUserId());
            produitsData.setAll(myProducts);
            if (commercantProductCount!=null) commercantProductCount.setText(myProducts.size()+" produits");
        } catch (SQLException e) { if (statusLabel!=null) statusLabel.setText("❌ Erreur DB"); e.printStackTrace(); }
    }

    private void refreshProducts() {
        try { produitsData.setAll(produitCRUD.afficher()); } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void onSearch() {
        if (searchField==null||searchField.isDisabled()) return;
        String q = searchField.getText()==null?"":searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) { if (Session.isLoggedIn()&&Session.isCommercant()) refreshCommercantProducts(); else { refreshProducts(); refreshClientProducts(); } return; }
        List<Produit> filtered = produitsData.stream()
                .filter(p -> (p.getNom()!=null&&p.getNom().toLowerCase().contains(q))||(p.getCategorie()!=null&&p.getCategorie().toLowerCase().contains(q))||(p.getRegion()!=null&&p.getRegion().toLowerCase().contains(q)))
                .collect(Collectors.toList());
        if (Session.isLoggedIn()&&Session.isCommercant()) produitsTable.setItems(FXCollections.observableArrayList(filtered));
        else { clientProductsGrid.getChildren().clear(); for (Produit p : filtered) clientProductsGrid.getChildren().add(createProductCard(p)); }
    }

    // ==================== CART UI ====================
    private void refreshCartUI() {
        if (cartItemsBox==null) return;
        cartItemsBox.getChildren().clear();
        if (!Session.isLoggedIn()) { cartData.clear(); if (cartCountLabel!=null) cartCountLabel.setText("0"); if (cartTotalLabel!=null) cartTotalLabel.setText("0.00 TND"); return; }
        try {
            cartData.setAll(panierCRUD.getActiveCart(Session.getUserId()));
            int totalQty = cartData.stream().mapToInt(CartItem::getQty).sum();
            double total = cartData.stream().mapToDouble(CartItem::getSubtotal).sum();
            if (cartCountLabel!=null) cartCountLabel.setText(String.valueOf(totalQty));
            if (cartTotalLabel!=null) cartTotalLabel.setText(String.format("%.2f TND", total));
            for (CartItem it : cartData) cartItemsBox.getChildren().add(createCartItemCard(it));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void setOverlayContainer(StackPane container) { this.overlayContainer = container; }

    @FXML public void showPanierDialog() {
        if (overlayContainer == null) {
            showAlert("⚠ Erreur", "Overlay non initialisé.");
            return;
        }
        if (!Session.isLoggedIn()) {
            showAlert("⚠ Connexion requise", "Connecte-toi pour accéder au panier.");
            return;
        }

        List<CartItem> items = new java.util.ArrayList<>();
        try { items = panierCRUD.getActiveCart(Session.getUserId()); }
        catch (SQLException e) { e.printStackTrace(); }
        StackPane overlay = new StackPane(); overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);"); overlay.setAlignment(javafx.geometry.Pos.CENTER);
        overlay.setOnMouseClicked(e -> { if (e.getTarget()==overlay) overlayContainer.getChildren().remove(overlay); });
        VBox panel = new VBox(0); panel.setPrefWidth(700); panel.setMaxWidth(700);
        panel.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 40, 0, 0, 10);");
        panel.setOnMouseClicked(javafx.event.Event::consume);
        HBox panelHeader = new HBox(); panelHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        panelHeader.setStyle("-fx-padding: 16 20; -fx-background-color: #F8F9FA; -fx-background-radius: 16 16 0 0; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
        Label panelTitle = new Label("🛒  Mon Panier"); panelTitle.setStyle("-fx-font-weight: 900; -fx-font-size: 16px; -fx-text-fill: #1E293B;");
        Region hSpacer = new Region(); HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕"); closeBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-background-radius: 999; -fx-font-weight: 900; -fx-font-size: 12px; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand; -fx-padding: 0;");
        closeBtn.setOnAction(e -> overlayContainer.getChildren().remove(overlay));
        panelHeader.getChildren().addAll(panelTitle, hSpacer, closeBtn);
        HBox colHeader = new HBox(); colHeader.setStyle("-fx-padding: 10 20; -fx-background-color: #F8F9FA; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
        colHeader.getChildren().addAll(makeHeaderLabel("PRODUIT",300),makeHeaderLabel("PRIX",110),makeHeaderLabel("QUANTITÉ",130),makeHeaderLabel("SOUS-TOTAL",120));
        Label sousTotalLbl = new Label("0.000 TND"); sousTotalLbl.setStyle("-fx-font-weight: 900; -fx-font-size: 14px; -fx-text-fill: #1E293B;");
        Label totalLbl = new Label("0.000 TND"); totalLbl.setStyle("-fx-font-weight: 900; -fx-font-size: 15px; -fx-text-fill: #1E293B;");
        VBox itemsBox = new VBox(0);
        if (items.isEmpty()) { Label empty = new Label("🛒  Votre panier est vide"); empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 15px; -fx-font-weight: 700; -fx-padding: 40 0;"); itemsBox.setAlignment(javafx.geometry.Pos.CENTER); itemsBox.getChildren().add(empty); }
        else { for (CartItem it : items) itemsBox.getChildren().add(buildPanierRow(it, itemsBox, sousTotalLbl, totalLbl, overlay)); }
        recalcPanierTotals(sousTotalLbl, totalLbl);
        ScrollPane scroll = new ScrollPane(itemsBox); scroll.setFitToWidth(true); scroll.setMaxHeight(260); scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        Button continueBtn = new Button("← POURSUIVRE LES ACHATS"); continueBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #475569; -fx-font-weight: 700; -fx-font-size: 12px; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
        continueBtn.setOnAction(e -> overlayContainer.getChildren().remove(overlay));
        HBox continueRow = new HBox(continueBtn); continueRow.setStyle("-fx-padding: 12 20;");
        VBox totalSection = new VBox(0); totalSection.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0; -fx-padding: 16 24; -fx-background-radius: 0 0 16 16;");
        Label totalTitle = new Label("TOTAL PANIER"); totalTitle.setStyle("-fx-font-weight: 900; -fx-font-size: 13px; -fx-text-fill: #1E293B; -fx-padding: 0 0 10 0;");
        HBox livrRow = new HBox(); livrRow.setStyle("-fx-padding: 8 0;");
        Label livrLbl = new Label("Livraison"); livrLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-weight: 700;"); livrLbl.setPrefWidth(200);
        Label livrInfo = new Label("Livraison à domicile sur toute la Tunisie (48h ouvrable): 4.500 TND"); livrInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;"); livrInfo.setWrapText(true);
        livrRow.getChildren().addAll(livrLbl, livrInfo);
        Button checkoutBtn = new Button("VALIDER LA COMMANDE"); checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 14 0; -fx-cursor: hand;");
        checkoutBtn.setOnAction(e -> { overlayContainer.getChildren().remove(overlay); showAlert("✅ Commande","Commande validée (à implémenter)."); });
        VBox.setMargin(checkoutBtn, new javafx.geometry.Insets(12,0,0,0));
        totalSection.getChildren().addAll(totalTitle, makePanierTotalRow("Sous-total",sousTotalLbl), livrRow, makePanierTotalRow("Total",totalLbl), checkoutBtn);
        panel.getChildren().addAll(panelHeader, colHeader, scroll, continueRow, totalSection);
        overlay.getChildren().add(panel); overlayContainer.getChildren().add(overlay); refreshCartUI();
    }

    private HBox buildPanierRow(CartItem it, VBox itemsBox, Label sousTotalLbl, Label totalLbl, StackPane overlay) {
        HBox row = new HBox(); row.setAlignment(javafx.geometry.Pos.CENTER_LEFT); row.setStyle("-fx-padding: 12 20; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");
        Button delBtn = new Button("✕"); delBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #94A3B8; -fx-background-radius: 999; -fx-font-size: 10px; -fx-min-width: 22; -fx-max-width: 22; -fx-min-height: 22; -fx-max-height: 22; -fx-cursor: hand; -fx-padding: 0;");
        ImageView iv = new ImageView(); iv.setFitWidth(55); iv.setFitHeight(55); iv.setPreserveRatio(true); iv.setSmooth(true);
        if (it.getImage()!=null&&!it.getImage().isBlank()) { try { iv.setImage(new Image(it.getImage().trim(),55,55,true,true,true)); } catch (Exception ignored) {} }
        javafx.scene.shape.Rectangle imgClip = new javafx.scene.shape.Rectangle(55,55); imgClip.setArcWidth(10); imgClip.setArcHeight(10); iv.setClip(imgClip);
        Label nomLbl = new Label(it.getNom()); nomLbl.setStyle("-fx-font-weight: 700; -fx-font-size: 13px; -fx-text-fill: #1E293B;"); nomLbl.setMaxWidth(170); nomLbl.setWrapText(true);
        HBox prodCell = new HBox(8, delBtn, iv, nomLbl); prodCell.setAlignment(javafx.geometry.Pos.CENTER_LEFT); prodCell.setPrefWidth(300);
        Label prixLbl = new Label(String.format("%.3f TND", it.getPrix())); prixLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-weight: 700;"); prixLbl.setPrefWidth(110);
        final int[] qty = {it.getQty()};
        Label qtyLbl = new Label(String.valueOf(qty[0])); qtyLbl.setStyle("-fx-font-weight: 900; -fx-font-size: 13px; -fx-text-fill: #1E293B; -fx-padding: 0 10; -fx-min-width: 32;"); qtyLbl.setAlignment(javafx.geometry.Pos.CENTER);
        Label subLbl = new Label(String.format("%.3f TND", it.getSubtotal())); subLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 900; -fx-text-fill: #1E293B;"); subLbl.setPrefWidth(120);
        Button minusBtn = new Button("−"); minusBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-font-weight: 900; -fx-font-size: 15px; -fx-min-width: 30; -fx-min-height: 30; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 0;");
        Button plusBtn = new Button("+"); plusBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-font-weight: 900; -fx-font-size: 15px; -fx-min-width: 30; -fx-min-height: 30; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 0;");
        HBox qtyBox = new HBox(0, minusBtn, qtyLbl, plusBtn); qtyBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT); qtyBox.setPrefWidth(130); qtyBox.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 8; -fx-max-width: 105;");
        minusBtn.setOnAction(e -> { try { panierCRUD.changeQty(Session.getUserId(),it.getIdProduit(),-1); qty[0]--; if(qty[0]<=0) itemsBox.getChildren().remove(row); else { qtyLbl.setText(String.valueOf(qty[0])); subLbl.setText(String.format("%.3f TND",qty[0]*it.getPrix())); } recalcPanierTotals(sousTotalLbl,totalLbl); refreshCartUI(); } catch(Exception ignored){} });
        plusBtn.setOnAction(e  -> { try { panierCRUD.changeQty(Session.getUserId(),it.getIdProduit(),+1); qty[0]++; qtyLbl.setText(String.valueOf(qty[0])); subLbl.setText(String.format("%.3f TND",qty[0]*it.getPrix())); recalcPanierTotals(sousTotalLbl,totalLbl); refreshCartUI(); } catch(Exception ignored){} });
        delBtn.setOnAction(e   -> { try { panierCRUD.remove(Session.getUserId(),it.getIdProduit()); itemsBox.getChildren().remove(row); recalcPanierTotals(sousTotalLbl,totalLbl); refreshCartUI(); } catch(Exception ignored){} });
        row.getChildren().addAll(prodCell, prixLbl, qtyBox, subLbl);
        return row;
    }

    private void recalcPanierTotals(Label sousTotalLbl, Label totalLbl) {
        try { List<CartItem> c = panierCRUD.getActiveCart(Session.getUserId()); double sub = c.stream().mapToDouble(CartItem::getSubtotal).sum(); sousTotalLbl.setText(String.format("%.3f TND",sub)); totalLbl.setText(String.format("%.3f TND",sub+4.5)); } catch(Exception ignored){}
    }
    private HBox makePanierTotalRow(String labelText, Label valueLabel) {
        HBox row = new HBox(); row.setAlignment(javafx.geometry.Pos.CENTER_LEFT); row.setStyle("-fx-padding: 8 0;");
        Label lbl = new Label(labelText); lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-weight: 700;"); lbl.setPrefWidth(200);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        row.getChildren().addAll(lbl, sp, valueLabel); return row;
    }
    private Label makeHeaderLabel(String text, double width) {
        Label lbl = new Label(text); lbl.setStyle("-fx-font-weight: 900; -fx-font-size: 11px; -fx-text-fill: #64748B;"); lbl.setPrefWidth(width); return lbl;
    }
    private HBox createCartItemCard(CartItem it) {
        ImageView iv = new ImageView(); iv.setFitWidth(56); iv.setFitHeight(56); iv.setPreserveRatio(true); iv.setSmooth(true);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(56,56); clip.setArcWidth(14); clip.setArcHeight(14); iv.setClip(clip);
        if (it.getImage()!=null&&!it.getImage().isBlank()) { try { iv.setImage(new Image(it.getImage().trim(),56,56,true,true,true)); } catch(Exception ignored){} }
        Label name = new Label(it.getNom()); name.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 13px;");
        Label price = new Label(String.format("%.2f TND",it.getPrix())); price.setStyle("-fx-text-fill: #FFBD00; -fx-font-weight: 900;");
        Label qty = new Label("x"+it.getQty()); qty.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-weight: 800;");
        VBox info = new VBox(4, name, price, qty);
        Button minus = new Button("➖"); Button plus = new Button("➕"); Button del = new Button("🗑");
        minus.setOnAction(e -> { try { panierCRUD.changeQty(Session.getUserId(),it.getIdProduit(),-1); refreshCartUI(); } catch(Exception ignored){} });
        plus.setOnAction(e  -> { try { panierCRUD.changeQty(Session.getUserId(),it.getIdProduit(),+1); refreshCartUI(); } catch(Exception ignored){} });
        del.setOnAction(e   -> { try { panierCRUD.remove(Session.getUserId(),it.getIdProduit()); refreshCartUI(); } catch(Exception ignored){} });
        String base = "-fx-background-radius: 999; -fx-padding: 6 10; -fx-font-weight: 900; -fx-cursor: hand;";
        minus.setStyle(base+"-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: white;");
        plus.setStyle(base+"-fx-background-color: rgba(255,189,0,0.25); -fx-text-fill: #FFBD00;");
        del.setStyle(base+"-fx-background-color: rgba(255,0,84,0.25); -fx-text-fill: white;");
        HBox actions = new HBox(8, minus, plus, del); actions.setAlignment(Pos.CENTER_RIGHT);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(12, iv, info, spacer, actions); row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(0,0,0,0.22); -fx-background-radius: 16; -fx-padding: 10;");
        return row;
    }

    // ==================== TOGGLE MODE ====================
    @FXML public void toggleMode() {
        if (!Session.isLoggedIn()||!Session.isCommercant()) return;
        viewAsCommercant = modeToggle.isSelected(); modeToggle.setText(viewAsCommercant?"Commerçant":"Client");
        updateUIForUserType(); showProducts();
        if (isCommercantView()) refreshCommercantProducts(); else { refreshProducts(); refreshClientProducts(); }
    }

    @FXML
    public void toggleModeSimple() {
        enModeCommercant = !enModeCommercant;
        updateSwitchBtn();
        if (enModeCommercant) {
            showModeCommercant();
        } else {
            showModeClient();
        }
    }
    private void updateSwitchBtn() {
        if (switchModeBtn == null) return;
        if (enModeCommercant) {
            switchModeBtn.setText("👤 Mode Client");
            switchModeBtn.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #6366F1;" +
                    "-fx-font-weight: 900; -fx-font-size: 12px; -fx-background-radius: 10;" +
                    "-fx-border-color: #6366F1; -fx-border-width: 2; -fx-border-radius: 10;" +
                    "-fx-padding: 10 18; -fx-cursor: hand;");
        } else {
            switchModeBtn.setText("🏪 Mode Commerçant");
            switchModeBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white;" +
                    "-fx-font-weight: 900; -fx-font-size: 12px; -fx-background-radius: 10;" +
                    "-fx-padding: 10 18; -fx-cursor: hand;");
        }
    }

    private void showModeCommercant() {
        // Affiche la vue commerçant : tableau de ses produits + boutons gérer/ajouter
        if (clientProductsGrid == null) return;
        clientProductsGrid.getChildren().clear();

        // Titre
        Label titre = new Label("🏪  Mes Produits");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #1E293B; -fx-padding: 0 0 10 0;");

        // Bouton Ajouter
        Button addBtn = new Button("➕  Ajouter un produit");
        addBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-weight: 900;" +
                "-fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        addBtn.setOnAction(e -> showAddProductOverlay());

        HBox topBar = new HBox(12, titre, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, addBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

        clientProductsGrid.getChildren().add(topBar);

        // Charger les produits du commerçant
        try {
            List<Produit> mesProduits = produitCRUD.afficherParUser(Session.getUserId());
            if (mesProduits.isEmpty()) {
                Label empty = new Label("Vous n'avez pas encore de produits.");
                empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px; -fx-padding: 20 0;");
                clientProductsGrid.getChildren().add(empty);
            } else {
                for (Produit p : mesProduits) {
                    clientProductsGrid.getChildren().add(createCommercantProductCard(p));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isCommercantView() { return Session.isLoggedIn()&&Session.isCommercant()&&viewAsCommercant; }

    // ==================== BECOME COMMERCANT — OVERLAY ====================

    /**
     * Lit le type de l'utilisateur depuis la DB.
     * Valeurs possibles : CLIENT | COMMERCANT | ADMIN | EN_ATTENTE
     */
    private String getTypeUtilisateur(int idUser) {
        try (PreparedStatement ps = MyBD.getInstance().getConn().prepareStatement(
                "SELECT type FROM utilisateur WHERE id = ?")) {
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("type");
        } catch (Exception e) { e.printStackTrace(); }
        return "CLIENT";
    }

    @FXML
    public void showBecomeCommercant() {
        if (overlayContainer == null) {
            showAlert("⚠ Erreur", "Overlay non initialisé.");
            return;
        }
        if (!Session.isLoggedIn()) {
            showAlert("⚠ Connexion requise", "Connecte-toi pour accéder à cette fonctionnalité.");
            return;
        }
        if (Session.isCommercant()) {
            showAlert("✅ Info", "Vous êtes déjà commerçant.");
            return;
        }

        String type = getTypeUtilisateur(Session.getUserId());

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.65);");
        overlay.setAlignment(javafx.geometry.Pos.CENTER);
        overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) overlayContainer.getChildren().remove(overlay); });

        VBox panel = buildBecomeCommercantPanel(overlay, type);
        panel.setOnMouseClicked(javafx.event.Event::consume);
        overlay.getChildren().add(panel);
        overlayContainer.getChildren().add(overlay);
    }

    private VBox buildBecomeCommercantPanel(StackPane overlay, String type) {
        VBox panel = new VBox(0);
        panel.setPrefWidth(540); panel.setMaxWidth(540);
        panel.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(99,102,241,0.45), 60, 0, 0, 0);");

        // ── HEADER ───────────────────────────────────────────
        HBox header = new HBox(); header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 20 24; -fx-background-color: linear-gradient(to right, #6366F1, #8B5CF6); -fx-background-radius: 20 20 0 0;");
        Label title = new Label("🏪  Devenir Commerçant"); title.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 17px;");
        Region hsp = new Region(); HBox.setHgrow(hsp, Priority.ALWAYS);
        Button closeBtn = new Button("✕"); closeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white; -fx-background-radius: 999; -fx-font-weight: 900; -fx-font-size: 12px; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand; -fx-padding: 0;");
        closeBtn.setOnAction(e -> overlayContainer.getChildren().remove(overlay));
        header.getChildren().addAll(title, hsp, closeBtn);

        VBox body = new VBox(14); body.setStyle("-fx-padding: 28 28 24 28;");

        if ("EN_ATTENTE".equals(type)) {
            // ── EN ATTENTE ───────────────────────────────────
            body.setAlignment(javafx.geometry.Pos.CENTER);
            Label ico = new Label("⏳"); ico.setStyle("-fx-font-size: 50px;");
            Label msg = new Label("Demande en cours d'examen");
            msg.setStyle("-fx-text-fill: #FFBD00; -fx-font-weight: 900; -fx-font-size: 18px;");
            Label sub = new Label("Votre demande est en attente de validation par un administrateur.\nVous serez notifié dès qu'une décision sera prise.");
            sub.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px; -fx-text-alignment: center;"); sub.setWrapText(true); sub.setMaxWidth(420); sub.setAlignment(javafx.geometry.Pos.CENTER);
            HBox badge = new HBox(8); badge.setAlignment(javafx.geometry.Pos.CENTER);
            badge.setStyle("-fx-background-color: rgba(255,189,0,0.12); -fx-background-radius: 12; -fx-padding: 12 24;");
            badge.getChildren().addAll(new Label("●") {{ setStyle("-fx-text-fill: #FFBD00; -fx-font-size: 10px;"); }},
                    new Label("EN ATTENTE DE VALIDATION") {{ setStyle("-fx-text-fill: #FFBD00; -fx-font-weight: 900; -fx-font-size: 12px;"); }});
            body.getChildren().addAll(ico, msg, sub, badge);

        } else {
            // ── FORMULAIRE (CLIENT ou REFUSE) ─────────────────
            if ("REFUSE".equals(type)) {
                // Petit message si refusé
                Label refuseLbl = new Label("❌  Votre demande précédente a été refusée. Vous pouvez en soumettre une nouvelle.");
                refuseLbl.setStyle("-fx-text-fill: #F87171; -fx-font-size: 12px; -fx-font-weight: 700;"); refuseLbl.setWrapText(true);
                Separator sep = new Separator(); sep.setStyle("-fx-background-color: rgba(255,255,255,0.08);");
                body.getChildren().addAll(refuseLbl, sep);
            } else {
                Label sub = new Label("Remplissez ce formulaire pour soumettre votre candidature.\nUn administrateur l'examinera sous 24-48h.");
                sub.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;"); sub.setWrapText(true);
                body.getChildren().add(sub);
            }

            // Champs formulaire
            TextField fNom   = bcField("👤  Nom complet");
            TextField fPhone = bcField("📞  Téléphone (ex: +216 22 123 456)");
            TextField fType  = bcField("🏷  Type de produits vendus");
            TextArea  fMotiv = new TextArea();
            fMotiv.setPromptText("✍  Motivations (min. 20 caractères)..."); fMotiv.setPrefRowCount(4); fMotiv.setWrapText(true);
            fMotiv.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 12; -fx-text-fill: white; -fx-padding: 10 12; -fx-font-size: 13px; -fx-prompt-text-fill: rgba(255,255,255,0.35);");

            Label statusLbl = new Label(""); statusLbl.setStyle("-fx-text-fill: #F87171; -fx-font-weight: 800; -fx-font-size: 12px;"); statusLbl.setWrapText(true);

            Button submitBtn = new Button("📨  Soumettre ma demande"); submitBtn.setMaxWidth(Double.MAX_VALUE);
            submitBtn.setStyle("-fx-background-color: linear-gradient(to right, #6366F1, #8B5CF6); -fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px; -fx-background-radius: 12; -fx-padding: 14 0; -fx-cursor: hand;");
            submitBtn.setOnMouseEntered(e -> submitBtn.setStyle("-fx-background-color: linear-gradient(to right, #4F46E5, #7C3AED); -fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px; -fx-background-radius: 12; -fx-padding: 14 0; -fx-cursor: hand;"));
            submitBtn.setOnMouseExited(e  -> submitBtn.setStyle("-fx-background-color: linear-gradient(to right, #6366F1, #8B5CF6); -fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px; -fx-background-radius: 12; -fx-padding: 14 0; -fx-cursor: hand;"));
            submitBtn.setOnAction(e -> {
                String nom   = fNom.getText()==null?"":fNom.getText().trim();
                String phone = fPhone.getText()==null?"":fPhone.getText().trim();
                String tp    = fType.getText()==null?"":fType.getText().trim();
                String motiv = fMotiv.getText()==null?"":fMotiv.getText().trim();

                if (nom.isEmpty()||!nom.matches("^[a-zA-ZÀ-ÿ\\s\\-']+$")||nom.length()<3) { statusLbl.setText("⚡ Nom invalide (lettres, min. 3 car.)."); bcMarkError(fNom); return; }
                if (!phone.replaceAll("[\\s\\-+]","").matches("^(\\+?216)?[2-9]\\d{7}$")) { statusLbl.setText("⚡ Téléphone invalide (ex: +216 22 123 456)."); bcMarkError(fPhone); return; }
                if (tp.isEmpty()||tp.length()<3) { statusLbl.setText("⚡ Type de produits obligatoire (min. 3 car.)."); bcMarkError(fType); return; }
                if (motiv.length()<20) { statusLbl.setText("⚡ Motivation trop courte ("+motiv.length()+"/20 car. min.)."); return; }

                // Enregistrer EN_ATTENTE dans utilisateur.type
                try (PreparedStatement ps = MyBD.getInstance().getConn().prepareStatement(
                        "UPDATE utilisateur SET type='EN_ATTENTE' WHERE id=?")) {
                    ps.setInt(1, Session.getUserId());
                    ps.executeUpdate();
                    // Fermer et rouvrir en mode EN_ATTENTE
                    overlayContainer.getChildren().remove(overlay);
                    showBecomeCommercant();
                } catch (SQLException ex) { statusLbl.setText("❌ Erreur DB : "+ex.getMessage()); ex.printStackTrace(); }
            });

            body.getChildren().addAll(
                    bcFieldRow("Nom complet", fNom),
                    bcFieldRow("Téléphone", fPhone),
                    bcFieldRow("Type de produits", fType),
                    bcFieldLabel("Motivation"), fMotiv,
                    statusLbl, submitBtn
            );
        }

        panel.getChildren().addAll(header, body);
        return panel;
    }

    // helpers UI overlay
    private TextField bcField(String prompt) {
        TextField f = new TextField(); f.setPromptText(prompt);
        f.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 12; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.35); -fx-padding: 12 14; -fx-font-size: 13px;");
        f.focusedProperty().addListener((obs,old,focused) -> {
            if (focused) f.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 12; -fx-border-color: #6366F1; -fx-border-width: 1.5; -fx-border-radius: 12; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.35); -fx-padding: 12 14; -fx-font-size: 13px;");
            else f.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 12; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.35); -fx-padding: 12 14; -fx-font-size: 13px;");
        });
        return f;
    }
    private void bcMarkError(TextField f) { f.setStyle("-fx-background-color: rgba(248,113,113,0.1); -fx-background-radius: 12; -fx-border-color: #F87171; -fx-border-width: 1.5; -fx-border-radius: 12; -fx-text-fill: white; -fx-padding: 12 14; -fx-font-size: 13px;"); }
    private VBox bcFieldRow(String labelTxt, Control field) { VBox box = new VBox(5); Label lbl = new Label(labelTxt); lbl.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 11px; -fx-font-weight: 700;"); box.getChildren().addAll(lbl, field); return box; }
    private Label bcFieldLabel(String text) { Label lbl = new Label(text); lbl.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 11px; -fx-font-weight: 700;"); VBox.setMargin(lbl, new javafx.geometry.Insets(4,0,0,0)); return lbl; }

    // Ancienne méthode conservée pour compatibilité FXML
    @FXML public void submitBecomeCommercant() { showBecomeCommercant(); }

    // ==================== ADD/EDIT/DELETE/SAVE PRODUCT ====================
    @FXML public void showAddForm() {
        if (!Session.isLoggedIn()||!Session.isCommercant()) { showAlert("⚠ Accès refusé","Connecte-toi en tant que commerçant."); showLogin(); return; }
        if (!isCommercantView()) { showAlert("⚠ Mode Commerçant","Active le mode Commerçant pour ajouter."); return; }
        hideAllScreens(); updateUIForUserType(); formScrollPane.setVisible(true); formScrollPane.setManaged(true);
        clearForm(); if (formTitleLabel!=null) formTitleLabel.setText("➕ Ajouter Produit"); if (saveBtn!=null) saveBtn.setText("✅ Enregistrer");
    }

    @FXML public void showDashboard() {
        if (!Session.isLoggedIn()||!Session.isCommercant()) { showLogin(); return; }
        if (!isCommercantView()) { showAlert("⚠ Mode Commerçant","Active le mode Commerçant pour voir le dashboard."); return; }
        hideAllScreens(); updateUIForUserType(); dashboardPane.setVisible(true); dashboardPane.setManaged(true); updateDashboardStats();
    }

    @FXML public void editSelectedProduct() {
        Produit selected = produitsTable.getSelectionModel().getSelectedItem();
        if (selected==null) { showAlert("✏ Modifier","Sélectionne un produit d'abord."); return; }
        hideAllScreens(); updateUIForUserType(); formScrollPane.setVisible(true); formScrollPane.setManaged(true);
        idProduitHidden.setText(String.valueOf(selected.getIdProduit())); nomTextField.setText(selected.getNom());
        prixTextField.setText(String.valueOf(selected.getPrix())); stockField.setText(String.valueOf(selected.getStock()));
        categorieField.setText(selected.getCategorie()); regionField.setText(selected.getRegion());
        imageField.setText(selected.getImage()); descriptionField.setText(selected.getDescription());
        formTitleLabel.setText("✏ Modifier Produit"); saveBtn.setText("💾 Mettre à jour");
    }

    @FXML public void deleteSelectedProduct() {
        Produit selected = produitsTable.getSelectionModel().getSelectedItem();
        if (selected==null) { showAlert("🗑 Supprimer","Sélectionne un produit d'abord."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION); confirm.setTitle("Confirmation"); confirm.setHeaderText(null); confirm.setContentText("Supprimer : "+selected.getNom()+" ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL)!=ButtonType.OK) return;
        try { produitCRUD.supprimer(selected.getIdProduit()); refreshCommercantProducts(); showAlert("✅ Supprimé","Produit supprimé."); }
        catch (SQLException e) { e.printStackTrace(); showAlert("❌ Erreur","Impossible de supprimer."); }
    }

    @FXML public void saveProduct() {
        if (!Session.isLoggedIn()||!Session.isCommercant()||!isCommercantView()) { showAlert("⚠ Accès refusé","Mode commerçant requis."); return; }
        String nom = nomTextField.getText()==null?"":nomTextField.getText().trim();
        String selectedCat = categorieCombo!=null?categorieCombo.getValue():"";
        String cat;
        if ("✏ Autre...".equals(selectedCat)) { cat = categorieAutreField.getText()==null?"":categorieAutreField.getText().trim(); if (cat.isEmpty()) { showInlineError(categorieAutreField,errCat,"⚡ Précisez votre catégorie"); return; } }
        else { cat = selectedCat==null?"":selectedCat.trim(); if (cat.isEmpty()) { errCat.setText("⚡ Choisissez une catégorie"); errCat.setVisible(true); errCat.setManaged(true); return; } }
        String region = regionField.getText()==null?"":regionField.getText().trim();
        String img = imageField.getText()==null?"":imageField.getText().trim();
        String desc = descriptionField.getText()==null?"":descriptionField.getText().trim();
        boolean valid = true;
        if (nom.isEmpty()) { showInlineError(nomTextField,errNom,"⚡ Le nom est obligatoire"); valid=false; }
        else if (nom.length()<2) { showInlineError(nomTextField,errNom,"⚡ Minimum 2 caractères"); valid=false; }
        else if (nom.length()>100) { showInlineError(nomTextField,errNom,"⚡ Maximum 100 caractères"); valid=false; }
        else clearInlineError(nomTextField,errNom);
        List<String> catsValides = List.of("Artisanat","Gastronomie","Textile","Bijoux","Art","Souvenirs");
        if (cat.isEmpty()) { showInlineError(categorieField,errCat,"⚡ La catégorie est obligatoire"); valid=false; }
        else if (!catsValides.contains(cat)) { showInlineError(categorieField,errCat,"⚡ Valeurs: Artisanat, Gastronomie, Textile, Bijoux, Art, Souvenirs"); valid=false; }
        else clearInlineError(categorieField,errCat);
        double prix=0;
        try { prix=Double.parseDouble(prixTextField.getText().trim()); if(prix<=0){showInlineError(prixTextField,errPrix,"⚡ Le prix doit être > 0");valid=false;} else if(prix>99999){showInlineError(prixTextField,errPrix,"⚡ Maximum 99 999 TND");valid=false;} else clearInlineError(prixTextField,errPrix); }
        catch(NumberFormatException ex){showInlineError(prixTextField,errPrix,"⚡ Chiffres uniquement (ex: 12.50)");valid=false;}
        int stock=0;
        try { stock=Integer.parseInt(stockField.getText().trim()); if(stock<0){showInlineError(stockField,errStock,"⚡ Stock ne peut pas être négatif");valid=false;} else if(stock>99999){showInlineError(stockField,errStock,"⚡ Maximum 99 999");valid=false;} else clearInlineError(stockField,errStock); }
        catch(NumberFormatException ex){showInlineError(stockField,errStock,"⚡ Entier uniquement (ex: 10)");valid=false;}
        if (!img.isEmpty()) {
            if (img.contains("encrypted-tbn")||img.contains("gstatic.com/images")){showInlineError(imageField,errImage,"🚫 URL Google Images non supportée");valid=false;}
            else if (!img.startsWith("http:/")&&!img.startsWith("https:/")&&!img.startsWith("file:")&&!new java.io.File(img).exists()){showInlineError(imageField,errImage,"⚡ URL invalide → doit commencer par https://");valid=false;}
            else clearInlineError(imageField,errImage);
        }
        if (desc.length()>500){showInlineError(descriptionField,errDesc,"⚡ Maximum 500 caractères");valid=false;}
        if (!valid) return;
        try {
            String idTxt = idProduitHidden.getText()==null?"":idProduitHidden.getText().trim();
            if (idTxt.isEmpty()) { produitCRUD.ajouter(new Produit(Session.getUserId(),nom,desc,prix,region,cat,stock,img)); showAlert("✅ Ajout","Produit ajouté avec succès."); }
            else { produitCRUD.modifier(new Produit(Integer.parseInt(idTxt),Session.getUserId(),nom,desc,prix,region,cat,stock,img)); showAlert("✅ Modifié","Produit mis à jour avec succès."); }
            refreshProducts(); refreshCommercantProducts(); refreshClientProducts(); clearForm(); showProducts();
        } catch (SQLException e) { e.printStackTrace(); showAlert("❌ Erreur DB","Impossible d'enregistrer : "+e.getMessage()); }
    }

    @FXML private void goProducts(javafx.scene.input.MouseEvent event) {
        try { Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml")); Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow(); stage.setScene(new Scene(root)); stage.show(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void clearForm() {
        idProduitHidden.clear(); nomTextField.clear(); descriptionField.clear(); prixTextField.clear(); stockField.clear(); regionField.clear(); imageField.clear();
        if (categorieCombo!=null) categorieCombo.setValue(null);
        if (categorieAutreField!=null) { categorieAutreField.clear(); categorieAutreField.setVisible(false); categorieAutreField.setManaged(false); }
        if (categorieField!=null) categorieField.clear();
        for (TextField f : new TextField[]{nomTextField,descriptionField,prixTextField,stockField,regionField,imageField}) if (f!=null) f.setStyle(STYLE_FIELD_OK);
        for (Label l : new Label[]{errNom,errCat,errPrix,errStock,errImage,errRegion,errDesc}) { l.setVisible(false); l.setManaged(false); l.setText(""); }
    }

    @FXML public void clearCart() { try { panierCRUD.clear(Session.getUserId()); refreshCartUI(); } catch (SQLException e) { e.printStackTrace(); } }
    @FXML public void checkoutNow() { showAlert("✅ Commande","Checkout (à implémenter)."); }

    // ==================== DASHBOARD ====================
    private void updateDashboardStats() {
        if (dashTotalProducts!=null) dashTotalProducts.setText(String.valueOf(produitsData.size()));
        if (dashTotalStock!=null) dashTotalStock.setText(String.valueOf(produitsData.stream().mapToInt(Produit::getStock).sum()));
        if (dashStockValue!=null) dashStockValue.setText(String.format("%.2f TND", produitsData.stream().mapToDouble(p->p.getPrix()*p.getStock()).sum()));
    }

    // ==================== ALERT ====================
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}