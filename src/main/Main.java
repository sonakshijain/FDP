package main;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import rest.Mailer;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main extends Application {

    @FXML Button signInButton;
    @FXML TextField userNameTextField;
    @FXML PasswordField passwordField;

    private Connection connection;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../loginScreen/LoginScreen.fxml"));
        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root, 362, 500));
        primaryStage.setResizable(false);

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



        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
