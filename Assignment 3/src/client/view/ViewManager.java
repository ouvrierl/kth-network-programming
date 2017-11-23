package client.view;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import common.catalog.CatalogServer;
import javafx.stage.Stage;

public class ViewManager {

	private CatalogServer server;
	private Stage stage;

	public ViewManager(Stage stage) throws MalformedURLException, RemoteException, NotBoundException {
		this.stage = stage;
		this.server = (CatalogServer) Naming.lookup("//localhost/" + CatalogServer.SERVER_NAME_IN_REGISTRY);
	}

	public CatalogServer getServer() {
		return this.server;
	}

	public Stage getStage() {
		return this.stage;
	}

}
