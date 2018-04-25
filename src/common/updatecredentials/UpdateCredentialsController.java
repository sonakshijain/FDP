package common.updatecredentials;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import util.AlertUtils;
import util.ConnectionUtils;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UpdateCredentialsController implements Initializable {

    @FXML
    private JFXPasswordField passwordPF;
    @FXML
    private JFXPasswordField repeatPasswordPF;
    @FXML
    private JFXButton submitButton;
    @FXML
    private Label passwordMatchLabel;

    private Connection mConnection;
    private int mFacultyID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            mConnection = ConnectionUtils.getDBConnection();
        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        submitButton.setOnAction(event -> handleSubmit(passwordPF.getText()));

        repeatPasswordPF.setOnKeyTyped(event -> validatePasswordsFields());

        Platform.runLater(() -> passwordPF.requestFocus());

    }

    private boolean validatePasswordsFields() {
        String password = passwordPF.getText();
        String repeatPassword = repeatPasswordPF.getText();

        if (password.equals(repeatPassword)) {
            passwordMatchLabel.setText("Passwords match.");
            passwordMatchLabel.setTextFill(Color.web("#64dd17"));
            return true;
        } else {
            passwordMatchLabel.setText("Passwords do not match.");
            passwordMatchLabel.setTextFill(Color.web("#ff0000"));
            return false;
        }
    }

    private void handleSubmit(String password) {

        if (validatePasswordsFields()) {

            try {

                PreparedStatement preparedStatement = mConnection.prepareStatement("UPDATE faculty_credentials SET password = ? WHERE faculty_id = ?");

                preparedStatement.setString(1, password);
                preparedStatement.setInt(2, mFacultyID);

                System.out.println(preparedStatement);
                preparedStatement.execute();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Password Change Successful", ButtonType.CLOSE);
                alert.setHeaderText("Success");
                alert.showAndWait();

            } catch (SQLException e) {
                AlertUtils.displaySQLErrorAlert(e, false);
            }

        }
    }

    public void setFacultyID(int facultyID) {
        mFacultyID = facultyID;

    }
}

