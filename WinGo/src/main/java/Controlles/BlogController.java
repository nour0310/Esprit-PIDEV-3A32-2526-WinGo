package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Entites.Utilisateur;
import Services.BlogCRUD;
import Services.CommentaireCRUD;
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

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BlogController implements Initializable {

    // Services
    private final BlogCRUD blogCRUD = new BlogCRUD();
    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private final UtilisateurCRUD utilisateurCRUD = new UtilisateurCRUD();

    // Données observables
    private ObservableList<Blog> blogList = FXCollections.observableArrayList();
    private ObservableList<Commentaire> commentaireList = FXCollections.observableArrayList();
    private Blog selectedBlog = null;
    private Blog displayedDetailBlog = null;

    // Utilisateur connecté
    private Utilisateur currentUser;

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

    // Filtres
    @FXML private ComboBox<String> regionFilterCombo;
    @FXML private ComboBox<String> categorieFilterCombo;

    // ScrollPanes
    @FXML private ScrollPane listViewScroll;
    @FXML private ScrollPane detailViewScroll;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateShortFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initComboBoxes();
        loadUtilisateurs();
        attachListeners();
        loadInitialData();
        showListView();
        updateFormButtons();
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
                auteurLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
                connectedUserLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
                detailConnectedUserLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
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
        } else {
            System.err.println("backToListBtn est null !");
        }
        detailAddCommentBtn.setOnAction(e -> ajouterCommentaireDetail());
    }

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
            loadBlogs();
            loadAllComments();
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

        // Conteneur de l'image avec clip
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

        content.getChildren().addAll(auteur, date, categorie, extraitLabel, commentCount, actions);
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

    // Méthode utilitaire pour charger une image
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
        // Mise à jour des labels
        detailTitreLabel.setText(blog.getTitre() != null ? blog.getTitre() : "");
        detailAuteurLabel.setText(blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu");
        detailDateLabel.setText(blog.getDatePublication() != null ? blog.getDatePublication().format(dateShortFormatter) : "");
        detailContenuLabel.setText(blog.getContenu() != null ? blog.getContenu() : "");

        // Chargement de l'image
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

    // ========== GESTION DES COMMENTAIRES (VUE DÉTAIL) ==========

    private void afficherCommentairesDetail() {
        if (displayedDetailBlog == null) return;
        detailCommentairesPane.getChildren().clear();
        List<Commentaire> comments = commentaireList.stream()
                .filter(c -> c.getArticleId() == displayedDetailBlog.getId())
                .toList();
        for (Commentaire c : comments) {
            detailCommentairesPane.getChildren().add(createCommentCard(c));
        }
    }

    private VBox createCommentCard(Commentaire commentaire) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.3, 0, 5);");
        card.setPrefWidth(250);
        card.setMaxWidth(250);

        // Contenu
        Label contenuLabel = new Label(commentaire.getContenu() != null ? commentaire.getContenu() : "");
        contenuLabel.setWrapText(true);
        contenuLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-padding: 5;");

        // Auteur et date
        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label auteurLabel = new Label("👤 " + (commentaire.getUtilisateurNom() != null ? commentaire.getUtilisateurNom() : "Utilisateur " + commentaire.getUtilisateur()));
        auteurLabel.setStyle("-fx-text-fill: #FFBD00; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label dateLabel = new Label("📅 " + (commentaire.getDateCommentaire() != null ? commentaire.getDateCommentaire().format(dateFormatter) : ""));
        dateLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        meta.getChildren().addAll(auteurLabel, spacer, dateLabel);

        // Boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        if (currentUser != null && commentaire.getUtilisateur() == currentUser.getId()) {
            Button editBtn = new Button("✏️");
            editBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 5 12; -fx-border-color: rgba(255,255,255,0.4); -fx-border-radius: 20;");
            editBtn.setOnAction(e -> showEditComment(commentaire, card, contenuLabel, meta, actions));

            Button deleteBtn = new Button("🗑️");
            deleteBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 5 12; -fx-border-color: rgba(255,255,255,0.4); -fx-border-radius: 20;");
            deleteBtn.setOnAction(e -> supprimerCommentaireDetail(commentaire));

            actions.getChildren().addAll(editBtn, deleteBtn);
        }

        card.getChildren().addAll(contenuLabel, meta, actions);
        return card;
    }

    private void showEditComment(Commentaire commentaire, VBox card, Label contenuLabel, HBox meta, HBox actions) {
        // Cacher les éléments d'affichage
        card.getChildren().clear();

        // Champ de texte pour l'édition
        TextField editField = new TextField(commentaire.getContenu());
        editField.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.7); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.4); -fx-border-radius: 15; -fx-padding: 8;");

        // Boutons Enregistrer et Annuler
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
                    commentaireList.setAll(commentaireCRUD.afficher());
                    afficherCommentairesDetail(); // Recharge toutes les cartes
                    detailStatusLabel.setText("✅ Commentaire modifié.");
                } catch (SQLException ex) {
                    detailStatusLabel.setText("❌ Erreur : " + ex.getMessage());
                }
            }
        });

        Button cancelBtn = new Button("✕ Annuler");
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 12px;");
        cancelBtn.setOnAction(e -> {
            afficherCommentairesDetail(); // Recharge toutes les cartes
        });

        editActions.getChildren().addAll(saveBtn, cancelBtn);

        // On garde les métadonnées (meta)
        card.getChildren().addAll(editField, meta, editActions);
    }

    private void supprimerCommentaireDetail(Commentaire commentaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce commentaire ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    commentaireCRUD.supprimer(commentaire.getId());
                    commentaireList.setAll(commentaireCRUD.afficher());
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
            commentaireList.setAll(commentaireCRUD.afficher());
            afficherCommentairesDetail();
            detailStatusLabel.setText("✅ Commentaire ajouté.");
        } catch (SQLException e) {
            detailStatusLabel.setText("❌ Erreur : " + e.getMessage());
        }
    }

    // ========== CRUD BLOG ==========

    private void supprimerBlog(Blog blog) {
        if (currentUser == null || blog.getAuteur() != currentUser.getId()) {
            showWarning("Vous ne pouvez supprimer que vos propres articles.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cet article ? Tous les commentaires associés seront également supprimés.",
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
        filterArticles();
        if (selectedBlog != null) {
            blogList.stream()
                    .filter(b -> b.getId() == selectedBlog.getId())
                    .findFirst()
                    .ifPresentOrElse(this::selectBlog, this::clearForm);
        }
        if (displayedDetailBlog != null) {
            commentaireList.setAll(commentaireCRUD.afficher());
            afficherCommentairesDetail();
        }
    }

    private void updateStats() {
        totalBlogsLabel.setText(String.valueOf(blogList.size()));
        totalCommentsLabel.setText(String.valueOf(commentaireList.size()));
    }

    private boolean validateBlogForm() {
        if (titreField.getText().trim().isEmpty()) {
            showWarning("Titre requis.");
            return false;
        }
        if (contenuField.getText().trim().isEmpty()) {
            showWarning("Contenu requis.");
            return false;
        }
        return true;
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

    // Navigation
    @FXML private void goDashboard() { System.out.println("Dashboard"); }
    @FXML private void goAjouter() { System.out.println("Ajouter"); }
    @FXML private void goListe() { System.out.println("Liste"); }
    @FXML private void goSettings() { System.out.println("Settings"); }
    @FXML private void goBlog() { System.out.println("Blog"); }
    @FXML private void goCommentaires() { System.out.println("Commentaires"); }
}