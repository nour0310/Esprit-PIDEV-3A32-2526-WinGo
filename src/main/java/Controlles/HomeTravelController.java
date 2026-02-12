package Controlles;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.MouseEvent;

public class HomeTravelController {

    // MAIN PANEL
    @FXML private BorderPane mainPane;  // Changé de rootPane à mainPane

    // TOP BAR
    @FXML private TextField searchField;
    @FXML private Button filterBtn;

    // LEFT NAV
    @FXML private Button navHomeBtn;
    @FXML private Button navExploreBtn;
    @FXML private Button navDealsBtn;
    @FXML private Button navFavBtn;
    @FXML private Button navSettingsBtn;

    // HERO BUTTONS
    @FXML private Button dealsBtn;
    @FXML private Button mapBtn;
    @FXML private Button tripsBtn;

    // DEAL
    @FXML private Button reserveDealBtn;

    // FAVORITE BUTTONS
    @FXML private Button favSousseBtn;
    @FXML private Button favTunisBtn;
    @FXML private Button favTozeurBtn;
    @FXML private Button favHammametBtn;

    // BOTTOM NAV
    @FXML private Button bottomHomeBtn;
    @FXML private Button bottomExploreBtn;
    @FXML private Button bottomBookBtn;
    @FXML private Button bottomFavBtn;
    @FXML private Button bottomProfileBtn;

    @FXML
    public void initialize() {
        System.out.println("🔥 WinGo Travel - Glass Mode ACTIVATED");

        // Ajoute ici tes event handlers
    }

    @FXML
    private void onSearch() {
        System.out.println("🔍 Recherche: " + searchField.getText());
    }

    @FXML
    private void onFilter() {
        System.out.println("⚙ Filtres ouverts");
    }

    @FXML
    private void onReserveDeal() {
        System.out.println("🔥 Deal réservé - Djerba 189 TND");
    }
}