package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Entites.Like;
import Entites.Notification;
import Entites.Rating;
import Entites.Tag;
import Entites.Utilisateur;
import Services.BlogCRUD;
import Services.CommentaireCRUD;
import Services.LikeCRUD;
import Services.NotificationCRUD;
import Services.RatingCRUD;
import Services.TagCRUD;
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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
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

public class BlogController implements Initializable {

    // Services
    private final BlogCRUD blogCRUD = new BlogCRUD();
    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private final UtilisateurCRUD utilisateurCRUD = new UtilisateurCRUD();
    private final LikeCRUD likeCRUD = new LikeCRUD();
    private final RatingCRUD ratingCRUD = new RatingCRUD();
    private final TagCRUD tagCRUD = new TagCRUD();
    private final NotificationCRUD notificationCRUD = new NotificationCRUD(); // MENTIONS

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

    // Images pour les cœurs
    private Image heartEmptyImage;
    private Image heartFullImage;

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
    @FXML private TextField newCommentField;
    @FXML private Button choisirImageBtn;
    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Button addCommentBtn;
    @FXML private Label statusLabel;
    @FXML private Label connectedUserLabel;

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
    @FXML private FlowPane detailCommentairesPane;
    @FXML private TextField detailNewCommentField;   // Champ de saisie des commentaires (avec mentions)
    @FXML private Button detailAddCommentBtn;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailConnectedUserLabel;
    @FXML private Button detailLikeButton;
    @FXML private ImageView detailLikeImageView;
    @FXML private Label detailLikeCountLabel;
    @FXML private HBox detailStarsBox;
    @FXML private Label detailAvgLabel;
    @FXML private HBox detailShareBox;

    // Filtres
    @FXML private ComboBox<String> regionFilterCombo;
    @FXML private ComboBox<String> categorieFilterCombo;

    // ScrollPanes
    @FXML private ScrollPane listViewScroll;
    @FXML private ScrollPane detailViewScroll;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateShortFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Patterns de validation
    private static final Pattern TITLE_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s\\-']{3,50}$");
    private static final Pattern CONTENT_PATTERN = Pattern.compile("^[\\w\\s\\p{Punct}À-ÿ]{10,500}$");

    // MENTIONS : Popup pour l'auto-complétion
    private Popup suggestionsPopup;
    private ListView<Utilisateur> suggestionsListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadImages();
        initComboBoxes();
        loadUtilisateurs();
        attachListeners();
        setupValidationListeners();
        loadInitialData();
        showListView();
        updateFormButtons();
        clearAllErrors();
        setupMentionAutoComplete(); // MENTIONS
    }

    // MENTIONS : Configuration de l'auto-complétion
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

        // Listener sur le champ de texte
        detailNewCommentField.textProperty().addListener((obs, oldText, newText) -> {
            int caretPos = detailNewCommentField.getCaretPosition();
            if (caretPos > 0 && newText.charAt(caretPos - 1) == '@') {
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
        });
    }

    // MENTIONS : Trouver le mot contenant '@' à la position donnée
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

    // MENTIONS : Afficher la liste des utilisateurs suggérés
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

    // MENTIONS : Insérer la mention sélectionnée
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

    // MENTIONS : Détecter toutes les mentions @ dans un texte
    private List<Integer> detecterMentions(String texte) {
        List<Integer> mentionsIds = new ArrayList<>();
        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(texte);
        try {
            List<Utilisateur> users = utilisateurCRUD.afficher();
            while (matcher.find()) {
                String pseudo = matcher.group(1);
                for (Utilisateur u : users) {
                    String full = u.getPrenom() + u.getNom(); // sans espace
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

    // MENTIONS : Envoyer des notifications pour les mentions
    private void envoyerNotificationsMention(List<Integer> utilisateursIds, String type, String contenu, String lien) {
        for (int userId : utilisateursIds) {
            try {
                Notification notif = new Notification(userId, currentUser.getId(), type, contenu, lien);
                notificationCRUD.ajouter(notif);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Les autres méthodes (loadImages, initComboBoxes, loadUtilisateurs, attachListeners, validation, etc.) restent inchangées.
    // Pour gagner de la place, je ne répète pas tout le code précédent ici, mais il doit être présent.
    // Vous devez conserver l'intégralité de votre contrôleur existant et y ajouter ces nouvelles méthodes.
    // Si vous souhaitez le fichier complet, je peux vous le fournir, mais il est extrêmement long.
    // Je vais plutôt indiquer les endroits où modifier les méthodes existantes.

    // NOTE : La méthode ajouterCommentaireDetail() doit être modifiée pour inclure la détection et l'envoi de notifications.
    // Voici sa nouvelle version :

    @FXML
    private void ajouterCommentaireDetail() {
        if (displayedDetailBlog == null) return;
        if (currentUser == null) {
            detailStatusLabel.setText("❌ Vous devez être connecté.");
            return;
        }
        String contenu = detailNewCommentField.getText();
        if (contenu == null || contenu.trim().isEmpty()) {
            detailStatusLabel.setText("❌ Le commentaire ne peut pas être vide.");
            return;
        }

        // Détecter les mentions
        List<Integer> mentionsIds = detecterMentions(contenu);

        Commentaire c = new Commentaire();
        c.setContenu(contenu.trim());
        c.setUtilisateur(currentUser.getId());
        c.setArticleId(displayedDetailBlog.getId());

        try {
            commentaireCRUD.ajouter(c);

            // Envoyer les notifications
            if (!mentionsIds.isEmpty()) {
                String lien = "/article/" + displayedDetailBlog.getId() + "#commentaire-" + c.getId();
                String message = currentUser.getPrenom() + " " + currentUser.getNom() +
                        " vous a mentionné dans un commentaire";
                envoyerNotificationsMention(mentionsIds, "mention", message, lien);
            }

            detailNewCommentField.clear();
            afficherCommentairesDetail();
            detailStatusLabel.setText("✅ Commentaire ajouté.");
        } catch (SQLException e) {
            detailStatusLabel.setText("❌ Erreur : " + e.getMessage());
        }
    }

    // Toutes les autres méthodes (createBlogCard, showDetailView, etc.) restent identiques.
    // Assurez-vous d'avoir importé les nouvelles classes (Notification, NotificationCRUD, Pattern, Matcher, KeyCode, Popup, ListView...)
}