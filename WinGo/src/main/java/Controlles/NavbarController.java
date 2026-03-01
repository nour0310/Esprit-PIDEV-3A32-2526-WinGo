package Controlles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class NavbarController implements Initializable {

    @FXML
    private HBox navbarRoot;

    @FXML
    private Button adminDashboardBtn;

    @FXML
    private Label notificationBadge;

    @FXML
    private StackPane notificationStack;

    @FXML
    private TextField navbarSearchField;

    @FXML
    private Button navbarSearchBtn;

    @FXML
    private HBox searchBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkAdminStatus();
    }

    private void checkAdminStatus() {
        if (Utils.Session.getCurrentUser() != null) {
            String role = Utils.Session.getCurrentUser().getType();
            if ("admin".equalsIgnoreCase(role)) {
                adminDashboardBtn.setVisible(true);
                adminDashboardBtn.setManaged(true);
            }
        }
    }

    @FXML
    public void goToHome(javafx.event.ActionEvent event) {
        loadPage("/Home.fxml");
    }

    @FXML
    public void goToBlogs(javafx.event.ActionEvent event) {
        loadPage("/Blogs.fxml");
    }

    @FXML
    public void goToSupport(javafx.event.ActionEvent event) {
        loadPage("/Support.fxml");
    }

    @FXML
    public void goToAdminDashboard(javafx.event.ActionEvent event) {
        loadPage("/BackOffice.fxml");
    }

    @FXML
    public void handleLogout(javafx.event.ActionEvent event) {
        Utils.Session.clear();
        loadPage("/Login.fxml");
    }

    public TextField getNavbarSearchField() {
        return navbarSearchField;
    }

    public Button getNavbarSearchBtn() {
        return navbarSearchBtn;
    }

    public HBox getSearchBar() {
        return searchBar;
    }

    public Button getNotificationBtn() {
        return (Button) notificationStack.getChildren().get(0);
    }

    public Label getNotificationBadge() {
        return notificationBadge;
    }

    private void loadPage(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) navbarRoot.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
