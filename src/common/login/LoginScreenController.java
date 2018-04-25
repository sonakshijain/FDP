package common.login;

import admin.mainscreen.MainScreenAdminController;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXSpinner;
import faculty.mainscreen.MainScreenFacultyController;
import hod.mainscreen.MainScreenHODController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.AlertUtils;
import util.ConnectionUtils;
import util.DialogUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class LoginScreenController implements Initializable {

    private static final int ADMIN = 1;
    private static final int HOD = 2;
    private static final int FACULTY = 3;

    @FXML Button signInButton;
    @FXML TextField usernameTextField;
    @FXML PasswordField passwordField;
    @FXML Label forgotPasswordLabel;
    @FXML StackPane root;
    @FXML JFXSpinner loadingSpinner;

    private Connection connection;

    private String mFullName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            connection = ConnectionUtils.getDBConnection();
        } catch (Exception e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        //
        root.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                signInUser(null);
            }
        });

        Platform.runLater(usernameTextField::requestFocus);
    }

    public void signInUser(ActionEvent actionEvent) {

        setSignInButtonState(1);

        PauseTransition pauseTransition = new PauseTransition();
        pauseTransition.setDuration(Duration.seconds(3));
        pauseTransition.setAutoReverse(false);

        pauseTransition.setOnFinished(ev -> {
            try {
                signIn(usernameTextField.getText(), passwordField.getText());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        pauseTransition.play();

    }

    private void signIn(String username, String password) throws SQLException {

        if (checkIfUserExists(username)) {

            if (password.equals(getPasswordOf(username))) {

                PreparedStatement preparedStatement = connection.prepareStatement("SELECT access_level FROM faculty_credentials WHERE username=?");
                preparedStatement.setString(1, username);
                ResultSet accessLevelResultSet = preparedStatement.executeQuery();

                //Get faculty id of current username for passing on to the new controller
                PreparedStatement getFacultyIDStmt = connection.prepareStatement("SELECT faculty_id FROM faculty_credentials WHERE username = ?");
                getFacultyIDStmt.setString(1, username);
                ResultSet resultSet1 = getFacultyIDStmt.executeQuery();
                resultSet1.next();
                int facultyID = resultSet1.getInt("faculty_id");

                //Fetch the current username's fullName to set in every child controller
                mFullName = getNameOf(username);

                accessLevelResultSet.next();

                if (accessLevelResultSet.getInt("access_level") == 1) {
                    //Admin Found
                    System.out.println("Admin");
                    openMainWindow(ADMIN, facultyID);
                    return;
                } else if (accessLevelResultSet.getInt("access_level") == 2) {
                    //Hod
                    System.out.println("HOD");
                    openMainWindow(HOD, facultyID);
                    return;
                } else if (accessLevelResultSet.getInt("access_level") == 3) {
                    //Faculty
                    System.out.println("Faculty");
                    openMainWindow(FACULTY, facultyID);
                    return;
                }

//                openMainWindow();
                if (connection != null) connection.close();
                System.out.println("Successful Sign In.");
            }

            else {
                setSignInButtonState(0);
                DialogUtils.createPresetDialog(root, DialogUtils.ERROR, "Please enter the correct credentials.");
            }
        }

        else {
            setSignInButtonState(0);
            Alert alert = new Alert(Alert.AlertType.WARNING, "Couldn't find user " + username + ".", ButtonType.OK);
            alert.show();


        }
    }

    private void setSignInButtonState(int state) {
        switch (state) {
            case 1: {
                signInButton.setText("");
                loadingSpinner.setVisible(true);
                break;
            }

            case 0: {
                signInButton.setText("Sign In");
                loadingSpinner.setVisible(false);
                break;
            }
        }
    }

    private void openMainWindow(int window, int facultyID) {
        try {
            Parent root;

            switch (window) {
                case ADMIN: {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../admin/mainscreen/MainScreenAdmin.fxml"));
                    root = fxmlLoader.load();
                    MainScreenAdminController msac = fxmlLoader.getController();
                    msac.setWelcomeLabel(getNameOf(usernameTextField.getText()));
                    break;
                }

                case HOD: {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../../hod/mainscreen/MainScreenHOD.fxml"));
                    root = loader.load();
                    MainScreenHODController controller = loader.getController();
                    controller.setFacultyID(facultyID);
                    controller.setCurrentUser(mFullName);
                    break;
                }

                case FACULTY: {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../faculty/mainscreen/MainScreenFaculty.fxml"));
                    root = fxmlLoader.load();
                    MainScreenFacultyController controller = fxmlLoader.getController();
                    controller.setFacultyID(facultyID);
                    controller.setCurrentUser(mFullName);
                    break;
                }

                default: {
                    root = FXMLLoader.load(getClass().getResource("../faculty/mainscreen/MainScreenFaculty.fxml"));
                    break;
                }
            }


            Stage stage = new Stage();

            JFXDecorator decorator = new JFXDecorator(stage, root);

            BorderPane root_b = (BorderPane) root;

            root_b.prefWidthProperty().bind(stage.widthProperty());
            root_b.prefHeightProperty().bind(stage.heightProperty());

            decorator.setPrefSize(1366D, 768D);

            decorator.setMaximized(true);

            Scene scene = new Scene(decorator);
            stage.setTitle("FDP Management");
            stage.setScene(scene);

            usernameTextField.getScene().getWindow().hide(); //Hide the login Screen
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkIfUserExists(String username) throws SQLException {

        Statement statement = connection.createStatement();

        String sql = "SELECT username FROM faculty_credentials;";

        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            if (resultSet.getString("username").equals(username)) {
                return true;
            }
        }

        return false;
    }

    private String getPasswordOf (String username) throws SQLException {
        String sql = "SELECT password FROM faculty_credentials WHERE username=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, username);

        ResultSet resultSet = preparedStatement.executeQuery();

        resultSet.next();

        return resultSet.getString("password");

    }

    private String getNameOf(String username) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT fullname FROM faculty_details WHERE faculty_id = (SELECT faculty_id FROM faculty_credentials WHERE username = ?)");
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();

            return resultSet.getString("fullname");

        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, false);
        }
        return null;
    }

    //Helper Methods.
    public void handleForgotPassword(MouseEvent mouseEvent) {
        System.out.println("OKAY?");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../forgotpassword/ForgotPasswordScreen.fxml"));
            Scene mainScene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Password Recovery");
            stage.setScene(mainScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
