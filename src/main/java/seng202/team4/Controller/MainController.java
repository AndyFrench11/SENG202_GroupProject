package seng202.team4.Controller;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LocalDateTimeStringConverter;
import seng202.team4.Model.*;
import seng202.team4.Services.AlertDialog;
import seng202.team4.Services.Geocoder;
import seng202.team4.Services.SQLHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Is instantiated when a Main.fxml is loaded and allows interaction with the Main Window of the application.
 *
 * Handles methods relating to:
 * - Visualising the Map Viewer
 * - Displaying all raw data
 * - Importing Data
 * - All other funcitonality of the application
 */
public class MainController implements Initializable {

	/////////////////////**************************************************////////////////////////////////
	//this section is for handling data from the database
	/////////////////////**************************************************////////////////////////////////
	private static ObservableList<BikeTrip> bikeTripList;
	private static ObservableList<Retailer> retailList;
	private static ObservableList<HotSpot> hotSpotList;
	private static ObservableList<Location> locationList;

	public static ObservableList<BikeTrip> getBikeTripList() {
		return bikeTripList;
	}

	public static ObservableList<HotSpot> getHotSpotList() {
		return hotSpotList;
	}

	public static ObservableList<Location> getLocationList() {
		return locationList;
	}

	public static ObservableList<Retailer> getRetailList() {
		return retailList;
	}

	/////////////////////**************************************************////////////////////////////////
	//this section declares the GUI elements
	/////////////////////**************************************************////////////////////////////////

	private MapController mapController;


	@FXML
	public AnchorPane gMapsAnchorPane;

	//Main application window
	@FXML
	public Pane welcomePane;
	@FXML
	public Pane planRoutePane;
	@FXML
	public AnchorPane rawDataViewerPane;
	@FXML
	public Button planRouteBtn;
	@FXML
	public Button viewDataBtn;

	@FXML
	public VBox buttonBox;
	@FXML
	public Button helpButton;
	@FXML
	public Button quitButton;


	//create database pane controls
	@FXML
	public TextField databaseNameField;
	@FXML
	public Button createDatabaseButton;
	@FXML
	public Label databaseLabel;

	//Database list
	private List<String> databaseList = new ArrayList<>();

	//File Menu Items
	@FXML
	public MenuItem fileHome;
	@FXML
	public MenuItem importData;
	@FXML
	public MenuItem createNewDatabase;
	@FXML
	public MenuItem chooseDatabase;
	@FXML
	public MenuItem currentDBMenu;
	@FXML
	public MenuItem deleteDatabaseMenu;

	//Declaring the Tab pane
	@FXML
	public Tab bikeTripTab;
	@FXML
	public Tab retailerTab;
	@FXML
	public Tab hotSpotTab;
	@FXML
	public Tab LocationTab;

	//Bike trips tab
	@FXML
	public TableView<BikeTrip> bikeTripTableView;
	@FXML
	public TableColumn<BikeTrip, Integer> dataBikeID;
	@FXML
	public TableColumn<BikeTrip, Boolean> bikeTripFavourite;
	@FXML
	public TableColumn<BikeTrip, Long> dataDuration;
	@FXML
	public TableColumn<BikeTrip, Double> dataDistance;
	@FXML
	public TableColumn<BikeTrip, String> dataGender;
	@FXML
	public TableColumn<TableColumn, TableColumn> startLocation;
	@FXML
	public TableColumn<BikeTrip, Double> startLatitude;
	@FXML
	public TableColumn<BikeTrip, Double> startLongitude;
	@FXML
	public TableColumn<TableColumn, TableColumn> endLocation;
	@FXML
	public TableColumn<BikeTrip, Double> endLatitude;
	@FXML
	public TableColumn<BikeTrip, Double> endLongitude;
	@FXML
	public TableColumn<BikeTrip, Double> caloriesBurnt;
	@FXML
	public TableColumn<BikeTrip, Double> avgSpeed;
	@FXML
	public TableColumn<BikeTrip, Double> rating;
	@FXML
	public TableColumn<BikeTrip, LocalDateTime> startTime;
	@FXML
	public TableColumn<BikeTrip, LocalDateTime> endTime;
	@FXML
	public Button addBikeTripButton;
	@FXML
	public Button editBikeTripButton;

	@FXML
	public Button endBikeTripEditButton;
	@FXML
	public Button tripUpdateButton;
	@FXML
	public TextField startAddressSearch;
	@FXML
	public TextField endAddressSearch;
	@FXML
	public TextField tripCitySearch;
	@FXML
	public TextField tripBikeIdSearch;
	@FXML
	public TextField minTripDurationSearch;
	@FXML
	public TextField maxTripDurationSearch;
	@FXML
	public TextField minTripDistanceSearch;
	@FXML
	public TextField maxTripDistanceSearch;
	@FXML
	public TextField tripGenderSearch;
	@FXML
	public TextField ratingSearch;
	@FXML
	public Button searchBikeTripsButton;
	@FXML
	public ProgressIndicator tripSearchProgressIndicator;
	@FXML
	public Button clearFilterBikeTripsButton;
	@FXML
	public RadioButton favoriteTrips;
	@FXML
	public Button displayTripButton; //TODO delete this if it is not used
	@FXML
	public Button displayAllTripsButton; //TODO delete this if it is not used


	//Retailer tab
	@FXML
	public TableView<Retailer> retailerTableView;
	@FXML
	public TableColumn<Retailer, String> retailerName;
	@FXML
	public TableColumn<Retailer, Boolean> retailerFavourite;
	@FXML
	public TableColumn<Retailer, String> retailPrType;
	@FXML
	public TableColumn<Retailer, String> retailScType;
	@FXML
	public TableColumn<Retailer, String> retailStr;
	@FXML
	public TableColumn<Retailer, String> retailState;
	@FXML
	public TableColumn<Retailer, Integer> retailZip;
	@FXML
	public TableColumn<Retailer, Double> retailLongitude;
	@FXML
	public TableColumn<Retailer, Double> retailLatitude;
	@FXML
	public TableColumn<Retailer, String> retailCity;
	@FXML
	public TableColumn<Retailer, Integer> retailBlock;
	@FXML
	public TableColumn<Retailer, Integer> retailLot;
	@FXML
	public Button addRetailerButton;
	@FXML
	public Button editRetailerButton;
	@FXML
	public Button endRetailerEditButton;
	@FXML
	public TextField retailerPrimarySearch;
	@FXML
	public TextField retailerSecondarySearch;
	@FXML
	public TextField retailerStreetSearch;
	@FXML
	public TextField retailerStateSearch;
	@FXML
	public TextField retailerZipSearch;
	@FXML
	public TextField retailerCitySearch;
	@FXML
	public Button searchRetailersButton;
	@FXML
	public ProgressIndicator retailerProgressIndicator;
	@FXML
	public TextField retailerSearchNameField;
	@FXML
	public RadioButton favoriteRetailer;
	@FXML
	public Button clearFilterRetailersButton;
	@FXML
	public Button displayAllRetailersButton;

	//Hot spots tab
	@FXML
	public TableView<HotSpot> hotSpotTableView;
	@FXML
	public TableColumn<HotSpot, String> hotSpotName;
	@FXML
	public TableColumn<HotSpot, Boolean> hotSpotFavourite;
	@FXML
	public TableColumn<HotSpot, String> hotSpotBorough;
	@FXML
	public TableColumn<HotSpot, String> hotSpotLocationInfo;
	@FXML
	public TableColumn<HotSpot, String> hotSpotLocationType;
	@FXML
	public TableColumn<HotSpot, String> hotSpotPolicy;
	@FXML
	public TableColumn<HotSpot, String> hotSpotPolicyDisc;
	@FXML
	public TableColumn<HotSpot, String> hotSpotProvider;
	@FXML
	public TableColumn<HotSpot, Double> hotSpotLatitude;
	@FXML
	public TableColumn<HotSpot, Double> hotSpotLongitude;
	@FXML
	public Button addHotSpotButton;
	@FXML
	public Button editHotSpotButton;
	@FXML
	public Button endHotSpotEditButton;
	@FXML
	public TextField hotSpotSearchNameField;
	@FXML
	public TextField hotspotBoroughSearch;
	@FXML
	public TextField hotspotLocationSearch;
	@FXML
	public TextField hotspotLocationTypeSearch;
	@FXML
	public TextField hotspotPolicySearch;
	@FXML
	public TextField hotspotPolicyDescSearch;
	@FXML
	public TextField hotspotProviderSearch;
	@FXML
	public Button searchHotspotsButton;
	@FXML
	public RadioButton favoriteHotSpot;
	@FXML
	public Button clearFilterHotSpotsButton;
	@FXML
	public Button displayAllHotspotsButton;

	//Location tab
	@FXML
	public TableView<Location> locationTableView;
	@FXML
	public TableColumn<Location, String> locationName;
	@FXML
	public TableColumn<Location, Boolean> locationFavourite;
	@FXML
	public TableColumn<Location, String> locationAddress;
	@FXML
	public TableColumn<Location, String> locationCity;
	@FXML
	public TableColumn<Location, Double> locationLongitude;
	@FXML
	public TableColumn<Location, Double> locationLatitude;
	@FXML
	public Button addLocationButton;
	@FXML
	public Button clearLocationFilterButton;
	@FXML
	public Button displayAllLocationsButton;
	@FXML
	public TextField locationSearchNameField;
	@FXML
	public MenuItem filterBikeTripsMenu;
	@FXML
	public Menu filterMenu;
	@FXML
	public MenuItem clearAllFiltersMenu;
	@FXML
	public MenuItem closeMenu;
	@FXML
	public TextField locationCitySearchField;
	@FXML
	public TextField locationAddressSearchField;
	@FXML
	public Button searchLocationsButton;
	@FXML
	public RadioButton favoriteLocation;

	//Help/Settings Menu items
	@FXML
	public Menu settingsMenu;
	@FXML
	public MenuItem helpItem;

	private Stage stage;

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/////////////////////**************************************************////////////////////////////////
	//this section declares the GUI buttons action results
	/////////////////////**************************************************////////////////////////////////

	public void closeApplication() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Quitting?");
		alert.setHeaderText("");
		alert.setContentText("Are you sure you would like to quit RouteMe?");
		Optional<ButtonType> confirmationResult = alert.showAndWait();
		if (confirmationResult.get() == ButtonType.OK) {
			// OK DELETE
			System.exit(0);

		} else {
			// NO BACK UP THE TRUCK DONT DELEte
		}
	}

	/**
	 * Changes to the GMaps view.
	 */
	public void planRoute() {
		welcomePane.setDisable(true);
		welcomePane.setVisible(false);

		rawDataViewerPane.setDisable(true);
		rawDataViewerPane.setVisible(false);


		planRoutePane.setDisable(false);
		planRoutePane.setVisible(true);



	}

	/**
	 * Switches to the data viewer tables.
	 */
	public void viewRawData() {
		welcomePane.setDisable(true);
		welcomePane.setVisible(false);

		rawDataViewerPane.setDisable(false);
		rawDataViewerPane.setVisible(true);

		planRoutePane.setDisable(true);
		planRoutePane.setVisible(false);

		bikeTripTableView.setEditable(false);
		retailerTableView.setEditable(false);
		hotSpotTableView.setEditable(false);
	}

	/**
	 * File - Home: Takes user back to the welcome screen.
	 */
	public void goToHomeScreen() {
		welcomePane.setDisable(false);
		welcomePane.setVisible(true);

		rawDataViewerPane.setDisable(true);
		rawDataViewerPane.setVisible(false);

		planRoutePane.setDisable(true);
		planRoutePane.setVisible(false);
	}

	/**
	 * File - Import: Opens a window to navigate to the CSV file user wants to import.
	 *
	 * @param csvType The type of CSV file they are trying to import
	 * @return The file path of the CSV to import
	 */
	private String getSelectedFilePath(DataType csvType) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter fileExtensions =
			new FileChooser.ExtensionFilter(
				"CSV Files", "*.csv");

		fileChooser.getExtensionFilters().add(fileExtensions);
		switch (csvType) {
			case BIKETRIP:
				fileChooser.setTitle("Open Bike Trip File");
				break;
			case HOTSPOT:
				fileChooser.setTitle("Open HotSpot File");
				break;
			case RETAILER:
				fileChooser.setTitle("Open Retailer File");
				break;
			default:
				throw new IllegalArgumentException("Not a valid CSV import type.");
		}

		try {
			File file = fileChooser.showOpenDialog(stage);
			return file.getAbsolutePath();
		} catch (NullPointerException e) {
			return null;
		}

	}

	/**
	 * Searches the DB according to the inputted trip search fields.
	 *
	 * All field searches are 'contains' and are not independent.
	 */
	public void searchBikeTrips() {
		tripSearchProgressIndicator.setVisible(true);
		String entireWhere = "";
		Boolean firstSearchClause = true;
		Alert missingRequirement = new Alert(Alert.AlertType.ERROR);
		missingRequirement.setHeaderText("Missing a required field");
		//ID search
		if (!tripBikeIdSearch.getText().trim().isEmpty()) {
			//BikeId search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND bikeId LIKE '%" + tripBikeIdSearch.getText().trim() + "%'";
			} else {
				entireWhere += "(bikeId LIKE '%" + tripBikeIdSearch.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}
		//Start/end location search
		if (!startAddressSearch.getText().trim().isEmpty() || !endAddressSearch.getText().trim()
			.isEmpty()) {
			if (tripCitySearch.getText().trim().isEmpty()) {
				missingRequirement
					.setContentText("Please enter a city when searching for an address.");
				missingRequirement.showAndWait();
			} else {
				NumberFormat formatter = new DecimalFormat("#0.00000");
				if (!firstSearchClause) {
					//start location only
					if (!startAddressSearch.getText().trim().isEmpty() && endAddressSearch.getText()
						.trim().isEmpty()) {
						String startTrip =
							startAddressSearch.getText() + " " + tripCitySearch.getText();
						double[] latLong = Geocoder.getLatitudeLongitude(startTrip);
						entireWhere +=
							"AND startLatitude BETWEEN '" + formatter.format(latLong[0] - 0.0010) +
								"' AND '" + formatter.format(latLong[0] + 0.0010)
								+ "' AND startLongitude BETWEEN '"
								+ formatter.format(latLong[1] - 0.0010) + "' AND '" +
								formatter.format(latLong[1] + 0.0010) + "'";
						//end location only
					} else if (startAddressSearch.getText().trim().isEmpty() && !endAddressSearch
						.getText().trim().isEmpty()) {
						String endTrip =
							endAddressSearch.getText() + " " + tripCitySearch.getText();
						double[] latLong = Geocoder.getLatitudeLongitude(endTrip);
						entireWhere +=
							"AND endLatitude BETWEEN '" + formatter.format(latLong[0] - 0.0010) +
								"' AND '" + formatter.format(latLong[0] + 0.0010)
								+ "' AND endLongitude BETWEEN '"
								+ formatter.format(latLong[1] - 0.0010) + "' AND '" +
								formatter.format(latLong[1] + 0.0010) + "'";
						//start and end location
					} else {
						String endTrip =
							endAddressSearch.getText() + " " + tripCitySearch.getText();
						String startTrip =
							startAddressSearch.getText() + " " + tripCitySearch.getText();
						double[] endLatLong = Geocoder.getLatitudeLongitude(endTrip);
						double[] startLatLong = Geocoder.getLatitudeLongitude(startTrip);
						entireWhere += "AND startLatitude BETWEEN '" + formatter
							.format(startLatLong[0] - 0.0010) +
							"' AND '" + formatter.format(startLatLong[0] + 0.0010)
							+ "' AND startLongitude BETWEEN '"
							+ formatter.format(startLatLong[1] - 0.0010) + "' AND '" +
							formatter.format(startLatLong[1] + 0.0010) + "AND endLatitude BETWEEN '"
							+
							formatter.format(endLatLong[0] - 0.0010) +
							"' AND '" + formatter.format(endLatLong[0] + 0.0010)
							+ "' AND endLongitude BETWEEN '"
							+ formatter.format(endLatLong[1] - 0.0010) + "' AND '" +
							formatter.format(endLatLong[1] + 0.0010) + "'";
					}
				} else {
					//start location only
					if (!startAddressSearch.getText().trim().isEmpty() && endAddressSearch.getText()
						.trim().isEmpty()) {
						String location =
							startAddressSearch.getText() + " " + tripCitySearch.getText();
						double[] latLong = Geocoder.getLatitudeLongitude(location);
						entireWhere +=
							"(startLatitude BETWEEN '" + formatter.format(latLong[0] - 0.0010) +
								"' AND '" + formatter.format(latLong[0] + 0.0010)
								+ "' AND startLongitude BETWEEN '"
								+ formatter.format(latLong[1] - 0.0010) + "' AND '" +
								formatter.format(latLong[1] + 0.0010) + "'";
						//end location only
					} else if (startAddressSearch.getText().trim().isEmpty() && !endAddressSearch
						.getText().trim().isEmpty()) {
						String location =
							endAddressSearch.getText() + " " + tripCitySearch.getText();
						double[] latLong = Geocoder.getLatitudeLongitude(location);
						entireWhere +=
							"(endLatitude BETWEEN '" + formatter.format(latLong[0] - 0.0010) +
								"' AND '" + formatter.format(latLong[0] + 0.0010)
								+ "' AND endLongitude BETWEEN '"
								+ formatter.format(latLong[1] - 0.0010) + "' AND '" +
								formatter.format(latLong[1] + 0.0010) + "'";
						//start and end location
					} else {
						String endTrip =
							endAddressSearch.getText() + " " + tripCitySearch.getText();
						String startTrip =
							startAddressSearch.getText() + " " + tripCitySearch.getText();
						double[] endLatLong = Geocoder.getLatitudeLongitude(endTrip);
						double[] startLatLong = Geocoder.getLatitudeLongitude(startTrip);
						entireWhere +=
							"(startLatitude BETWEEN '" + formatter.format(startLatLong[0] - 0.0010)
								+
								"' AND '" + formatter.format(startLatLong[0] + 0.0010) +
								"' AND startLongitude BETWEEN '" + formatter
								.format(startLatLong[1] - 0.0010) +
								"' AND '" + formatter.format(startLatLong[1] + 0.0010)
								+ "AND endLatitude BETWEEN '" +
								formatter.format(endLatLong[0] - 0.0010) + "' AND '" +
								formatter.format(endLatLong[0] + 0.0010)
								+ "' AND endLongitude BETWEEN '"
								+ formatter.format(endLatLong[1] - 0.0010) + "' AND '" +
								formatter.format(endLatLong[1] + 0.0010) + "'";
					}
				}
			}
		}

		if (!minTripDurationSearch.getText().trim().isEmpty() || !maxTripDurationSearch.getText()
			.trim().isEmpty()) {
			//Trip duration search not empty; append to query
			if (!firstSearchClause) {
				if (!minTripDurationSearch.getText().trim().isEmpty()) {
					entireWhere +=
						"AND Duration >= '" + minTripDurationSearch.getText().trim() + "'";
				}
				if (!maxTripDurationSearch.getText().trim().isEmpty()) {
					entireWhere +=
						"AND Duration <= '" + maxTripDurationSearch.getText().trim() + "'";
				}
			} else {
				if (!minTripDurationSearch.getText().trim().isEmpty()) {
					entireWhere += "(Duration >= '" + minTripDurationSearch.getText().trim() + "'";
					if (!maxTripDurationSearch.getText().trim().isEmpty()) {
						entireWhere +=
							" AND Duration <= '" + maxTripDurationSearch.getText().trim() + "'";
					}
				} else if (!maxTripDurationSearch.getText().trim().isEmpty()) {
					entireWhere += "(Duration <= '" + maxTripDurationSearch.getText().trim() + "'";
				}
				firstSearchClause = false;
			}
		}

		if (!minTripDistanceSearch.getText().trim().isEmpty() || !maxTripDistanceSearch.getText()
			.trim().isEmpty()) {
			//Trip duration search not empty; append to query
			if (!firstSearchClause) {
				//min only
				if (!minTripDistanceSearch.getText().trim().isEmpty() && maxTripDistanceSearch
					.getText().trim().isEmpty()) {
					entireWhere +=
						"AND Distance >= '" + minTripDistanceSearch.getText().trim() + "'";
					//max only
				} else if (minTripDistanceSearch.getText().trim().isEmpty()
					&& !maxTripDistanceSearch.getText().trim().isEmpty()) {
					entireWhere +=
						"AND Distance <= '" + maxTripDistanceSearch.getText().trim() + "'";
					//min + max range
				} else {
					entireWhere +=
						"AND Distance <= '" + maxTripDistanceSearch.getText().trim() + "'" +
							"AND Distance >= '" + minTripDistanceSearch.getText().trim() + "'";
				}
			} else {
				//min only
				if (!minTripDistanceSearch.getText().trim().isEmpty() && maxTripDistanceSearch
					.getText().trim().isEmpty()) {
					entireWhere += "(Distance >= '" + minTripDistanceSearch.getText().trim() + "'";
					//max only
				} else if (minTripDistanceSearch.getText().trim().isEmpty()
					&& !maxTripDistanceSearch.getText().trim().isEmpty()) {
					entireWhere += "(Distance <= '" + maxTripDistanceSearch.getText().trim() + "'";
					//min and max range
				} else {
					entireWhere += "(Distance >= '" + minTripDistanceSearch.getText().trim() + "'"
						+ "AND Distance " +
						"<= '" + maxTripDistanceSearch.getText().trim() + "'";
				}
				firstSearchClause = false;
			}
		}

		if (!tripGenderSearch.getText().trim().isEmpty()) {
			//Trip duration search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND Gender LIKE '%" + tripGenderSearch.getText().trim() + "%'";
			} else {
				entireWhere += "(Gender LIKE '%" + tripGenderSearch.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!entireWhere.isEmpty()) {
			entireWhere += ")";
			bikeTripList.setAll(FXCollections.observableArrayList(Main.getCurrentDb()
				.queryBikeTripData(entireWhere)));
			clearFilterBikeTripsButton.setVisible(true);
		} else {
			clearFilterBikeTrips();
		}
		tripSearchProgressIndicator.setVisible(false);
	}

	/**
	 * Passes currently selected trip to MapsController to get the visual route and switches view.
	 */
	public void displayRoute() {
		if (bikeTripTableView.getSelectionModel().getSelectedItem() == null) {
			return;
		}
		BikeTrip selectedTrip = bikeTripTableView.getSelectionModel().getSelectedItem();
		updateEntry();
		mapController.displayRoute(selectedTrip);
		planRoute();
	}

	/**
	 * Passes currently selected location to MapsController to get the visual representation and switches view.
	 */
	public void displayLocation() {

		if (Main.getTabState() == DataType.RETAILER) {
			TableView<Retailer> currentTable = retailerTableView;
			if (currentTable.getSelectionModel().getSelectedItem() == null) {
				return;
			}
			Location selectedLocation = currentTable.getSelectionModel().getSelectedItem();
			mapController.displayLocation(selectedLocation);
			planRoute();

		} else if (Main.getTabState() == DataType.HOTSPOT) {
			TableView<HotSpot> currentTable = hotSpotTableView;
			if (currentTable.getSelectionModel().getSelectedItem() == null) {
				return;
			}
			Location selectedLocation = currentTable.getSelectionModel().getSelectedItem();
			mapController.displayLocation(selectedLocation);
			planRoute();

		} else if (Main.getTabState() == DataType.LOCATION) {
			TableView<Location> currentTable = locationTableView;
			if (currentTable.getSelectionModel().getSelectedItem() == null) {
				return;
			}
			Location selectedLocation = currentTable.getSelectionModel().getSelectedItem();
			mapController.displayLocation(selectedLocation);
			planRoute();
		}


	}


	/**
	 * Searches the DB according to the inputted retailer search fields.
	 *
	 * All field searches are 'contains' and are not independent.
	 */
	public void searchRetailers() {
		retailerProgressIndicator.setVisible(true);

		String entireWhere = "";
		Boolean firstSearchClause = true;

		if (!retailerSearchNameField.getText().trim().isEmpty()) {
			//Retailer primaryType search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND name LIKE '%" + retailerSearchNameField.getText().trim() + "%'";
			} else {
				entireWhere += "(name LIKE '%" + retailerSearchNameField.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!retailerPrimarySearch.getText().trim().isEmpty()) {
			//Retailer primaryType search not empty; append to query
			if (!firstSearchClause) {
				entireWhere +=
					"AND primaryType LIKE '%" + retailerPrimarySearch.getText().trim() + "%'";
			} else {
				entireWhere +=
					"(primaryType LIKE '%" + retailerPrimarySearch.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!retailerSecondarySearch.getText().trim().isEmpty()) {
			//Retailer secondary type search not empty; append to query
			if (!firstSearchClause) {
				entireWhere +=
					"AND secondaryType LIKE '%" + retailerSecondarySearch.getText().trim() + "%'";
			} else {
				entireWhere +=
					"(secondaryType LIKE '%" + retailerSecondarySearch.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!retailerStreetSearch.getText().trim().isEmpty()) {
			//Retailer street search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND street LIKE '%" + retailerStreetSearch.getText().trim() + "%'";
			} else {
				entireWhere += "(street LIKE '%" + retailerStreetSearch.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!retailerStateSearch.getText().trim().isEmpty()) {
			//Retailer state search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND state LIKE '%" + retailerStateSearch.getText().trim() + "%'";
			} else {
				entireWhere += "(state LIKE '%" + retailerStateSearch.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!retailerCitySearch.getText().trim().isEmpty()) {
			//Retailer state search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND city LIKE '%" + retailerCitySearch.getText().trim() + "%'";
			} else {
				entireWhere += "(city LIKE '%" + retailerCitySearch.getText().trim() + "%'";
			}
		}

		if (!retailerZipSearch.getText().trim().isEmpty()) {
			//Retailer state search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND zipCode LIKE '%" + retailerZipSearch.getText().trim() + "%'";
			} else {
				entireWhere += "(zipCode LIKE '%" + retailerZipSearch.getText().trim() + "%'";
			}
		}

		if (!entireWhere.isEmpty()) {
			entireWhere += ");";
			retailList.setAll(FXCollections.observableArrayList(Main.getCurrentDb()
				.queryRetailerData(entireWhere)));
			clearFilterRetailersButton.setVisible(true);
		} else {
			clearFilterRetailers();
		}
		retailerProgressIndicator.setVisible(false);
	}

	/**
	 * Displays all retailers in current table to Google Maps then switches to Route view.
	 */
	public void displayAllRetailers() {
		if (retailList.isEmpty()) {
			AlertDialog.showWarning("Empty Search", "", "No entries to display!");
			return;
		}
		planRoute();
		mapController.updateRetailerMarkers();

	}

	/**
	 * Searches the DB according to the inputted hotspot search fields.
	 *
	 * All field searches are 'contains' and are not independent.
	 */
	public void searchHotspots() {
		String entireWhere = "";
		Boolean firstSearchClause = true;

		if (!hotSpotSearchNameField.getText().trim().isEmpty()) {
			//Retailer primaryType search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND name LIKE '%" + hotSpotSearchNameField.getText().trim() + "%'";
			} else {
				entireWhere += "(name LIKE '%" + hotSpotSearchNameField.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!hotspotBoroughSearch.getText().trim().isEmpty()) {
			//Retailer primaryType search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND borough LIKE '%" + hotspotBoroughSearch.getText().trim() + "%'";
			} else {
				entireWhere += "(borough LIKE '%" + hotspotBoroughSearch.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!hotspotLocationSearch.getText().trim().isEmpty()) {
			//Hotspot location search not empty; append to query
			if (!firstSearchClause) {
				entireWhere +=
					"AND locationInfo LIKE '%" + hotspotLocationSearch.getText().trim() + "%'";
			} else {
				entireWhere +=
					"(locationInfo LIKE '%" + hotspotLocationSearch.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!hotspotLocationTypeSearch.getText().trim().isEmpty()) {
			//Hotspot location type search not empty; append to query
			if (!firstSearchClause) {
				entireWhere +=
					"AND locationType LIKE '%" + hotspotLocationTypeSearch.getText().trim() + "%'";
			} else {
				entireWhere +=
					"(locationType LIKE '%" + hotspotLocationTypeSearch.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!hotspotPolicySearch.getText().trim().isEmpty()) {
			//Hotspot policy search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND policy LIKE '%" + hotspotPolicySearch.getText().trim() + "%'";
			} else {
				entireWhere += "(policy LIKE '%" + hotspotPolicySearch.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!hotspotPolicyDescSearch.getText().trim().isEmpty()) {
			//Hotspot policy desc search not empty; append to query
			if (!firstSearchClause) {
				entireWhere +=
					"AND policyDescription LIKE '%" + hotspotPolicyDescSearch.getText().trim()
						+ "%'";
			} else {
				entireWhere +=
					"(policyDescription LIKE '%" + hotspotPolicyDescSearch.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!hotspotProviderSearch.getText().trim().isEmpty()) {
			//Hotspot provider search not empty; append to query
			if (!firstSearchClause) {
				entireWhere +=
					"AND provider LIKE '%" + hotspotProviderSearch.getText().trim() + "%'";
			} else {
				entireWhere += "(provider LIKE '%" + hotspotProviderSearch.getText().trim() + "%'";
			}
		}

		if (!entireWhere.isEmpty()) {
			entireWhere += ");";
			hotSpotList.setAll(FXCollections.observableArrayList(Main.getCurrentDb()
				.queryHotSpotData(entireWhere)));
			clearFilterHotSpotsButton.setVisible(true);
		} else {
			clearFilterHotSpots();
		}
	}

	/**
	 * Displays all hotspots in current table to Google Maps then switches to Route view.
	 */
	public void displayAllHotspots() {
		if (hotSpotList.isEmpty()) {
			AlertDialog.showWarning("Empty Search", "", "No entries to display!");
		}
		planRoute();
		mapController.updateHotspotMarkers();
	}

	/*
	Handles the searching functionality of the locations data table
	 */
	public void searchLocations() {
		String entireWhere = "";
		Boolean firstSearchClause = true;

		if (!locationSearchNameField.getText().trim().isEmpty()) {
			//Retailer primaryType search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND name LIKE '%" + locationSearchNameField.getText().trim() + "%'";
			} else {
				entireWhere += "(name LIKE '%" + locationSearchNameField.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!locationAddressSearchField.getText().trim().isEmpty()) {
			//Retailer primaryType search not empty; append to query
			if (!firstSearchClause) {
				entireWhere +=
					"AND address LIKE '%" + locationAddressSearchField.getText().trim() + "%'";
			} else {
				entireWhere +=
					"(address LIKE '%" + locationAddressSearchField.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!locationCitySearchField.getText().trim().isEmpty()) {
			//Retailer secondary type search not empty; append to query
			if (!firstSearchClause) {
				entireWhere += "AND city LIKE '%" + locationCitySearchField.getText().trim() + "%'";
			} else {
				entireWhere += "(city LIKE '%" + locationCitySearchField.getText().trim() + "%'";
				firstSearchClause = false;
			}
		}

		if (!entireWhere.isEmpty()) {
			entireWhere += ");";
			ArrayList<Location> queriedLocations = Main.getCurrentDb()
				.queryLocationData(entireWhere);
			locationList = FXCollections.observableArrayList(queriedLocations);
			//mapController.setMapsLocations(queriedLocations);
			locationTableView.setItems(locationList);
			clearLocationFilterButton.setVisible(true);
		} else {
			clearFilterHotSpots();
		}
	}

	/**
	 * Displays all locations in current table and switches to Route view.
     */
	public void displayAllLocations() {
		if (locationList.isEmpty()) {
			AlertDialog.showWarning("Empty Search", "", "No entries to display!");
			return;
		}
		planRoute();
		mapController.updateLocationMarkers();
	}

	/**
	 * Clear all search fields and reset all current arrays in MainController, and MapController.
	 */
	public void clearAllFilters() {
		clearFilterBikeTrips();
		clearFilterRetailers();
		clearFilterHotSpots();
	}

	/**
	 * Clear all search text fields in the bike trip data table. Query all Trip data from the DB and
	 * push this to the current array, and MapController array.
	 */
	public void clearFilterBikeTrips() {
		bikeTripList.setAll(FXCollections.observableArrayList(Main.getCurrentDb()
			.queryBikeTripData(null)));
		tripBikeIdSearch.setText("");
		minTripDurationSearch.setText("");
		minTripDistanceSearch.setText("");
		tripGenderSearch.setText("");
		clearFilterBikeTripsButton.setVisible(false);
	}

	/**
	 * Clear all search text fields in the bike trip data table. Query all retailer data from the DB
	 * and push this to the current array, and MapController array.
	 */
	public void clearFilterRetailers() {
		retailList.setAll(FXCollections.observableArrayList(Main.getCurrentDb()
			.queryRetailerData(null)));
		retailerPrimarySearch.setText("");
		retailerSecondarySearch.setText("");
		retailerStreetSearch.setText("");
		retailerStateSearch.setText("");
		retailerZipSearch.setText("");
		clearFilterRetailersButton.setVisible(false);
	}

	/**
	 * Clear all search text fields in the bike trip data table. Query all hotspot data from the DB
	 * and push this to the current array, and MapController array.
	 */
	public void clearFilterHotSpots() {
		hotSpotList = FXCollections.observableArrayList(Main.getCurrentDb()
			.queryHotSpotData(null));
		hotspotBoroughSearch.setText("");
		hotspotLocationSearch.setText("");
		hotspotLocationTypeSearch.setText("");
		hotspotPolicySearch.setText("");
		hotspotPolicyDescSearch.setText("");
		hotspotProviderSearch.setText("");
		clearFilterHotSpotsButton.setVisible(false);
	}

	/**
	 * Clear all search text fields in the location data table. Query all location data from the DB
	 * and push this to the current array, and MapController array.
	 */
	public void clearFilterLocations() {
		ArrayList<Location> queriedLocations = Main.getCurrentDb().queryLocationData(null);
		locationList = FXCollections.observableArrayList(queriedLocations);
		//mapController.setMapsLocations(queriedLocations);
		locationTableView.setItems(locationList);

		locationCitySearchField.setText("");
		locationSearchNameField.setText("");
		locationAddressSearchField.setText("");
		clearLocationFilterButton.setVisible(false);
	}


	/**
	 * After users have selected a CSV file to import this functions handles the importing of the
	 * data into the database
	 */
	public void loadData() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirm Data Type");
		alert.setHeaderText(
			"Please Select the CSV Data Format to Import\n\nDestination Database: " + Main
				.getCurrentDb().toString());
		alert.setContentText("Choose your option:");

		ButtonType tripButton = new ButtonType("Trips");
		ButtonType retailerButton = new ButtonType("Retailers");
		ButtonType wifiButton = new ButtonType("Wi-Fi");
		ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(tripButton, retailerButton, wifiButton, cancelButton);

		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == tripButton) {
			String fileToProcess = getSelectedFilePath(DataType.BIKETRIP);
			if (fileToProcess == null) {
				return;
			}
			try {
				if (!Main.getCurrentDb().importCSV(fileToProcess, false, DataType.BIKETRIP)) {
					AlertDialog.showWarning("Error", "Error Importing Data",
						"Ooops, there was an error importing bike trip data\n" +
							"from " + fileToProcess);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			bikeTripList.setAll(
				FXCollections.observableArrayList(Main.getCurrentDb().queryBikeTripData(null)));

		} else if (result.get() == retailerButton) {
			String fileToProcess = getSelectedFilePath(DataType.RETAILER);
			if (fileToProcess == null) {
				return;
			}
			try {
				if (!Main.getCurrentDb().importCSV(fileToProcess, false, DataType.RETAILER)) {
					AlertDialog.showWarning("Error", "Error Importing Data",
						"Ooops, there was an error importing bike trip data\n" +
							"from " + fileToProcess);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			retailList.setAll(
				FXCollections.observableArrayList(Main.getCurrentDb().queryRetailerData(null)));

		} else if (result.get() == wifiButton) {
			String fileToProcess = getSelectedFilePath(DataType.HOTSPOT);
			if (fileToProcess == null) {
				return;
			}
			try {
				if (!Main.getCurrentDb().importCSV(fileToProcess, false, DataType.HOTSPOT)) {
					AlertDialog.showWarning("Error", "Error Importing Data",
						"Ooops, there was an error importing bike trip data\n" +
							"from " + fileToProcess);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			hotSpotList.setAll(
				FXCollections.observableArrayList(Main.getCurrentDb().queryHotSpotData(null)));

		} else {
			alert.close();
		}


	}


	/**
	 * Opens the window that handles the adding of a item into the database (Retailer, HotSpot,
	 * BikeTrip or Personal Location
	 *
	 * @throws Exception if fxml cannot load the fxml file
	 */
	public void addDatabaseEntry() throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/View/AddWindow.fxml"));
		Stage addEntryStage = new Stage();
		addEntryStage.setTitle("Add new entry");
		addEntryStage.setScene(new Scene(root, 600, 500));
		addEntryStage.show();

	}

	/**
	 * Opens a TextInputDialog to create a new database of inputted name.
	 */
	public void createDatabaseName() {
		String dbName;
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Create Database");
		dialog.setHeaderText("Database Name");
		dialog.setContentText("Please enter the name of the database:");

		Optional<String> result = dialog.showAndWait();

		if (result.isPresent()) {
			dbName = result.get();
		} else {
			return;
		}

		if (dbName.isEmpty()) {
			AlertDialog.showWarning("Create Database", "Invalid Database Name",
				"Please enter a valid database name.");
			createDatabaseName();
			return;
		}
		for (SQLHandler currentHandler : Main.getDatabases()) {
			if (((dbName + ".rmdb").toLowerCase()
				.compareTo(currentHandler.toString().toLowerCase())) == 0) {
				AlertDialog.showWarning("Create Database", "Database Name Exists",
					"Please enter a unique database name.");
				createDatabaseName();
				return;
			}
		}
		Main.addDatabase(dbName + ".rmdb");
		databaseList.add(dbName);
		AlertDialog
			.showInformation("Create Database", "", "Successfully created " + (dbName + ".rmdb"));
	}

	/**
	 * Updates the MenuItem currentDBMenu to display the currently selected database.
	 */
	private void updateDBMenuTag() {
		currentDBMenu.setText(String.format("Current DB:\n      %s",
			Main.getDatabases().get(Main.getCurrentDbIndex()).toString()));
		currentDBMenu.setStyle("-fx-text-fill: grey;\n" +
			"-fx-border-color: white;\n" +
			"-fx-background-color: white;");
	}


	/**
	 * User selects a database to show with a prompt giving them a list to choose from
	 */
	public void selectDatabase() {
		ChoiceDialog<String> databaseDialog = new ChoiceDialog("", Main.getDatabases());
		//databaseDialog.setSelectedItem(Main.getDatabases().get(0).toString());
		databaseDialog.setTitle("Select Database");
		databaseDialog.setHeaderText("Select Current Database");
		databaseDialog.setContentText("Select an existing database to read and write from:");
		Optional<String> result = databaseDialog.showAndWait();
		if (result.isPresent()) {
			if (result.get() == "") {
				AlertDialog.showInformation("Choose Database", "", "Please select a database.");
				selectDatabase();
				return;
			}
			int index = Main.databases.indexOf(databaseDialog.getSelectedItem());
			Main.setCurrentDbIndex(index);
			Main.getCurrentDb();
			updateDBMenuTag();
			bikeTripList.setAll(
				FXCollections.observableArrayList(Main.getCurrentDb().queryBikeTripData(null)));
			retailList.setAll(
				FXCollections.observableArrayList(Main.getCurrentDb().queryRetailerData(null)));
			hotSpotList.setAll(
				FXCollections.observableArrayList(Main.getCurrentDb().queryHotSpotData(null)));
			locationList.setAll(
				FXCollections.observableArrayList(Main.getCurrentDb().queryLocationData(null)));
		}
	}

	/**
	 * User selects a database to show with a prompt giving them a list to choose from
	 */
	public void selectStartDatabase() {
		ChoiceDialog<String> databaseDialog = new ChoiceDialog("", Main.getDatabases());
		//databaseDialog.setSelectedItem(Main.getDatabases().get(0).toString());
		databaseDialog.setTitle("Welcome to RouteMe!");
		databaseDialog.setHeaderText("In order to start, please select Current Database");
		databaseDialog.setContentText("Select an existing database to read and write from:");
		Optional<String> result = databaseDialog.showAndWait();
		if (result.isPresent()) {
			if (result.get() == "") {
				AlertDialog.showInformation("Choose Database", "", "Please select a database.");
				selectDatabase();
				return;
			}
			int index = Main.databases.indexOf(databaseDialog.getSelectedItem());
			Main.setCurrentDbIndex(index);
			Main.getCurrentDb();
			updateDBMenuTag();
			bikeTripList.setAll(
					FXCollections.observableArrayList(Main.getCurrentDb().queryBikeTripData(null)));
			retailList.setAll(
					FXCollections.observableArrayList(Main.getCurrentDb().queryRetailerData(null)));
			hotSpotList.setAll(
					FXCollections.observableArrayList(Main.getCurrentDb().queryHotSpotData(null)));
			locationList.setAll(
					FXCollections.observableArrayList(Main.getCurrentDb().queryLocationData(null)));
		}
	}

	/**
	 * Opens a ChoiceDialog to select a database to delete, offers confirmation also.
	 */
	public void deleteDatabase() {
		ChoiceDialog<String> databaseDialog = new ChoiceDialog("", Main.getDatabases());
		//databaseDialog.setSelectedItem(Main.getDatabases().get(0).toString());
		databaseDialog.setTitle("Select Database to Delete");
		databaseDialog.setHeaderText("Select Database to Delete");
		databaseDialog.setContentText("Select an existing database to delete:");
		Optional<String> result = databaseDialog.showAndWait();
		if (result.isPresent()) {
			if (result.get() == "") {
				AlertDialog
					.showInformation("Delete Database", "", "Please select a database to delete.");
				deleteDatabase();
				return;
			}
			int index = Main.databases.indexOf(databaseDialog.getSelectedItem());
			SQLHandler handlerToDelete = Main.getDatabases().get(index);
			if (Main.getCurrentDb().toString().equals(handlerToDelete.toString())) {
				AlertDialog.showWarning("Failed to Delete Database", "",
					"You cannot delete your current database. " +
						"Please switch to another database to continue.");
				return;
			}
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Confirm Database Deletion");
			alert.setHeaderText("Deleting Database " + handlerToDelete.toString());
			alert.setContentText("Are you sure? This action cannot be undone.");
			Optional<ButtonType> confirmationResult = alert.showAndWait();
			if (confirmationResult.get() == ButtonType.OK) {
				// OK DELETE
				boolean deletionSuccessful = handlerToDelete.deleteDatabase();
				if (!deletionSuccessful) {
					AlertDialog
						.showWarning("Failed to Delete Database", "", "Failed to delete database.");
					return;
				}
				Main.getDatabases().remove(handlerToDelete);
				return;
			} else {
				// NO BACK UP THE TRUCK DONT DELEte
				return;
			}
		}
	}


	/**
	 * Changes the tabstate when user navigates to the retailer data table
	 */
	public void retailState() {
		Main.setTabState(DataType.RETAILER);
	}

	/**
	 * Changes the tabstate when user navigates to the bike trip data table
	 */
	public void bikeTripState() {
		Main.setTabState(DataType.BIKETRIP);
	}

	/**
	 * Changes the tabstate when user navigates to the hot spot data table
	 */
	public void hotSpotState() {
		Main.setTabState(DataType.HOTSPOT);
	}

	/**
	 * Changes the tabstate when user navigates to the location data table
	 */
	public void locationState() {
		Main.setTabState(DataType.LOCATION);
	}

	/**
	 * Handles the deleting of an entry in the data tables
	 */
	public void deleteEntryAlert() {
		Alert deleteConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
		deleteConfirmation.setHeaderText("Are you sure you want to delete this entry?");
		deleteConfirmation.setTitle("Delete entry");

		ButtonType yesButton = new ButtonType("Yes");
		ButtonType cancelButton = new ButtonType("Cancel");

		deleteConfirmation.getButtonTypes().setAll(yesButton, cancelButton);
		Optional<ButtonType> result = deleteConfirmation.showAndWait();
		if (result.get() == yesButton) {
			switch (Main.getTabState()) {
				case BIKETRIP:
					BikeTrip bikeTrip = bikeTripTableView.getSelectionModel().getSelectedItem();
					if (Main.getCurrentDb().removeEntry(DataType.BIKETRIP, bikeTrip.getId())) {
						bikeTripList.remove(bikeTrip);
					} else {
						System.err.println("Unable to delete bike trip.");
					}
					break;
				case RETAILER:
					Retailer retailer = retailerTableView.getSelectionModel().getSelectedItem();
					if (Main.getCurrentDb().removeEntry(DataType.RETAILER, retailer.getId())) {
						retailList.remove(retailer);
					} else {
						System.err.println("Unable to delete retailer.");
					}
					break;
				case HOTSPOT:
					HotSpot hotSpot = hotSpotTableView.getSelectionModel().getSelectedItem();
					if (Main.getCurrentDb().removeEntry(DataType.HOTSPOT, hotSpot.getId())) {
						hotSpotList.remove(hotSpot);
					} else {
						System.err.println("Unable to delete hot spot.");
					}
					break;
				case LOCATION:
					Location location = locationTableView.getSelectionModel().getSelectedItem();
					if (Main.getCurrentDb().removeEntry(DataType.LOCATION, location.getId())) {
						locationList.remove(location);
					} else {
						System.err.println("Unable to delete location.");
					}
				default:
					System.out.println("Not able to delete data of type " + Main.getTabState());
			}
		}

	}

	/////////////////////**************************************************////////////////////////////////
	//section handles editing entries in data viewer's tables
	/////////////////////**************************************************////////////////////////////////
	//Bike trips

	/**
	 * Handles the editing of a Bike Trip Entry, when the 'Edit' Button is pressed.
	 */
	public void editBikeTripEntry() {
		bikeTripTableView.setEditable(true);
		endBikeTripEditButton.setVisible(true);
		editBikeTripButton.setDisable(true);
		System.out.println(dataGender.getStyleClass());
		dataGender.getStyleClass().clear();
		dataGender.getStyleClass().add("style1");
		startTime.getStyleClass().clear();
		startTime.getStyleClass().add("style1");
		endTime.getStyleClass().clear();
		endTime.getStyleClass().add("style1");
	}

	/**
	 * Handles the editing of a Bike Trip Entry, when the 'Done Editing' Button is pressed.
	 */
	public void endBikeTripEdit() {
		bikeTripTableView.setEditable(false);
		endBikeTripEditButton.setVisible(false);
		editBikeTripButton.setDisable(false);
		dataGender.getStyleClass().remove("style1");
		dataGender.getStyleClass().add("style2");
		startTime.getStyleClass().remove("style1");
		startTime.getStyleClass().add("style2");
		endTime.getStyleClass().remove("style1");
		endTime.getStyleClass().add("style2");
	}

	/**
	 * Handles the backend editing of an entry in the given ArrayList.
	 */
	public void updateEntry() {
		BikeTrip selected = bikeTripTableView.getSelectionModel().getSelectedItem();
		if (selected == null) {
			return;
		}
		String stats = selected.calcStats();
		Main.getCurrentDb().updateEntry(DataType.BIKETRIP, stats, selected.getId());
		bikeTripList.setAll(FXCollections.observableArrayList(Main.getCurrentDb()
			.queryBikeTripData(null)));
	}

	public void displayFavorites() {
		switch (Main.getTabState()) {
			case BIKETRIP:
				if (favoriteTrips.isSelected()) {
					bikeTripList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryBikeTripData("favourite = " + 1)));
				} else {
					bikeTripList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryBikeTripData("favourite = " + 0)));
				}
				break;
			case RETAILER:
				if (favoriteRetailer.isSelected()) {
					retailList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryRetailerData("favourite = " + 1)));
				} else {
				retailList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryRetailerData("favourite = " + 0)));
				}
				break;
			case HOTSPOT:
				if (favoriteHotSpot.isSelected()) {
					hotSpotList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryHotSpotData("favourite = " + 1)));
				} else {
					hotSpotList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryHotSpotData("favourite = " + 0)));
				}
				break;
			case LOCATION:
				if (favoriteLocation.isSelected()) {
					locationList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryLocationData("favourite = " + 1)));
				} else {
					locationList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryLocationData("favourite = " + 0)));
				}
				break;
		}

	}


	//Retailers

	/**
	 * Handles the editing of a Retailer Entry, when the 'Edit' Button is pressed.
	 */
	public void editRetailerEntry() {
		retailerTableView.setEditable(true);
		endRetailerEditButton.setVisible(true);
		editRetailerButton.setDisable(true);
		retailerName.getStyleClass().clear();
		retailerName.getStyleClass().add("style1");
		retailPrType.getStyleClass().clear();
		retailPrType.getStyleClass().add("style1");
		retailScType.getStyleClass().clear();
		retailScType.getStyleClass().add("style1");
		retailBlock.getStyleClass().clear();
		retailBlock.getStyleClass().add("style1");
		retailLot.getStyleClass().clear();
		retailLot.getStyleClass().add("style1");
	}

	/**
	 * Handles the editing of a Retailer Entry, when the 'Done Editing' Button is pressed.
	 */
	public void endRetailerEdit() {
		retailerTableView.setEditable(false);
		endRetailerEditButton.setVisible(false);
		editRetailerButton.setDisable(false);
		retailerName.getStyleClass().clear();
		retailerName.getStyleClass().add("style2");
		retailPrType.getStyleClass().clear();
		retailPrType.getStyleClass().add("style2");
		retailScType.getStyleClass().clear();
		retailScType.getStyleClass().add("style2");
		retailBlock.getStyleClass().clear();
		retailBlock.getStyleClass().add("style2");
		retailLot.getStyleClass().clear();
		retailLot.getStyleClass().add("style2");
	}

	//Hot spot locations

	/**
	 * Handles the editing of a HotSpot Entry, when the 'Edit' Button is pressed.
	 */
	public void editHotSpotEntry() {
		hotSpotTableView.setEditable(true);
		endHotSpotEditButton.setVisible(true);
		editHotSpotButton.setDisable(true);
		hotSpotName.getStyleClass().clear();
		hotSpotName.getStyleClass().add("style1");
		hotSpotBorough.getStyleClass().clear();
		hotSpotBorough.getStyleClass().add("style1");
		hotSpotLocationType.getStyleClass().clear();
		hotSpotLocationType.getStyleClass().add("style1");
		hotSpotPolicy.getStyleClass().clear();
		hotSpotPolicy.getStyleClass().add("style1");
		hotSpotPolicyDisc.getStyleClass().clear();
		hotSpotPolicyDisc.getStyleClass().add("style1");
		hotSpotProvider.getStyleClass().clear();
		hotSpotProvider.getStyleClass().add("style1");
	}

	/**
	 * Handles the editing of a Hotspot Entry, when the 'Done Editing' Button is pressed.
	 */
	public void endHotSpotEdit() {
		hotSpotTableView.setEditable(false);
		endHotSpotEditButton.setVisible(false);
		editHotSpotButton.setDisable(false);
		hotSpotName.getStyleClass().clear();
		hotSpotName.getStyleClass().add("style2");
		hotSpotBorough.getStyleClass().clear();
		hotSpotBorough.getStyleClass().add("style2");
		hotSpotLocationType.getStyleClass().clear();
		hotSpotLocationType.getStyleClass().add("style2");
		hotSpotPolicy.getStyleClass().clear();
		hotSpotPolicy.getStyleClass().add("style2");
		hotSpotPolicyDisc.getStyleClass().clear();
		hotSpotPolicyDisc.getStyleClass().add("style2");
		hotSpotProvider.getStyleClass().clear();
		hotSpotProvider.getStyleClass().add("style2");
	}

	/**
	 * Loads the Map.fxml into an AnchorPane and fetches its controller.
	 */
	public void loadMapPane() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Map.fxml"));
			loader.setBuilderFactory(new JavaFXBuilderFactory());
			AnchorPane mapPane = loader.load();

			gMapsAnchorPane.getChildren().clear();
			gMapsAnchorPane.getChildren().add(mapPane);
			mapPane.setVisible(true);
			mapController = loader.getController();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Opens up the help window
	 *
	 * @throws Exception for null pointer
	 */
	public void openHelpWindow() throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/View/HelpWindow.fxml"));
		Stage helpStage = new Stage();
		helpStage.setTitle("Help");
		helpStage.setScene(new Scene(root, 800, 600));
		helpStage.show();
	}

	/**
	 * Initializes stuff that is required when the GUI main window opens
	 *
	 * @param location FXML File for Main Controller
	 * @param resources Target folder from where the file is held.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//Load the MapView pane, insert it into our Main, then fetch its controller
		loadMapPane();

		//Initializes the GUI to display home screen at start
		welcomePane.setDisable(false);
		welcomePane.setVisible(true);

//		Image welcomeImage = new Image("/HomeScreen.jpg");
//		BackgroundImage imageBackground = new BackgroundImage(welcomeImage,
//			BackgroundRepeat.NO_REPEAT,
//			BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
//			BackgroundSize.DEFAULT);
//		welcomePane.setBackground(new Background(imageBackground));

		buttonBox.setOpacity(0.7);
		buttonBox.getStylesheets().add(getClass().getResource("/CSS/MenuStyle.css").toExternalForm());
		buttonBox.setTranslateX(-160);
		TranslateTransition menuTranslation = new TranslateTransition(Duration.millis(500),
			buttonBox);

		Image planRouteImage = new Image("/Images/rsz_1icon_1024.jpg");
		BackgroundImage planRouteBackground = new BackgroundImage(planRouteImage,
				BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER,
				BackgroundSize.DEFAULT);
		planRouteBtn.setBackground(new Background(planRouteBackground));

		menuTranslation.setFromX(0);
		menuTranslation.setToX(-160);

		buttonBox.setOnMouseEntered(evt -> {

			menuTranslation.setRate(-1);
			menuTranslation.play();
		});
		buttonBox.setOnMouseExited(evt -> {
			menuTranslation.setRate(1);
			menuTranslation.play();

		});

		rawDataViewerPane.setDisable(true);
		rawDataViewerPane.setVisible(false);

		planRoutePane.setDisable(true);
		planRoutePane.setVisible(false);

		endBikeTripEditButton.setVisible(false);
		endRetailerEditButton.setVisible(false);
		endHotSpotEditButton.setVisible(false);
		endRetailerEditButton.setVisible(false);
		endBikeTripEditButton.setVisible(false);

		updateDBMenuTag();

		/////////////////////**************************************************////////////////////////////////
		//this part tells the tables and table columns what data to track and sets them to editable text fields
		/////////////////////**************************************************////////////////////////////////
		//Bike trips table
		dataBikeID.setCellValueFactory(new PropertyValueFactory<>("bikeId"));
		dataDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
		startLatitude.setCellValueFactory(new PropertyValueFactory<>("startLatitude"));
		startLongitude.setCellValueFactory(new PropertyValueFactory<>("startLongitude"));
		dataGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
		dataGender.setCellFactory(TextFieldTableCell.forTableColumn());
		endLatitude.setCellValueFactory(new PropertyValueFactory<>("endLatitude"));
		endLongitude.setCellValueFactory(new PropertyValueFactory<>("endLongitude"));
		caloriesBurnt.setCellValueFactory(new PropertyValueFactory<>("caloriesBurnt"));
		avgSpeed.setCellValueFactory(new PropertyValueFactory<>("avgSpeed"));
		rating.setCellValueFactory(new PropertyValueFactory<>("avgRating"));
		startTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
		startTime
			.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateTimeStringConverter()));
		endTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
		endTime
			.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateTimeStringConverter()));
		dataDistance.setCellValueFactory(new PropertyValueFactory<>("distance"));
		bikeTripFavourite.setCellValueFactory(new PropertyValueFactory<>("favourite"));
		bikeTripFavourite.setCellFactory(column -> {
			return new TableCell<BikeTrip, Boolean>() {
				@Override
				protected void updateItem(Boolean item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						// Format date.
						if (item == true) {
							setText("★");
							setTextFill(Paint.valueOf("GOLD"));
							setStyle("");
						} else {
							setText("");
							setStyle("");

						}
					}
				}
			};
		});

		//Retailer table
		retailerName.setCellValueFactory(new PropertyValueFactory<>("name"));
		retailerName.setCellFactory(TextFieldTableCell.forTableColumn());
		retailPrType.setCellValueFactory(new PropertyValueFactory<>("primaryType"));
		retailPrType.setCellFactory(TextFieldTableCell.forTableColumn());
		retailScType.setCellValueFactory(new PropertyValueFactory<>("secondaryType"));
		retailScType.setCellFactory(TextFieldTableCell.forTableColumn());
		retailStr.setCellValueFactory(new PropertyValueFactory<>("street"));
		retailCity.setCellValueFactory(new PropertyValueFactory<>("city"));
		retailState.setCellValueFactory(new PropertyValueFactory<>("state"));
		retailZip.setCellValueFactory(new PropertyValueFactory<>("zipCode"));
		retailLatitude.setCellValueFactory(new PropertyValueFactory<>("latitude"));
		retailLongitude.setCellValueFactory(new PropertyValueFactory<>("longitude"));
		retailBlock.setCellValueFactory(new PropertyValueFactory<>("block"));
		retailBlock.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		retailLot.setCellValueFactory(new PropertyValueFactory<>("lot"));
		retailLot.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		retailerFavourite.setCellValueFactory(new PropertyValueFactory<>("favourite"));
		retailerFavourite.setCellFactory(column -> {
			return new TableCell<Retailer, Boolean>() {
				@Override
				protected void updateItem(Boolean item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						// Format date.
						if (item) {
							setText("★");
							setTextFill(Paint.valueOf("GOLD"));
							setStyle("");
						} else {
							setText("");
							setStyle("");
						}
					}
				}
			};
		});

		//Wi-Fi hots spots table
		hotSpotName.setCellValueFactory(new PropertyValueFactory<>("name"));
		hotSpotName.setCellFactory(TextFieldTableCell.forTableColumn());
		hotSpotBorough.setCellValueFactory(new PropertyValueFactory<>("borough"));
		hotSpotBorough.setCellFactory(TextFieldTableCell.forTableColumn());
		hotSpotLocationInfo.setCellValueFactory(new PropertyValueFactory<>("locationInfo"));
		hotSpotLocationType.setCellValueFactory(new PropertyValueFactory<>("locationType"));
		hotSpotLocationType.setCellFactory(TextFieldTableCell.forTableColumn());
		hotSpotPolicy.setCellValueFactory(new PropertyValueFactory<>("policy"));
		hotSpotPolicy.setCellFactory(TextFieldTableCell.forTableColumn());
		hotSpotPolicyDisc.setCellValueFactory(new PropertyValueFactory<>("policyDescription"));
		hotSpotPolicyDisc.setCellFactory(TextFieldTableCell.forTableColumn());
		hotSpotProvider.setCellValueFactory(new PropertyValueFactory<>("provider"));
		hotSpotProvider.setCellFactory(TextFieldTableCell.forTableColumn());
		hotSpotLatitude.setCellValueFactory(new PropertyValueFactory<>("latitude"));
		hotSpotLongitude.setCellValueFactory(new PropertyValueFactory<>("longitude"));
		hotSpotFavourite.setCellValueFactory(new PropertyValueFactory<>("favourite"));
		hotSpotFavourite.setCellFactory(column -> {
			return new TableCell<HotSpot, Boolean>() {
				@Override
				protected void updateItem(Boolean item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						// Format date.
						if (item == true) {
							setText("★");
							setTextFill(Paint.valueOf("GOLD"));
							setStyle("");
						} else {
							setText("");
							setStyle("");
						}
					}
				}
			};
		});

		//Locations table (will always start empty)
		locationName.setCellValueFactory(new PropertyValueFactory<>("name"));
		locationName.setCellFactory(TextFieldTableCell.forTableColumn());
		locationAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
		locationCity.setCellValueFactory(new PropertyValueFactory<>("city"));
		locationLatitude.setCellValueFactory(new PropertyValueFactory<>("latitude"));
		locationLongitude.setCellValueFactory(new PropertyValueFactory<>("longitude"));
		locationFavourite.setCellValueFactory(new PropertyValueFactory<>("favourite"));
		locationFavourite.setCellFactory(column -> {
			return new TableCell<Location, Boolean>() {
				@Override
				protected void updateItem(Boolean item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						// Format date.
						if (item == true) {
							setText("★");
							setTextFill(Paint.valueOf("GOLD"));
							setStyle("");
						} else {
							setText("");
							setStyle("");
						}
					}
				}
			};
		});

		//Initialize the list that are used for populating the raw data tables
		bikeTripList = FXCollections.observableArrayList();
		retailList = FXCollections.observableArrayList();
		hotSpotList = FXCollections.observableArrayList();
		locationList = FXCollections.observableArrayList();

		//Add listeners that when the core list is modified, reflect these changes in the Table and Google Maps visualisations
		bikeTripList.addListener((ListChangeListener<BikeTrip>) c -> {
			//System.out.println("bikeTripList changed to " + c);
			bikeTripTableView.setItems(bikeTripList);
			mapController.setMapsBikeTrips(
				new ArrayList<>(bikeTripList.stream().collect(Collectors.toList())));
		});

		retailList.addListener((ListChangeListener<Retailer>) c -> {
			//System.out.println("retailList changed to " + c);
			retailerTableView.setItems(retailList);
			mapController.setMapsRetailers(
				new ArrayList<>(retailList.stream().collect(Collectors.toList())));
		});

		hotSpotList.addListener((ListChangeListener<HotSpot>) c -> {
			//System.out.println("hotspotList changed to " + c);
			hotSpotTableView.setItems(hotSpotList);
			mapController.setMapsHotspots(
				new ArrayList<>(hotSpotList.stream().collect(Collectors.toList())));
		});



		locationList.addListener((ListChangeListener<Location>) c -> {
			//System.out.println("locationList changed to " + c);
			locationTableView.setItems(locationList);
			mapController.setMapsLocations(new ArrayList<Location>(new ArrayList<>(locationList)));
		});

		//Query DB to get any previously imported data and fire listeners
		bikeTripList.setAll(FXCollections
			.observableArrayList(Main.getCurrentDb().queryBikeTripData(null)));
		retailList.setAll(FXCollections
			.observableArrayList(Main.getCurrentDb().queryRetailerData(null)));
		hotSpotList.setAll(FXCollections
			.observableArrayList(Main.getCurrentDb().queryHotSpotData(null)));
		locationList.setAll(FXCollections
			.observableArrayList(Main.getCurrentDb().queryLocationData(null)));

		/////////////////////**************************************************////////////////////////////////
		//Set listeners for every numeric only text field to limit entry
		/////////////////////**************************************************////////////////////////////////
		Alert wrongValue = new Alert(Alert.AlertType.ERROR);
		wrongValue.setHeaderText("Wrong value entered");
		tripBikeIdSearch.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				tripBikeIdSearch.setText(newValue.replaceAll("[^\\d]", ""));
				wrongValue.setContentText("You can only enter integer values in this field");
				wrongValue.showAndWait();
			}
		});

		minTripDurationSearch.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*(\\.\\d*)?")) {
				minTripDurationSearch.setText(newValue.replaceAll("[^\\d(\\.\\d)?]", ""));
				wrongValue.setContentText("You can only enter decimal values in this field");
				wrongValue.showAndWait();
			}
		});

		maxTripDurationSearch.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*(\\.\\d*)?")) {
				maxTripDurationSearch.setText(newValue.replaceAll("[^\\d(\\.\\d)?]", ""));
				wrongValue.setContentText("You can only enter decimal values in this field");
				wrongValue.showAndWait();
			}
		});

		minTripDistanceSearch.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*(\\.\\d*)?")) {
				minTripDistanceSearch.setText(newValue.replaceAll("[^\\d(\\.\\d)?]", ""));
				wrongValue.setContentText("You can only enter decimal values in this field");
				wrongValue.showAndWait();
			}
		});

		maxTripDistanceSearch.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*(\\.\\d*)?")) {
				maxTripDistanceSearch.setText(newValue.replaceAll("[^\\d(\\.\\d)?]", ""));
				wrongValue.setContentText("You can only enter decimal values in this field");
				wrongValue.showAndWait();
			}
		});

		ratingSearch.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("([0-4]?(\\.\\d*)?)|5")) {
				ratingSearch
					.setText(newValue.replaceAll("[^\\[[0-4]?(\\.\\d*)?)\\]|\\[^5\\]]", ""));
				wrongValue.setContentText("Ratings can only range between 0 and 5");
				wrongValue.showAndWait();
			}
		});

		retailerZipSearch.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				minTripDistanceSearch.setText(newValue.replaceAll("[^\\d]", ""));
			}
		});



		/////////////////////**************************************************////////////////////////////////
		//Creating the right click context menu
		/////////////////////**************************************************////////////////////////////////
		ContextMenu rightClickTripMenu = new ContextMenu();
		ContextMenu rightClickLocationMenu = new ContextMenu();

		MenuItem deleteTripMenu = new MenuItem("Delete");
		deleteTripMenu.setOnAction(event -> {
			deleteEntryAlert();
			bikeTripTableView.getSelectionModel().select(null);
			retailerTableView.getSelectionModel().select(null);
			hotSpotTableView.getSelectionModel().select(null);
			locationTableView.getSelectionModel().select(null);
		});

		MenuItem deleteLocationMenu = new MenuItem("Delete");
		deleteLocationMenu.setOnAction(event -> {
			deleteEntryAlert();
			bikeTripTableView.getSelectionModel().select(null);
			retailerTableView.getSelectionModel().select(null);
			hotSpotTableView.getSelectionModel().select(null);
			locationTableView.getSelectionModel().select(null);
		});

		MenuItem displayTripMenu = new MenuItem("Get Route");
		displayTripMenu.setOnAction(event -> {
			displayRoute();
		});

		MenuItem updateTripStats = new MenuItem("Get Stats");
		updateTripStats.setOnAction(event -> {
			updateEntry();
		});

		MenuItem displayLocationMenu = new MenuItem("Display Location");
		displayLocationMenu.setOnAction(event -> {
			displayLocation();
		});

		MenuItem favoriteEntry = new MenuItem("Add to Favourites");
		favoriteEntry.setOnAction(event -> {
			switch (Main.getTabState()) {
				case BIKETRIP:
					BikeTrip edit = bikeTripTableView.getSelectionModel().getSelectedItem();
					edit.setFavourite(true);
					Main.getCurrentDb().setFavourite(DataType.BIKETRIP, true, edit.getId());
					break;
				case RETAILER:
					Retailer edit1 = retailerTableView.getSelectionModel().getSelectedItem();
					edit1.setFavourite(true);
					Main.getCurrentDb().setFavourite(DataType.RETAILER, true, edit1.getId());
					break;
				case HOTSPOT:
					HotSpot edit2 = hotSpotTableView.getSelectionModel().getSelectedItem();
					edit2.setFavourite(true);
					Main.getCurrentDb().setFavourite(DataType.HOTSPOT, true, edit2.getId());
					break;
				case LOCATION:
					Location edit3 = locationTableView.getSelectionModel().getSelectedItem();
					edit3.setFavourite(true);
					Main.getCurrentDb().setFavourite(DataType.LOCATION, true, edit3.getId());
					break;
			}
			bikeTripList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryBikeTripData(null)));
			retailList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryRetailerData(null)));
			hotSpotList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryHotSpotData(null)));
			locationList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryLocationData(null)));

			bikeTripTableView.getSelectionModel().select(null);
			retailerTableView.getSelectionModel().select(null);
			hotSpotTableView.getSelectionModel().select(null);
			locationTableView.getSelectionModel().select(null);
		});

		MenuItem favoriteTrip = new MenuItem("Add to Favourites");
		favoriteTrip.setOnAction(event -> {
			switch (Main.getTabState()) {
				case BIKETRIP:
					BikeTrip edit = bikeTripTableView.getSelectionModel().getSelectedItem();
					edit.setFavourite(true);
					Main.getCurrentDb().setFavourite(DataType.BIKETRIP, true, edit.getId());
					break;
				case RETAILER:
					Retailer edit1 = retailerTableView.getSelectionModel().getSelectedItem();
					edit1.setFavourite(true);
					Main.getCurrentDb().setFavourite(DataType.RETAILER, true, edit1.getId());
					break;
				case HOTSPOT:
					HotSpot edit2 = hotSpotTableView.getSelectionModel().getSelectedItem();
					edit2.setFavourite(true);
					Main.getCurrentDb().setFavourite(DataType.HOTSPOT, true, edit2.getId());
					break;
				case LOCATION:
					Location edit3 = locationTableView.getSelectionModel().getSelectedItem();
					edit3.setFavourite(true);
					Main.getCurrentDb().setFavourite(DataType.LOCATION, true, edit3.getId());
					break;
			}
			bikeTripList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryBikeTripData(null)));
			retailList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryRetailerData(null)));
			hotSpotList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryHotSpotData(null)));
			locationList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryLocationData(null)));

			bikeTripTableView.getSelectionModel().select(null);
			retailerTableView.getSelectionModel().select(null);
			hotSpotTableView.getSelectionModel().select(null);
			locationTableView.getSelectionModel().select(null);
		});

		MenuItem unfavoriteEntry = new MenuItem("Remove From Favourites");
		unfavoriteEntry.setOnAction(event -> {
			switch (Main.getTabState()) {
				case BIKETRIP:
					BikeTrip edit = bikeTripTableView.getSelectionModel().getSelectedItem();
					edit.setFavourite(false);
					Main.getCurrentDb().setFavourite(DataType.BIKETRIP, false, edit.getId());
					break;
				case RETAILER:
					Retailer edit1 = retailerTableView.getSelectionModel().getSelectedItem();
					edit1.setFavourite(false);
					Main.getCurrentDb().setFavourite(DataType.RETAILER, false, edit1.getId());
					break;
				case HOTSPOT:
					HotSpot edit2 = hotSpotTableView.getSelectionModel().getSelectedItem();
					edit2.setFavourite(false);
					Main.getCurrentDb().setFavourite(DataType.HOTSPOT, false, edit2.getId());
					break;
				case LOCATION:
					Location edit3 = locationTableView.getSelectionModel().getSelectedItem();
					edit3.setFavourite(false);
					Main.getCurrentDb().setFavourite(DataType.LOCATION, false, edit3.getId());
					break;
			}
			bikeTripList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryBikeTripData(null)));
			retailList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryRetailerData(null)));
			hotSpotList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryHotSpotData(null)));
			locationList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryLocationData(null)));

			bikeTripTableView.getSelectionModel().select(null);
			retailerTableView.getSelectionModel().select(null);
			hotSpotTableView.getSelectionModel().select(null);
			locationTableView.getSelectionModel().select(null);
		});

		MenuItem unfavoriteTrip = new MenuItem("Remove From Favourites");
		unfavoriteTrip.setOnAction(event -> {
			switch (Main.getTabState()) {
				case BIKETRIP:
					BikeTrip edit = bikeTripTableView.getSelectionModel().getSelectedItem();
					edit.setFavourite(false);
					Main.getCurrentDb().setFavourite(DataType.BIKETRIP, false, edit.getId());
					break;
				case RETAILER:
					Retailer edit1 = retailerTableView.getSelectionModel().getSelectedItem();
					edit1.setFavourite(false);
					Main.getCurrentDb().setFavourite(DataType.RETAILER, false, edit1.getId());
					break;
				case HOTSPOT:
					HotSpot edit2 = hotSpotTableView.getSelectionModel().getSelectedItem();
					edit2.setFavourite(false);
					Main.getCurrentDb().setFavourite(DataType.HOTSPOT, false, edit2.getId());
					break;
				case LOCATION:
					Location edit3 = locationTableView.getSelectionModel().getSelectedItem();
					edit3.setFavourite(false);
					Main.getCurrentDb().setFavourite(DataType.LOCATION, false, edit3.getId());
					break;
			}
			bikeTripList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryBikeTripData(null)));
			retailList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryRetailerData(null)));
			hotSpotList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryHotSpotData(null)));
			locationList.setAll(FXCollections.observableArrayList(Main.getCurrentDb().queryLocationData(null)));

			bikeTripTableView.getSelectionModel().select(null);
			retailerTableView.getSelectionModel().select(null);
			hotSpotTableView.getSelectionModel().select(null);
			locationTableView.getSelectionModel().select(null);
		});

		rightClickTripMenu.getItems().add(displayTripMenu);
		rightClickTripMenu.getItems().add(updateTripStats);
		rightClickTripMenu.getItems().add(deleteTripMenu);

		rightClickLocationMenu.getItems().add(displayLocationMenu);
		rightClickLocationMenu.getItems().add(deleteLocationMenu);


		bikeTripTableView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				if (bikeTripTableView.getSelectionModel().getSelectedItem() == null) {
					return;
				}
				if (!bikeTripTableView.getSelectionModel().getSelectedItem().isFavourite()) {
					rightClickTripMenu.getItems().remove(unfavoriteTrip);
					rightClickTripMenu.getItems().remove(favoriteTrip);
					rightClickTripMenu.getItems().add(favoriteTrip);
				} else {
					rightClickTripMenu.getItems().remove(unfavoriteTrip);
					rightClickTripMenu.getItems().remove(favoriteTrip);
					rightClickTripMenu.getItems().add(unfavoriteTrip);
				}
				rightClickTripMenu.show(bikeTripTableView, event.getScreenX(), event.getScreenY());
			}
		});

		retailerTableView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				if (retailerTableView.getSelectionModel().getSelectedItem() == null) {
					return;
				}
				if (!retailerTableView.getSelectionModel().getSelectedItem().isFavourite()) {
					rightClickLocationMenu.getItems().remove(unfavoriteEntry);
					rightClickLocationMenu.getItems().remove(favoriteEntry);
					rightClickLocationMenu.getItems().add(favoriteEntry);
				} else {
					rightClickLocationMenu.getItems().remove(unfavoriteEntry);
					rightClickLocationMenu.getItems().remove(favoriteEntry);
					rightClickLocationMenu.getItems().add(unfavoriteEntry);
				}
				rightClickLocationMenu
					.show(retailerTableView, event.getScreenX(), event.getScreenY());
			}
		});

		hotSpotTableView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				if (hotSpotTableView.getSelectionModel().getSelectedItem() == null) {
					return;
				}
				if (!hotSpotTableView.getSelectionModel().getSelectedItem().isFavourite()) {
					rightClickLocationMenu.getItems().remove(unfavoriteEntry);
					rightClickLocationMenu.getItems().remove(favoriteEntry);
					rightClickLocationMenu.getItems().add(favoriteEntry);
				} else {
					rightClickLocationMenu.getItems().remove(unfavoriteEntry);
					rightClickLocationMenu.getItems().remove(favoriteEntry);
					rightClickLocationMenu.getItems().add(unfavoriteEntry);
				}
				rightClickLocationMenu
					.show(hotSpotTableView, event.getScreenX(), event.getScreenY());
			}
		});

		locationTableView.setOnContextMenuRequested(event -> {
			if (locationTableView.getSelectionModel().getSelectedItem() == null) {
				return;
			}
			if (!locationTableView.getSelectionModel().getSelectedItem().isFavourite()) {
				rightClickLocationMenu.getItems().remove(unfavoriteEntry);
				rightClickLocationMenu.getItems().remove(favoriteEntry);
				rightClickLocationMenu.getItems().add(favoriteEntry);
			} else {
				rightClickLocationMenu.getItems().remove(unfavoriteEntry);
				rightClickLocationMenu.getItems().remove(favoriteEntry);
				rightClickLocationMenu.getItems().add(unfavoriteEntry);
			}
			rightClickLocationMenu.show(locationTableView, event.getScreenX(), event.getScreenY());
		});

		tripSearchProgressIndicator.setVisible(false);
		retailerProgressIndicator.setVisible(false);

		clearFilterBikeTripsButton.setVisible(false);
		clearFilterRetailersButton.setVisible(false);
		clearLocationFilterButton.setVisible(false);
		clearFilterHotSpotsButton.setVisible(false);

		selectStartDatabase();
		/////////////////////**************************************************////////////////////////////////
		//sections sets all the settings for the edit committing on the relevant columns of the tables
		/////////////////////**************************************************////////////////////////////////
		//biketrips
		startTime.setOnEditCommit(event -> {
			if (event.getNewValue().isBefore(bikeTripTableView.getSelectionModel().getSelectedItem().getEndTime())) {
                event.getTableView().getItems().get(event.getTablePosition().getRow())
                        .setStartTime(event.getNewValue());
                bikeTripTableView.getColumns().get(0).setVisible(false);
                bikeTripTableView.getColumns().get(0).setVisible(true);
                System.out.println("Edited");
                BikeTrip edit = bikeTripTableView.getSelectionModel().getSelectedItem();
                if (Main.getCurrentDb()
                        .updateEntry(DataType.BIKETRIP, "startTime = '" + edit.getStartTime() + "'" +
                                String.format(", duration = '%d%n'", edit.getDuration()), edit.getId())) {
                    System.out.println("Successful table update");
                } else {
                    System.out.println("Failed table update");
                }
            } else {
                AlertDialog.showWarning("Entry error", "Invalid Entry","Start time can not be after end time" );
            }

		});

		endTime.setOnEditCommit(event -> {
			if (event.getNewValue()
				.isAfter(bikeTripTableView.getSelectionModel().getSelectedItem().getStartTime())) {
				event.getTableView().getItems().get(event.getTablePosition().getRow())
					.setEndTime(event.getNewValue());
                System.out.println("edited");
                BikeTrip edit = bikeTripTableView.getSelectionModel().getSelectedItem();
                bikeTripTableView.getColumns().get(0).setVisible(false);
                bikeTripTableView.getColumns().get(0).setVisible(true);
                if (Main.getCurrentDb()
                        .updateEntry(DataType.BIKETRIP, "endTime = '" + edit.getEndTime() + "'" +
                                String.format(", duration = '%d%n'", edit.getDuration()), edit.getId())) {
                    System.out.println("Successful table update");
                } else {
                    System.out.println("Failed table update");
                }
			} else {
			    AlertDialog.showWarning("Entry error", "Invalid Entry","End time can not be before start time" );
            }

		});

		dataGender.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setGender(event.getNewValue());
			System.out.println("edited" + bikeTripTableView.getSelectionModel().getSelectedItem());
			BikeTrip edit = bikeTripTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.BIKETRIP, String.format("gender = %s", edit.getGender()),
					edit.getId())) {
				System.out.println("Successful table update");
			} else {
				System.out.println("Failed table update");
			}
		});

		//retailers
		retailerName.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setName(event.getNewValue());
			Retailer edit = retailerTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.RETAILER, String.format("name = '%s'", edit.getName()),
					edit.getId())) {
				System.out.println("Successful table update");
			} else {
				System.out.println("Failed table update");
			}
		});

		retailPrType.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setPrimaryType(event.getNewValue());
			Retailer edit = retailerTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.RETAILER,
					String.format("primaryType = '%s'", edit.getPrimaryType()),
					edit.getId())) {
				System.out.println("Successful table update");
			} else {
				System.out.println("Failed table update");
			}
		});

		retailScType.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setSecondaryType(event.getNewValue());
			Retailer edit = retailerTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.RETAILER,
					String.format("secondaryType = '%s'", edit.getSecondaryType()),
					edit.getId())) {
				System.out.println("Successful table update");
			} else {
				System.out.println("Failed table update");
			}
		});

		retailBlock.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setBlock(event.getNewValue());
			Retailer edit = retailerTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.RETAILER, String.format("block = '%d'", edit.getBlock()),
					edit.getId())) {
				System.out.println("Successful table update");
			} else {
				System.out.println("Failed table update");
			}
		});

		retailLot.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setLot(event.getNewValue());
			Retailer edit = retailerTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.RETAILER, String.format("lot = '%d'", edit.getLot()),
					edit.getId())) {
				System.out.println("Successful table update");
			} else {
				System.out.println("Failed table update");
			}
		});

		//Wi-Fi locations
		hotSpotName.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setName(event.getNewValue());
			HotSpot edit = hotSpotTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.HOTSPOT, String.format("name = '%s'", edit.getName()),
					edit.getId())) {
				System.out.println("Successful table edit");
			} else {
				System.out.println("Unsuccessful table edit");
			}
		});

		hotSpotBorough.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setBorough(event.getNewValue());
			HotSpot edit = hotSpotTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.HOTSPOT, String.format("borough = '%s'", edit.getBorough()),
					edit.getId())) {
				System.out.println("Successful table edit");
			} else {
				System.out.println("Unsuccessful table edit");
			}
		});

		hotSpotLocationType.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setLocationType(event.getNewValue());
			HotSpot edit = hotSpotTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.HOTSPOT,
					String.format("locationType = '%s'", edit.getLocationType()),
					edit.getId())) {
				System.out.println("Successful table edit");
			} else {
				System.out.println("Unsuccessful table edit");
			}
		});

		hotSpotPolicy.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setPolicy(event.getNewValue());
			HotSpot edit = hotSpotTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.HOTSPOT, String.format("policy = '%s'", edit.getPolicy()),
					edit.getId())) {
				System.out.println("Successful table edit");
			} else {
				System.out.println("Unsuccessful table edit");
			}
		});

		hotSpotPolicyDisc.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setPolicyDescription(event.getNewValue());
			HotSpot edit = hotSpotTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.HOTSPOT,
					String.format("policyDescription = '%s'", edit.getPolicyDescription()),
					edit.getId())) {
				System.out.println("Successful table edit");
			} else {
				System.out.println("Unsuccessful table edit");
			}
		});

		hotSpotProvider.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow())
				.setProvider(event.getNewValue());
			HotSpot edit = hotSpotTableView.getSelectionModel().getSelectedItem();
			if (Main.getCurrentDb()
				.updateEntry(DataType.HOTSPOT, String.format("provider = '%s'", edit.getProvider()),
					edit.getId())) {
				System.out.println("Successful table edit");
			} else {
				System.out.println("Unsuccessful table edit");
			}
		});
	}
}