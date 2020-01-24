package seng202.team4.Controller;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.directions.*;
import com.lynden.gmapsfx.util.MarkerImageFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import netscape.javascript.JSObject;
import org.controlsfx.control.Rating;
import seng202.team4.Model.*;
import seng202.team4.Services.AlertDialog;
import seng202.team4.Services.Geocoder;
import seng202.team4.Services.SQLHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static java.lang.Math.round;

/**
 * Is instantiated when a MapView.fxml is loaded and allows interaction with the Map.
 *
 * Handles methods relating to visualising data on the Google Map view.
 */
public class MapController implements Initializable, MapComponentInitializedListener,
	DirectionsServiceCallback {

	private ArrayList<BikeTrip> mapsBikeTrips = new ArrayList<>();
	private ArrayList<Retailer> mapsRetailers = new ArrayList<>();
	private ArrayList<HotSpot> mapsHotspots = new ArrayList<>();
	private ArrayList<Location> mapsLocations = new ArrayList<>();

	private SQLHandler currentDB;


	@FXML
	private GoogleMapView mapView;

	private GoogleMap map;

	@FXML
	private GridPane infoGridPane;
	@FXML
	private Label currentTripStartLabel;
	@FXML
	private Label currentTripEndLabel;
	@FXML
	private Label currentTripDateLabel;
	@FXML
	private Label currentTripDurationLabel;
	@FXML
	private Label currentTripDistanceLabel;
	@FXML
	private Label currentTripGenderLabel;
	@FXML
	private Label currentTripSpeedLabel;
	@FXML
	private ComboBox<String> nearestComboBox;
	@FXML
	private TextField rangeTextField;
	@FXML
	private Rating tripRatingInput;

    private ChangeListener<Number> ratingListener;

	private BikeTrip currentBikeTrip;

	private Location currentLocation;


	// Declares the lists that will hold all markers and shapes (lines) on the GMap
	private ArrayList<Marker> hotspotMarkerList = new ArrayList<>();
	private ArrayList<Marker> retailerMarkerList = new ArrayList<>();
	private ArrayList<Marker> locationMarkerList = new ArrayList<>();

	private DirectionsService directionsService;
	private DirectionsRenderer directionsRenderer;
	private DirectionsPane directionsPane;

	private boolean routeLoaded = false;
	private boolean mapLoaded = false;

	public boolean getRouteLoaded() {
		return routeLoaded;
	}

	public ArrayList<Marker> getHotspotMarkerList() {
		return hotspotMarkerList;
	}

	public ArrayList<Marker> getRetailerMarkerList() {
		return retailerMarkerList;
	}

	public ArrayList<Marker> getLocationMarkerList() {
		return locationMarkerList;
	}



	@Override
	public void initialize(URL url, ResourceBundle rb) {
		infoGridPane.setBackground(
			new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		infoGridPane.setBorder(new Border(new BorderStroke(Color.BLACK,
			BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		infoGridPane.setVisible(false);
		mapView.addMapInializedListener(this);

		rangeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*(\\.\\d*)?")) {
				rangeTextField.setText(newValue.replaceAll("[^\\d(\\.\\d)?]", ""));
			}
		});
		nearestComboBox.getItems().addAll("Retailers", "Hotspots");
		checkCurrentDB();
	}

	/**
	 * Sets up the map options, called by GMapsFX internally.
	 */
	@Override
	public void mapInitialized() {
		System.out.println("MapController Initialised...");

		// Returns message to STDOUT when map is ready
		mapView.addMapReadyListener(() -> {
			// This call will fail unless the map is completely ready and subsequent display calls would fail also.
			System.out.println("Map ready...");
			mapLoaded = true;
		});

		// Specifies the map options (currently centred at NYC)
		MapOptions options = new MapOptions();
		options.center(new LatLong(40.730610, -73.935242))
			.zoomControl(true)
			.zoom(12)
			.overviewMapControl(false)
			.mapType(MapTypeIdEnum.ROADMAP);

		map = mapView.createMap(options);
		directionsService = new DirectionsService();
		directionsPane = mapView.getDirec();

	}

	public void setCurrentBikeTrip(BikeTrip currentBikeTrip) {
		this.currentBikeTrip = currentBikeTrip;
	}

	/**
	 * Fetches the current database from Main
	 */
	public void checkCurrentDB() {
		currentDB = Main.getCurrentDb();
	}

	/**
	 * Removes all currently plotted entries, clears the marker and line lists and then performs a
	 * recenter.
	 */
	public void clearMap(boolean clearRoute, boolean clearHotspots, boolean clearRetailers,
		boolean clearLocations) {
		if (mapLoaded) {
			if (clearRoute) {
				if (routeLoaded) {
					directionsRenderer.clearDirections();
				}
			}
			if (clearHotspots) {
				map.removeMarkers(hotspotMarkerList);
				hotspotMarkerList.clear();
			}
			if (clearRetailers) {
				map.removeMarkers(retailerMarkerList);
				retailerMarkerList.clear();
			}
			if (clearLocations) {
				map.removeMarkers(locationMarkerList);
				locationMarkerList.clear();
			}
		}
	}

	/**
	 * Resets the map, clearing the map and recentring the map
	 */
	public void resetMap() {
		clearMap(true, true, true, true);
		map.setCenter(new LatLong(40.730610, -73.935242));
		map.setZoom(12);
		infoGridPane.setVisible(false);
	}

	/**
	 * Checks if the map is currently loaded with markers
	 */
	public void checkMapLoaded() {
		if (!mapLoaded) {
			AlertDialog.showWarning("Connection Error", "",
				"Google Maps connection error, application will now close, please restart application.");
			System.exit(0);
		}
	}

	/**
	 * Start function to check validity of search function of the current bike trip
	 * relative to Retailers/HotSpots
	 */
	public void findNearest() {
		String selectedType = nearestComboBox.getSelectionModel().getSelectedItem();
		String selectedRangeStr = rangeTextField.getText();
		if (selectedRangeStr == null || selectedRangeStr.isEmpty() || selectedType == null) {
			AlertDialog.showInformation("Missing Details", "", "Please select a type and range.");
			return;
		}
		if (selectedRangeStr.length() > 9) {
			AlertDialog.showWarning("Invalid Range", "", "Please enter a reasonable range.");
			rangeTextField.clear();
			return;
		}
		int selectedRange = Integer.parseInt(rangeTextField.getText());
		switch (selectedType) {
			case "Retailers":
				if (mapsRetailers.isEmpty()) {
					AlertDialog.showWarning("No Data", "", "No retailer data imported.");
					return;
				}
				findNearestRetailer(selectedRange);
				break;
			case "Hotspots":
				if (mapsHotspots.isEmpty()) {
					AlertDialog.showWarning("No Data", "", "No hotspot data imported.");
					return;
				}
				findNearestHotspot(selectedRange);
				break;
		}
	}

	/**
	 * Finds the nearest Hotspot to the current Bike Trip
	 * @param range The given range from the bounding box
	 */
	public void findNearestHotspot(int range) {
		checkCurrentDB();
		clearMap(false, true, true, true);
		ArrayList<HotSpot> nearbyHotspots = currentDB
			.findHotSpotsNearTrip(currentBikeTrip, range);
		if (nearbyHotspots.isEmpty()) {
			return;
		}

		mapsHotspots.clear();
		mapsHotspots.addAll(nearbyHotspots);
		updateHotspotMarkers();
		int currentZoom = map.getZoom();
		map.setZoom(currentZoom - 1);
		map.setZoom(currentZoom);
	}

	/**
	 * Finds the nearest retailer to the current Bike Trip
	 * @param range The given range from the bounding box
	 */
	public void findNearestRetailer(int range) {
		checkCurrentDB();
		clearMap(false, true, true, true);
		ArrayList<Retailer> nearbyRetailers = currentDB
			.findRetailersNearTrip(currentBikeTrip, range);
		if (nearbyRetailers.isEmpty()) {
			AlertDialog.showInformation("No Nearby Retailers", "", "No nearby retailers found.");
			return;
		}

		mapsRetailers.clear();
		mapsRetailers.addAll(nearbyRetailers);
		updateRetailerMarkers();
		int currentZoom = map.getZoom();
		map.setZoom(currentZoom - 1);
		map.setZoom(currentZoom);
	}


	public void setMapsBikeTrips(ArrayList<BikeTrip> mapsBikeTrips) {
		clearMap(false, false, false, false);
		this.mapsBikeTrips = mapsBikeTrips;
	}

	public void setMapsLocations(ArrayList<Location> mapsLocations) {
		clearMap(false, false, false, true);
		this.mapsLocations = mapsLocations;
	}

	public void setMapsRetailers(ArrayList<Retailer> mapsRetailers) {
		clearMap(false, false, true, false);
		this.mapsRetailers = mapsRetailers;
	}

	public void setMapsHotspots(ArrayList<HotSpot> mapsHotspots) {
		clearMap(false, true, false, false);
		this.mapsHotspots = mapsHotspots;
	}

	/**
	 * Displays all currently stored retailers onto the Google Maps View pane, with popups.
	 */
	public void updateRetailerMarkers() {
		checkMapLoaded();
		LatLong newCenter = null;
		for (Marker currentMarker : retailerMarkerList) {
			map.removeMarker(currentMarker);
		}
		retailerMarkerList.clear();

		// Change this value to modify max no. of retailers to process + geocode
		int maxRetailersToProcess = 1000;

		for (Retailer currentRetailer : mapsRetailers) {

			// Currently limits to 30 entries, else the map is unreadable
			int i = 0;
			if ((i++) > maxRetailersToProcess) {
				break;
			}

			LatLong retailerCoords = new LatLong(currentRetailer.getLatitude(),
				currentRetailer.getLongitude());

			// Specifies the marker options for the start and stop markers
			MarkerOptions currentOptions = new MarkerOptions();
			currentOptions.position(retailerCoords);
			currentOptions.title(Integer.toString(currentRetailer.getId()));
			String retailerIconPath = MarkerImageFactory
				.createMarkerImage("/Images/Icons/RetailerIcon.png", "png");
			retailerIconPath = retailerIconPath.replace("(", "");
			retailerIconPath = retailerIconPath.replace(")", "");
			currentOptions.icon(retailerIconPath);

			// Instantiates the new markers with provided options
			Marker currentRetailerMarker = new Marker(currentOptions);

			// Specifies the small popup windows for each marker
			InfoWindowOptions infoStartOptions = new InfoWindowOptions();
			infoStartOptions
				.content(String.format("<b>%s</b><br>%s<br><br>%s", currentRetailer.getName(),
					currentRetailer.getPrimaryType(), currentRetailer.getStreet()))
				.position(retailerCoords);
			InfoWindow startPopupWindow = new InfoWindow(infoStartOptions);

			// Add handlers to open the small popup windows
			map.addUIEventHandler(currentRetailerMarker, UIEventType.click, (JSObject obj) -> {
				startPopupWindow.open(map, currentRetailerMarker);
			});

			retailerMarkerList.add(currentRetailerMarker);
			newCenter = retailerCoords;
		}

		// Process each marker onto the list (maybe speed improvements vs. adding 1 by 1?)
		for (Marker currentMarker : retailerMarkerList) {
			map.addMarker(currentMarker);
		}
		map.setCenter(newCenter);
	}


	/**
	 * Displays all currently stored hotspots onto the Google Maps View pane, with popups.
	 */
	public void updateHotspotMarkers() {
		checkMapLoaded();
		LatLong newCenter = null;
		for (Marker currentMarker : hotspotMarkerList) {
			map.removeMarker(currentMarker);
		}
		hotspotMarkerList.clear();

		// Change this value to modify max no. of retailers to process + geocode
		int maxHotspotsToProcess = 1000;

		int i = 0;
		for (HotSpot currentHotspot : this.mapsHotspots) {

			// Currently limits to 30 entries, else the map is unreadable
			if ((i++) > maxHotspotsToProcess) {
				break;
			}

			LatLong hotspotCoords = new LatLong(currentHotspot.getLatitude(),
				currentHotspot.getLongitude());

			// Specifies the marker options for the start and stop markers
			MarkerOptions currentOptions = new MarkerOptions();
			currentOptions.position(hotspotCoords);
			currentOptions.title(Integer.toString(currentHotspot.getId()));
			String hotSpotIcon = MarkerImageFactory
				.createMarkerImage("/Images/Icons/HotSpotIcon.png", "png");
			hotSpotIcon = hotSpotIcon.replace("(", "");
			hotSpotIcon = hotSpotIcon.replace(")", "");
			currentOptions.icon(hotSpotIcon);

			// Instantiates the new markers with provided options
			Marker currentHotspotMarker = new Marker(currentOptions);

			// Specifies the small popup windows for each marker
			InfoWindowOptions infoStartOptions = new InfoWindowOptions();
			infoStartOptions
				.content(String.format("<b>SSID: %s</b><br>%s<br><br>%s", currentHotspot.getName(),
					currentHotspot.getPolicy(), currentHotspot.getLocationType()))
				.position(hotspotCoords);
			InfoWindow startPopupWindow = new InfoWindow(infoStartOptions);

			// Add handlers to open the small popup windows
			map.addUIEventHandler(currentHotspotMarker, UIEventType.click, (JSObject obj) -> {
				startPopupWindow.open(map, currentHotspotMarker);
			});

			hotspotMarkerList.add(currentHotspotMarker);
			newCenter = hotspotCoords;
		}

		// Process each marker onto the list (maybe speed improvements vs. adding 1 by 1?)
		for (Marker currentMarker : hotspotMarkerList) {
			map.addMarker(currentMarker);
		}
		map.setCenter(newCenter);
	}

	/**
	 * Displays all currently stored locations onto the Google Maps View pane, with popups.
	 */
	public void updateLocationMarkers() {
		checkMapLoaded();
		LatLong newCenter = null;
		for (Marker currentMarker : locationMarkerList) {
			map.removeMarker(currentMarker);
		}
		locationMarkerList.clear();

		// Change this value to modify max no. of retailers to process + geocode
		int maxLocationsToProcess = 1000;

		int i = 0;
		for (Location currentLocation : this.mapsLocations) {

			// Currently limits to 30 entries, else the map is unreadable
			if ((i++) > maxLocationsToProcess) {
				break;
			}

			LatLong locationCoords = new LatLong(currentLocation.getLatitude(),
				currentLocation.getLongitude());

			// Specifies the marker options for the start and stop markers
			MarkerOptions currentOptions = new MarkerOptions();
			currentOptions.position(locationCoords);
			currentOptions.title(Integer.toString(currentLocation.getId()));
			String locationIcon = MarkerImageFactory
				.createMarkerImage("/Images/Icons/LocationIcon.png", "png");
			locationIcon = locationIcon.replace("(", "");
			locationIcon = locationIcon.replace(")", "");
			currentOptions.icon(locationIcon);

			// Instantiates the new markers with provided options
			Marker currentLocationMarker = new Marker(currentOptions);

			// Specifies the small popup windows for each marker

			InfoWindowOptions infoStartOptions = new InfoWindowOptions();
			infoStartOptions.content(String.format("<b>%s</b><br>%s", currentLocation.getName(),
				currentLocation.getCity()))
				.position(locationCoords);

			InfoWindow startPopupWindow = new InfoWindow(infoStartOptions);

			// Add handlers to open the small popup windows
			map.addUIEventHandler(currentLocationMarker, UIEventType.click, (JSObject obj) -> {
				startPopupWindow.open(map, currentLocationMarker);
			});

			locationMarkerList.add(currentLocationMarker);
			newCenter = locationCoords;
		}

		// Process each marker onto the list (maybe speed improvements vs. adding 1 by 1?)
		for (Marker currentMarker : locationMarkerList) {
			map.addMarker(currentMarker);
		}
		map.setCenter(newCenter);
	}

	/**
	 * GMapsFX internally called method, sets routeLoaded flag to allow clearing of any loaded route.
	 */
	public void directionsReceived(DirectionsResult results,
		DirectionStatus status) {
		if (status.toString().equals("OK")) {
			routeLoaded = true;
		}
	}

	/**
	 * Displays a single locationToDisplay on the maps view
	 *
	 * @param locationToDisplay The location to display
	 */
	public void displayLocation(Location locationToDisplay) {
		checkMapLoaded();
		if (!routeLoaded) {
			infoGridPane.setVisible(false);
		}
		currentLocation = locationToDisplay;

		LatLong locationCoords = new LatLong(currentLocation.getLatitude(),
			currentLocation.getLongitude());

		// Specifies the marker options for the start and stop markers
		MarkerOptions currentOptions = new MarkerOptions();
		currentOptions.position(locationCoords);
		currentOptions.title(Integer.toString(currentLocation.getId()));
		String locationIcon = "";
		if (Main.getTabState() == DataType.RETAILER) {
			locationIcon = MarkerImageFactory.createMarkerImage("/Images/Icons/RetailerIcon.png", "png");
		} else if (Main.getTabState() == DataType.HOTSPOT) {
			locationIcon = MarkerImageFactory.createMarkerImage("/Images/Icons/HotSpotIcon.png", "png");
		} else if (Main.getTabState() == DataType.LOCATION) {
			locationIcon = MarkerImageFactory.createMarkerImage("/Images/Icons/LocationIcon.png", "png");
		}

		locationIcon = locationIcon.replace("(", "");
		locationIcon = locationIcon.replace(")", "");
		currentOptions.icon(locationIcon);

		// Instantiates the new markers with provided options
		Marker currentLocationMarker = new Marker(currentOptions);

		// Specifies the small popup windows for each marker
		InfoWindowOptions infoStartOptions = new InfoWindowOptions();
		if (Main.getTabState() == DataType.RETAILER) {
			infoStartOptions.content(
				String.format("<b>%s</b><br>%s<br><br>%s", ((Retailer) currentLocation).getName(),
					((Retailer) currentLocation).getPrimaryType(),
					((Retailer) currentLocation).getStreet()))
				.position(locationCoords);

		} else if (Main.getTabState() == DataType.HOTSPOT) {
			infoStartOptions
				.content(String.format("<b>SSID: %s</b><br>%s<br><br>%s", currentLocation.getName(),
					((HotSpot) currentLocation).getPolicy(),
					((HotSpot) currentLocation).getLocationType()))
				.position(locationCoords);
		} else if (Main.getTabState() == DataType.LOCATION) {
			infoStartOptions.content(String.format("<b>%s</b><br>%s", currentLocation.getName(),
				currentLocation.getCity()))
				.position(locationCoords);
		}

		InfoWindow startPopupWindow = new InfoWindow(infoStartOptions);

		// Add handlers to open the small popup windows
		map.addUIEventHandler(currentLocationMarker, UIEventType.click, (JSObject obj) -> {
			startPopupWindow.open(map, currentLocationMarker);
		});

		locationMarkerList.add(currentLocationMarker);
		map.addMarker(currentLocationMarker);
		map.setCenter(locationCoords);
	}

	/**
	 * Displays the tripToDisplay as a calculated Google Maps route and displays details about the
	 * trip
	 *
	 * @param tripToDisplay The trip to display
	 */
	public void displayRoute(BikeTrip tripToDisplay) {
		currentBikeTrip = tripToDisplay;
		checkMapLoaded();
		clearMap(true, false, false, false);

		String start = Geocoder
			.getLocation(tripToDisplay.getStartLatitude(), tripToDisplay.getStartLongitude());
		String end = Geocoder
			.getLocation(tripToDisplay.getEndLatitude(), tripToDisplay.getEndLongitude());

		if (start == null || end == null) {
			AlertDialog.showWarning("Failed Display", "", "Failed to find route.");
			return;
		}

		ArrayList<DirectionsWaypoint> tripWaypoints = new ArrayList<>();
		for (double[] coord : tripToDisplay.getOtherCoords()) {
			LatLong currentWaypointCoord = new LatLong(coord[0], coord[1]);
			DirectionsWaypoint currentWaypoint = new DirectionsWaypoint(currentWaypointCoord);
			tripWaypoints.add(currentWaypoint);
		}

		DirectionsWaypoint[] waypointArray = new DirectionsWaypoint[10];
		int i = 0;
		for (DirectionsWaypoint currentWaypoint : tripWaypoints) {
			waypointArray[i] = currentWaypoint;
			i++;
		}

		DirectionsRequest request = new DirectionsRequest(start, end, TravelModes.BICYCLING);
		directionsRenderer = new DirectionsRenderer(true, mapView.getMap(), directionsPane);
		directionsService.getRoute(request, this, directionsRenderer);

		String startSt = start.substring(0, start.indexOf(','));
		String startCity = start.substring(start.indexOf(',') + 1, start.length());
		startCity = startCity.substring(0, startCity.indexOf(','));

		String endSt = end.substring(0, end.indexOf(','));
		String endCity = end.substring(end.indexOf(',') + 1, end.length());
		endCity = endCity.substring(0, endCity.indexOf(','));

		currentTripStartLabel.setText(startSt + ", " + startCity);
		currentTripEndLabel.setText(endSt + ", " + endCity);
		Integer day = tripToDisplay.getStartTime().getDayOfMonth();
		Integer month = tripToDisplay.getStartTime().getMonthValue();
		Integer year = tripToDisplay.getStartTime().getYear();
		currentTripDateLabel.setText(String.format("%s/%s/%s", day, month, year));
		currentTripDurationLabel.setText(tripToDisplay.getDuration() + " min");
		currentTripDistanceLabel.setText(tripToDisplay.getDistance() + " km");
		if (tripToDisplay.getGender() == "") {
			currentTripGenderLabel.setText("~");
		} else {
			currentTripGenderLabel.setText(tripToDisplay.getGender());
		}
		currentTripSpeedLabel
			.setText(Long.toString(round(tripToDisplay.getAvgSpeed()), 0) + " km/h");

		checkCurrentDB();
		ratingListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue,
				Number newValue) {
				currentBikeTrip.addRating(newValue.intValue());
				currentDB.updateRatings(tripToDisplay);
				MainController.getBikeTripList()
					.setAll(FXCollections.observableArrayList(Main.getCurrentDb()
						.queryBikeTripData(null)));
				System.out.println("Added rating of " + newValue);
				System.out.println("Current avg: " + currentBikeTrip.getAvgRating() + "\n");
			}
		};
		tripRatingInput.ratingProperty().removeListener(ratingListener);
		tripRatingInput.setRating(tripToDisplay.getAvgRating());
		tripRatingInput.ratingProperty().addListener(ratingListener);

		infoGridPane.setVisible(true);
	}
}
