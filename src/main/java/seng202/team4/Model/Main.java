package seng202.team4.Model;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import seng202.team4.Controller.LoadWindowController;
import seng202.team4.Controller.MainController;
import seng202.team4.Services.AlertDialog;
import seng202.team4.Services.SQLHandler;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Main class which controls application startup and stores SQLControllers for use by other
 * controllers, as well as storing information about progress of geocoding operations if they are
 * happening.
 */
public class Main extends Application {

	private static String jarDir;
	public static ArrayList<SQLHandler> databases = new ArrayList<>();
	private static int currentDbIndex = 0;
	private static DataType tabState = DataType.BIKETRIP;
	private static int geocodesComplete = 0;
	private static int totalGeocodesToDo = 0;
	private static LoadWindowController loadingController = null;

	public static DataType getTabState() {
		return tabState;
	}

	public static ArrayList<SQLHandler> getDatabases() {
		return databases;
	}

	public static int getTotalGeocodesToDo() {
		return totalGeocodesToDo;
	}

	public static int getCurrentDbIndex() {
		return currentDbIndex;
	}

	public static void setTabState(DataType newState) {
		tabState = newState;
	}

	public static void setCurrentDbIndex(int newIndex) {
		currentDbIndex = newIndex;
	}

	/**
	 * Sets the total amount of geocodes to do at the start of a geocoding operation.
	 *
	 * @param totalGeocodesToDo The number of geocodes to do
	 */
	public static void setTotalGeocodesToDo(int totalGeocodesToDo) {
		Main.totalGeocodesToDo = totalGeocodesToDo;
		if (totalGeocodesToDo > 0 && loadingController != null) {
			loadingController.startDialog(totalGeocodesToDo);
		}
	}

	/**
	 * Increment the number of geocodes complete, reset the count if all are complete.
	 */
	public static void incrementGeocodesComplete() {
		geocodesComplete++;
		if (loadingController != null) {
			loadingController.updateDialog(geocodesComplete);
		}
		System.out.println(String.format("%d/%d entries geocoded.",
			geocodesComplete, totalGeocodesToDo));
		if (geocodesComplete == totalGeocodesToDo) {
			geocodesComplete = 0;
			totalGeocodesToDo = 0;
			if (loadingController != null) {
				loadingController.finishDialog();
				MainController.getRetailList().setAll(FXCollections.observableArrayList(
					getCurrentDb().queryRetailerData(null)));
			}
		}
	}

	/**
	 * Get the currently selected database.
	 *
	 * @return The currently selected database
	 */
	public static SQLHandler getCurrentDb() {
		return databases.get(currentDbIndex);
	}

	/**
	 * Add a new SQLHandler and initialize it.
	 *
	 * @param name The name of the new database
	 */
	public static void addDatabase(String name) {
		databases.add(new SQLHandler(jarDir, name));
		databases.get(databases.size() - 1).createDatabase();
	}

	/**
	 * Starts the JavaFX gui. Loads databases in the jar directory, or creates a new database if
	 * none were found.
	 *
	 * @param primaryStage The stage associated with the main window
	 * @throws Exception TODO add documentation
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		//initialize the databases
		try {
			jarDir = Paths
				.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI())
				.getParent().toString();

			File[] files = new File(jarDir).listFiles();

			if (files != null) {
				String fileName;
				String extension;
				for (File file : files) {
					fileName = file.getName();
					int i = fileName.lastIndexOf('.');
					if (i > 0) {
						extension = fileName.substring(i + 1);
						if (extension.equals("rmdb")) {
							addDatabase(fileName);
						}
					}
				}
			}
			if (databases.size() == 0) {
				System.out.println("No databases were found.");
				addDatabase("Default.rmdb");
			}
			Parent root = FXMLLoader.load(getClass().getResource("/View/Main.fxml"));
			primaryStage.setTitle("RouteMe");
			primaryStage.getIcons()
				.add(new Image(getClass().getResourceAsStream("/Images/Icons/AppIcon.png")));
			primaryStage.setScene(new Scene(root, 1200, 650));
			primaryStage.show();

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/LoadingDialog.fxml"));
			Parent loadRoot = loader.load();
			loadingController = loader.getController();
			Stage loadingStage = new Stage();
			loadingController.setStage(loadingStage);
			loadingStage.setTitle("Loading Data");
			loadingStage.setScene(new Scene(loadRoot, 454, 135));
			AlertDialog.showInformation("Welcome again!","Currently, there is nothing to display on the map.",
					"Please go to 'Plan a Route' to add a Route to the Map or a Location" +
							" \nand enjoy our application!");

		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
			System.out.println("Cannot access protection domain.");
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}