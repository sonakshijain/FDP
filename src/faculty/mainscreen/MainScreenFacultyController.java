package faculty.mainscreen;

import com.jfoenix.controls.JFXButton;
import faculty.fragments.submitfdprequestsview.SubmitFDPRequestController;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import util.ConnectionUtils;
import util.DialogUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainScreenFacultyController implements Initializable {

    @FXML private BorderPane root;
    @FXML private StackPane left;
    @FXML private JFXButton addFDPButton;
    @FXML private JFXButton allFDPsButton;
    @FXML private JFXButton updateCredButton;
    @FXML private StackPane mainStackPane;
    @FXML private AnchorPane dataView;

    private Connection mConnection;

    //Fragments
    private StackPane submitFDPRequestView;
    private int mFacultyID;

    //Controllers
    SubmitFDPRequestController mSubmitFDPRequestController;

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

    }

    public void openSubmitFDPView(ActionEvent event) {
        setContent(submitFDPRequestView);
    }

    public void openUpdateCredentialsFacultyView(ActionEvent event) {

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fragments/submitfdprequestsview/submitFDPRequest.fxml"));
            submitFDPRequestView = loader.load();
            mSubmitFDPRequestController = loader.getController();


//            addFDPAttendanceView = FXMLLoader.load(getClass().getResource("../registerUserScreen/RegisterUserScreen.fxml"));
//            seeAllFDPsView = FXMLLoader.load(getClass().getResource("..
//            updateCredentialsView = FXMLLoader.load(getClass().getResource("../updateCredentialsScreen/UpdateCredentials.fxml"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addFDPAttendanceView(ActionEvent actionEvent) {
    }

    public void setFacultyID(int facultyID) {
        mFacultyID = facultyID;
        System.out.println("MSFC setting in SFRC value mFacultyID = " + mFacultyID);

        mSubmitFDPRequestController.setFacultyID(mFacultyID);
    }
}
