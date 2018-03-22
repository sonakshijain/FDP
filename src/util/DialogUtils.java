package util;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DialogUtils {

    public static final int CONFIRMATION = 1;
    public static final int ERROR = 2;
    public static final int WARNING = 3;
    public static final int YESNO = 4;

    public static void createDialog(StackPane parent, String heading, String content, Node... actions) {
        JFXDialogLayout dialogLayout = new JFXDialogLayout();
        dialogLayout.setHeading(new Label(heading));
        dialogLayout.setBody(new Label(content));
        dialogLayout.setActions(actions);

        dialogLayout.getStylesheets().add(DialogUtils.class.getResource("DialogStyle.css").toExternalForm());

        JFXDialog dialog = new JFXDialog(parent, dialogLayout, JFXDialog.DialogTransition.CENTER);

        for (Node action: actions) {
            action.setOnMousePressed(event -> dialog.close());
        }

        dialog.show();
    }

    public static void createPresetDialog(StackPane parent, int dialogType, String content) {
        switch (dialogType) {

            case CONFIRMATION: {
                createDialog(parent, "ALERT", content, new JFXButton("OK"));
                break;
            }

            case ERROR: {
                createDialog(parent, "ERROR", content, new JFXButton("OK"));
                break;
            }

            case YESNO: {
                createDialog(parent, "ALERT", content, new JFXButton("NO"), new JFXButton("YES"));
                break;
            }

            case WARNING: {
                createDialog(parent, "WARNING", content, new JFXButton("OK"));
                break;
            }
        }
    }

}
