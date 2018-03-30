package main;

import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.AlertUtils;
import util.ConnectionUtils;

import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../common/login/LoginScreen.fxml"));

        JFXDecorator decorator = new JFXDecorator(primaryStage, root);

        decorator.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        decorator.setTitle("LOGIN");

        primaryStage.setScene(new Scene(decorator));
        primaryStage.setResizable(false);

        try {
            Connection connection = ConnectionUtils.getDBConnection();
        } catch (Exception e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
