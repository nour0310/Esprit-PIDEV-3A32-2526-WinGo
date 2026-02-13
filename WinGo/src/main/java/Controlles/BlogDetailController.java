package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Entites.Utilisateur;
import Services.CommentaireCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;  // ← IMPORT AJOUTÉ

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BlogDetailController implements Initializable {

    @FXML private Label titreLabel;
    @FXML private ImageView imageView;
    @FXML private Label auteurLabel;
    @FXML private Label dateLabel;
    @FXML private Label regionLabel;
    @FXML private Label categorieLabel;
    @FXML private Label contenuLabel;
    @FXML private FlowPane commentairesPane;
    @FXML private TextField newCommentField;
    @FXML private Button addCommentBtn;
    @FXML private Button backButton;
    @FXML private Label statusLabel;

    private Blog blog;
    private Utilisateur currentUser;
    private ObservableList<Commentaire> commentaireList;
    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setBlog(Blog blog, List<Commentaire> allComments, Utilisateur currentUser) {
        this.blog = blog;
        this.currentUser = currentUser;
        // Filtrer les commentaires de cet article
        this.commentaireList = FXCollections.observableArrayList(
                allComments.stream().filter(c -> c.getArticleId() == blog.getId()).toList()
        );
        afficherDetails();
        afficherCommentaires();
    }

    private void afficherDetails() {
        titreLabel.setText(blog.getTitre());
        auteurLabel.setText("👤 " + (blog.getAuteurNom() != null ? blog.getAuteurNom() : "Inconnu"));
        dateLabel.setText("📅 " + (blog.getDatePublication() != null ? blog.getDatePublication().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""));
        regionLabel.setText("📍 " + (blog.getRegion() != null ? blog.getRegion() : ""));
        categorieLabel.setText("🏷️ " + (blog.getCategorie() != null ? blog.getCategorie() : ""));
        contenuLabel.setText(blog.getContenu());

        try {
            if (blog.getImage() != null && !blog.getImage().isEmpty()) {
                Image img = new Image("file:" + blog.getImage(), true);
                imageView.setImage(img);
            } else {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                imageView.setImage(defaultImg);
            }
        } catch (Exception e) {
            try {
                Image defaultImg = new Image(getClass().getResourceAsStream("/default.jpg"));
                imageView.setImage(defaultImg);
            } catch (Exception ex) { }
        }
    }

    private void afficherCommentaires() {
        commentairesPane.getChildren().clear();
        for (Commentaire c : commentaireList) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(8));
            card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
            card.setPrefWidth(200);

            Label contenu = new Label(c.getContenu());
            contenu.setWrapText(true);
            contenu.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");

            Label auteur = new Label("👤 " + (c.getUtilisateurNom() != null ? c.getUtilisateurNom() : "Utilisateur " + c.getUtilisateur()));
            auteur.setStyle("-fx-text-fill: #b7472a; -fx-font-size: 11px;");

            Label date = new Label("📅 " + (c.getDateCommentaire() != null ? c.getDateCommentaire().format(dateFormatter) : ""));
            date.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

            // Boutons de modification/suppression pour l'utilisateur connecté
            if (currentUser != null && c.getUtilisateur() == currentUser.getId()) {
                HBox actions = new HBox(5);
                actions.setAlignment(Pos.CENTER_RIGHT);
                Button editBtn = new Button("✏️");
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 3;");
                editBtn.setOnAction(e -> modifierCommentaire(c));
                Button deleteBtn = new Button("🗑️");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3;");
                deleteBtn.setOnAction(e -> supprimerCommentaire(c));
                actions.getChildren().addAll(editBtn, deleteBtn);
                card.getChildren().addAll(contenu, auteur, date, actions);
            } else {
                card.getChildren().addAll(contenu, auteur, date);
            }

            commentairesPane.getChildren().add(card);
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
                    rafraichirCommentaires();
                    statusLabel.setText("✅ Commentaire modifié.");
                } catch (SQLException e) {
                    statusLabel.setText("❌ Erreur : " + e.getMessage());
                }
            }
        });
    }

    private void supprimerCommentaire(Commentaire commentaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce commentaire ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    commentaireCRUD.supprimer(commentaire.getId());
                    rafraichirCommentaires();
                    statusLabel.setText("✅ Commentaire supprimé.");
                } catch (SQLException e) {
                    statusLabel.setText("❌ Erreur : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void ajouterCommentaire() {
        if (currentUser == null) {
            statusLabel.setText("❌ Vous devez être connecté.");
            return;
        }
        String contenu = newCommentField.getText();
        if (contenu == null || contenu.trim().isEmpty()) {
            statusLabel.setText("❌ Le commentaire ne peut pas être vide.");
            return;
        }
        Commentaire c = new Commentaire();
        c.setContenu(contenu.trim());
        c.setUtilisateur(currentUser.getId());
        c.setArticleId(blog.getId());
        try {
            commentaireCRUD.ajouter(c);
            newCommentField.clear();
            rafraichirCommentaires();
            statusLabel.setText("✅ Commentaire ajouté.");
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur : " + e.getMessage());
        }
    }

    private void rafraichirCommentaires() {
        try {
            commentaireList.setAll(commentaireCRUD.getCommentsByArticle(blog.getId()));
            afficherCommentaires();
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur chargement commentaires.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backButton.setOnAction(e -> ((Stage) backButton.getScene().getWindow()).close());
        addCommentBtn.setOnAction(e -> ajouterCommentaire());
    }
}