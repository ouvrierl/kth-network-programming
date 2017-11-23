package server.startup;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import server.controller.Controller;

public class Main {

	public static void main(String[] args) {
		try {
			new Main().startRegistry();
			Naming.rebind(Controller.SERVER_NAME_IN_REGISTRY, new Controller());
		} catch (Exception e) {
			System.err.println("Error while launching catalog server.");
		}
	}

	private void startRegistry() throws RemoteException {
		try {
			LocateRegistry.getRegistry().list();
		} catch (RemoteException noRegistryIsRunning) {
			LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		}
	}

}
