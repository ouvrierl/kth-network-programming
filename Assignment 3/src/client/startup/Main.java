package client.startup;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import client.view.Home;
import client.view.ViewManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		ViewManager viewManager = new ViewManager(primaryStage);
		Home home = new Home(viewManager);
		Scene homeScene = home.getScene();
		primaryStage.setScene(homeScene);
		primaryStage.show();
	}

}
