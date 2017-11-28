package server.startup;

import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.exception.ConnectionException;
import server.controller.Controller;
import server.net.ClientHandler;

public class Main implements Runnable {

	private static final int PORT = 8080;

	private Controller controller;

	public static void main(String[] args) {
		try {
			Main main = new Main();
			main.startRegistry();
			main.controller = new Controller();
			Naming.rebind("rmi://localhost:" + Registry.REGISTRY_PORT + "/" + Controller.SERVER_NAME_IN_REGISTRY,
					main.controller);
			new Thread(main).start();
		} catch (Exception e) {
			e.printStackTrace();
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

	@Override
	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(PORT);) {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				ClientHandler clientHandler = new ClientHandler(clientSocket);
				this.controller.newClientHandler(clientHandler);
				new Thread(clientHandler).start();
			}
		} catch (Exception e) {
			throw new ConnectionException("Error during server launch on port " + PORT);
		}
	}

}
