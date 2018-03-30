package hod.confirmfdpdialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import rest.Fdp;
import util.AlertUtils;
import util.ConnectionUtils;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

import static javafx.scene.control.ButtonType.FINISH;
import static javafx.scene.control.ButtonType.OK;
import static javafx.scene.control.ButtonType.YES;

public class ConfirmFDPDialogController implements Initializable {

    @FXML private JFXTextField fdpNameTF;
    @FXML private JFXTextArea detailsTF;
    @FXML private JFXDatePicker fromDate;
    @FXML private JFXDatePicker toDate;
    @FXML private Label durationLabel;
    @FXML private JFXTextArea remarksTA;
    @FXML private JFXButton resetButton;
    @FXML private JFXButton acceptButton;
    @FXML private JFXButton denyButton;
    @FXML private Label facultyNameLabel;

    private Fdp mCurrentFdp;
    private Connection mConnection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            mConnection = ConnectionUtils.getDBConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private LocalDate getLocalDateFromDate(Date date) {
        Instant instant = Instant.ofEpochMilli(date.getTime());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDate localDate = localDateTime.toLocalDate();
        return localDate;
    }

    public void setCurrentFdp(Fdp currentFdp) {
        mCurrentFdp = currentFdp;

        fillDetails();
    }

    private void fillDetails() {
        fdpNameTF.setText(mCurrentFdp.getFdpName());
        detailsTF.setText(mCurrentFdp.getComments());
        fromDate.setValue(getLocalDateFromDate(mCurrentFdp.dateFromProperty.get()));
        toDate.setValue(getLocalDateFromDate(mCurrentFdp.dateToProperty.get()));
        durationLabel.setText(mCurrentFdp.days.get() + " Days");
        facultyNameLabel.setText(mCurrentFdp.requestedByName.getValue());
    }

    public void resetDetails(ActionEvent actionEvent) {
        fillDetails();
    }


    public void submitRequest(ActionEvent actionEvent) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Accept the FDP Request?", YES, ButtonType.NO);
        alert.setHeaderText("Confirmation");
        alert.showAndWait();

        if (alert.getResult() == YES) {

            boolean completedWithoutErrors = false;

            try {

                PreparedStatement fdpDetailsUpdateStmt = mConnection.prepareStatement("UPDATE fdp_details SET " +
                        "fdp_name = ?, " +
                        "comments = ?, " +
                        "date_from = ?, " +
                        "date_to = ?, " +
                        "days = ?, " +
                        "hod_rec = TRUE " +
                        "WHERE fdp_id = ?");

                fdpDetailsUpdateStmt.setString(1, fdpNameTF.getText());
                fdpDetailsUpdateStmt.setString(2, detailsTF.getText());
                fdpDetailsUpdateStmt.setDate(3, java.sql.Date.valueOf(fromDate.getValue()));
                fdpDetailsUpdateStmt.setDate(4, java.sql.Date.valueOf(fromDate.getValue()));
                fdpDetailsUpdateStmt.setInt(5, (int) durationLabel.getText().charAt(0));
                fdpDetailsUpdateStmt.setInt(6, mCurrentFdp.getFdpID());

                fdpDetailsUpdateStmt.execute();

                PreparedStatement remarksUpdateStmt = mConnection.prepareStatement("UPDATE fdp_requests SET " +
                        "remarks = ?" +
                        "WHERE fdp_id = ?");

                remarksUpdateStmt.setString(1, remarksTA.getText());
                remarksUpdateStmt.setInt(2, mCurrentFdp.getFdpID());

                remarksUpdateStmt.execute();

                completedWithoutErrors = true;

            } catch (SQLException e) {

                AlertUtils.displaySQLErrorAlert(e, false);

                if (!completedWithoutErrors) {
                    //TODO
                }

            } finally {

                if (completedWithoutErrors) {
                    Alert finalAlert = new Alert(Alert.AlertType.INFORMATION, "Request accepted successfully.", ButtonType.OK);
                    finalAlert.setHeaderText("Success");
                    finalAlert.showAndWait();
                }

            }

        }



    }
}
