package faculty.fragments.submitfdprequestsview;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXToolbar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import rest.AutocompletionTextField;
import util.AlertUtils;
import util.ConnectionUtils;
import util.DialogUtils;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static java.time.temporal.ChronoUnit.DAYS;

public class SubmitFDPRequestController implements Initializable, AutocompletionTextField.OnSelectionListener {

    @FXML private StackPane root;
    @FXML private JFXToolbar toolbar;
    @FXML
    private AutocompletionTextField fdpNameTF;
    @FXML private JFXTextArea fdpCommentsTA;
    @FXML private JFXDatePicker dateFrom;
    @FXML private JFXDatePicker dateTo;
    @FXML private Label daysCountLabel;
    @FXML private JFXButton submitButton;

    private Connection mConnection;
    private int mFacultyID;

    private ObservableList<String> mFDPs = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            mConnection = ConnectionUtils.getDBConnection();
        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        dateFrom.valueProperty().addListener((observable, oldValue, newValue) -> {
            dateTo.setValue(newValue.plusDays(1));
        });

        dateTo.valueProperty().addListener((observable, oldValue, newValue) -> {
            daysCountLabel.setText("Duration: " + "\"" + DAYS.between(dateFrom.getValue(), dateTo.getValue()) + "\"" + "Days");
        });

        loadFDPs();
        fdpNameTF.setListener(this);
    }

    private void loadFDPs() {
        try {

            Statement statement = mConnection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT fdp_name FROM fdp_details");

            while (resultSet.next()) {
                mFDPs.add(resultSet.getString(1));
            }

            SortedList<String> sortedList = new SortedList<>(mFDPs);
            fdpNameTF.getEntries().addAll(sortedList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleSubmit(ActionEvent actionEvent) {

        if (fdpNameTF.getText().equals("") || dateFrom.getValue() == null || dateTo.getValue() == null) {
            DialogUtils.createDialog(root, "PLEASE RETRY", "Please ensure none of the fields are empty.", new JFXButton("OK") {{
                setStyle("-fx-background-color: RED"); //FIXME: COLOR
            }});
        } else {
            String FDPName = fdpNameTF.getText();
            String FDPComments = fdpCommentsTA.getText();
            LocalDate dateF = dateFrom.getValue();
            LocalDate dateT = dateTo.getValue();

            boolean rollback = true;

            int currentFdpID = -1;

            try {

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Please recheck the data before submitting.", ButtonType.YES, ButtonType.NO);
                confirmation.setHeaderText("Submit?");
                confirmation.showAndWait();

                if (confirmation.getResult() == ButtonType.YES) {

                    String createFDPStatementQuery = "INSERT INTO fdp_details (fdp_name, date_from, date_to, days, hod_rec, dir_app, comments) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING fdp_id";

                    PreparedStatement createFDPStatement = mConnection.prepareStatement(createFDPStatementQuery);

                    createFDPStatement.setString(1, FDPName);
                    createFDPStatement.setDate(2, Date.valueOf(dateF));
                    createFDPStatement.setDate(3, Date.valueOf(dateT));
                    createFDPStatement.setInt(4, (int) DAYS.between(dateFrom.getValue(), dateTo.getValue()));
                    createFDPStatement.setBoolean(5, false);
                    createFDPStatement.setBoolean(6, true); //FIXME: IMPLEMENT DIRECTOR APPROVED
                    createFDPStatement.setString(7, FDPComments);

//                    createFDPStatement.execute();

                    //Get fdp id of this and store this in local variable
                    ResultSet keySet = createFDPStatement.executeQuery();
                    keySet.next();
                    currentFdpID = keySet.getInt(1);
                    System.out.println(currentFdpID);

                    rollback = false;

                    String addFDPRequestQuery = "INSERT INTO fdp_requests (fdp_id, recommended, requested_by_id) VALUES (?, FALSE, ?)";

                    PreparedStatement addFDPRequestStatement = mConnection.prepareStatement(addFDPRequestQuery);

                    addFDPRequestStatement.setInt(1, currentFdpID);
                    addFDPRequestStatement.setInt(2, mFacultyID);

                    addFDPRequestStatement.execute();

                }

            } catch (SQLException e) {
                AlertUtils.displaySQLErrorAlert(e, false);

                if (!rollback) {

                    try {

                        String dropFDPStatementQuery = "DELETE FROM fdp_details WHERE fdp_id = ?";

                        PreparedStatement dropStmt = mConnection.prepareStatement(dropFDPStatementQuery);

                        dropStmt.setInt(1, currentFdpID);

                        dropStmt.execute();

                    } catch (SQLException e1) {
                        AlertUtils.displaySQLErrorAlert(e, false);
                        return;
                    }
                }

                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Request submitted successfully. Please wait for your hod to respond.", ButtonType.OK);
            alert.setHeaderText("Success");
            alert.showAndWait();
        }

    }

    public void setFacultyID(int facultyID) {
        mFacultyID = facultyID;
    }

    @Override
    public void setOnSelection(String selection) {
        try {

            PreparedStatement statement = mConnection.prepareStatement("SELECT * FROM fdp_details WHERE fdp_name = ?");

            statement.setString(1, selection);

            ResultSet resultSet = statement.executeQuery();

            resultSet.next();

            //Convert from_date and to_date to LocalDates from java.sql.date
            Date fromDate = resultSet.getDate("date_from");
            Date toDate = resultSet.getDate("date_to");

            LocalDate fromLocalDate = fromDate.toLocalDate();
            LocalDate toLocalDate = toDate.toLocalDate();

            fdpCommentsTA.setText(resultSet.getString("comments"));
            dateFrom.setValue(fromLocalDate);
            dateTo.setValue(toLocalDate);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

