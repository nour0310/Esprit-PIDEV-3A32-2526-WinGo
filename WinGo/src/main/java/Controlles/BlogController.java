package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Services.BlogCRUD;
import Services.CommentaireCRUD;  // Correction ici
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class BlogController implements Initializable {

    // ========== SERVICES ==========
    private final BlogCRUD blogService = new BlogCRUD();
    private final CommentaireCRUD commentaireService = new CommentaireCRUD();  // Changé
    private ObservableList<Blog> blogList = FXCollections.observableArrayList();
    private ObservableList<Commentaire> commentaireList = FXCollections.observableArrayList();
    private Blog selectedBlog = null;

    // ========== COMPOSANTS FXML ==========
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Label totalBlogsLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private Label totalViewsLabel;
    @FXML private Label totalLikesLabel;
    @FXML private ComboBox<String> regionFilterCombo;
    @FXML private FlowPane articlesFlowPane;
    @FXML private FlowPane commentairesFlowPane;
    @FXML private Label articlesCountLabel;
    @FXML private Label commentsCountLabel;
    @FXML private Label articleIdLabel;
    @FXML private TextField titreField;
    @FXML private TextArea contenuField;
    @FXML private TextField imageField;
    @FXML private TextField auteurField;
    @FXML private ComboBox<String> regionField;
    @FXML private ComboBox<String> categorieField;
    @FXML private TextField newCommentField;
    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Button refreshBtn;
    @FXML private Button addCommentBtn;
    @FXML private Button fabButton;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setupComboBoxes();
            loadData();
            setupListeners();
            updateStatistics();
            statusLabel.setText("✅ Prêt - " + blogList.size() + " articles chargés");
        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les données: " + e.getMessage());
        }
    }

    private void setupComboBoxes() {
        ObservableList<String> regions = FXCollections.observableArrayList(
                "Toutes", "Tunis", "Sousse", "Sfax", "Nabeul", "Hammamet", "Djerba",
                "Tozeur", "Douz", "Kairouan", "Monastir", "Mahdia", "Gabès", "Tataouine"
        );
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Plage", "Désert", "Montagne", "Culture", "Bien-être", "Événements", "Gastronomie"
        );
        regionFilterCombo.setItems(regions);
        regionFilterCombo.setValue("Toutes");
        regionField.setItems(regions);
        categorieField.setItems(categories);
    }

    private void setupListeners() {
        searchBtn.setOnAction(e -> filterArticles());
        searchField.setOnAction(e -> filterArticles());
        regionFilterCombo.setOnAction(e -> filterArticles());
        fabButton.setOnAction(e -> clearForm());
        clearBtn.setOnAction(e -> clearForm());
        refreshBtn.setOnAction(e -> refreshData());
        addCommentBtn.setOnAction(e -> ajouterCommentaire());
    }

    private void loadData() throws SQLException {
        blogList.clear();
        blogList.addAll(blogService.readAll());

        commentaireList.clear();
        commentaireList.addAll(commentaireService.readAll());

        displayArticles(blogList);
        displayCommentaires(commentaireList);

        articlesCountLabel.setText("(" + blogList.size() + ")");
        commentsCountLabel.setText("(" + commentaireList.size() + ")");
    }

    private void displayArticles(List<Blog> articles) {
        articlesFlowPane.getChildren().clear();
        for (Blog blog : articles) {
            VBox articleCard = createArticleCard(blog);
            articlesFlowPane.getChildren().add(articleCard);
        }
    }

    private VBox createArticleCard(Blog blog) {
        VBox card = new VBox(10);
        card.getStyleClass().add("article-card");
        card.setPrefWidth(280);
        card.setPrefHeight(320);
        card.setPadding(new Insets(15));
        card.setOnMouseClicked(e -> selectArticle(blog));

        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label categoryIcon = new Label(getCategoryIcon(blog.getCategorie()));
        categoryIcon.setStyle("-fx-font-size: 24px;");
        Label category = new Label(blog.getCategorie());
        category.setStyle("-fx-font-weight: bold; -fx-text-fill: #b7472a;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label region = new Label("📍 " + blog.getRegion());
        region.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        header.getChildren().addAll(categoryIcon, category, spacer, region);

        Label title = new Label(blog.getTitre());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-wrap-text: true;");
        title.setWrapText(true);
        title.setPrefHeight(50);

        String content = blog.getContenu();
        if (content.length() > 80) content = content.substring(0, 80) + "...";
        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-wrap-text: true;");
        contentLabel.setWrapText(true);
        contentLabel.setPrefHeight(60);

        HBox authorBox = new HBox(5);
        authorBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label authorIcon = new Label("👤");
        Label author = new Label(blog.getAuteur());
        author.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 12px;");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        Label date = new Label(blog.getDateCreation().toString());
        date.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
        authorBox.getChildren().addAll(authorIcon, author, spacer2, date);

        HBox stats = new HBox(15);
        stats.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox views = new HBox(3);
        views.getChildren().addAll(new Label("👁️"), new Label(String.valueOf(blog.getNbVues())));
        views.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        HBox likes = new HBox(3);
        likes.getChildren().addAll(new Label("❤️"), new Label(String.valueOf(blog.getLikes())));
        likes.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        long commentCount = commentaireList.stream().filter(c -> c.getBlogId() == blog.getId()).count();
        HBox comments = new HBox(3);
        comments.getChildren().addAll(new Label("💬"), new Label(String.valueOf(commentCount)));
        comments.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        stats.getChildren().addAll(views, likes, comments);

        card.getChildren().addAll(header, title, contentLabel, authorBox, stats);
        return card;
    }

    private String getCategoryIcon(String categorie) {
        switch (categorie) {
            case "Plage": return "🏖️";
            case "Désert": return "🏜️";
            case "Montagne": return "⛰️";
            case "Culture": return "🏛️";
            case "Bien-être": return "🧘";
            case "Événements": return "🎉";
            default: return "📝";
        }
    }

    private void displayCommentaires(List<Commentaire> commentaires) {
        commentairesFlowPane.getChildren().clear();
        commentaires.stream().limit(10).forEach(comment -> {
            VBox commentCard = createCommentCard(comment);
            commentairesFlowPane.getChildren().add(commentCard);
        });
    }

    private VBox createCommentCard(Commentaire comment) {
        VBox card = new VBox(5);
        card.getStyleClass().add("comment-card");
        card.setPrefWidth(200);
        card.setPadding(new Insets(10));

        String articleTitre = blogList.stream()
                .filter(b -> b.getId() == comment.getBlogId())
                .map(Blog::getTitre)
                .findFirst()
                .orElse("Article inconnu");

        Label article = new Label("📌 " + articleTitre);
        article.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Label content = new Label("💬 " + comment.getContenu());
        content.setStyle("-fx-text-fill: #666; -fx-font-size: 11px; -fx-wrap-text: true;");
        content.setWrapText(true);
        content.setPrefHeight(50);

        HBox footer = new HBox(5);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label auteur = new Label("👤 " + comment.getAuteur());
        auteur.setStyle("-fx-text-fill: #333; -fx-font-size: 10px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(comment.getDateCreation().toString());
        date.setStyle("-fx-text-fill: #999; -fx-font-size: 9px;");
        footer.getChildren().addAll(auteur, spacer, date);

        card.getChildren().addAll(article, content, footer);
        return card;
    }

    private void selectArticle(Blog blog) {
        this.selectedBlog = blog;
        articleIdLabel.setText(String.valueOf(blog.getId()));
        titreField.setText(blog.getTitre());
        contenuField.setText(blog.getContenu());
        imageField.setText(blog.getImage());
        auteurField.setText(blog.getAuteur());
        regionField.setValue(blog.getRegion());
        categorieField.setValue(blog.getCategorie());

        List<Commentaire> articleComments = commentaireService.getCommentairesByBlog(blog.getId());
        displayCommentaires(articleComments);
        statusLabel.setText("✅ Article sélectionné: " + blog.getTitre());
    }

    @FXML
    private void ajouterBlog() {
        try {
            if (!validateForm()) return;
            Blog blog = new Blog();
            blog.setTitre(titreField.getText());
            blog.setContenu(contenuField.getText());
            blog.setImage(imageField.getText());
            blog.setAuteur(auteurField.getText());
            blog.setRegion(regionField.getValue());
            blog.setCategorie(categorieField.getValue());
            blog.setNbVues(0);
            blog.setLikes(0);
            blogService.create(blog);
            refreshData();
            clearForm();
            showSuccess("Article ajouté avec succès!");
        } catch (SQLException e) {
            showError("Erreur d'ajout", e.getMessage());
        }
    }

    @FXML
    private void modifierBlog() {
        if (selectedBlog == null) {
            showWarning("Sélection requise", "Veuillez sélectionner un article à modifier.");
            return;
        }
        try {
            if (!validateForm()) return;
            selectedBlog.setTitre(titreField.getText());
            selectedBlog.setContenu(contenuField.getText());
            selectedBlog.setImage(imageField.getText());
            selectedBlog.setAuteur(auteurField.getText());
            selectedBlog.setRegion(regionField.getValue());
            selectedBlog.setCategorie(categorieField.getValue());
            blogService.update(selectedBlog);
            refreshData();
            clearForm();
            showSuccess("Article modifié avec succès!");
        } catch (SQLException e) {
            showError("Erreur de modification", e.getMessage());
        }
    }

    @FXML
    private void supprimerBlog() {
        if (selectedBlog == null) {
            showWarning("Sélection requise", "Veuillez sélectionner un article à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'article");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cet article ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    commentaireService.deleteByBlog(selectedBlog.getId());
                    blogService.delete(selectedBlog.getId());
                    refreshData();
                    clearForm();
                    showSuccess("Article supprimé avec succès!");
                } catch (SQLException e) {
                    showError("Erreur de suppression", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void ajouterCommentaire() {
        if (selectedBlog == null) {
            showWarning("Sélection requise", "Veuillez sélectionner un article pour ajouter un commentaire.");
            return;
        }
        String commentText = newCommentField.getText();
        if (commentText == null || commentText.trim().isEmpty()) {
            showWarning("Champ vide", "Veuillez entrer un commentaire.");
            return;
        }
        try {
            Commentaire comment = new Commentaire();
            comment.setContenu(commentText);
            comment.setAuteur("Utilisateur");
            comment.setBlogId(selectedBlog.getId());
            commentaireService.create(comment);
            newCommentField.clear();
            refreshData();
            showSuccess("Commentaire ajouté!");
        } catch (SQLException e) {
            showError("Erreur d'ajout de commentaire", e.getMessage());
        }
    }

    private void filterArticles() {
        String searchText = searchField.getText().toLowerCase();
        String selectedRegion = regionFilterCombo.getValue();
        List<Blog> filtered = blogList.stream()
                .filter(blog -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                            blog.getTitre().toLowerCase().contains(searchText) ||
                            blog.getContenu().toLowerCase().contains(searchText) ||
                            blog.getAuteur().toLowerCase().contains(searchText);
                    boolean matchesRegion = selectedRegion == null ||
                            selectedRegion.equals("Toutes") ||
                            blog.getRegion().equals(selectedRegion);
                    return matchesSearch && matchesRegion;
                })
                .toList();
        displayArticles(filtered);
        articlesCountLabel.setText("(" + filtered.size() + ")");
    }

    private void refreshData() {
        try {
            loadData();
            updateStatistics();
            filterArticles();
            statusLabel.setText("✅ Données actualisées");
        } catch (SQLException e) {
            showError("Erreur d'actualisation", e.getMessage());
        }
    }

    private void clearForm() {
        selectedBlog = null;
        articleIdLabel.setText("Nouveau");
        titreField.clear();
        contenuField.clear();
        imageField.clear();
        auteurField.clear();
        regionField.setValue(null);
        categorieField.setValue(null);
        newCommentField.clear();
        displayCommentaires(commentaireList);
    }

    private void updateStatistics() throws SQLException {
        totalBlogsLabel.setText(String.valueOf(blogList.size()));
        totalCommentsLabel.setText(String.valueOf(commentaireList.size()));
        int totalViews = blogList.stream().mapToInt(Blog::getNbVues).sum();
        int totalLikes = blogList.stream().mapToInt(Blog::getLikes).sum();
        totalViewsLabel.setText(String.valueOf(totalViews));
        totalLikesLabel.setText(String.valueOf(totalLikes));
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        if (titreField.getText() == null || titreField.getText().trim().isEmpty())
            errors.append("- Le titre est requis\n");
        if (contenuField.getText() == null || contenuField.getText().trim().isEmpty())
            errors.append("- Le contenu est requis\n");
        if (auteurField.getText() == null || auteurField.getText().trim().isEmpty())
            errors.append("- L'auteur est requis\n");
        if (regionField.getValue() == null)
            errors.append("- La région est requise\n");
        if (categorieField.getValue() == null)
            errors.append("- La catégorie est requise\n");
        if (errors.length() > 0) {
            showWarning("Formulaire incomplet", errors.toString());
            return false;
        }
        return true;
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}