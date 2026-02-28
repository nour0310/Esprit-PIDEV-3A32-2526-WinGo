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

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    private List<Blog> allBlogs = new ArrayList<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadData();
        switchToDashboard();
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
                b.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 12 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-weight: bold;");
            } else {
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748B; -fx-alignment: CENTER_LEFT; -fx-padding: 12 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-weight: bold;");
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
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-border-color: #F1F5F9; -fx-border-radius: 15;");
        row.setEffect(new DropShadow(5, Color.web("#00000005")));

        // Thumbnail
        ImageView iv = new ImageView();
        try {
            if (b.getImage() != null && !b.getImage().isEmpty()) {
                iv.setImage(new Image(b.getImage(), true));
            } else {
                iv.setImage(new Image("testt.png")); // Fallback
            }
        } catch (Exception e) {
             iv.setImage(new Image("testt.png"));
        }
        iv.setFitHeight(55);
        iv.setFitWidth(55);
        iv.setPreserveRatio(true);
        StackPane imgWrap = new StackPane(iv);
        imgWrap.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 10;");
        imgWrap.setPrefSize(55,55);

        // Content Info
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label title = new Label(b.getTitre());
        title.setStyle("-fx-text-fill: #1E293B; -fx-font-size: 15px; -fx-font-weight: bold;");
        
        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label author = new Label("By " + b.getAuteurNom());
        author.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
        Label category = new Label(b.getCategorie());
        category.setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #6366F1; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 5;");
        meta.getChildren().addAll(author, category);
        
        info.getChildren().addAll(title, meta);

        // Stats Mini
        VBox stats = new VBox(2);
        stats.setAlignment(Pos.CENTER_RIGHT);
        stats.setMinWidth(120);
        Label date = new Label(b.getDatePublication() != null ? b.getDatePublication().format(dateFormatter) : "");
        date.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
        Label region = new Label("📍 " + b.getRegion());
        region.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px; -fx-font-weight: bold;");
        stats.getChildren().addAll(region, date);

        // Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        editBtn.setOnAction(e -> openEditBlogModal(b));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> confirmDelete(b));

        actions.getChildren().addAll(editBtn, deleteBtn);

        row.getChildren().addAll(imgWrap, info, stats, actions);

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 15; -fx-border-color: #6366F133; -fx-border-radius: 15;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-border-color: #F1F5F9; -fx-border-radius: 15;"));

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
        Dialog<Blog> dialog = new Dialog<>();
        dialog.setTitle(blog == null ? "Ajouter un Article" : "Modifier l'Article");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox form = new VBox(15);
        form.setPrefWidth(450);
        form.setPadding(new Insets(20));

        TextField titre = new TextField(blog != null ? blog.getTitre() : "");
        titre.setPromptText("Titre de l'article");
        TextArea contenu = new TextArea(blog != null ? blog.getContenu() : "");
        contenu.setPromptText("Contenu...");
        contenu.setPrefRowCount(8);
        contenu.setWrapText(true);
        TextField img = new TextField(blog != null ? blog.getImage() : "");
        img.setPromptText("URL de l'image");
        
        ComboBox<String> reg = new ComboBox<>(FXCollections.observableArrayList("Tunis", "Sousse", "Sfax", "Monastir", "Djerba", "Nabeul", "Bizerte", "Ariana", "Ben Arous", "Kairouan", "Gafsa", "Gabès", "Kasserine", "Médenine", "Beja", "Jendouba", "Kef", "Mahdia", "Sidi Bouzid", "Siliana", "Tataouine", "Tozeur", "Zaghouan", "Manouba"));
        reg.setValue(blog != null ? blog.getRegion() : "Tunis");
        
        ComboBox<String> cat = new ComboBox<>(FXCollections.observableArrayList("Plage", "Désert", "Montagne", "Culture", "Bien-être", "Événements", "Gastronomie", "Aventure", "Nature", "Histoire"));
        cat.setValue(blog != null ? blog.getCategorie() : "Culture");

        form.getChildren().addAll(
            new Label("Titre"), titre,
            new Label("Contenu"), contenu,
            new Label("Région"), reg,
            new Label("Catégorie"), cat,
            new Label("Lien Image"), img
        );

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (blog == null) {
                    return new Blog(titre.getText(), contenu.getText(), 1, img.getText(), reg.getValue(), cat.getValue());
                } else {
                    blog.setTitre(titre.getText());
                    blog.setContenu(contenu.getText());
                    blog.setImage(img.getText());
                    blog.setRegion(reg.getValue());
                    blog.setCategorie(cat.getValue());
                    return blog;
                }
            }
            return null;
        });

        Optional<Blog> result = dialog.showAndWait();
        result.ifPresent(b -> {
            try {
                if (blog == null) blogCRUD.ajouter(b);
                else blogCRUD.modifier(b);
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
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
                card.setPadding(new Insets(12, 20, 12, 20));
                card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-border-color: #F1F5F9;");
                
                VBox text = new VBox(2);
                HBox.setHgrow(text, Priority.ALWAYS);
                Label user = new Label(c.getUtilisateurNom());
                user.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");
                Label msg = new Label(c.getContenu());
                msg.setWrapText(true);
                msg.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");
                text.getChildren().addAll(user, msg);

                Button del = new Button("Supprimer");
                del.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                del.setOnAction(e -> {
                    try {
                        commentaireCRUD.supprimer(c.getId());
                        loadGlobalComments();
                        loadData(); 
                    } catch (SQLException ex) { ex.printStackTrace(); }
                });

                card.getChildren().addAll(text, del);
                globalCommentsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
