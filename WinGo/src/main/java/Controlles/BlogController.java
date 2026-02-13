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

    @FXML private FlowPane articlesFlowPane;
    @FXML private FlowPane commentairesFlowPane;

    @FXML private TextField titreField;
    @FXML private TextArea contenuField;
    @FXML private TextField imageField;
    @FXML private TextField auteurField; // ID utilisateur
    @FXML private ComboBox<String> regionCombo;
    @FXML private ComboBox<String> categorieCombo;

    @FXML private TextField newCommentField;
    @FXML private TextField commentUtilisateurField; // ID utilisateur
    @FXML private Label selectedDestinationLabel;
    @FXML private Label selectedDestinationMeta;

    @FXML private TextField searchField;
    @FXML private Label statsLabel;

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
        regionCombo.getItems().addAll("Nord","Sud","Centre","Djerba","Hammamet","Tozeur","Douz","Sousse","Tunis");
        categorieCombo.getItems().addAll("Voyage","Histoire","Gastronomie","Culture","Aventure");
        regionCombo.setValue("Djerba");
        categorieCombo.setValue("Voyage");
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs,o,n)->filterBlogs(n));
    }

    private void loadBlogs() {
        try {
            blogList.setAll(blogCRUD.afficher());
            displayBlogCards();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayBlogCards() {
        articlesFlowPane.getChildren().clear();
        for (Blog blog : blogList) {
            articlesFlowPane.getChildren().add(createBlogCard(blog));
        }
    }

    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:white;-fx-padding:15;-fx-background-radius:10;");
        card.setPrefWidth(250);

        Label title = new Label(blog.getTitre());
        Label author = new Label("Auteur ID: " + blog.getAuteur());
        Label region = new Label("📍 " + blog.getRegion());

        Button commentBtn = new Button("💬");
        commentBtn.setOnAction(e -> selectBlogForComments(blog));

        card.getChildren().addAll(title, author, region, commentBtn);
        return card;
    }

    private void selectBlogForComments(Blog blog) {
        selectedBlog = blog;
        selectedDestinationLabel.setText(blog.getTitre());
        selectedDestinationMeta.setText("Auteur #" + blog.getAuteur());
        loadCommentsForBlog(blog);
    }

    private void loadCommentsForBlog(Blog blog) {
        commentairesFlowPane.getChildren().clear();
        try {
            List<Commentaire> comments =
                    commentaireCRUD.getCommentsByBlogId(blog.getId());

            for (Commentaire c : comments) {
                commentairesFlowPane.getChildren().add(createCommentCard(c));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createCommentCard(Commentaire c) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color:#F5F5F5;-fx-padding:10;-fx-background-radius:8;");

        Label user = new Label("Utilisateur #" + c.getUtilisateur());
        Label date = new Label(new SimpleDateFormat("dd/MM/yyyy").format(c.getDateCommentaire()));
        Label content = new Label(c.getContenu());

        card.getChildren().addAll(user, date, content);
        return card;
    }

    @FXML
    private void handleCreateBlog() {
        try {
            int auteurId = Integer.parseInt(auteurField.getText().trim());

            Blog blog = new Blog(
                    titreField.getText(),
                    contenuField.getText(),
                    imageField.getText(),
                    auteurId,
                    regionCombo.getValue(),
                    categorieCombo.getValue()
            );

            blogCRUD.ajouter(blog);
            loadBlogs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddComment() {
        try {
            int userId = Integer.parseInt(commentUtilisateurField.getText().trim());

            Commentaire c = new Commentaire(
                    newCommentField.getText(),
                    new Date(),
                    selectedBlog.getId(),
                    userId
            );

            commentaireCRUD.ajouter(c);
            loadCommentsForBlog(selectedBlog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterBlogs(String keyword) {
        List<Blog> filtered = blogList.stream()
                .filter(b -> b.getTitre().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        articlesFlowPane.getChildren().clear();
        filtered.forEach(b -> articlesFlowPane.getChildren().add(createBlogCard(b)));
    }

    private void updateStats() {
        try {
            int total = 0;
            for (Blog b : blogCRUD.afficher()) {
                total += commentaireCRUD.getCommentsByBlogId(b.getId()).size();
            }
            statsLabel.setText(blogList.size()+" blogs · "+total+" commentaires");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}