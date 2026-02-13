package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Services.BlogCRUD;
import Services.CommentaireCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.Cursor;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlogController {

    // ========== FLOWPANE ==========
    @FXML private FlowPane articlesFlowPane;
    @FXML private FlowPane commentairesFlowPane;

    // ========== FORMULAIRE BLOG ==========
    @FXML private TextField titreField;
    @FXML private TextArea contenuField;
    @FXML private TextField imageField;
    @FXML private TextField auteurField;
    @FXML private ComboBox<String> regionCombo;
    @FXML private ComboBox<String> categorieCombo;

    // ========== COMMENTAIRES ==========
    @FXML private TextField newCommentField;
    @FXML private TextField commentUtilisateurField;
    @FXML private Label selectedDestinationLabel;
    @FXML private Label selectedDestinationMeta;

    // ========== RECHERCHE ==========
    @FXML private TextField searchField;

    // ========== STATS (simples pour front office) ==========
    @FXML private Label statsLabel;

    // ========== SERVICES ==========
    private BlogCRUD blogCRUD = new BlogCRUD();
    private CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private ObservableList<Blog> blogList = FXCollections.observableArrayList();
    private Blog selectedBlog = null;

    @FXML
    public void initialize() {
        configureComboBoxes();
        loadBlogs();
        setupListeners();
        updateStats();
    }

    private void configureComboBoxes() {
        regionCombo.getItems().addAll("Nord", "Sud", "Centre", "Djerba", "Hammamet", "Tozeur", "Douz", "Sousse", "Tunis", "Carthage", "Tabarka", "Bizerte");
        categorieCombo.getItems().addAll("Voyage", "Histoire", "Gastronomie", "Culture", "Aventure", "Plage", "Désert", "Montagne", "Famille");
        regionCombo.setValue("Djerba");
        categorieCombo.setValue("Voyage");
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterBlogs(newVal));
    }

    private void loadBlogs() {
        try {
            blogList.setAll(blogCRUD.afficher());
            displayBlogCards();
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "❌ Impossible de charger les récits");
        }
    }

    private void displayBlogCards() {
        articlesFlowPane.getChildren().clear();
        for (Blog blog : blogList) {
            articlesFlowPane.getChildren().add(createBlogCard(blog));
        }
        if (blogList.isEmpty()) {
            Label emptyLabel = new Label("📝 Aucun récit de voyage pour le moment. Soyez le premier à partager !");
            emptyLabel.setStyle("-fx-padding: 30; -fx-font-size: 14px; -fx-text-fill: #9E0059;");
            articlesFlowPane.getChildren().add(emptyLabel);
        }
    }

    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; " +
                "-fx-border-color: #FF005440; -fx-border-width: 1; -fx-padding: 18; " +
                "-fx-effect: dropshadow(three-pass-box, #9E005930, 8, 0, 0, 3);");
        card.setPrefWidth(280);
        card.setCursor(Cursor.HAND);

        // En-tête avec titre et auteur
        VBox header = new VBox(5);
        Label title = new Label(blog.getTitre());
        title.setStyle("-fx-font-weight: 900; -fx-font-size: 18px; -fx-text-fill: #390099;");
        title.setWrapText(true);

        Label author = new Label("✈️ par " + blog.getAuteur());
        author.setStyle("-fx-font-size: 12px; -fx-text-fill: #9E0059; -fx-font-weight: 600;");

        header.getChildren().addAll(title, author);

        // Contenu (tronqué)
        Label content = new Label(blog.getContenu());
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-padding: 8 0 5 0;");
        content.setMaxHeight(65);
        if (blog.getContenu().length() > 80) {
            content.setText(blog.getContenu().substring(0, 80) + "...");
        }

        // Métadonnées (région et catégorie)
        HBox meta = new HBox(15);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.setStyle("-fx-padding: 5 0 0 0;");

        Label region = new Label("📍 " + blog.getRegion());
        region.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF5400; -fx-font-weight: 600;");

        Label category = new Label("🏷️ " + blog.getCategorie());
        category.setStyle("-fx-font-size: 12px; -fx-text-fill: #9E0059; -fx-font-weight: 600;");

        meta.getChildren().addAll(region, category);

        // Boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setStyle("-fx-padding: 10 0 0 0;");

        Button commentBtn = new Button("💬 Commenter");
        commentBtn.setStyle("-fx-background-color: #FF5400; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-size: 12px; -fx-padding: 6 18; -fx-font-weight: 700;");
        commentBtn.setCursor(Cursor.HAND);
        commentBtn.setOnAction(e -> selectBlogForComments(blog));

        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color: #9E0059; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-size: 12px; -fx-padding: 6 12; -fx-font-weight: 700;");
        editBtn.setCursor(Cursor.HAND);
        editBtn.setOnAction(e -> selectBlogForEdit(blog));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle("-fx-background-color: #390099; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-size: 12px; -fx-padding: 6 12; -fx-font-weight: 700;");
        deleteBtn.setCursor(Cursor.HAND);
        deleteBtn.setOnAction(e -> handleDeleteBlog(blog));

        actions.getChildren().addAll(commentBtn, editBtn, deleteBtn);

        card.getChildren().addAll(header, content, meta, actions);
        card.setOnMouseClicked(e -> selectBlogForComments(blog));

        return card;
    }

    private void selectBlogForEdit(Blog blog) {
        selectedBlog = blog;
        titreField.setText(blog.getTitre());
        contenuField.setText(blog.getContenu());
        auteurField.setText(blog.getAuteur());
        regionCombo.setValue(blog.getRegion());
        categorieCombo.setValue(blog.getCategorie());
        imageField.setText(blog.getImage());

        // Changer d'onglet vers le formulaire
        TabPane tabPane = (TabPane) titreField.getScene().lookup(".tab-pane");
        if (tabPane != null) tabPane.getSelectionModel().select(0);
    }

    private void selectBlogForComments(Blog blog) {
        selectedBlog = blog;
        selectedDestinationLabel.setText(blog.getTitre());
        selectedDestinationMeta.setText(blog.getRegion() + " · " + blog.getAuteur());
        loadCommentsForBlog(blog);

        // Changer d'onglet vers les commentaires
        TabPane tabPane = (TabPane) titreField.getScene().lookup(".tab-pane");
        if (tabPane != null) tabPane.getSelectionModel().select(1);
    }

    private void loadCommentsForBlog(Blog blog) {
        commentairesFlowPane.getChildren().clear();
        try {
            List<Commentaire> comments = commentaireCRUD.getCommentsByBlogId(blog.getId());

            if (comments.isEmpty()) {
                Label emptyLabel = new Label("💬 Aucun avis pour cette destination");
                emptyLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9E0059; -fx-padding: 15;");
                commentairesFlowPane.getChildren().add(emptyLabel);
                return;
            }

            for (Commentaire comment : comments) {
                commentairesFlowPane.getChildren().add(createCommentCard(comment));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createCommentCard(Commentaire comment) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #FFF9FB; -fx-background-radius: 12; -fx-border-radius: 12; " +
                "-fx-border-color: #FF005430; -fx-border-width: 1; -fx-padding: 15;");
        card.setPrefWidth(280);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(16);
        avatar.setFill(Color.web("#FF5400"));
        Label icon = new Label("👤");
        icon.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        StackPane avatarStack = new StackPane();
        avatarStack.getChildren().addAll(avatar, icon);

        VBox info = new VBox(2);
        Label user = new Label(comment.getUtilisateur());
        user.setStyle("-fx-font-weight: 900; -fx-font-size: 14px; -fx-text-fill: #390099;");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Label date = new Label(sdf.format(comment.getDateCommentaire()));
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: #9E0059;");

        info.getChildren().addAll(user, date);
        header.getChildren().addAll(avatarStack, info);

        Label content = new Label(comment.getContenu());
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-padding: 5 0 0 0;");

        card.getChildren().addAll(header, content);
        return card;
    }

    private void filterBlogs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            displayBlogCards();
        } else {
            try {
                List<Blog> filtered = blogList.stream()
                        .filter(blog ->
                                blog.getTitre().toLowerCase().contains(keyword.toLowerCase()) ||
                                        blog.getContenu().toLowerCase().contains(keyword.toLowerCase()) ||
                                        blog.getRegion().toLowerCase().contains(keyword.toLowerCase()) ||
                                        blog.getAuteur().toLowerCase().contains(keyword.toLowerCase()))
                        .collect(Collectors.toList());

                articlesFlowPane.getChildren().clear();
                if (filtered.isEmpty()) {
                    Label emptyLabel = new Label("🔍 Aucun résultat pour \"" + keyword + "\"");
                    emptyLabel.setStyle("-fx-padding: 30; -fx-font-size: 14px; -fx-text-fill: #9E0059;");
                    articlesFlowPane.getChildren().add(emptyLabel);
                } else {
                    filtered.forEach(blog -> articlesFlowPane.getChildren().add(createBlogCard(blog)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clearFields() {
        titreField.clear();
        contenuField.clear();
        imageField.clear();
        auteurField.clear();
        regionCombo.setValue("Djerba");
        categorieCombo.setValue("Voyage");
        selectedBlog = null;
    }

    // ========== CRUD BLOG ==========
    @FXML
    private void handleCreateBlog() {
        if (!validateForm()) return;

        try {
            Blog blog = new Blog(
                    titreField.getText().trim(),
                    contenuField.getText().trim(),
                    imageField.getText().trim(),
                    auteurField.getText().trim(),
                    regionCombo.getValue(),
                    categorieCombo.getValue()
            );

            blogCRUD.ajouter(blog);
            loadBlogs();
            clearFields();
            showAlert("Succès", "✅ Votre récit a été publié !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "❌ Erreur lors de la publication");
        }
    }

    @FXML
    private void handleUpdateBlog() {
        if (selectedBlog == null) {
            showAlert("Attention", "⚠️ Sélectionnez un récit à modifier");
            return;
        }

        if (!validateForm()) return;

        try {
            selectedBlog.setTitre(titreField.getText().trim());
            selectedBlog.setContenu(contenuField.getText().trim());
            selectedBlog.setImage(imageField.getText().trim());
            selectedBlog.setAuteur(auteurField.getText().trim());
            selectedBlog.setRegion(regionCombo.getValue());
            selectedBlog.setCategorie(categorieCombo.getValue());

            blogCRUD.modifier(selectedBlog);
            loadBlogs();
            clearFields();
            showAlert("Succès", "✅ Récit modifié !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "❌ Erreur lors de la modification");
        }
    }

    private void handleDeleteBlog(Blog blog) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le récit");
        confirm.setContentText("Voulez-vous vraiment supprimer \"" + blog.getTitre() + "\" ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                blogCRUD.supprimer(blog.getId());
                blogList.remove(blog);
                displayBlogCards();
                if (selectedBlog != null && selectedBlog.getId() == blog.getId()) {
                    clearFields();
                    selectedDestinationLabel.setText("Aucune destination");
                    selectedDestinationMeta.setText("Cliquez sur une carte pour voir les avis");
                    commentairesFlowPane.getChildren().clear();
                    selectedBlog = null;
                }
                updateStats();
                showAlert("Succès", "🗑️ Récit supprimé");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "❌ Erreur lors de la suppression");
            }
        }
    }

    @FXML
    private void handleDeleteBlog() {
        if (selectedBlog == null) {
            showAlert("Attention", "⚠️ Sélectionnez un récit à supprimer");
            return;
        }
        handleDeleteBlog(selectedBlog);
    }

    @FXML
    private void handleAddComment() {
        if (selectedBlog == null) {
            showAlert("Attention", "⚠️ Sélectionnez d'abord une destination");
            return;
        }

        if (commentUtilisateurField.getText().trim().isEmpty()) {
            showAlert("Validation", "👤 Veuillez entrer votre nom");
            return;
        }

        if (newCommentField.getText().trim().isEmpty()) {
            showAlert("Validation", "💬 Veuillez entrer votre avis");
            return;
        }

        try {
            Commentaire comment = new Commentaire(
                    newCommentField.getText().trim(),
                    new Date(),
                    selectedBlog.getId(),
                    commentUtilisateurField.getText().trim()
            );

            commentaireCRUD.ajouter(comment);
            loadCommentsForBlog(selectedBlog);
            commentUtilisateurField.clear();
            newCommentField.clear();
            updateStats();
            showAlert("Succès", "✅ Avis ajouté !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "❌ Erreur lors de l'ajout");
        }
    }

    // ========== PAGES VIDES POUR LES AUTRES MODULES ==========
    @FXML private void handleUtilisateur() {
        showAlert("Module Utilisateur", "👤 Page réservée à Yomna & Balkis");
    }
    @FXML private void handleEvenement() {
        showAlert("Module Événements", "📅 Page réservée à Fatma");
    }
    @FXML private void handleReclamation() {
        showAlert("Module Réclamations", "📢 Page réservée à Nour EL Houda");
    }
    @FXML private void handleBoutique() {
        showAlert("Module Boutique", "🛍️ Page réservée à Nour");
    }
    @FXML private void handleRestauration() {
        showAlert("Module Restauration", "🍽️ Page réservée à Abderahmen");
    }
    @FXML private void handleTransport() {
        showAlert("Module Transport", "🚌 Page transport");
    }
    @FXML private void handleSuggestion() {
        showAlert("Module Suggestions", "💡 Page suggestions");
    }
    @FXML private void handleBlog() {
        showAlert("Module Blog", "📝 Vous êtes sur le carnet de voyage !");
    }

    @FXML
    private void handleHelp() {
        showAlert("Aide - Blog WinGo",
                "📝 CARNET DE VOYAGE COMMUNAUTAIRE\n\n" +
                        "📍 Publiez vos expériences de voyage\n" +
                        "💬 Commentez les récits des autres voyageurs\n" +
                        "🔍 Recherchez des destinations\n\n" +
                        "Partagez vos aventures avec la communauté ! ✈️");
    }

    private boolean validateForm() {
        if (titreField.getText().trim().isEmpty()) {
            showAlert("Validation", "📍 La destination est obligatoire");
            titreField.requestFocus();
            return false;
        }
        if (contenuField.getText().trim().isEmpty()) {
            showAlert("Validation", "📝 Le récit est obligatoire");
            contenuField.requestFocus();
            return false;
        }
        if (auteurField.getText().trim().isEmpty()) {
            showAlert("Validation", "👤 Votre nom est obligatoire");
            auteurField.requestFocus();
            return false;
        }
        return true;
    }

    private void updateStats() {
        try {
            List<Blog> blogs = blogCRUD.afficher();
            int totalComments = 0;
            for (Blog blog : blogs) {
                totalComments += commentaireCRUD.getCommentsByBlogId(blog.getId()).size();
            }
            statsLabel.setText(blogs.size() + " récits · " + totalComments + " avis");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

