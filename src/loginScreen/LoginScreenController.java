package loginScreen;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginScreenController {

    @FXML Button signInButton;
    @FXML TextField userNameTextField;
    @FXML PasswordField passwordField;

    private Connection connection;

    private HashMap<HashMap<String, String>, String> mUsersHashMap; //Required?

    public LoginScreenController() {
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
    }

    @FXML protected void signInUser(ActionEvent actionEvent) throws SQLException {
        System.out.println(userNameTextField.getText() + " " + passwordField.getText());

        if(checkIfUserExists(userNameTextField.getText())) {
            if (passwordField.getText().equals(decrypt(getEncryptedUserPassword(userNameTextField.getText()), "ThisI0s16BitPass"))) {
                openMainWindow();
                System.out.println("Successful Sign In.");
            }

            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter the correct credentials.", ButtonType.OK);
                alert.showAndWait();
            }
        }

        else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Couldn't find user " + userNameTextField.getText() + ".", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML protected void createUserHandle(ActionEvent actionEvent) {
        try {
//            FXMLLoader fxmlLoader = new FXMLLoader();
//            fxmlLoader.setLocation(getClass().getResource("CreateUser.fxml"));
            Parent root = FXMLLoader.load(getClass().getResource("../createUserScreen/CreateUser.fxml"));
            /*
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            Scene scene = new Scene(root, 600, 500);
            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Failed to create new Window.", e);
        }
    }

    private void openMainWindow() {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("../MainScreen/MainScreen.fxml"));
            stage.setTitle("FDP MANAGEMENT");
            stage.setScene(new Scene(root));
            userNameTextField.getScene().getWindow().hide(); //Hide the login Screen
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkIfUserExists(String username) throws SQLException {
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT * FROM memberdetails");

        while (resultSet.next()) {
            if (resultSet.getString("username").equals(username)) return true;
        }

        return false;
    }

    private byte[] getEncryptedUserPassword(String username) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT password FROM memberdetails WHERE username=?");
        preparedStatement.setString(1, username);

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            return encrypt(resultSet.getString("password"), "ThisI0s16BitPass");
        }
        return null;
    }

    //Helper Methods.
    private static byte[] encrypt(String message, String key) {
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            return cipher.doFinal(message.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String decrypt(byte[] encrypted, String key) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES");
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            return new String(cipher.doFinal(encrypted));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }



}
