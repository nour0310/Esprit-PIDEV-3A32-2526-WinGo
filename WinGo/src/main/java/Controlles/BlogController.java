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
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    // Utilisateur connecté
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
    @FXML private Button choisirImageBtn;
    @FXML private ComboBox<String> regionCombo;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private Label auteurLabel;
    @FXML private TextField newCommentField;
    @FXML private Label connectedUserLabel;
    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Button addCommentBtn;
    @FXML private Label statusLabel;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initComboBoxes();
        loadUtilisateurs();
        attachListeners();
        loadInitialData();
    }

    private void initComboBoxes() {
        ObservableList<String> regions = FXCollections.observableArrayList(
                "Tunis", "Sousse", "Sfax", "Nabeul", "Hammamet", "Djerba",
                "Tozeur", "Douz", "Kairouan", "Monastir", "Mahdia", "Gabès", "Tataouine"
        );
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Plage", "Désert", "Montagne", "Culture", "Bien-être", "Événements", "Gastronomie"
        );
        regionCombo.setItems(regions);
        categorieCombo.setItems(categories);
    }

    private void loadUtilisateurs() {
        try {
            ObservableList<Utilisateur> users = FXCollections.observableArrayList(utilisateurCRUD.afficher());
            currentUser = users.stream().filter(u -> u.getId() == 1).findFirst().orElse(null);
            if (currentUser != null) {
                auteurLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
                connectedUserLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            } else {
                auteurLabel.setText("Utilisateur inconnu");
                connectedUserLabel.setText("Utilisateur inconnu");
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
    }

    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            imageField.setText(file.getAbsolutePath());
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

    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        // Fond dégradé chaud
        card.setStyle("-fx-background-color: linear-gradient(to bottom, #f9e5d2, #f0d9c0); " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #c49a6c; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2); " +
                "-fx-cursor: hand;");
        card.setPrefWidth(250);
        card.setOnMouseClicked(e -> selectBlog(blog));

        // Titre
        Label titre = new Label(blog.getTitre());
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #5a3e2b;");
        titre.setWrapText(true);

        // Auteur
        String auteurText = blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu";
        Label auteur = new Label("👤 " + auteurText);
        auteur.setStyle("-fx-text-fill: #8b5a2b; -fx-font-size: 13px;");

        // Région et Catégorie avec icônes
        String regionIcon = getRegionIcon(blog.getRegion());
        String catIcon = getCategoryIcon(blog.getCategorie());
        Label region = new Label(regionIcon + " " + (blog.getRegion() != null ? blog.getRegion() : "Non spécifiée"));
        region.setStyle("-fx-text-fill: #a0522d; -fx-font-size: 12px;");
        Label categorie = new Label(catIcon + " " + (blog.getCategorie() != null ? blog.getCategorie() : "Non spécifiée"));
        categorie.setStyle("-fx-text-fill: #a0522d; -fx-font-size: 12px;");

        // Image (icône)
        Label imageLabel = new Label("🖼️ " + (blog.getImage() != null ? "Image" : "Pas d'image"));
        imageLabel.setStyle("-fx-text-fill: #8b5a2b; -fx-font-size: 11px;");

        // Nombre de commentaires
        long nbComments = commentaireList.stream()
                .filter(c -> c.getArticleId() == blog.getId())
                .count();
        Label comments = new Label("💬 " + nbComments);
        comments.setStyle("-fx-text-fill: #b7472a; -fx-font-weight: bold; -fx-font-size: 12px;");

        // Date
        String dateText = blog.getDatePublication() != null
                ? blog.getDatePublication().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "";
        Label date = new Label("📅 " + dateText);
        date.setStyle("-fx-text-fill: #a0522d; -fx-font-size: 11px;");

        card.getChildren().addAll(titre, auteur, region, categorie, imageLabel, comments, date);
        return card;
    }

    private String getRegionIcon(String region) {
        if (region == null) return "🌍";
        switch (region) {
            case "Tunis": return "🏛️";
            case "Sousse": return "🏖️";
            case "Sfax": return "⚓";
            case "Djerba": return "🌴";
            case "Tozeur": return "🏜️";
            case "Kairouan": return "🕌";
            default: return "📍";
        }
    }

    private String getCategoryIcon(String categorie) {
        if (categorie == null) return "📌";
        switch (categorie) {
            case "Plage": return "🏖️";
            case "Désert": return "🏜️";
            case "Montagne": return "⛰️";
            case "Culture": return "🏛️";
            case "Bien-être": return "🧘";
            case "Événements": return "🎉";
            case "Gastronomie": return "🍽️";
            default: return "📝";
        }
    }

    private void selectBlog(Blog blog) {
        this.selectedBlog = blog;
        articleIdLabel.setText(String.valueOf(blog.getId()));
        titreField.setText(blog.getTitre());
        contenuField.setText(blog.getContenu());
        imageField.setText(blog.getImage());
        regionCombo.setValue(blog.getRegion());
        categorieCombo.setValue(blog.getCategorie());
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
            Blog b = new Blog(
                    titreField.getText().trim(),
                    contenuField.getText().trim(),
                    currentUser.getId(),
                    imageField.getText().trim(),
                    regionCombo.getValue(),
                    categorieCombo.getValue()
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
            selectedBlog.setRegion(regionCombo.getValue());
            selectedBlog.setCategorie(categorieCombo.getValue());
            // L'auteur ne change pas
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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cet article ? Tous les commentaires associés seront également supprimés.",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    commentaireCRUD.supprimerParArticle(selectedBlog.getId());
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
        regionCombo.setValue(null);
        categorieCombo.setValue(null);
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
        List<Blog> filtered = blogList.stream()
                .filter(b -> search.isEmpty() ||
                        b.getTitre().toLowerCase().contains(search) ||
                        b.getContenu().toLowerCase().contains(search) ||
                        (b.getAuteurNom() != null && b.getAuteurNom().toLowerCase().contains(search)))
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
        if (regionCombo.getValue() == null) {
            showWarning("Veuillez sélectionner une région.");
            return false;
        }
        if (categorieCombo.getValue() == null) {
            showWarning("Veuillez sélectionner une catégorie.");
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

    // Méthodes de navigation pour les boutons du menu
    @FXML private void goDashboard() { System.out.println("Dashboard"); }
    @FXML private void goAjouter() { System.out.println("Ajouter"); }
    @FXML private void goListe() { System.out.println("Liste"); }
    @FXML private void goSettings() { System.out.println("Settings"); }
    @FXML private void goBlog() { System.out.println("Blog"); }
    @FXML private void goCommentaires() { System.out.println("Commentaires"); }
}