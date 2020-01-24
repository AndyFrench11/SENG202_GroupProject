package seng202.team4.Model;

/**
 * Enum declaring constants for each table name in the SQLite database.
 */
public enum DataType {
	BIKETRIP("BikeTrips"),
	RETAILER("Retailers"),
	HOTSPOT("HotSpots"),
	LOCATION("Locations");

	private final String tableName;

	DataType(final String tableName) {
		this.tableName = tableName;
	}

	public String toString() {
		return tableName;
	}
}