/**
 * 
 */
package main.java.control;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.java.common.CommonDefine;
import main.java.model.Stock;
import main.java.utility.AlertGenerator;
import main.java.utility.Screen;
import main.java.utility.Utility;
import yahoofinance.YahooFinance;

/**
 * @author doquocanh-macbook
 *
 */
public class HomeController extends ParentController implements Initializable {
	@FXML private AnchorPane homeAP;
	@FXML private JFXTreeTableView<Stock> stockTableView;
	
	@FXML private TreeTableColumn<Stock, String> stockCodeCol;
	@FXML private TreeTableColumn<Stock, String> companyCol;
	@FXML private TreeTableColumn<Stock, String> priceCol;
	@FXML private TreeTableColumn<Stock, String> lastPriceCol;
	@FXML private TreeTableColumn<Stock, String> changeCol;
	@FXML private TreeTableColumn<Stock, String> percentChangeCol;
//	@FXML private JFXTreeTableColumn<Integer, String> stockBuyCol;
	
	@FXML private JFXTextField searchTF;
	@FXML private StackPane sp;
	@FXML private JFXSpinner spinner;
	
	private Stock stock; // The selected stock for alert
	
	private ObservableList<Stock> stocks;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// List of 30 stocks that will be displayed in Home page
		final String[] stockSymbols = new String[] {"INTC", "AAPL", "GOOG", "YHOO", "XOM", "WMT",
													"TM", "KO", "HPQ", "FB", "F", "MSFT", 
													"BRK-A", "AMZN", "XOM", "JPM", "WFC", "GE",
													"BAC", "T", "BABA", "PG", "CVX", "V",
													"VZ", "HD", "DIS", "INTC", "ORCL", "HSBC"};
		
		RealTimeUpdateService service = new RealTimeUpdateService(stockSymbols);
		service.setPeriod(Duration.minutes(2));
		service.start();
		service.setOnSucceeded(event -> {
			System.out.println("Continue update...");
			stocks = service.getValue();
			 TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
			 stockTableView.setRoot(root);
			// Finish loading, hide spinner
			spinner.setVisible(false);
			System.out.println("Finish updating!");
		});
		
		// Initialize GUI
		stocks = FXCollections.observableArrayList();
		TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
		stockTableView.setRoot(root);
		stockTableView.setShowRoot(false);
		stockTableView.setEditable(true);
		// Set Cell Value Factory
		setCellValueStockCode();
		setCellValueCompanyName();
		setCellValueStockPrice();
		setCellFactoryLastPrice();
		setCellFactoryPriceChange();
		setCellFactoryPercentageChange();
		// Initialize context menu when user click right mouse on a row
		stockTableView.setRowFactory(ttv -> {
		    ContextMenu contextMenu = new ContextMenu();
		    MenuItem alertSettingsItem = new MenuItem("Alert Settings");
		    // ...
		    MenuItem removeItem = new MenuItem("Remove Stock");
		    // ...
		    contextMenu.getItems().addAll(alertSettingsItem, removeItem);
		    TreeTableRow<Stock> row = new TreeTableRow<Stock>() {
		        @Override
		        public void updateItem(Stock item, boolean empty) {
		            super.updateItem(item, empty);
		            if (empty) {
		                setContextMenu(null);
		            } else {
		                // configure context menu with appropriate menu items, 
		                // depending on value of item
		                setContextMenu(contextMenu);
		            }
		        }
		    };
		    alertSettingsItem.setOnAction(evt -> {
		        stock = row.getItem();
		        makeNewStage(Screen.ALERT_SETTINGS, "Alert Settings", "../view/AlertSettings.fxml");
		    });
		    // event handlers for other menu items...
		    removeItem.setOnAction(evt -> {
		    	// Display dialog warning users
		    	// when they try to remove an owned stock
		    	String stockCode = row.getItem().getStockCode();
		    	if (userStockManager.hasStock(user.getId(), stockCode)) {
		    		// Display alert to notify user before removing an owned stock
		    		Alert alert = AlertGenerator.generateAlert(AlertType.CONFIRMATION, CommonDefine.REMOVE_STOCK_SMS);
		    		Optional<ButtonType> result = alert.showAndWait();
		    		if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
		    			// User cancels removing
		    			return;
		    		}
		    	} else {
		    		// Do nothing, just remove selected stock
		    	}
		    	// Remove selected stock from table view
		    	TreeItem<Stock> treeItem = row.getTreeItem();
		    	treeItem.getParent().getChildren().remove(treeItem);
		    	stockTableView.getSelectionModel().clearSelection();
		    });
		    return row ;
		});
		
		// Add listener for search field
		searchTF.textProperty().addListener((o,oldVal,newVal)->{
			stockTableView.setPredicate(stock -> filterCriteria(stock, newVal));
		});
		
		// Add listener when user select a row
		stockTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
//				System.out.println("Current number of rows: " + stockTableView.getCurrentItemsCount());
				if (mouseEvent.getClickCount() == 2 && stockTableView.getCurrentItemsCount() > 0) { // Double click
					makeNewStage(Screen.STOCK_DETAILS, "Stock Details", "../view/StockDetails.fxml");
//					TreeItem<Stock> item = stockTableView.getSelectionModel().getSelectedItem();
//					System.out.println("Selected stock: " + item.getValue().getStockName());
				}
			}
			
		});
	}
	
	@Override
	protected void makeNewStage(Screen target, String stageTitle, String url) {
		Stage newStage = new Stage();
		newStage.setTitle(stageTitle);
		FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
		Parent root = null;
		try {
			root = (Parent)loader.load();
		} catch (IOException e) {
        	System.err.println("Could not load url: " + url);
        	e.printStackTrace();
        	return;
        }
		switch(target) {
			case ALERT_SETTINGS:
				AlertSettingsController alertSettingsController = loader.<AlertSettingsController>getController();
				alertSettingsController.setUser(user);
				alertSettingsController.initAlertSettings(stock);
				break;
			case PORFOLIO:
				PortfolioController portfolioController = loader.<PortfolioController>getController();
				portfolioController.setUser(user);
				portfolioController.initPortfolio();
				portfolioController.initTransactionHistory();
				break;
			case SETTINGS:
				// Set current user
				SettingsController settingsController = loader.<SettingsController>getController();
				settingsController.setUser(user);
				settingsController.initUserInfo();
				break;
			case STOCK_DETAILS:
				StockDetailsController stockController = loader.<StockDetailsController>getController();
				stockController.setUser(user);
				// Find selected stock in Java API
				TreeItem<Stock> item = stockTableView.getSelectionModel().getSelectedItem();
				// TODO: Think about better way to find the stock given stock code
				yahoofinance.Stock yahooStock = null;
				try {
					yahooStock = YahooFinance.get(item.getValue().getStockCode(), false);
				} catch (IOException e) {
					System.err.println("Stock code is invalid: " + item.getValue().getStockCode());
					e.printStackTrace();
					return;
				}
				stockController.setStock(yahooStock);
				stockController.updateStockData();
				break;
			default:
				return;
		}
		
		newStage.setScene(new Scene(root));
		newStage.setResizable(false);
		newStage.show();
	}
	
	/**
	 * Open new screen when user select [Settings] on Menu bar
	 * @param event Capture the action user performed. 
	 */
	@FXML private void openAccountSettings(ActionEvent event) {
		makeNewStage(Screen.SETTINGS, "Settings", "../view/Settings.fxml");
	}
	
	@FXML private void openPorfolio(ActionEvent event) {
		makeNewStage(Screen.PORFOLIO, "My Porfolio", "../view/Portfolio.fxml");
	}

	/**
	 * <p>
	 * Filter stock based on stock code or company name. It searches if stock code or company name 
	 * contains search query.
	 * </p>
	 * @param stock
	 * @param value
	 * @return <code>True</code> if matched, otherwise return <code>False</code> 
	 */
	private boolean filterCriteria(TreeItem<Stock> stock, String value) {
		return stock.getValue().getStockCode().toLowerCase().contains(value.toLowerCase()) ||
				stock.getValue().getStockName().toLowerCase().contains(value.toLowerCase());
	}
	
	private void setCellValueStockCode() {
		stockCodeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getStockCode()));
	}
	
	private void setCellValueCompanyName() {
		companyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getStockName()));
	}
	
	private void setCellValueStockPrice() {
		priceCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceString()));
	}
	
	private void setCellFactoryLastPrice() {
		lastPriceCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPreviousPriceString()));
	}
	
	private void setCellFactoryPriceChange() {
		changeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceChangeString()));
	}
	
	private void setCellFactoryPercentageChange() {
		percentChangeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPriceChangePercentString()));
		//setPriceFormatColumn(percentChangeCol);
	}
	
	/**
	 * <p>
	 * Scheduled service to automatically download real-time stock information.
	 * Default downloading time interval is 2 minutes. 
	 * </p>
	 * @author doquocanh-macbook
	 *
	 */
	private static class RealTimeUpdateService extends ScheduledService<ObservableList<Stock>> {
		String[] stockSymbols;
		
		public RealTimeUpdateService(String[] stockSymbols) {
			this.stockSymbols = stockSymbols;
		}
		
//		public void setStockSymbols(String[] stockSymbols) {
//			this.stockSymbols = stockSymbols;
//		}
		
		@Override
		protected Task<ObservableList<Stock>> createTask() {
			return new Task<ObservableList<Stock>>() {
				@Override
				protected ObservableList<Stock> call() throws IOException {
					 return Utility.getMultipleStockData(stockSymbols);
				}
			};
		}
	}
}
