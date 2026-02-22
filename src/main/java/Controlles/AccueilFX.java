package Controlles;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.io.IOException;

public class AccueilFX {

    @FXML private BorderPane mainContent;
    @FXML private Button menuToggleBtn;

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

    // ✅ Ouvre Produit.fxml au centre
    @FXML
    private void goProducts() {
        try {
            Parent produitsPage = FXMLLoader.load(getClass().getResource("/Produits.fxml"));
            mainContent.setCenter(produitsPage);

            // optionnel: fermer le menu après clic
            if (isMenuOpen) toggleMenu();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}