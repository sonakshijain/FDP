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
import java.sql.SQLException;

public class Main extends Application {

    public static void main(String[] args) {

        launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        System.out.println("jjj");

        Parent root = FXMLLoader.load(getClass().getResource("../common/login/LoginScreen.fxml"));

        JFXDecorator decorator = new JFXDecorator(primaryStage, root);

        decorator.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        decorator.setTitle("LOGIN");

        Scene scene = new Scene(decorator);

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        try {
            Connection connection = ConnectionUtils.getDBConnection();
        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        primaryStage.show();
    }
}
