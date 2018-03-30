package admin.mainscreen;

import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import admin.fragments.registeruserview.RegisterUserController;
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

    @FXML RegisterUserController registerUserController;

    private AnchorPane dbView;
    private AnchorPane registrationView;
    private AnchorPane updateCredentialsView;

    private Connection connection;

    private double xOffset = 0;
    private double yOffset = 0;
    private boolean firstRun = true;

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

            //FIXME: GETTING CONTROLLER ISSUE
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("../registerUserScreen/RegisterUserScreen.fxml"));
//            Parent root = loader.load();
//            registerUserController = loader.getController();
//            registerUserController.init(this);

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
}


