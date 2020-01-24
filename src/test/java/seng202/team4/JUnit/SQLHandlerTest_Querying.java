package seng202.team4.JUnit;

import org.junit.*;
import seng202.team4.Model.BikeTrip;
import seng202.team4.Model.DataType;
import seng202.team4.Model.HotSpot;
import seng202.team4.Model.Location;
import seng202.team4.Model.Main;
import seng202.team4.Model.Retailer;
import seng202.team4.Services.SQLHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class SQLHandlerTest_Querying {

	private static String jarDir;
	private SQLHandler sqlHandler;

	@BeforeClass
	public static void setUp() {
		try {
			jarDir = Paths
				.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI())
				.getParent().toString();
		} catch (URISyntaxException e) {
			fail("Cannot access protection domain.");
		}
	}

	@Before
	public void testSetUp() {
		sqlHandler = new SQLHandler(jarDir, "testDb");
		sqlHandler.createDatabase();
	}

	@After
	public void tearDown() {
		if (!sqlHandler.deleteDatabase()) {
			fail();
		}
	}

	//Basic query tests

	@Test
	public void testQueryBikeTrips() {
		try {
			sqlHandler.importCSV("/TestData/20BikeTrips.csv", true, DataType.BIKETRIP);
			ArrayList<BikeTrip> queriedTrips = sqlHandler
				.queryBikeTripData("Duration BETWEEN 13 and 25");
			assertEquals(5, queriedTrips.size());
			for (BikeTrip bikeTrip : queriedTrips) {
				assertTrue(bikeTrip.getDuration() <= 25 && bikeTrip.getDuration() >= 13);
			}
		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void testQueryHotSpots() {
		try {
			sqlHandler.importCSV("/TestData/20HotSpots.csv", true, DataType.HOTSPOT);
			ArrayList<HotSpot> queriedHotSpots = sqlHandler
				.queryHotSpotData("LOWER(provider) LIKE '%nypl%'");
			assertEquals(8, queriedHotSpots.size());
			for (HotSpot hotSpot : queriedHotSpots) {
				assertTrue(hotSpot.getProvider().toUpperCase().contains("NYPL"));
			}
		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void testQueryRetailers() {
		try {
			sqlHandler.importCSV("/TestData/20Retailers.csv", true, DataType.RETAILER);
			while (Main.getTotalGeocodesToDo() != 0) {
				Thread.sleep(100);
			}
			ArrayList<Retailer> queriedRetailers = sqlHandler
				.queryRetailerData("LOWER(primaryType) LIKE '%shopping%'");
			assertEquals(3, queriedRetailers.size());
			for (Retailer retailer : queriedRetailers) {
				assertTrue(retailer.getPrimaryType().toLowerCase().contains("shopping"));
			}
		} catch (IOException | InterruptedException e) {
			fail();
		}
	}

	@Test
	public void testQueryLocations() {
		sqlHandler.addLocation("Riccarton Mall", "Christchurch");
		sqlHandler.addLocation("Burnside High School", "Christchurch");

		ArrayList<Location> queriedLocations = sqlHandler
			.queryLocationData("LOWER(city) LIKE '%hristchu%'");
		assertEquals(2, queriedLocations.size());
		for (Location location : queriedLocations) {
			assertTrue(location.getCity().toLowerCase().contains("hristchu"));
		}

		queriedLocations = sqlHandler.queryLocationData("LOWER(name) LIKE '%school%'");
		assertEquals(1, queriedLocations.size());
		for (Location location : queriedLocations) {
			assertTrue(location.getName().toLowerCase().contains("school"));
		}
	}

	//Near BikeTrip queries

	@Test
	public void testBikeTripsNearTrip() {
		try {
			sqlHandler.importCSV("/TestData/20BikeTrips.csv", true, DataType.BIKETRIP);
			LocalDateTime startTime = LocalDateTime.parse("2007-12-03T10:16:30");
			LocalDateTime endTime = LocalDateTime.parse("2007-12-03T10:17:30");
			BikeTrip toFindNearby = new BikeTrip(0, 0, 40.732, -74.000,
				40.715, -74.013, new ArrayList<>(), startTime, endTime, 0, "F", 0, 0,
				0, 0, new ArrayList<>(), false);
			ArrayList<BikeTrip> nearby = sqlHandler.findBikeTripsNearBikeTrip(toFindNearby, 2);
			assertEquals(2, nearby.size());
		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void testHotSpotsNearTrip() {
		try {
			sqlHandler.importCSV("/TestData/20HotSpots.csv", true, DataType.HOTSPOT);
			LocalDateTime startTime = LocalDateTime.parse("2007-12-03T10:16:30");
			LocalDateTime endTime = LocalDateTime.parse("2007-12-03T10:17:30");
			BikeTrip toFindNearby = new BikeTrip(0, 0, 40.686907, -73.824830,
				40.849494, -73.917578, new ArrayList<>(), startTime, endTime, 0, "F", 0, 0,
				0, 0, new ArrayList<>(), false);

			ArrayList<HotSpot> nearby = sqlHandler.findHotSpotsNearTrip(toFindNearby, 0.5);
			assertEquals(6, nearby.size());
		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void testRetailersNearTrip() {
		try {
			sqlHandler.importCSV("/TestData/20Retailers.csv", true, DataType.RETAILER);
			while (Main.getTotalGeocodesToDo() != 0) {
				Thread.sleep(100);
			}
			LocalDateTime startTime = LocalDateTime.parse("2007-12-03T10:16:30");
			LocalDateTime endTime = LocalDateTime.parse("2007-12-03T10:17:30");
			BikeTrip toFindNearby = new BikeTrip(0, 0, 40.705035, -74.009140,
				40.705035, -74.009140, new ArrayList<>(), startTime, endTime, 0, "F", 0, 0,
				0, 0, new ArrayList<>(), false);

			ArrayList<Retailer> nearby = sqlHandler.findRetailersNearTrip(toFindNearby, 0.1);
			assertEquals(8, nearby.size());
		} catch (IOException | InterruptedException e) {
			fail();
		}
	}

	@Test
	public void testLocationsNearTrip() {
		sqlHandler.addLocation("Riccarton Mall", "Christchurch");
		sqlHandler.addLocation("Burnside High School", "Christchurch");
		LocalDateTime startTime = LocalDateTime.parse("2007-12-03T10:16:30");
		LocalDateTime endTime = LocalDateTime.parse("2007-12-03T10:17:30");
		BikeTrip toFindNearby = new BikeTrip(0, 0, -43.530963, 172.597610,
			-43.530963, 172.597610, new ArrayList<>(), startTime, endTime, 0, "F", 0, 0,
			0, 0, new ArrayList<>(), false);

		ArrayList<Location> nearby = sqlHandler.findLocationsNearTrip(toFindNearby, 1);
		assertEquals(1, nearby.size());
		nearby = sqlHandler.findLocationsNearTrip(toFindNearby, 5);
		assertEquals(2, nearby.size());
	}

	//Nearest hotspot to retailer test

	@Test
	public void testNearestHotSpotToRetailer() {
		Retailer toTest = sqlHandler.addRetailer("Pizza Pizza NYC", "New York",
			"77 Pearl Street", "NY", 10004, "Casual Eating & Takeout",
			"F-Pizza", 29, 22);
		double retailerX = SQLHandler.convertToX(toTest.getLatitude());
		double retailerY = SQLHandler.convertToY(toTest.getLatitude(), toTest.getLongitude());
		//Check that it returns null if no hotspots are found
		assertEquals(sqlHandler.findNearestHotSpotToRetailer(toTest), null);

		try {
			sqlHandler.importCSV("/TestData/20HotSpots.csv", true, DataType.HOTSPOT);

			double minDistance = Double.MAX_VALUE;
			HotSpot closest = null;
			for (HotSpot hotSpot : sqlHandler.queryHotSpotData(null)) {
				double x = SQLHandler.convertToX(hotSpot.getLatitude());
				double y = SQLHandler.convertToY(hotSpot.getLatitude(), hotSpot.getLongitude());
				if (sqlHandler.getDistanceBetween(x, y, retailerX, retailerY) < minDistance) {
					minDistance = sqlHandler.getDistanceBetween(x, y, retailerX, retailerY);
					closest = hotSpot;
				}
			}

			assertEquals(sqlHandler.findNearestHotSpotToRetailer(toTest).getLocationInfo(),
				closest.getLocationInfo());
		} catch (IOException | NullPointerException e) {
			fail();
		}
	}

	@Test
	public void testDistanceGetter() {
		assertEquals(5, sqlHandler.getDistanceBetween(0, 0, 3, 4), 0);
		assertEquals(13, sqlHandler.getDistanceBetween(0, 0, 5, 12), 0);
	}

	@Test
	public void testXYConversion() {
		assertEquals(15540, SQLHandler.convertToX(140), 0.5);
		assertEquals(2551, SQLHandler.convertToY(140, -30), 0.5);
	}
}