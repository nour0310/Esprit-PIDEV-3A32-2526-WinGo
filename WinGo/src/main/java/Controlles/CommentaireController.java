package Controlles;

import Services.CommentaireCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.Date;

public class CommentaireController {

    @FXML private TextArea contenuField;
    @FXML private TextField utilisateurField;
    @FXML private TextField articleField;

    private CommentaireCRUD crud = new CommentaireCRUD();

    @FXML
    private void handleAdd() {
        try {
            Commentaire c = new Commentaire(
                    contenuField.getText(),
                    new Date(),
                    Integer.parseInt(articleField.getText()),
                    Integer.parseInt(utilisateurField.getText())
            );

            crud.ajouter(c);
            System.out.println("Commentaire ajouté !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}