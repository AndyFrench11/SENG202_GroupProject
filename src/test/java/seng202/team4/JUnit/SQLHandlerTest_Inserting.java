package seng202.team4.JUnit;

import org.apache.commons.dbutils.DbUtils;
import org.junit.*;
import org.junit.rules.ExpectedException;
import seng202.team4.Model.BikeTrip;
import seng202.team4.Model.DataType;
import seng202.team4.Model.HotSpot;
import seng202.team4.Model.Location;
import seng202.team4.Model.Main;
import seng202.team4.Model.Retailer;
import seng202.team4.Services.SQLHandler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;

import static java.sql.DriverManager.getConnection;
import static org.junit.Assert.*;

public class SQLHandlerTest_Inserting {

	private static String jarDir;
	private SQLHandler sqlHandler;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

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
	}

	@After
	public void tearDown() {
		if (!sqlHandler.deleteDatabase()) {
			fail();
		}
	}

	@Test
	public void testCreateDatabase() {
		if (!sqlHandler.createDatabase()) {
			fail();
		}
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		boolean pass = true;
		try {
			conn = getConnection(
				String.format("jdbc:sqlite:%s%s%s", jarDir, File.separatorChar, "testDb"));
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM sqlite_master;");
			String[] correctTables = {
				"table BikeTrips BikeTrips", "index tripID BikeTrips",
				"table HotSpots HotSpots", "index hotSpotID HotSpots",
				"table Retailers Retailers", "index retailerID Retailers",
				"table Locations Locations", "index locationID Locations"
			};
			for (int i = 0; i < 8; i++) {
				rs.next();
				if (!String.format("%s %s %s", rs.getString(1), rs.getString(2), rs.getString(3))
					.equals(correctTables[i])) {
					pass = false;
				}
			}

		} catch (SQLException e) {
			pass = false;
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
		assertTrue(pass);
	}

	//CSV import tests

	@Test
	public void testBikeTripImport() {
		try {
			sqlHandler.createDatabase();
			sqlHandler.importCSV("/TestData/750BikeTrips.csv", true, DataType.BIKETRIP);
			assertEquals(750, sqlHandler.queryBikeTripData(null).size());
		} catch (IOException e) {
			fail("Could not read the test bike trip data.");
		}
	}

	@Test
	public void testHotSpotImport() {
		try {
			sqlHandler.createDatabase();
			sqlHandler.importCSV("/TestData/750HotSpots.csv", true, DataType.HOTSPOT);
			assertEquals(688, sqlHandler.queryHotSpotData(null).size());
		} catch (IOException e) {
			fail("Could not read the test hot spot data.");
		}
	}

	@Ignore //Because it uses 60 geocodes
	@Test
	public void testRetailerImport() {
		try {
			sqlHandler.createDatabase();
			sqlHandler.importCSV("/TestData/750Retailers.csv", true, DataType.RETAILER);
			while (Main.getTotalGeocodesToDo() != 0) {
				Thread.sleep(100);
			}
			assertEquals(750, sqlHandler.queryRetailerData(null).size());
		} catch (IOException | InterruptedException e) {
			fail("Could not read the test retailer data.");
		}
	}

	@Test
	public void testInvalidCSVName() {
		exception.expect(NullPointerException.class);
		try {
			sqlHandler.createDatabase();
			sqlHandler.importCSV("blork", true, DataType.BIKETRIP);
		} catch (IOException e) {
			fail();
		}
	}

	//Add entry tests

	@Test
	public void testAddBikeTrip() {
		sqlHandler.createDatabase();
		LocalDateTime startTime = LocalDateTime.parse("2007-12-03T10:16:30");
		LocalDateTime endTime = LocalDateTime.parse("2007-12-03T10:17:30");
		sqlHandler.addBikeTrip(12345, "Burnside High School", "Bush Inn Centre",
			"Christchurch", startTime, endTime, "M", 4);
		BikeTrip added = sqlHandler.queryBikeTripData(null).get(0);
		assertEquals(12345, added.getBikeId());
		assertEquals(added.getStartTime(), startTime);
		assertEquals(added.getEndTime(), endTime);
		assertEquals("M", added.getGender());
	}

	@Test
	public void testAddHotSpot() {
		sqlHandler.createDatabase();
		sqlHandler.addHotSpot("Test", "Christchurch", "Canterbury",
			"University of Canterbury", "Outdoor", "Free", "", "");
		HotSpot added = sqlHandler.queryHotSpotData(null).get(0);
		assertEquals("Test", added.getName());
		assertEquals("Christchurch", added.getCity());
		assertEquals("Canterbury", added.getBorough());
		assertEquals("University of Canterbury", added.getLocationInfo());
		assertEquals("Outdoor", added.getLocationType());
		assertEquals("Free", added.getPolicy());
	}

	@Test
	public void testAddRetailer() {
		sqlHandler.createDatabase();
		sqlHandler.addRetailer("Riccarton Mall", "Christchurch", "Riccarton Road",
			"Canterbury", 8053, "Shopping", "Mall", 12, 20);
		Retailer added = sqlHandler.queryRetailerData(null).get(0);
		assertEquals("Riccarton Mall", added.getName());
		assertEquals("Christchurch", added.getCity());
		assertEquals("Riccarton Road", added.getStreet());
		assertEquals("Canterbury", added.getState());
		assertEquals(8053, added.getZipCode());
		assertEquals("Shopping", added.getPrimaryType());
		assertEquals("Mall", added.getSecondaryType());
		assertEquals(12, added.getBlock());
		assertEquals(20, added.getLot());
	}

	@Test
	public void testAddLocation() {
		sqlHandler.createDatabase();
		sqlHandler.addLocation("Riccarton Mall", "Christchurch");
		Location added = sqlHandler.queryLocationData(null).get(0);
		assertEquals("Riccarton Mall", added.getName());
		assertEquals("Christchurch", added.getCity());
	}

}