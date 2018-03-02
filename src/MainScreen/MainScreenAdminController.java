package MainScreen;

import com.jfoenix.controls.*;
import javafx.animation.FadeTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainScreenAdminController {

    @FXML private JFXButton viewDatabaseButton;
    @FXML private JFXButton registerUserButton;
    @FXML private JFXButton updateCredButton;
    @FXML private JFXButton closeButton;
    @FXML private AnchorPane dataView;

    private AnchorPane dbView; //TODO ADD MORE;

    private Connection connection;

    private double xOffset = 0;
    private double yOffset = 0;

    public MainScreenAdminController() {
            connection = null;
            try {
                connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/members", "postgres", "password");
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
                alert.setHeaderText("Couldn't connect to the database.");
                alert.setContentText(e.getMessage());
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.showAndWait();
                System.exit(1);
            }

        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("MainScreenAdmin.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage primaryStage = (Stage) viewDatabaseButton.getScene().getWindow();

            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            });
//            createPages();
        }

    //Set selected node to a content holder
    private void setNode(Node node) {
        dataView.getChildren().clear();
        dataView.getChildren().add((Node) node);

        FadeTransition ft = new FadeTransition(Duration.millis(1500));
        ft.setNode(node);
        ft.setFromValue(0.1);
        ft.setToValue(1);
        ft.setCycleCount(1);
        ft.setAutoReverse(false);
        ft.play();
    }

    //Load all fxml files to a cache for swapping
    private void createPages() {
        try {
            dbView = FXMLLoader.load(getClass().getResource("../dbViewFragment/DBView.fxml"));
//            list = FXMLLoader.load(getClass().getResource("/modules/Profile.fxml"));
//            add = FXMLLoader.load(getClass().getResource("/modules/Register.fxml"));

            //set up default node on page load
            setNode(dbView);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

    }


}
