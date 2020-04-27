import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class WordGuessServer extends Application {
	
	private HashMap<String, Scene> sceneMap;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		sceneMap = new HashMap<String, Scene>();
		
		//create Scene
		ServerUI serverUI = new ServerUI(sceneMap, primaryStage);
    	sceneMap.put("serverUI", serverUI.createServeScene());
    	
    	primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setTitle("(server) Playing word guess!!!");
		primaryStage.setScene(sceneMap.get("serverUI"));
		primaryStage.show();
	}

}
