package seng202.team4.Services;

import javafx.scene.control.Alert;

/**
 * Contains static methods to quickly create Alert dialogs
 */
public class AlertDialog {

	/**
	 * Creates an Alert dialog with Warning theme.
	 *
	 * @param title Sets the title content of the dialog.
	 * @param header Sets the header content of the dialog.
	 * @param content Sets the content of the dialog.
	 */
	public static void showWarning(String title, String header, String content) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	/**
	 * Creates an Alert dialog with Information theme.
	 *
	 * @param title Sets the title content of the dialog.
	 * @param header Sets the header content of the dialog.
	 * @param content Sets the content of the dialog.
	 */
	public static void showInformation(String title, String header, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
