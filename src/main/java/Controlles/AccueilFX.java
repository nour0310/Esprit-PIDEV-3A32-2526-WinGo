package Controlles;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class AccueilFX {

    @FXML private BorderPane mainContent;
    @FXML private Button menuToggleBtn;
    @FXML private StackPane centerStack;   // ← le StackPane racine du center
    @FXML private VBox homeContent;        // ← la home page
    @FXML private StackPane dynamicContent; // ← zone produits

    private boolean isMenuOpen = false;

    @FXML
    public void initialize() {
        if (menuToggleBtn != null) {
            menuToggleBtn.setOnAction(e -> toggleMenu());
        }
    }

    private void toggleMenu() {
        if (mainContent == null) return;
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), mainContent);
        transition.setToX(isMenuOpen ? 0 : 250);
        transition.play();
        isMenuOpen = !isMenuOpen;
    }

    @FXML
    private void goProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produits.fxml"));
            Parent produitsPage = loader.load();

            // ✅ Passe le StackPane au controller pour que le panier s'affiche en overlay
            WinGoShopController shopCtrl = loader.getController();
            shopCtrl.setOverlayContainer(centerStack);

            // Affiche produits, cache home
            dynamicContent.getChildren().setAll(produitsPage);
            dynamicContent.setVisible(true);
            dynamicContent.setManaged(true);
            homeContent.setVisible(false);
            homeContent.setManaged(false);

            if (isMenuOpen) toggleMenu();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
