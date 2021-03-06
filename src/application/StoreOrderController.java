package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class StoreOrderController implements Initializable{

	private MainScreenController mainController;
	

    @FXML
    private TextField TotalAmountOfOrder;

    @FXML
    private ComboBox<String> OrderNumberComboBox;

    @FXML
    private TextArea logger;

    @FXML
    private ListView<String> PrintArea;

    private final ObservableList<String> numberOfOrderList = FXCollections.observableArrayList();
    private final ObservableList<String> orderItemList = FXCollections.observableArrayList(); 
    
    
    
    @FXML
    void OrderNumberComboBoxChange(ActionEvent event) {
    	if(!numberOfOrderList.isEmpty()) {
    		int index = OrderNumberComboBox.getSelectionModel().getSelectedIndex();
    		PrintOrderInfo(index);
    	}
    }
	
    private void PrintOrderInfo(int index) {
    	CafeHandler handler = mainController.getCafeHandler();
    	ObservableList<Order> orderList = handler.getStoreOrderList();
    	
    	if (orderList.isEmpty()) {
    		logger.appendText("There is not store order place. Store order is empty.\n");
    		return;
    	}
    	
    	if ((index < 0) || (index > orderList.size())) {
    		logger.appendText("Order Number is out of range.\n");
    		return;
    	}
    	Order order = orderList.get(index);
    	ObservableList<MenuItem> orderItems = order.getObserveOrderList();
		
		if (orderItems.isEmpty()) {
			logger.appendText("Order has no information. There is somthing wrong.\n");
			return;
		}
		
		orderItemList.clear();
		
		for(int orderindex = 0; orderindex < orderItems.size();orderindex++) {
			MenuItem Items  = orderItems.get(orderindex);
			for(int itemIndex = 0; itemIndex < Items.getNumberItems();itemIndex++) {
				orderItemList.add(Items.toString(itemIndex));
			}
		}
		if (!orderItemList.isEmpty()) {
			PrintArea.getSelectionModel().selectFirst();
		}
		
		TotalAmountOfOrder.setText(handler.getTwoUpToTwoDecimalPoint(order.getTotalPrice()));
    }
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		OrderNumberComboBox.setItems(numberOfOrderList);
		PrintArea.setItems(orderItemList);
	}
	

    @FXML
    void CancelOrderButtonPress(ActionEvent event) {
    	if(!numberOfOrderList.isEmpty()) {
    		int index = OrderNumberComboBox.getSelectionModel().getSelectedIndex();
    		CafeHandler handler = mainController.getCafeHandler();
    		if (handler.RemoveOrder(index)) {
    			logger.appendText("Order is Canceled Successfully.\n");
    		}
    	}
    }

    @FXML
    void ExportOrdersButtonPress(ActionEvent event) {
    	FileChooser chooser = new FileChooser();
    	chooser.setTitle("Save Target File for Export");
    	chooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files","*.txt"),
    			new ExtensionFilter("All Files","*.*"));
    	Stage stage = new Stage();
    	File targetFile = chooser.showSaveDialog(stage);
    	
    	if (targetFile == null) {
    		return;
    	}

    	if (!targetFile.exists()) {
    		try {
    			targetFile.createNewFile();
    			
    		} catch (IOException e) {
    			logger.appendText("File is not found exception generated. While creating new file.\n");
				return;
			}
    		catch(Exception e){
    			logger.appendText("File exception is generated. Please retry export the file. \n");
    			return;
    		}
    	}
    	writeDateBaseToFile(targetFile.getPath());
    }
    
    private void writeDateBaseToFile(String filePath) {
        PrintWriter fw = null;
        BufferedWriter bw  = null;
        try {
            fw = new PrintWriter(filePath);
            bw = new BufferedWriter(fw);
            CafeHandler handler = mainController.getCafeHandler();
            if (!handler.getStoreOrderList().isEmpty()) {
            	bw.write(handler.exportDatabase());
            	logger.appendText("Store orderList  is write successful in file.\n");
            }
            else {
            	bw.write("Store orderlist is empty.");
            	logger.appendText("Store orderlist is empty. Empty file is generated.\n");
            }
        } catch (IOException e) {
        	logger.appendText("Error is generated while writting the file.\n");
        }
        finally { 
    	   try{
    	      if(bw!=null) {
    	    	  bw.close();
    	      }
    	      if (fw != null) {
    	    	  fw.close();
    	      }
    	   }catch(Exception ex){
    		   logger.appendText("Error in closing the BufferedWriter.\n");
    	    }
    	}
    }
    

	private void initValues() {
		CafeHandler handler = mainController.getCafeHandler();
		
		numberOfOrderList.clear();
		ObservableList<Order> storeorderList = handler.getStoreOrderList();
		for(int index = 0; index < storeorderList.size();index++) {
			numberOfOrderList.add(String.valueOf(storeorderList.get(index).getUniqueOrderNumber()));
		}
		
		if (!numberOfOrderList.isEmpty()) {
			OrderNumberComboBox.getSelectionModel().selectFirst();
			PrintOrderInfo(0);
		}
		else {
			orderItemList.clear();
			TotalAmountOfOrder.setText(handler.getTwoUpToTwoDecimalPoint(0));
		}
	}
	
	public void setMainController(MainScreenController controller) {
		mainController = controller;
		
		initValues();
		
		CafeHandler handler = mainController.getCafeHandler();
		handler.getStoreOrderList().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable arg0) {
				initValues();
			}
	    });
	}
	
}
