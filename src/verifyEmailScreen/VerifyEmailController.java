package verifyEmailScreen;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.text.StrSubstitutor;
import rest.Mailer;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class VerifyEmailController {

    @FXML Label mailSentSuccessfullyLabel;
    @FXML Label emailInformationLabel;
    @FXML TextField verificationCodeTF;
    @FXML HBox submitButtonHBox;
    @FXML TextField usernameTF;

    private Connection connection;
    private String sentCode = "";
    private String currentEmailAddress;
    private String currentName;

    private static final String alphaNumerics = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public VerifyEmailController() {
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

    public void sendCode() {
        //TODO: Validate username, find email
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM memberdetails WHERE username=?");
            preparedStatement.setString(1, usernameTF.getText());

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                currentEmailAddress = resultSet.getString("email_address");
                currentName = resultSet.getString("name");
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }

        Random random = new Random();

        for (int i = 1; i <= 5; i++) {
            sentCode += alphaNumerics.charAt(random.nextInt(35));
        }

        System.out.println(sentCode);

        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("fullname", currentName);
        valuesMap.put("code", sentCode);

        StrSubstitutor strSubstitutor = new StrSubstitutor(valuesMap);
        String resolvedContent = strSubstitutor.replace(Mailer.PRESET_MAIL);

        Mailer.sendMail(currentEmailAddress, "Verification Mail", resolvedContent);

        mailSentSuccessfullyLabel.setText("Mail sent successfully.");
        mailSentSuccessfullyLabel.setTextFill(Color.web("#00FF00"));

        mailSentSuccessfullyLabel.setVisible(true);

        emailInformationLabel.setManaged(true);

        emailInformationLabel.getScene().getWindow().setHeight(350);

        emailInformationLabel.setText("An Email with a verification code has been sent to " + currentEmailAddress +
                ". Kindly check your email for the code and enter it below.");

        emailInformationLabel.setVisible(true);

        verificationCodeTF.setManaged(true);

        verificationCodeTF.setVisible(true);

        submitButtonHBox.setManaged(true);

        submitButtonHBox.setVisible(true);

    }

    public void validateCode() {
        if (verificationCodeTF.getText().equals(sentCode)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Email Verified Successfully!", ButtonType.OK);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please try again.", ButtonType.OK);
            alert.showAndWait();

        }
    }

}
