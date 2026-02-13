package Controlles;

import Entites.Commentaire;
import Services.CommentaireCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class CommentaireController implements Initializable {

    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();

    private ObservableList<Commentaire> commentaireList = FXCollections.observableArrayList();
    private Commentaire selectedCommentaire = null;

    @FXML private TableView<Commentaire> commentairesTable;
    @FXML private TableColumn<Commentaire, Integer> colId;
    @FXML private TableColumn<Commentaire, String> colContenu;
    @FXML private TableColumn<Commentaire, String> colAuteurNom;  // nom affiché
    @FXML private TableColumn<Commentaire, Integer> colArticle;
    @FXML private TableColumn<Commentaire, String> colDate;

    @FXML private TextField searchField;
    @FXML private ComboBox<Integer> articleFilterCombo;
    @FXML private Label commentIdLabel;
    @FXML private TextField contenuField;
    @FXML private TextField utilisateurIdField;       // champ texte pour ID utilisateur
    @FXML private TextField articleIdField;
    @FXML private Label statusLabel;

    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button clearBtn;
    @FXML private Button refreshBtn;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
        setupListeners();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colAuteurNom.setCellValueFactory(new PropertyValueFactory<>("utilisateurNom"));
        colArticle.setCellValueFactory(new PropertyValueFactory<>("articleId"));
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateCommentaire() != null)
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateCommentaire().format(dateFormatter));
            else return new javafx.beans.property.SimpleStringProperty("");
        });

        commentairesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newSel) -> {
                    if (newSel != null) selectCommentaire(newSel);
                });
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, old, n) -> filterCommentaires());
        articleFilterCombo.setOnAction(e -> filterCommentaires());
        refreshBtn.setOnAction(e -> refreshData());
        clearBtn.setOnAction(e -> clearForm());
    }

    private void loadData() {
        try {
            commentaireList.clear();
            commentaireList.addAll(commentaireCRUD.afficher());
            commentairesTable.setItems(commentaireList);

            // Remplir le filtre combo avec les IDs d'articles uniques
            List<Integer> articles = commentaireList.stream()
                    .map(Commentaire::getArticleId)
                    .distinct()
                    .toList();
            articleFilterCombo.setItems(FXCollections.observableArrayList(articles));
            articleFilterCombo.getItems().add(0, null);
            articleFilterCombo.setValue(null);

            statusLabel.setText("Prêt - " + commentaireList.size() + " commentaires.");
        } catch (SQLException e) {
            showError("Erreur chargement", e.getMessage());
        }
    }

    private void filterCommentaires() {
        String search = searchField.getText().toLowerCase();
        Integer articleId = articleFilterCombo.getValue();

        List<Commentaire> filtered = commentaireList.stream()
                .filter(c -> search.isEmpty() ||
                        c.getContenu().toLowerCase().contains(search) ||
                        (c.getUtilisateurNom() != null && c.getUtilisateurNom().toLowerCase().contains(search)))
                .filter(c -> articleId == null || c.getArticleId() == articleId)
                .toList();

        commentairesTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void selectCommentaire(Commentaire c) {
        selectedCommentaire = c;
        commentIdLabel.setText(String.valueOf(c.getId()));
        contenuField.setText(c.getContenu());
        utilisateurIdField.setText(String.valueOf(c.getUtilisateur()));
        articleIdField.setText(String.valueOf(c.getArticleId()));
    }

    @FXML
    private void ajouterCommentaire() {
        if (!validateForm()) return;
        try {
            Commentaire c = new Commentaire();
            c.setContenu(contenuField.getText().trim());
            c.setUtilisateur(Integer.parseInt(utilisateurIdField.getText().trim()));
            c.setArticleId(Integer.parseInt(articleIdField.getText().trim()));

            commentaireCRUD.ajouter(c);
            refreshData();
            clearForm();
            showInfo("Commentaire ajouté.");
        } catch (SQLException | NumberFormatException e) {
            showError("Erreur ajout", e.getMessage());
        }
    }

    @FXML
    private void modifierCommentaire() {
        if (selectedCommentaire == null) {
            showWarning("Sélectionnez un commentaire.");
            return;
        }
        if (!validateForm()) return;
        try {
            selectedCommentaire.setContenu(contenuField.getText().trim());
            selectedCommentaire.setUtilisateur(Integer.parseInt(utilisateurIdField.getText().trim()));
            selectedCommentaire.setArticleId(Integer.parseInt(articleIdField.getText().trim()));

            commentaireCRUD.modifier(selectedCommentaire);
            refreshData();
            clearForm();
            showInfo("Commentaire modifié.");
        } catch (SQLException | NumberFormatException e) {
            showError("Erreur modification", e.getMessage());
        }
    }

    @FXML
    private void supprimerCommentaire() {
        if (selectedCommentaire == null) {
            showWarning("Sélectionnez un commentaire.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce commentaire ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    commentaireCRUD.supprimer(selectedCommentaire.getId());
                    refreshData();
                    clearForm();
                    showInfo("Commentaire supprimé.");
                } catch (SQLException e) {
                    showError("Erreur suppression", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void clearForm() {
        selectedCommentaire = null;
        commentIdLabel.setText("Nouveau");
        contenuField.clear();
        utilisateurIdField.clear();
        articleIdField.clear();
        commentairesTable.getSelectionModel().clearSelection();
    }

    private void refreshData() {
        loadData();
    }

    private boolean validateForm() {
        if (contenuField.getText().trim().isEmpty()) {
            showWarning("Contenu requis.");
            return false;
        }
        if (utilisateurIdField.getText().trim().isEmpty()) {
            showWarning("ID utilisateur requis.");
            return false;
        }
        if (articleIdField.getText().trim().isEmpty()) {
            showWarning("ID article requis.");
            return false;
        }
        try {
            Integer.parseInt(utilisateurIdField.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("L'ID utilisateur doit être un nombre.");
            return false;
        }
        try {
            Integer.parseInt(articleIdField.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("L'ID article doit être un nombre.");
            return false;
        }
        return true;
    }

    private void showInfo(String msg) { statusLabel.setText("✅ " + msg); }
    private void showWarning(String msg) { new Alert(Alert.AlertType.WARNING, msg).show(); }
    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setTitle(title);
        a.show();
    }
}