package Controlles;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public class HomeTravelController {

    @FXML private BorderPane rootPane;
    @FXML private Button searchBtn;
    @FXML private Button dealCtaBtn;
    @FXML private Button expertBtn1;
    @FXML private Button expertBtn2;
    @FXML private Button navHomeBtn, navSearchBtn, navBookBtn, navFavBtn, navProfileBtn;

    @FXML
    public void initialize() {
        System.out.println("🔥 WIN GO TRAVEL - MODE TUNISIA VIBES ACTIVÉ");
    }

    @FXML
    private void onSearchClick(MouseEvent e) {
        System.out.println("🔍 Recherche: " + searchBtn.getScene().lookup(".search-field"));
    }

    @FXML
    private void onDealReserve(MouseEvent e) {
        System.out.println("🔥 OFFRE RÉSERVÉE: Djerba -30% | 189 DT");
    }

    @FXML
    private void onDiscoverTozeur(MouseEvent e) {
        System.out.println("🏜️ Découverte: Coucher de soleil à Tozeur");
    }

    @FXML
    private void onDiscoverCarthage(MouseEvent e) {
        System.out.println("🏛️ Découverte: Sites historiques de Carthage");
    }
}