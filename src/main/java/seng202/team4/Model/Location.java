package seng202.team4.Model;

/**
 * Stores information about a single location.
 */
public class Location {

	protected String name, city;
	protected boolean favourite;
	protected double latitude, longitude;
	protected int id;

	/**
	 * Constructs a new location.
	 *
	 * @param name The name of the location
	 * @param city The city this location is in
	 * @param id The primary key id of this location
	 * @param latitude The latitude coordinate of this location
	 * @param longitude The longitude coordinate of this location
	 * @param favourite Whether this location is a favourite
	 */
	public Location(String name, String city, int id, double latitude, double longitude,
		boolean favourite) {
		this.name = name;
		this.city = city;
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.favourite = favourite;
	}

	public String getName() {
		return name;
	}

	public boolean isFavourite() {
		return favourite;
	}

	public String getCity() {
		return city;
	}

	public int getId() {
		return id;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setName(String newName) {
		name = newName;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}

	/**
	 * Returns a string which can be used for geocoding queries.
	 *
	 * @return The geocoding-usable string
	 */
	public String locationString() {
		return String.format("%s, %s", name, city);
	}

	public String toString() {
		return String.format(String.format("%s, %s at (%f, %f)", name, city, latitude, longitude));
	}

}