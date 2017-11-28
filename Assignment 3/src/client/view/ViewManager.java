package client.view;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import client.controller.Controller;
import common.catalog.CatalogServer;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ViewManager {

	private static final int PORT = 8080;
	private static final String ADDRESS = "localhost";

	private CatalogServer server;
	private CatalogServer serverReader;
	private Stage stage;
	private Controller controller;

	public ViewManager(Stage stage) throws MalformedURLException, RemoteException, NotBoundException {
		this.server = (CatalogServer) Naming.lookup("rmi://localhost/" + CatalogServer.SERVER_NAME_IN_REGISTRY);
		this.serverReader = new ServerReader();
		this.server.incomingClient(this.serverReader);
		this.controller = new Controller();
		this.controller.connect(ADDRESS, PORT);
		this.stage = stage;
		this.initStage();
	}

	public CatalogServer getServer() {
		return this.server;
	}

	public CatalogServer getServerReader() {
		return this.serverReader;
	}

	public Stage getStage() {
		return this.stage;
	}

	public Controller getController() {
		return this.controller;
	}

	public void initStage() {
		this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				controller.disconnect();
			}
		});
	}

}
