package Controlles;

import Entites.Blog;
import Entites.Commentaire;
import Services.BlogCRUD;
import Services.CommentaireCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import java.sql.SQLException;
import java.util.List;

public class BlogController {

    @FXML private TextField titreField, auteurField, imageField, searchField, regionField, categorieField;
    @FXML private TextArea contenuField;
    @FXML private FlowPane articlesFlowPane, commentairesFlowPane;
    @FXML private Label selectedDestinationLabel, selectedDestinationMeta, statsLabel;
    @FXML private TextField newCommentField;

    private BlogCRUD blogCRUD = new BlogCRUD();
    private CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private ObservableList<Blog> blogList = FXCollections.observableArrayList();
    private Blog selectedBlog = null;

    @FXML
    public void initialize() {
        // Initialisation des combos si nécessaire
        regionField.setText("Djerba");
        categorieField.setText("Voyage");

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
        titreField.setText(b.getTitre());
        contenuField.setText(b.getContenu());
        imageField.setText(b.getImage());
        auteurField.setText(b.getAuteur());
        regionField.setText(b.getRegion());
        categorieField.setText(b.getCategorie());

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
    private void ajouterBlog(ActionEvent event) {
        try {
            Blog b = new Blog(
                    titreField.getText(),
                    contenuField.getText(),
                    imageField.getText(),
                    auteurField.getText(),
                    regionField.getText(),
                    categorieField.getText()
            );
            blogCRUD.ajouter(b);
            loadBlogs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierBlog(ActionEvent event) {
        if (selectedBlog == null) return;
        try {
            selectedBlog.setTitre(titreField.getText());
            selectedBlog.setContenu(contenuField.getText());
            selectedBlog.setImage(imageField.getText());
            selectedBlog.setAuteur(auteurField.getText());
            selectedBlog.setRegion(regionField.getText());
            selectedBlog.setCategorie(categorieField.getText());
            blogCRUD.modifier(selectedBlog);
            loadBlogs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerBlog(ActionEvent event) {
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
    private void ajouterCommentaire(ActionEvent event) {
        if (selectedBlog == null) return;
        try {
            Commentaire c = new Commentaire(
                    newCommentField.getText(),
                    new java.util.Date(),
                    selectedBlog.getId_article(),
                    1 // utilisateur fictif
            );
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
}