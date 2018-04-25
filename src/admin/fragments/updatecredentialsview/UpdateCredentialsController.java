package admin.fragments.updatecredentialsview;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import util.AlertUtils;
import util.ConnectionUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UpdateCredentialsController implements Initializable{

    @FXML private JFXTextField searchTF;
    @FXML private Label selectLabel;
    @FXML private JFXListView<String> entriesList;

    private Connection mConnection;
    private String mUsername;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            mConnection = ConnectionUtils.getDBConnection();
        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        loadEntries();
    }

    private void loadEntries() {
        try {
            Statement statement = mConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM faculty_details WHERE faculty_id != 9999;");

            while (resultSet.next()) {
                entriesList.getItems().add(resultSet.getString("fullname"));
            }

        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, false);
        }
    }

    public void handleSearch(KeyEvent keyEvent) {

        entriesList.getItems().clear();

        //If empty
        if (searchTF.getText().equals("")) {

            try {
                PreparedStatement preparedStatement = mConnection.prepareStatement("SELECT fullname FROM faculty_details");

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    entriesList.getItems().add(resultSet.getString("fullname"));
                }

            } catch (SQLException e) {

                e.printStackTrace();
            }

        } else {
            try {
                PreparedStatement preparedStatement = mConnection.prepareStatement(" SELECT * FROM faculty_details JOIN faculty_credentials fc ON faculty_details.faculty_id = fc.faculty_id WHERE fullname LIKE ? OR username LIKE ?");
                preparedStatement.setString(1, "%" + searchTF.getText() + "%");
                preparedStatement.setString(2, "%" + searchTF.getText() + "%");

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    entriesList.getItems().add(resultSet.getString("fullname"));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleUpdateCred(MouseEvent mouseEvent) {
        if (entriesList.getSelectionModel().getSelectedItem() != null) {

            mUsername = getUsernameOf(entriesList.getSelectionModel().getSelectedItem());

            openChangePasswordWindow(mUsername);
        }
    }

    private void openChangePasswordWindow(String username) {

        try {

            Stage stage = new Stage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../common/updatecredentials/UpdateCredentialsView.fxml"));
            Parent root = loader.load();
            common.updatecredentials.UpdateCredentialsController controller = loader.getController();

            System.out.println(getUsernameOf(entriesList.getSelectionModel().getSelectedItem()));
            System.out.println(getIdOf(getUsernameOf(entriesList.getSelectionModel().getSelectedItem())));
            controller.setFacultyID(getIdOf(getUsernameOf(entriesList.getSelectionModel().getSelectedItem())));

            JFXDecorator decorator = new JFXDecorator(stage, root);
            decorator.setTitle(username);

            Scene scene = new Scene(decorator);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Utility Functions
    private String getUsernameOf(String selectedItem) {

        try {

            PreparedStatement statement = mConnection.prepareStatement("SELECT username FROM faculty_credentials JOIN faculty_details d2 ON faculty_credentials.faculty_id = d2.faculty_id WHERE fullname = ?");

            statement.setString(1, selectedItem);

            ResultSet r = statement.executeQuery();

            while (r.next()) {
                return r.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private int getIdOf(String username) {
        try {

            PreparedStatement statement = mConnection.prepareStatement("SELECT faculty_id FROM faculty_credentials WHERE username = ?");

            statement.setString(1, username);

            ResultSet r = statement.executeQuery();

            while (r.next()) {
                return r.getInt("faculty_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }


}
