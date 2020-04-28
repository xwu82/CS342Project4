import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ClientUI {
	
	private HashMap<String, Scene> sceneMap;
	private ListView<String> listItems;
	private HBox letters;
	private ArrayList<Button> buttons;
	private Stage primaryStage;
	private Client clientConnection;
	private static int MaxLetters = 12;
	private int playerID, startPosition;
	private GameInfo roundGameInfo = new GameInfo();	//use to keep track every round guess

	public ClientUI(HashMap<String, Scene> sceneMap, Stage primaryStage) {
		this.sceneMap = sceneMap;
		this.primaryStage = primaryStage;
	}
	
	public Scene createServeScene() {
		sceneMap.put("ClientScene1", createScene1());
		return sceneMap.get("ClientScene1");
	}

	//------Client Scene 1
	private Scene createScene1() {
		//------Declare
		listItems = new ListView<String>();
		Text ipText = new Text("Server IP: ");
		Text portText = new Text("Client Port:");
		ipText.setFont(Font.font ("Verdana", 50));
		portText.setFont(Font.font ("Verdana", 50));
		TextField ipTextField = new TextField("127.0.0.1");
		TextField portTextField = new TextField("5555");
		Button portEnterBtn = new Button("Enter");
		
		//------Layout
		VBox portVBox = new VBox (40, ipText, ipTextField, portText, portTextField, portEnterBtn);
		portVBox.setPadding(new Insets(0,150,0,150));
		portVBox.setAlignment(Pos.CENTER);
		
		//------Methods
		//the following function only accept user input integer
		portTextField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
		    if (!newValue.matches("\\d*")) {
		    	portTextField.setText(newValue.replaceAll("[^\\d]", ""));
		    }
		});
		
		portEnterBtn.setOnAction(e->{
			try {
				clientConnection = new Client(ipTextField.getText(), Integer.parseInt(portTextField.getText()), data->{
					Platform.runLater(()->{
						roundGameInfo = (GameInfo) data;
						String s = roundGameInfo.message;
						listItems.getItems().add(s);
						parseCallback(roundGameInfo);
					});
				});
				clientConnection.start();
				System.out.println("client port: " + portTextField.getText());
				sceneMap.put("ClientScene2", createScene2());
				primaryStage.setScene(sceneMap.get("ClientScene2"));
				
			} catch (NumberFormatException e2) {
				System.out.println("Port number format error");
			}
		});
		
		return new Scene(portVBox,600,600);
	}
	
	//Client scene2: choose one of categories
	private Scene createScene2() {
		
		//create drop down menu
		ObservableList<String> options = 
		    FXCollections.observableArrayList(
		        "Foods",
		        "Animals",
		        "U.S. States"
		    );
		final ComboBox<String> comboBox = new ComboBox<String>(options);
		comboBox.setStyle("-fx-pref-width: 150;");
		//set first category default
		comboBox.getSelectionModel().selectFirst();
		
		Text text = new Text("Please select one category:");
		text.setFont(Font.font ("Verdana", 50));
		Button enterBtn = new Button("Enter");
		
		//------Layout
		VBox vbox = new VBox(30, comboBox, enterBtn);
		vbox.setAlignment(Pos.CENTER);
		
		//------Methods
		enterBtn.setOnAction(e->{
			String category = comboBox.getSelectionModel().getSelectedItem().toString();
			clientConnection.send(playerID, category);
			listItems.getItems().add("Your category is " + category);
			sceneMap.put("ClientScene3", createScene3());
			primaryStage.setScene(sceneMap.get("ClientScene3"));
		});
		
		return new Scene(vbox, 600, 600);
	}
	
	//main scene for playing game
	private Scene createScene3() {
		//------Declare
		//Maximum word letter is 12
		letters = new HBox(20);
		buttons = new ArrayList<Button>();
		for (int i = 0; i < MaxLetters; i++) {
			Button btn = new Button("_");
			btn.setVisible(false);
			letters.getChildren().add(btn);
			buttons.add(btn);
		}
		Button sendBtn = new Button("Send");
		TextField textField = new TextField();
		VBox vbox = new VBox(10, textField, sendBtn);
		vbox.setAlignment(Pos.CENTER_RIGHT);
		Text gameInfo = new Text("Game Info: ");
		VBox vbox2 = new VBox(10, gameInfo, listItems);
		HBox buttomLayout = new HBox(100, vbox2, vbox);
		ImageView backgroundImageView = new ImageView("background.jpeg");
		backgroundImageView.setPreserveRatio(true);
		backgroundImageView.setFitWidth(1000);
		Group group = new Group();
		Scene scene = new Scene(group);
		
		//------Layout
		letters.setLayoutX(20);
		letters.setLayoutY(20);
		listItems.setPrefHeight(200);
		listItems.setPrefWidth(280);
		buttomLayout.setLayoutX(190);
		buttomLayout.setLayoutY(200);
		group.getChildren().addAll(backgroundImageView, letters, buttomLayout);
		
		//------Method
		gameInfo.setStyle("-fx-font-size: 32px;" + 
				"-fx-font-family: \"times\";" + 
				"-fx-fill: black;");
		//set textfield can only take one char
		textField.setTextFormatter(new TextFormatter<String>((Change change) -> {
		    String newText = change.getControlNewText();
		    if (newText.length() > 1) {
		        return null ;
		    } else {
		        return change ;
		    }
		}));
		sendBtn.getStylesheets().add
 			(ClientUI.class.getResource("sendBtn.css").toExternalForm());
		sendBtn.setOnAction(e->{
			if(!textField.getText().trim().isEmpty()) {
				clientConnection.send(playerID, Character.toLowerCase(textField.getText().charAt(0)));
				textField.clear();
			}
		});
		letters.setMouseTransparent(true);
		letters.getStylesheets().add
	 		(ClientUI.class.getResource("letters.css").toExternalForm());
		
		return scene;
	}
	
	//After player win or lose, let then choose restart or quit
	private Scene createScene4(boolean result) {
		
		Button restart = new Button("Restart");
		Button quit = new Button("Quit");
		HBox layout = new HBox(20, restart, quit);
		layout.setAlignment(Pos.CENTER);
		
		//------Methods
		sceneMap.remove("ClientScene4");	//remove this scene from scene map, because every time generating this scene won't cause hashmap collision
		restart.setOnAction(e->{
			//TODO: reset number of guesses and total number of categories win
			primaryStage.setScene(sceneMap.get("ClientScene2"));
		});
		quit.setOnAction(e->{
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	            @Override
	            public void handle(WindowEvent t) {
	                Platform.exit();
	                System.exit(0);
	            }
	        });
		});
		return new Scene(layout, 600, 600);
	}

	private void parseCallback(GameInfo gameInfo) {
		String s = gameInfo.message;
		if (s.contains("You are player")){
			playerID = Integer.valueOf(s.substring(s.length() - 1, s.length()));
			System.out.println("This client is player " + playerID);
		}
		else if(s.contains("Your word length")) {
			int length = Integer.parseInt(s.substring(s.length() - 1, s.length()));
			updateLetters(length);
		}
		else if(gameInfo.message.contains("correctly guessed") && gameInfo.positions.size() != 0) {
			updateLetters(gameInfo.letter, gameInfo.positions);
		}
	}
	
	private void updateLetters(char letter, ArrayList<Integer> positions) {
		for(int i : positions) {
			buttons.get(i + startPosition).setText(String.valueOf(letter));
		}
		//after update matching letters on UI, check if word complete next
		checkWordComplete();
	}

	private boolean checkWordComplete() {
		for (Node n : letters.getChildren()) {
			//convert Node to Button
			if (n instanceof Button) {
		        final Button button = (Button) n;
		        if(((Button) n).getText().compareTo("_") == 0) {
		        	return false;
		        }
		    }
		}
		categoryWin();
		return true;
	}

	private void categoryWin() {
		sceneMap.put("ClientScene4",createScene4(true));
		primaryStage.setScene(sceneMap.get("ClientScene4"));
		
	}
	
	private void categoryLose() {
		sceneMap.put("ClientScene4",createScene4(false));
		primaryStage.setScene(sceneMap.get("ClientScene4"));
	}

	private void updateLetters(int length) {
		int mid = MaxLetters/2;
		startPosition = mid - (length/2);
		int endPosition = mid + (length - (length/2));
		for(int i = startPosition; i < endPosition; i++) {
			letters.getChildren().get(i).setVisible(true);
		}
	}
}
