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

    // FXML Components
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

    // Detail view
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

    // Filtres
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
        showListView();
    }

    private void initComboBoxes() {
        ObservableList<String> regions = FXCollections.observableArrayList(
                "Ariana","Béja","Ben Arous","Bizerte","Gabès","Gafsa","Jendouba","Kairouan",
                "Kasserine","Kébili","Le Kef","Mahdia","La Manouba","Médenine","Monastir","Nabeul",
                "Sfax","Sidi Bouzid","Siliana","Sousse","Tataouine","Tozeur","Tunis","Zaghouan"
        );
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Plage","Désert","Montagne","Culture","Bien-être","Événements","Gastronomie","Aventure","Nature","Histoire"
        );
        regionField.setItems(regions);
        categorieField.setItems(categories);

        regionFilterCombo.setItems(FXCollections.observableArrayList("Toutes"));
        regionFilterCombo.getItems().addAll(regions);
        regionFilterCombo.setValue("Toutes");

        categorieFilterCombo.setItems(FXCollections.observableArrayList("Toutes"));
        categorieFilterCombo.getItems().addAll(categories);
        categorieFilterCombo.setValue("Toutes");
    }

    private void loadUtilisateurs() {
        try {
            ObservableList<Utilisateur> users = FXCollections.observableArrayList(utilisateurCRUD.afficher());
            currentUser = users.stream().filter(u -> u.getId() == 1).findFirst().orElse(null);
            auteurLabel.setText(currentUser != null ? currentUser.getPrenom() + " " + currentUser.getNom() : "Utilisateur inconnu");
        } catch (SQLException e) {
            showError("Erreur utilisateurs", e.getMessage());
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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) imageField.setText(file.getAbsolutePath());
    }

    private void loadInitialData() {
        try {
            loadBlogs();
            loadAllComments();
            updateStats();
            statusLabel.setText("✅ Prêt, " + blogList.size() + " articles chargés.");
        } catch (SQLException e) {
            showError("Erreur initialisation", e.getMessage());
        }
    }

    private void loadBlogs() throws SQLException {
        blogList.clear();
        blogList.addAll(blogCRUD.afficher());
        displayBlogs(blogList);
    }

    private void loadAllComments() throws SQLException {
        commentaireList.clear();
        commentaireList.addAll(commentaireCRUD.afficher());
    }

    private void displayBlogs(List<Blog> blogs) {
        articlesFlowPane.getChildren().clear();
        for (Blog b : blogs) articlesFlowPane.getChildren().add(createBlogCard(b));
        totalBlogsLabel.setText(String.valueOf(blogList.size()));
        totalCommentsLabel.setText(String.valueOf(commentaireList.size()));
    }

    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color:#fff; -fx-border-color:#ccc; -fx-border-radius:10; -fx-background-radius:10; -fx-cursor: hand;");
        card.setPrefWidth(300);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        try {
            Image img = (blog.getImage() != null && !blog.getImage().isEmpty()) ?
                    new Image("file:" + blog.getImage(), true) :
                    new Image(getClass().getResourceAsStream("/default.jpg"));
            imageView.setImage(img);
        } catch (Exception ignored) {}

        Label titre = new Label(blog.getTitre());
        titre.setStyle("-fx-font-weight:bold; -fx-font-size:16px;");
        Label auteur = new Label("👤 " + (blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu"));
        Label date = new Label("📅 " + (blog.getDatePublication() != null ? blog.getDatePublication().format(dateShortFormatter) : ""));
        Label extrait = new Label(blog.getContenu().length() > 80 ? blog.getContenu().substring(0,80)+"..." : blog.getContenu());
        extrait.setWrapText(true);
        Button voirBtn = new Button("Voir"); voirBtn.setOnAction(e -> showDetailView(blog));
        Button modifierBtn = new Button("Modifier"); modifierBtn.setOnAction(e -> selectBlog(blog));
        Button supprimerBtn = new Button("Supprimer"); supprimerBtn.setOnAction(e -> supprimerBlog(blog));
        HBox actions = new HBox(5, voirBtn, modifierBtn, supprimerBtn);
        actions.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageView, titre, auteur, date, extrait, actions);
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
            Image img = (blog.getImage() != null && !blog.getImage().isEmpty()) ? new Image("file:" + blog.getImage()) : new Image(getClass().getResourceAsStream("/default.jpg"));
            detailImageView.setImage(img);
        } catch (Exception ignored) {}
        afficherCommentairesDetail();
        listView.setVisible(false);
        detailView.setVisible(true);
    }

    private void showListView() {
        detailView.setVisible(false);
        listView.setVisible(true);
        displayedDetailBlog = null;
    }

    private void afficherCommentairesDetail() {
        if (displayedDetailBlog == null) return;
        detailCommentairesPane.getChildren().clear();
        List<Commentaire> comments = commentaireList.stream().filter(c -> c.getArticleId() == displayedDetailBlog.getId()).toList();
        for (Commentaire c : comments) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(5));
            card.setStyle("-fx-background-color:#eee; -fx-background-radius:5;");
            Label contenu = new Label(c.getContenu());
            Label auteur = new Label("👤 " + (c.getUtilisateurNom() != null ? c.getUtilisateurNom() : "Inconnu"));
            Label date = new Label("📅 " + (c.getDateCommentaire() != null ? c.getDateCommentaire().format(dateFormatter) : ""));
            card.getChildren().addAll(contenu, auteur, date);
            detailCommentairesPane.getChildren().add(card);
        }
    }

    private void ajouterCommentaireDetail() {
        if (displayedDetailBlog == null || currentUser == null) return;
        String contenu = detailNewCommentField.getText().trim();
        if (contenu.isEmpty()) return;
        Commentaire c = new Commentaire();
        c.setContenu(contenu); c.setArticleId(displayedDetailBlog.getId()); c.setUtilisateur(currentUser.getId());
        try {
            commentaireCRUD.ajouter(c);
            commentaireList.setAll(commentaireCRUD.afficher());
            afficherCommentairesDetail();
            detailNewCommentField.clear();
        } catch (SQLException e) { showError("Erreur commentaire", e.getMessage()); }
    }

    private void selectBlog(Blog blog) {
        selectedBlog = blog;
        articleIdLabel.setText(String.valueOf(blog.getId()));
        titreField.setText(blog.getTitre());
        contenuField.setText(blog.getContenu());
        imageField.setText(blog.getImage());
        regionField.setValue(blog.getRegion());
        categorieField.setValue(blog.getCategorie());
        selectedArticleLabel.setText("Article sélectionné : " + blog.getTitre());
    }

    private void supprimerBlog(Blog blog) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet article ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    commentaireCRUD.supprimerParArticle(blog.getId());
                    blogCRUD.supprimer(blog.getId());
                    loadBlogs();
                    loadAllComments();
                    showListView();
                } catch (SQLException e) { showError("Erreur suppression", e.getMessage()); }
            }
        });
    }

    private void clearForm() {
        selectedBlog = null;
        articleIdLabel.setText("Nouveau");
        titreField.clear();
        contenuField.clear();
        imageField.clear();
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
                .filter(b -> (search.isEmpty() || b.getTitre().toLowerCase().contains(search) || b.getContenu().toLowerCase().contains(search)))
                .filter(b -> "Toutes".equals(region) || region.equals(b.getRegion()))
                .filter(b -> "Toutes".equals(cat) || cat.equals(b.getCategorie()))
                .toList();
        displayBlogs(filtered);
    }

    private void updateStats() {
        totalBlogsLabel.setText(String.valueOf(blogList.size()));
        totalCommentsLabel.setText(String.valueOf(commentaireList.size()));
    }

    private void showError(String title, String msg) { Alert a = new Alert(Alert.AlertType.ERROR, msg); a.setTitle(title); a.show(); }
}