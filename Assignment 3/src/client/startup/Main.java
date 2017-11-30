package client.startup;

import client.controller.ClientManager;
import client.view.Home;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		ClientManager viewManager = new ClientManager(primaryStage);
		Home home = new Home(viewManager);
		Scene homeScene = home.getScene();
		primaryStage.setScene(homeScene);
		primaryStage.show();
	}

}
