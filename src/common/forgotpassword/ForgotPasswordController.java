package common.forgotpassword;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.mail.EmailException;
import rest.Mailer;
import rest.templateStrings.Templates;

import java.net.MalformedURLException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPasswordController {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private static final String alphaNumerics = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @FXML JFXTextField usernameMailTF;
    @FXML JFXButton sendEmailButton;
    @FXML HBox successMailContainer;
    @FXML VBox codeRecievedContainer;
    @FXML VBox changePasswordBox;
    @FXML ImageView sendProgressIcon;
    @FXML Label sendProgressLabel;
    @FXML Label thankYouLabel;
    @FXML JFXTextField codeTF;
    @FXML Label passwordsMatchLabel;
    @FXML JFXPasswordField newPasswordPF;
    @FXML JFXPasswordField repeatNewPasswordPF;

    private String currentEmailAddress;
    private String currentCode = "";
    private boolean isCodeGenerated = false;

    private Connection connection;
    private boolean passwordsMatch = false;
    private String newPassword;

    public ForgotPasswordController() {
        //TODO ADD DATABASE CONNECT CODE
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

    public void sendEmail(ActionEvent actionEvent) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(usernameMailTF.getText());

        if (matcher.find()) { //Entered text is an email

            if (doesEmailExist(usernameMailTF.getText())) {
                currentEmailAddress = usernameMailTF.getText();

                if (!isCodeGenerated) generateCode();

                Map<String, String> valuesMap = new HashMap<>();
                valuesMap.put("name", getFullNameByEmail(currentEmailAddress));
                valuesMap.put("code", currentCode);

                StrSubstitutor strSubstitutor = new StrSubstitutor(valuesMap);
                String resolvedHTML = strSubstitutor.replace(Templates.PASSWORD_RECOVERY_HTML);

                try {
                    Mailer.sendHTMLMail(currentEmailAddress, getFullNameByEmail(currentEmailAddress), "Administrator (FDP Management)", "Password Recovery", resolvedHTML);
                } catch (EmailException | MalformedURLException e) {
                    e.printStackTrace();
                }

                //Add contents back

                sendProgressIcon.setManaged(true);
                sendProgressIcon.setVisible(true);

                sendProgressLabel.setManaged(true);
                sendProgressLabel.setVisible(true);

                successMailContainer.setVisible(true);

                codeRecievedContainer.setVisible(true);
            }

            else {
                Alert emailNotExistAlert = new Alert(Alert.AlertType.WARNING, "Specified E-mail doesn't exist. Please try again.", ButtonType.OK);
                emailNotExistAlert.setHeaderText("Invalid E-mail");
                emailNotExistAlert.showAndWait();
            }

        }

        else if (doesUsernameExist(usernameMailTF.getText())) { //Entered Text is a valid username
            currentEmailAddress = getEmailByUsername(usernameMailTF.getText());

            //Generate code and Message
            if (!isCodeGenerated) generateCode();

            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("name", getFullNameByEmail(currentEmailAddress));
            valuesMap.put("code", currentCode);

            StrSubstitutor strSubstitutor = new StrSubstitutor(valuesMap);
            String resolvedHTML = strSubstitutor.replace(Templates.PASSWORD_RECOVERY_HTML);

            try {
                Mailer.sendHTMLMail(currentEmailAddress, getFullNameByEmail(currentEmailAddress), "Administrator (FDP Management)", "Password Recovery", resolvedHTML);
            } catch (EmailException | MalformedURLException e) {
                e.printStackTrace();
            }

            //Add contents back

            sendProgressIcon.setManaged(true);
            sendProgressIcon.setVisible(true);

            sendProgressLabel.setManaged(true);
            sendProgressLabel.setVisible(true);

            successMailContainer.setVisible(true);

            codeRecievedContainer.setVisible(true);
        }


        else { //Entered Text is not a valid username/email
            Alert invalidInputAlert = new Alert(Alert.AlertType.WARNING, "Please enter a valid username/email.", ButtonType.OK);
            invalidInputAlert.setHeaderText("Invalid Input");
            invalidInputAlert.showAndWait();
        }



    }

    private void generateCode() {
        Random random = new Random();

        for (int i = 1; i <= 5; i++) {
            currentCode += alphaNumerics.charAt(random.nextInt(35));
        }

        System.out.println(currentCode);
        isCodeGenerated = true;
    }

    private String getFullNameByEmail(String currentEmailAddress) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM memberdetails WHERE email_address=?;");
            preparedStatement.setString(1, currentEmailAddress);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    private String getEmailByUsername(String username) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM memberdetails WHERE username=?;");
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getString("email_address");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    private boolean doesEmailExist(String email) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM memberdetails WHERE email_address=?;");
            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    private boolean doesUsernameExist(String username) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM memberdetails WHERE username=?;");
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public void submitCode(ActionEvent actionEvent) {
        if (codeTF.getText().equals(currentCode)) { //Code Correct

            Map<String, String> nameMap = new HashMap<>();
            nameMap.put("name", getFullNameByEmail(currentEmailAddress));

            thankYouLabel.setText(new StrSubstitutor(nameMap).replace(thankYouLabel.getText()));

            thankYouLabel.setVisible(true);

            changePasswordBox.setVisible(true);

        }

        else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please try again.", ButtonType.OK);
            alert.setHeaderText("Invalid Code.");
            alert.showAndWait();
        }
    }

    @SuppressWarnings("Duplicates")
    public void checkPasswordsMatch(KeyEvent keyEvent) {

        passwordsMatchLabel.setVisible(true);

        if (repeatNewPasswordPF.getText().equals("")) {
            passwordsMatch = false;
            passwordsMatchLabel.setText("Passwords do not match.");
        } else {
            if (repeatNewPasswordPF.getText().equals(newPasswordPF.getText())) {
                passwordsMatch = true;
                passwordsMatchLabel.setVisible(true);
                passwordsMatchLabel.setText("Passwords match.");
                newPassword = repeatNewPasswordPF.getText();
                passwordsMatchLabel.setTextFill(Color.web("#00FF00"));
            } else {
                passwordsMatch = false;
                passwordsMatchLabel.setVisible(true);
                passwordsMatchLabel.setText("Passwords do not match");
            }
        }
    }

    public void updatePassword(ActionEvent actionEvent) {
        if (passwordsMatch) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE memberdetails SET password=? WHERE email_address=?;");
                preparedStatement.setString(1, newPassword);
                preparedStatement.setString(2, currentEmailAddress);

                preparedStatement.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Password change successful!", ButtonType.OK);
                alert.setHeaderText("Successful");
                alert.showAndWait();

                //Close the scene
                usernameMailTF.getScene().getWindow().hide();

            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "An error occurred and the request could not be processed.", ButtonType.OK);
                alert.setHeaderText("ERROR");
                alert.showAndWait();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Passwords do not match.", ButtonType.OK);
            alert.setHeaderText("Please retry");
            alert.showAndWait();
        }
    }
}
