package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Services.BlogCRUD;
import Services.CommentaireCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import java.sql.SQLException;
import java.util.List;

public class BlogController {

    @FXML private TextField titreField, auteurField, imageField, searchField;
    @FXML private TextArea contenuField;
    @FXML private ComboBox<String> regionCombo, categorieCombo;
    @FXML private FlowPane articlesFlowPane, commentairesFlowPane;
    @FXML private Label selectedDestinationLabel, selectedDestinationMeta, statsLabel;

    private BlogCRUD blogCRUD = new BlogCRUD();
    private CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private ObservableList<Blog> blogList = FXCollections.observableArrayList();
    private Blog selectedBlog = null;

    @FXML
    public void initialize() {
        regionCombo.getItems().addAll("Nord", "Sud", "Centre", "Djerba", "Hammamet", "Tozeur", "Douz", "Sousse", "Tunis", "Carthage", "Tabarka", "Bizerte");
        categorieCombo.getItems().addAll("Voyage", "Histoire", "Gastronomie", "Culture", "Aventure", "Plage", "Désert", "Montagne", "Famille");
        regionCombo.setValue("Djerba");
        categorieCombo.setValue("Voyage");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterBlogs(newVal));

        loadBlogs();
    }

    private void loadBlogs() {
        try {
            blogList.setAll(blogCRUD.afficher());
            displayBlogCards();
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayBlogCards() {
        articlesFlowPane.getChildren().clear();
        for (Blog b : blogList) {
            Label label = new Label(b.getTitre() + " (" + b.getRegion() + ")");
            label.setStyle("-fx-border-color:#333;-fx-padding:5;");
            label.setOnMouseClicked(e -> selectBlog(b));
            articlesFlowPane.getChildren().add(label);
        }
    }

    private void selectBlog(Blog b) {
        selectedBlog = b;
        selectedDestinationLabel.setText(b.getTitre());
        selectedDestinationMeta.setText(b.getRegion() + " · " + b.getAuteur());
        loadComments(b);
    }

    private void loadComments(Blog b) {
        commentairesFlowPane.getChildren().clear();
        try {
            List<Commentaire> comments = commentaireCRUD.getCommentsByBlogId(b.getId_article());
            for (Commentaire c : comments) {
                Label lbl = new Label(c.getUtilisateur() + ": " + c.getContenu());
                lbl.setStyle("-fx-background-color:#f0f0f0;-fx-padding:5;");
                commentairesFlowPane.getChildren().add(lbl);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterBlogs(String keyword) {
        articlesFlowPane.getChildren().clear();
        for (Blog b : blogList) {
            if (b.getTitre().toLowerCase().contains(keyword.toLowerCase())) {
                Label label = new Label(b.getTitre() + " (" + b.getRegion() + ")");
                label.setStyle("-fx-border-color:#333;-fx-padding:5;");
                label.setOnMouseClicked(e -> selectBlog(b));
                articlesFlowPane.getChildren().add(label);
            }
        }
    }

    @FXML
    private void handleCreateBlog() {
        try {
            Blog b = new Blog(titreField.getText(), contenuField.getText(), imageField.getText(), auteurField.getText(),
                    regionCombo.getValue(), categorieCombo.getValue());
            blogCRUD.ajouter(b);
            loadBlogs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateBlog() {
        if (selectedBlog == null) return;
        try {
            selectedBlog.setTitre(titreField.getText());
            selectedBlog.setContenu(contenuField.getText());
            selectedBlog.setImage(imageField.getText());
            selectedBlog.setAuteur(auteurField.getText());
            selectedBlog.setRegion(regionCombo.getValue());
            selectedBlog.setCategorie(categorieCombo.getValue());
            blogCRUD.modifier(selectedBlog);
            loadBlogs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteBlog() {
        if (selectedBlog == null) return;
        try {
            blogCRUD.supprimer(selectedBlog.getId_article());
            selectedBlog = null;
            loadBlogs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddComment() {
        if (selectedBlog == null) return;
        try {
            Commentaire c = new Commentaire(newCommentField.getText(), new java.util.Date(),
                    selectedBlog.getId_article(), 1); // 1 = utilisateur fictif pour test
            commentaireCRUD.ajouter(c);
            loadComments(selectedBlog);
            newCommentField.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStats() {
        statsLabel.setText(blogList.size() + " récits");
    }

    @FXML private TextField newCommentField;
}