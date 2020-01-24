package seng202.team4.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;

/**
 *Help controller for the help window. Inititialized some GUI elements
 */
public class HelpController {

	/**
	 * Initialize all fxml callers
	 *
	*/
	@FXML
	public AnchorPane helpPane = new AnchorPane();
	@FXML
	public Accordion helpAccordion = new Accordion();
	@FXML
	public TitledPane planRouteTitledPane = new TitledPane();
	@FXML
	public TitledPane viewDataTitledPane = new TitledPane();
	@FXML
	public TitledPane favoritesTitledPane = new TitledPane();
	@FXML
	public TitledPane importDataTitledPane = new TitledPane();
	@FXML
	public TitledPane returnHomePageTitledPane = new TitledPane();
	@FXML
	public TitledPane createDatabaseTitledPane = new TitledPane();
	@FXML
	public TitledPane chooseDatabaseTitledPane = new TitledPane();
	@FXML
	public TextField searchField = new TextField();
	@FXML
	public Button searchButton = new Button();
}
