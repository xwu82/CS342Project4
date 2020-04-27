import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class WordGuessClient extends Application {
	
	private HashMap<String, Scene> sceneMap;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		sceneMap = new HashMap<String, Scene>();
		
		//create Scene
		ClientUI clientUI = new ClientUI(sceneMap, primaryStage);
		sceneMap.put("clientUI", clientUI.createServeScene());
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
		
		primaryStage.setTitle("(Client) Word Guess!!!");
		primaryStage.setScene(sceneMap.get("clientUI"));
		primaryStage.show();
	}

}
