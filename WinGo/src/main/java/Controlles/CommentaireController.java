package Controlles;

import Entites.Commentaire;
import Services.CommentaireCRUD;  // Correction ici
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class CommentaireController implements Initializable {

    private final CommentaireCRUD commentaireService = new CommentaireCRUD();
    private ObservableList<Commentaire> commentaireList = FXCollections.observableArrayList();
    private Commentaire selectedCommentaire = null;

    @FXML private TableView<Commentaire> commentairesTable;
    @FXML private TableColumn<Commentaire, Integer> colId;
    @FXML private TableColumn<Commentaire, String> colContenu;
    @FXML private TableColumn<Commentaire, String> colAuteur;
    @FXML private TableColumn<Commentaire, Integer> colBlogId;
    @FXML private TableColumn<Commentaire, String> colDate;

    @FXML private TextField searchField;
    @FXML private ComboBox<Integer> blogFilterCombo;
    @FXML private Label totalCommentsLabel;

    @FXML private TextField contenuField;
    @FXML private TextField auteurField;
    @FXML private ComboBox<Integer> blogIdField;
    @FXML private Label commentaireIdLabel;

    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Button refreshBtn;

    @FXML private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setupTable();
            setupComboBoxes();
            loadData();
            setupListeners();
            statusLabel.setText("✅ Prêt - " + commentaireList.size() + " commentaires chargés");
        } catch (SQLException e) {
            showError("Erreur de chargement", e.getMessage());
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colAuteur.setCellValueFactory(new PropertyValueFactory<>("auteur"));
        colBlogId.setCellValueFactory(new PropertyValueFactory<>("blogId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        commentairesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) selectCommentaire(newSelection);
                });
    }

    private void setupComboBoxes() {
        // À remplir avec les IDs des blogs disponibles
        ObservableList<Integer> blogIds = FXCollections.observableArrayList();
        // Exemple : blogIds.addAll(1,2,3); À récupérer depuis BlogCRUD
        blogFilterCombo.setItems(blogIds);
        blogIdField.setItems(blogIds);
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, old, newVal) -> filterCommentaires());
        blogFilterCombo.setOnAction(e -> filterCommentaires());
        refreshBtn.setOnAction(e -> refreshData());
        clearBtn.setOnAction(e -> clearForm());
    }

    private void loadData() throws SQLException {
        commentaireList.clear();
        commentaireList.addAll(commentaireService.readAll());
        commentairesTable.setItems(commentaireList);
        totalCommentsLabel.setText(String.valueOf(commentaireList.size()));
    }

    private void filterCommentaires() {
        String searchText = searchField.getText().toLowerCase();
        Integer selectedBlogId = blogFilterCombo.getValue();
        List<Commentaire> filtered = commentaireList.stream()
                .filter(c -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                            c.getContenu().toLowerCase().contains(searchText) ||
                            c.getAuteur().toLowerCase().contains(searchText);
                    boolean matchesBlog = selectedBlogId == null || c.getBlogId() == selectedBlogId;
                    return matchesSearch && matchesBlog;
                })
                .toList();
        commentairesTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void selectCommentaire(Commentaire commentaire) {
        this.selectedCommentaire = commentaire;
        commentaireIdLabel.setText(String.valueOf(commentaire.getId()));
        contenuField.setText(commentaire.getContenu());
        auteurField.setText(commentaire.getAuteur());
        blogIdField.setValue(commentaire.getBlogId());
    }

    @FXML
    private void ajouterCommentaire() {
        try {
            if (!validateForm()) return;
            Commentaire comment = new Commentaire();
            comment.setContenu(contenuField.getText());
            comment.setAuteur(auteurField.getText());
            comment.setBlogId(blogIdField.getValue());
            commentaireService.create(comment);
            refreshData();
            clearForm();
            showSuccess("Commentaire ajouté!");
        } catch (SQLException e) {
            showError("Erreur d'ajout", e.getMessage());
        }
    }

    @FXML
    private void modifierCommentaire() {
        if (selectedCommentaire == null) {
            showWarning("Sélection requise", "Veuillez sélectionner un commentaire.");
            return;
        }
        try {
            if (!validateForm()) return;
            selectedCommentaire.setContenu(contenuField.getText());
            selectedCommentaire.setAuteur(auteurField.getText());
            selectedCommentaire.setBlogId(blogIdField.getValue());
            commentaireService.update(selectedCommentaire);
            refreshData();
            clearForm();
            showSuccess("Commentaire modifié!");
        } catch (SQLException e) {
            showError("Erreur de modification", e.getMessage());
        }
    }

    @FXML
    private void supprimerCommentaire() {
        if (selectedCommentaire == null) {
            showWarning("Sélection requise", "Veuillez sélectionner un commentaire.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le commentaire");
        confirm.setContentText("Êtes-vous sûr ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    commentaireService.delete(selectedCommentaire.getId());
                    refreshData();
                    clearForm();
                    showSuccess("Commentaire supprimé!");
                } catch (SQLException e) {
                    showError("Erreur de suppression", e.getMessage());
                }
            }
        });
    }

    private void refreshData() {
        try {
            loadData();
            filterCommentaires();
            statusLabel.setText("✅ Données actualisées");
        } catch (SQLException e) {
            showError("Erreur d'actualisation", e.getMessage());
        }
    }

    private void clearForm() {
        selectedCommentaire = null;
        commentaireIdLabel.setText("Nouveau");
        contenuField.clear();
        auteurField.clear();
        blogIdField.setValue(null);
        commentairesTable.getSelectionModel().clearSelection();
    }

    private boolean validateForm() {
        if (contenuField.getText() == null || contenuField.getText().trim().isEmpty()) {
            showWarning("Champ vide", "Le contenu est requis");
            return false;
        }
        if (auteurField.getText() == null || auteurField.getText().trim().isEmpty()) {
            showWarning("Champ vide", "L'auteur est requis");
            return false;
        }
        if (blogIdField.getValue() == null) {
            showWarning("Sélection requise", "Veuillez sélectionner un blog");
            return false;
        }
        return true;
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}