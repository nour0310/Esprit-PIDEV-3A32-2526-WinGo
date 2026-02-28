package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Services.BlogCRUD;
import Services.CommentaireCRUD;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;

public class BlogBackOfficeController implements Initializable {

    // Services
    private final BlogCRUD blogCRUD = new BlogCRUD();
    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();

    // Navigation & Views
    @FXML private StackPane contentStack;
    @FXML private VBox viewDashboard;
    @FXML private VBox viewBlogs;
    @FXML private VBox viewComments;
    @FXML private ToggleButton btnDashboard, btnBlogs, btnComments;

    // Dashboard Stats
    @FXML private Label statTotalBlogs, statTotalComments, statEngagement;
    @FXML private PieChart regionChart;
    @FXML private VBox topArticlesContainer;

    // Blogs List
    @FXML private VBox articlesContainer;
    @FXML private TextField searchField;

    // Comments List
    @FXML private VBox globalCommentsContainer;

    // Blog Form (In-Place)
    @FXML private VBox viewBlogForm;
    @FXML private Label formTitle;
    @FXML private TextField formTitre, formImage;
    @FXML private TextArea formContenu;
    @FXML private ComboBox<String> formRegion, formCategorie;

    // Form Live Preview
    @FXML private ImageView previewImage;
    @FXML private Label previewTitre, previewRegion, previewCategorie;

    private Blog editingBlog = null;
    private List<Blog> allBlogs = new ArrayList<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFormComboBoxes();
        setupFormListeners();
        loadData();
        switchToDashboard();
    }

    private void setupFormListeners() {
        formTitre.textProperty().addListener((obs, oldVal, newVal) -> previewTitre.setText(newVal.isEmpty() ? "Votre titre s'affichera ici" : newVal));
        formRegion.valueProperty().addListener((obs, oldVal, newVal) -> previewRegion.setText(newVal == null ? "RÉGION" : newVal.toUpperCase()));
        formCategorie.valueProperty().addListener((obs, oldVal, newVal) -> previewCategorie.setText(newVal == null ? "CATÉGORIE" : newVal.toUpperCase()));
        formImage.textProperty().addListener((obs, oldVal, newVal) -> {
            Image img = loadImage(newVal);
            previewImage.setImage(img != null ? img : new Image("testt.png"));
        });
    }

    private void initFormComboBoxes() {
        formRegion.setItems(FXCollections.observableArrayList("Tunis", "Sousse", "Sfax", "Monastir", "Djerba", "Nabeul", "Bizerte", "Ariana", "Ben Arous", "Kairouan", "Gafsa", "Gabès", "Kasserine", "Médenine", "Beja", "Jendouba", "Kef", "Mahdia", "Sidi Bouzid", "Siliana", "Tataouine", "Tozeur", "Zaghouan", "Manouba"));
        formCategorie.setItems(FXCollections.observableArrayList("Plage", "Désert", "Montagne", "Culture", "Bien-être", "Événements", "Gastronomie", "Aventure", "Nature", "Histoire"));
    }

    // ========== NAVIGATION & SWITCHING ==========

    @FXML
    private void switchToDashboard() {
        showView(viewDashboard);
        updateNavStyles(btnDashboard);
    }

    @FXML
    private void switchToBlogs() {
        showView(viewBlogs);
        updateNavStyles(btnBlogs);
        renderArticlesList(allBlogs);
    }

    @FXML
    private void switchToComments() {
        showView(viewComments);
        updateNavStyles(btnComments);
        loadGlobalComments();
    }

    private void showView(VBox view) {
        viewDashboard.setVisible(false);
        viewBlogs.setVisible(false);
        viewComments.setVisible(false);
        viewBlogForm.setVisible(false);
        
        view.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(400), view);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void updateNavStyles(ToggleButton activeBtn) {
        List<ToggleButton> btns = Arrays.asList(btnDashboard, btnBlogs, btnComments);
        for (ToggleButton b : btns) {
            if (b == activeBtn) {
                b.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 14 22; -fx-background-radius: 14; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 14px;");
                b.setEffect(new DropShadow(10, 0, 4, Color.web("#6366F14D")));
            } else {
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-alignment: CENTER_LEFT; -fx-padding: 14 22; -fx-background-radius: 14; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 14px;");
                b.setEffect(null);
            }
        }
    }

    // ========== DATA LOADING & STATS ==========

    @FXML
    public void loadData() {
        try {
            allBlogs = blogCRUD.afficher();
            List<Commentaire> allComments = commentaireCRUD.afficher();
            
            // Stats
            statTotalBlogs.setText(String.valueOf(allBlogs.size()));
            statTotalComments.setText(String.valueOf(allComments.size()));
            double engagement = allBlogs.isEmpty() ? 0 : (double) allComments.size() / allBlogs.size();
            statEngagement.setText(String.format("%.1f", engagement));

            // Chart
            updateRegionChart(allBlogs);

            // Top Articles
            updateTopArticles(allBlogs, allComments);

            // If we are on blogs view, re-render
            if (viewBlogs.isVisible()) renderArticlesList(allBlogs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateRegionChart(List<Blog> blogs) {
        Map<String, Long> counts = blogs.stream()
                .filter(b -> b.getRegion() != null)
                .collect(Collectors.groupingBy(Blog::getRegion, Collectors.counting()));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        counts.forEach((region, count) -> pieData.add(new PieChart.Data(region, count)));
        regionChart.setData(pieData);
    }

    private void updateTopArticles(List<Blog> blogs, List<Commentaire> comments) {
        topArticlesContainer.getChildren().clear();
        
        // Count comments per article
        Map<Integer, Long> commentCounts = comments.stream()
                .collect(Collectors.groupingBy(Commentaire::getArticleId, Collectors.counting()));

        // Sort blogs by count
        List<Blog> topBlogs = blogs.stream()
                .sorted((b1, b2) -> Long.compare(
                        commentCounts.getOrDefault(b2.getId(), 0L),
                        commentCounts.getOrDefault(b1.getId(), 0L)))
                .limit(5)
                .collect(Collectors.toList());

        for (Blog b : topBlogs) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 15, 10, 15));
            row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12;");

            VBox text = new VBox(2);
            HBox.setHgrow(text, Priority.ALWAYS);
            Label title = new Label(b.getTitre());
            title.setStyle("-fx-font-weight: 800; -fx-font-size: 13px; -fx-text-fill: #1E293B;");
            title.setWrapText(false);
            title.setMaxWidth(220);
            
            Label subtitle = new Label(commentCounts.getOrDefault(b.getId(), 0L) + " commentaires • " + b.getRegion());
            subtitle.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-font-weight: bold;");
            
            text.getChildren().addAll(title, subtitle);

            Label badge = new Label(b.getCategorie());
            badge.setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #6366F1; -fx-font-size: 10px; -fx-font-weight: 900; -fx-padding: 3 8; -fx-background-radius: 6;");

            row.getChildren().addAll(text, badge);
            topArticlesContainer.getChildren().add(row);
        }
    }

    // ========== BLOGS LIST RENDERING ==========

    private void renderArticlesList(List<Blog> blogs) {
        articlesContainer.getChildren().clear();
        for (Blog b : blogs) {
            articlesContainer.getChildren().add(createArticleRow(b));
        }
    }

    private Node createArticleRow(Blog b) {
        HBox row = new HBox(25);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(20, 25, 20, 25));
        row.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-color: #F1F5F9; -fx-border-radius: 20; -fx-border-width: 1;");
        row.setEffect(new DropShadow(15, 0, 5, Color.web("#00000008")));

        // Thumbnail with nice rounded corners
        ImageView iv = new ImageView();
        iv.setFitWidth(100);
        iv.setFitHeight(70);
        iv.setPreserveRatio(false);
        Image img = loadImage(b.getImage());
        if (img != null && !img.isError()) iv.setImage(img);
        else iv.setImage(new Image("testt.png"));
        
        StackPane imgContainer = new StackPane(iv);
        imgContainer.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 15;");
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(100, 70);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imgContainer.setClip(clip);

        // Content
        VBox info = new VBox(8);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(b.getTitre());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #1E293B;");
        
        HBox meta = new HBox(12);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label user = new Label("👤 Admin");
        user.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        Label cat = new Label(b.getCategorie().toUpperCase());
        cat.setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #6366F1; -fx-font-size: 10px; -fx-font-weight: 900; -fx-padding: 4 10; -fx-background-radius: 10;");
        
        meta.getChildren().addAll(user, cat);
        info.getChildren().addAll(title, meta);

        // Stats Badge
        VBox stats = new VBox(5);
        stats.setAlignment(Pos.CENTER_RIGHT);
        stats.setMinWidth(150);
        Label loc = new Label("📍 " + b.getRegion());
        loc.setStyle("-fx-text-fill: #1E293B; -fx-font-weight: 800; -fx-font-size: 13px;");
        Label date = new Label(b.getDatePublication() != null ? b.getDatePublication().format(dateFormatter) : "");
        date.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
        stats.getChildren().addAll(loc, date);

        // Actions
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("✏️");
        editBtn.setOnAction(e -> showBlogForm(b));
        editBtn.setStyle("-fx-background-color: #F59E0B1A; -fx-text-fill: #F59E0B; -fx-background-radius: 12; -fx-font-size: 18px; -fx-cursor: hand; -fx-min-width: 48; -fx-min-height: 48; -fx-font-weight: bold;");

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setOnAction(e -> confirmDelete(b));
        deleteBtn.setStyle("-fx-background-color: #EF44441A; -fx-text-fill: #EF4444; -fx-background-radius: 12; -fx-font-size: 18px; -fx-cursor: hand; -fx-min-width: 48; -fx-min-height: 48; -fx-font-weight: bold;");

        actions.getChildren().addAll(editBtn, deleteBtn);

        row.getChildren().addAll(imgContainer, info, stats, actions);

        // Hover effect
        row.setOnMouseEntered(e -> {
            row.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-padding: 20 25; -fx-border-color: #6366F144; -fx-border-radius: 20; -fx-border-width: 1;");
            row.setEffect(new DropShadow(25, 0, 10, Color.web("#6366F11A")));
            row.setTranslateY(-2);
        });
        row.setOnMouseExited(e -> {
            row.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-padding: 20 25; -fx-border-color: #F1F5F9; -fx-border-radius: 20; -fx-border-width: 1;");
            row.setEffect(new DropShadow(15, 0, 5, Color.web("#00000008")));
            row.setTranslateY(0);
        });

        return row;
    }

    @FXML
    private void rechercherBlog() {
        String query = searchField.getText().toLowerCase();
        List<Blog> filtered = allBlogs.stream()
                .filter(b -> b.getTitre().toLowerCase().contains(query) || 
                             b.getAuteurNom().toLowerCase().contains(query) ||
                             b.getRegion().toLowerCase().contains(query))
                .collect(Collectors.toList());
        renderArticlesList(filtered);
    }

    // ========== CRUD ACTIONS (MODALS) ==========

    @FXML
    private void openAddBlogModal() {
        showBlogForm(null);
    }

    private void openEditBlogModal(Blog b) {
        showBlogForm(b);
    }

    private void showBlogForm(Blog blog) {
        editingBlog = blog;
        if (blog == null) {
            formTitle.setText("Rédaction Créative");
            formTitre.clear();
            formContenu.clear();
            formImage.clear();
            formRegion.getSelectionModel().select(0);
            formCategorie.getSelectionModel().select(0);
        } else {
            formTitle.setText("Modification Créative");
            formTitre.setText(blog.getTitre());
            formContenu.setText(blog.getContenu());
            formImage.setText(blog.getImage());
            formRegion.setValue(blog.getRegion());
            formCategorie.setValue(blog.getCategorie());
        }
        updatePreview();
        showView(viewBlogForm);
    }

    private void updatePreview() {
        previewTitre.setText(formTitre.getText().isEmpty() ? "Votre titre s'affichera ici" : formTitre.getText());
        previewRegion.setText(formRegion.getValue() == null ? "RÉGION" : formRegion.getValue().toUpperCase());
        previewCategorie.setText(formCategorie.getValue() == null ? "CATÉGORIE" : formCategorie.getValue().toUpperCase());
        Image img = loadImage(formImage.getText());
        previewImage.setImage(img != null ? img : new Image("testt.png"));
    }

    @FXML
    private void saveBlog() {
        if (formTitre.getText().isEmpty() || formContenu.getText().isEmpty()) {
            // Petite validation
            return;
        }

        try {
            if (editingBlog == null) {
                // Ajout
                Blog b = new Blog(
                    formTitre.getText(),
                    formContenu.getText(),
                    1, // auteur hardcodé
                    formImage.getText(),
                    formRegion.getValue(),
                    formCategorie.getValue()
                );
                blogCRUD.ajouter(b);
            } else {
                // Modification
                editingBlog.setTitre(formTitre.getText());
                editingBlog.setContenu(formContenu.getText());
                editingBlog.setImage(formImage.getText());
                editingBlog.setRegion(formRegion.getValue());
                editingBlog.setCategorie(formCategorie.getValue());
                blogCRUD.modifier(editingBlog);
            }
            loadData();
            switchToBlogs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelBlogForm() {
        switchToBlogs();
    }

    private void confirmDelete(Blog b) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer '" + b.getTitre() + "' ?");
        alert.setContentText("Toutes les données associées seront perdues.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                blogCRUD.supprimer(b.getId());
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ========== COMMENTS MODERATION ==========

    private void loadGlobalComments() {
        globalCommentsContainer.getChildren().clear();
        try {
            List<Commentaire> comments = commentaireCRUD.afficher();
            for (Commentaire c : comments) {
                HBox card = new HBox(15);
                card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(15, 25, 15, 25));
                card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-border-color: #F1F5F9; -fx-border-radius: 15;");
                card.setEffect(new DropShadow(5, Color.web("#00000005")));
                
                // Avatar (Initials)
                String initials = "";
                if (c.getUtilisateurNom() != null && !c.getUtilisateurNom().isEmpty()) {
                    String[] parts = c.getUtilisateurNom().split(" ");
                    for (String p : parts) if(!p.isEmpty()) initials += p.substring(0,1).toUpperCase();
                }
                if (initials.length() > 2) initials = initials.substring(0, 2);
                
                Label avatarLabel = new Label(initials);
                avatarLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
                StackPane avatar = new StackPane(avatarLabel);
                avatar.setPrefSize(40, 40);
                avatar.setMinSize(40, 40);
                avatar.setStyle("-fx-background-color: #6366F1; -fx-background-radius: 50;");

                VBox content = new VBox(5);
                HBox.setHgrow(content, Priority.ALWAYS);
                
                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);
                Label user = new Label(c.getUtilisateurNom());
                user.setStyle("-fx-font-weight: 800; -fx-text-fill: #1E293B; -fx-font-size: 14px;");
                
                Label date = new Label(c.getDateCommentaire() != null ? c.getDateCommentaire().format(dateFormatter) : "");
                date.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Label articleBadge = new Label(c.getArticleTitre() != null ? "sur " + c.getArticleTitre() : "");
                articleBadge.setStyle("-fx-text-fill: #6366F1; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #EEF2FF; -fx-padding: 2 8; -fx-background-radius: 5;");
                articleBadge.setMaxWidth(200);
                
                header.getChildren().addAll(user, articleBadge, spacer, date);

                Label msg = new Label(c.getContenu());
                msg.setWrapText(true);
                msg.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px; -fx-line-spacing: 1.5;");
                
                content.getChildren().addAll(header, msg);

                Button del = new Button("Supprimer");
                del.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
                del.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Modération");
                    confirm.setHeaderText("Supprimer ce commentaire ?");
                    confirm.setContentText("Cette action est irréversible.");
                    if (confirm.showAndWait().get() == ButtonType.OK) {
                        try {
                            commentaireCRUD.supprimer(c.getId());
                            loadGlobalComments();
                            loadData(); 
                        } catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });

                card.getChildren().addAll(avatar, content, del);
                
                // Hover effect
                card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 15; -fx-border-color: #6366F133; -fx-border-radius: 15;"));
                card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-border-color: #F1F5F9; -fx-border-radius: 15;"));
                
                globalCommentsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir l'Image de l'Article");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(viewBlogForm.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Créer le dossier images s'il n'existe pas dans le projet (optionnel mais recommandé)
                Path targetDir = Paths.get("src/main/resources/images");
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                
                // On copie le fichier
                Path targetPath = targetDir.resolve(selectedFile.getName());
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // On met à jour le champ texte avec le nom relatif ou absolu
                // Ici on met "images/nom.png" pour que ce soit compatible avec nos ressources
                formImage.setText("images/" + selectedFile.getName());
                
            } catch (IOException e) {
                e.printStackTrace();
                // En cas d'erreur on met au moins le chemin absolu
                formImage.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    private Image loadImage(String path) {
        if (path == null || path.isEmpty()) return null;
        try {
            // Tentative 1: Chemin local
            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString(), true);
            }
            
            // Tentative 2: URL directe
            if (path.startsWith("http") || path.startsWith("file:")) {
                return new Image(path, true);
            }

            // Tentative 3: Ressource (si c'est juste un nom de fichier dans le classpath)
            InputStream is = getClass().getResourceAsStream("/" + path);
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception e) {
            // Ignorer
        }
        return null;
    }
}
