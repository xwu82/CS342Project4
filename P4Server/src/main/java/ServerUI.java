import java.util.HashMap;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ServerUI {
	
	private HashMap<String, Scene> sceneMap;
	private Stage primaryStage;
	private Server serverConnection;
	private ListView<String> listItems;
	
	public ServerUI(HashMap<String, Scene> sceneMap, Stage primaryStage) {
		this.sceneMap = sceneMap;
		this.primaryStage = primaryStage;
	}
	
	public Scene createServeScene() {
		sceneMap.put("ServerScene1", createScene1());
		return sceneMap.get("ServerScene1");
	}
	
	//------Scene 1
	public Scene createScene1() {
		//------Declare
		Text portText = new Text("Server Port:");
		portText.setFont(Font.font ("Verdana", 50));
		TextField portTextField = new TextField("5555");
		Button portEnterBtn = new Button("Enter");
		VBox portVBox = new VBox(40, portText, portTextField, portEnterBtn);
		portVBox.setPadding(new Insets(0,150,0,150));
		
		//------Layout
		portVBox.setAlignment(Pos.CENTER);
		
		//------Methods
		//the following function only accept user input integer
		portTextField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
		    if (!newValue.matches("\\d*")) {
		    	portTextField.setText(newValue.replaceAll("[^\\d]", ""));
		    }
		});
		portEnterBtn.setOnAction(e->{
			sceneMap.put("ServerScene2", createScene2());
			try {
				serverConnection = new Server(Integer.parseInt(portTextField.getText()), data -> {
					Platform.runLater(()->{
						listItems.getItems().add(data.toString());
					});
				});
				System.out.println("server port: " + portTextField.getText());
				primaryStage.setScene(sceneMap.get("ServerScene2"));
			} catch (NumberFormatException e2) {
				System.out.println("Port number format error");
			}
		});
		
		return new Scene(portVBox,600,600);
	}
	
	//------Scene 2
	private Scene createScene2() {
		listItems = new ListView<String>();
		
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(70));
		pane.setStyle("-fx-background-color: #AAFDFF");
		
		pane.setCenter(listItems);
	
		return new Scene(pane, 500, 400);
	}
}
