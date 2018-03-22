package dbViewFragment;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import model.Person;
import util.AlertUtils;
import util.ConnectionUtils;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class DBViewController implements Initializable {

    @FXML private JFXTextField  searchBarTF;
    @FXML private TableView<Person> dataTable;
    @FXML private TableColumn<Person, String> nameCol;
    @FXML private TableColumn<Person, String> deptCol;
    @FXML private TableColumn<Person, String> usernameCol;
    @FXML private TableColumn<Person, Long> facultyIdCol;
    @FXML private JFXButton exportButton;

    private ObservableList<Person> mPeople = FXCollections.observableArrayList();

    private Connection mConnection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            mConnection = ConnectionUtils.getDBConnection();
        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        loadPeople();

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        facultyIdCol.setCellValueFactory(new PropertyValueFactory<>("facultyId"));
        deptCol.setCellValueFactory(new PropertyValueFactory<>("dept"));

        dataTable.setItems(mPeople);

        exportButton.setOnAction(event -> {
            openDirectoryChooser();
        });
    }

    private void openDirectoryChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(searchBarTF.getScene().getWindow());

        if (selectedDirectory != null) System.out.println(selectedDirectory.getAbsolutePath());
        else System.out.println("You selected nothing.");
    }

    private void loadPeople() {
        try {
            Statement statement = mConnection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT faculty_details.faculty_id, faculty_details.fullname, fc.username, faculty_details.dept FROM faculty_details JOIN faculty_credentials fc ON faculty_details.faculty_id = fc.faculty_id;");

            while (resultSet.next()) {
                Person person = new Person(
                        resultSet.getInt("faculty_id"),
                        resultSet.getString("fullname"),
                        resultSet.getString("username"),
                        "",                                              //FIXME: REMOVE THIS PARAMETER
                        resultSet.getString("dept")
                );
                mPeople.add(person);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleSearch(KeyEvent keyEvent) {

        mPeople.clear();

        try {

            PreparedStatement preparedStatement = null;

            try {

                checkNumeric(searchBarTF.getText());                            //Check if the search text is numeric

                preparedStatement = mConnection.prepareStatement("SELECT * FROM faculty_details " +
                        "WHERE (faculty_id LIKE ?) " +
                        "AND fullname != \'Administrator\'");

                preparedStatement.setString(1, searchBarTF.getText() + "%");

            } catch (Exception e) {                                             //The search text was characters

                preparedStatement = mConnection.prepareStatement("SELECT * FROM faculty_details JOIN faculty_credentials fc ON faculty_details.faculty_id = fc.faculty_id" +
                                    " WHERE (fullname LIKE ? OR username LIKE ? OR email_address LIKE ?) " +
                                    "AND fullname != \'Administrator\'");

                preparedStatement.setString(1, "%" + searchBarTF.getText() + "%");
                preparedStatement.setString(2, searchBarTF.getText() + "%");
                preparedStatement.setString(3, searchBarTF.getText() + "%");
            }
            finally {
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    mPeople.add(new Person(resultSet.getInt("faculty_id"),
                            resultSet.getString("name"),
                            resultSet.getString("username"),
                            "",                                             //FIXME: REMOVE PARAMETER
                            resultSet.getString("email_address")));
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void checkEmpty(KeyEvent keyEvent) {
        if (searchBarTF.getText().equals("")) {
            mPeople.clear();
            loadPeople();
        }
    }

    private boolean checkNumeric (String str) throws Exception {
        if (str.matches("\\d+")) return true;
        else throw new Exception("Not a valid numeric string.");
    }
}
