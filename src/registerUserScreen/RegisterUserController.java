package registerUserScreen;

import MainScreen.MainScreenAdminController;
import com.jfoenix.controls.*;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;
import com.jfoenix.effects.JFXDepthManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import util.AlertUtils;
import util.ConnectionUtils;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class RegisterUserController implements Initializable {

    @FXML private AnchorPane root;
    @FXML private AnchorPane personalDetailsCard;
    @FXML private JFXChipView<String> coursesChipView;
    @FXML private JFXTextField fullNameTF;
    @FXML private JFXTextField usernameTF;
    @FXML private JFXCheckBox autoUsernameCB;
    @FXML private JFXTextField emailTF;
    @FXML private JFXTextField contactTF;
    @FXML private JFXRadioButton maleRB;
    @FXML private ToggleGroup genderToggleGroup;
    @FXML private JFXRadioButton femaleRB;
    @FXML private VBox professionalDetailsCard;
    @FXML private JFXComboBox<String> deptCombo;
    @FXML private JFXComboBox<String> qualCombo;
    @FXML private JFXComboBox<String> designationCombo;
    @FXML private JFXToggleButton clearTB;
    @FXML private JFXToggleButton openDBTB;
    @FXML private JFXButton submitButton;
    @FXML private JFXPasswordField passwordPF;
    @FXML private JFXCheckBox autoPasswordCB;
    @FXML private JFXTextField facultyIdTF;

    @FXML MainScreenAdminController mainScreenAdminController;

    private HashMap<String, String> courses = new HashMap<>();
    private Connection mConnection;

    public void init(MainScreenAdminController mainScreenAdminController) {
        this.mainScreenAdminController = mainScreenAdminController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            mConnection = ConnectionUtils.getDBConnection();
            initSuggestions();
        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        autoUsernameCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) generateUsername();
            else usernameTF.clear();
        });
        autoPasswordCB.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) generatePassword();
            else passwordPF.clear();
        }));

        fullNameTF.setOnKeyTyped(event -> {
            if (autoUsernameCB.isSelected()) generateUsername();
        });

        maleRB.setSelected(true);
        autoUsernameCB.setSelected(true);
        autoPasswordCB.setSelected(true);

        qualCombo.getItems().clear();
        qualCombo.getItems().addAll(
                "B.Sc",
                         "M.Sc",
                         "PhD"
        );

        deptCombo.getItems().clear();
        deptCombo.getItems().addAll(
                "Computer Science",
                         "Electronics & Telecommunications",
                         "Mechanical",
                         "Civil"
        );

        designationCombo.getItems().clear();
        designationCombo.getItems().addAll(
                "Administrator",
                "Head of Department",
                "Faculty"
        );

        JFXDepthManager.setDepth(coursesChipView, 5);

    }

    private void generatePassword() {
        passwordPF.setText("password");

        JFXSnackbar snackbar = new JFXSnackbar(root);
        snackbar.fireEvent(new SnackbarEvent("Password set to default value.",
                "CLOSE",
                3000,
                true,
                b -> snackbar.close()));
    }

    private void generateUsername() {

        String[] words = fullNameTF.getText().split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("[^\\w]", "");
        }

        StringBuilder generatedUsername = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i == 0) generatedUsername.append(words[i]);
            else        generatedUsername.append(".").append(words[i]);
        }

        usernameTF.setText(generatedUsername.toString().toLowerCase());
    }

    private void initSuggestions() throws SQLException {

        Statement statement = mConnection.createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT * FROM subjects;");

        while (resultSet.next()) {
            courses.put(resultSet.getString("abbreviation"), resultSet.getString("name"));
        }

        statement.close();

        coursesChipView.getSuggestions().addAll(courses.keySet());

        System.out.println("courses = " + courses);
    }

    public void createUser(ActionEvent actionEvent) {

//        mainScreenAdminController.updatePage(1); //TODO:FIXME

        PreparedStatement facultyDetailsPS;
        PreparedStatement facultyCredentialsPS;
        PreparedStatement facultyQualificationsPS;
        PreparedStatement facultySubjectsPS;

        boolean completedWithoutErrors = false;
        boolean rollback = false;

        try {
            String name = fullNameTF.getText();
            String username = usernameTF.getText();
            String password = passwordPF.getText();
            String email = emailTF.getText();
            int facultyID = Integer.parseInt(facultyIdTF.getText());
            String dept = deptCombo.getSelectionModel().getSelectedItem();
            String qual = qualCombo.getSelectionModel().getSelectedItem();

            ArrayList<String> subjects = new ArrayList<>() {{
                    addAll(coursesChipView.getChips());
            }};

            for (String subject: subjects) {
                PreparedStatement preparedStatement = mConnection.prepareStatement("SELECT abbreviation FROM subjects WHERE abbreviation=?");
                preparedStatement.setString(1, subject);

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) { //TODO: FIXME FOR UNKNOWN SUBJECTS
                    if (!resultSet.getString("abbreviation").equals(subject)) {
                        System.out.println("Unknown subjects found");
                        return;
                    }
                }

                preparedStatement.close();
            }

            int gender; //Gender: 1- Male, 2-Female
            if (genderToggleGroup.getSelectedToggle() == maleRB) gender = 1;
            else gender = 2;

            int accessLevel = designationCombo.getSelectionModel().getSelectedIndex() + 1; //1: Admin 2: HOD 3: Faculty

            if (name.equals("") || username.equals("") || email.equals("") || (password.equals("") && !autoPasswordCB.isSelected())) {
                throw new Exception("Please ensure that none of the fields are empty.");
            }

            rollback = !checkIfFacultyIDExists(facultyID);
            System.out.println("rollback = " + rollback);

            String query1 = "INSERT INTO faculty_details (faculty_id, fullname, dept, gender) VALUES (?, ?, ?, ?)";
            String query2 = "INSERT INTO faculty_credentials (faculty_id, username, password, email_address, access_level) VALUES (?, ?, ?, ?, ?)";
            String query3 = "INSERT INTO faculty_qualifications (faculty_id, qualifications) VALUES (?, ?)";
            String query4 = "INSERT INTO faculty_subjects (faculty_id, sub_id) VALUES (?, ?)";

            facultyDetailsPS = mConnection.prepareStatement(query1);
            facultyCredentialsPS = mConnection.prepareStatement(query2);
            facultyQualificationsPS = mConnection.prepareStatement(query3);
            facultySubjectsPS = mConnection.prepareStatement(query4);

            facultyDetailsPS.setInt(1, facultyID);
            facultyDetailsPS.setString(2, name);
            facultyDetailsPS.setString(3, dept);
            facultyDetailsPS.setInt(4, gender);

            facultyCredentialsPS.setInt(1, facultyID);
            facultyCredentialsPS.setString(2, username);
            facultyCredentialsPS.setString(3, password);
            facultyCredentialsPS.setString(4, email);
            facultyCredentialsPS.setInt(5, accessLevel);

            facultyQualificationsPS.setInt(1, facultyID);
            facultyQualificationsPS.setString(2, qual);

            //TODO Subjects

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Proceed " + name + "?", ButtonType.YES, ButtonType.NO);
            confirmAlert.setHeaderText("Are you sure you want to continue?");
            confirmAlert.setContentText("Please recheck the entered data.");
            confirmAlert.showAndWait();

            if (confirmAlert.getResult() == ButtonType.YES) {
                facultyDetailsPS.execute();
                facultyCredentialsPS.execute();
                facultyQualificationsPS.execute();

                for (String subject: subjects) { //FIXME :_)

                    PreparedStatement preparedStatement = mConnection.prepareStatement("SELECT sub_id FROM subjects WHERE abbreviation=?");
                    preparedStatement.setString(1, subject);

                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {

                        int subID = resultSet.getInt("sub_id");
                        System.out.println("subID = " + subID);

                        preparedStatement.close();
                        resultSet.close();

                        facultySubjectsPS.setInt(1, facultyID);
                        facultySubjectsPS.setInt(2, subID);

                        facultySubjectsPS.execute();

                        facultySubjectsPS.clearParameters();
                    }
                }
            }

            completedWithoutErrors = true;

        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, false);

            if (rollback) {

                System.out.println("Rolling Back..");

                //Remove Incomplete information
                PreparedStatement facultyDetailsDropPS;
                PreparedStatement facultyCredentialsDropPS;
                PreparedStatement facultyQualificationsDropPS;
                PreparedStatement facultySubjectsDropPS;

                try {

                    String query3 = "DELETE FROM faculty_qualifications WHERE faculty_id=?";
                    String query2 = "DELETE FROM faculty_credentials WHERE faculty_id=?";
                    String query4 = "DELETE FROM faculty_subjects WHERE faculty_id=?";
                    String query1 = "DELETE FROM faculty_details WHERE faculty_id=?";

                    facultyCredentialsDropPS = mConnection.prepareStatement(query2);
                    facultyQualificationsDropPS = mConnection.prepareStatement(query3);
                    facultySubjectsDropPS = mConnection.prepareStatement(query4);
                    facultyDetailsDropPS = mConnection.prepareStatement(query1);

                    facultyCredentialsDropPS.setInt(1, Integer.parseInt(facultyIdTF.getText()));
                    facultyQualificationsDropPS.setInt(1, Integer.parseInt(facultyIdTF.getText()));
                    facultySubjectsDropPS.setInt(1, Integer.parseInt(facultyIdTF.getText()));
                    facultyDetailsDropPS.setInt(1, Integer.parseInt(facultyIdTF.getText()));

                    facultyCredentialsDropPS.execute();
                    facultyQualificationsDropPS.execute();
                    facultySubjectsDropPS.execute();
                    facultyDetailsDropPS.execute();

                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

            }

        } catch (RuntimeException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please ensure none of the fields are empty.", ButtonType.OK);
            alert.setHeaderText("Some Fields Empty.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (completedWithoutErrors) {

            AlertUtils.displayConfirmation("User Created Successfully.");

            if (clearTB.isSelected()) {
                clearAll();
            }

            if (openDBTB.isSelected()) {
                openDBView();
            }
        }

    }

    private boolean checkIfFacultyIDExists(int facultyID) throws SQLException{
        Statement statement = mConnection.createStatement();

        String sql = "SELECT faculty_id FROM faculty_details;";

        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            if (resultSet.getInt("faculty_id") == facultyID) {
                return true;
            }
        }

        return false;
    }

    private void openDBView() {
        mainScreenAdminController.updatePage(1);
    } //FIXME: DOESN'T WORK

    private void clearAll() {
        usernameTF.clear();
        usernameTF.clear();
//        autoUsernameCB.setSelected(false);
        facultyIdTF.clear();
        emailTF.clear();
        contactTF.clear();
        passwordPF.clear();
//        autoPasswordCB.setSelected(false);
//        genderToggleGroup.getSelectedToggle().setSelected(false);
        deptCombo.getSelectionModel().clearSelection();
        qualCombo.getSelectionModel().clearSelection();
        coursesChipView.getChips().clear();
    }
}

