package createUserScreen;

import exception.PasswordContainsUsernameException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateUserController {

    @FXML TextField fullNameTF;
    @FXML TextField usernameTF;
    @FXML PasswordField passwordPF;
    @FXML PasswordField repeatPasswordPF;
    @FXML ProgressBar passwordStrengthMeter;
    @FXML Label passwordStrengthLabel;
    @FXML CheckBox upperCaseCheckBox;
    @FXML CheckBox lowerCaseCheckBox;
    @FXML CheckBox specialCharCheckBox;
    @FXML CheckBox numCharCheckBox;
    @FXML Label passwordsMatchLabel;
    @FXML TextField emailAddressTF;
    @FXML Label validEmailLabel;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private Connection connection;
    private boolean passwordsMatch = false;

    public CreateUserController() throws SQLException {
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

    public void createUser(ActionEvent actionEvent) {
        Statement selectStatement = null;
        PreparedStatement preparedStatement = null;

        boolean completedWithoutErrors = false;

        try {
            String name = fullNameTF.getText();
            String username = usernameTF.getText();
            String password = passwordPF.getText();
            String email = emailAddressTF.getText();

            if (name.equals("") || username.equals("") || password.equals("") || email.equals("")) {
                throw new RuntimeException("Please ensure that none of the fields are empty.");
            }

            if (passwordPF.getText().contains(username) || passwordPF.getText().contains(name)) {
                throw new PasswordContainsUsernameException();
            }

            if (!passwordsMatch) {
                throw new Exception("Passwords do not match.");
            }

            String query = "INSERT INTO memberdetails (name, username, password, email_address) VALUES (?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, email);

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Proceed " + name + "?", ButtonType.YES, ButtonType.NO);
            confirmAlert.setHeaderText("Are you sure you want to continue?");
            confirmAlert.setContentText("Please recheck the entered data.");
            confirmAlert.showAndWait();

            if (confirmAlert.getResult() == ButtonType.YES) {

                preparedStatement.execute();

                selectStatement = connection.createStatement();
                ResultSet resultSet = selectStatement.executeQuery("SELECT * FROM memberdetails;");

                while (resultSet.next()) {
                    System.out.println(resultSet.getString("name") + " " + resultSet.getString("username") + " " + resultSet.getString("password"));
                }

                completedWithoutErrors = true;

            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "An error occured. Do you want to check it?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION, e.getMessage(), ButtonType.OK);
                alert1.setHeaderText("DEBUG INFO:");
                alert1.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert1.showAndWait();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please ensure none of the fields are empty.", ButtonType.OK);
            alert.setHeaderText("Some Fields Empty.");
            alert.showAndWait();
        } catch (PasswordContainsUsernameException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
            alert.setHeaderText("Password can't contain the username/name.");
            alert.setContentText("Please change the password.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }

        if (completedWithoutErrors) {
            fullNameTF.getParent().getScene().getWindow().hide();
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "User created Successfully.", ButtonType.OK);
            successAlert.showAndWait();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Confirm E-mail?");
            alert.setContentText("Do you want to confirm the email address now?");
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                openEmailActivationWindow();
            }
        }

    }

    private void openEmailActivationWindow() {
        try {
//            FXMLLoader fxmlLoader = new FXMLLoader();
//            fxmlLoader.setLocation(getClass().getResource("CreateUser.fxml"));
            Parent root = FXMLLoader.load(getClass().getResource("../verifyEmailScreen/VerifyEmailScreen.fxml"));
            /*
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Verify Email Address");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Failed to create new Window.", e);
        }
    }

    public void validatePassword(KeyEvent keyEvent) {
        passwordStrengthMeter.setProgress(checkPasswordStrength(passwordPF.getText()) / 100);

        if (passwordStrengthMeter.getProgress() == 0) {
            numCharCheckBox.setSelected(false);
            specialCharCheckBox.setSelected(false);
            lowerCaseCheckBox.setSelected(false);
            upperCaseCheckBox.setSelected(false);
            passwordStrengthLabel.setText("Please enter a password.");
            passwordStrengthLabel.setTextFill(Color.web("#000000"));
        }

        if (passwordStrengthMeter.getProgress() == 0.25) {
            passwordStrengthLabel.setText("Password Strength: Weak");
            passwordStrengthLabel.setTextFill(Color.web("#FF0000")); //Red
        }

        else if (passwordStrengthMeter.getProgress() == 0.50) {
            passwordStrengthLabel.setText("Password Strength: Good");
            passwordStrengthLabel.setTextFill(Color.web("#FFA500")); //Orange
        }

        else if (passwordStrengthMeter.getProgress() == 0.75) {
            passwordStrengthLabel.setText("Password Strength: Strong");
            passwordStrengthLabel.setTextFill(Color.web("#0000FF")); //Blue
        }

        else if (passwordStrengthMeter.getProgress() == 1) {
            passwordStrengthLabel.setText("Password Strength: Very Strong");
            passwordStrengthLabel.setTextFill(Color.web("#00FF00")); //Green
        }

        if (passwordPF.getText().matches(".*[a-z]+.*")) {
            lowerCaseCheckBox.setSelected(true);
        }

        if (passwordPF.getText().matches(".*[A-Z]+.*")) {
            upperCaseCheckBox.setSelected(true);
        }

        if (passwordPF.getText().matches(".*[\\d]+.*")) {
            numCharCheckBox.setSelected(true);
        }

        if (passwordPF.getText().matches(".*[!@#$%^&*()]+.*")) {
            specialCharCheckBox.setSelected(true);
        }
        System.out.println(checkPasswordStrength(passwordPF.getText()) / 100);
    }

    private double checkPasswordStrength(String password) {
        double strengthPercentage=0;
        String[] partialRegexChecks = { ".*[a-z]+.*", // lower
                ".*[A-Z]+.*", // upper
                ".*[\\d]+.*", // digits
                ".*[!@#$%^&*()]+.*" // symbols
        };


        if (password.matches(partialRegexChecks[0])) {
            strengthPercentage+=25;
        }
        if (password.matches(partialRegexChecks[1])) {
            strengthPercentage+=25;
        }
        if (password.matches(partialRegexChecks[2])) {
            strengthPercentage+=25;
        }
        if (password.matches(partialRegexChecks[3])) {
            strengthPercentage+=25;
        }


        return strengthPercentage;
    }

    public void recheckPassword(KeyEvent keyEvent) {

        if (repeatPasswordPF.getText().equals("")) {
            passwordsMatch = false;
            passwordsMatchLabel.setText("Passwords do not match.");
            passwordsMatchLabel.setTextFill(Color.web("#FF0000"));
        } else {
            if (repeatPasswordPF.getText().equals(passwordPF.getText())) {
                passwordsMatch = true;
                passwordsMatchLabel.setVisible(true);
                passwordsMatchLabel.setText("Passwords match.");
                passwordsMatchLabel.setTextFill(Color.web("#00FF00"));
            } else {
                passwordsMatch = false;
                passwordsMatchLabel.setVisible(true);
                passwordsMatchLabel.setText("Passwords do not match");
                passwordsMatchLabel.setTextFill(Color.web("#FF0000"));
            }
        }
    }

    public void validateEmail(KeyEvent keyEvent) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailAddressTF.getText());
        if (!matcher.find()) {
            validEmailLabel.setVisible(true);
        } else {
            validEmailLabel.setVisible(false);
        }
    }
}
