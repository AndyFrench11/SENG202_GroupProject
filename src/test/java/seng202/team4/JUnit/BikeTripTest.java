package seng202.team4.JUnit;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import seng202.team4.Model.BikeTrip;

import static junit.framework.TestCase.assertEquals;

public class BikeTripTest {

	private BikeTrip testBikeTrip;

	@Before
	public void testSetUp() {
		LocalDateTime startTime = LocalDateTime.parse("2007-12-03T10:16:30");
		LocalDateTime endTime = LocalDateTime.parse("2007-12-03T10:17:30");
		testBikeTrip = new BikeTrip(0, 0, 37.421200, -122.077264, 37.421981,
			-122.085104, new ArrayList<>(), startTime, endTime,
			ChronoUnit.MINUTES.between(startTime, endTime), "M", 10, 1, 5, 0,
			new ArrayList<>(), false);
	}

	@Test
	public void testAddRating() {
		testBikeTrip.addRating(3);
		assertEquals(3.0, testBikeTrip.getAvgRating());
		testBikeTrip.addRating(5);
		assertEquals(4.0, testBikeTrip.getAvgRating());
		testBikeTrip.addRating(2);
		testBikeTrip.addRating(1);
		assertEquals(2.75, testBikeTrip.getAvgRating());
	}

	@Test
	public void testCalcStats() {
		String update = testBikeTrip.calcStats();
		assertEquals(57.0, testBikeTrip.getAvgSpeed());
		assertEquals(6.628385416666666, testBikeTrip.getCaloriesBurnt());
		assertEquals("distance = 0.95, avgSpeed = 57.00, caloriesBurnt = 6.63",
			update);
		testBikeTrip.addOtherCoord(new double[]{37.141200, -122.057264});
		testBikeTrip.calcStats();
		assertEquals(104.987, testBikeTrip.getDistance());
		testBikeTrip.addOtherCoord(new double[]{37.241200, -122.027264});
		testBikeTrip.calcStats();
		assertEquals(108.129, testBikeTrip.getDistance());
	}
}
