package faculty.fragments.submitfdprequestsview;

import com.jfoenix.controls.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import util.AlertUtils;
import util.ConnectionUtils;
import util.DialogUtils;

import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static java.time.temporal.ChronoUnit.DAYS;

public class SubmitFDPRequestController implements Initializable{

    @FXML private StackPane root;
    @FXML private JFXToolbar toolbar;
    @FXML private JFXTextField fdpNameTF;
    @FXML private JFXTextArea fdpCommentsTA;
    @FXML private JFXDatePicker dateFrom;
    @FXML private JFXDatePicker dateTo;
    @FXML private Label daysCountLabel;
    @FXML private JFXButton submitButton;

    private Connection mConnection;
    private int mFacultyID;

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

    }

    public void handleSubmit(ActionEvent actionEvent) {

        if (fdpNameTF.getText().equals("") || dateFrom.getValue() == null || dateTo.getValue() == null) {
            DialogUtils.createDialog(root, "PLEASE RETRY", "Please ensure none of the fields are empty.", new JFXButton("OK") {{
                setStyle("-fx-background-color: RED"); //FIXME: COLOR BHADDDDDAAA HAI
            }});
        } else {
            String FDPName = fdpNameTF.getText();
            String FDPComments = fdpCommentsTA.getText();
            LocalDate dateF = dateFrom.getValue();
            LocalDate dateT = dateTo.getValue();

            boolean rollback = true;

            try {

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Please recheck the data before submitting.", ButtonType.YES, ButtonType.NO);
                confirmation.setHeaderText("Submit?");
                confirmation.showAndWait();

                if (confirmation.getResult() == ButtonType.YES) {

                    String createFDPStatementQuery = "INSERT INTO fdp_details (fdp_name, date_from, date_to, days, hod_rec, dir_app, comments) VALUES (?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement createFDPStatement = mConnection.prepareStatement(createFDPStatementQuery);

                    createFDPStatement.setString(1, FDPName);
                    createFDPStatement.setDate(2, Date.valueOf(dateF));
                    createFDPStatement.setDate(3, Date.valueOf(dateT));
                    createFDPStatement.setInt(4, (int) DAYS.between(dateFrom.getValue(), dateTo.getValue()));
                    createFDPStatement.setBoolean(5, false);
                    createFDPStatement.setBoolean(6, true); //FIXME: IMPLEMENT DIRECTOR APPROVED
                    createFDPStatement.setString(7, FDPComments);

                    createFDPStatement.execute();
                    rollback = false;

                    String addFDPRequestQuery = "INSERT INTO fdp_requests (fdp_id, recommended, requested_by_id) VALUES ((SELECT fdp_id FROM fdp_details WHERE fdp_name = ?), FALSE, ?)";

                    PreparedStatement addFDPRequestStatement = mConnection.prepareStatement(addFDPRequestQuery);

                    addFDPRequestStatement.setString(1, FDPName);
                    System.out.println("SFRC mFacultyID = " + mFacultyID);
                    addFDPRequestStatement.setInt(2, mFacultyID);

                    addFDPRequestStatement.execute();

                }

            } catch (SQLException e) {
                AlertUtils.displaySQLErrorAlert(e, false);

                if (!rollback) {

                    try {

                        String dropFDPStatementQuery = "DELETE FROM fdp_details WHERE fdp_name = ?";

                        PreparedStatement dropStmt = mConnection.prepareStatement(dropFDPStatementQuery);

                        dropStmt.setString(1, fdpNameTF.getText());

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
}

