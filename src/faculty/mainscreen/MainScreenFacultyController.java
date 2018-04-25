package faculty.mainscreen;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import common.updatecredentials.UpdateCredentialsController;
import faculty.fragments.allfdpscreen.FacultyAllFDPScreenController;
import faculty.fragments.submitfdprequestsview.SubmitFDPRequestController;
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
import util.ConnectionUtils;
import util.DialogUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

@SuppressWarnings("Duplicates")
public class MainScreenFacultyController implements Initializable {

    @FXML private BorderPane root;
    @FXML private StackPane left;
    @FXML private JFXButton addFDPButton;
    @FXML private JFXButton allFDPsButton;
    @FXML private JFXButton updateCredButton;
    @FXML private StackPane mainStackPane;
    @FXML private AnchorPane dataView;
    UpdateCredentialsController mUpdateCredentialsController;
    FacultyAllFDPScreenController mAllFDPScreenController;

    private Connection mConnection;

    //Fragments
    private StackPane submitFDPRequestView;
    @FXML
    private Label welcomeLabel;
    private String mCurrentUser;

    //Controllers
    SubmitFDPRequestController mSubmitFDPRequestController;
    private StackPane updateCredentialsView;
    private StackPane allFDPScreen;
    private int mFacultyID;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            mConnection = ConnectionUtils.getDBConnection();
        } catch (SQLException e) {
            DialogUtils.createPresetDialog(mainStackPane, DialogUtils.ERROR, e.getMessage());
        }

        handleLoad();

        setContent(submitFDPRequestView);
    }

    public void openAllFDPsView(ActionEvent event) {
        setContent(allFDPScreen);
    }

    public void openSubmitFDPView(ActionEvent event) {
        setContent(submitFDPRequestView);
    }

    public void openUpdateCredentialsFacultyView(ActionEvent event) {
        setContent(updateCredentialsView);
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
            FXMLLoader submitFdpLoader = new FXMLLoader(getClass().getResource("../fragments/submitfdprequestsview/submitFDPRequest.fxml"));
            submitFDPRequestView = submitFdpLoader.load();
            mSubmitFDPRequestController = submitFdpLoader.getController();

            FXMLLoader updateCredLoader = new FXMLLoader(getClass().getResource("../../common/updatecredentials/UpdateCredentialsView.fxml"));
            updateCredentialsView = updateCredLoader.load();
            mUpdateCredentialsController = updateCredLoader.getController();

            FXMLLoader allFDPLoader = new FXMLLoader(getClass().getResource("../fragments/allfdpscreen/FacultyAllFDPScreen.fxml"));
            allFDPScreen = allFDPLoader.load();
            mAllFDPScreenController = allFDPLoader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFacultyID(int facultyID) {
        mFacultyID = facultyID;
        System.out.println("MSFC setting in SFRC value mFacultyID = " + mFacultyID);

        mSubmitFDPRequestController.setFacultyID(facultyID);
        mUpdateCredentialsController.setFacultyID(facultyID);
        mAllFDPScreenController.setFacultyID(facultyID);
    }

    public void setCurrentUser(String name) {
        mCurrentUser = name;
        welcomeLabel.setText("Welcome, " + mCurrentUser);
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
}
