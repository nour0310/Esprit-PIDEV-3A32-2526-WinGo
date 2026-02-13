package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Services.BlogCRUD;
import Services.CommentaireCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class BlogController implements Initializable {

    private final BlogCRUD blogCRUD = new BlogCRUD();
    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();

    private ObservableList<Blog> blogList = FXCollections.observableArrayList();
    private ObservableList<Commentaire> commentaireList = FXCollections.observableArrayList();
    private Blog selectedBlog = null;

    // ========== COMPOSANTS FXML ==========
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private ComboBox<String> regionFilterCombo;
    @FXML private ComboBox<String> categorieFilterCombo;
    @FXML private Label totalBlogsLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private FlowPane articlesFlowPane;
    @FXML private FlowPane commentairesFlowPane;
    @FXML private Label selectedArticleLabel;
    @FXML private Label articleIdLabel;
    @FXML private TextField titreField;
    @FXML private TextArea contenuField;
    @FXML private TextField imageField;
    @FXML private TextField auteurIdField;        // ID de l'utilisateur
    @FXML private ComboBox<String> regionField;
    @FXML private ComboBox<String> categorieField;
    @FXML private TextField newCommentField;
    @FXML private TextField commentUserField;      // ID de l'utilisateur pour le commentaire
    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Button addCommentBtn;
    @FXML private Label statusLabel;

    // Formatteur de date pour affichage
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initComboBoxes();
        attachListeners();
        loadInitialData();
    }

    private void initComboBoxes() {
        ObservableList<String> regions = FXCollections.observableArrayList(
                "Toutes", "Tunis", "Sousse", "Sfax", "Nabeul", "Hammamet", "Djerba",
                "Tozeur", "Douz", "Kairouan", "Monastir", "Mahdia", "Gabès", "Tataouine"
        );
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Toutes", "Plage", "Désert", "Montagne", "Culture", "Bien-être", "Événements", "Gastronomie"
        );

        regionFilterCombo.setItems(regions);
        regionFilterCombo.setValue("Toutes");
        categorieFilterCombo.setItems(categories);
        categorieFilterCombo.setValue("Toutes");

        // Pour les formulaires d'édition, on enlève "Toutes"
        regionField.setItems(FXCollections.observableArrayList(regions.subList(1, regions.size())));
        categorieField.setItems(FXCollections.observableArrayList(categories.subList(1, categories.size())));
    }

    private void attachListeners() {
        searchBtn.setOnAction(e -> filterArticles());
        searchField.setOnAction(e -> filterArticles());
        regionFilterCombo.setOnAction(e -> filterArticles());
        categorieFilterCombo.setOnAction(e -> filterArticles());
        clearBtn.setOnAction(e -> clearForm());
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

    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        card.setPrefWidth(250);
        card.setOnMouseClicked(e -> selectBlog(blog));

        Label titre = new Label(blog.getTitre());
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        titre.setWrapText(true);

        String auteurText = blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu";
        Label auteur = new Label("👤 " + auteurText);
        Label region = new Label("📍 " + (blog.getRegion() != null ? blog.getRegion() : "Non spécifiée"));
        Label categorie = new Label("🏷️ " + (blog.getCategorie() != null ? blog.getCategorie() : "Non spécifiée"));

        long nbComments = commentaireList.stream()
                .filter(c -> c.getArticleId() == blog.getId())
                .count();
        Label comments = new Label("💬 " + nbComments);

        card.getChildren().addAll(titre, auteur, region, categorie, comments);
        return card;
    }

    private void selectBlog(Blog blog) {
        this.selectedBlog = blog;
        articleIdLabel.setText(String.valueOf(blog.getId()));
        titreField.setText(blog.getTitre());
        contenuField.setText(blog.getContenu());
        imageField.setText(blog.getImage());
        auteurIdField.setText(String.valueOf(blog.getAuteur()));
        regionField.setValue(blog.getRegion());
        categorieField.setValue(blog.getCategorie());

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
            VBox card = new VBox(3);
            card.setPadding(new Insets(8));
            card.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-radius: 3;");
            card.setPrefWidth(200);

            Label contenu = new Label(c.getContenu());
            contenu.setWrapText(true);
            String auteurText = c.getUtilisateurNom() != null ? c.getUtilisateurNom() : "Utilisateur " + c.getUtilisateur();
            Label auteur = new Label("👤 " + auteurText);
            String dateText = c.getDateCommentaire() != null ? c.getDateCommentaire().format(dateFormatter) : "";
            Label date = new Label(dateText);

            card.getChildren().addAll(contenu, auteur, date);
            commentairesFlowPane.getChildren().add(card);
        }
    }

    @FXML
    private void ajouterBlog() {
        if (!validateBlogForm()) return;
        try {
            int auteurId = Integer.parseInt(auteurIdField.getText().trim());
            Blog b = new Blog(
                    titreField.getText().trim(),
                    contenuField.getText().trim(),
                    imageField.getText().trim(),
                    regionField.getValue(),
                    categorieField.getValue(),
                    auteurId
            );
            blogCRUD.ajouter(b);
            refreshData();
            clearForm();
            showInfo("Article ajouté avec succès.");
        } catch (SQLException | NumberFormatException e) {
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
            selectedBlog.setAuteur(Integer.parseInt(auteurIdField.getText().trim()));

            blogCRUD.modifier(selectedBlog);
            refreshData();
            clearForm();
            showInfo("Article modifié.");
        } catch (SQLException | NumberFormatException e) {
            showError("Erreur modification", e.getMessage());
        }
    }

    @FXML
    private void supprimerBlog() {
        if (selectedBlog == null) {
            showWarning("Sélectionnez un article à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cet article ? Tous les commentaires associés seront également supprimés.",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    // Supprimer d'abord les commentaires liés
                    commentaireCRUD.supprimerParArticle(selectedBlog.getId());
                    // Puis l'article
                    blogCRUD.supprimer(selectedBlog.getId());
                    refreshData();
                    clearForm();
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

            // Recharger les commentaires de l'article sélectionné
            List<Commentaire> comments = commentaireCRUD.getCommentsByArticle(selectedBlog.getId());
            displayCommentaires(comments);
            // Mettre à jour le compteur global
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
        auteurIdField.clear();
        regionField.setValue(null);
        categorieField.setValue(null);
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
            // Essayer de retrouver l'article sélectionné dans la nouvelle liste
            blogList.stream()
                    .filter(b -> b.getId() == selectedBlog.getId())
                    .findFirst()
                    .ifPresentOrElse(this::selectBlog,
                            () -> clearForm()); // si l'article n'existe plus (supprimé), on vide le formulaire
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
        if (auteurIdField.getText().trim().isEmpty()) {
            showWarning("ID auteur requis.");
            return false;
        }
        if (regionField.getValue() == null) {
            showWarning("Région requise.");
            return false;
        }
        if (categorieField.getValue() == null) {
            showWarning("Catégorie requise.");
            return false;
        }
        try {
            Integer.parseInt(auteurIdField.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("L'ID auteur doit être un nombre entier.");
            return false;
        }
        return true;
    }

    private void showInfo(String msg) {
        statusLabel.setText("✅ " + msg);
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
}