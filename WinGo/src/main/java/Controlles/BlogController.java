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

    // Utilisateur connecté (à adapter avec le système d'authentification)
    private Utilisateur currentUser;

    // Composants FXML
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
    @FXML private ComboBox<String> regionField;       // pour les régions
    @FXML private ComboBox<String> categorieField;    // pour les catégories
    @FXML private ComboBox<Utilisateur> auteurCombo;
    @FXML private TextField newCommentField;
    @FXML private TextField commentUserField;
    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Button addCommentBtn;
    @FXML private Label statusLabel;
    @FXML private Button choisirImageBtn;  // nouveau bouton pour choisir une image

    // Pour les filtres (optionnel, si présents dans FXML)
    @FXML private ComboBox<String> regionFilterCombo;
    @FXML private ComboBox<String> categorieFilterCombo;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateShortFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initComboBoxes();
        loadUtilisateurs();
        attachListeners();
        loadInitialData();
    }

    /**
     * Initialise les ComboBox avec les régions et catégories.
     */
    private void initComboBoxes() {
        // Liste des 24 gouvernorats tunisiens
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

        if (regionField != null) regionField.setItems(regions);
        if (categorieField != null) categorieField.setItems(categories);
        if (regionFilterCombo != null) {
            regionFilterCombo.setItems(regions);
            regionFilterCombo.getItems().add(0, "Toutes");
            regionFilterCombo.setValue("Toutes");
        }
        if (categorieFilterCombo != null) {
            categorieFilterCombo.setItems(categories);
            categorieFilterCombo.getItems().add(0, "Toutes");
            categorieFilterCombo.setValue("Toutes");
        }
    }

    /**
     * Charge la liste des utilisateurs dans la ComboBox.
     */
    private void loadUtilisateurs() {
        try {
            ObservableList<Utilisateur> users = FXCollections.observableArrayList(utilisateurCRUD.afficher());
            if (auteurCombo != null) {
                auteurCombo.setItems(users);
                auteurCombo.setCellFactory(param -> new ListCell<Utilisateur>() {
                    @Override
                    protected void updateItem(Utilisateur item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) setText(null);
                        else setText(item.getPrenom() + " " + item.getNom() + " (ID: " + item.getId() + ")");
                    }
                });
                auteurCombo.setButtonCell(new ListCell<Utilisateur>() {
                    @Override
                    protected void updateItem(Utilisateur item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) setText(null);
                        else setText(item.getPrenom() + " " + item.getNom());
                    }
                });
            }

            // Définir l'utilisateur connecté (ici on prend l'ID 1, à adapter)
            currentUser = users.stream().filter(u -> u.getId() == 1).findFirst().orElse(null);
        } catch (SQLException e) {
            showError("Erreur chargement utilisateurs", e.getMessage());
        }
    }

    private void attachListeners() {
        if (searchBtn != null) searchBtn.setOnAction(e -> filterArticles());
        if (searchField != null) searchField.setOnAction(e -> filterArticles());
        if (clearBtn != null) clearBtn.setOnAction(e -> clearForm());
        if (choisirImageBtn != null) choisirImageBtn.setOnAction(e -> choisirImage());
        if (regionFilterCombo != null) regionFilterCombo.setOnAction(e -> filterArticles());
        if (categorieFilterCombo != null) categorieFilterCombo.setOnAction(e -> filterArticles());
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
            if (statusLabel != null) statusLabel.setText("✅ Prêt, " + blogList.size() + " articles chargés.");
        } catch (SQLException e) {
            showError("Erreur de chargement", e.getMessage());
        }
    }

    private void loadBlogs() throws SQLException {
        blogList.clear();
        blogList.addAll(blogCRUD.afficher());
        displayBlogs(blogList);
        if (totalBlogsLabel != null) totalBlogsLabel.setText(String.valueOf(blogList.size()));
    }

    private void loadAllComments() throws SQLException {
        commentaireList.clear();
        commentaireList.addAll(commentaireCRUD.afficher());
        if (totalCommentsLabel != null) totalCommentsLabel.setText(String.valueOf(commentaireList.size()));
    }

    private void displayBlogs(List<Blog> blogs) {
        if (articlesFlowPane == null) return;
        articlesFlowPane.getChildren().clear();
        for (Blog b : blogs) {
            articlesFlowPane.getChildren().add(createBlogCard(b));
        }
    }

    /**
     * Crée une carte de blog moderne avec image, titre, extrait, métadonnées et boutons.
     */
    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); " +
                "-fx-cursor: hand;");
        card.setPrefWidth(320);
        card.setMaxWidth(320);

        // Conteneur de l'image avec effet de zoom au survol
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-radius: 15 15 0 0; -fx-clip: true;");
        imageContainer.setPrefHeight(180);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(320);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.setOnMouseEntered(e -> imageView.setScaleX(1.05));
        imageView.setOnMouseExited(e -> imageView.setScaleX(1.0));

        try {
            if (blog.getImage() != null && !blog.getImage().isEmpty()) {
                Image img = new Image("file:" + blog.getImage(), true);
                imageView.setImage(img);
            } else {
                // Image par défaut (à placer dans resources)
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                imageView.setImage(defaultImg);
            }
        } catch (Exception e) {
            try {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                imageView.setImage(defaultImg);
            } catch (Exception ex) { }
        }
        imageContainer.getChildren().add(imageView);

        // Overlay avec titre (fond semi-transparent)
        Label titleOverlay = new Label(blog.getTitre());
        titleOverlay.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 10; -fx-background-radius: 0 0 10 10;");
        titleOverlay.setMaxWidth(320);
        titleOverlay.setWrapText(true);
        StackPane.setAlignment(titleOverlay, Pos.BOTTOM_LEFT);
        StackPane.setMargin(titleOverlay, new Insets(0, 0, 10, 10));
        imageContainer.getChildren().add(titleOverlay);

        // Badge région en haut à droite (si présente)
        if (blog.getRegion() != null && !blog.getRegion().isEmpty()) {
            Label regionBadge = new Label("📍 " + blog.getRegion());
            regionBadge.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.6); -fx-padding: 5 10; -fx-background-radius: 20;");
            StackPane.setAlignment(regionBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(regionBadge, new Insets(10, 10, 0, 0));
            imageContainer.getChildren().add(regionBadge);
        }

        // Contenu texte sous l'image
        VBox content = new VBox(10);
        content.setPadding(new Insets(15, 15, 15, 15));

        // Auteur et date avec nombre de commentaires
        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label auteur = new Label("👤 " + (blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu"));
        auteur.setStyle("-fx-text-fill: #b7472a; -fx-font-size: 13px;");
        Label date = new Label("📅 " + (blog.getDatePublication() != null ? blog.getDatePublication().format(dateShortFormatter) : ""));
        date.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 13px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        long nbComments = commentaireList.stream().filter(c -> c.getArticleId() == blog.getId()).count();
        Label comments = new Label("💬 " + nbComments);
        comments.setStyle("-fx-text-fill: #3498db; -fx-font-size: 13px;");
        meta.getChildren().addAll(auteur, date, spacer, comments);

        // Extrait du contenu
        String extrait = blog.getContenu().length() > 100 ? blog.getContenu().substring(0, 100) + "..." : blog.getContenu();
        Label contenuLabel = new Label(extrait);
        contenuLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
        contenuLabel.setWrapText(true);

        // Boutons d'action : Voir, Modifier, Supprimer
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        Button voirBtn = new Button("Voir");
        voirBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        voirBtn.setOnAction(e -> {
            // À implémenter : afficher les détails complets (peut-être une nouvelle fenêtre ou une section dédiée)
            // Pour l'instant, on sélectionne simplement le blog
            selectBlog(blog);
        });

        Button modifierBtn = new Button("Modifier");
        modifierBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        modifierBtn.setOnAction(e -> selectBlog(blog)); // remplit le formulaire

        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        supprimerBtn.setOnAction(e -> supprimerBlog(blog));

        actions.getChildren().addAll(voirBtn, modifierBtn, supprimerBtn);

        content.getChildren().addAll(meta, contenuLabel, actions);
        card.getChildren().addAll(imageContainer, content);

        // Clic sur la carte pour sélectionner (en plus du bouton)
        card.setOnMouseClicked(e -> selectBlog(blog));

        return card;
    }

    private void selectBlog(Blog blog) {
        this.selectedBlog = blog;
        if (articleIdLabel != null) articleIdLabel.setText(String.valueOf(blog.getId()));
        if (titreField != null) titreField.setText(blog.getTitre());
        if (contenuField != null) contenuField.setText(blog.getContenu());
        if (imageField != null) imageField.setText(blog.getImage());
        if (regionField != null) regionField.setValue(blog.getRegion());
        if (categorieField != null) categorieField.setValue(blog.getCategorie());
        // Sélectionner l'auteur correspondant dans la combo
        if (auteurCombo != null) {
            for (Utilisateur u : auteurCombo.getItems()) {
                if (u.getId() == blog.getAuteur()) {
                    auteurCombo.setValue(u);
                    break;
                }
            }
        }
        if (selectedArticleLabel != null) selectedArticleLabel.setText("Article sélectionné : " + blog.getTitre());

        try {
            List<Commentaire> comments = commentaireCRUD.getCommentsByArticle(blog.getId());
            displayCommentaires(comments);
        } catch (SQLException e) {
            showError("Erreur lors du chargement des commentaires", e.getMessage());
        }
    }

    private void displayCommentaires(List<Commentaire> comments) {
        if (commentairesFlowPane == null) return;
        commentairesFlowPane.getChildren().clear();
        for (Commentaire c : comments) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(8));
            card.setStyle("-fx-background-color: rgba(0,0,0,0.6); " +
                    "-fx-background-radius: 8; " +
                    "-fx-border-color: #c49a6c; " +
                    "-fx-border-radius: 8;");
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
            int auteurId = auteurCombo.getValue().getId();
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
            int auteurId = auteurCombo.getValue().getId();
            selectedBlog.setTitre(titreField.getText().trim());
            selectedBlog.setContenu(contenuField.getText().trim());
            selectedBlog.setImage(imageField.getText().trim());
            selectedBlog.setRegion(regionField.getValue());
            selectedBlog.setCategorie(categorieField.getValue());
            selectedBlog.setAuteur(auteurId);

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
                    showInfo("Article supprimé.");
                } catch (SQLException e) {
                    showError("Erreur suppression", e.getMessage());
                }
            }
        });
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
        int userId;
        try {
            userId = Integer.parseInt(commentUserField.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("L'ID utilisateur doit être un nombre.");
            return;
        }

        Commentaire c = new Commentaire();
        c.setContenu(contenu.trim());
        c.setUtilisateur(userId);
        c.setArticleId(selectedBlog.getId());

        try {
            commentaireCRUD.ajouter(c);
            newCommentField.clear();
            commentUserField.clear();

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
        if (articleIdLabel != null) articleIdLabel.setText("Nouveau");
        if (titreField != null) titreField.clear();
        if (contenuField != null) contenuField.clear();
        if (imageField != null) imageField.clear();
        if (regionField != null) regionField.setValue(null);
        if (categorieField != null) categorieField.setValue(null);
        if (auteurCombo != null) auteurCombo.setValue(null);
        if (selectedArticleLabel != null) selectedArticleLabel.setText("(aucun article sélectionné)");
        if (commentairesFlowPane != null) commentairesFlowPane.getChildren().clear();
    }

    private void filterArticles() {
        String search = searchField.getText().toLowerCase();
        String region = regionFilterCombo != null ? regionFilterCombo.getValue() : null;
        String cat = categorieFilterCombo != null ? categorieFilterCombo.getValue() : null;

        List<Blog> filtered = blogList.stream()
                .filter(b -> (search.isEmpty() ||
                        b.getTitre().toLowerCase().contains(search) ||
                        b.getContenu().toLowerCase().contains(search) ||
                        (b.getAuteurNom() != null && b.getAuteurNom().toLowerCase().contains(search))))
                .filter(b -> region == null || region.equals("Toutes") || (b.getRegion() != null && b.getRegion().equals(region)))
                .filter(b -> cat == null || cat.equals("Toutes") || (b.getCategorie() != null && b.getCategorie().equals(cat)))
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
    }

    private void updateStats() {
        if (totalBlogsLabel != null) totalBlogsLabel.setText(String.valueOf(blogList.size()));
        if (totalCommentsLabel != null) totalCommentsLabel.setText(String.valueOf(commentaireList.size()));
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
        if (auteurCombo.getValue() == null) {
            showWarning("Veuillez sélectionner un auteur.");
            return false;
        }
        return true;
    }

    private void showInfo(String msg) {
        if (statusLabel != null) statusLabel.setText("✅ " + msg);
    }

    private void showWarning(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg);
        a.setHeaderText(null);
        a.show();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.show();
    }

    // Méthodes de navigation pour les boutons du menu
    @FXML private void goDashboard() { System.out.println("Dashboard"); }
    @FXML private void goAjouter() { System.out.println("Ajouter"); }
    @FXML private void goListe() { System.out.println("Liste"); }
    @FXML private void goSettings() { System.out.println("Settings"); }
    @FXML private void goBlog() { System.out.println("Blog"); }
    @FXML private void goCommentaires() { System.out.println("Commentaires"); }
}