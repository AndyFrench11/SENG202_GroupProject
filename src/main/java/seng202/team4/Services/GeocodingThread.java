package seng202.team4.Services;

import java.util.HashSet;
import seng202.team4.Model.DataType;
import seng202.team4.Model.Location;
import seng202.team4.Model.Main;

/**
 * Thread class to geocode a list of some location type, and update a SQLite database with the new
 * latitude and longitude found.
 *
 * @param <LocationType> Location or a subclass of location
 */
public class GeocodingThread<LocationType extends Location> extends Thread {

	private Thread t;
	private String name;
	private HashSet<LocationType> toGeocode;
	private DataType dataType;
	private SQLHandler sqlHandler;

	/**
	 * Constructs a new geocoding thread.
	 *
	 * @param name The thread name
	 * @param toGeocode The locations to geocode
	 * @param dataType The data type of the locations to geocode
	 * @param sqlHandler The database to write updated latitudes and longitudes to
	 */
	public GeocodingThread(
		String name, HashSet<LocationType> toGeocode, DataType dataType, SQLHandler sqlHandler) {
		this.name = name;
		this.toGeocode = toGeocode;
		this.dataType = dataType;
		this.sqlHandler = sqlHandler;
	}

	/**
	 * Geocodes the locations in the toGeocode HashSet and saves them to the associated database.
	 */
	public void run() {
		double[] latLong = {0.0, 0.0};
		int attempts;
		for (LocationType location : toGeocode) {
			attempts = 0;
			while (latLong[0] == 0.0 && latLong[1] == 0.0) {
				latLong = Geocoder.getLatitudeLongitude(location.locationString());
				attempts++;
				if (attempts > 3) {
					System.err.println("Failed to geocode " + location.getName());
					break;
				}
			}
			sqlHandler.updateEntry(dataType,
				String.format("latitude = %f, longitude = %f, x = %f, y = %f",
					latLong[0], latLong[1], SQLHandler.convertToX(latLong[0]),
					SQLHandler.convertToY(latLong[0], latLong[1])), location.getId());
			//System.out.println(String.format("%s geocoded %s", name, location.getName()));
			Main.incrementGeocodesComplete();
		}
		System.out.println(String.format("Thread %s exiting.", name));
	}

	/**
	 * Starts the thread.
	 */
	public void start() {
		if (t == null) {
			t = new Thread(this, name);
			t.start();
		}
	}
}
