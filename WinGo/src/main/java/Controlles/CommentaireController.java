package Controlles;

import Entites.Commentaire;
import Services.CommentaireCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import java.sql.SQLException;
import java.util.List;

public class CommentaireController {

    @FXML private FlowPane commentairesFlowPane;
    private CommentaireCRUD commentaireCRUD = new CommentaireCRUD();

    public void loadComments(int blogId) {
        commentairesFlowPane.getChildren().clear();
        try {
            List<Commentaire> comments = commentaireCRUD.getCommentsByBlogId(blogId);
            for (Commentaire c : comments) {
                Label lbl = new Label(c.getUtilisateur() + ": " + c.getContenu());
                lbl.setStyle("-fx-background-color:#f0f0f0;-fx-padding:5;");
                commentairesFlowPane.getChildren().add(lbl);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}