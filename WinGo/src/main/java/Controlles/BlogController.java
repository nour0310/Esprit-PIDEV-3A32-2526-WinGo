package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Entites.Favori;
import Entites.Like;
import Entites.Notification;
import Entites.Rating;
import Entites.Tag;
import Entites.Utilisateur;
import Services.BlogCRUD;
import Services.CommentaireCRUD;
import Services.FavoriCRUD;
import Services.LikeCRUD;
import Services.NotificationCRUD;
import Services.RatingCRUD;
import Services.TagCRUD;
import Services.UtilisateurCRUD;
import Services.External.MyMemoryService;
import Services.External.OpenWeatherService;
import Services.External.GoogleTTSService;
import Services.External.HuggingFaceSummaryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Popup;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlogController implements Initializable {

    // Services
    private final BlogCRUD blogCRUD = new BlogCRUD();
    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private final UtilisateurCRUD utilisateurCRUD = new UtilisateurCRUD();
    private final LikeCRUD likeCRUD = new LikeCRUD();
    private final RatingCRUD ratingCRUD = new RatingCRUD();
    private final TagCRUD tagCRUD = new TagCRUD();
    private final NotificationCRUD notificationCRUD = new NotificationCRUD();
    private final FavoriCRUD favoriCRUD = new FavoriCRUD();

    // Données observables
    private ObservableList<Blog> blogList = FXCollections.observableArrayList();
    private ObservableList<Commentaire> commentaireList = FXCollections.observableArrayList();
    private Blog selectedBlog = null;
    private Blog displayedDetailBlog = null;

    // Utilisateur connecté
    private Utilisateur currentUser;

    // Données pour les likes
    private Map<Integer, Integer> likeCounts = new HashMap<>();
    private Set<Integer> likedByCurrentUser = new HashSet<>();

    // Données pour les notes (étoiles)
    private Map<Integer, Double> ratingAverages = new HashMap<>();
    private Map<Integer, Integer> voteCounts = new HashMap<>();
    private Map<Integer, Integer> userRatings = new HashMap<>();

    // Données pour les favoris
    private Set<Integer> favorisUtilisateur = new HashSet<>();
    private boolean modeFavoris = false;

    // Images pour les cœurs
    private Image heartEmptyImage;
    private Image heartFullImage;

    // Langue courante pour la synthèse vocale (défaut français)
    private String currentTTSLang = "fr";

    // Composants FXML de la vue liste
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Label totalBlogsLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private FlowPane articlesFlowPane;
    @FXML private Label selectedArticleLabel;
    @FXML private Label articleIdLabel;
    @FXML private TextField titreField;
    @FXML private TextArea contenuField;
    @FXML private TextField imageField;
    @FXML private ComboBox<String> regionField;
    @FXML private ComboBox<String> categorieField;
    @FXML private TextField tagsField;
    @FXML private Label auteurLabel;
    @FXML private Button choisirImageBtn;
    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Label statusLabel;

    // Labels d'erreur pour la validation
    @FXML private Label titreError;
    @FXML private Label contenuError;
    @FXML private Label regionError;
    @FXML private Label categorieError;
    @FXML private Label imageError;

    // Composants FXML de la vue détail
    @FXML private VBox listView;
    @FXML private VBox detailView;
    @FXML private Button backToListBtn;
    @FXML private StackPane detailImageContainer;
    @FXML private ImageView detailImageView;
    @FXML private Label detailTitreLabel;
    @FXML private Label detailAuteurLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailContenuLabel;
    @FXML private VBox detailCommentairesPane;
    @FXML private TextField detailNewCommentField;
    @FXML private Button detailAddCommentBtn;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailConnectedUserLabel;
    @FXML private Button detailLikeButton;
    @FXML private ImageView detailLikeImageView;
    @FXML private Label detailLikeCountLabel;
    @FXML private HBox detailStarsBox;
    @FXML private Label detailAvgLabel;
    @FXML private HBox detailShareBox;
    @FXML private Button detailFavButton;

    // Composants pour la météo
    @FXML private ImageView detailMeteoIcon;
    @FXML private Label detailMeteoLabel;

    // Composants pour la traduction
    @FXML private ComboBox<String> langueCombo;
    @FXML private Button traduireBtn;

    // Composant pour la synthèse vocale
    @FXML private Button ecouterBtn;

    // Bouton pour le résumé
    @FXML private Button resumerBtn;

    // Notifications
    @FXML private Button notificationButton;
    @FXML private Label notificationBadge;

    // Filtres
    @FXML private ComboBox<String> regionFilterCombo;
    @FXML private ComboBox<String> categorieFilterCombo;

    // ScrollPanes
    @FXML private ScrollPane listViewScroll;
    @FXML private ScrollPane detailViewScroll;
    @FXML private ScrollPane articleFormScroll;

    // Composants pour la section Favoris (dans la vue liste)
    @FXML private VBox favorisSection;
    @FXML private VBox favorisContainer;

    // Composants pour le résumé réactif
    @FXML private VBox resumeContainer;
    @FXML private TextArea resumeTextArea;
    @FXML private Label resumeStatusLabel;

    // Composants pour la sidebar enrichie
    @FXML private StackPane rootPane;
    @FXML private Button darkModeBtn;
    @FXML private Label sidebarUserName;
    @FXML private Pane backgroundPane;

    // Bouton retour du formulaire
    @FXML private Button backFromFormBtn;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateShortFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Patterns de validation
    private static final Pattern TITLE_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s\\-']{3,50}$");
    private static final Pattern CONTENT_PATTERN = Pattern.compile("^[\\w\\s\\p{Punct}À-ÿ]{10,}$");

    // Popup pour l'auto-complétion des mentions
    private Popup suggestionsPopup;
    private ListView<Utilisateur> suggestionsListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadImages();
        initComboBoxes();
        initLangues();
        loadUtilisateurs();
        attachListeners();
        setupValidationListeners();
        loadInitialData();
        showListView();
        updateFormButtons();
        clearAllErrors();
        setupMentionAutoComplete();

        // Fix notification button icon (emoji may not render from FXML on all platforms)
        notificationButton.setText("\uD83D\uDD14");
        notificationButton.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-text-fill: #1E293B;" +
            "-fx-background-radius: 50;" +
            "-fx-font-size: 20px;" +
            "-fx-padding: 8 14;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0.4, 0, 3);"
        );
        loadNotifications();

        // Initialiser la vue formulaire
        articleFormScroll.setVisible(false);
        articleFormScroll.setManaged(false);
        backFromFormBtn.setOnAction(e -> showListView());
    }

    private void loadImages() {
        String emptyPath = "/images/heart.png";
        String fullPath = "/images/heartRed.png";

        System.out.println("Chargement de " + emptyPath + " : " + getClass().getResource(emptyPath));
        System.out.println("Chargement de " + fullPath + " : " + getClass().getResource(fullPath));

        try (InputStream emptyStream = getClass().getResourceAsStream(emptyPath);
             InputStream fullStream = getClass().getResourceAsStream(fullPath)) {
            if (emptyStream == null || fullStream == null) {
                System.err.println(" Images non trouvées. Utilisation des émojis en fallback.");
                heartEmptyImage = null;
                heartFullImage = null;
            } else {
                heartEmptyImage = new Image(emptyStream);
                heartFullImage = new Image(fullStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
            heartEmptyImage = null;
            heartFullImage = null;
        }
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

    private void initLangues() {
        ObservableList<String> langues = FXCollections.observableArrayList(
                "Anglais", "Espagnol", "Allemand", "Arabe", "Italien", "Portugais"
        );
        langueCombo.setItems(langues);
        langueCombo.setValue("Anglais");
    }

    private void loadUtilisateurs() {
        try {
            ObservableList<Utilisateur> users = FXCollections.observableArrayList(utilisateurCRUD.afficher());
            currentUser = users.stream().filter(u -> u.getId() == 2).findFirst().orElse(null);
            if (currentUser != null) {
                String nomComplet = currentUser.getPrenom() + " " + currentUser.getNom();
                auteurLabel.setText(nomComplet);
                // Avatar bubble: show initials
                String initials = "";
                if (currentUser.getPrenom() != null && !currentUser.getPrenom().isEmpty())
                    initials += currentUser.getPrenom().charAt(0);
                if (currentUser.getNom() != null && !currentUser.getNom().isEmpty())
                    initials += currentUser.getNom().charAt(0);
                detailConnectedUserLabel.setText(initials.toUpperCase());
                sidebarUserName.setText(nomComplet);
                loadNotifications();
                loadFavoris();
            } else {
                auteurLabel.setText("Utilisateur inconnu");
                detailConnectedUserLabel.setText("?");
                sidebarUserName.setText("Invité");
            }
        } catch (SQLException e) {
            showError("Erreur chargement utilisateurs", e.getMessage());
        }
    }

    private void loadNotifications() {
        if (currentUser == null) return;
        try {
            List<Notification> notifs = notificationCRUD.getNotificationsByUser(currentUser.getId());
            long nonLues = notifs.stream().filter(n -> !n.isLu()).count();
            if (nonLues > 0) {
                notificationBadge.setText(String.valueOf(nonLues));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setVisible(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFavoris() throws SQLException {
        if (currentUser == null) return;
        favorisUtilisateur.clear();
        List<Integer> ids = favoriCRUD.getFavorisByUser(currentUser.getId());
        favorisUtilisateur.addAll(ids);
        System.out.println("Favoris chargés : " + favorisUtilisateur.size());
    }

    private void attachListeners() {
        searchBtn.setOnAction(e -> filterArticles());
        searchField.setOnAction(e -> filterArticles());
        clearBtn.setOnAction(e -> clearForm());
        choisirImageBtn.setOnAction(e -> choisirImage());
        if (backToListBtn != null) {
            backToListBtn.setOnAction(e -> showListView());
        }
        detailAddCommentBtn.setOnAction(e -> ajouterCommentaireDetail());
        notificationButton.setOnAction(e -> afficherNotifications());
        traduireBtn.setOnAction(e -> traduireArticle());
        ecouterBtn.setOnAction(e -> ecouterArticle());
        resumerBtn.setOnAction(e -> resumerArticle());
    }

    private void afficherNotifications() {
        try {
            List<Notification> notifs = notificationCRUD.getNotificationsByUser(currentUser.getId());
            long nonLues = notifs.stream().filter(n -> !n.isLu()).count();
            Popup notifPopup = new Popup();

            // ── Outer container ──────────────────────────────────────────────────
            VBox content = new VBox(0);
            content.setMinWidth(370);
            content.setMaxWidth(370);
            content.setStyle(
                    "-fx-background-color: #FFFFFF;" +
                    "-fx-background-radius: 24;" +
                    "-fx-effect: dropshadow(gaussian, rgba(60,60,120,0.22), 28, 0.5, 0, 8);"
            );

            // ── Gradient header ──────────────────────────────────────────────────
            VBox header = new VBox(4);
            header.setPadding(new Insets(18, 22, 14, 22));
            header.setStyle(
                    "-fx-background-color: linear-gradient(to right, #6366F1, #A3B1FF);" +
                    "-fx-background-radius: 24 24 0 0;"
            );

            HBox headerRow = new HBox(10);
            headerRow.setAlignment(Pos.CENTER_LEFT);

            StackPane bellWrap = new StackPane();
            Label bellLbl = new Label("🔔");
            bellLbl.setStyle("-fx-font-size: 22px;");
            if (nonLues > 0) {
                Label badge = new Label(String.valueOf(nonLues));
                badge.setStyle(
                        "-fx-background-color: #EF4444;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 1 5;" +
                        "-fx-font-size: 9px;" +
                        "-fx-font-weight: bold;"
                );
                StackPane.setAlignment(badge, Pos.TOP_RIGHT);
                bellWrap.getChildren().addAll(bellLbl, badge);
            } else {
                bellWrap.getChildren().add(bellLbl);
            }

            VBox titleBlock = new VBox(2);
            Label titleLbl = new Label("Notifications");
            titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
            Label subLbl = new Label(nonLues > 0 ? nonLues + " non lue(s)" : "Tout est à jour ✓");
            subLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.75);");
            titleBlock.getChildren().addAll(titleLbl, subLbl);

            Region hSpacer = new Region();
            HBox.setHgrow(hSpacer, Priority.ALWAYS);

            Button closeBtn = new Button("✕");
            closeBtn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.18);" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 20;" +
                    "-fx-padding: 4 10;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-size: 13px;"
            );
            closeBtn.setOnAction(ev -> notifPopup.hide());

            headerRow.getChildren().addAll(bellWrap, titleBlock, hSpacer, closeBtn);
            header.getChildren().add(headerRow);

            // ── Body ─────────────────────────────────────────────────────────────
            VBox body = new VBox(0);
            body.setPadding(new Insets(10, 12, 10, 12));
            body.setStyle("-fx-background-color: #FAFAFE;");

            if (notifs.isEmpty()) {
                // Empty state
                VBox emptyState = new VBox(10);
                emptyState.setAlignment(Pos.CENTER);
                emptyState.setPadding(new Insets(30, 0, 30, 0));
                Label emptyIcon = new Label("🎉");
                emptyIcon.setStyle("-fx-font-size: 40px;");
                Label emptyLbl = new Label("Aucune notification");
                emptyLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");
                Label emptyHint = new Label("Vous êtes à jour !");
                emptyHint.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 12px;");
                emptyState.getChildren().addAll(emptyIcon, emptyLbl, emptyHint);
                body.getChildren().add(emptyState);
            } else {
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setFitToWidth(true);
                scrollPane.setPrefHeight(260);
                scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

                VBox notifList = new VBox(8);
                notifList.setPadding(new Insets(5, 4, 5, 4));

                for (Notification n : notifs) {
                    HBox card = new HBox(12);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setPadding(new Insets(11, 14, 11, 14));
                    boolean unread = !n.isLu();
                    card.setStyle(
                            "-fx-background-color: " + (unread ? "#EEF2FF" : "#FFFFFF") + ";" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-color: " + (unread ? "#C7D2FE" : "#E2E8F0") + ";" +
                            "-fx-border-radius: 14;" +
                            "-fx-border-width: 1.2;" +
                            "-fx-cursor: hand;"
                    );

                    // Type icon bubble
                    StackPane iconBubble = new StackPane();
                    iconBubble.setMinSize(38, 38);
                    iconBubble.setMaxSize(38, 38);
                    String bubbleColor, iconText;
                    if ("mention".equals(n.getType())) {
                        bubbleColor = "linear-gradient(to bottom right, #F472B6, #EC4899)";
                        iconText = "📢";
                    } else if ("reponse".equals(n.getType())) {
                        bubbleColor = "linear-gradient(to bottom right, #34D399, #10B981)";
                        iconText = "💬";
                    } else {
                        bubbleColor = "linear-gradient(to bottom right, #A78BFA, #7C3AED)";
                        iconText = "🔔";
                    }
                    iconBubble.setStyle(
                            "-fx-background-color: " + bubbleColor + ";" +
                            "-fx-background-radius: 19;"
                    );
                    Label iconLbl = new Label(iconText);
                    iconLbl.setStyle("-fx-font-size: 16px;");
                    iconBubble.getChildren().add(iconLbl);

                    // Text block
                    VBox textBlock = new VBox(3);
                    HBox.setHgrow(textBlock, Priority.ALWAYS);

                    Label msgLbl = new Label(n.getContenu());
                    msgLbl.setWrapText(true);
                    msgLbl.setMaxWidth(220);
                    msgLbl.setStyle(
                            "-fx-font-size: 13px;" +
                            "-fx-text-fill: #1E293B;" +
                            "-fx-font-weight: " + (unread ? "bold" : "normal") + ";"
                    );

                    // Time-ago label
                    java.time.LocalDateTime now2 = java.time.LocalDateTime.now();
                    long minutes = java.time.Duration.between(n.getDateCreation(), now2).toMinutes();
                    String timeAgo;
                    if (minutes < 1) timeAgo = "À l'instant";
                    else if (minutes < 60) timeAgo = minutes + " min";
                    else if (minutes < 1440) timeAgo = (minutes / 60) + " h";
                    else timeAgo = (minutes / 1440) + " j";

                    HBox metaRow = new HBox(6);
                    metaRow.setAlignment(Pos.CENTER_LEFT);
                    Label typeLbl = new Label(n.getType() != null ? n.getType().toUpperCase() : "INFO");
                    typeLbl.setStyle(
                            "-fx-background-color: " + (unread ? "#C7D2FE" : "#E2E8F0") + ";" +
                            "-fx-text-fill: " + (unread ? "#4F46E5" : "#64748B") + ";" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 1 6;" +
                            "-fx-font-size: 9px;" +
                            "-fx-font-weight: bold;"
                    );
                    Label timeLbl = new Label("· " + timeAgo);
                    timeLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
                    metaRow.getChildren().addAll(typeLbl, timeLbl);

                    if (unread) {
                        Label dotLbl = new Label("●");
                        dotLbl.setStyle("-fx-text-fill: #6366F1; -fx-font-size: 8px;");
                        metaRow.getChildren().add(dotLbl);
                    }
                    textBlock.getChildren().addAll(msgLbl, metaRow);

                    card.getChildren().addAll(iconBubble, textBlock);

                    // Add click listener to the card
                    card.setOnMouseClicked(ev -> {
                        try {
                            // Mark as read
                            if (!n.isLu()) {
                                notificationCRUD.marquerCommeLu(n.getId());
                            }
                            // Close popup
                            notifPopup.hide();

                            // Parse lien to get blog ID (e.g. "/blogs/7")
                            if (n.getLien() != null && n.getLien().startsWith("/blogs/")) {
                                try {
                                    int blogId = Integer.parseInt(n.getLien().substring(7));
                                    Blog targetBlog = blogList.stream()
                                            .filter(b -> b.getId() == blogId)
                                            .findFirst()
                                            .orElse(null);
                                    if (targetBlog != null) {
                                        showDetailView(targetBlog);
                                    } else {
                                        System.out.println("Blog non trouvé pour ID: " + blogId);
                                    }
                                } catch (NumberFormatException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            // Reload notifications to update badge and list
                            loadNotifications();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });

                    notifList.getChildren().add(card);
                }

                scrollPane.setContent(notifList);
                body.getChildren().add(scrollPane);
            }

            // ── Footer ───────────────────────────────────────────────────────────
            HBox footer = new HBox(10);
            footer.setAlignment(Pos.CENTER);
            footer.setPadding(new Insets(12, 18, 16, 18));
            footer.setStyle(
                    "-fx-background-color: #FFFFFF;" +
                    "-fx-background-radius: 0 0 24 24;" +
                    "-fx-border-color: #E2E8F0;" +
                    "-fx-border-width: 1 0 0 0;"
            );

            Button marquerLu = new Button("✓  Tout marquer lu");
            marquerLu.setStyle(
                    "-fx-background-color: linear-gradient(to right, #6366F1, #A3B1FF);" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 30;" +
                    "-fx-padding: 9 22;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-size: 13px;"
            );
            marquerLu.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(marquerLu, Priority.ALWAYS);
            marquerLu.setOnAction(ev -> {
                try {
                    for (Notification n : notifs) {
                        notificationCRUD.marquerCommeLu(n.getId());
                    }
                    loadNotifications();
                    notifPopup.hide();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            footer.getChildren().add(marquerLu);

            content.getChildren().addAll(header, body, footer);
            notifPopup.getContent().add(content);
            notifPopup.setAutoHide(true);
            double x = notificationButton.localToScreen(0, 0).getX() - 370 + notificationButton.getWidth();
            double y = notificationButton.localToScreen(0, notificationButton.getHeight()).getY() + 6;
            notifPopup.show(notificationButton, x, y);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void afficherFavoris() {
        modeFavoris = !modeFavoris;
        filterArticles();
    }

    // ========== TRADUCTION ==========
    private void traduireArticle() {
        if (displayedDetailBlog == null) {
            detailStatusLabel.setText("❌ Aucun article sélectionné");
            return;
        }

        String langueChoisie = langueCombo.getValue();
        if (langueChoisie == null) return;

        String codeLangue = switch (langueChoisie) {
            case "Anglais" -> "en";
            case "Espagnol" -> "es";
            case "Allemand" -> "de";
            case "Arabe" -> "ar";
            case "Italien" -> "it";
            case "Portugais" -> "pt";
            default -> "en";
        };

        String texteOriginal = displayedDetailBlog.getContenu();
        String titreOriginal = displayedDetailBlog.getTitre();

        detailStatusLabel.setText("⏳ Traduction en cours...");

        MyMemoryService.translateAsync(texteOriginal, "fr", codeLangue)
                .thenAccept(texteTraduit -> {
                    javafx.application.Platform.runLater(() -> {
                        detailContenuLabel.setText(texteTraduit);
                        detailStatusLabel.setText("✅ Traduit en " + langueChoisie);
                        currentTTSLang = codeLangue;
                    });
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> detailStatusLabel.setText("❌ Erreur de traduction"));
                    return null;
                });

        MyMemoryService.translateAsync(titreOriginal, "fr", codeLangue)
                .thenAccept(titreTraduit -> javafx.application.Platform.runLater(() -> detailTitreLabel.setText(titreTraduit)));
    }

    // ========== SYNTHÈSE VOCALE ==========
    private void ecouterArticle() {
        if (displayedDetailBlog == null) {
            detailStatusLabel.setText("❌ Aucun article sélectionné");
            return;
        }
        String texte = detailContenuLabel.getText();
        if (texte == null || texte.trim().isEmpty()) return;

        ecouterBtn.setDisable(true);
        ecouterBtn.setText("⏳ Génération...");
        detailStatusLabel.setText("⏳ Génération audio...");

        GoogleTTSService.generateSpeechAsync(texte, currentTTSLang)
                .thenAccept(audioData -> {
                    javafx.application.Platform.runLater(() -> {
                        ecouterBtn.setDisable(false);
                        ecouterBtn.setText("🔊 Écouter");
                        if (audioData != null) {
                            GoogleTTSService.playAudio(audioData);
                            detailStatusLabel.setText("✅ Lecture en cours...");
                        } else {
                            detailStatusLabel.setText("❌ Erreur de génération audio");
                        }
                    });
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> {
                        ecouterBtn.setDisable(false);
                        ecouterBtn.setText("🔊 Écouter");
                        detailStatusLabel.setText("❌ Erreur : " + ex.getMessage());
                        ex.printStackTrace();
                    });
                    return null;
                });
    }

    // ========== RÉSUMÉ AUTOMATIQUE RÉACTIF ==========
    private void resumerArticle() {
        if (displayedDetailBlog == null) {
            detailStatusLabel.setText("❌ Aucun article sélectionné");
            return;
        }
        String texte = detailContenuLabel.getText();
        if (texte == null || texte.trim().isEmpty()) return;

        resumeContainer.setVisible(true);
        resumeContainer.setManaged(true);
        resumeStatusLabel.setText("⏳ Génération du résumé...");
        resumeTextArea.clear();

        resumerBtn.setDisable(true);
        resumerBtn.setText("⏳ Résumé...");
        detailStatusLabel.setText("⏳ Génération du résumé...");

        HuggingFaceSummaryService.summarizeAsync(texte)
                .thenAccept(result -> {
                    javafx.application.Platform.runLater(() -> {
                        resumeStatusLabel.setText("");
                        if (result != null && !result.isEmpty()) {
                            resumeTextArea.setText(result);
                            detailStatusLabel.setText("✅ Résumé généré.");
                        } else {
                            resumeTextArea.setText("Impossible de générer un résumé.");
                            detailStatusLabel.setText("❌ Erreur de génération.");
                        }
                        resumerBtn.setDisable(false);
                        resumerBtn.setText("📝 Résumé");
                    });
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> {
                        resumeStatusLabel.setText("❌ Erreur : " + ex.getMessage());
                        detailStatusLabel.setText("❌ Erreur de génération.");
                        resumerBtn.setDisable(false);
                        resumerBtn.setText("📝 Résumé");
                        ex.printStackTrace();
                    });
                    return null;
                });
    }

    // ========== VALIDATION ==========
    private void setupValidationListeners() {
        titreField.textProperty().addListener((obs, oldVal, newVal) -> validateTitre());
        contenuField.textProperty().addListener((obs, oldVal, newVal) -> validateContenu());
        regionField.valueProperty().addListener((obs, oldVal, newVal) -> validateRegion());
        categorieField.valueProperty().addListener((obs, oldVal, newVal) -> validateCategorie());
        imageField.textProperty().addListener((obs, oldVal, newVal) -> validateImage());
    }

    private boolean validateTitre() {
        String titre = titreField.getText();
        if (titre == null || titre.trim().isEmpty()) {
            showError(titreError, "Le titre ne peut pas être vide.");
            return false;
        } else if (!TITLE_PATTERN.matcher(titre).matches()) {
            showError(titreError, "Le titre doit contenir uniquement des lettres, espaces, tirets ou apostrophes (3-50 caractères).");
            return false;
        } else {
            clearError(titreError);
            return true;
        }
    }

    private boolean validateContenu() {
        String contenu = contenuField.getText();
        if (contenu == null || contenu.trim().isEmpty()) {
            showError(contenuError, "Le contenu ne peut pas être vide.");
            return false;
        } else if (!CONTENT_PATTERN.matcher(contenu).matches()) {
            showError(contenuError, "Le contenu doit contenir au moins 10 caractères.");
            return false;
        } else {
            clearError(contenuError);
            return true;
        }
    }

    private boolean validateRegion() {
        if (regionField.getValue() == null || regionField.getValue().isEmpty()) {
            showError(regionError, "Veuillez sélectionner une région.");
            return false;
        } else {
            clearError(regionError);
            return true;
        }
    }

    private boolean validateCategorie() {
        if (categorieField.getValue() == null || categorieField.getValue().isEmpty()) {
            showError(categorieError, "Veuillez sélectionner une catégorie.");
            return false;
        } else {
            clearError(categorieError);
            return true;
        }
    }

    private boolean validateImage() {
        String imagePath = imageField.getText();
        if (imagePath == null || imagePath.trim().isEmpty()) {
            showError(imageError, "L'image est obligatoire.");
            return false;
        } else {
            File f = new File(imagePath);
            if (!f.exists()) {
                showError(imageError, "Le fichier image n'existe pas.");
                return false;
            } else {
                clearError(imageError);
                return true;
            }
        }
    }

    private boolean validateBlogForm() {
        boolean valid = true;
        valid &= validateTitre();
        valid &= validateContenu();
        valid &= validateRegion();
        valid &= validateCategorie();
        valid &= validateImage();
        return valid;
    }

    private void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText("⚠ " + message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void clearError(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void clearAllErrors() {
        clearError(titreError);
        clearError(contenuError);
        clearError(regionError);
        clearError(categorieError);
        clearError(imageError);
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
            loadAllComments();
            loadLikes();
            loadRatings();
            loadFavoris();
            loadBlogs();
            updateStats();
            statusLabel.setText("✅ Prêt, " + blogList.size() + " articles chargés.");
        } catch (SQLException e) {
            showError("Erreur de chargement", e.getMessage());
        }
    }

    private void loadBlogs() throws SQLException {
        blogList.clear();
        List<Blog> blogs = blogCRUD.afficher();
        for (Blog b : blogs) {
            b.setTags(tagCRUD.getTagsByArticle(b.getId()));
        }
        blogList.addAll(blogs);
        displayBlogs(blogList);
        totalBlogsLabel.setText(String.valueOf(blogList.size()));
    }

    private void loadAllComments() throws SQLException {
        commentaireList.clear();
        commentaireList.addAll(commentaireCRUD.afficher());
        totalCommentsLabel.setText(String.valueOf(commentaireList.size()));
    }

    private void loadLikes() throws SQLException {
        List<Like> allLikes = likeCRUD.afficherTous();
        likeCounts.clear();
        likedByCurrentUser.clear();
        for (Like l : allLikes) {
            likeCounts.merge(l.getArticleId(), 1, Integer::sum);
            if (currentUser != null && l.getUtilisateurId() == currentUser.getId()) {
                likedByCurrentUser.add(l.getArticleId());
            }
        }
    }

    private void loadRatings() throws SQLException {
        List<Rating> allRatings = ratingCRUD.afficherTous();
        ratingAverages.clear();
        voteCounts.clear();
        userRatings.clear();

        Map<Integer, List<Integer>> votes = new HashMap<>();
        for (Rating r : allRatings) {
            votes.computeIfAbsent(r.getArticleId(), k -> new ArrayList<>()).add(r.getNote());
            if (currentUser != null && r.getUtilisateurId() == currentUser.getId()) {
                userRatings.put(r.getArticleId(), r.getNote());
            }
        }
        for (Map.Entry<Integer, List<Integer>> entry : votes.entrySet()) {
            double avg = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0);
            ratingAverages.put(entry.getKey(), avg);
            voteCounts.put(entry.getKey(), entry.getValue().size());
        }
    }

    private void displayBlogs(List<Blog> blogs) {
        articlesFlowPane.getChildren().clear();
        for (Blog b : blogs) {
            articlesFlowPane.getChildren().add(createBlogCard(b));
        }

        afficherSectionFavoris();
    }

    private void afficherSectionFavoris() {
        if (currentUser == null || favorisUtilisateur.isEmpty()) {
            favorisSection.setVisible(false);
            favorisSection.setManaged(false);
            return;
        }
        
        // Hide favorites section if we're filtering by something that doesn't include them, 
        // to avoid visual clutter, but normally we just show the ones that match or all of them.
        // For distinctness, let's always show 'favorisUtilisateur' from the full list.
        List<Blog> likedBlogs = blogList.stream()
                .filter(b -> favorisUtilisateur.contains(b.getId()))
                .collect(Collectors.toList());

        if (likedBlogs.isEmpty()) {
            favorisSection.setVisible(false);
            favorisSection.setManaged(false);
            return;
        }

        favorisSection.setVisible(true);
        favorisSection.setManaged(true);
        favorisContainer.getChildren().clear();

        for (Blog b : likedBlogs) {
            VBox miniCard = createMiniBlogCard(b);
            favorisContainer.getChildren().add(miniCard);
        }
    }

    // ==================== MINI CARTE ARTICLE (POUR FAVORIS) ====================
    private VBox createMiniBlogCard(Blog blog) {
        VBox card = new VBox();
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-radius: 12;" +
                "-fx-cursor: hand;"
        );
        card.setPrefWidth(200);
        card.setMaxWidth(200);
        card.setPadding(Insets.EMPTY);

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(110);
        imageContainer.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 12 12 0 0;");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imageContainer.widthProperty());
        clip.setHeight(110);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        imageContainer.setClip(clip);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.fitWidthProperty().bind(imageContainer.widthProperty());
        imageView.setFitHeight(110);

        Image img = loadImage(blog.getImage());
        if (img != null && !img.isError()) {
            imageView.setImage(img);
        } else {
            try {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                imageView.setImage(defaultImg);
            } catch (Exception ex) {}
        }
        imageContainer.getChildren().add(imageView);

        VBox body = new VBox(6);
        body.setPadding(new Insets(10));
        
        String titre = blog.getTitre() != null ? blog.getTitre() : "Sans titre";
        Label titleLbl = new Label(titre);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1E293B;");
        titleLbl.setMaxWidth(180);
        
        Label dateLbl = new Label(blog.getDatePublication() != null ? blog.getDatePublication().format(dateShortFormatter) : "");
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        body.getChildren().addAll(titleLbl, dateLbl);
        card.getChildren().addAll(imageContainer, body);

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle().replace(
                    "-fx-border-color: #E2E8F0;",
                    "-fx-border-color: #6366F1;"));
            card.setScaleX(1.03);
            card.setScaleY(1.03);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace(
                    "-fx-border-color: #6366F1;",
                    "-fx-border-color: #E2E8F0;"));
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        
        card.setOnMouseClicked(e -> showDetailView(blog));

        return card;
    }

    // ==================== CARTE ARTICLE ====================
    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox();
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0.4, 0, 6);" +
                "-fx-cursor: hand;"
        );
        card.setPrefWidth(270);
        card.setMaxWidth(270);
        card.setPadding(Insets.EMPTY);

        // ── Image container ──────────────────────────────────────────────────
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(160);
        imageContainer.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 18 18 0 0;");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imageContainer.widthProperty());
        clip.setHeight(160);
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        imageContainer.setClip(clip);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.fitWidthProperty().bind(imageContainer.widthProperty());
        imageView.setFitHeight(160);

        Image img = loadImage(blog.getImage());
        if (img != null && !img.isError()) {
            imageView.setImage(img);
        } else {
            try {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                imageView.setImage(defaultImg);
            } catch (Exception ex) {}
        }
        imageContainer.getChildren().add(imageView);

        // Dark gradient overlay at bottom for title readability
        javafx.scene.shape.Rectangle gradient = new javafx.scene.shape.Rectangle();
        gradient.widthProperty().bind(imageContainer.widthProperty());
        gradient.setHeight(80);
        gradient.setStyle("-fx-fill: linear-gradient(to top, rgba(0,0,0,0.65), transparent);");
        StackPane.setAlignment(gradient, Pos.BOTTOM_CENTER);
        imageContainer.getChildren().add(gradient);

        // Title overlay
        String titre = blog.getTitre() != null ? blog.getTitre() : "Sans titre";
        Label titleOverlay = new Label(titre);
        titleOverlay.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-wrap-text: true;" +
                "-fx-padding: 0 12 10 12;"
        );
        titleOverlay.setMaxWidth(240);
        titleOverlay.setWrapText(true);
        StackPane.setAlignment(titleOverlay, Pos.BOTTOM_LEFT);
        imageContainer.getChildren().add(titleOverlay);

        // Region badge (top-right)
        if (blog.getRegion() != null && !blog.getRegion().isEmpty()) {
            Label regionBadge = new Label("📍 " + blog.getRegion());
            regionBadge.setStyle(
                    "-fx-background-color: rgba(255,215,0,0.92);" +
                    "-fx-text-fill: #1E293B;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 3 10;" +
                    "-fx-background-radius: 20;" +
                    "-fx-font-size: 11px;"
            );
            StackPane.setAlignment(regionBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(regionBadge, new Insets(10));
            imageContainer.getChildren().add(regionBadge);
        }

        // ── Card body ────────────────────────────────────────────────────────
        VBox body = new VBox(10);
        body.setPadding(new Insets(14, 16, 14, 16));

        // Category pill + author row
        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        String catStr = blog.getCategorie() != null ? blog.getCategorie() : "Divers";
        Label categorie = new Label(catStr);
        String[] catColors = {"#6366F1","#10B981","#F59E0B","#EC4899","#3B82F6","#8B5CF6"};
        String catColor = catColors[Math.abs(catStr.hashCode()) % catColors.length];
        categorie.setStyle(
                "-fx-background-color: " + catColor + "22;" +
                "-fx-text-fill: " + catColor + ";" +
                "-fx-padding: 3 10;" +
                "-fx-background-radius: 20;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        String auteurNom = blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu";
        Label auteur = new Label("✍ " + auteurNom);
        auteur.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
        auteur.setMaxWidth(130);

        metaRow.getChildren().addAll(categorie, auteur);

        // Short excerpt (max 60 chars)
        String contenuBlog = blog.getContenu();
        String extrait = (contenuBlog != null && contenuBlog.length() > 60)
                ? contenuBlog.substring(0, 60) + "..."
                : (contenuBlog != null ? contenuBlog : "");
        Label extraitLabel = new Label(extrait);
        extraitLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-wrap-text: true;");
        extraitLabel.setWrapText(true);

        // Comment count + date row
        long nbComments = commentaireList.stream().filter(c -> c.getArticleId() == blog.getId()).count();
        String dateStr = blog.getDatePublication() != null
                ? blog.getDatePublication().format(dateShortFormatter) : "";

        HBox statsRow = new HBox(12);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        Label commentLbl = new Label("💬 " + nbComments);
        commentLbl.setStyle("-fx-text-fill: #6366F1; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label dateLbl = new Label("🗓 " + dateStr);
        dateLbl.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 11px;");
        statsRow.getChildren().addAll(commentLbl, dateLbl);

        // "Voir" button — full width
        Button voirBtn = new Button("Voir l'article  →");
        voirBtn.setMaxWidth(Double.MAX_VALUE);
        voirBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366F1, #A3B1FF);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 30;" +
                "-fx-padding: 8 0;" +
                "-fx-font-size: 13px;" +
                "-fx-cursor: hand;"
        );
        voirBtn.setOnAction(e -> showDetailView(blog));
        VBox.setMargin(voirBtn, new Insets(4, 0, 0, 0));

        body.getChildren().addAll(metaRow, extraitLabel, statsRow, voirBtn);

        // Boutons Modifier / Supprimer — uniquement pour l'auteur
        if (currentUser != null && blog.getAuteur() == currentUser.getId()) {
            HBox ownerActions = new HBox(8);
            ownerActions.setAlignment(Pos.CENTER);
            VBox.setMargin(ownerActions, new Insets(4, 0, 0, 0));

            Button modifierBtn = new Button("✏ Modifier");
            modifierBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(modifierBtn, javafx.scene.layout.Priority.ALWAYS);
            modifierBtn.setStyle(
                    "-fx-background-color: #F59E0B;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 30;" +
                    "-fx-padding: 7 0;" +
                    "-fx-font-size: 12px;" +
                    "-fx-cursor: hand;"
            );
            modifierBtn.setOnAction(e -> selectBlog(blog));

            Button supprimerBtn = new Button("🗑 Supprimer");
            supprimerBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(supprimerBtn, javafx.scene.layout.Priority.ALWAYS);
            supprimerBtn.setStyle(
                    "-fx-background-color: #EF4444;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 30;" +
                    "-fx-padding: 7 0;" +
                    "-fx-font-size: 12px;" +
                    "-fx-cursor: hand;"
            );
            supprimerBtn.setOnAction(e -> supprimerBlog(blog));

            ownerActions.getChildren().addAll(modifierBtn, supprimerBtn);
            body.getChildren().add(ownerActions);
        }

        card.getChildren().addAll(imageContainer, body);

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle().replace(
                    "dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0.4, 0, 6)",
                    "dropshadow(gaussian, rgba(99,102,241,0.28), 22, 0.5, 0, 8)"));
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace(
                    "dropshadow(gaussian, rgba(99,102,241,0.28), 22, 0.5, 0, 8)",
                    "dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0.4, 0, 6)"));
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        card.setOnMouseClicked(e -> showDetailView(blog));

        return card;
    }

    private Image loadImage(String path) {
        if (path == null || path.isEmpty()) return null;
        try {
            File file = new File(path);
            if (file.exists()) {
                String url = file.toURI().toString();
                return new Image(url, true);
            }
        } catch (Exception e) {
            // Ignorer
        }
        return null;
    }

    // ========== VUE DÉTAIL ==========
    private void showDetailView(Blog blog) {
        displayedDetailBlog = blog;
        detailTitreLabel.setText(blog.getTitre() != null ? blog.getTitre() : "");
        detailAuteurLabel.setText(blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu");
        detailDateLabel.setText(blog.getDatePublication() != null ? blog.getDatePublication().format(dateShortFormatter) : "");
        detailContenuLabel.setText(blog.getContenu() != null ? blog.getContenu() : "");

        currentTTSLang = "fr";

        resumeContainer.setVisible(false);
        resumeContainer.setManaged(false);

        Image img = loadImage(blog.getImage());
        if (img != null && !img.isError()) {
            detailImageView.setImage(img);
        } else {
            try {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                detailImageView.setImage(defaultImg);
            } catch (Exception ex) {
                detailImageView.setImage(null);
            }
        }
        detailImageView.fitWidthProperty().bind(detailImageContainer.widthProperty());
        detailImageView.setFitHeight(300);

        boolean isLiked = likedByCurrentUser.contains(blog.getId());
        if (heartFullImage != null && heartEmptyImage != null) {
            detailLikeImageView.setImage(isLiked ? heartFullImage : heartEmptyImage);
            detailLikeButton.setText("");
        } else {
            detailLikeButton.setText(isLiked ? "❤️" : "🤍");
            detailLikeButton.setStyle("-fx-background-color: transparent; -fx-font-size: 24px; -fx-cursor: hand;");
            detailLikeImageView.setVisible(false);
        }
        int likes = likeCounts.getOrDefault(blog.getId(), 0);
        detailLikeCountLabel.setText(likes + (likes > 1 ? " likes" : " like"));
        detailLikeButton.setOnAction(e -> toggleLikeDetail(blog));

        // Add action to show likers popup
        detailLikeCountLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-underline: true;");
        detailLikeCountLabel.setOnMouseClicked(e -> showLikersPopup(blog, detailLikeCountLabel));

        boolean estFavori = favorisUtilisateur.contains(blog.getId());
        detailFavButton.setText("🔖");
        detailFavButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 24px;");
        detailFavButton.setTextFill(estFavori ? Color.GOLD : Color.GRAY);
        detailFavButton.setOnAction(e -> toggleFavori(blog, detailFavButton));

        updateDetailStars();

        detailShareBox.getChildren().clear();
        Button whatsappBtn = createShareButton("WhatsApp", "/images/whatsapp.png", "#25D366", blog, null);
        Button facebookBtn = createShareButton("Facebook", "/images/facebook.png", "#4267B2", blog, null);
        Button instagramBtn = createShareButton("Instagram", "/images/instagram.png", "#C13584", blog, null);
        whatsappBtn.setOnAction(e -> share("WhatsApp", blog));
        facebookBtn.setOnAction(e -> share("Facebook", blog));
        instagramBtn.setOnAction(e -> share("Instagram", blog));
        detailShareBox.getChildren().addAll(whatsappBtn, facebookBtn, instagramBtn);

        VBox tagsBox = new VBox(5);
        tagsBox.setAlignment(Pos.CENTER);
        tagsBox.setPadding(new Insets(10, 20, 10, 20));
        if (!blog.getTags().isEmpty()) {
            HBox tagsContainer = new HBox(5);
            tagsContainer.setAlignment(Pos.CENTER);
            for (Tag tag : blog.getTags()) {
                Label tagLabel = new Label("#" + tag.getNom());
                tagLabel.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 30; -fx-font-size: 12px;");
                tagsContainer.getChildren().add(tagLabel);
            }
            tagsBox.getChildren().add(tagsContainer);
        }
        int index = detailView.getChildren().indexOf(detailCommentairesPane);
        if (index > 0) {
            detailView.getChildren().add(index, tagsBox);
        } else {
            detailView.getChildren().add(tagsBox);
        }

        if (blog.getRegion() != null && !blog.getRegion().isEmpty()) {
            detailMeteoLabel.setText("⏳ Chargement météo...");
            detailMeteoIcon.setImage(null);
            OpenWeatherService.getWeatherAsync(blog.getRegion())
                    .thenAccept(weather -> javafx.application.Platform.runLater(() -> {
                        if (weather.isSuccess()) {
                            detailMeteoLabel.setText(String.format("%.0f°C, %s", weather.getTemp(), weather.getDescription()));
                            Image iconImage = new Image(weather.getIconUrl(), true);
                            detailMeteoIcon.setImage(iconImage);
                        } else {
                            detailMeteoLabel.setText("❌ " + weather.getError());
                        }
                    }))
                    .exceptionally(ex -> {
                        javafx.application.Platform.runLater(() -> detailMeteoLabel.setText("❌ Erreur météo"));
                        return null;
                    });
        } else {
            detailMeteoLabel.setText("🌍 Région non spécifiée");
            detailMeteoIcon.setImage(null);
        }

        afficherCommentairesDetail();
        listViewScroll.setVisible(false);
        listViewScroll.setManaged(false);
        detailViewScroll.setVisible(true);
        detailViewScroll.setManaged(true);
        articleFormScroll.setVisible(false);
        articleFormScroll.setManaged(false);
    }

    private void showListView() {
        listViewScroll.setVisible(true);
        listViewScroll.setManaged(true);
        detailViewScroll.setVisible(false);
        detailViewScroll.setManaged(false);
        articleFormScroll.setVisible(false);
        articleFormScroll.setManaged(false);
        displayedDetailBlog = null;
    }

    @FXML
    private void showArticleForm() {
        listViewScroll.setVisible(false);
        listViewScroll.setManaged(false);
        detailViewScroll.setVisible(false);
        detailViewScroll.setManaged(false);
        articleFormScroll.setVisible(true);
        articleFormScroll.setManaged(true);
        clearForm();
    }

    // ========== GESTION DES COMMENTAIRES ==========
    private void afficherCommentairesDetail() {
        if (displayedDetailBlog == null) return;
        detailCommentairesPane.getChildren().clear();
        try {
            List<Commentaire> roots = commentaireCRUD.getHierarchicalComments(displayedDetailBlog.getId());
            for (Commentaire root : roots) {
                VBox commentCard = createCommentCard(root, 0);
                detailCommentairesPane.getChildren().add(commentCard);
            }
        } catch (SQLException e) {
            detailStatusLabel.setText("❌ Erreur chargement commentaires");
        }
    }

    private void ajouterCommentaireDetail() {
        if (displayedDetailBlog == null) {
            detailStatusLabel.setText("❌ Aucun article sélectionné.");
            return;
        }
        if (currentUser == null) {
            detailStatusLabel.setText("❌ Vous devez être connecté pour commenter.");
            return;
        }
        String contenu = detailNewCommentField.getText();
        if (contenu == null || contenu.trim().isEmpty()) {
            detailStatusLabel.setText("❌ Le commentaire ne peut pas être vide.");
            return;
        }
        try {
            Commentaire commentaire = new Commentaire(contenu.trim(), currentUser.getId(), displayedDetailBlog.getId());
            commentaireCRUD.ajouter(commentaire);

            // Notify blog owner
            if (displayedDetailBlog.getAuteur() != currentUser.getId()) {
                String lien = "/blogs/" + displayedDetailBlog.getId();
                Notification notif = new Notification(displayedDetailBlog.getAuteur(), currentUser.getId(), "commentaire", currentUser.getPrenom() + " " + currentUser.getNom() + " a commenté votre article.", lien);
                notificationCRUD.ajouter(notif);
            }

            // Detect and notify mentions
            List<Integer> mentionIds = detecterMentions(contenu.trim());
            if (!mentionIds.isEmpty()) {
                String lien = "/blogs/" + displayedDetailBlog.getId();
                envoyerNotificationsMention(mentionIds, "mention",
                        currentUser.getPrenom() + " " + currentUser.getNom() + " vous a mentionné dans un commentaire.", lien);
            }
            detailNewCommentField.clear();
            afficherCommentairesDetail();
            loadAllComments();
            updateStats();
            detailStatusLabel.setText("✅ Commentaire ajouté.");
        } catch (SQLException e) {
            detailStatusLabel.setText("❌ Erreur : " + e.getMessage());
        }
    }

    private VBox createCommentCard(Commentaire commentaire, int level) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14, 16, 14, 16));
        // Solid white background — fully readable on any parent
        String borderLeft = level > 0 ? "-fx-border-color: transparent transparent transparent #6366F1; -fx-border-width: 0 0 0 3;" : "";
        card.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.10), 12, 0.3, 0, 4);" +
            borderLeft
        );
        card.setPrefWidth(Double.MAX_VALUE);
        card.setMaxWidth(Double.MAX_VALUE);
        if (level > 0) {
            VBox.setMargin(card, new Insets(0, 0, 0, 22));
        }

        // ── Header row: avatar + author + time ──────────────────────────────
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Initials avatar
        String rawName = commentaire.getUtilisateurNom() != null
                ? commentaire.getUtilisateurNom()
                : "U" + commentaire.getUtilisateur();
        String[] parts = rawName.trim().split("\\s+");
        String initials = "";
        if (parts.length >= 2)
            initials = String.valueOf(parts[0].charAt(0)).toUpperCase() + String.valueOf(parts[1].charAt(0)).toUpperCase();
        else if (parts.length == 1 && !parts[0].isEmpty())
            initials = String.valueOf(parts[0].charAt(0)).toUpperCase();
        else initials = "?";

        // Pick a deterministic colour from a palette based on user id
        String[] avatarGradients = {
            "linear-gradient(to bottom right,#6366F1,#8B5CF6)",
            "linear-gradient(to bottom right,#EC4899,#F472B6)",
            "linear-gradient(to bottom right,#10B981,#34D399)",
            "linear-gradient(to bottom right,#F59E0B,#FBBF24)",
            "linear-gradient(to bottom right,#3B82F6,#60A5FA)"
        };
        String grad = avatarGradients[Math.abs(commentaire.getUtilisateur()) % avatarGradients.length];

        StackPane avatarPane = new StackPane();
        avatarPane.setMinSize(40, 40);
        avatarPane.setMaxSize(40, 40);
        avatarPane.setStyle("-fx-background-color: " + grad + "; -fx-background-radius: 20;");
        Label avatarLbl = new Label(initials);
        avatarLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        avatarPane.getChildren().add(avatarLbl);

        // Author + time column
        VBox authorCol = new VBox(2);
        HBox.setHgrow(authorCol, Priority.ALWAYS);
        Label authorLbl = new Label(rawName);
        authorLbl.setStyle("-fx-text-fill: #1E293B; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Time-ago
        String timeAgo = "";
        if (commentaire.getDateCommentaire() != null) {
            long mins = java.time.Duration.between(commentaire.getDateCommentaire(), java.time.LocalDateTime.now()).toMinutes();
            if (mins < 1) timeAgo = "À l'instant";
            else if (mins < 60) timeAgo = mins + " min";
            else if (mins < 1440) timeAgo = (mins / 60) + " h";
            else timeAgo = commentaire.getDateCommentaire().format(dateShortFormatter);
        }
        Label timeLbl = new Label(timeAgo);
        timeLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
        authorCol.getChildren().addAll(authorLbl, timeLbl);

        header.getChildren().addAll(avatarPane, authorCol);

        // ── Content ──────────────────────────────────────────────────────────
        TextFlow contenuFlow = formaterTexteAvecMentions(commentaire.getContenu());
        // Force all plain Text nodes to be dark and readable
        contenuFlow.getChildren().forEach(node -> {
            if (node instanceof javafx.scene.text.Text t) {
                t.setStyle("-fx-fill: #334155; -fx-font-size: 14px;");
            }
        });
        contenuFlow.setPrefWidth(Double.MAX_VALUE);
        contenuFlow.setMaxWidth(Double.MAX_VALUE);

        // ── Actions row ──────────────────────────────────────────────────────
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (currentUser != null) {
            Button replyBtn = new Button("↩ Répondre");
            replyBtn.setStyle(
                "-fx-background-color: #EEF2FF;" +
                "-fx-text-fill: #6366F1;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 5 14;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            );
            replyBtn.setOnAction(e -> showReplyField(commentaire, card, level));
            actions.getChildren().add(replyBtn);
        }

        if (currentUser != null && commentaire.getUtilisateur() == currentUser.getId()) {
            Button editBtn = new Button("✏ Modifier");
            editBtn.setStyle(
                "-fx-background-color: #FEF3C7;" +
                "-fx-text-fill: #D97706;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 5 14;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            );
            editBtn.setOnAction(e -> showEditComment(commentaire, card, contenuFlow, header, actions, level));

            Button deleteBtn = new Button("🗑 Supprimer");
            deleteBtn.setStyle(
                "-fx-background-color: #FEE2E2;" +
                "-fx-text-fill: #EF4444;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 5 14;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            );
            deleteBtn.setOnAction(e -> supprimerCommentaireDetail(commentaire));
            actions.getChildren().addAll(editBtn, deleteBtn);
        }

        card.getChildren().addAll(header, contenuFlow, actions);

        // ── Replies ──────────────────────────────────────────────────────────
        if (!commentaire.getReplies().isEmpty()) {
            VBox repliesBox = new VBox(10);
            repliesBox.setPadding(new Insets(8, 0, 0, 0));
            for (Commentaire reply : commentaire.getReplies()) {
                repliesBox.getChildren().add(createCommentCard(reply, level + 1));
            }
            card.getChildren().add(repliesBox);
        }

        return card;
    }

    private TextFlow formaterTexteAvecMentions(String texte) {
        TextFlow textFlow = new TextFlow();
        if (texte == null || texte.isEmpty()) return textFlow;

        Pattern pattern = Pattern.compile("(@\\w+)");
        Matcher matcher = pattern.matcher(texte);
        int lastEnd = 0;
        while (matcher.find()) {
            String avant = texte.substring(lastEnd, matcher.start());
            if (!avant.isEmpty()) {
                Text textAvant = new Text(avant);
                textAvant.setStyle("-fx-fill: #334155; -fx-font-size: 14px;");
                textFlow.getChildren().add(textAvant);
            }
            String mention = matcher.group();
            Text mentionText = new Text(mention);
            mentionText.setStyle("-fx-fill: #6366F1; -fx-font-size: 14px; -fx-font-weight: bold;");
            textFlow.getChildren().add(mentionText);

            lastEnd = matcher.end();
        }
        if (lastEnd < texte.length()) {
            String reste = texte.substring(lastEnd);
            Text textReste = new Text(reste);
            textReste.setStyle("-fx-fill: #334155; -fx-font-size: 14px;");
            textFlow.getChildren().add(textReste);
        }
        return textFlow;
    }

    private void showReplyField(Commentaire parentComment, VBox parentCard, int level) {
        if (currentUser == null) {
            detailStatusLabel.setText("❌ Connectez-vous pour répondre.");
            return;
        }

        TextField replyField = new TextField();
        replyField.setPromptText("Écrire une réponse...");
        replyField.setStyle(
            "-fx-background-color: #F8FAFF;" +
            "-fx-text-fill: #1E293B;" +
            "-fx-prompt-text-fill: #94A3B8;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: #C7D2FE;" +
            "-fx-border-radius: 20;" +
            "-fx-padding: 8 14;"
        );
        HBox.setHgrow(replyField, Priority.ALWAYS);

        Button sendReplyBtn = new Button("↩ Envoyer");
        sendReplyBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 7 16; -fx-font-size: 12px; -fx-font-weight: bold;");
        Button cancelReplyBtn = new Button("✕");
        cancelReplyBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 7 12; -fx-font-size: 12px;");

        HBox replyInputBox = new HBox(10, replyField, sendReplyBtn, cancelReplyBtn);
        replyInputBox.setAlignment(Pos.CENTER_LEFT);
        replyInputBox.setPadding(new Insets(8, 0, 4, 0));
        replyInputBox.setStyle("-fx-background-color: #EEF2FF; -fx-background-radius: 14; -fx-padding: 8 12;");

        parentCard.getChildren().add(replyInputBox);

        sendReplyBtn.setOnAction(e -> {
            String replyText = replyField.getText().trim();
            if (replyText.isEmpty()) {
                detailStatusLabel.setText("❌ La réponse ne peut pas être vide.");
                return;
            }
            try {
                Commentaire reply = new Commentaire();
                reply.setContenu(replyText);
                reply.setUtilisateur(currentUser.getId());
                reply.setArticleId(parentComment.getArticleId());
                reply.setParentId(parentComment.getId());
                commentaireCRUD.ajouter(reply);

                // Notify parent comment owner
                if (parentComment.getUtilisateur() != currentUser.getId()) {
                    String lien = "/blogs/" + parentComment.getArticleId();
                    Notification notif = new Notification(parentComment.getUtilisateur(), currentUser.getId(), "reponse", currentUser.getPrenom() + " " + currentUser.getNom() + " a répondu à votre commentaire.", lien);
                    notificationCRUD.ajouter(notif);
                }

                afficherCommentairesDetail();
                detailStatusLabel.setText("✅ Réponse ajoutée.");
            } catch (SQLException ex) {
                detailStatusLabel.setText("❌ Erreur : " + ex.getMessage());
            }
        });

        cancelReplyBtn.setOnAction(e -> parentCard.getChildren().remove(replyInputBox));
    }

    private void showEditComment(Commentaire commentaire, VBox card, TextFlow contenuFlow, HBox headerOrMeta, HBox actions, int level) {
        card.getChildren().clear();

        TextField editField = new TextField(commentaire.getContenu());
        editField.setStyle(
            "-fx-background-color: #F8FAFF;" +
            "-fx-text-fill: #1E293B;" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #C7D2FE;" +
            "-fx-border-radius: 14;" +
            "-fx-padding: 10 14;"
        );
        HBox.setHgrow(editField, Priority.ALWAYS);

        HBox editActions = new HBox(10);
        editActions.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button("✓ Enregistrer");
        saveBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 7 18; -fx-font-size: 12px; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> {
            String newContent = editField.getText().trim();
            if (!newContent.isEmpty()) {
                commentaire.setContenu(newContent);
                try {
                    commentaireCRUD.modifier(commentaire);
                    afficherCommentairesDetail();
                    detailStatusLabel.setText("✅ Commentaire modifié.");
                } catch (SQLException ex) {
                    detailStatusLabel.setText("❌ Erreur : " + ex.getMessage());
                }
            }
        });

        Button cancelBtn = new Button("✕ Annuler");
        cancelBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 7 18; -fx-font-size: 12px;");
        cancelBtn.setOnAction(e -> afficherCommentairesDetail());

        editActions.getChildren().addAll(saveBtn, cancelBtn);
        card.getChildren().addAll(headerOrMeta, editField, editActions);
    }

    private void supprimerCommentaireDetail(Commentaire commentaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce commentaire ? Toutes ses réponses seront également supprimées.", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    commentaireCRUD.supprimer(commentaire.getId());
                    afficherCommentairesDetail();
                    detailStatusLabel.setText("✅ Commentaire supprimé.");
                } catch (SQLException e) {
                    detailStatusLabel.setText("❌ Erreur : " + e.getMessage());
                }
            }
        });
    }

    // ========== GESTION DES LIKES ==========
    private void toggleLike(Blog blog, Button heartButton, Label countLabel) {
        if (currentUser == null) {
            showWarning("Vous devez être connecté pour liker un article.");
            return;
        }
        int articleId = blog.getId();
        boolean currentlyLiked = likedByCurrentUser.contains(articleId);

        try {
            if (currentlyLiked) {
                likeCRUD.supprimerParUtilisateurEtArticle(currentUser.getId(), articleId);
                likedByCurrentUser.remove(articleId);
                likeCounts.put(articleId, likeCounts.getOrDefault(articleId, 0) - 1);
            } else {
                Like like = new Like(currentUser.getId(), articleId);
                likeCRUD.ajouter(like);
                likedByCurrentUser.add(articleId);
                likeCounts.put(articleId, likeCounts.getOrDefault(articleId, 0) + 1);
            }
            int newLikes = likeCounts.getOrDefault(articleId, 0);
            boolean newLikedState = likedByCurrentUser.contains(articleId);
            if (heartEmptyImage != null && heartFullImage != null) {
                ImageView iv = (ImageView) heartButton.getGraphic();
                iv.setImage(newLikedState ? heartFullImage : heartEmptyImage);
            } else {
                heartButton.setText(newLikedState ? "❤️" : "🤍");
            }
            countLabel.setText(newLikes + (newLikes > 1 ? " likes" : " like"));

            if (displayedDetailBlog != null && displayedDetailBlog.getId() == articleId) {
                updateDetailLikeButton();
            }
        } catch (SQLException ex) {
            showError("Erreur lors du like", ex.getMessage());
        }
    }

    private void toggleLikeDetail(Blog blog) {
        if (currentUser == null) {
            showWarning("Connectez-vous pour liker.");
            return;
        }
        int articleId = blog.getId();
        boolean currentlyLiked = likedByCurrentUser.contains(articleId);

        try {
            if (currentlyLiked) {
                likeCRUD.supprimerParUtilisateurEtArticle(currentUser.getId(), articleId);
                likedByCurrentUser.remove(articleId);
                likeCounts.put(articleId, likeCounts.getOrDefault(articleId, 0) - 1);
            } else {
                Like like = new Like(currentUser.getId(), articleId);
                likeCRUD.ajouter(like);
                likedByCurrentUser.add(articleId);
                likeCounts.put(articleId, likeCounts.getOrDefault(articleId, 0) + 1);
            }
            int newLikes = likeCounts.getOrDefault(articleId, 0);
            boolean newLikedState = likedByCurrentUser.contains(articleId);
            if (heartFullImage != null && heartEmptyImage != null) {
                detailLikeImageView.setImage(newLikedState ? heartFullImage : heartEmptyImage);
            } else {
                detailLikeButton.setText(newLikedState ? "❤️" : "🤍");
            }
            detailLikeCountLabel.setText(newLikes + (newLikes > 1 ? " likes" : " like"));

            refreshAllCards();
        } catch (SQLException ex) {
            showError("Erreur like", ex.getMessage());
        }
    }

    private void updateDetailLikeButton() {
        if (displayedDetailBlog != null) {
            boolean isLiked = likedByCurrentUser.contains(displayedDetailBlog.getId());
            int likes = likeCounts.getOrDefault(displayedDetailBlog.getId(), 0);
            if (heartFullImage != null && heartEmptyImage != null) {
                detailLikeImageView.setImage(isLiked ? heartFullImage : heartEmptyImage);
            } else {
                detailLikeButton.setText(isLiked ? "❤️" : "🤍");
            }
            detailLikeCountLabel.setText(likes + (likes > 1 ? " likes" : " like"));
        }
    }

    private void showLikersPopup(Blog blog, Label anchor) {
        try {
            List<Like> allLikes = likeCRUD.afficherTous();
            List<Integer> likerIds = allLikes.stream()
                    .filter(l -> l.getArticleId() == blog.getId())
                    .map(Like::getUtilisateurId)
                    .collect(Collectors.toList());

            if (likerIds.isEmpty()) {
                showInfo("Aucun like pour le moment.");
                return;
            }

            List<Utilisateur> allUsers = utilisateurCRUD.afficher();
            List<Utilisateur> likers = allUsers.stream()
                    .filter(u -> likerIds.contains(u.getId()))
                    .collect(Collectors.toList());

            Popup popup = new Popup();
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            VBox container = new VBox(10);
            container.setStyle(
                    "-fx-background-color: #FFFFFF;" +
                    "-fx-background-radius: 14;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.4, 0, 4);" +
                    "-fx-padding: 15;"
            );
            container.setPrefWidth(240);

            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);
            Label title = new Label("Aimé par :");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1E293B;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button closeBtn = new Button("✕");
            closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 13px; -fx-text-fill: #94A3B8;");
            closeBtn.setOnAction(e -> popup.hide());
            header.getChildren().addAll(title, spacer, closeBtn);

            VBox usersList = new VBox(8);
            for (Utilisateur u : likers) {
                HBox userRow = new HBox(10);
                userRow.setAlignment(Pos.CENTER_LEFT);

                String initials = "";
                if (u.getPrenom() != null && !u.getPrenom().isEmpty()) initials += u.getPrenom().charAt(0);
                if (u.getNom() != null && !u.getNom().isEmpty()) initials += u.getNom().charAt(0);
                initials = initials.toUpperCase();

                StackPane avatar = new StackPane();
                avatar.setMinSize(32, 32);
                avatar.setMaxSize(32, 32);
                String[] colors = {"#EF4444", "#3B82F6", "#10B981", "#F59E0B", "#8B5CF6"};
                String color = colors[Math.abs(u.getId()) % colors.length];
                avatar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 16;");
                Label initLbl = new Label(initials);
                initLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
                avatar.getChildren().add(initLbl);

                Label nameLbl = new Label(u.getPrenom() + " " + u.getNom());
                nameLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");

                userRow.getChildren().addAll(avatar, nameLbl);
                usersList.getChildren().add(userRow);
            }

            ScrollPane scroll = new ScrollPane(usersList);
            scroll.setFitToWidth(true);
            scroll.setPrefHeight(Math.min(likers.size() * 50, 250));
            scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

            container.getChildren().addAll(header, new Separator(), scroll);
            popup.getContent().add(container);

            popup.show(anchor, anchor.localToScreen(0, anchor.getHeight()).getX(), anchor.localToScreen(0, anchor.getHeight()).getY());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les likes.");
        }
    }

    // ========== GESTION DES ÉTOILES (VUE DÉTAIL) ==========
    private void updateDetailStars() {
        if (displayedDetailBlog == null) return;
        int articleId = displayedDetailBlog.getId();
        double avg = ratingAverages.getOrDefault(articleId, 0.0);
        int userNote = userRatings.getOrDefault(articleId, 0);
        int voteCount = voteCounts.getOrDefault(articleId, 0);

        detailStarsBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Button star = new Button();
            star.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 20px;");
            if (userNote >= i) {
                star.setText("★");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            } else if (avg >= i - 0.5 && avg < i) {
                star.setText("½");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            } else if (avg >= i) {
                star.setText("★");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            } else {
                star.setText("☆");
                star.setStyle(star.getStyle() + " -fx-text-fill: #FFD700;");
            }
            int note = i;
            star.setOnAction(e -> {
                if (currentUser == null) {
                    showWarning("Connectez-vous pour noter.");
                    return;
                }
                try {
                    Rating rating = new Rating(currentUser.getId(), articleId, note);
                    ratingCRUD.ajouterOuModifier(rating);
                    loadRatings();
                    refreshAllCards();
                    updateDetailStars();
                } catch (SQLException ex) {
                    showError("Erreur notation", ex.getMessage());
                }
            });
            detailStarsBox.getChildren().add(star);
        }
        detailAvgLabel.setText(String.format("%.1f (%d votes)", avg, voteCount));
    }

    private void refreshAllCards() {
        displayBlogs(blogList);
    }

    // ========== GESTION DES TAGS ==========
    private void traiterTags(int articleId, String tagsInput) {
        if (tagsInput == null || tagsInput.trim().isEmpty()) return;
        String[] tagNames = tagsInput.split(",");
        for (String tagName : tagNames) {
            tagName = tagName.trim();
            if (!tagName.isEmpty()) {
                try {
                    int tagId = tagCRUD.ajouterOuRecuperer(tagName);
                    System.out.println("Tag '" + tagName + "' a ID " + tagId);
                    tagCRUD.associerTagArticle(articleId, tagId);
                    System.out.println("Association article " + articleId + " - tag " + tagId);
                } catch (SQLException e) {
                    System.err.println("❌ Erreur lors de l'ajout du tag '" + tagName + "' : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    // ========== MENTIONS ==========
    private void setupMentionAutoComplete() {
        suggestionsPopup = new Popup();
        suggestionsPopup.setAutoHide(true);
        suggestionsPopup.setHideOnEscape(true);

        suggestionsListView = new ListView<>();
        suggestionsListView.setPrefHeight(150);
        suggestionsListView.setPrefWidth(200);
        suggestionsListView.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #ddd; -fx-border-radius: 10;");

        suggestionsListView.setOnMouseClicked(e -> {
            Utilisateur selected = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                insererMention(selected);
                suggestionsPopup.hide();
            }
        });

        suggestionsListView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                Utilisateur selected = suggestionsListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    insererMention(selected);
                    suggestionsPopup.hide();
                }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                suggestionsPopup.hide();
            }
        });

        suggestionsPopup.getContent().add(suggestionsListView);

        detailNewCommentField.textProperty().addListener((obs, oldText, newText) -> {
            int caretPos = detailNewCommentField.getCaretPosition();
            if (caretPos > 0 && caretPos <= newText.length()) {
                char prevChar = newText.charAt(caretPos - 1);
                if (prevChar == '@') {
                    String motEnCours = trouverMotEnCours(newText, caretPos - 1);
                    if (motEnCours != null && motEnCours.startsWith("@")) {
                        String recherche = motEnCours.substring(1);
                        afficherSuggestions(recherche);
                    }
                } else {
                    if (suggestionsPopup.isShowing()) {
                        suggestionsPopup.hide();
                    }
                }
            } else {
                if (suggestionsPopup.isShowing()) {
                    suggestionsPopup.hide();
                }
            }
        });
    }

    private String trouverMotEnCours(String texte, int position) {
        if (position < 0 || position >= texte.length()) return null;
        int debut = position;
        while (debut > 0 && texte.charAt(debut - 1) != ' ' && texte.charAt(debut - 1) != '\n') {
            debut--;
        }
        int fin = position;
        while (fin < texte.length() && texte.charAt(fin) != ' ' && texte.charAt(fin) != '\n') {
            fin++;
        }
        return texte.substring(debut, fin);
    }

    private void afficherSuggestions(String recherche) {
        try {
            List<Utilisateur> users = utilisateurCRUD.afficher();
            ObservableList<Utilisateur> suggestions = FXCollections.observableArrayList();
            for (Utilisateur u : users) {
                if (u.getId() != currentUser.getId()) {
                    String fullName = u.getPrenom() + " " + u.getNom();
                    if (fullName.toLowerCase().contains(recherche.toLowerCase())) {
                        suggestions.add(u);
                    }
                }
            }
            suggestionsListView.setItems(suggestions);

            suggestionsListView.setCellFactory(lv -> new ListCell<Utilisateur>() {
                @Override
                protected void updateItem(Utilisateur item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("@" + item.getPrenom() + " " + item.getNom());
                    }
                }
            });

            if (!suggestions.isEmpty() && !suggestionsPopup.isShowing()) {
                suggestionsPopup.show(detailNewCommentField,
                        detailNewCommentField.localToScreen(0, detailNewCommentField.getHeight()).getX(),
                        detailNewCommentField.localToScreen(0, detailNewCommentField.getHeight()).getY());
            } else if (suggestions.isEmpty()) {
                suggestionsPopup.hide();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insererMention(Utilisateur user) {
        String texte = detailNewCommentField.getText();
        int caretPos = detailNewCommentField.getCaretPosition();

        int debut = caretPos - 1;
        while (debut > 0 && texte.charAt(debut - 1) != ' ' && texte.charAt(debut - 1) != '\n') {
            debut--;
        }

        String mention = "@" + user.getPrenom() + user.getNom() + " ";
        String newText = texte.substring(0, debut) + mention + texte.substring(caretPos);
        detailNewCommentField.setText(newText);
        detailNewCommentField.positionCaret(debut + mention.length());
    }

    private List<Integer> detecterMentions(String texte) {
        List<Integer> mentionsIds = new ArrayList<>();
        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(texte);
        try {
            List<Utilisateur> users = utilisateurCRUD.afficher();
            while (matcher.find()) {
                String pseudo = matcher.group(1);
                for (Utilisateur u : users) {
                    String full = u.getPrenom() + u.getNom();
                    if (full.equalsIgnoreCase(pseudo) && u.getId() != currentUser.getId()) {
                        mentionsIds.add(u.getId());
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mentionsIds;
    }

    private void envoyerNotificationsMention(List<Integer> utilisateursIds, String type, String contenu, String lien) {
        for (int userId : utilisateursIds) {
            try {
                Notification notif = new Notification(userId, currentUser.getId(), type, contenu, lien);
                notificationCRUD.ajouter(notif);
                System.out.println("Notification envoyée à l'utilisateur " + userId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ========== GESTION DES FAVORIS (SIGNET) ==========
    private void toggleFavori(Blog blog, Button favButton) {
        if (currentUser == null) {
            showWarning("Connectez-vous pour ajouter aux favoris.");
            return;
        }
        int articleId = blog.getId();
        boolean actuellementFavori = favorisUtilisateur.contains(articleId);

        try {
            if (actuellementFavori) {
                favoriCRUD.supprimer(currentUser.getId(), articleId);
                favorisUtilisateur.remove(articleId);
                showInfo("Article retiré des favoris.");
            } else {
                Favori f = new Favori(currentUser.getId(), articleId);
                favoriCRUD.ajouter(f);
                favorisUtilisateur.add(articleId);
                showInfo("Article ajouté aux favoris.");
            }
            boolean nouveauState = favorisUtilisateur.contains(articleId);
            favButton.setTextFill(nouveauState ? Color.GOLD : Color.GRAY);
            if (displayedDetailBlog != null && displayedDetailBlog.getId() == articleId) {
                detailFavButton.setTextFill(nouveauState ? Color.GOLD : Color.GRAY);
            }
            refreshAllCards();
        } catch (SQLException e) {
            showError("Erreur lors de la gestion des favoris", e.getMessage());
        }
    }

    // ========== CRUD BLOG ==========
    private void supprimerBlog(Blog blog) {
        if (currentUser == null || blog.getAuteur() != currentUser.getId()) {
            showWarning("Vous ne pouvez supprimer que vos propres articles.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cet article ? Tous les commentaires, likes et notes associés seront également supprimés.",
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
        if (currentUser == null || blog.getAuteur() != currentUser.getId()) {
            showWarning("Vous ne pouvez modifier que vos propres articles.");
            return;
        }
        showArticleForm(); // bascule vers le formulaire
        this.selectedBlog = blog;
        articleIdLabel.setText(String.valueOf(blog.getId()));
        titreField.setText(blog.getTitre() != null ? blog.getTitre() : "");
        contenuField.setText(blog.getContenu() != null ? blog.getContenu() : "");
        imageField.setText(blog.getImage() != null ? blog.getImage() : "");
        regionField.setValue(blog.getRegion());
        categorieField.setValue(blog.getCategorie());
        auteurLabel.setText(blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu");
        if (!blog.getTags().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Tag tag : blog.getTags()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(tag.getNom());
            }
            tagsField.setText(sb.toString());
        } else {
            tagsField.clear();
        }
        selectedArticleLabel.setText("Article sélectionné : " + (blog.getTitre() != null ? blog.getTitre() : ""));
        updateFormButtons();
        clearAllErrors();
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
            System.out.println("✅ Article ajouté avec ID : " + b.getId());

            String tagsInput = tagsField.getText();
            if (tagsInput != null && !tagsInput.trim().isEmpty()) {
                traiterTags(b.getId(), tagsInput);
            }

            refreshData();
            clearForm();
            showListView(); // retour à la liste
            showInfo("Article ajouté avec succès.");
        } catch (SQLException e) {
            showError("Erreur ajout", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierBlog() {
        if (selectedBlog == null) {
            showWarning("Sélectionnez un article à modifier.");
            return;
        }
        if (currentUser == null || selectedBlog.getAuteur() != currentUser.getId()) {
            showWarning("Vous ne pouvez modifier que vos propres articles.");
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

            tagCRUD.supprimerAssociationsArticle(selectedBlog.getId());
            String tagsInput = tagsField.getText();
            if (tagsInput != null && !tagsInput.trim().isEmpty()) {
                traiterTags(selectedBlog.getId(), tagsInput);
            }

            refreshData();
            clearForm();
            showListView(); // retour à la liste
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
        if (currentUser == null || selectedBlog.getAuteur() != currentUser.getId()) {
            showWarning("Vous ne pouvez supprimer que vos propres articles.");
            return;
        }
        supprimerBlog(selectedBlog);
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
        tagsField.clear();
        if (currentUser != null) {
            auteurLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        } else {
            auteurLabel.setText("Utilisateur inconnu");
        }
        selectedArticleLabel.setText("(aucun article sélectionné)");
        updateFormButtons();
        clearAllErrors();
    }

    private void filterArticles() {
        String search = searchField.getText().toLowerCase();
        String region = regionFilterCombo.getValue();
        String cat = categorieFilterCombo.getValue();

        List<Blog> filtered = blogList.stream()
                .filter(b -> (search.isEmpty() ||
                        (b.getTitre() != null && b.getTitre().toLowerCase().contains(search)) ||
                        (b.getContenu() != null && b.getContenu().toLowerCase().contains(search)) ||
                        (b.getAuteurNom() != null && b.getAuteurNom().toLowerCase().contains(search))))
                .filter(b -> "Toutes".equals(region) || (b.getRegion() != null && b.getRegion().equals(region)))
                .filter(b -> "Toutes".equals(cat) || (b.getCategorie() != null && b.getCategorie().equals(cat)))
                .collect(Collectors.toList());

        if (modeFavoris) {
            filtered = filtered.stream()
                    .filter(b -> favorisUtilisateur.contains(b.getId()))
                    .collect(Collectors.toList());
        }
        displayBlogs(filtered);
    }

    private void refreshData() throws SQLException {
        loadBlogs();
        loadAllComments();
        loadLikes();
        loadRatings();
        loadFavoris();
        filterArticles();
        if (selectedBlog != null) {
            blogList.stream()
                    .filter(b -> b.getId() == selectedBlog.getId())
                    .findFirst()
                    .ifPresentOrElse(this::selectBlog, this::clearForm);
        }
        if (displayedDetailBlog != null) {
            afficherCommentairesDetail();
            updateDetailLikeButton();
            updateDetailStars();
            detailShareBox.getChildren().clear();
            Button whatsappBtn = createShareButton("WhatsApp", "/images/whatsapp.png", "#25D366", displayedDetailBlog, null);
            Button facebookBtn = createShareButton("Facebook", "/images/facebook.png", "#4267B2", displayedDetailBlog, null);
            Button instagramBtn = createShareButton("Instagram", "/images/instagram.png", "#C13584", displayedDetailBlog, null);
            whatsappBtn.setOnAction(e -> share("WhatsApp", displayedDetailBlog));
            facebookBtn.setOnAction(e -> share("Facebook", displayedDetailBlog));
            instagramBtn.setOnAction(e -> share("Instagram", displayedDetailBlog));
            detailShareBox.getChildren().addAll(whatsappBtn, facebookBtn, instagramBtn);
        }
        loadNotifications();
    }

    private void updateStats() {
        totalBlogsLabel.setText(String.valueOf(blogList.size()));
        totalCommentsLabel.setText(String.valueOf(commentaireList.size()));
    }

    private void showInfo(String msg) { statusLabel.setText("✅ " + msg); }
    private void showWarning(String msg) { new Alert(Alert.AlertType.WARNING, msg).show(); }
    private void showError(String title, String msg) { Alert a = new Alert(Alert.AlertType.ERROR, msg); a.setTitle(title); a.show(); }

    private void updateFormButtons() {
        boolean isEditing = (selectedBlog != null);
        ajouterBtn.setVisible(!isEditing);
        ajouterBtn.setManaged(!isEditing);
        modifierBtn.setVisible(isEditing);
        modifierBtn.setManaged(isEditing);
        supprimerBtn.setVisible(isEditing);
        supprimerBtn.setManaged(isEditing);
    }

    // ========== PARTAGE CRÉATIF ==========
    private void showSharePopup(Button anchor, Blog blog) {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        VBox content = new VBox(10);
        content.setStyle("-fx-background-color: rgba(30,30,30,0.95); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 15; -fx-padding: 15;");
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Partager sur");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button whatsappBtn = createShareButton("WhatsApp", "/images/whatsapp.png", "#25D366", blog, popup);
        Button facebookBtn = createShareButton("Facebook", "/images/facebook.png", "#4267B2", blog, popup);
        Button instagramBtn = createShareButton("Instagram", "/images/instagram.png", "#C13584", blog, popup);

        content.getChildren().addAll(title, whatsappBtn, facebookBtn, instagramBtn);

        popup.getContent().add(content);

        popup.show(anchor, anchor.localToScreen(0, anchor.getHeight()).getX(), anchor.localToScreen(0, anchor.getHeight()).getY());
    }

    private Button createShareButton(String name, String iconPath, String color, Blog blog, Popup popup) {
        Button btn = new Button(name);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-size: 12px;");
        btn.setMaxWidth(Double.MAX_VALUE);

        try {
            InputStream is = getClass().getResourceAsStream(iconPath);
            if (is != null) {
                Image icon = new Image(is);
                ImageView iv = new ImageView(icon);
                iv.setFitHeight(20);
                iv.setFitWidth(20);
                btn.setGraphic(iv);
                btn.setContentDisplay(ContentDisplay.LEFT);
            } else {
                String emoji = "";
                if (name.equals("WhatsApp")) emoji = "📱 ";
                else if (name.equals("Facebook")) emoji = "📘 ";
                else if (name.equals("Instagram")) emoji = "📷 ";
                btn.setText(emoji + name);
            }
        } catch (Exception e) {
            btn.setText(name);
        }

        btn.setOnAction(e -> {
            share(name, blog);
            if (popup != null) popup.hide();
        });

        return btn;
    }

    private void share(String platform, Blog blog) {
        String titre = blog.getTitre();
        String contenu = blog.getContenu();
        String articleUrl = "http://wingo.tn/article/" + blog.getId();
        String shareText = titre + " - " + contenu + " " + articleUrl;
        String link = "";

        try {
            switch (platform) {
                case "WhatsApp":
                    link = "https://wa.me/?text=" + URLEncoder.encode(shareText, StandardCharsets.UTF_8);
                    break;
                case "Facebook":
                    link = "https://www.facebook.com/sharer/sharer.php?u=" + URLEncoder.encode(articleUrl, StandardCharsets.UTF_8);
                    break;
                case "Instagram":
                    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(shareText), null);
                    showInfo("Texte copié dans le presse-papier pour Instagram");
                    return;
                default:
                    return;
            }
            java.awt.Desktop.getDesktop().browse(URI.create(link));
        } catch (Exception e) {
            showError("Erreur de partage", e.getMessage());
        }
    }

    // ========== NOUVELLES MÉTHODES POUR LA SIDEBAR ==========
    @FXML
    private void toggleDarkMode() {
        if (rootPane.getStyleClass().contains("dark")) {
            rootPane.getStyleClass().remove("dark");
            darkModeBtn.setText("🌙 Dark Mode");
            backgroundPane.setStyle("-fx-background-color: #F4F7FB;");
        } else {
            rootPane.getStyleClass().add("dark");
            darkModeBtn.setText("☀️ Light Mode");
            backgroundPane.setStyle("-fx-background-color: #1E293B;");
        }
    }

    @FXML
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment vous déconnecter ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                currentUser = null;
                auteurLabel.setText("Non connecté");
                detailConnectedUserLabel.setText("?");
                sidebarUserName.setText("Invité");
                showInfo("Déconnexion réussie.");
            }
        });
    }

    // Navigation
    @FXML private void goDashboard() { System.out.println("Dashboard"); }
    @FXML private void goBlog() { showListView(); }
    @FXML private void goCommentaires() { System.out.println("Commentaires"); }
    @FXML private void goSettings() { System.out.println("Settings"); }
}
