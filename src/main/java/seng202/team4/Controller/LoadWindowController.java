package seng202.team4.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

/**
 * A controller class for the loading progress window shown while retailer locations are geocoded.
 */
public class LoadWindowController {

	private Stage stage;
	private int toGeocode;

	@FXML
	public Label headerLabel;
	@FXML
	public Label statusLabel;
	@FXML
	public ProgressBar progressBar;
	@FXML
	public Button closeButton;

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void hideWindow() {
		stage.hide();
	}

	/**
	 * Show the load dialog with a number of entries to geocode.
	 *
	 * @param toGeocode The number of entries to be geocoded
	 */
	public void startDialog(int toGeocode) {
		this.toGeocode = toGeocode;
		updateDialog(0);
		headerLabel.setText("Loading Data");
		closeButton.setDisable(true);
		stage.show();
	}

	/**
	 * Update the amount of geocodes complete.
	 *
	 * @param geocodesDone The amount of geocodes complete
	 */
	public void updateDialog(int geocodesDone) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				statusLabel.setText(
					String.format("Geocoding Retailers (%d/%d complete)", geocodesDone, toGeocode));
				progressBar.setProgress(((double) geocodesDone) / toGeocode);
			}
		});
	}

	/**
	 * Show a done message.
	 */
	public void finishDialog() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				headerLabel.setText("Loading Done");
				statusLabel.setText("Geocoding Complete.");
				closeButton.setDisable(false);
			}
		});
	}
}
