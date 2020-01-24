package seng202.team4.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import seng202.team4.Model.*;
import seng202.team4.Services.AlertDialog;

/**
 * Is instantiated when a AddWindow.fxml is loaded and allows the user to manually create a new
 * Bike Trip, Retailer, HotSpot or Location.
 *
 * Handles the methods regarding when the add Button is pressed in the Main Window, and then
 * the submit button within the Add Window.
 */
public class AddWindowController implements Initializable {

	private DataType locationType;

	//All components needed for access for the retailer pane
	@FXML
	public Pane retailerPane;
	@FXML
	public TextField retailerNameField;
	@FXML
	public TextField retailerAddressField;
	@FXML
	public TextField retailerCityField;
	@FXML
	public TextField retailerStateField;
	@FXML
	public TextField retailerZipField;
	@FXML
	public TextField retailerPrimTypeField;
	@FXML
	public TextField retailerSecTypeField;
	@FXML
	public Button retailerAddButton;

	//All components needed for access for the WiFi pane
	@FXML
	public Pane wifiPane;
	@FXML
	public TextField wifiNameField;
	@FXML
	public TextField wifiCityField;
	@FXML
	public TextField wifiBoroughField;
	@FXML
	public TextField wifiLocInfoField;
	@FXML
	public TextField wifiLocField;
	@FXML
	public TextField wifiPolicyField;
	@FXML
	public TextField wifiPolicyDescField;
	@FXML
	public TextField wifiProviderField;
	@FXML
	public Button hotSpotAddButton;

	//All components needed for access for the BikeTrip pane
	@FXML
	public Pane bikeTripPane;
	@FXML
	public TextField bikeIDField;
	@FXML
	public TextField startAddressField;
	@FXML
	public TextField endAddressField;

	@FXML
	public DatePicker startDateField;
	@FXML
	public DatePicker endDateField;
	@FXML
	public TextField startTimeField;
	@FXML
	public TextField endTimeField;
	@FXML
	public ComboBox startAMORPMField;
	@FXML
	public ComboBox endAMORPMField;


	@FXML
	public TextField genderField;
	@FXML
	public Slider ratingSlider;
	@FXML
	public Button bikeTripAddButton;
	@FXML
	public TextField bikeTripCityField;

	//All components needed for location pane
	@FXML
	public Pane locationPane;
	@FXML
	public TextField locationNameField;
	@FXML
	public TextField locationCityField;
	@FXML
	public Button locationAddButton;

	/**
	 * Initializes the Pane to be visible that is required when the GUI add window opens
	 *
	 * @param location Location of FXML file
	 * @param resources Target folder from where the file is held.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		switch (Main.getTabState()) {
			case RETAILER:
				retailerPane.setVisible(true);
				wifiPane.setVisible(false);
				bikeTripPane.setVisible(false);
				locationPane.setVisible(false);
				locationType = DataType.RETAILER;
				break;
			case HOTSPOT:
				retailerPane.setVisible(false);
				wifiPane.setVisible(true);
				bikeTripPane.setVisible(false);
				locationPane.setVisible(false);
				locationType = DataType.HOTSPOT;
				break;
			case BIKETRIP:
				retailerPane.setVisible(false);
				wifiPane.setVisible(false);
				bikeTripPane.setVisible(true);
				locationPane.setVisible(false);

				// Setting defaults for time selections
				startAMORPMField.setItems(FXCollections.observableArrayList(
					"AM", "PM "));
				endAMORPMField.setItems(FXCollections.observableArrayList(
					"AM", "PM "));

				LocalTime now = LocalTime.now();
				String Hour = String.valueOf(now.getHour());
				String Minute = String.valueOf(now.getMinute());
				if (Integer.parseInt(Minute) < 10) {
					Minute = "0" + Minute;
				}
				if (Integer.parseInt(Hour) > 12) {
					Hour = String.valueOf(Integer.parseInt(Hour) - 12);
					startAMORPMField.setValue("PM");
					endAMORPMField.setValue("PM");
				}
				if (Integer.parseInt(Hour) < 10) {
					Hour = "0" + Hour;
				}

				startTimeField.setText(Hour + ":" + Minute);
				endTimeField.setText(Hour + ":" + Minute);

				startDateField.setValue(LocalDate.now());
				endDateField.setValue(LocalDate.now());

				locationType = DataType.BIKETRIP;
				break;
			case LOCATION:
				retailerPane.setVisible(false);
				wifiPane.setVisible(false);
				bikeTripPane.setVisible(false);
				locationPane.setVisible(true);
				locationType = DataType.LOCATION;
				break;
			default:
				System.out.println("Unrecognised");
		}
	}

	/**
	 * Handles when the 'Add' Button is pressed for each type of location, creating a new location
	 * and adding it to the database or shows an error message if required fields are not filled, or
	 * invalid literals are entered.
	 */
	public void addEntryButton() {
		switch (locationType) {
			case RETAILER:
				try {

					int block = 69; //Unsure if to implement or not
					int lot = 69; //unsure if to implement or not

					if ((retailerAddressField.getText().trim().equals("")) || (retailerStateField
						.getText()
						.trim().equals(""))
						|| (retailerZipField.getText().trim().equals("")) || (retailerNameField
						.getText()
						.trim().equals(""))
						|| (retailerCityField.getText().trim().equals(""))) {
						AlertDialog.showWarning("Error", "Error Creating Retailer",
							"Please fill in all required fields with valid inputs! ");
						return;
					}
					Retailer newRetailer = Main.getCurrentDb().addRetailer(
						retailerNameField.getText(),
						retailerCityField.getText(),
						retailerAddressField.getText(),
						retailerStateField.getText(),
						Integer.parseInt(retailerZipField.getText()),
						retailerPrimTypeField.getText(),
						retailerSecTypeField.getText(),
						block, lot);
					System.out.println("Retailer Created!");
					MainController.getRetailList().add(newRetailer);
					Stage stage = (Stage) retailerAddButton.getScene().getWindow();
					stage.close();
				} catch (Exception e) {
					AlertDialog.showWarning("Error", "Error Creating Retailer",
						"Please fill in all required fields with valid inputs! ");
				}
				break;
			case HOTSPOT:
				try {
					if ((wifiBoroughField.getText().trim().equals("")) || (wifiCityField.getText()
						.trim()
						.equals(""))
						|| (wifiNameField.getText().trim().equals("")) || (wifiLocField.getText()
						.trim()
						.equals(""))) {
						AlertDialog.showWarning("Error", "Error Creating Hotspot",
							"Please fill in all required fields with valid inputs! ");
						return;
					}
					HotSpot newHotSpot = Main.getCurrentDb().addHotSpot(
						wifiNameField.getText(),
						wifiCityField.getText(),
						wifiBoroughField.getText(),
						wifiLocInfoField.getText(),
						wifiLocField.getText(),
						wifiPolicyField.getText(),
						wifiPolicyDescField.getText(),
						wifiProviderField.getText());
					System.out.println("Hotspot Created");
					MainController.getHotSpotList().add(newHotSpot);
					Stage stage = (Stage) hotSpotAddButton.getScene().getWindow();
					stage.close();
				} catch (Exception e) {
					AlertDialog.showWarning("Error", "Error Creating Hotspot",
						"Please fill in all required fields with valid inputs! ");
				}
				break;
			case BIKETRIP:

				try {
					LocalDate startDate = startDateField.getValue();
					LocalDate endDate = endDateField.getValue();

					String startTime = startTimeField.getText();
					String endTime = endTimeField.getText();

					String startAMORPM = startAMORPMField.getValue().toString();
					String endAMORPM = endAMORPMField.getValue().toString();

					if (startAMORPM.equals("PM")) {
						try {
							LocalTime startHour = LocalTime.parse(startTime);
							String hours = String.valueOf(startHour.getHour() + 12);
							String minutes = String.valueOf(startHour.getMinute());
							if (Integer.parseInt(minutes) < 10) {
								minutes = "0" + minutes;
							}
							startTime = hours + ":" + minutes;

						} catch (DateTimeParseException e) {
							e.printStackTrace();
							AlertDialog.showWarning("Error", "Error Creating Bike Trip",
								"Please ensure time is filled out in format 'HH:MM' \n" +
									" e.g '04:12'");
						}
					}
					if (endAMORPM.equals("PM")) {
						try {
							LocalTime endHour = LocalTime.parse(endTime);
							String hours = String.valueOf(endHour.getHour() + 12);
							String minutes = String.valueOf(endHour.getMinute());
							if (Integer.parseInt(minutes) < 10) {
								minutes = "0" + minutes;
							}
							endTime = hours + ":" + minutes;
						} catch (DateTimeParseException e) {
							e.printStackTrace();

						}

					}

					String finalStartTimeString = startDate + " " + startTime + " " + startAMORPM;
					String finalEndTimeString = endDate + " " + endTime + " " + endAMORPM;

					System.out.println(finalStartTimeString);
					System.out.println(finalEndTimeString);

					LocalDateTime finalStartTime;
					LocalDateTime finalEndTime;
					DateTimeFormatter formatter;

					formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a");

					finalStartTime = LocalDateTime.parse(finalStartTimeString, formatter);
					finalEndTime = LocalDateTime.parse(finalEndTimeString, formatter);

					if ((bikeTripCityField.getText().trim().equals("")) || (bikeIDField.getText()
						.trim()
						.equals(""))
						|| (startAddressField.getText().trim().equals("")) || (endAddressField
						.getText().trim().equals(""))) {
						AlertDialog.showWarning("Error", "Error Creating Bike Trip",
							"Please fill in all required fields with valid inputs! ");
						return;
					}
					//Edit time inputs
					BikeTrip newBikeTrip = Main.getCurrentDb().addBikeTrip(
						Integer.parseInt(bikeIDField.getText()),
						startAddressField.getText(),
						endAddressField.getText(),
						bikeTripCityField.getText(),
						finalStartTime,
						finalEndTime,
						genderField.getText(),
						(int) ratingSlider.getValue());
					Stage stage = (Stage) bikeTripAddButton.getScene().getWindow();
					MainController.getBikeTripList().add(newBikeTrip);
					stage.close();
					System.out.println("Bike Trip Created!");
				} catch (DateTimeParseException e) {
					e.printStackTrace();
					AlertDialog.showWarning("Error", "Error Creating Bike Trip",
						"Please ensure time is filled out in format 'HH:MM' \n" +
							"e.g '04:12'");
				} catch (Exception e) {
					e.printStackTrace();
					AlertDialog.showWarning("Error", "Error Creating Bike Trip",
						"Please fill in all required fields with valid inputs! ");

				}

				break;
			case LOCATION:

				try {
					if ((locationCityField.getText()
						.trim()
						.equals(""))
						|| (locationNameField.getText().trim().equals(""))) {
						AlertDialog.showWarning("Error", "Error Creating Location",
							"Please fill in all required fields with valid inputs! ");
						return;
					}
					Location newLocation = Main.getCurrentDb().addLocation(
						locationNameField.getText(),
						locationCityField.getText());

					System.out.println("Location Created");
					MainController.getLocationList().add(newLocation);
					Stage stage = (Stage) locationAddButton.getScene().getWindow();
					stage.close();

				} catch (Exception e) {
					AlertDialog.showWarning("Error", "Error Creating Location",
						"Please fill in all required fields with valid inputs! ");
				}
				break;

			default:
				System.out.println("Ooppsie error time babay");
		}
	}
}
