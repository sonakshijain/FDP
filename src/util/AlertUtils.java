package util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

public class AlertUtils {

    public static void displaySQLErrorAlert(Exception e, boolean exit) {

        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), new ButtonType("Exit"));
        alert.setHeaderText("Database Error");
//        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();

        if (exit) System.exit(1);
    }

    public static void displayConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, new ButtonType("OK"));
        alert.setHeaderText("Alert");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}
