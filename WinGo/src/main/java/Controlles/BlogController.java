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
    @FXML private FlowPane commentairesFlowPane;
    @FXML private Label selectedArticleLabel;
    @FXML private Label articleIdLabel;
    @FXML private TextField titreField;
    @FXML private TextArea contenuField;
    @FXML private TextField imageField;
    @FXML private ComboBox<String> regionField;
    @FXML private ComboBox<String> categorieField;
    @FXML private Label auteurLabel;
    @FXML private TextField newCommentField;
    @FXML private TextField commentUserField;
    @FXML private Button choisirImageBtn;
    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Button addCommentBtn;
    @FXML private Label statusLabel;
    @FXML private Label connectedUserLabel; // pour le commentaire

    // Composants FXML de la vue détail
    @FXML private VBox listView;
    @FXML private VBox detailView;
    @FXML private Button backToListBtn;
    @FXML private ImageView detailImageView;
    @FXML private Label detailAuteurLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailRegionLabel;
    @FXML private Label detailCategorieLabel;
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
        backToListBtn.setOnAction(e -> showListView());
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

    // ==================== CARTE AMÉLIORÉE ====================
    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: linear-gradient(to bottom, #fef9e7, #ffffff); " +
                "-fx-background-radius: 12; " +
                "-fx-border-radius: 12; " +
                "-fx-border-color: #c49a6c; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2); " +
                "-fx-cursor: hand;");
        card.setPrefWidth(250);
        card.setMaxWidth(250);
        card.setPadding(new Insets(0));

        // Conteneur de l'image avec effet de superposition
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(160);
        imageContainer.setStyle("-fx-background-radius: 12 12 0 0; -fx-clip: true;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(250);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        try {
            if (blog.getImage() != null && !blog.getImage().isEmpty()) {
                Image img = new Image("file:" + blog.getImage(), true);
                imageView.setImage(img);
            } else {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                imageView.setImage(defaultImg);
            }
        } catch (Exception e) {
            try {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                imageView.setImage(defaultImg);
            } catch (Exception ex) {}
        }
        imageContainer.getChildren().add(imageView);

        // Overlay dégradé pour améliorer la lisibilité du titre
        Region gradientOverlay = new Region();
        gradientOverlay.setStyle("-fx-background-color: linear-gradient(to top, rgba(0,0,0,0.7), transparent);");
        gradientOverlay.setPrefHeight(160);
        gradientOverlay.setMaxWidth(250);
        StackPane.setAlignment(gradientOverlay, Pos.BOTTOM_CENTER);
        imageContainer.getChildren().add(gradientOverlay);

        // Titre superposé en bas à gauche
        Label titleOverlay = new Label(blog.getTitre());
        titleOverlay.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8; -fx-wrap-text: true;");
        titleOverlay.setMaxWidth(230);
        titleOverlay.setWrapText(true);
        StackPane.setAlignment(titleOverlay, Pos.BOTTOM_LEFT);
        StackPane.setMargin(titleOverlay, new Insets(0, 0, 10, 10));
        imageContainer.getChildren().add(titleOverlay);

        // Badge région (en haut à droite)
        if (blog.getRegion() != null && !blog.getRegion().isEmpty()) {
            Label regionBadge = new Label("📍 " + blog.getRegion());
            regionBadge.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 20; -fx-font-size: 12px;");
            StackPane.setAlignment(regionBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(regionBadge, new Insets(8));
            imageContainer.getChildren().add(regionBadge);
        }

        // Contenu texte sous l'image (compact)
        VBox content = new VBox(6);
        content.setPadding(new Insets(10, 10, 10, 10));

        // Ligne auteur + date
        HBox auteurDate = new HBox(6);
        auteurDate.setAlignment(Pos.CENTER_LEFT);
        Label auteur = new Label("👤 " + (blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu"));
        auteur.setStyle("-fx-text-fill: #b7472a; -fx-font-size: 12px;");
        Label date = new Label("📅 " + (blog.getDatePublication() != null ? blog.getDatePublication().format(dateShortFormatter) : ""));
        date.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        auteurDate.getChildren().addAll(auteur, date, spacer);

        // Catégorie
        Label categorie = new Label("🏷️ " + (blog.getCategorie() != null ? blog.getCategorie() : "Non catégorisé"));
        categorie.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px;");

        // Extrait
        String extrait = blog.getContenu().length() > 60 ? blog.getContenu().substring(0, 60) + "..." : blog.getContenu();
        Label extraitLabel = new Label(extrait);
        extraitLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-wrap-text: true;");
        extraitLabel.setWrapText(true);

        // Nombre de commentaires
        long nbComments = commentaireList.stream().filter(c -> c.getArticleId() == blog.getId()).count();
        Label commentCount = new Label("💬 " + nbComments + " commentaire(s)");
        commentCount.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");

        // Boutons d'action (plus petits)
        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER);

        Button voirBtn = new Button("Voir");
        voirBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;");
        voirBtn.setOnAction(e -> showDetailView(blog));

        Button modifierBtn = new Button("Modifier");
        modifierBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;");
        modifierBtn.setOnAction(e -> selectBlog(blog));

        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;");
        supprimerBtn.setOnAction(e -> supprimerBlog(blog));

        actions.getChildren().addAll(voirBtn, modifierBtn, supprimerBtn);

        content.getChildren().addAll(auteurDate, categorie, extraitLabel, commentCount, actions);
        card.getChildren().addAll(imageContainer, content);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                showDetailView(blog);
            }
        });

        return card;
    }

    private void showDetailView(Blog blog) {
        displayedDetailBlog = blog;
        detailAuteurLabel.setText("👤 " + (blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu"));
        detailDateLabel.setText("📅 " + (blog.getDatePublication() != null ? blog.getDatePublication().format(dateShortFormatter) : ""));
        detailRegionLabel.setText("📍 " + (blog.getRegion() != null ? blog.getRegion() : ""));
        detailCategorieLabel.setText("🏷️ " + (blog.getCategorie() != null ? blog.getCategorie() : ""));
        detailContenuLabel.setText(blog.getContenu());

        try {
            if (blog.getImage() != null && !blog.getImage().isEmpty()) {
                Image img = new Image("file:" + blog.getImage(), true);
                detailImageView.setImage(img);
            } else {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                detailImageView.setImage(defaultImg);
            }
        } catch (Exception e) {
            try {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                detailImageView.setImage(defaultImg);
            } catch (Exception ex) {}
        }

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

    private void afficherCommentairesDetail() {
        if (displayedDetailBlog == null) return;
        detailCommentairesPane.getChildren().clear();
        List<Commentaire> comments = commentaireList.stream()
                .filter(c -> c.getArticleId() == displayedDetailBlog.getId())
                .toList();
        for (Commentaire c : comments) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(8));
            card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
            card.setPrefWidth(200);

            Label contenu = new Label(c.getContenu());
            contenu.setWrapText(true);
            contenu.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");

            Label auteur = new Label("👤 " + (c.getUtilisateurNom() != null ? c.getUtilisateurNom() : "Utilisateur " + c.getUtilisateur()));
            auteur.setStyle("-fx-text-fill: #b7472a; -fx-font-size: 11px;");

            Label date = new Label("📅 " + (c.getDateCommentaire() != null ? c.getDateCommentaire().format(dateFormatter) : ""));
            date.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

            if (currentUser != null && c.getUtilisateur() == currentUser.getId()) {
                HBox actions = new HBox(5);
                actions.setAlignment(Pos.CENTER_RIGHT);
                Button editBtn = new Button("✏️");
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 3;");
                editBtn.setOnAction(e -> modifierCommentaireDetail(c));
                Button deleteBtn = new Button("🗑️");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3;");
                deleteBtn.setOnAction(e -> supprimerCommentaireDetail(c));
                actions.getChildren().addAll(editBtn, deleteBtn);
                card.getChildren().addAll(contenu, auteur, date, actions);
            } else {
                card.getChildren().addAll(contenu, auteur, date);
            }
            detailCommentairesPane.getChildren().add(card);
        }
    }

    private void modifierCommentaireDetail(Commentaire commentaire) {
        TextInputDialog dialog = new TextInputDialog(commentaire.getContenu());
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText("Modification du commentaire");
        dialog.setContentText("Nouveau contenu :");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouveauContenu -> {
            if (!nouveauContenu.trim().isEmpty()) {
                commentaire.setContenu(nouveauContenu.trim());
                try {
                    commentaireCRUD.modifier(commentaire);
                    commentaireList.setAll(commentaireCRUD.afficher());
                    afficherCommentairesDetail();
                    detailStatusLabel.setText("✅ Commentaire modifié.");
                } catch (SQLException e) {
                    detailStatusLabel.setText("❌ Erreur : " + e.getMessage());
                }
            }
        });
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

    private void supprimerBlog(Blog blog) {
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
        this.selectedBlog = blog;
        articleIdLabel.setText(String.valueOf(blog.getId()));
        titreField.setText(blog.getTitre());
        contenuField.setText(blog.getContenu());
        imageField.setText(blog.getImage());
        regionField.setValue(blog.getRegion());
        categorieField.setValue(blog.getCategorie());
        auteurLabel.setText(blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu");
        selectedArticleLabel.setText("Article sélectionné : " + blog.getTitre());

        try {
            List<Commentaire> comments = commentaireCRUD.getCommentsByArticle(blog.getId());
            displayCommentaires(comments);
        } catch (SQLException e) {
            showError("Erreur lors du chargement des commentaires", e.getMessage());
        }
    }

    private void displayCommentaires(List<Commentaire> comments) {
        commentairesFlowPane.getChildren().clear();
        for (Commentaire c : comments) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(8));
            card.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-background-radius: 8; -fx-border-color: #c49a6c; -fx-border-radius: 8;");
            card.setPrefWidth(220);
            Label contenu = new Label(c.getContenu());
            contenu.setWrapText(true);
            contenu.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            String auteurText = c.getUtilisateurNom() != null ? c.getUtilisateurNom() : "Utilisateur " + c.getUtilisateur();
            Label auteur = new Label("👤 " + auteurText);
            auteur.setStyle("-fx-text-fill: #FFBD00; -fx-font-size: 12px;");
            String dateText = c.getDateCommentaire() != null ? c.getDateCommentaire().format(dateFormatter) : "";
            Label date = new Label(dateText);
            date.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");
            Button btnModifier = new Button("✏️");
            btnModifier.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-background-radius: 5; -fx-cursor: hand;");
            btnModifier.setOnAction(e -> modifierCommentaire(c));
            Button btnSupprimer = new Button("🗑️");
            btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
            btnSupprimer.setOnAction(e -> supprimerCommentaire(c));
            HBox actions = new HBox(5, btnModifier, btnSupprimer);
            actions.setStyle("-fx-alignment: center-right;");
            card.getChildren().addAll(contenu, auteur, date, actions);
            commentairesFlowPane.getChildren().add(card);
        }
    }

    private void modifierCommentaire(Commentaire commentaire) {
        TextInputDialog dialog = new TextInputDialog(commentaire.getContenu());
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText("Modification du commentaire");
        dialog.setContentText("Nouveau contenu :");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouveauContenu -> {
            if (!nouveauContenu.trim().isEmpty()) {
                commentaire.setContenu(nouveauContenu.trim());
                try {
                    commentaireCRUD.modifier(commentaire);
                    if (selectedBlog != null) {
                        List<Commentaire> comments = commentaireCRUD.getCommentsByArticle(selectedBlog.getId());
                        displayCommentaires(comments);
                    }
                    loadAllComments();
                    showInfo("Commentaire modifié.");
                } catch (SQLException e) {
                    showError("Erreur modification", e.getMessage());
                }
            } else {
                showWarning("Le contenu ne peut pas être vide.");
            }
        });
    }

    private void supprimerCommentaire(Commentaire commentaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer ce commentaire ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    commentaireCRUD.supprimer(commentaire.getId());
                    if (selectedBlog != null) {
                        List<Commentaire> comments = commentaireCRUD.getCommentsByArticle(selectedBlog.getId());
                        displayCommentaires(comments);
                    }
                    loadAllComments();
                    showInfo("Commentaire supprimé.");
                } catch (SQLException e) {
                    showError("Erreur suppression", e.getMessage());
                }
            }
        });
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
        // On utilise l'utilisateur connecté, pas de champ ID
        Commentaire c = new Commentaire();
        c.setContenu(contenu.trim());
        c.setUtilisateur(currentUser.getId());
        c.setArticleId(selectedBlog.getId());
        try {
            commentaireCRUD.ajouter(c);
            newCommentField.clear();
            List<Commentaire> comments = commentaireCRUD.getCommentsByArticle(selectedBlog.getId());
            displayCommentaires(comments);
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
        commentairesFlowPane.getChildren().clear();
    }

    private void filterArticles() {
        String search = searchField.getText().toLowerCase();
        String region = regionFilterCombo.getValue();
        String cat = categorieFilterCombo.getValue();

        List<Blog> filtered = blogList.stream()
                .filter(b -> (search.isEmpty() ||
                        b.getTitre().toLowerCase().contains(search) ||
                        b.getContenu().toLowerCase().contains(search) ||
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

    @FXML private void goDashboard() { System.out.println("Dashboard"); }
    @FXML private void goAjouter() { System.out.println("Ajouter"); }
    @FXML private void goListe() { System.out.println("Liste"); }
    @FXML private void goSettings() { System.out.println("Settings"); }
    @FXML private void goBlog() { System.out.println("Blog"); }
    @FXML private void goCommentaires() { System.out.println("Commentaires"); }
}