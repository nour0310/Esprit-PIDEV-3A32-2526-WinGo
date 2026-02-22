package Controlles;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

public class AccueilFX {

    @FXML private BorderPane mainContent;
    @FXML private Button menuToggleBtn;

    // Track the state of the sidebar
    private boolean isMenuOpen = false;

    @FXML
    public void initialize() {
        // Set up the click event for the hamburger button
        menuToggleBtn.setOnAction(e -> toggleMenu());
    }

    private void toggleMenu() {
        // Create a smooth animation lasting 300 milliseconds
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), mainContent);

        if (isMenuOpen) {
            // Slide back to original position
            transition.setToX(0);
        } else {
            // Slide to the right by 250 pixels (the width of the sidebar)
            transition.setToX(250);
        }

        transition.play();
        isMenuOpen = !isMenuOpen;
    }
}