package common.login;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXSpinner;
import faculty.mainscreen.MainScreenFacultyController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.Main;
import org.controlsfx.control.Notifications;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {



        connection = null;
        try {
            connection = ConnectionUtils.getDBConnection();
        } catch (Exception e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        passwordField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                LoginScreenController.this.signInUser();
            }
        });

        Platform.runLater(usernameTextField::requestFocus);

//        ONLY DEBUGGING
        usernameTextField.setText("Admin");
        passwordField.setText("Password");
//        signInUser();
    }

    public void signInUser() {

            setSignInButtonState(1);

            PauseTransition pauseTransition = new PauseTransition();
            pauseTransition.setDuration(Duration.seconds(3));
            pauseTransition.setOnFinished(ev -> {
                try {
                    signIn();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            pauseTransition.play();
    }

    private void signIn() throws SQLException {

//        System.out.println(usernameTextField.getText() + " " + passwordField.getText());

        if (checkIfUserExists(usernameTextField.getText())) {

            if (passwordField.getText().equals(getPasswordOf(usernameTextField.getText()))) {

                PreparedStatement preparedStatement = connection.prepareStatement("SELECT access_level FROM faculty_credentials WHERE username=?");
                preparedStatement.setString(1, usernameTextField.getText());
                ResultSet resultSet = preparedStatement.executeQuery();

                //Get faculty id of current username for passing on to the new controller
                PreparedStatement getFacultyIDStmt = connection.prepareStatement("SELECT faculty_id FROM faculty_credentials WHERE username = ?");
                getFacultyIDStmt.setString(1, usernameTextField.getText());
                ResultSet resultSet1 = getFacultyIDStmt.executeQuery();
                resultSet1.next();
                int facultyID = resultSet1.getInt("faculty_id");

                while (resultSet.next()) {

                    if (resultSet.getInt("access_level") == 1) {
                        //Admin Found
                        System.out.println("Admin");
                        openMainWindow(ADMIN, facultyID);
                        return;
                    }

                    else if (resultSet.getInt("access_level") == 2) {
                        System.out.println("hod");
                        openMainWindow(HOD, facultyID);
                        return;
                    }

                    else if (resultSet.getInt("access_level") == 3) {
                        //Faculty
                        System.out.println("Faculty");
                        openMainWindow(FACULTY, facultyID);
                        return;
                    }


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
            Alert alert = new Alert(Alert.AlertType.WARNING, "Couldn't find user " + usernameTextField.getText() + ".", ButtonType.OK);
            alert.showAndWait();

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
                    root = FXMLLoader.load(getClass().getResource("../../admin/mainscreen/MainScreenAdmin.fxml"));
                    //TODO PASS FAC ID
                    break;
                }

                case HOD: {
                    root = FXMLLoader.load(getClass().getResource("../../hod/mainscreen/MainScreenHOD.fxml"));
                    break;
                }

                case FACULTY: {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../faculty/mainscreen/MainScreenFaculty.fxml"));
                    root = fxmlLoader.load();
                    MainScreenFacultyController controller = fxmlLoader.getController();
                    controller.setFacultyID(facultyID);
                    System.out.println("LSC setting in MSFC value facultyID = " + facultyID);
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


//            ResizeHelper.addResizeListener(stage);
//            root.getChildrenUnmodifiable().get(1).setOnMousePressed(event -> {
//                xOffset = event.getSceneX();
//                yOffset = event.getSceneY();
//            });
//            root.getChildrenUnmodifiable().get(1).setOnMouseDragged(event -> {
//                stage.setX(event.getScreenX() - xOffset);
//                stage.setY(event.getScreenY() - yOffset);
//            });




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
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT password FROM faculty_credentials WHERE username=?");
        preparedStatement.setString(1, username);

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            return resultSet.getString("password");
        }
        return null;
    }


    //Helper Methods.
    public void handleForgotPassword(MouseEvent mouseEvent) {
        System.out.println("OKAY?");
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("../common.forgotpassword/ForgotPasswordScreen.fxml"));
            Scene mainScene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Password Recovery");
            stage.setScene(mainScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        connection.close();
    }
}
