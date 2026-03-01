package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Entites.Profil;
import Entites.Utilisateur;
import Services.BlogCRUD;
import Services.CommentaireCRUD;
import Services.ProfilCRUD;
import Services.UtilisateurCRUD;
import Utils.Session;
import javafx.animation.FadeTransition;
import javafx.scene.Cursor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
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

public class BackOfficeController implements Initializable {

    // Services
    private final BlogCRUD blogCRUD = new BlogCRUD();
    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private final UtilisateurCRUD utilisateurCRUD = new UtilisateurCRUD();
    private final ProfilCRUD profilCRUD = new ProfilCRUD();

    // Navigation & Views
    @FXML private StackPane contentStack;
    @FXML private VBox viewDashboard, viewUsers, viewBlogs, viewBlogForm;
    @FXML private TabPane blogTabPane;
    @FXML private ToggleButton btnDashboard, btnUsers, btnBlogs;

    // Dashboard Stats
    @FXML private Label statTotalUsers, statTotalBlogs, statTotalComments;
    @FXML private PieChart userTypeChart;
    @FXML private VBox topArticlesContainer;

    // Section Specific Stats
    @FXML private Label secStatAdmins, secStatClients, secStatTotalBlogs, secStatTopRegion, secStatTotalComments;

    // Users Management
    @FXML private VBox usersCardsContainer;
    @FXML private TextField userSearchField, editUserName, editUserPrenom;
    @FXML private ComboBox<String> editUserType;
    @FXML private HBox userEditBar;

    // Profiles Management
    @FXML private VBox profilesCardsContainer;
    @FXML private TextField profileSearchField;
    @FXML private TextArea editProfBio;
    @FXML private VBox profileEditBar;

    // Blogs List
    @FXML private VBox articlesContainer;
    @FXML private TextField blogSearchField;

    // Comments List
    @FXML private VBox globalCommentsContainer;
    @FXML private TextField commentSearchField;

    // Charts
    @FXML private PieChart articlesRegionChart;
    @FXML private VBox statsChartContainer;

    // Blog Form
    @FXML private Label formTitle;
    @FXML private TextField formTitre, formImage;
    @FXML private TextArea formContenu;
    @FXML private ComboBox<String> formRegion, formCategorie;
    @FXML private ImageView previewImage;
    @FXML private Label previewTitre, previewCategorie;

    // Add User Overlay
    @FXML private StackPane addUserOverlay;
    @FXML private TextField newNomField, newPrenomField, newEmailField, newTelephoneField, newAgeField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<String> newTypeCombo;
    @FXML private Label addErrorMessage;

    private Blog editingBlog = null;
    private List<Blog> allBlogs = new ArrayList<>();
    private List<Commentaire> allComments = new ArrayList<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        initComboBoxes();
        setupFormListeners();
        loadData();
        setupSearchListeners();
        setupTabListeners();
        switchToDashboard();
    }

    private void setupSearchListeners() {
        userSearchField.textProperty().addListener((obs, old, val) -> {
            String newVal = (val == null) ? "" : val.toLowerCase();
            List<Utilisateur> users = new ArrayList<>(); // Will be filled by loadData or current list
            try { users = utilisateurCRUD.afficher(); } catch (SQLException e) {}
            List<Utilisateur> filtered = users.stream().filter(u -> u.getNom().toLowerCase().contains(newVal) || u.getEmail().toLowerCase().contains(newVal) || u.getType().toLowerCase().contains(newVal)).collect(Collectors.toList());
            renderUserCards(filtered);
        });

        profileSearchField.textProperty().addListener((obs, old, val) -> {
            String newVal = (val == null) ? "" : val.toLowerCase();
            List<Profil> profiles = new ArrayList<>();
            try { profiles = profilCRUD.afficher(); } catch (SQLException e) {}
            List<Profil> filtered = profiles.stream().filter(p -> p.getBio().toLowerCase().contains(newVal)).collect(Collectors.toList());
            renderProfileCards(filtered);
        });

        blogSearchField.textProperty().addListener((obs, old, val) -> {
            String newVal = (val == null) ? "" : val.toLowerCase();
            List<Blog> filtered = allBlogs.stream().filter(b -> b.getTitre().toLowerCase().contains(newVal) || b.getRegion().toLowerCase().contains(newVal) || b.getCategorie().toLowerCase().contains(newVal)).collect(Collectors.toList());
            renderArticlesList(filtered);
        });

        commentSearchField.textProperty().addListener((obs, old, val) -> {
            String newVal = (val == null) ? "" : val.toLowerCase();
            List<Commentaire> filtered = allComments.stream().filter(c -> c.getContenu().toLowerCase().contains(newVal) || c.getUtilisateurNom().toLowerCase().contains(newVal)).collect(Collectors.toList());
            renderCommentsList(filtered);
        });
    }

    private void setupTabListeners() {
        blogTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() == 1) loadGlobalComments();
            if (newVal.intValue() == 2) {
                FadeTransition ft = new FadeTransition(Duration.millis(800), statsChartContainer);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            }
        });
    }

    private void initComboBoxes() {
        if (editUserType != null) editUserType.setItems(FXCollections.observableArrayList("user", "admin"));
        if (newTypeCombo != null) {
            newTypeCombo.setItems(FXCollections.observableArrayList("user", "admin"));
            newTypeCombo.setValue("user");
        }
        formRegion.setItems(FXCollections.observableArrayList("Tunis", "Sousse", "Sfax", "Monastir", "Djerba", "Nabeul", "Bizerte", "Ariana", "Ben Arous", "Kairouan", "Gafsa", "Gabès", "Kasserine", "Médenine", "Beja", "Jendouba", "Kef", "Mahdia", "Sidi Bouzid", "Siliana", "Tataouine", "Tozeur", "Zaghouan", "Manouba"));
        formCategorie.setItems(FXCollections.observableArrayList("Plage", "Désert", "Montagne", "Culture", "Bien-être", "Événements", "Gastronomie", "Aventure", "Nature", "Histoire"));
    }

    private void setupFormListeners() {
        formTitre.textProperty().addListener((obs, oldVal, newVal) -> previewTitre.setText(newVal.isEmpty() ? "Votre titre s'affichera ici" : newVal));
        formCategorie.valueProperty().addListener((obs, oldVal, newVal) -> previewCategorie.setText(newVal == null ? "CATÉGORIE" : newVal.toUpperCase()));
        formImage.textProperty().addListener((obs, oldVal, newVal) -> {
            Image img = loadImage(newVal);
            previewImage.setImage(img != null ? img : new Image("testt.png"));
        });
    }

    private void setupTables() {
        userEditBar.setVisible(false);
        profileEditBar.setVisible(false);
    }

    // ========== NAVIGATION ==========

    @FXML private void switchToDashboard() { showView(viewDashboard); updateNavStyles(btnDashboard); }
    @FXML private void switchToUsers() { showView(viewUsers); updateNavStyles(btnUsers); loadUsers(); loadProfiles(); }
    @FXML private void switchToBlogs() { showView(viewBlogs); updateNavStyles(btnBlogs); blogTabPane.getSelectionModel().select(0); renderArticlesList(allBlogs); }

    private void showView(VBox view) {
        viewDashboard.setVisible(false);
        viewUsers.setVisible(false);
        viewBlogs.setVisible(false);
        viewBlogForm.setVisible(false);
        
        view.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(400), view);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void updateNavStyles(ToggleButton activeBtn) {
        List<ToggleButton> btns = Arrays.asList(btnDashboard, btnUsers, btnBlogs);
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

    @FXML public void loadData() {
        try {
            allBlogs = blogCRUD.afficher();
            allComments = commentaireCRUD.afficher();
            List<Utilisateur> allUsers = utilisateurCRUD.afficher();
            
            statTotalBlogs.setText(String.valueOf(allBlogs.size()));
            statTotalComments.setText(String.valueOf(allComments.size()));
            statTotalUsers.setText(String.valueOf(allUsers.size()));

            // Update Section-Specific Stats
            secStatTotalBlogs.setText(String.valueOf(allBlogs.size()));
            secStatTotalComments.setText(String.valueOf(allComments.size()));
            
            long admins = allUsers.stream().filter(u -> "admin".equalsIgnoreCase(u.getType())).count();
            long clients = allUsers.size() - admins;
            secStatAdmins.setText(String.valueOf(admins));
            secStatClients.setText(String.valueOf(clients));

            Map<String, Long> regionCounts = allBlogs.stream().collect(Collectors.groupingBy(Blog::getRegion, Collectors.counting()));
            String topRegion = regionCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("---");
            secStatTopRegion.setText(topRegion);

            updateUserTypeChart(allUsers);
            updateTopArticles(allBlogs, allComments);
            updateRegionalCharts(allBlogs, allComments);
            loadGlobalComments();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateUserTypeChart(List<Utilisateur> users) {
        Map<String, Long> counts = users.stream().collect(Collectors.groupingBy(Utilisateur::getType, Collectors.counting()));
        double total = users.size();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        counts.forEach((type, count) -> {
            double percentage = (total > 0) ? (count / total) * 100 : 0;
            String label = String.format("%s (%.1f%%)", type, percentage);
            pieData.add(new PieChart.Data(label, count));
        });
        userTypeChart.setData(pieData);
    }

    private void updateTopArticles(List<Blog> blogs, List<Commentaire> comments) {
        topArticlesContainer.getChildren().clear();
        Map<Integer, Long> commentCounts = comments.stream().collect(Collectors.groupingBy(Commentaire::getArticleId, Collectors.counting()));
        List<Blog> topBlogs = blogs.stream().sorted((b1, b2) -> Long.compare(commentCounts.getOrDefault(b2.getId(), 0L), commentCounts.getOrDefault(b1.getId(), 0L))).limit(5).collect(Collectors.toList());
        for (Blog b : topBlogs) {
            HBox row = createSmallArticleRow(b);
            topArticlesContainer.getChildren().add(row);
        }
    }

    private void updateRegionalCharts(List<Blog> blogs, List<Commentaire> comments) {
        if (articlesRegionChart == null) return;

        // Pie Chart: Blogs per Region
        Map<String, Long> blogCountsByRegion = blogs.stream()
                .collect(Collectors.groupingBy(Blog::getRegion, Collectors.counting()));
        
        double total = blogs.size();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        blogCountsByRegion.forEach((region, count) -> {
            double percentage = (total > 0) ? (count / total) * 100 : 0;
            String label = String.format("%s (%.1f%%)", region, percentage);
            pieData.add(new PieChart.Data(label, count));
        });
        articlesRegionChart.setData(pieData);
    }

    private HBox createSmallArticleRow(Blog b) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 15, 10, 15));
        row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12;");
        Label title = new Label(b.getTitre());
        title.setStyle("-fx-font-weight: 800; -fx-font-size: 13px; -fx-text-fill: #1E293B;");
        title.setMaxWidth(250);
        row.getChildren().addAll(title);
        return row;
    }

    // ========== USERS CRUD ==========

    private Utilisateur selectedUser = null;
    private Profil selectedProfile = null;

    private void loadUsers() {
        try {
            List<Utilisateur> users = utilisateurCRUD.afficher();
            renderUserCards(users);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void renderUserCards(List<Utilisateur> users) {
        usersCardsContainer.getChildren().clear();
        for (Utilisateur u : users) {
            HBox card = new HBox(20);
            card.getStyleClass().add("card-creative");
            card.setAlignment(Pos.CENTER_LEFT);
            card.setCursor(Cursor.HAND);
            
            VBox info = new VBox(2);
            Label name = new Label(u.getNom() + " " + u.getPrenom());
            name.setStyle("-fx-font-weight: 900; -fx-font-size: 15px; -fx-text-fill: #1E293B;");
            Label email = new Label(u.getEmail());
            email.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
            info.getChildren().addAll(name, email);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label roleBadge = new Label(u.getType().toUpperCase());
            roleBadge.setPadding(new Insets(4, 12, 4, 12));
            roleBadge.setStyle("-fx-background-radius: 10; -fx-font-weight: 900; -fx-font-size: 10px;");
            if ("admin".equalsIgnoreCase(u.getType())) {
                roleBadge.setStyle(roleBadge.getStyle() + "-fx-background-color: #6366F115; -fx-text-fill: #6366F1;");
            } else {
                roleBadge.setStyle(roleBadge.getStyle() + "-fx-background-color: #10B98115; -fx-text-fill: #10B981;");
            }

            Label agePhone = new Label(u.getAge() + " ans • " + u.getTelephone());
            agePhone.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B; -fx-font-weight: 600;");

            card.getChildren().addAll(info, agePhone, roleBadge);
            
            card.setOnMouseClicked(e -> {
                selectedUser = u;
                editUserName.setText(u.getNom());
                editUserPrenom.setText(u.getPrenom());
                editUserType.setValue(u.getType());
                userEditBar.setVisible(true);
                // Highlight selection
                usersCardsContainer.getChildren().forEach(c -> c.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #F1F5F9; -fx-border-width: 1;"));
                card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 15; -fx-border-color: #6366F1; -fx-border-width: 1.5;");
            });

            card.setOnMouseEntered(e -> { if (selectedUser != u) card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 15; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-cursor: hand;"); });
            card.setOnMouseExited(e -> { if (selectedUser != u) card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #F1F5F9; -fx-border-width: 1;"); });

            usersCardsContainer.getChildren().add(card);
        }
    }

    private void loadProfiles() {
        try {
            List<Profil> profiles = profilCRUD.afficher();
            renderProfileCards(profiles);
        } catch (Exception e) {}
    }

    private void renderProfileCards(List<Profil> profiles) {
        profilesCardsContainer.getChildren().clear();
        for (Profil p : profiles) {
            HBox card = new HBox(20);
            card.getStyleClass().add("card-creative");
            card.setAlignment(Pos.CENTER_LEFT);
            card.setCursor(Cursor.HAND);

            VBox info = new VBox(2);
            Label uid = new Label("User ID: " + p.getUtilisateurId());
            uid.setStyle("-fx-font-weight: 800; -fx-font-size: 14px; -fx-text-fill: #1E293B;");
            Label bio = new Label(p.getBio().length() > 60 ? p.getBio().substring(0, 57) + "..." : p.getBio());
            bio.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
            info.getChildren().addAll(uid, bio);
            HBox.setHgrow(info, Priority.ALWAYS);

            card.getChildren().add(info);

            card.setOnMouseClicked(e -> {
                selectedProfile = p;
                editProfBio.setText(p.getBio());
                profileEditBar.setVisible(true);
                profilesCardsContainer.getChildren().forEach(c -> c.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #F1F5F9; -fx-border-width: 1;"));
                card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 15; -fx-border-color: #6366F1; -fx-border-width: 1.5;");
            });

            card.setOnMouseEntered(e -> { if (selectedProfile != p) card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 15; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-cursor: hand;"); });
            card.setOnMouseExited(e -> { if (selectedProfile != p) card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #F1F5F9; -fx-border-width: 1;"); });

            profilesCardsContainer.getChildren().add(card);
        }
    }

    @FXML private void updateUser() {
        if (selectedUser == null) return;
        try {
            selectedUser.setNom(editUserName.getText());
            selectedUser.setPrenom(editUserPrenom.getText());
            selectedUser.setType(editUserType.getValue());
            utilisateurCRUD.modifier(selectedUser);
            loadUsers();
        } catch (Exception e) {}
    }

    @FXML private void deleteUser() {
        if (selectedUser == null) return;
        try { utilisateurCRUD.supprimer(selectedUser.getId()); selectedUser = null; userEditBar.setVisible(false); loadUsers(); loadData(); } catch (Exception e) {}
    }

    @FXML private void updateProfile() {
        if (selectedProfile == null) return;
        try { selectedProfile.setBio(editProfBio.getText()); profilCRUD.modifier(selectedProfile); loadProfiles(); } catch (Exception e) {}
    }

    @FXML private void deleteProfile() {
        if (selectedProfile == null) return;
        try { profilCRUD.supprimer(selectedProfile.getId()); selectedProfile = null; profileEditBar.setVisible(false); loadProfiles(); } catch (Exception e) {}
    }

    @FXML private void openAddUserModal() { addUserOverlay.setVisible(true); addErrorMessage.setText(""); }
    @FXML private void hideAddUserForm() { addUserOverlay.setVisible(false); }

    @FXML private void addUser() {
        if (newNomField.getText().isEmpty() || newEmailField.getText().isEmpty()) { addErrorMessage.setText("Champs requis!"); return; }
        try {
            Utilisateur u = new Utilisateur();
            u.setNom(newNomField.getText()); u.setPrenom(newPrenomField.getText());
            u.setEmail(newEmailField.getText()); u.setTelephone(newTelephoneField.getText());
            u.setAge(Integer.parseInt(newAgeField.getText())); u.setMotDePasse(newPasswordField.getText());
            u.setType(newTypeCombo.getValue()); u.setVerified(true);
            utilisateurCRUD.ajouter(u); loadUsers(); hideAddUserForm(); loadData();
        } catch (Exception e) { addErrorMessage.setText("Erreur: " + e.getMessage()); }
    }

    // ========== BLOGS CRUD ==========

    private void renderArticlesList(List<Blog> blogs) {
        articlesContainer.getChildren().clear();
        for (Blog b : blogs) articlesContainer.getChildren().add(createArticleRow(b));
    }

    private Node createArticleRow(Blog b) {
        HBox row = new HBox(20);
        row.getStyleClass().add("card-creative");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(Cursor.HAND);
        row.setMinWidth(700); // Ensure it doesn't compress too much
        
        // Thumbnail
        StackPane imgWrapper = new StackPane();
        ImageView iv = new ImageView(); iv.setFitWidth(130); iv.setFitHeight(90); 
        Image img = loadImage(b.getImage()); iv.setImage(img != null ? img : new Image("testt.png"));
        Rectangle clip = new Rectangle(130, 90); clip.setArcWidth(15); clip.setArcHeight(15);
        iv.setClip(clip);
        imgWrapper.getChildren().add(iv);
        
        // Content Area
        VBox info = new VBox(8); 
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label catBadge = new Label(b.getCategorie().toUpperCase());
        catBadge.setStyle("-fx-background-color: #6366F115; -fx-text-fill: #6366F1; -fx-font-size: 10px; -fx-font-weight: 900; -fx-padding: 4 10; -fx-background-radius: 6;");
        
        Label regBadge = new Label(b.getRegion());
        regBadge.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-font-size: 10px; -fx-font-weight: 800; -fx-padding: 4 10; -fx-background-radius: 6;");
        
        String dateStr = b.getDatePublication() != null ? b.getDatePublication().format(dateFormatter) : "---";
        Label dateLabel = new Label("📅 " + dateStr);
        dateLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-font-weight: 600;");
        
        topRow.getChildren().addAll(catBadge, regBadge, new Region(){{HBox.setHgrow(this, Priority.ALWAYS);}}, dateLabel);

        Label title = new Label(b.getTitre());
        title.setStyle("-fx-text-fill: #1E293B; -fx-font-size: 20px; -fx-font-weight: 900;");
        
        String snippetText = b.getContenu();
        if (snippetText != null && snippetText.length() > 100) snippetText = snippetText.substring(0, 97) + "...";
        Label snippet = new Label(snippetText);
        snippet.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px; -fx-line-spacing: 1.4;");
        snippet.setWrapText(true);
        
        info.getChildren().addAll(topRow, title, snippet);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Actions
        VBox actions = new VBox(8);
        actions.setAlignment(Pos.CENTER);
        
        Button edit = new Button("Modifier"); 
        edit.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 15;");
        edit.setOnAction(e -> showBlogForm(b));
        
        Button del = new Button("Supprimer"); 
        del.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 15;");
        del.setOnAction(e -> confirmDelete(b));

        actions.getChildren().addAll(edit, del);

        row.getChildren().addAll(imgWrapper, info, actions);
        
        // Hover Magic
        row.setOnMouseEntered(e -> {
            row.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-color: #6366F1; -fx-border-width: 2; -fx-cursor: hand;");
            row.setEffect(new DropShadow(20, 0, 8, Color.rgb(99,102,241,0.12)));
        });
        row.setOnMouseExited(e -> {
            row.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #F1F5F9; -fx-border-width: 1.5;");
            row.setEffect(null);
        });
        
        return row;
    }

    @FXML private void openAddBlogModal() { showBlogForm(null); }
    
    private void showBlogForm(Blog blog) {
        editingBlog = blog;
        if (blog == null) {
            formTitle.setText("Ajouter un blog"); formTitre.clear(); formContenu.clear(); formImage.clear();
        } else {
            formTitle.setText("Modifier un blog"); formTitre.setText(blog.getTitre()); formContenu.setText(blog.getContenu()); formImage.setText(blog.getImage()); formRegion.setValue(blog.getRegion()); formCategorie.setValue(blog.getCategorie());
        }
        showView(viewBlogForm);
    }

    @FXML private void saveBlog() {
        if (formTitre.getText().isEmpty()) return;
        try {
            if (editingBlog == null) {
                blogCRUD.ajouter(new Blog(formTitre.getText(), formContenu.getText(), Session.getCurrentUser().getId(), formImage.getText(), formRegion.getValue(), formCategorie.getValue()));
            } else {
                editingBlog.setTitre(formTitre.getText()); editingBlog.setContenu(formContenu.getText()); editingBlog.setImage(formImage.getText()); editingBlog.setRegion(formRegion.getValue()); editingBlog.setCategorie(formCategorie.getValue());
                blogCRUD.modifier(editingBlog);
            }
            loadData(); switchToBlogs();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void cancelBlogForm() { switchToBlogs(); }

    private void confirmDelete(Blog b) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer '" + b.getTitre() + "' ?", ButtonType.YES, ButtonType.NO);
        if (a.showAndWait().get() == ButtonType.YES) {
            try { blogCRUD.supprimer(b.getId()); loadData(); renderArticlesList(allBlogs); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ========== COMMENTS AND HELPERS ==========

    private void loadGlobalComments() {
        try {
            allComments = commentaireCRUD.afficher();
            renderCommentsList(allComments);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void renderCommentsList(List<Commentaire> comments) {
        globalCommentsContainer.getChildren().clear();
        for (Commentaire c : comments) {
            HBox row = new HBox(20);
            row.getStyleClass().add("card-creative");
            row.setAlignment(Pos.CENTER_LEFT);
            row.setCursor(Cursor.HAND);
            
            StackPane avatar = new StackPane();
            avatar.setPrefSize(45, 45);
            avatar.setStyle("-fx-background-color: #6366F115; -fx-background-radius: 50;");
            
            Label initial = new Label(c.getUtilisateurNom().substring(0, 1).toUpperCase());
            initial.setStyle("-fx-text-fill: #6366F1; -fx-font-size: 16px; -fx-font-weight: 900;");
            avatar.getChildren().add(initial);

            VBox content = new VBox(4);
            HBox authorLine = new HBox(10);
            authorLine.setAlignment(Pos.CENTER_LEFT);
            
            Label author = new Label(c.getUtilisateurNom());
            author.setStyle("-fx-text-fill: #1E293B; -fx-font-weight: 900; -fx-font-size: 15px;");
            
            Label date = new Label("• Juste maintenant");
            date.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-font-weight: 500;");
            
            authorLine.getChildren().addAll(author, date);
            
            Label msg = new Label(c.getContenu());
            msg.setWrapText(true);
            msg.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px; -fx-line-spacing: 5;");
            
            content.getChildren().addAll(authorLine, msg);
            HBox.setHgrow(content, Priority.ALWAYS);

            Button del = new Button("🗑"); 
            del.setStyle("-fx-background-color: #EF444408; -fx-text-fill: #EF4444; -fx-background-radius: 12; -fx-padding: 10 12; -fx-cursor: hand; -fx-font-size: 16px;");
            del.setOnAction(e -> {
                try { commentaireCRUD.supprimer(c.getId()); loadGlobalComments(); loadData(); } catch (SQLException ex) { ex.printStackTrace(); }
            });
            
            row.getChildren().addAll(avatar, content, del); 
            globalCommentsContainer.getChildren().add(row);
        }
    }

    @FXML private void chooseImage() {
        FileChooser fc = new FileChooser();
        File f = fc.showOpenDialog(contentStack.getScene().getWindow());
        if (f != null) formImage.setText(f.getAbsolutePath());
    }

    private Image loadImage(String path) {
        if (path == null || path.isEmpty()) return null;
        try {
            if (path.startsWith("http") || path.startsWith("file:")) return new Image(path, true);
            File f = new File(path); if (f.exists()) return new Image(f.toURI().toString(), true);
            InputStream is = getClass().getResourceAsStream("/" + (path.startsWith("/") ? path.substring(1) : path));
            if (is != null) return new Image(is);
        } catch (Exception e) {}
        return null;
    }

    @FXML private void handleLogout(ActionEvent event) {
        Session.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) contentStack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
