package Controlles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class HomeClientController {

    // Pas de TableView ici — c'est une page vitrine client

    @FXML
    public void initialize() {
        // Initialisation si nécessaire
    }

    @FXML
    private void allerDestinations(ActionEvent event) {
        // Navigation vers la page destinations
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("/Destinations.fxml"));
        // ...
    }

    @FXML
    private void allerBoutique(ActionEvent event) {
        // Navigation vers la page boutique
    }

    @FXML
    private void allerCircuits(ActionEvent event) {
        // Navigation vers la page circuits
    }

    @FXML
    private void allerReservations(ActionEvent event) {
        // Navigation vers mes réservations
    }

    @FXML
    private void rechercher(ActionEvent event) {
        // Logique de recherche
    }

    @FXML
    private void seConnecter(ActionEvent event) {
        // Ouvrir page login
    }

    @FXML
    private void sInscrire(ActionEvent event) {
        // Ouvrir page inscription
    }
}