package seng202.team4.Services;

import com.opencsv.CSVReader;
import java.util.HashSet;
import java.util.Properties;
import org.apache.commons.dbutils.DbUtils;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.JournalMode;
import org.sqlite.SQLiteConfig.SynchronousMode;
import seng202.team4.Model.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import static java.sql.DriverManager.getConnection;

/**
 * A class to import data from SQLite or csv data, and export to a SQLite database. Can also
 * query/insert/update/remove its associated SQLite database and return results, if any, in an
 * ArrayList.
 */
public class SQLHandler {

	private String path;
	private String name;
	private Properties config;

	/**
	 * Instantiates a new SQLHandler.
	 *
	 * @param jarDir The path to the directory the database file will be stored in
	 * @param name The name of the database file
	 */
	public SQLHandler(String jarDir, String name) {
		this.name = name;
		this.path = String.format("jdbc:sqlite:%s%s%s", jarDir, File.separatorChar, name);
		SQLiteConfig sqlConfig = new SQLiteConfig();
		sqlConfig.setSynchronous(SynchronousMode.OFF);
		sqlConfig.setJournalMode(JournalMode.MEMORY);
		config = sqlConfig.toProperties();
	}

	public String toString() {
		return name;
	}

	/**
	 * Creates the database file and tables if they do not already exist.
	 *
	 * @return Whether the operation completed successfully
	 */
	public boolean createDatabase() {
		Connection conn = null;
		Statement stmt = null;
		boolean creationSuccessful = true;

		synchronized (this) {
			try {
				//Create the database file if it doesn't yet exist
				conn = getConnection(path, config);
				System.out.println(String.format("Database at %s accessed.", path));

				//Create tables if they do not already exist
				stmt = conn.createStatement();

				//Stores bike trips, creates a unique ID to avoid duplicate entries
				stmt.execute("CREATE TABLE IF NOT EXISTS BikeTrips(" +
					"ID             integer primary key, " +
					"bikeID         integer, " +
					"startLatitude  real not null, " +
					"startLongitude real not null, " +
					"endLatitude    real not null, " +
					"endLongitude   real not null, " +
					"otherCoords	real not null, " +
					"startTime      real not null, " +
					"endTime        real not null, " +
					"duration		integer not null, " +
					"gender         text, " +
					"distance       real, " +
					"avgSpeed       real, " +
					"caloriesBurnt  real, " +
					"avgRating      real, " +
					"ratings        text, " +
					"favourite		integer default 0, " +

					"startX         real not null, " +
					"startY         real not null, " +
					"endX           real not null, " +
					"endY           real not null " +
					");");
				stmt.execute(
					"CREATE UNIQUE INDEX IF NOT EXISTS tripID ON BikeTrips(bikeID, startLatitude, "
						+ "startLongitude, endLatitude, otherCoords, endLongitude, "
						+ "startTime, endTime);");

				//Stores wi-fi hot spots, creates a unique ID to avoid duplicate entries
				stmt.execute("CREATE TABLE IF NOT EXISTS HotSpots(" +
					"ID             integer primary key, " +
					"name           text not null, " +
					"city           text not null, " +
					"latitude       real not null, " +
					"longitude      real not null, " +

					"borough        text not null, " +
					"locationInfo   text, " +
					"locationType   text, " +
					"policy         text, " +
					"policyDescription text, " +
					"provider       text, " +
					"favourite		integer default 0, " +

					"x              real not null, " +
					"y              real not null " +
					");");
				stmt.execute(
					"CREATE UNIQUE INDEX IF NOT EXISTS hotSpotID ON HotSpots(name, city, latitude, "
						+ "longitude, borough, locationInfo, locationType, policy, policyDescription, "
						+ "provider);");

				//Stores retailers, creates a unique ID to avoid duplicate entries
				stmt.execute("CREATE TABLE IF NOT EXISTS Retailers(" +
					"ID             integer primary key, " +
					"name           text not null, " +
					"city           text not null, " +
					"latitude       real, " +
					"longitude      real, " +

					"street         text not null, " +
					"state          text not null, " +
					"zipCode        integer, " +
					"primaryType    text not null, " +
					"secondaryType  text not null, " +
					"block          integer not null, " +
					"lot            integer not null, " +
					"favourite		integer default 0, " +

					"x              real, " +
					"y              real " +
					");");
				stmt.execute(
					"CREATE UNIQUE INDEX IF NOT EXISTS retailerID ON Retailers(name, city, street, "
						+ "state, primaryType, secondaryType);");

				//Stores raw locations, creates a unique ID to avoid duplicate entries
				stmt.execute("CREATE TABLE IF NOT EXISTS Locations(" +
					"ID             integer primary key, " +
					"name           text not null, " +
					"city           text, " +
					"latitude       real not null, " +
					"longitude      real not null, " +
					"favourite		integer default 0, " +

					"x              real not null, " +
					"y              real not null " +
					");");
				stmt.execute(
					"CREATE UNIQUE INDEX IF NOT EXISTS locationID ON Locations(name, latitude, "
						+ "longitude);");

			} catch (SQLException e) {
				e.printStackTrace();
				creationSuccessful = false;
			} finally {
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return creationSuccessful;
	}

	/**
	 * Delete the database referenced by this SQLHandler.
	 *
	 * @return Whether the delete operation was successful
	 */
	public boolean deleteDatabase() {
		File database = new File(path.substring(12));
		synchronized (this) {
			return database.delete();
		}
	}

	/**
	 * Start up to 50 threads to geocode all entries of a data type with lat and long 0,0.
	 *
	 * @param dataType The data type to geocode
	 */
	private void threadedGeocode(DataType dataType) {
		int threadNum;
		int locationsInThread;
		int currentIndex = 0;
		ArrayList<Location> locations = new ArrayList<>();
		HashSet<Location> threadLocations;

		switch (dataType) {
			case RETAILER:
				locations.addAll(queryRetailerData("latitude = 0 AND longitude = 0"));
				break;
			case HOTSPOT:
				locations.addAll(queryHotSpotData("latitude = 0 AND longitude = 0"));
				break;
			case LOCATION:
				locations.addAll(queryLocationData("latitude = 0 AND longitude = 0"));
				break;
			default:
				throw new IllegalArgumentException("Invalid dataType");
		}

		threadNum = (locations.size() < 50) ? locations.size() : 50;
		Main.setTotalGeocodesToDo(locations.size());

		for (int i = 0; i < threadNum; i++) {
			threadLocations = new HashSet<>();
			locationsInThread = (i < locations.size() % threadNum) ?
				locations.size() / threadNum + 1 : locations.size() / threadNum;
			for (int j = 0; j < locationsInThread; j++) {
				threadLocations.add(locations.get(currentIndex));
				currentIndex++;
			}
			new GeocodingThread(String.format("%sGeocoder%d", dataType, i),
				threadLocations, dataType, this).start();
		}
	}

	/**
	 * Error checks incoming bike trip data from a CSV file, and creates a PreparedStatement
	 * containing all of the valid entries, ready to be written to an SQLite database.
	 *
	 * @param rawData The raw data from a CSV file in List<String> format
	 * @param conn The database connection to use to create the PreparedStatements
	 * @return The PreparedStatement(s) containing the processed bike trip data
	 * @throws SQLException If the PreparedStatement operations fail due to the database connection
	 * being closed
	 * @throws IllegalArgumentException If no valid bike trip entries were read
	 */
	private PreparedStatement prepareNewBikeTrips(List<String[]> rawData, Connection conn)
		throws SQLException, IllegalArgumentException {
		PreparedStatement pstmt = conn
			.prepareStatement("INSERT OR IGNORE INTO BikeTrips(bikeID, startLatitude, "
				+ "startLongitude, endLatitude, endLongitude, otherCoords, startTime, endTime, "
				+ "duration, gender, startX, startY, endX, endY) "
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		int malformed = 0;
		String[] line;

		String startTime, endTime, gender;
		double startLatitude, startLongitude, endLatitude, endLongitude;
		long duration;

		for (int i = 1; i < rawData.size(); i++) {
			line = rawData.get(i);
			try {
				if (line.length < 14) {
					throw new IllegalArgumentException("Missing information.");
				}

				startTime = line[1].substring(0, 10) + "T" + line[1].substring(11);
				endTime = line[2].substring(0, 10) + "T" + line[2].substring(11);
				//throws DateTimeParseException if startTime or endTime are malformed
				duration = ChronoUnit.MINUTES.between(LocalDateTime.parse(startTime),
					LocalDateTime.parse(endTime));

				switch (line[14]) {
					case "1":
						gender = "M";
						break;
					case "2":
						gender = "F";
						break;
					default:
						gender = null;
				}

				startLatitude = Double.parseDouble(line[5]);
				startLongitude = Double.parseDouble(line[6]);
				endLatitude = Double.parseDouble(line[9]);
				endLongitude = Double.parseDouble(line[10]);

				pstmt.setInt(1, Integer.parseInt(line[11]));
				pstmt.setDouble(2, startLatitude);
				pstmt.setDouble(3, startLongitude);
				pstmt.setDouble(4, endLatitude);
				pstmt.setDouble(5, endLongitude);
				pstmt.setString(6, "");
				pstmt.setString(7, startTime);
				pstmt.setString(8, endTime);
				pstmt.setLong(9, duration);
				pstmt.setString(10, gender);
				pstmt.setDouble(11, convertToX(startLatitude));
				pstmt.setDouble(12, convertToY(startLatitude, startLongitude));
				pstmt.setDouble(13, convertToX(endLatitude));
				pstmt.setDouble(14, convertToY(endLatitude, endLongitude));

				pstmt.addBatch();
			} catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
				malformed++;
			}
		}
		System.out
			.println(
				String.format("%d entries were malformed and therefore discarded. ", malformed));
		if (rawData.size() - 1 == malformed) {
			throw new IllegalArgumentException("No entries were read.");
		}
		return pstmt;
	}

	/**
	 * Error checks incoming hot spot data from a CSV file, and creates a PreparedStatement
	 * containing all of the valid entries, ready to be written to an SQLite database.
	 *
	 * @param rawData The raw data from a CSV file in List<String> format
	 * @param conn The database connection to use to create the PreparedStatements
	 * @return The PreparedStatement(s) containing the processed hot spot data
	 * @throws SQLException If the PreparedStatement operations fail due to the database connection
	 * being closed
	 * @throws IllegalArgumentException If no valid hot spot entries were read
	 */
	private PreparedStatement prepareNewHotSpots(List<String[]> rawData, Connection conn)
		throws SQLException, IllegalArgumentException {
		PreparedStatement pstmt = conn
			.prepareStatement("INSERT OR IGNORE INTO HotSpots(name, city, borough, locationType, "
				+ "provider, policy, policyDescription, locationInfo, latitude, longitude, x, y) "
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		int malformed = 0;
		String[] line;

		String locationInfo;
		double latitude, longitude;
		//Columns: 14=name, 13=city, 0=borough, 11=locationType, 4=provider, 1=policy,
		// 12=policyDescription, 5 and 6=locationInfo
		int[] indicesToCheck = {14, 13, 0, 11, 4, 1, 6};

		for (int i = 1; i < rawData.size(); i++) {
			line = rawData.get(i);
			try {
				if (line.length < 14) {
					throw new IllegalArgumentException("Missing information.");
				}
				for (int index : indicesToCheck) {
					if (line[index].isEmpty()) {
						throw new IllegalArgumentException("Missing information.");
					}
				}

				locationInfo = line[6];
				if (!line[5].isEmpty()) {
					locationInfo = String.format("%s, %s", line[5], locationInfo);
				}

				latitude = Double.parseDouble(line[7]);
				longitude = Double.parseDouble(line[8]);

				for (int j = 0; j < indicesToCheck.length - 1; j++) {
					pstmt.setString(j + 1, line[indicesToCheck[j]]);
				}
				pstmt.setString(7, line[12]);
				pstmt.setString(8, locationInfo);
				pstmt.setDouble(9, latitude);
				pstmt.setDouble(10, longitude);
				pstmt.setDouble(11, convertToX(latitude));
				pstmt.setDouble(12, convertToY(latitude, longitude));

				pstmt.addBatch();
			} catch (IllegalArgumentException e) {
				malformed++;
			}
		}
		System.out
			.println(
				String.format("%d entries were malformed and therefore discarded. ", malformed));
		if (rawData.size() - 1 == malformed) {
			throw new IllegalArgumentException("No entries were read.");
		}
		return pstmt;
	}

	/**
	 * Error checks incoming retailer data from a CSV file, and creates a PreparedStatement
	 * containing all of the valid entries, ready to be written to an SQLite database.
	 *
	 * @param rawData The raw data from a CSV file in List<String> format
	 * @param conn The database connection to use to create the PreparedStatements
	 * @return The PreparedStatement(s) containing the processed retailer data
	 * @throws SQLException If the PreparedStatement operations fail due to the database connection
	 * being closed
	 * @throws IllegalArgumentException If no valid retailer entries were read
	 */
	private PreparedStatement prepareNewRetailers(List<String[]> rawData, Connection conn)
		throws SQLException, IllegalArgumentException {
		PreparedStatement pstmt = conn
			.prepareStatement("INSERT OR IGNORE INTO Retailers(name, city, street, state, " +
				"primaryType, secondaryType, zipCode, block, lot, latitude, longitude) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		int malformed = 0;
		String[] line;

		String[] blockLot;
		//Columns: 0=name, 3=city, 1=street, 4=state, 7=primaryType, 8=secondaryType
		int[] indicesToCheck = {0, 3, 1, 4, 7, 8};

		for (int i = 1; i < rawData.size(); i++) {
			line = rawData.get(i);
			try {
				if (line.length < 11) {
					throw new IllegalArgumentException("Missing information.");
				}
				for (int index : indicesToCheck) {
					if (line[index].isEmpty()) {
						throw new IllegalArgumentException("Missing information.");
					}
				}

				blockLot = line[6].split("-");

				for (int j = 0; j < indicesToCheck.length; j++) {
					pstmt.setString(j + 1, line[indicesToCheck[j]]);
				}
				if (!line[5].isEmpty()) {
					pstmt.setInt(7, Integer.parseInt(line[5]));
				} else {
					pstmt.setInt(7, 0);
				}

				try {
					pstmt.setInt(8, Integer.parseInt(blockLot[0]));
					pstmt.setInt(9, Integer.parseInt(blockLot[1]));
				} catch (NumberFormatException e) {
					pstmt.setInt(8, 0);
					pstmt.setInt(9, 0);
				}

				try {
					pstmt.setDouble(10, Double.parseDouble(line[10]));
					pstmt.setDouble(11, Double.parseDouble(line[11]));
				} catch (NumberFormatException e) {
					pstmt.setDouble(10, 0);
					pstmt.setDouble(11, 0);
				}

				pstmt.addBatch();
			} catch (IllegalArgumentException e) {
				malformed++;
				e.printStackTrace();
			}
		}
		System.out
			.println(
				String.format("%d entries were malformed and therefore discarded. ", malformed));
		if (rawData.size() - 1 == malformed) {
			throw new IllegalArgumentException("No entries were read.");
		}
		return pstmt;
	}

	/**
	 * Imports a csv file to the associated SQLite database.
	 *
	 * @param csvName The path to and name of the csv file to be read
	 * @param internal Whether the CSV file is internal and should be fetched with getResource
	 * @param csvType The type of CSV data (BIKETRIP, RETAILER, or HOTSPOT are valid)
	 * @return Whether the operation completed successfully
	 * @throws IOException If the CSV file could not be read
	 * @throws NullPointerException If the file does not exist
	 */
	public boolean importCSV(String csvName, boolean internal, DataType csvType)
		throws IOException, NullPointerException {

		//Read the csv file
		double operationStartTime = System.currentTimeMillis();
		CSVReader reader;
		if (internal) {
			reader = new CSVReader(
				new InputStreamReader(SQLHandler.class.getResourceAsStream(csvName)));
		} else {
			reader = new CSVReader(new FileReader(csvName));
		}
		List<String[]> csvData = reader.readAll();
		reader.close();
		System.out.print(String.format("%d lines read. ", csvData.size() - 1));

		System.out.println(String.format("CSV read time: %.3fs",
			(System.currentTimeMillis() - operationStartTime) / 1000));
		operationStartTime = System.currentTimeMillis();

		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean importSuccessful = true;

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				conn.setAutoCommit(false);
				switch (csvType) {
					case BIKETRIP:
						pstmt = prepareNewBikeTrips(csvData, conn);
						break;
					case HOTSPOT:
						pstmt = prepareNewHotSpots(csvData, conn);
						break;
					case RETAILER:
						pstmt = prepareNewRetailers(csvData, conn);
						break;
					default:
						throw new IllegalArgumentException("Not a valid CSV import type.");
				}
				System.out.println(String
					.format("Error checking and statement preparing time: %.3fs",
						(System.currentTimeMillis() - operationStartTime) / 1000));
				operationStartTime = System.currentTimeMillis();
				pstmt.executeBatch();
				conn.commit();
				System.out.println(String.format("SQL write time: %.3fs",
					(System.currentTimeMillis() - operationStartTime) / 1000));

				if (csvType == DataType.RETAILER) {
					threadedGeocode(csvType);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				importSuccessful = false;
			} catch (IllegalArgumentException e) {
				System.out.println("No entries were read. Prompting GUI error.");
				importSuccessful = false;
			} finally {
				DbUtils.closeQuietly(pstmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return importSuccessful;
	}

	/**
	 * Builds a query for a specified datatype, with optionally specified where and order by
	 * clauses, and optional use of the internally saved query coordinate/radius values to execute
	 * an area-based query.
	 *
	 * @param table The table to be queried
	 * @param where The where clause, if it is null then no where clause will be added
	 * @return The built query
	 */
	private String buildQuery(DataType table, String where) {
		String query = "SELECT * FROM " + table;
		if (!(where == null)) {
			query += " WHERE " + where;
		}
		return query;
	}

	/**
	 * Creates an SQL query of the form: SELECT * FROM BikeTrips WHERE where
	 *
	 * @param where Parameter used in the query's WHERE clause - clause omitted if parameter is
	 * null
	 * @return The bike trips found by the query in an ArrayList
	 */
	public ArrayList<BikeTrip> queryBikeTripData(String where) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<BikeTrip> queriedTrips = new ArrayList<>();

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(buildQuery(DataType.BIKETRIP, where));
				LocalDateTime startTime, endTime;
				ArrayList<Integer> ratings;
				ArrayList<double[]> otherCoords;
				String[] raw, rawCoord;
				double[] newCoord;

				while (rs.next()) {
					startTime = LocalDateTime.parse(rs.getString("startTime"));
					endTime = LocalDateTime.parse(rs.getString("endTime"));

					ratings = new ArrayList<>();
					String rawRatings = rs.getString("ratings");
					if (rawRatings != null && !rawRatings.isEmpty()) {
						raw = rs.getString("ratings").split(",");
						for (String rating : raw) {
							ratings.add(Integer.parseInt(rating));
						}
					}

					otherCoords = new ArrayList<>();
					String rawOtherCoords = rs.getString("otherCoords");
					if (!rawOtherCoords.isEmpty()) {
						raw = rawOtherCoords.split(",");
						for (String rawCombinedCoord : raw) {
							rawCoord = rawCombinedCoord.split(" ");
							newCoord = new double[2];
							newCoord[0] = Double.parseDouble(rawCoord[0]);
							newCoord[1] = Double.parseDouble(rawCoord[1]);
							otherCoords.add(newCoord);
						}
					}

					queriedTrips.add(new BikeTrip(
						rs.getInt("bikeId"),
						rs.getInt("ID"),
						rs.getDouble("startLatitude"),
						rs.getDouble("startLongitude"),
						rs.getDouble("endLatitude"),
						rs.getDouble("endLongitude"),
						otherCoords,
						startTime,
						endTime,
						rs.getLong("duration"),
						rs.getString("gender"),
						rs.getDouble("distance"),
						rs.getDouble("avgSpeed"),
						rs.getDouble("caloriesBurnt"),
						rs.getDouble("avgRating"),
						ratings,
						rs.getInt("favourite") == 1));
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return queriedTrips;
	}

	/**
	 * Creates an SQL query of the form: SELECT * FROM HotSpots WHERE where
	 *
	 * @param where Parameter used in the query's WHERE clause - clause omitted if parameter is
	 * null
	 * @return The hot spots found by the query in an ArrayList
	 */
	public ArrayList<HotSpot> queryHotSpotData(String where) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<HotSpot> queriedHotSpots = new ArrayList<>();

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(buildQuery(DataType.HOTSPOT, where));

				while (rs.next()) {
					queriedHotSpots.add(new HotSpot(
						rs.getString("name"),
						rs.getString("city"),
						rs.getInt("ID"),
						rs.getDouble("latitude"),
						rs.getDouble("longitude"),
						rs.getString("borough"),
						rs.getString("locationInfo"),
						rs.getString("locationType"),
						rs.getString("policy"),
						rs.getString("policyDescription"),
						rs.getString("provider"),
						rs.getInt("favourite") == 1));
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return queriedHotSpots;
	}

	/**
	 * Creates an SQL query of the form: SELECT * FROM Retailers WHERE where
	 *
	 * @param where Parameter used in the query's WHERE clause - clause omitted if parameter is
	 * null
	 * @return The retailers found by the query in an ArrayList
	 */
	public ArrayList<Retailer> queryRetailerData(String where) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<Retailer> queriedRetailers = new ArrayList<>();

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(buildQuery(DataType.RETAILER, where));

				while (rs.next()) {
					queriedRetailers.add(new Retailer(
						rs.getString("name"),
						rs.getString("city"),
						rs.getInt("ID"),
						rs.getDouble("latitude"),
						rs.getDouble("longitude"),
						rs.getString("street"),
						rs.getString("state"),
						rs.getInt("zipCode"),
						rs.getString("primaryType"),
						rs.getString("secondaryType"),
						rs.getInt("block"),
						rs.getInt("lot"),
						rs.getInt("favourite") == 1));
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return queriedRetailers;
	}

	/**
	 * Creates an SQL query of the form: SELECT * FROM Locations WHERE where
	 *
	 * @param where Parameter used in the query's WHERE clause - clause omitted if parameter is
	 * null
	 * @return The locations found by the query in an ArrayList
	 */
	public ArrayList<Location> queryLocationData(String where) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<Location> queriedLocations = new ArrayList<>();

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(buildQuery(DataType.LOCATION, where));

				while (rs.next()) {
					queriedLocations.add(new Location(
						rs.getString("name"),
						rs.getString("city"),
						rs.getInt("ID"),
						rs.getDouble("latitude"),
						rs.getDouble("longitude"),
						rs.getInt("favourite") == 1));
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return queriedLocations;
	}

	/**
	 * Adds a new bike trip to the associated SQLite database based on basic input by querying the
	 * google maps geocoder to get exact latitude and longitude coordinates. Returns the new bike
	 * trip if no error occurred.
	 *
	 * @param bikeId The id of the bike used
	 * @param startAddress The start address of the trip as text
	 * @param endAddress The end address of the trip as text
	 * @param city The city the bike trip was in
	 * @param startTime The start time of the bike trip
	 * @param endTime The end time of the bike trip
	 * @param gender The gender of the person who went on this bike trip
	 * @param rating The rating this user gave the bike trip (not yet implemented)
	 * @return A new BikeTrip object based on the information given if the operation was successful
	 * (the given information could be converted to a new bike trip), or null if an error occurred.
	 */
	public BikeTrip addBikeTrip(int bikeId, String startAddress, String endAddress, String city,
		LocalDateTime startTime, LocalDateTime endTime, String gender, int rating) {
		Connection conn = null;
		Statement stmt = null;
		BikeTrip newBikeTrip = null;

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				stmt = conn.createStatement();

				double[] startLatLong = Geocoder
					.getLatitudeLongitude(String.format("%s, %s", startAddress, city));
				double[] endLatLong = Geocoder
					.getLatitudeLongitude(String.format("%s, %s", endAddress, city));
				String strRating = String.valueOf(rating);
				if (rating == 0) {
					strRating = "";
				}
				stmt.execute(String.format(
					"INSERT OR IGNORE INTO BikeTrips(bikeID, startLatitude, startLongitude, " +
						"endLatitude, endLongitude, otherCoords, startTime, endTime, duration, "
						+ "gender, ratings, avgRating, startX, startY, endX, endY) " +
						"VALUES(%d, %f, %f, %f, %f, '', '%s', '%s', %d, "
						+ "'%s', '%s', %d, %f, %f, %f, %f);",
					bikeId, startLatLong[0], startLatLong[1], endLatLong[0], endLatLong[1],
					startTime, endTime, ChronoUnit.MINUTES.between(startTime, endTime), gender,
					strRating, rating,
					convertToX(startLatLong[0]), convertToY(startLatLong[0], startLatLong[1]),
					convertToX(endLatLong[0]), convertToY(endLatLong[0], endLatLong[1])));

				newBikeTrip = queryBikeTripData(String
					.format(
						"(bikeID LIKE %d) " +
							"AND (startLatitude LIKE %f) AND (startLongitude LIKE %f) " +
							"AND (endLatitude LIKE %f) AND (endLongitude LIKE %f) " +
							"AND (startTime LIKE '%s') AND (endTime LIKE '%s')",
						bikeId, startLatLong[0], startLatLong[1], endLatLong[0], endLatLong[1],
						startTime, endTime)).get(0);

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return newBikeTrip;
	}

	/**
	 * Adds a new hot spot to the associated SQLite database based on basic input by querying the
	 * google maps geocoder to get exact latitude and longitude coordinates. Returns the new hot
	 * spot if no error occurred.
	 *
	 * @param name The name (SSID) of the hot spot
	 * @param city The city the hot spot is in
	 * @param borough The borough the hot spot is in
	 * @param locationInfo Information about the location that could be used to find it using the
	 * google maps geocoder
	 * @param locationType The type of location (eg. outdoor)
	 * @param policy The policy used for this hot spot (eg. Free)
	 * @param policyDescription A more detailed description of the policy
	 * @param provider The provider of this hot spot
	 * @return A new HotSpot object based on the information given if the operation was successful
	 * (the given information could be converted to a new hot spot), or null if an error occurred.
	 */
	public HotSpot addHotSpot(String name, String city, String borough, String locationInfo,
		String locationType, String policy, String policyDescription, String provider) {
		Connection conn = null;
		Statement stmt = null;
		HotSpot newHotSpot = null;

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				stmt = conn.createStatement();

				double[] latLong = Geocoder
					.getLatitudeLongitude(
						String.format("%s, %s, %s", locationInfo, borough, city));

				stmt.execute(String.format(
					"INSERT OR IGNORE INTO HotSpots(name, city, borough, locationType, provider, " +
						"policy, policyDescription, locationInfo, latitude, longitude, x, y) " +
						"VALUES('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %f, %f, %f, %f);",
					name, city, borough, locationType, provider, policy, policyDescription,
					locationInfo,
					latLong[0], latLong[1],
					convertToX(latLong[0]), convertToY(latLong[0], latLong[1])));

				newHotSpot = queryHotSpotData(
					String.format("(name LIKE '%s') AND (city LIKE '%s') AND (latitude LIKE %f) " +
							"AND (longitude LIKE %f) AND (borough LIKE '%s') " +
							"AND (locationInfo LIKE '%s') AND (locationType LIKE '%s') " +
							"AND (policy LIKE '%s') AND (policyDescription LIKE '%s') " +
							"AND (provider LIKE '%s')",
						name, city, latLong[0], latLong[1], borough, locationInfo, locationType,
						policy, policyDescription, provider)).get(0);

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}

		return newHotSpot;
	}

	/**
	 * Adds a new retailer to the associated SQLite database based on basic input by querying the
	 * google maps geocoder to get exact latitude and longitude coordinates. Returns the new
	 * retailer if no error occurred.
	 *
	 * @param name The name of the retailer
	 * @param city The city the retailer is in
	 * @param street The street address of the retailer
	 * @param state The state the retailer is in
	 * @param zipCode The zip code of the area the retailer is in
	 * @param primaryType The general type of retailer
	 * @param secondaryType A more specific type specification for this retailer
	 * @param block The block this retailer is in
	 * @param lot The lot this retailer is in
	 * @return A new Retailer object based on the information given if the operation was successful
	 * (the given information could be converted to a new retailer), or null if an error occurred.
	 */
	public Retailer addRetailer(String name, String city, String street, String state,
		int zipCode, String primaryType, String secondaryType, int block, int lot) {
		Connection conn = null;
		Statement stmt = null;
		Retailer newRetailer = null;

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				stmt = conn.createStatement();

				double[] latLong = Geocoder
					.getLatitudeLongitude(
						String.format("%s, %s, %s, %s", name, street, city, state));

				stmt.execute(String.format(
					"INSERT OR IGNORE INTO Retailers(name, city, street, state, primaryType, " +
						"secondaryType, zipCode, block, lot, latitude, longitude, x, y) " +
						"VALUES('%s', '%s', '%s', '%s', '%s', '%s', %d, %d, %d, %f, %f, %f, %f);",
					name, city, street, state, primaryType, secondaryType, zipCode, block, lot,
					latLong[0],
					latLong[1],
					convertToX(latLong[0]), convertToY(latLong[0], latLong[1])));

				newRetailer = queryRetailerData(
					String.format("(name LIKE '%s') AND (city LIKE '%s') AND (street LIKE '%s') " +
							"AND (state LIKE '%s') AND (primaryType LIKE '%s') " +
							"AND (secondaryType LIKE '%s')", name, city, street, state,
						primaryType, secondaryType)).get(0);

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return newRetailer;
	}

	/**
	 * Adds a new location to the associated SQLite database based on basic input by querying the
	 * google maps geocoder to get exact latitude and longitude coordinates. Returns the new
	 * location if no error occurred.
	 *
	 * @param name The name of the location
	 * @param city The city the location is in
	 * @return A new Location object based on the information given if the operation was successful
	 * (the given information could be converted to a new location), or null if an error occurred.
	 */
	public Location addLocation(String name, String city) {
		Connection conn = null;
		Statement stmt = null;
		Location newLocation = null;

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				stmt = conn.createStatement();

				double[] latLong = Geocoder
					.getLatitudeLongitude(
						String.format("%s, %s", name, city));

				stmt.execute(String.format(
					"INSERT OR IGNORE INTO Locations(name, city, latitude, longitude, x, y) " +
						"VALUES('%s', '%s', %f, %f, %f, %f);",
					name, city, latLong[0], latLong[1],
					convertToX(latLong[0]), convertToY(latLong[0], latLong[1])));

				newLocation = queryLocationData(
					String.format("(name LIKE '%s') AND (city LIKE '%s')", name, city)).get(0);

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return newLocation;
	}

	/**
	 * Executes an update to the associated SQLite database for a specified data type, for an object
	 * with a given primary key id.
	 *
	 * @param dataType The table the entry to be update is in
	 * @param set The content of the SET clause in this update
	 * @param id The primary key id of the object to be updated
	 * @return Whether the update was successful
	 */
	public boolean updateEntry(DataType dataType, String set, int id) {
		Connection conn = null;
		Statement stmt = null;
		boolean updateSuccessful = true;

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				stmt = conn.createStatement();
				stmt.execute(String.format("UPDATE %s SET %s WHERE ID = '%d'", dataType, set, id));
			} catch (SQLException e) {
				e.printStackTrace();
				updateSuccessful = false;
			} finally {
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return updateSuccessful;
	}

	/**
	 * Updates the ratings of a bike trip in the database.
	 *
	 * @param bikeTrip The trip to update
	 * @return Whether the update was successful
	 */
	public boolean updateRatings(BikeTrip bikeTrip) {
		String ratings = "";
		if (bikeTrip.getRatings().size() > 0) {
			ratings += bikeTrip.getRatings().get(0);
			for (int i = 1; i < bikeTrip.getRatings().size(); i++) {
				ratings += "," + bikeTrip.getRatings().get(i);
			}
		}
		return updateEntry(DataType.BIKETRIP,
			String.format("ratings = '%s', avgRating = %f", ratings, bikeTrip.getAvgRating()),
			bikeTrip.getId());
	}

	/**
	 * Updates the otherCoords of a bike trip in the database.
	 *
	 * @param bikeTrip The trip to update
	 * @return Whether the update was successful
	 */
	public boolean updateOtherCoords(BikeTrip bikeTrip) {
		String otherCoords = "";
		if (bikeTrip.getOtherCoords().size() > 0) {
			otherCoords += String.format("%f %f", bikeTrip.getOtherCoords().get(0)[0],
				bikeTrip.getOtherCoords().get(0)[1]);
			for (int i = 1; i < bikeTrip.getOtherCoords().size(); i++) {
				otherCoords += String.format(",%f %f", bikeTrip.getOtherCoords().get(i)[0],
					bikeTrip.getOtherCoords().get(i)[1]);
			}
		}
		return updateEntry(DataType.BIKETRIP,
			String.format("otherCoords = '%s'", otherCoords),
			bikeTrip.getId());
	}

	/**
	 * Remove an entry from the associated SQLite database based on a given data type, and primary
	 * key id.
	 *
	 * @param dataType The type of the entry to be deleted
	 * @param id The primary key id of the entry to be deleted
	 * @return Whether the entry was successfully deleted
	 */
	public boolean removeEntry(DataType dataType, int id) {
		Connection conn = null;
		Statement stmt = null;
		boolean removeSuccessful = true;

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				stmt = conn.createStatement();
				stmt.execute(String.format("DELETE FROM %s WHERE ID = %d", dataType, id));
			} catch (SQLException e) {
				e.printStackTrace();
				removeSuccessful = false;
			} finally {
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return removeSuccessful;
	}

	/**
	 * Set whether an entry is favourite.
	 *
	 * @param dataType The data type of the entry
	 * @param favourite Whether this entry should be set as favourite
	 * @param id The primary key id of the entry
	 * @return Whether the operation completed successfully
	 */
	public boolean setFavourite(DataType dataType, boolean favourite, int id) {
		Connection conn = null;
		Statement stmt = null;
		boolean favSuccessful = true;
		int intFavourite = favourite ? 1 : 0;

		synchronized (this) {
			try {
				conn = getConnection(path, config);
				stmt = conn.createStatement();
				stmt.execute(String.format("UPDATE %s SET favourite = %d WHERE ID = %d",
					dataType, intFavourite, id));
			} catch (SQLException e) {
				e.printStackTrace();
				favSuccessful = false;
			} finally {
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}
		}
		return favSuccessful;
	}

	/**
	 * Finds the nearest hot spot to a given retailer. Returns null if no nearest hot spot was found
	 * because either there were no hot spots in the database, or none within 5km of the given
	 * retailer exist.
	 *
	 * @param retailer The retailer to find a hot spot near
	 * @return The nearest hot spot
	 */
	public HotSpot findNearestHotSpotToRetailer(Retailer retailer) {
		double x = convertToX(retailer.getLatitude());
		double y = convertToY(retailer.getLatitude(), retailer.getLongitude());
		double querySize = 0.1;

		ArrayList<HotSpot> queriedHotSpots = new ArrayList<>();
		//Find a bounding box that contains at least 1 hot spot
		while (queriedHotSpots.size() == 0 && querySize < 5) {
			queriedHotSpots = queryHotSpotData(
				String.format("(x BETWEEN %.5f AND %.5f) AND (y BETWEEN %.5f and %.5f)",
					x - querySize, x + querySize, y - querySize, y + querySize));
			querySize += 0.1;
		}
		if (queriedHotSpots.size() == 0) {
			return null;
		}
		//Expand the bounding box to include all hot spots that are in a circle that minimally
		//contains the previous bounding box
		querySize = getDistanceBetween(0, 0, querySize, querySize);
		queriedHotSpots = queryHotSpotData(
			String.format("(x BETWEEN %.5f AND %.5f) AND (y BETWEEN %.5f and %.5f)",
				x - querySize, x + querySize, y - querySize, y + querySize));

		//Find the closest hot spot in that bounding box
		HotSpot closest = queriedHotSpots.get(0);
		double smallestDistance = Double.MAX_VALUE;
		double distance;
		for (HotSpot hotSpot : queriedHotSpots) {
			distance = getDistanceBetween(x, y, convertToX(hotSpot.getLatitude()),
				convertToY(hotSpot.getLatitude(), hotSpot.getLongitude()));
			if (distance <= smallestDistance) {
				closest = hotSpot;
				smallestDistance = distance;
			}
		}
		return closest;
	}

	/**
	 * Finds the bounding box of the start and end coordinates of a bike trip, and returns that area
	 * with a specified amount of extra kilometres added on each edge.
	 *
	 * @param bikeTrip The bike trip to find a bounding box of
	 * @param extraKms The amount of extra kilometres to be added to each edge of the bounding box
	 * @return The bounding box in the form {min x, max x, min y, max y} in kilometres
	 */
	private double[] getTripBoundingBox(BikeTrip bikeTrip, double extraKms) {
		double[] allX = new double[bikeTrip.getOtherCoords().size() + 2];
		double[] allY = new double[bikeTrip.getOtherCoords().size() + 2];
		double[] box = new double[4];
		box[0] = Double.MAX_VALUE;
		box[1] = -Double.MAX_VALUE;
		box[2] = Double.MAX_VALUE;
		box[3] = -Double.MAX_VALUE;

		allX[0] = convertToX(bikeTrip.getStartLatitude());
		allX[1] = convertToX(bikeTrip.getEndLatitude());
		allY[0] = convertToY(bikeTrip.getStartLatitude(), bikeTrip.getStartLongitude());
		allY[1] = convertToY(bikeTrip.getEndLatitude(), bikeTrip.getEndLongitude());

		int i = 2;
		for (double[] coord : bikeTrip.getOtherCoords()) {
			allX[i] = convertToX(coord[0]);
			allY[i] = convertToY(coord[0], coord[1]);
			i++;
		}

		for (double x : allX) {
			if (x < box[0]) {
				box[0] = x;
			}
			if (x > box[1]) {
				box[1] = x;
			}
		}

		for (double y : allY) {
			if (y < box[2]) {
				box[2] = y;
			}
			if (y > box[3]) {
				box[3] = y;
			}
		}

		box[0] -= extraKms;
		box[1] += extraKms;
		box[2] -= extraKms;
		box[3] += extraKms;
		return box;
	}

	/**
	 * Query the associated SQLite database to find bike trips near a specified bike trip. Nearby
	 * bike trips are bike trips that have a start and end location within a given radius of the
	 * start and end location of the specified bike trip respectively.
	 *
	 * @param bikeTrip The bike trip to find bike trips nearby
	 * @param withinDistance The radius the start and end locations must be within
	 * @return An ArrayList containing the nearby bike trips
	 */
	public ArrayList<BikeTrip> findBikeTripsNearBikeTrip(BikeTrip bikeTrip, double withinDistance) {
		double startX = convertToX(bikeTrip.getStartLatitude());
		double startY = convertToY(bikeTrip.getStartLatitude(), bikeTrip.getStartLongitude());
		double endX = convertToX(bikeTrip.getEndLatitude());
		double endY = convertToY(bikeTrip.getEndLatitude(), bikeTrip.getEndLongitude());
		ArrayList<BikeTrip> nearby = queryBikeTripData(String.format(
			"(startX BETWEEN %.5f AND %.5f) AND (startY BETWEEN %.5f and %.5f) AND " +
				"(endX BETWEEN %.5f AND %.5f) AND (endY BETWEEN %.5f and %.5f)",
			startX - withinDistance, startX + withinDistance,
			startY - withinDistance, startY + withinDistance,
			endX - withinDistance, endX + withinDistance,
			endY - withinDistance, endY + withinDistance));
		for (BikeTrip nearBikeTrip : nearby) {
			if (getDistanceBetween(startX, startY, convertToX(nearBikeTrip.getStartLatitude()),
				convertToY(nearBikeTrip.getStartLatitude(), nearBikeTrip.getStartLongitude()))
				> withinDistance
				||
				getDistanceBetween(endX, endY, convertToX(nearBikeTrip.getEndLatitude()),
					convertToY(nearBikeTrip.getEndLatitude(), nearBikeTrip.getEndLongitude()))
					> withinDistance) {
				nearby.remove(nearBikeTrip);
			}
		}
		return nearby;
	}

	/**
	 * Query the associated SQLite database to find hot spots near a specified bike trip. At the
	 * moment only uses a bounding box extended by withinDistance kilometres in each direction. Will
	 * be switched to a better search in the future
	 *
	 * @param bikeTrip The bike trip to find hot spots nearby
	 * @param withinDistance The amount to extend each edge of the bounding box found
	 * @return An ArrayList containing the nearby hot spots
	 */
	public ArrayList<HotSpot> findHotSpotsNearTrip(BikeTrip bikeTrip, double withinDistance) {
		double[] boundingBox = getTripBoundingBox(bikeTrip, withinDistance);
		return queryHotSpotData(String.format(
			"(x BETWEEN %.5f AND %.5f) AND (y BETWEEN %.5f and %.5f)", boundingBox[0],
			boundingBox[1], boundingBox[2], boundingBox[3]));
	}

	/**
	 * Query the associated SQLite database to find retailers near a specified bike trip. At the
	 * moment only uses a bounding box extended by withinDistance kilometres in each direction. Will
	 * be switched to a better search in the future
	 *
	 * @param bikeTrip The bike trip to find hot spots nearby
	 * @param withinDistance The amount to extend each edge of the bounding box found
	 * @return An ArrayList containing the nearby retailers
	 */
	public ArrayList<Retailer> findRetailersNearTrip(BikeTrip bikeTrip, double withinDistance) {
		double[] boundingBox = getTripBoundingBox(bikeTrip, withinDistance);
		return queryRetailerData(
			String.format("(x BETWEEN %.5f AND %.5f) AND (y BETWEEN %.5f and %.5f)", boundingBox[0],
				boundingBox[1], boundingBox[2], boundingBox[3]));
	}

	/**
	 * Query the associated SQLite database to find locations near a specified bike trip. At the
	 * moment only uses a bounding box extended by withinDistance kilometres in each direction. Will
	 * be switched to a better search in the future
	 *
	 * @param bikeTrip The bike trip to find hot spots nearby
	 * @param withinDistance The amount to extend each edge of the bounding box found
	 * @return An ArrayList containing the nearby locations
	 */
	public ArrayList<Location> findLocationsNearTrip(BikeTrip bikeTrip, double withinDistance) {
		double[] boundingBox = getTripBoundingBox(bikeTrip, withinDistance);
		return queryLocationData(
			String.format("(x BETWEEN %.5f AND %.5f) AND (y BETWEEN %.5f and %.5f)",
				boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3]));
	}

	/**
	 * Finds the distance between two points.
	 *
	 * @param x1 The x value of point 1
	 * @param y1 The y value of point 1
	 * @param x2 The x value of point 2
	 * @param y2 The y value of point 2
	 * @return The distance between the two points
	 */
	public double getDistanceBetween(double x1, double y1, double x2, double y2) {
		return Math.pow(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2), 0.5);
	}

	/**
	 * Converts a latitude value to an x value in kilometres.
	 *
	 * @param latitude The latitude to convert from
	 * @return The x value in kilometres
	 */
	public static double convertToX(double latitude) {
		return latitude * 111;
	}

	/**
	 * Converts a latitude/longitude pair to a y value in kilometres.
	 *
	 * @param latitude The latitude to convert from
	 * @param longitude The longitude to convert from
	 * @return The y value in kilometres
	 */
	public static double convertToY(double latitude, double longitude) {
		return Math.cos(Math.toRadians(latitude)) * 111 * longitude;
	}

}
