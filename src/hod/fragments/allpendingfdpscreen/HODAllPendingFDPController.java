package hod.fragments.allpendingfdpscreen;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXTextField;
import hod.confirmfdpdialog.ConfirmFDPDialogController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import rest.Fdp;
import util.AlertUtils;
import util.ConnectionUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class HODAllPendingFDPController implements Initializable {

    @FXML
    public TableView<Fdp> allFDPsView;
    @FXML
    public TableColumn<Fdp, String> idCol;
    @FXML
    public TableColumn<Fdp, String> aboutCol;
    @FXML
    public TableColumn<Fdp, Date> dateFromCol;
    @FXML
    public TableColumn<Fdp, Date> dateToCol;
    @FXML
    public TableColumn<Fdp, Number> daysCol;
    @FXML
    public TableColumn<Fdp, String> requestedByCol;
    @FXML
    public JFXTextField searchBar;
    @FXML
    public JFXButton refreshButton;

    private ObservableList<Fdp> mFdpRequests = FXCollections.observableArrayList();

    private Connection mConnection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            mConnection = ConnectionUtils.getDBConnection();
        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, true);
        }

        initTreeTableView();

        setupSearchField(allFDPsView, searchBar);

//        setAutoRefresh();
    }

    private void setAutoRefresh() {
        //Refresh automatically
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                refreshTable(null);
            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    private void initTreeTableView() {

        idCol.setCellValueFactory(new PropertyValueFactory<>("fdpName"));
        aboutCol.setCellValueFactory(new PropertyValueFactory<>("comments"));
        dateFromCol.setCellValueFactory(new PropertyValueFactory<>("dateFromProperty"));
        dateToCol.setCellValueFactory(new PropertyValueFactory<>("dateToProperty"));
        daysCol.setCellValueFactory(new PropertyValueFactory<>("days"));
        requestedByCol.setCellValueFactory(new PropertyValueFactory<>("requestedByName"));

        loadRequests();

        allFDPsView.setItems(mFdpRequests);

        idCol.prefWidthProperty().bind(allFDPsView.widthProperty().multiply(0.2));
        aboutCol.prefWidthProperty().bind(allFDPsView.widthProperty().multiply(0.3));
        dateFromCol.prefWidthProperty().bind(allFDPsView.widthProperty().multiply(0.1));
        dateFromCol.prefWidthProperty().bind(allFDPsView.widthProperty().multiply(0.1));
        daysCol.prefWidthProperty().bind(allFDPsView.widthProperty().multiply(0.1));
        requestedByCol.prefWidthProperty().bind(allFDPsView.widthProperty().multiply(0.2));

        allFDPsView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                Fdp selectedFdp = allFDPsView.getSelectionModel().getSelectedItem();
                try {
                    openFDPAbout(selectedFdp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void openFDPAbout(Fdp selectedFdp) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../confirmfdpdialog/ConfirmFDPDialog.fxml"));
        Parent root = loader.load();

        ConfirmFDPDialogController controller = loader.getController();
        controller.setCurrentFdp(selectedFdp);

        Stage stage = new Stage();

        JFXDecorator decorator = new JFXDecorator(stage, root);
        decorator.setTitle("Requested FDP's");
        Scene scene = new Scene(decorator);

        stage.setOnHidden(event -> {
            refreshTable(null);
            System.out.println("Refreshed.");
            stage.close();
        });

        stage.setScene(scene);

        stage.showAndWait();

    }

    private void loadRequests() {

        try {

            String loadFDPDetailsQuery = "SELECT * FROM fdp_details JOIN fdp_requests request ON fdp_details.fdp_id = request.fdp_id WHERE seen_by_hod = FALSE;";

            PreparedStatement FDPDetailsStmt = mConnection.prepareStatement(loadFDPDetailsQuery);

            ResultSet resultSet = FDPDetailsStmt.executeQuery();

            while (resultSet.next()) {

                Statement statement = mConnection.createStatement();
                ResultSet resultSetInner = statement.executeQuery("SELECT fullname FROM faculty_details WHERE faculty_id = " + resultSet.getInt("requested_by_id") + ";");

                resultSetInner.next();

                String facultyName = resultSetInner.getString("fullname");

                Fdp fdp = new Fdp(
                        resultSet.getInt("fdp_id"),
                        resultSet.getString("fdp_name"),
                        resultSet.getDate("date_from"),
                        resultSet.getDate("date_to"),
                        resultSet.getInt("days"),
                        resultSet.getBoolean("hod_rec"),
                        resultSet.getString("comments"),
                        resultSet.getBoolean("dir_app"),
                        resultSet.getInt("requested_by_id"),
                        facultyName,
                        resultSet.getString("remarks")
                );

                mFdpRequests.add(fdp);
            }

        } catch (SQLException e) {
            AlertUtils.displaySQLErrorAlert(e, false);
        }
    }

    private void setupSearchField(final TableView<Fdp> tableView, final JFXTextField searchBar) {
        FilteredList<Fdp> filteredList = new FilteredList<>(mFdpRequests, p -> true);

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(fdp -> {
                if (newValue == null || newValue.isEmpty()) return true; //if empty, display all

                if (String.valueOf(fdp.fdpName).toLowerCase().contains(newValue.toLowerCase()))
                    return true; // fdpName match

                if (String.valueOf(fdp.requestedByName).toLowerCase().contains(newValue.toLowerCase()))
                    return true; //RequestedBy Matched

                if (String.valueOf(fdp.dateFromProperty.getValue().toString()).contains(newValue)) return true;

                return String.valueOf(fdp.comments).toLowerCase().contains(newValue.toLowerCase());

            });
        });

        SortedList<Fdp> sortedList = new SortedList<>(filteredList);

        sortedList.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedList);
    }

    public void refreshTable(ActionEvent actionEvent) {
        mFdpRequests.clear();
        loadRequests();
        allFDPsView.refresh();
    }
}

