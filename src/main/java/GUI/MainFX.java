package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        TabPane tabPane = new TabPane();

        Tab eventTab = new Tab("Events");
        eventTab.setContent(FXMLLoader.load(getClass().getResource("/EventForm.fxml")));
        eventTab.setClosable(false);

        Tab participationTab = new Tab("Participations");
        participationTab.setContent(FXMLLoader.load(getClass().getResource("/AJOUTER PARTICIPATION.fxml")));
        participationTab.setClosable(false);

        tabPane.getTabs().addAll(eventTab, participationTab);

        Scene scene = new Scene(tabPane, 900, 600);
        stage.setTitle("Event Management");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
