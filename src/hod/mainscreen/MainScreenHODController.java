package hod.mainscreen;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import common.updatecredentials.UpdateCredentialsController;
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
import java.sql.SQLException;
import java.util.ResourceBundle;

@SuppressWarnings("Duplicates")
public class MainScreenHODController implements Initializable {

    @FXML private BorderPane root;
    @FXML private StackPane left;
    @FXML private JFXButton allFDPsButton;
    @FXML private JFXButton pendingFDPsButton;
    @FXML private JFXButton updateCredButton;
    @FXML
    private JFXButton logoutButton;
    @FXML private StackPane mainStackPane;
    @FXML private AnchorPane dataView;
    @FXML
    private Label welcomeLabel;

    private StackPane allPendingFDPsScreen;
    private StackPane allFDPScreen;

    private Connection mConnection;
    private StackPane updateCredentialsView;
    private UpdateCredentialsController mUpdateCredentialsController;

    private int mFacultyID;
    private String mCurrentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            mConnection = ConnectionUtils.getDBConnection();
        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        handleLoad();
        setContent(allPendingFDPsScreen);
    }

    private void setContent(Node node) {

        dataView.getChildren().clear();

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
            FXMLLoader l1 = new FXMLLoader(getClass().getResource("../fragments/allpendingfdpscreen/HODAllPendingFDPScreen.fxml"));
            allPendingFDPsScreen = l1.load();
//            mSubmitFDPRequestController = loader.getController();
//
            FXMLLoader l2 = new FXMLLoader(getClass().getResource("../fragments/allfdpscreen/HODAllFDPScreen.fxml"));
            allFDPScreen = l2.load();

            FXMLLoader updateCredLoader = new FXMLLoader(getClass().getResource("../../common/updatecredentials/UpdateCredentialsView.fxml"));
            updateCredentialsView = updateCredLoader.load();
            mUpdateCredentialsController = updateCredLoader.getController();


//            addFDPAttendanceView = FXMLLoader.load(getClass().getResource("../registerUserScreen/RegisterUserScreen.fxml"));
//            seeAllFDPsView = FXMLLoader.load(getClass().getResource("..
//            updateCredentialsView = FXMLLoader.load(getClass().getResource("../updateCredentialsScreen/UpdateCredentials.fxml"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openAllFDPView(ActionEvent event) {
        setContent(allFDPScreen);
    }

    public void openPendingFDPView(ActionEvent event) {
        setContent(allPendingFDPsScreen);
    }

    public void openUpdateCredentialsView(ActionEvent event) {
        setContent(updateCredentialsView);
    }

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

    public void setFacultyID(int facultyID) {
        mFacultyID = facultyID;

        mUpdateCredentialsController.setFacultyID(mFacultyID);
    }

    public void setCurrentUser(String currentUser) {
        mCurrentUser = currentUser;
        welcomeLabel.setText("Welcome, " + mCurrentUser);
    }
}
