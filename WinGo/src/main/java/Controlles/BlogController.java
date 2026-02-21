package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Entites.Like;
import Entites.Rating;
import Entites.Utilisateur;
import Services.BlogCRUD;
import Services.CommentaireCRUD;
import Services.LikeCRUD;
import Services.RatingCRUD;
import Services.UtilisateurCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Popup;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class BlogController implements Initializable {

    // Services
    private final BlogCRUD blogCRUD = new BlogCRUD();
    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private final UtilisateurCRUD utilisateurCRUD = new UtilisateurCRUD();
    private final LikeCRUD likeCRUD = new LikeCRUD();
    private final RatingCRUD ratingCRUD = new RatingCRUD();

    // Données observables
    private ObservableList<Blog> blogList = FXCollections.observableArrayList();
    private ObservableList<Commentaire> commentaireList = FXCollections.observableArrayList();
    private Blog selectedBlog = null;
    private Blog displayedDetailBlog = null;

    // Utilisateur connecté
    private Utilisateur currentUser;

    // Données pour les likes
    private Map<Integer, Integer> likeCounts = new HashMap<>();
    private Set<Integer> likedByCurrentUser = new HashSet<>();

    // Données pour les notes (étoiles)
    private Map<Integer, Double> ratingAverages = new HashMap<>();
    private Map<Integer, Integer> voteCounts = new HashMap<>();
    private Map<Integer, Integer> userRatings = new HashMap<>();

    // Images pour les cœurs
    private Image heartEmptyImage;
    private Image heartFullImage;

    // Composants FXML de la vue liste
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Label totalBlogsLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private FlowPane articlesFlowPane;
    @FXML private Label selectedArticleLabel;
    @FXML private Label articleIdLabel;
    @FXML private TextField titreField;
    @FXML private TextArea contenuField;
    @FXML private TextField imageField;
    @FXML private ComboBox<String> regionField;
    @FXML private ComboBox<String> categorieField;
    @FXML private Label auteurLabel;
    @FXML private TextField newCommentField;
    @FXML private Button choisirImageBtn;
    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Button addCommentBtn;
    @FXML private Label statusLabel;
    @FXML private Label connectedUserLabel;

    // Labels d'erreur pour la validation
    @FXML private Label titreError;
    @FXML private Label contenuError;
    @FXML private Label regionError;
    @FXML private Label categorieError;
    @FXML private Label imageError;

    // Composants FXML de la vue détail
    @FXML private VBox listView;
    @FXML private VBox detailView;
    @FXML private Button backToListBtn;
    @FXML private StackPane detailImageContainer;
    @FXML private ImageView detailImageView;
    @FXML private Label detailTitreLabel;
    @FXML private Label detailAuteurLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailContenuLabel;
    @FXML private FlowPane detailCommentairesPane;
    @FXML private TextField detailNewCommentField;
    @FXML private Button detailAddCommentBtn;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailConnectedUserLabel;
    @FXML private Button detailLikeButton;
    @FXML private ImageView detailLikeImageView;
    @FXML private Label detailLikeCountLabel;
    @FXML private HBox detailStarsBox;
    @FXML private Label detailAvgLabel;
    @FXML private HBox detailShareBox;  // Conteneur pour les boutons de partage

    // Filtres
    @FXML private ComboBox<String> regionFilterCombo;
    @FXML private ComboBox<String> categorieFilterCombo;

    // ScrollPanes
    @FXML private ScrollPane listViewScroll;
    @FXML private ScrollPane detailViewScroll;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateShortFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Patterns de validation
    private static final Pattern TITLE_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s\\-']{3,50}$");
    private static final Pattern CONTENT_PATTERN = Pattern.compile("^[\\w\\s\\p{Punct}À-ÿ]{10,500}$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadImages();
        initComboBoxes();
        loadUtilisateurs();
        attachListeners();
        setupValidationListeners();
        loadInitialData();
        showListView();
        updateFormButtons();
        clearAllErrors();
    }

    private void loadImages() {
        String emptyPath = "/images/heart.png";
        String fullPath = "/images/heartRed.png";

        System.out.println("Chargement de " + emptyPath + " : " + getClass().getResource(emptyPath));
        System.out.println("Chargement de " + fullPath + " : " + getClass().getResource(fullPath));

        try (InputStream emptyStream = getClass().getResourceAsStream(emptyPath);
             InputStream fullStream = getClass().getResourceAsStream(fullPath)) {
            if (emptyStream == null || fullStream == null) {
                System.err.println(" Images non trouvées. Utilisation des émojis en fallback.");
                heartEmptyImage = null;
                heartFullImage = null;
            } else {
                heartEmptyImage = new Image(emptyStream);
                heartFullImage = new Image(fullStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
            heartEmptyImage = null;
            heartFullImage = null;
        }
    }

    private void initComboBoxes() {
        ObservableList<String> regions = FXCollections.observableArrayList(
                "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                "Jendouba", "Kairouan", "Kasserine", "Kébili", "Le Kef", "Mahdia",
                "La Manouba", "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid",
                "Siliana", "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        );
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Plage", "Désert", "Montagne", "Culture", "Bien-être",
                "Événements", "Gastronomie", "Aventure", "Nature", "Histoire"
        );
        regionField.setItems(regions);
        categorieField.setItems(categories);
        regionFilterCombo.setItems(FXCollections.observableArrayList("Toutes", "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                "Jendouba", "Kairouan", "Kasserine", "Kébili", "Le Kef", "Mahdia",
                "La Manouba", "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid",
                "Siliana", "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"));
        regionFilterCombo.setValue("Toutes");
        categorieFilterCombo.setItems(FXCollections.observableArrayList("Toutes", "Plage", "Désert", "Montagne", "Culture", "Bien-être",
                "Événements", "Gastronomie", "Aventure", "Nature", "Histoire"));
        categorieFilterCombo.setValue("Toutes");
    }

    private void loadUtilisateurs() {
        try {
            ObservableList<Utilisateur> users = FXCollections.observableArrayList(utilisateurCRUD.afficher());
            currentUser = users.stream().filter(u -> u.getId() == 1).findFirst().orElse(null);
            if (currentUser != null) {
                String nomComplet = currentUser.getPrenom() + " " + currentUser.getNom();
                auteurLabel.setText(nomComplet);
                connectedUserLabel.setText(nomComplet);
                detailConnectedUserLabel.setText(nomComplet);
            } else {
                auteurLabel.setText("Utilisateur inconnu");
                connectedUserLabel.setText("Utilisateur inconnu");
                detailConnectedUserLabel.setText("Utilisateur inconnu");
            }
        } catch (SQLException e) {
            showError("Erreur chargement utilisateurs", e.getMessage());
        }
    }

    private void attachListeners() {
        searchBtn.setOnAction(e -> filterArticles());
        searchField.setOnAction(e -> filterArticles());
        clearBtn.setOnAction(e -> clearForm());
        choisirImageBtn.setOnAction(e -> choisirImage());
        if (backToListBtn != null) {
            backToListBtn.setOnAction(e -> showListView());
        }
        detailAddCommentBtn.setOnAction(e -> ajouterCommentaireDetail());
    }

    // ========== VALIDATION EN TEMPS RÉEL ==========
    private void setupValidationListeners() {
        titreField.textProperty().addListener((obs, oldVal, newVal) -> validateTitre());
        contenuField.textProperty().addListener((obs, oldVal, newVal) -> validateContenu());
        regionField.valueProperty().addListener((obs, oldVal, newVal) -> validateRegion());
        categorieField.valueProperty().addListener((obs, oldVal, newVal) -> validateCategorie());
        imageField.textProperty().addListener((obs, oldVal, newVal) -> validateImage());
    }

    private boolean validateTitre() {
        String titre = titreField.getText();
        if (titre == null || titre.trim().isEmpty()) {
            showError(titreError, "Le titre ne peut pas être vide.");
            return false;
        } else if (!TITLE_PATTERN.matcher(titre).matches()) {
            showError(titreError, "Le titre doit contenir uniquement des lettres, espaces, tirets ou apostrophes (3-50 caractères).");
            return false;
        } else {
            clearError(titreError);
            return true;
        }
    }

    private boolean validateContenu() {
        String contenu = contenuField.getText();
        if (contenu == null || contenu.trim().isEmpty()) {
            showError(contenuError, "Le contenu ne peut pas être vide.");
            return false;
        } else if (!CONTENT_PATTERN.matcher(contenu).matches()) {
            showError(contenuError, "Le contenu doit faire entre 10 et 500 caractères.");
            return false;
        } else {
            clearError(contenuError);
            return true;
        }
    }

    private boolean validateRegion() {
        if (regionField.getValue() == null || regionField.getValue().isEmpty()) {
            showError(regionError, "Veuillez sélectionner une région.");
            return false;
        } else {
            clearError(regionError);
            return true;
        }
    }

    private boolean validateCategorie() {
        if (categorieField.getValue() == null || categorieField.getValue().isEmpty()) {
            showError(categorieError, "Veuillez sélectionner une catégorie.");
            return false;
        } else {
            clearError(categorieError);
            return true;
        }
    }

    private boolean validateImage() {
        String imagePath = imageField.getText();
        if (imagePath == null || imagePath.trim().isEmpty()) {
            showError(imageError, "L'image est obligatoire.");
            return false;
        } else {
            File f = new File(imagePath);
            if (!f.exists()) {
                showError(imageError, "Le fichier image n'existe pas.");
                return false;
            } else {
                clearError(imageError);
                return true;
            }
        }
    }

    private boolean validateBlogForm() {
        boolean valid = true;
        valid &= validateTitre();
        valid &= validateContenu();
        valid &= validateRegion();
        valid &= validateCategorie();
        valid &= validateImage();
        return valid;
    }

    private void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText("⚠ " + message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void clearError(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void clearAllErrors() {
        clearError(titreError);
        clearError(contenuError);
        clearError(regionError);
        clearError(categorieError);
        clearError(imageError);
    }
    // ========== FIN VALIDATION ==========

    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            imageField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void loadInitialData() {
        try {
            loadAllComments();
            loadLikes();
            loadRatings();
            loadBlogs();
            updateStats();
            statusLabel.setText("✅ Prêt, " + blogList.size() + " articles chargés.");
        } catch (SQLException e) {
            showError("Erreur de chargement", e.getMessage());
        }
    }

    private void loadBlogs() throws SQLException {
        blogList.clear();
        blogList.addAll(blogCRUD.afficher());
        displayBlogs(blogList);
        totalBlogsLabel.setText(String.valueOf(blogList.size()));
    }

    private void loadAllComments() throws SQLException {
        commentaireList.clear();
        commentaireList.addAll(commentaireCRUD.afficher());
        totalCommentsLabel.setText(String.valueOf(commentaireList.size()));
    }

    private void loadLikes() throws SQLException {
        List<Like> allLikes = likeCRUD.afficherTous();
        likeCounts.clear();
        likedByCurrentUser.clear();
        for (Like l : allLikes) {
            likeCounts.merge(l.getArticleId(), 1, Integer::sum);
            if (currentUser != null && l.getUtilisateurId() == currentUser.getId()) {
                likedByCurrentUser.add(l.getArticleId());
            }
        }
    }

    private void loadRatings() throws SQLException {
        List<Rating> allRatings = ratingCRUD.afficherTous();
        ratingAverages.clear();
        voteCounts.clear();
        userRatings.clear();

        Map<Integer, List<Integer>> votes = new HashMap<>();
        for (Rating r : allRatings) {
            votes.computeIfAbsent(r.getArticleId(), k -> new ArrayList<>()).add(r.getNote());
            if (currentUser != null && r.getUtilisateurId() == currentUser.getId()) {
                userRatings.put(r.getArticleId(), r.getNote());
            }
        }
        for (Map.Entry<Integer, List<Integer>> entry : votes.entrySet()) {
            double avg = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0);
            ratingAverages.put(entry.getKey(), avg);
            voteCounts.put(entry.getKey(), entry.getValue().size());
        }
    }

    private void displayBlogs(List<Blog> blogs) {
        articlesFlowPane.getChildren().clear();
        for (Blog b : blogs) {
            articlesFlowPane.getChildren().add(createBlogCard(b));
        }
    }

    // ==================== CARTE ARTICLE ====================
    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox();
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0.5, 0, 5);" +
                        "-fx-cursor: hand;"
        );
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setPadding(Insets.EMPTY);

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(180);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 20 20 0 0;");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imageContainer.widthProperty());
        clip.heightProperty().bind(imageContainer.heightProperty());
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageContainer.setClip(clip);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.fitWidthProperty().bind(imageContainer.widthProperty());
        imageView.fitHeightProperty().bind(imageContainer.heightProperty());

        Image img = loadImage(blog.getImage());
        if (img != null && !img.isError()) {
            imageView.setImage(img);
        } else {
            try {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                imageView.setImage(defaultImg);
            } catch (Exception ex) {}
        }

        imageContainer.getChildren().add(imageView);

        // Titre superposé
        String titre = blog.getTitre() != null ? blog.getTitre() : "Sans titre";
        Label titleOverlay = new Label(titre);
        titleOverlay.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: rgba(0,0,0,0.5);" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 5 15;" +
                        "-fx-wrap-text: true;"
        );
        titleOverlay.setMaxWidth(260);
        titleOverlay.setWrapText(true);
        StackPane.setAlignment(titleOverlay, Pos.BOTTOM_LEFT);
        StackPane.setMargin(titleOverlay, new Insets(0, 0, 15, 15));
        imageContainer.getChildren().add(titleOverlay);

        // Badge région
        if (blog.getRegion() != null && !blog.getRegion().isEmpty()) {
            Label regionBadge = new Label(blog.getRegion());
            regionBadge.setStyle(
                    "-fx-background-color: #FFD700;" +
                            "-fx-text-fill: black;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 5 12;" +
                            "-fx-background-radius: 20;" +
                            "-fx-font-size: 12px;"
            );
            StackPane.setAlignment(regionBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(regionBadge, new Insets(12));
            imageContainer.getChildren().add(regionBadge);
        }

        // Contenu texte
        VBox content = new VBox(8);
        content.setPadding(new Insets(15, 15, 15, 15));

        // Affichage du nom de l'auteur (prénom + nom)
        String auteurNom = blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu";
        Label auteur = new Label("Auteur: " + auteurNom);
        auteur.setStyle("-fx-text-fill: #b7472a; -fx-font-weight: bold; -fx-font-size: 13px;");

        String dateStr = blog.getDatePublication() != null ? blog.getDatePublication().format(dateShortFormatter) : "";
        Label date = new Label("Date: " + dateStr);
        date.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        String categorieStr = blog.getCategorie() != null ? blog.getCategorie() : "Divers";
        Label categorie = new Label(categorieStr);
        categorie.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 4 12;" +
                        "-fx-background-radius: 30;" +
                        "-fx-font-size: 12px;"
        );

        String contenuBlog = blog.getContenu();
        String extrait = (contenuBlog != null && contenuBlog.length() > 70) ? contenuBlog.substring(0, 70) + "..." : (contenuBlog != null ? contenuBlog : "");
        Label extraitLabel = new Label(extrait);
        extraitLabel.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px; -fx-wrap-text: true;");
        extraitLabel.setWrapText(true);

        long nbComments = commentaireList.stream().filter(c -> c.getArticleId() == blog.getId()).count();
        Label commentCount = new Label("Commentaires: " + nbComments);
        commentCount.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px;");

        // --- LIKES ---
        int likes = likeCounts.getOrDefault(blog.getId(), 0);
        boolean isLiked = likedByCurrentUser.contains(blog.getId());

        Button likeButton = new Button();
        likeButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        if (heartEmptyImage != null && heartFullImage != null) {
            ImageView heartView = new ImageView();
            heartView.setFitWidth(40);
            heartView.setFitHeight(40);
            heartView.setImage(isLiked ? heartFullImage : heartEmptyImage);
            likeButton.setGraphic(heartView);
        } else {
            likeButton.setText(isLiked ? "❤️" : "🤍");
            likeButton.setStyle(likeButton.getStyle() + " -fx-font-size: 16px;");
        }

        Label likeCountLabel = new Label(likes + (likes > 1 ? " likes" : " like"));
        likeCountLabel.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px;");

        HBox likeBox = new HBox(5, likeButton, likeCountLabel);
        likeBox.setAlignment(Pos.CENTER_LEFT);

        likeButton.setOnAction(e -> toggleLike(blog, likeButton, likeCountLabel));
        // --- FIN LIKES ---

        // --- ÉTOILES (notation) ---
        double avg = ratingAverages.getOrDefault(blog.getId(), 0.0);
        int userNote = userRatings.getOrDefault(blog.getId(), 0);
        int voteCount = voteCounts.getOrDefault(blog.getId(), 0);

        HBox starsBox = new HBox(2);
        starsBox.setAlignment(Pos.CENTER_LEFT);
        for (int i = 1; i <= 5; i++) {
            Button star = new Button();
            star.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px;");
            if (userNote >= i) {
                star.setText("★");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            } else if (avg >= i - 0.5 && avg < i) {
                star.setText("½");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            } else if (avg >= i) {
                star.setText("★");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            } else {
                star.setText("☆");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            }
            int note = i;
            star.setOnAction(e -> {
                if (currentUser == null) {
                    showWarning("Connectez-vous pour noter.");
                    return;
                }
                try {
                    Rating rating = new Rating(currentUser.getId(), blog.getId(), note);
                    ratingCRUD.ajouterOuModifier(rating);
                    loadRatings();
                    refreshAllCards();
                    if (displayedDetailBlog != null && displayedDetailBlog.getId() == blog.getId()) {
                        updateDetailStars();
                    }
                } catch (SQLException ex) {
                    showError("Erreur notation", ex.getMessage());
                }
            });
            starsBox.getChildren().add(star);
        }
        Label avgLabel = new Label(String.format("%.1f (%d votes)", avg, voteCount));
        avgLabel.setStyle("-fx-text-fill: #34495e; -fx-font-size: 11px;");
        VBox ratingBox = new VBox(3, starsBox, avgLabel);
        // --- FIN ÉTOILES ---

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);

        Button voirBtn = new Button("Voir");
        voirBtn.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 6 15;" +
                        "-fx-font-size: 12px;" +
                        "-fx-cursor: hand;"
        );
        voirBtn.setOnAction(e -> showDetailView(blog));
        actions.getChildren().add(voirBtn);

        // Bouton Partager avec popup contextuel
        Button shareBtn = new Button("📤 Partager");
        shareBtn.setStyle(
                "-fx-background-color: #9b59b6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 6 15;" +
                        "-fx-font-size: 12px;" +
                        "-fx-cursor: hand;"
        );
        shareBtn.setOnAction(e -> showSharePopup(shareBtn, blog));
        actions.getChildren().add(shareBtn);

        if (currentUser != null && blog.getAuteur() == currentUser.getId()) {
            Button modifierBtn = new Button("Modifier");
            modifierBtn.setStyle(
                    "-fx-background-color: #f39c12;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 30;" +
                            "-fx-padding: 6 15;" +
                            "-fx-font-size: 12px;" +
                            "-fx-cursor: hand;"
            );
            modifierBtn.setOnAction(e -> selectBlog(blog));

            Button supprimerBtn = new Button("Supprimer");
            supprimerBtn.setStyle(
                    "-fx-background-color: #e74c3c;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 30;" +
                            "-fx-padding: 6 15;" +
                            "-fx-font-size: 12px;" +
                            "-fx-cursor: hand;"
            );
            supprimerBtn.setOnAction(e -> supprimerBlog(blog));

            actions.getChildren().addAll(modifierBtn, supprimerBtn);
        }

        content.getChildren().addAll(auteur, date, categorie, extraitLabel, commentCount, likeBox, ratingBox, actions);
        card.getChildren().addAll(imageContainer, content);

        card.setOnMouseEntered(e -> card.setScaleX(1.02));
        card.setOnMouseExited(e -> card.setScaleX(1.0));
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                showDetailView(blog);
            }
        });

        return card;
    }

    private Image loadImage(String path) {
        if (path == null || path.isEmpty()) return null;
        try {
            File file = new File(path);
            if (file.exists()) {
                String url = file.toURI().toString();
                return new Image(url, true);
            }
        } catch (Exception e) {
            // Ignorer
        }
        return null;
    }

    // ========== VUE DÉTAIL ==========
    private void showDetailView(Blog blog) {
        displayedDetailBlog = blog;
        detailTitreLabel.setText(blog.getTitre() != null ? blog.getTitre() : "");
        detailAuteurLabel.setText(blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu");
        detailDateLabel.setText(blog.getDatePublication() != null ? blog.getDatePublication().format(dateShortFormatter) : "");
        detailContenuLabel.setText(blog.getContenu() != null ? blog.getContenu() : "");

        Image img = loadImage(blog.getImage());
        if (img != null && !img.isError()) {
            detailImageView.setImage(img);
        } else {
            try {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                detailImageView.setImage(defaultImg);
            } catch (Exception ex) {
                detailImageView.setImage(null);
            }
        }
        detailImageView.fitWidthProperty().bind(detailImageContainer.widthProperty());
        detailImageView.setFitHeight(300);

        // Mise à jour du like
        boolean isLiked = likedByCurrentUser.contains(blog.getId());
        if (heartFullImage != null && heartEmptyImage != null) {
            detailLikeImageView.setImage(isLiked ? heartFullImage : heartEmptyImage);
            detailLikeButton.setText("");
        } else {
            detailLikeButton.setText(isLiked ? "❤️" : "🤍");
            detailLikeButton.setStyle("-fx-background-color: transparent; -fx-font-size: 24px; -fx-cursor: hand;");
            detailLikeImageView.setVisible(false);
        }
        int likes = likeCounts.getOrDefault(blog.getId(), 0);
        detailLikeCountLabel.setText(likes + (likes > 1 ? " likes" : " like"));
        detailLikeButton.setOnAction(e -> toggleLikeDetail(blog));

        // Mise à jour des étoiles
        updateDetailStars();

        // Mise à jour des boutons de partage (affichés directement)
        detailShareBox.getChildren().clear();
        Button whatsappBtn = createShareButton("WhatsApp", "/images/whatsapp.png", "#25D366", blog);
        Button facebookBtn = createShareButton("Facebook", "/images/facebook.png", "#4267B2", blog);
        Button instagramBtn = createShareButton("Instagram", "/images/instagram.png", "#C13584", blog);
        // Redéfinir l'action pour fermer le popup (ici pas de popup)
        whatsappBtn.setOnAction(e -> share("WhatsApp", blog));
        facebookBtn.setOnAction(e -> share("Facebook", blog));
        instagramBtn.setOnAction(e -> share("Instagram", blog));
        detailShareBox.getChildren().addAll(whatsappBtn, facebookBtn, instagramBtn);

        afficherCommentairesDetail();
        listViewScroll.setVisible(false);
        listViewScroll.setManaged(false);
        detailViewScroll.setVisible(true);
        detailViewScroll.setManaged(true);
    }

    private void showListView() {
        listViewScroll.setVisible(true);
        listViewScroll.setManaged(true);
        detailViewScroll.setVisible(false);
        detailViewScroll.setManaged(false);
        displayedDetailBlog = null;
    }

    // ========== GESTION DES COMMENTAIRES ==========
    private void afficherCommentairesDetail() {
        if (displayedDetailBlog == null) return;
        detailCommentairesPane.getChildren().clear();
        try {
            List<Commentaire> roots = commentaireCRUD.getHierarchicalComments(displayedDetailBlog.getId());
            for (Commentaire root : roots) {
                VBox commentCard = createCommentCard(root, 0);
                detailCommentairesPane.getChildren().add(commentCard);
            }
        } catch (SQLException e) {
            detailStatusLabel.setText("❌ Erreur chargement commentaires");
        }
    }

    private VBox createCommentCard(Commentaire commentaire, int level) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.3, 0, 5);");
        card.setPrefWidth(250 + level * 20);
        card.setMaxWidth(250 + level * 20);

        Label contenuLabel = new Label(commentaire.getContenu() != null ? commentaire.getContenu() : "");
        contenuLabel.setWrapText(true);
        contenuLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-padding: 5;");

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);

        StackPane userIconContainer = new StackPane();
        userIconContainer.setPrefSize(24, 24);
        userIconContainer.setStyle("-fx-background-color: #FFBD00; -fx-background-radius: 12;");
        Label userIcon = new Label("👤");
        userIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #390099;");
        userIconContainer.getChildren().add(userIcon);

        String utilisateurNom = commentaire.getUtilisateurNom() != null ? commentaire.getUtilisateurNom() : "Utilisateur " + commentaire.getUtilisateur();
        Label auteurLabel = new Label(utilisateurNom);
        auteurLabel.setStyle("-fx-text-fill: #FFBD00; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox auteurBox = new HBox(5, userIconContainer, auteurLabel);
        auteurBox.setAlignment(Pos.CENTER_LEFT);

        StackPane dateIconContainer = new StackPane();
        dateIconContainer.setPrefSize(24, 24);
        dateIconContainer.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 12;");
        Label dateIcon = new Label("📅");
        dateIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        dateIconContainer.getChildren().add(dateIcon);

        Label dateLabel = new Label(commentaire.getDateCommentaire() != null ? commentaire.getDateCommentaire().format(dateFormatter) : "");
        dateLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");

        HBox dateBox = new HBox(5, dateIconContainer, dateLabel);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        meta.getChildren().addAll(auteurBox, spacer, dateBox);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (currentUser != null) {
            Button replyBtn = new Button("↩️ Répondre");
            replyBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 5 12; -fx-font-size: 11px;");
            replyBtn.setOnAction(e -> showReplyField(commentaire, card, level));
            actions.getChildren().add(replyBtn);
        }

        if (currentUser != null && commentaire.getUtilisateur() == currentUser.getId()) {
            Button editBtn = new Button("✏️");
            editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 5 12; -fx-border-color: rgba(255,255,255,0.4); -fx-border-radius: 20;");
            editBtn.setOnAction(e -> showEditComment(commentaire, card, contenuLabel, meta, actions, level));

            Button deleteBtn = new Button("🗑️");
            deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 5 12; -fx-border-color: rgba(255,255,255,0.4); -fx-border-radius: 20;");
            deleteBtn.setOnAction(e -> supprimerCommentaireDetail(commentaire));

            actions.getChildren().addAll(editBtn, deleteBtn);
        }

        card.getChildren().addAll(contenuLabel, meta, actions);

        if (!commentaire.getReplies().isEmpty()) {
            VBox repliesBox = new VBox(8);
            repliesBox.setPadding(new Insets(10, 0, 0, 20));
            for (Commentaire reply : commentaire.getReplies()) {
                repliesBox.getChildren().add(createCommentCard(reply, level + 1));
            }
            card.getChildren().add(repliesBox);
        }

        return card;
    }

    private void showReplyField(Commentaire parentComment, VBox parentCard, int level) {
        if (currentUser == null) {
            detailStatusLabel.setText("❌ Connectez-vous pour répondre.");
            return;
        }

        TextField replyField = new TextField();
        replyField.setPromptText("Écrire une réponse...");
        replyField.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.7); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.4); -fx-border-radius: 15; -fx-padding: 8;");

        Button sendReplyBtn = new Button("Envoyer");
        sendReplyBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 12px;");
        Button cancelReplyBtn = new Button("Annuler");
        cancelReplyBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 12px;");

        HBox replyInputBox = new HBox(10, replyField, sendReplyBtn, cancelReplyBtn);
        replyInputBox.setAlignment(Pos.CENTER_LEFT);
        replyInputBox.setPadding(new Insets(5, 0, 5, 20));

        parentCard.getChildren().add(replyInputBox);

        sendReplyBtn.setOnAction(e -> {
            String replyText = replyField.getText().trim();
            if (replyText.isEmpty()) {
                detailStatusLabel.setText("❌ La réponse ne peut pas être vide.");
                return;
            }
            try {
                Commentaire reply = new Commentaire();
                reply.setContenu(replyText);
                reply.setUtilisateur(currentUser.getId());
                reply.setArticleId(parentComment.getArticleId());
                reply.setParentId(parentComment.getId());
                commentaireCRUD.ajouter(reply);

                afficherCommentairesDetail();
                detailStatusLabel.setText("✅ Réponse ajoutée.");
            } catch (SQLException ex) {
                detailStatusLabel.setText("❌ Erreur : " + ex.getMessage());
            }
        });

        cancelReplyBtn.setOnAction(e -> parentCard.getChildren().remove(replyInputBox));
    }

    private void showEditComment(Commentaire commentaire, VBox card, Label contenuLabel, HBox meta, HBox actions, int level) {
        card.getChildren().clear();

        TextField editField = new TextField(commentaire.getContenu());
        editField.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.7); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.4); -fx-border-radius: 15; -fx-padding: 8;");

        HBox editActions = new HBox(10);
        editActions.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button("✓ Enregistrer");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 12px;");
        saveBtn.setOnAction(e -> {
            String newContent = editField.getText().trim();
            if (!newContent.isEmpty()) {
                commentaire.setContenu(newContent);
                try {
                    commentaireCRUD.modifier(commentaire);
                    afficherCommentairesDetail();
                    detailStatusLabel.setText("✅ Commentaire modifié.");
                } catch (SQLException ex) {
                    detailStatusLabel.setText("❌ Erreur : " + ex.getMessage());
                }
            }
        });

        Button cancelBtn = new Button("✕ Annuler");
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 12px;");
        cancelBtn.setOnAction(e -> afficherCommentairesDetail());

        editActions.getChildren().addAll(saveBtn, cancelBtn);
        card.getChildren().addAll(editField, meta, editActions);
    }

    private void supprimerCommentaireDetail(Commentaire commentaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce commentaire ? Toutes ses réponses seront également supprimées.", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    commentaireCRUD.supprimer(commentaire.getId());
                    afficherCommentairesDetail();
                    detailStatusLabel.setText("✅ Commentaire supprimé.");
                } catch (SQLException e) {
                    detailStatusLabel.setText("❌ Erreur : " + e.getMessage());
                }
            }
        });
    }

    private void ajouterCommentaireDetail() {
        if (displayedDetailBlog == null) return;
        if (currentUser == null) {
            detailStatusLabel.setText("❌ Vous devez être connecté.");
            return;
        }
        String contenu = detailNewCommentField.getText();
        if (contenu == null || contenu.trim().isEmpty()) {
            detailStatusLabel.setText("❌ Le commentaire ne peut pas être vide.");
            return;
        }
        Commentaire c = new Commentaire();
        c.setContenu(contenu.trim());
        c.setUtilisateur(currentUser.getId());
        c.setArticleId(displayedDetailBlog.getId());
        try {
            commentaireCRUD.ajouter(c);
            detailNewCommentField.clear();
            afficherCommentairesDetail();
            detailStatusLabel.setText("✅ Commentaire ajouté.");
        } catch (SQLException e) {
            detailStatusLabel.setText("❌ Erreur : " + e.getMessage());
        }
    }

    // ========== GESTION DES LIKES ==========
    private void toggleLike(Blog blog, Button heartButton, Label countLabel) {
        if (currentUser == null) {
            showWarning("Vous devez être connecté pour liker un article.");
            return;
        }
        int articleId = blog.getId();
        boolean currentlyLiked = likedByCurrentUser.contains(articleId);

        try {
            if (currentlyLiked) {
                likeCRUD.supprimerParUtilisateurEtArticle(currentUser.getId(), articleId);
                likedByCurrentUser.remove(articleId);
                likeCounts.put(articleId, likeCounts.getOrDefault(articleId, 0) - 1);
            } else {
                Like like = new Like(currentUser.getId(), articleId);
                likeCRUD.ajouter(like);
                likedByCurrentUser.add(articleId);
                likeCounts.put(articleId, likeCounts.getOrDefault(articleId, 0) + 1);
            }
            int newLikes = likeCounts.getOrDefault(articleId, 0);
            boolean newLikedState = likedByCurrentUser.contains(articleId);
            if (heartEmptyImage != null && heartFullImage != null) {
                ImageView iv = (ImageView) heartButton.getGraphic();
                iv.setImage(newLikedState ? heartFullImage : heartEmptyImage);
            } else {
                heartButton.setText(newLikedState ? "❤️" : "🤍");
            }
            countLabel.setText(newLikes + (newLikes > 1 ? " likes" : " like"));

            if (displayedDetailBlog != null && displayedDetailBlog.getId() == articleId) {
                updateDetailLikeButton();
            }
        } catch (SQLException ex) {
            showError("Erreur lors du like", ex.getMessage());
        }
    }

    private void toggleLikeDetail(Blog blog) {
        if (currentUser == null) {
            showWarning("Connectez-vous pour liker.");
            return;
        }
        int articleId = blog.getId();
        boolean currentlyLiked = likedByCurrentUser.contains(articleId);

        try {
            if (currentlyLiked) {
                likeCRUD.supprimerParUtilisateurEtArticle(currentUser.getId(), articleId);
                likedByCurrentUser.remove(articleId);
                likeCounts.put(articleId, likeCounts.getOrDefault(articleId, 0) - 1);
            } else {
                Like like = new Like(currentUser.getId(), articleId);
                likeCRUD.ajouter(like);
                likedByCurrentUser.add(articleId);
                likeCounts.put(articleId, likeCounts.getOrDefault(articleId, 0) + 1);
            }
            int newLikes = likeCounts.getOrDefault(articleId, 0);
            boolean newLikedState = likedByCurrentUser.contains(articleId);
            if (heartFullImage != null && heartEmptyImage != null) {
                detailLikeImageView.setImage(newLikedState ? heartFullImage : heartEmptyImage);
            } else {
                detailLikeButton.setText(newLikedState ? "❤️" : "🤍");
            }
            detailLikeCountLabel.setText(newLikes + (newLikes > 1 ? " likes" : " like"));

            refreshAllCards();
        } catch (SQLException ex) {
            showError("Erreur like", ex.getMessage());
        }
    }

    private void updateDetailLikeButton() {
        if (displayedDetailBlog != null) {
            boolean isLiked = likedByCurrentUser.contains(displayedDetailBlog.getId());
            int likes = likeCounts.getOrDefault(displayedDetailBlog.getId(), 0);
            if (heartFullImage != null && heartEmptyImage != null) {
                detailLikeImageView.setImage(isLiked ? heartFullImage : heartEmptyImage);
            } else {
                detailLikeButton.setText(isLiked ? "❤️" : "🤍");
            }
            detailLikeCountLabel.setText(likes + (likes > 1 ? " likes" : " like"));
        }
    }

    // ========== GESTION DES ÉTOILES (VUE DÉTAIL) ==========
    private void updateDetailStars() {
        if (displayedDetailBlog == null) return;
        int articleId = displayedDetailBlog.getId();
        double avg = ratingAverages.getOrDefault(articleId, 0.0);
        int userNote = userRatings.getOrDefault(articleId, 0);
        int voteCount = voteCounts.getOrDefault(articleId, 0);

        detailStarsBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Button star = new Button();
            star.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 20px;");
            if (userNote >= i) {
                star.setText("★");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            } else if (avg >= i - 0.5 && avg < i) {
                star.setText("½");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            } else if (avg >= i) {
                star.setText("★");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            } else {
                star.setText("☆");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            }
            int note = i;
            star.setOnAction(e -> {
                if (currentUser == null) {
                    showWarning("Connectez-vous pour noter.");
                    return;
                }
                try {
                    Rating rating = new Rating(currentUser.getId(), articleId, note);
                    ratingCRUD.ajouterOuModifier(rating);
                    loadRatings();
                    refreshAllCards();
                    updateDetailStars();
                } catch (SQLException ex) {
                    showError("Erreur notation", ex.getMessage());
                }
            });
            detailStarsBox.getChildren().add(star);
        }
        detailAvgLabel.setText(String.format("%.1f (%d votes)", avg, voteCount));
    }

    private void refreshAllCards() {
        displayBlogs(blogList);
    }

    // ========== CRUD BLOG ==========
    private void supprimerBlog(Blog blog) {
        if (currentUser == null || blog.getAuteur() != currentUser.getId()) {
            showWarning("Vous ne pouvez supprimer que vos propres articles.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cet article ? Tous les commentaires, likes et notes associés seront également supprimés.",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    commentaireCRUD.supprimerParArticle(blog.getId());
                    blogCRUD.supprimer(blog.getId());
                    refreshData();
                    if (selectedBlog != null && selectedBlog.getId() == blog.getId()) {
                        clearForm();
                    }
                    if (displayedDetailBlog != null && displayedDetailBlog.getId() == blog.getId()) {
                        showListView();
                    }
                    showInfo("Article supprimé.");
                } catch (SQLException e) {
                    showError("Erreur suppression", e.getMessage());
                }
            }
        });
    }

    private void selectBlog(Blog blog) {
        if (currentUser == null || blog.getAuteur() != currentUser.getId()) {
            showWarning("Vous ne pouvez modifier que vos propres articles.");
            return;
        }
        this.selectedBlog = blog;
        articleIdLabel.setText(String.valueOf(blog.getId()));
        titreField.setText(blog.getTitre() != null ? blog.getTitre() : "");
        contenuField.setText(blog.getContenu() != null ? blog.getContenu() : "");
        imageField.setText(blog.getImage() != null ? blog.getImage() : "");
        regionField.setValue(blog.getRegion());
        categorieField.setValue(blog.getCategorie());
        auteurLabel.setText(blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu");
        selectedArticleLabel.setText("Article sélectionné : " + (blog.getTitre() != null ? blog.getTitre() : ""));
        updateFormButtons();
        clearAllErrors();
    }

    @FXML
    private void ajouterBlog() {
        if (!validateBlogForm()) return;
        if (currentUser == null) {
            showWarning("Aucun utilisateur connecté.");
            return;
        }
        try {
            Blog b = new Blog(
                    titreField.getText().trim(),
                    contenuField.getText().trim(),
                    currentUser.getId(),
                    imageField.getText().trim(),
                    regionField.getValue(),
                    categorieField.getValue()
            );
            blogCRUD.ajouter(b);
            refreshData();
            clearForm();
            showInfo("Article ajouté avec succès.");
        } catch (SQLException e) {
            showError("Erreur ajout", e.getMessage());
        }
    }

    @FXML
    private void modifierBlog() {
        if (selectedBlog == null) {
            showWarning("Sélectionnez un article à modifier.");
            return;
        }
        if (currentUser == null || selectedBlog.getAuteur() != currentUser.getId()) {
            showWarning("Vous ne pouvez modifier que vos propres articles.");
            return;
        }
        if (!validateBlogForm()) return;
        try {
            selectedBlog.setTitre(titreField.getText().trim());
            selectedBlog.setContenu(contenuField.getText().trim());
            selectedBlog.setImage(imageField.getText().trim());
            selectedBlog.setRegion(regionField.getValue());
            selectedBlog.setCategorie(categorieField.getValue());
            blogCRUD.modifier(selectedBlog);
            refreshData();
            clearForm();
            showInfo("Article modifié.");
        } catch (SQLException e) {
            showError("Erreur modification", e.getMessage());
        }
    }

    @FXML
    private void supprimerBlog() {
        if (selectedBlog == null) {
            showWarning("Sélectionnez un article à supprimer.");
            return;
        }
        if (currentUser == null || selectedBlog.getAuteur() != currentUser.getId()) {
            showWarning("Vous ne pouvez supprimer que vos propres articles.");
            return;
        }
        supprimerBlog(selectedBlog);
    }

    @FXML
    private void ajouterCommentaire() {
        if (selectedBlog == null) {
            showWarning("Sélectionnez un article pour commenter.");
            return;
        }
        if (currentUser == null) {
            showWarning("Aucun utilisateur connecté.");
            return;
        }
        String contenu = newCommentField.getText();
        if (contenu == null || contenu.trim().isEmpty()) {
            showWarning("Le commentaire ne peut pas être vide.");
            return;
        }
        Commentaire c = new Commentaire();
        c.setContenu(contenu.trim());
        c.setUtilisateur(currentUser.getId());
        c.setArticleId(selectedBlog.getId());
        try {
            commentaireCRUD.ajouter(c);
            newCommentField.clear();
            loadAllComments();
            showInfo("Commentaire ajouté.");
        } catch (SQLException e) {
            showError("Erreur ajout commentaire", e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        selectedBlog = null;
        articleIdLabel.setText("Nouveau");
        titreField.clear();
        contenuField.clear();
        imageField.clear();
        regionField.setValue(null);
        categorieField.setValue(null);
        if (currentUser != null) {
            auteurLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        } else {
            auteurLabel.setText("Utilisateur inconnu");
        }
        selectedArticleLabel.setText("(aucun article sélectionné)");
        updateFormButtons();
        clearAllErrors();
    }

    private void filterArticles() {
        String search = searchField.getText().toLowerCase();
        String region = regionFilterCombo.getValue();
        String cat = categorieFilterCombo.getValue();

        List<Blog> filtered = blogList.stream()
                .filter(b -> (search.isEmpty() ||
                        (b.getTitre() != null && b.getTitre().toLowerCase().contains(search)) ||
                        (b.getContenu() != null && b.getContenu().toLowerCase().contains(search)) ||
                        (b.getAuteurNom() != null && b.getAuteurNom().toLowerCase().contains(search))))
                .filter(b -> "Toutes".equals(region) || (b.getRegion() != null && b.getRegion().equals(region)))
                .filter(b -> "Toutes".equals(cat) || (b.getCategorie() != null && b.getCategorie().equals(cat)))
                .toList();
        displayBlogs(filtered);
    }

    private void refreshData() throws SQLException {
        loadBlogs();
        loadAllComments();
        loadLikes();
        loadRatings();
        filterArticles();
        if (selectedBlog != null) {
            blogList.stream()
                    .filter(b -> b.getId() == selectedBlog.getId())
                    .findFirst()
                    .ifPresentOrElse(this::selectBlog, this::clearForm);
        }
        if (displayedDetailBlog != null) {
            afficherCommentairesDetail();
            updateDetailLikeButton();
            updateDetailStars();
            // Re-créer les boutons de partage (au cas où les données changent)
            detailShareBox.getChildren().clear();
            Button whatsappBtn = createShareButton("WhatsApp", "/images/whatsapp.png", "#25D366", displayedDetailBlog);
            Button facebookBtn = createShareButton("Facebook", "/images/facebook.png", "#4267B2", displayedDetailBlog);
            Button instagramBtn = createShareButton("Instagram", "/images/instagram.png", "#C13584", displayedDetailBlog);
            whatsappBtn.setOnAction(e -> share("WhatsApp", displayedDetailBlog));
            facebookBtn.setOnAction(e -> share("Facebook", displayedDetailBlog));
            instagramBtn.setOnAction(e -> share("Instagram", displayedDetailBlog));
            detailShareBox.getChildren().addAll(whatsappBtn, facebookBtn, instagramBtn);
        }
    }

    private void updateStats() {
        totalBlogsLabel.setText(String.valueOf(blogList.size()));
        totalCommentsLabel.setText(String.valueOf(commentaireList.size()));
    }

    private void showInfo(String msg) { statusLabel.setText("✅ " + msg); }
    private void showWarning(String msg) { new Alert(Alert.AlertType.WARNING, msg).show(); }
    private void showError(String title, String msg) { Alert a = new Alert(Alert.AlertType.ERROR, msg); a.setTitle(title); a.show(); }

    private void updateFormButtons() {
        boolean isEditing = (selectedBlog != null);
        ajouterBtn.setVisible(!isEditing);
        ajouterBtn.setManaged(!isEditing);
        modifierBtn.setVisible(isEditing);
        modifierBtn.setManaged(isEditing);
        supprimerBtn.setVisible(isEditing);
        supprimerBtn.setManaged(isEditing);
    }

    // ========== PARTAGE CRÉATIF ==========

    /**
     * Affiche un popup contextuel avec les options de partage sous le bouton.
     */
    private void showSharePopup(Button anchor, Blog blog) {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        VBox content = new VBox(10);
        content.setStyle("-fx-background-color: rgba(30,30,30,0.95); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 15; -fx-padding: 15;");
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Partager sur");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button whatsappBtn = createShareButton("WhatsApp", "/images/whatsapp.png", "#25D366", blog, popup);
        Button facebookBtn = createShareButton("Facebook", "/images/facebook.png", "#4267B2", blog, popup);
        Button instagramBtn = createShareButton("Instagram", "/images/instagram.png", "#C13584", blog, popup);

        content.getChildren().addAll(title, whatsappBtn, facebookBtn, instagramBtn);

        popup.getContent().add(content);

        // Positionner le popup sous le bouton
        popup.show(anchor, anchor.localToScreen(0, anchor.getHeight()).getX(), anchor.localToScreen(0, anchor.getHeight()).getY());
    }

    /**
     * Crée un bouton de partage stylisé avec icône.
     * @param name Nom de la plateforme
     * @param iconPath Chemin vers l'icône dans les ressources
     * @param color Couleur de fond
     * @param blog Article à partager
     * @param popup Popup à fermer après action (peut être null)
     */
    private Button createShareButton(String name, String iconPath, String color, Blog blog, Popup popup) {
        Button btn = new Button(name);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-size: 12px;");
        btn.setMaxWidth(Double.MAX_VALUE);

        // Charger l'icône si disponible
        try {
            InputStream is = getClass().getResourceAsStream(iconPath);
            if (is != null) {
                Image icon = new Image(is);
                ImageView iv = new ImageView(icon);
                iv.setFitHeight(20);
                iv.setFitWidth(20);
                btn.setGraphic(iv);
                btn.setContentDisplay(ContentDisplay.LEFT);
            } else {
                // Fallback emoji
                String emoji = "";
                if (name.equals("WhatsApp")) emoji = "📱 ";
                else if (name.equals("Facebook")) emoji = "📘 ";
                else if (name.equals("Instagram")) emoji = "📷 ";
                btn.setText(emoji + name);
            }
        } catch (Exception e) {
            // Fallback texte simple
            btn.setText(name);
        }

        btn.setOnAction(e -> {
            share(name, blog);
            if (popup != null) popup.hide();
        });

        return btn;
    }

    /**
     * Version simplifiée pour les boutons sans popup (vue détail).
     */
    private Button createShareButton(String name, String iconPath, String color, Blog blog) {
        return createShareButton(name, iconPath, color, blog, null);
    }

    /**
     * Effectue le partage vers la plateforme choisie.
     */
    private void share(String platform, Blog blog) {
        String titre = blog.getTitre();
        String contenu = blog.getContenu();
        String articleUrl = "http://wingo.tn/article/" + blog.getId(); // URL fictive
        String shareText = titre + " - " + contenu + " " + articleUrl;
        String link = "";

        try {
            switch (platform) {
                case "WhatsApp":
                    link = "https://wa.me/?text=" + URLEncoder.encode(shareText, StandardCharsets.UTF_8);
                    break;
                case "Facebook":
                    link = "https://www.facebook.com/sharer/sharer.php?u=" + URLEncoder.encode(articleUrl, StandardCharsets.UTF_8);
                    break;
                case "Instagram":
                    // Instagram n'a pas d'API de partage direct, on copie le texte dans le presse-papier
                    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(shareText), null);
                    showInfo("Texte copié dans le presse-papier pour Instagram");
                    return;
                default:
                    return;
            }
            java.awt.Desktop.getDesktop().browse(URI.create(link));
        } catch (Exception e) {
            showError("Erreur de partage", e.getMessage());
        }
    }

    // Navigation
    @FXML private void goDashboard() { System.out.println("Dashboard"); }
    @FXML private void goBlog() { showListView(); }
    @FXML private void goCommentaires() { System.out.println("Commentaires"); }
    @FXML private void goSettings() { System.out.println("Settings"); }
}