package admin.mainscreen;

import admin.fragments.registeruserview.RegisterUserController;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.AlertUtils;
import util.ConnectionUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

public class MainScreenAdminController implements Initializable {
//
    @FXML private BorderPane root;
    @FXML private JFXButton viewDatabaseButton;
    @FXML private JFXButton registerUserButton;
    @FXML private JFXButton updateCredButton;
    @FXML private JFXButton closeButton;
    @FXML private AnchorPane dataView;
    @FXML private TextField labelView;
    @FXML private StackPane mainStackPane;
    @FXML
    private JFXButton logoutButton;
    @FXML
    private Label welcomeLabel;

    @FXML RegisterUserController registerUserController;

    private AnchorPane dbView;
    private AnchorPane registrationView;
    private AnchorPane updateCredentialsView;

    private Connection connection;

   @Override
    public void initialize(URL urlLocation, ResourceBundle resourceBundle) {
        connection = null;
        try {
            connection = ConnectionUtils.getDBConnection();
        } catch (Exception e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        //Cache all the pages
        handleLoad();

        setContent(dbView);

    }


    //Set selected node to a content holder
    private void setContent(Node node) {

        dataView.getChildren().clear();

        while (node == null) {}

        dataView.getChildren().add(node);

        AnchorPane.setBottomAnchor(node, 0D);
        AnchorPane.setTopAnchor(node, 0D);
        AnchorPane.setLeftAnchor(node, 0D);
        AnchorPane.setRightAnchor(node, 0D);

        FadeTransition ft = new FadeTransition(Duration.millis(500));
        ft.setNode(node);
        ft.setFromValue(0.1);
        ft.setToValue(1);
        ft.setCycleCount(1);
        ft.setAutoReverse(false);
        ft.play();
    }

    private void handleLoad() {
        try {
            dbView = FXMLLoader.load(getClass().getResource("../fragments/dbview/DBView.fxml"));
            registrationView = FXMLLoader.load(getClass().getResource("../fragments/registeruserview/RegisterUserScreen.fxml"));
            updateCredentialsView = FXMLLoader.load(getClass().getResource("../fragments/updatecredentialsview/UpdateCredentials.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openDBView(ActionEvent actionEvent) {
        setContent(dbView);
    }

    public void openRegisterView(ActionEvent actionEvent) {
        setContent(registrationView);
    }

    public void openUpdateCredentialsView(ActionEvent actionEvent) {
        setContent(updateCredentialsView);
    }

    public void updatePage(int i) {
        switch (i) {
            case 1: {
                setContent(dbView);
                break;
            }

            case 2: {
                setContent(registrationView);
                break;
            }

            case 3: {
                setContent(updateCredentialsView);
                break;
            }
        }
    }

    @SuppressWarnings("Duplicates")
    public void handleLogout(ActionEvent actionEvent) {
        Alert logoutAlert = new Alert(Alert.AlertType.WARNING, "Are you sure you want to log out?", ButtonType.YES, ButtonType.NO);
        logoutAlert.setHeaderText("Log out");
        logoutAlert.showAndWait();

        if (logoutAlert.getResult() == ButtonType.YES) {
            //open login window
            try {
                Stage primaryStage = new Stage();

                Parent root = FXMLLoader.load(getClass().getResource("../../common/login/LoginScreen.fxml"));

                JFXDecorator decorator = new JFXDecorator(primaryStage, root);

                decorator.getStylesheets().add(getClass().getResource("../../main/style.css").toExternalForm());

                decorator.setTitle("LOGIN");

                primaryStage.setScene(new Scene(decorator));
                primaryStage.setResizable(false);

                primaryStage.show();

            } catch (IOException e) {
                e.printStackTrace();

            }

            //close the current window
            root.getScene().getWindow().hide();
        }
    }

    public void setWelcomeLabel(String name) {
        welcomeLabel.setText("Welcome, " + name);
    }
}


