package admin.fragments.dbview;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import rest.Person;
import util.AlertUtils;
import util.ConnectionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    private String[] columns = {"Faculty ID", "Name", "Username", "Dept"};

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

        exportButton.setOnAction(event -> openDirectoryChooser());

        setupSearchField(dataTable, searchBarTF);
    }

    private void openDirectoryChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(searchBarTF.getScene().getWindow());

        if (selectedDirectory != null) System.out.println(selectedDirectory.getAbsolutePath());
        else {
            System.out.println("You selected nothing.");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a location.", ButtonType.OK);
            alert.setHeaderText("No location specified");
            alert.show();
            return;
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Faculty Details");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;

        for (Person person : mPeople) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(person.getFacultyId());
            row.createCell(1).setCellValue(person.getName());
            row.createCell(2).setCellValue(person.getUsername());
            row.createCell(3).setCellValue(person.getDept());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(selectedDirectory.getAbsolutePath() + "\\Faculty Details.xls");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (FileNotFoundException e) {
            AlertUtils.displaySQLErrorAlert(e, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @SuppressWarnings("Duplicates")
    private void setupSearchField(TableView<Person> tableView, JFXTextField searchBar) {
        FilteredList<Person> filteredList = new FilteredList<>(mPeople, p -> true);

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(person -> {
                if (person.getName().toLowerCase().contains(newValue.toLowerCase())) return true;

                if (person.getUsername().toLowerCase().contains(newValue.toLowerCase())) return true;

                if (person.getDept().toLowerCase().contains(newValue.toLowerCase())) return true;

                if (String.valueOf(person.getFacultyId()).contains(newValue)) ;

                return false;
            });
        });

        SortedList<Person> sortedList = new SortedList<>(filteredList);

        sortedList.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedList);
    }
}
