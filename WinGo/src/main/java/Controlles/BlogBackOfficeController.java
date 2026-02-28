package Controlles;

import Entites.Blog;
import Services.BlogCRUD;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class BlogBackOfficeController implements Initializable {

    // Service
    private final BlogCRUD blogCRUD = new BlogCRUD();

    // UI Elements
    @FXML private TableView<Blog> tableBlogs;
    @FXML private TableColumn<Blog, Integer> colId;
    @FXML private TableColumn<Blog, String> colTitre;
    @FXML private TableColumn<Blog, String> colAuteur;
    @FXML private TableColumn<Blog, String> colDate;
    @FXML private TableColumn<Blog, String> colRegion;
    @FXML private TableColumn<Blog, String> colCategorie;

    @FXML private Label totalBlogsLabel;
    @FXML private Label totalCommentsLabel; // optional later if you load stats
    @FXML private Label statusLabel;
    @FXML private Label adminUserLabel;

    @FXML private TextField idField;
    @FXML private TextField titreField;
    @FXML private ComboBox<String> regionField;
    @FXML private ComboBox<String> categorieField;
    @FXML private TextArea contenuField;
    @FXML private TextField imageField;
    @FXML private TextField searchField;

    private ObservableList<Blog> masterData = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initComboBoxes();
        setupTableColumns();
        setupTableSelection();
        loadData();
        
        // Simulating the connected admin for now
        adminUserLabel.setText("Connecté en tant que: Administrateur");
    }

    private void initComboBoxes() {
        regionField.setItems(FXCollections.observableArrayList(
                "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                "Jendouba", "Kairouan", "Kasserine", "Kébili", "Le Kef", "Mahdia",
                "La Manouba", "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid",
                "Siliana", "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        ));
        categorieField.setItems(FXCollections.observableArrayList(
                "Plage", "Désert", "Montagne", "Culture", "Bien-être",
                "Événements", "Gastronomie", "Aventure", "Nature", "Histoire"
        ));
    }

    private void setupTableColumns() {
        // ID, Titre, Auteur, Region, Category are handled in FXML via PropertyValueFactory
        // We handle Date specifically to format it
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDatePublication() != null) {
                return new SimpleStringProperty(cellData.getValue().getDatePublication().format(dateFormatter));
            } else {
                return new SimpleStringProperty("");
            }
        });
    }

    private void setupTableSelection() {
        tableBlogs.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                remplirFormulaire(newSelection);
            }
        });
    }

    private void loadData() {
        try {
            List<Blog> blogs = blogCRUD.afficher();
            masterData.clear();
            masterData.addAll(blogs);
            tableBlogs.setItems(masterData);
            totalBlogsLabel.setText(String.valueOf(masterData.size()));
        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des données", e.getMessage());
        }
    }

    private void remplirFormulaire(Blog b) {
        idField.setText(String.valueOf(b.getId()));
        titreField.setText(b.getTitre());
        regionField.setValue(b.getRegion());
        categorieField.setValue(b.getCategorie());
        contenuField.setText(b.getContenu());
        imageField.setText(b.getImage());
    }

    @FXML
    private void clearForm() {
        tableBlogs.getSelectionModel().clearSelection();
        idField.setText("Auto");
        titreField.clear();
        regionField.setValue(null);
        categorieField.setValue(null);
        contenuField.clear();
        imageField.clear();
        statusLabel.setText("Formulaire vidé. Prêt.");
        statusLabel.setStyle("-fx-text-fill: #64748B;");
    }

    @FXML
    private void ajouterBlog() {
        if (!validerChamps()) return;

        // Using user ID 1 as mock connected Admin/User adding from back office
        Blog newBlog = new Blog(
                titreField.getText(),
                contenuField.getText(),
                1, // auteur (int)
                imageField.getText(), // image (String)
                regionField.getValue(),
                categorieField.getValue()
        );

        try {
            blogCRUD.ajouter(newBlog);
            afficherSucces("Article ajouté avec succès !");
            loadData();
            clearForm();
        } catch (SQLException e) {
            afficherErreur("Erreur lors de l'ajout", e.getMessage());
        }
    }

    @FXML
    private void modifierBlog() {
        Blog selected = tableBlogs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Sélection requise", "Veuillez sélectionner un article dans le tableau.");
            return;
        }

        if (!validerChamps()) return;

        selected.setTitre(titreField.getText());
        selected.setContenu(contenuField.getText());
        selected.setRegion(regionField.getValue());
        selected.setCategorie(categorieField.getValue());
        selected.setImage(imageField.getText());

        try {
            blogCRUD.modifier(selected);
            afficherSucces("Article modifié avec succès !");
            loadData();
            clearForm();
        } catch (SQLException e) {
            afficherErreur("Erreur lors de la modification", e.getMessage());
        }
    }

    @FXML
    private void supprimerBlog() {
        Blog selected = tableBlogs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Sélection requise", "Veuillez sélectionner un article dans le tableau.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression définitive");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'article #" + selected.getId() + " ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    blogCRUD.supprimer(selected.getId());
                    afficherSucces("Article supprimé !");
                    loadData();
                    clearForm();
                } catch (SQLException e) {
                    afficherErreur("Erreur de suppression", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void rechercherBlog() {
        String filterName = searchField.getText();
        if (filterName == null || filterName.isEmpty()) {
            tableBlogs.setItems(masterData);
            return;
        }
        String lowerCaseFilter = filterName.toLowerCase();
        ObservableList<Blog> filteredList = FXCollections.observableArrayList();
        for (Blog b : masterData) {
            if (b.getTitre() != null && b.getTitre().toLowerCase().contains(lowerCaseFilter)) {
                filteredList.add(b);
            } else if (b.getAuteurNom() != null && b.getAuteurNom().toLowerCase().contains(lowerCaseFilter)) {
                filteredList.add(b);
            } else if (b.getRegion() != null && b.getRegion().toLowerCase().contains(lowerCaseFilter)) {
                filteredList.add(b);
            }
        }
        tableBlogs.setItems(filteredList);
    }

    @FXML
    private void goFrontOffice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blogs.fxml"));
            Stage stage = (Stage) adminUserLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Navigation impossible", "Erreur lors du chargement de la vue Front-Office.");
        }
    }

    private boolean validerChamps() {
        if (titreField.getText() == null || titreField.getText().trim().isEmpty()) {
            afficherErreur("Validation", "Le titre ne peut pas être vide.");
            return false;
        }
        if (contenuField.getText() == null || contenuField.getText().trim().isEmpty()) {
            afficherErreur("Validation", "Le contenu ne peut pas être vide.");
            return false;
        }
        if (regionField.getValue() == null) {
            afficherErreur("Validation", "Veuillez sélectionner une région.");
            return false;
        }
        if (categorieField.getValue() == null) {
            afficherErreur("Validation", "Veuillez sélectionner une catégorie.");
            return false;
        }
        return true;
    }

    private void afficherSucces(String message) {
        statusLabel.setText("✅ " + message);
        statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
    }

    private void afficherErreur(String header, String message) {
        statusLabel.setText("❌ " + header + ": " + message);
        statusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
    }
}
