package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Services.CommentaireCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class CommentaireController {

    @FXML private TableView<Commentaire> commentTable;
    @FXML private TableColumn<Commentaire, Integer> colId;
    @FXML private TableColumn<Commentaire, String> colUtilisateur;
    @FXML private TableColumn<Commentaire, String> colContenu;
    @FXML private TableColumn<Commentaire, Date> colDate;
    @FXML private TableColumn<Commentaire, Void> colActions;

    @FXML private TextField utilisateurField;
    @FXML private TextArea contenuField;
    @FXML private Label articleTitleLabel;
    @FXML private Label articleAuthorLabel;

    private CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private ObservableList<Commentaire> commentaireList = FXCollections.observableArrayList();
    private Blog currentBlog;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUtilisateur.setCellValueFactory(new PropertyValueFactory<>("utilisateur"));
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCommentaire"));

        colDate.setCellFactory(column -> new TableCell<Commentaire, Date>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(format.format(date));
                }
            }
        });

        addActionButtons();
        commentTable.setItems(commentaireList);
    }

    private void addActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑️");

            {
                deleteBtn.setStyle("-fx-background-color: #FF5400; -fx-text-fill: white; -fx-background-radius: 15; -fx-font-weight: 700; -fx-cursor: hand; -fx-padding: 8 15;");
                deleteBtn.setOnAction(event -> {
                    Commentaire comment = getTableView().getItems().get(getIndex());
                    handleDeleteComment(comment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
    }

    public void setBlog(Blog blog) {
        this.currentBlog = blog;
        if (blog != null) {
            articleTitleLabel.setText("📍 " + blog.getTitre());
            articleAuthorLabel.setText("Voyageur : " + blog.getAuteur() + " · " + blog.getRegion() + " · " + blog.getCategorie());
            loadCommentsForBlog(blog.getId());
        } else {
            articleTitleLabel.setText("📍 Aucune destination sélectionnée");
            articleAuthorLabel.setText("Sélectionnez un récit de voyage");
            commentaireList.clear();
        }
    }

    private void loadCommentsForBlog(int blogId) {
        try {
            commentaireList.clear();
            commentaireList.addAll(commentaireCRUD.getCommentsByBlogId(blogId));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "❌ Impossible de charger les avis");
        }
    }

    @FXML
    private void handleAddComment() {
        if (currentBlog == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "⚠️ Sélectionnez d'abord une destination");
            return;
        }

        if (utilisateurField.getText().isEmpty() || contenuField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "👤 Tous les champs sont obligatoires");
            return;
        }

        try {
            Commentaire comment = new Commentaire(
                    contenuField.getText().trim(),
                    new Date(),
                    currentBlog.getId(),
                    utilisateurField.getText().trim()
            );

            commentaireCRUD.ajouter(comment);
            loadCommentsForBlog(currentBlog.getId());
            clearFields();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Avis ajouté avec succès !");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "❌ Erreur lors de l'ajout de l'avis");
        }
    }

    private void handleDeleteComment(Commentaire comment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'avis");
        confirm.setContentText("Voulez-vous vraiment supprimer cet avis ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                commentaireCRUD.supprimer(comment.getId());
                commentaireList.remove(comment);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "🗑️ Avis supprimé");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "❌ Erreur lors de la suppression");
            }
        }
    }

    private void clearFields() {
        utilisateurField.clear();
        contenuField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #FF5400; -fx-border-width: 2; -fx-border-radius: 10;");

        alert.showAndWait();
    }
}