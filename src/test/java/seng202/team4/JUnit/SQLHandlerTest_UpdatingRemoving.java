package seng202.team4.JUnit;

import org.junit.*;
import seng202.team4.Model.BikeTrip;
import seng202.team4.Model.DataType;
import seng202.team4.Model.HotSpot;
import seng202.team4.Model.Main;
import seng202.team4.Services.SQLHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class SQLHandlerTest_UpdatingRemoving {

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

	@Test
	public void testUpdateEntry() {
		try {
			sqlHandler.importCSV("/TestData/20HotSpots.csv", true, DataType.HOTSPOT);
			HotSpot beforeEdit = sqlHandler.queryHotSpotData(null).get(0);
			sqlHandler.updateEntry(DataType.HOTSPOT,
				"locationInfo = 'blah', locationType = 'blahblah', latitude = 123.456",
				beforeEdit.getId());
			HotSpot afterEdit = sqlHandler.queryHotSpotData(null).get(0);
			assertEquals(beforeEdit.getName(), afterEdit.getName());
			assertEquals(beforeEdit.getCity(), afterEdit.getCity());
			assertEquals("blah", afterEdit.getLocationInfo());
			assertEquals("blahblah", afterEdit.getLocationType());
			assertEquals(123.456, afterEdit.getLatitude(), 0.001);
		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void testUpdateRatings() {
		LocalDateTime startTime = LocalDateTime.parse("2007-12-03T10:16:30");
		LocalDateTime endTime = LocalDateTime.parse("2007-12-03T10:17:30");
		BikeTrip testBikeTrip = sqlHandler.addBikeTrip(0, "Riccarton Mall",
			"Burnside High School", "Christchurch", startTime, endTime, "M", 5);
		assertEquals(5.0, testBikeTrip.getAvgRating(), 0);
		testBikeTrip.addRating(3);
		testBikeTrip.addRating(1);
		if (!sqlHandler.updateRatings(testBikeTrip)) {
			fail();
		}
		testBikeTrip = sqlHandler.queryBikeTripData(null).get(0);
		assertEquals(3, testBikeTrip.getRatings().size());
		assertEquals(3.0, testBikeTrip.getAvgRating(), 0);
	}

	@Test
	public void testUpdateOtherCoords() {
		LocalDateTime startTime = LocalDateTime.parse("2007-12-03T10:16:30");
		LocalDateTime endTime = LocalDateTime.parse("2007-12-03T10:17:30");
		BikeTrip testBikeTrip = sqlHandler.addBikeTrip(0, "Riccarton Mall",
			"Burnside High School", "Christchurch", startTime, endTime, "M", 5);
		double[] otherCoord = {140, 60};
		testBikeTrip.addOtherCoord(otherCoord);
		testBikeTrip.addOtherCoord(otherCoord);
		assertEquals(2, testBikeTrip.getOtherCoords().size());
		if (!sqlHandler.updateOtherCoords(testBikeTrip)) {
			fail();
		}
		testBikeTrip = sqlHandler.queryBikeTripData(null).get(0);
		assertEquals(2, testBikeTrip.getOtherCoords().size());
		assertEquals(140.0, testBikeTrip.getOtherCoords().get(1)[0], 0);
	}

	@Test
	public void testSetFavourite() {
		try {
			sqlHandler.importCSV("/TestData/20BikeTrips.csv", true, DataType.BIKETRIP);
			BikeTrip favTrip = sqlHandler.queryBikeTripData(null).get(0);
			sqlHandler.setFavourite(DataType.BIKETRIP, true, favTrip.getId());
			favTrip.setFavourite(true);
			BikeTrip favourite = sqlHandler.queryBikeTripData("favourite = 1").get(0);
			assertEquals(favourite.getId(), favTrip.getId());
		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void testRemoveEntry() {
		try {
			sqlHandler.importCSV("/TestData/20HotSpots.csv", true, DataType.HOTSPOT);
			ArrayList<HotSpot> beforeRemove = sqlHandler.queryHotSpotData(null);
			sqlHandler.removeEntry(DataType.HOTSPOT, beforeRemove.get(0).getId());
			ArrayList<HotSpot> afterRemove = sqlHandler.queryHotSpotData(null);
			assertEquals(beforeRemove.size(), afterRemove.size() + 1);
		} catch (IOException e) {
			fail();
		}
	}
}