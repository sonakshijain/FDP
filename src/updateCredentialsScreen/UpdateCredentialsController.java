package updateCredentialsScreen;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import util.AlertUtils;
import util.ConnectionUtils;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UpdateCredentialsController implements Initializable{

    @FXML private JFXTextField searchTF;

    @FXML private Label selectLabel;

    @FXML private JFXListView<String> entriesList;

    private Connection mConnection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            mConnection = ConnectionUtils.getDBConnection();
        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        try {
            Statement statement = mConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM faculty_details WHERE fullname != \'Administrator\';");

            while (resultSet.next()) {
                entriesList.getItems().add(resultSet.getString("fullname"));
            }

        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, false);
        }
    }


    public void handleSearch(KeyEvent keyEvent) {

        entriesList.getItems().clear();

        try {
            PreparedStatement preparedStatement = mConnection.prepareStatement("SELECT * FROM memberdetails " +
                                                                                   "WHERE (name LIKE ? OR username LIKE ? OR email_address LIKE ?) " +
                                                                                   "AND name != \'Administrator\'");
            preparedStatement.setString(1, "%" + searchTF.getText() + "%");
            preparedStatement.setString(2, searchTF.getText() + "%");
            preparedStatement.setString(3, searchTF.getText() + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                entriesList.getItems().add(resultSet.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleUpdateCred(MouseEvent mouseEvent) {
        if (entriesList.getSelectionModel().getSelectedItem() != null)
        System.out.println(entriesList.getSelectionModel().getSelectedItem());
    }


}
